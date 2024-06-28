import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PokemonAgent extends Agent {
    private String tipo;
    private Point posicion;
    private Random random;
    private static final Map<String, Map<String, Double>> probabilidades = new HashMap<>();

    static {
        // Inicializa la tabla de probabilidades
        Map<String, Double> fuegoProb = new HashMap<>();
        fuegoProb.put("fuego", 0.5);
        fuegoProb.put("agua", 0.2);
        fuegoProb.put("electric", 0.7);
        fuegoProb.put("planta", 0.8);
        probabilidades.put("fuego", fuegoProb);

        Map<String, Double> aguaProb = new HashMap<>();
        aguaProb.put("fuego", 0.8);
        aguaProb.put("agua", 0.5);
        aguaProb.put("electric", 0.3);
        aguaProb.put("planta", 0.7);
        probabilidades.put("agua", aguaProb);

        Map<String, Double> electricProb = new HashMap<>();
        electricProb.put("fuego", 0.3);
        electricProb.put("agua", 0.8);
        electricProb.put("electric", 0.5);
        electricProb.put("planta", 0.6);
        probabilidades.put("electric", electricProb);

        Map<String, Double> plantaProb = new HashMap<>();
        plantaProb.put("fuego", 0.2);
        plantaProb.put("agua", 0.3);
        plantaProb.put("electric", 0.4);
        plantaProb.put("planta", 0.5);
        probabilidades.put("planta", plantaProb);
    }


    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length == 3 && args[0] instanceof String && args[1] instanceof Integer && args[2] instanceof Integer) {
            tipo = (String) args[0];
            posicion = new Point((int) args[1], (int) args[2]);
        } else {
            // Si los argumentos no son válidos, terminar el agente
            System.out.println("Argumentos inválidos para el agente Pokémon.");
            doDelete();
            return;
        }
        random = new Random();


        ACLMessage initialPosMsg = new ACLMessage(ACLMessage.INFORM);
        initialPosMsg.addReceiver(new AID("gui", AID.ISLOCALNAME));
        initialPosMsg.setContent(posicion.x + "," + posicion.y + "," + tipo);
        send(initialPosMsg);

        addBehaviour(new MoveBehaviour(this, 1000));
        addBehaviour(new LoseBehaviour());
    }


    private class MoveBehaviour extends TickerBehaviour {
        public MoveBehaviour(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            if (posicion != null && tipo != null) {
                int dx = random.nextInt(3) - 1; // Move -1, 0, or 1 in x direction
                int dy = random.nextInt(3) - 1; // Move -1, 0, or 1 in y direction
                posicion.translate(dx, dy);
                // Ensure the position stays within bounds
                posicion.x = Math.max(0, Math.min(99, posicion.x));
                posicion.y = Math.max(0, Math.min(99, posicion.y));

                // Inform GUI about the position update
                ACLMessage posMsg = new ACLMessage(ACLMessage.INFORM);
                posMsg.addReceiver(new AID("gui", AID.ISLOCALNAME));
                posMsg.setContent(posicion.x + "," + posicion.y + "," + tipo);
                send(posMsg);

                // Check for collisions and battle
                ACLMessage battleMsg = new ACLMessage(ACLMessage.REQUEST);
                battleMsg.addReceiver(new AID("gui", AID.ISLOCALNAME));
                battleMsg.setContent("battle," + getLocalName() + "," + posicion.x + "," + posicion.y);
                send(battleMsg);
            } else {
                System.out.println("Posición o tipo no definido.");
            }
        }
    }

    private class LoseBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null && msg.getContent().equals("lose")) {
                doDelete();
            } else {
                block();
            }
        }
    }

    public String getTipo() {
        return tipo;
    }

    public Point getPosicion() {
        return posicion;
    }

    public boolean luchar(String otroTipo) {
        if (tipo != null && probabilidades.containsKey(tipo) && probabilidades.get(tipo).containsKey(otroTipo)) {
            double probabilidadGanar = probabilidades.get(tipo).get(otroTipo);
            return random.nextDouble() < probabilidadGanar;
        }
        return false;
    }
}

