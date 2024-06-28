import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldGUI extends Agent {
    private WorldPanel worldPanel;
    private Map<String, PokemonInfo> pokemones;
    private int numBatallas;
    private int numEliminados;

    @Override
    protected void setup() {
        pokemones = new ConcurrentHashMap<>();
        numBatallas = 0;
        numEliminados = 0;
        worldPanel = new WorldPanel();

        JFrame frame = new JFrame("Mundo Pokémon");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1500, 1500);
        frame.add(worldPanel);
        frame.setVisible(true);

        addBehaviour(new UpdateGUIBehaviour());
        addBehaviour(new BattleHandlerBehaviour());
    }

    private class UpdateGUIBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                String[] content = msg.getContent().split(",");
                if (content.length == 3) {
                    // Positional update
                    int x = Integer.parseInt(content[0]);
                    int y = Integer.parseInt(content[1]);
                    String tipo = content[2];
                    pokemones.put(msg.getSender().getLocalName(), new PokemonInfo(new Point(x, y), tipo));
                    worldPanel.updatePokemonPosition(msg.getSender().getLocalName(), x, y, tipo);
                }
            } else {
                block();
            }
        }
    }

    private class BattleHandlerBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null && msg.getContent().startsWith("battle")) {
                String[] content = msg.getContent().split(",");
                String pokemonName = content[1];
                int x = Integer.parseInt(content[2]);
                int y = Integer.parseInt(content[3]);

                // Check for collisions
                for (Map.Entry<String, PokemonInfo> entry : pokemones.entrySet()) {
                    if (!entry.getKey().equals(pokemonName) && entry.getValue().posicion.equals(new Point(x, y))) {
                        numBatallas++;
                        // Battle
                        String otroPokemonName = entry.getKey();
                        String otroTipo = entry.getValue().tipo;
                        ACLMessage battleResult = new ACLMessage(ACLMessage.INFORM);
                        battleResult.addReceiver(new AID(otroPokemonName, AID.ISLOCALNAME));
                        battleResult.setContent("battle_result," + pokemonName);
                        send(battleResult);

                        ACLMessage resultMsg = new ACLMessage(ACLMessage.INFORM);
                        resultMsg.addReceiver(new AID(pokemonName, AID.ISLOCALNAME));
                        resultMsg.setContent("battle_result," + otroPokemonName);
                        send(resultMsg);

                        // Determine winner and loser
                        boolean ganadorEsPokemon = pokemones.get(pokemonName).tipo.equals(otroTipo) || Math.random() < 0.5;
                        String perdedorName = ganadorEsPokemon ? otroPokemonName : pokemonName;

                        // Inform the loser that it has lost
                        ACLMessage loseMsg = new ACLMessage(ACLMessage.INFORM);
                        loseMsg.addReceiver(new AID(perdedorName, AID.ISLOCALNAME));
                        loseMsg.setContent("lose");
                        send(loseMsg);

                        // Remove the loser from the GUI
                        pokemones.remove(perdedorName);
                        worldPanel.removePokemon(perdedorName);
                        numEliminados++;
                        break;
                    }
                }
            } else {
                block();
            }
        }
    }

    class WorldPanel extends JPanel {
        private Map<String, Point> pokemones;
        private Map<String, String> pokemonTypes;

        public WorldPanel() {
            pokemones = new ConcurrentHashMap<>();
            pokemonTypes = new ConcurrentHashMap<>();
        }

        public void updatePokemonPosition(String name, int x, int y, String tipo) {
            pokemones.put(name, new Point(x, y));
            pokemonTypes.put(name, tipo);
            repaint();
        }

        public void removePokemon(String name) {
            pokemones.remove(name);
            pokemonTypes.remove(name);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (Map.Entry<String, Point> entry : pokemones.entrySet()) {
                Point pos = entry.getValue();
                String tipo = pokemonTypes.get(entry.getKey());
                g.setColor(getColorForType(tipo));
                g.fillOval(pos.x * 8, pos.y * 8, 8, 8); // Adjust scaling as necessary
            }
            g.setColor(Color.BLACK);
            g.drawString("Pokémon restantes: " + pokemones.size(), 10, 20);
            g.drawString("Batallas: " + numBatallas, 10, 40);
            g.drawString("Pokémon eliminados: " + numEliminados, 10, 60);
        }

        private Color getColorForType(String tipo) {
            switch (tipo) {
                case "fuego":
                    return Color.RED;
                case "agua":
                    return Color.BLUE;
                case "electric":
                    return Color.YELLOW;
                case "planta":
                    return Color.GREEN;
                default:
                    return Color.GRAY;
            }
        }
    }

    class PokemonInfo {
        Point posicion;
        String tipo;

        public PokemonInfo(Point posicion, String tipo) {
            this.posicion = posicion;
            this.tipo = tipo;
        }
    }
}

