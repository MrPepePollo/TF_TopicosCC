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
        Map<String, Double> fuegoProb = new HashMap<>();
        fuegoProb.put("fuego", 0.5);
        fuegoProb.put("agua", 0.2);
        fuegoProb.put("electric", 0.4);
        fuegoProb.put("planta", 0.8);
        fuegoProb.put("roca", 0.2);
        fuegoProb.put("psiquico", 0.6);
        fuegoProb.put("ghost", 0.4);
        fuegoProb.put("lucha", 0.6);
        fuegoProb.put("bicho", 0.7);
        probabilidades.put("fuego", fuegoProb);

        Map<String, Double> aguaProb = new HashMap<>();
        aguaProb.put("fuego", 0.8);
        aguaProb.put("agua", 0.5);
        aguaProb.put("electric", 0.3);
        aguaProb.put("planta", 0.4);
        aguaProb.put("roca", 0.7);
        aguaProb.put("psiquico", 0.5);
        aguaProb.put("ghost", 0.5);
        aguaProb.put("lucha", 0.6);
        aguaProb.put("bicho", 0.5);
        probabilidades.put("agua", aguaProb);

        Map<String, Double> electricProb = new HashMap<>();
        electricProb.put("fuego", 0.3);
        electricProb.put("agua", 0.8);
        electricProb.put("electric", 0.5);
        electricProb.put("planta", 0.6);
        electricProb.put("roca", 0.6);
        electricProb.put("psiquico", 0.5);
        electricProb.put("ghost", 0.4);
        electricProb.put("lucha", 0.4);
        electricProb.put("bicho", 0.6);
        probabilidades.put("electric", electricProb);

        Map<String, Double> plantaProb = new HashMap<>();
        plantaProb.put("fuego", 0.2);
        plantaProb.put("agua", 0.6);
        plantaProb.put("electric", 0.4);
        plantaProb.put("planta", 0.5);
        plantaProb.put("roca", 0.8);
        plantaProb.put("psiquico", 0.4);
        plantaProb.put("ghost", 0.6);
        plantaProb.put("lucha", 0.5);
        plantaProb.put("bicho", 0.6);
        probabilidades.put("planta", plantaProb);

        Map<String, Double> rocaProb = new HashMap<>();
        rocaProb.put("fuego", 0.7);
        rocaProb.put("agua", 0.3);
        rocaProb.put("electric", 0.4);
        rocaProb.put("planta", 0.6);
        rocaProb.put("roca", 0.5);
        rocaProb.put("psiquico", 0.6);
        rocaProb.put("ghost", 0.4);
        rocaProb.put("lucha", 0.4);
        rocaProb.put("bicho", 0.6);
        probabilidades.put("roca", rocaProb);

        Map<String, Double> psiquicoProb = new HashMap<>();
        psiquicoProb.put("fuego", 0.6);
        psiquicoProb.put("agua", 0.6);
        psiquicoProb.put("electric", 0.6);
        psiquicoProb.put("planta", 0.6);
        psiquicoProb.put("roca", 0.6);
        psiquicoProb.put("psiquico", 0.5);
        psiquicoProb.put("ghost", 0.4);
        psiquicoProb.put("lucha", 0.7);
        psiquicoProb.put("bicho", 0.4);
        probabilidades.put("psiquico", psiquicoProb);

        Map<String, Double> ghostProb = new HashMap<>();
        ghostProb.put("fuego", 0.6);
        ghostProb.put("agua", 0.5);
        ghostProb.put("electric", 0.5);
        ghostProb.put("planta", 0.5);
        ghostProb.put("roca", 0.6);
        ghostProb.put("psiquico", 0.7);
        ghostProb.put("ghost", 0.5);
        ghostProb.put("lucha", 0.5);
        ghostProb.put("bicho", 0.5);
        probabilidades.put("ghost", ghostProb);

        Map<String, Double> luchaProb = new HashMap<>();
        luchaProb.put("fuego", 0.6);
        luchaProb.put("agua", 0.4);
        luchaProb.put("electric", 0.6);
        luchaProb.put("planta", 0.5);
        luchaProb.put("roca", 0.7);
        luchaProb.put("psiquico", 0.3);
        luchaProb.put("ghost", 0.4);
        luchaProb.put("lucha", 0.5);
        luchaProb.put("bicho", 0.6);
        probabilidades.put("lucha", luchaProb);

        Map<String, Double> bichoProb = new HashMap<>();
        bichoProb.put("fuego", 0.4);
        bichoProb.put("agua", 0.5);
        bichoProb.put("electric", 0.4);
        bichoProb.put("planta", 0.6);
        bichoProb.put("roca", 0.4);
        bichoProb.put("psiquico", 0.6);
        bichoProb.put("ghost", 0.5);
        bichoProb.put("lucha", 0.6);
        bichoProb.put("bicho", 0.5);
        probabilidades.put("bicho", bichoProb);
    }


    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length == 3 && args[0] instanceof String && args[1] instanceof Integer && args[2] instanceof Integer) {
            tipo = (String) args[0];
            posicion = new Point((int) args[1], (int) args[2]);
        } else {
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
                int dx = random.nextInt(3) - 1;
                int dy = random.nextInt(3) - 1;
                posicion.translate(dx, dy);
                // Ensure the position stays within bounds
                posicion.x = Math.max(0, Math.min(99, posicion.x));
                posicion.y = Math.max(0, Math.min(99, posicion.y));

                ACLMessage posMsg = new ACLMessage(ACLMessage.INFORM);
                posMsg.addReceiver(new AID("gui", AID.ISLOCALNAME));
                posMsg.setContent(posicion.x + "," + posicion.y + "," + tipo);
                send(posMsg);

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

