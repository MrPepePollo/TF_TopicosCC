import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class WorldGUI extends Agent {
    private WorldPanel worldPanel;
    private Map<String, PokemonInfo> pokemones;

    @Override
    protected void setup() {
        pokemones = new ConcurrentHashMap<>();

        worldPanel = new WorldPanel();

        JFrame frame = new JFrame("Mundo Pokémon");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1250, 1250);
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
                    try {
                        int x = Integer.parseInt(content[0]);
                        int y = Integer.parseInt(content[1]);
                        String tipo = content[2];
                        if (!pokemones.containsKey(msg.getSender().getLocalName())) {
                            BufferedImage image = worldPanel.getRandomImageForType(tipo);
                            pokemones.put(msg.getSender().getLocalName(), new PokemonInfo(new Point(x, y), tipo, image));
                        } else {
                            pokemones.get(msg.getSender().getLocalName()).setPosicion(new Point(x, y));
                        }
                        worldPanel.updatePokemonPosition(msg.getSender().getLocalName(), x, y, tipo);
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing position: " + e.getMessage());
                    }
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
                if (content.length == 4) {
                    String pokemonName = content[1];
                    int x = Integer.parseInt(content[2]);
                    int y = Integer.parseInt(content[3]);

                    PokemonInfo attackerInfo = pokemones.get(pokemonName);
                    if (attackerInfo != null) {
                        for (Map.Entry<String, PokemonInfo> entry : pokemones.entrySet()) {
                            if (!entry.getKey().equals(pokemonName) && entry.getValue().posicion.equals(new Point(x, y))) {
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

                                boolean ganadorEsPokemon = pokemones.get(pokemonName).tipo.equals(otroTipo) || Math.random() < 0.5;
                                String perdedorName = ganadorEsPokemon ? otroPokemonName : pokemonName;

                                ACLMessage loseMsg = new ACLMessage(ACLMessage.INFORM);
                                loseMsg.addReceiver(new AID(perdedorName, AID.ISLOCALNAME));
                                loseMsg.setContent("lose");
                                send(loseMsg);

                                pokemones.remove(perdedorName);
                                worldPanel.removePokemon(perdedorName);
                                break;
                            }
                        }
                    } else {
                        System.out.println("Attacker info is null for: " + pokemonName);
                    }
                } else {
                    System.out.println("Invalid battle message format");
                }
            } else {
                block();
            }
        }
    }

    class WorldPanel extends JPanel {
        private Map<String, PokemonInfo> pokemones;
        private Map<String, List<BufferedImage>> pokemonImages;
        private Random random;
        private BufferedImage backgroundImage;

        public WorldPanel() {
            pokemones = new ConcurrentHashMap<>();
            pokemonImages = new ConcurrentHashMap<>();
            random = new Random();
            loadImages();
            loadBackgroundImage();
        }

        private void loadImages() {
            loadImagesForType("fuego", "img_pokemones/fuego.png", "img_pokemones/fuego2.png", "img_pokemones/fuego3.png");
            loadImagesForType("agua", "img_pokemones/agua.png", "img_pokemones/agua2.png", "img_pokemones/agua3.png");
            loadImagesForType("electric", "img_pokemones/electric.png", "img_pokemones/electric2.png", "img_pokemones/electric3.png");
            loadImagesForType("planta", "img_pokemones/planta.png", "img_pokemones/planta2.png", "img_pokemones/planta3.png");
            loadImagesForType("normal", "img_pokemones/normal.png", "img_pokemones/normal2.png", "img_pokemones/normal3.png");
            loadImagesForType("roca", "img_pokemones/roca.png", "img_pokemones/roca2.png", "img_pokemones/roca3.png");
            loadImagesForType("psiquico", "img_pokemones/psiquico.png", "img_pokemones/psiquico2.png", "img_pokemones/psiquico3.png");
            loadImagesForType("ghost", "img_pokemones/ghost.png", "img_pokemones/ghost2.png", "img_pokemones/ghost3.png");
            loadImagesForType("lucha", "img_pokemones/lucha.png", "img_pokemones/lucha2.png", "img_pokemones/lucha3.png");
            loadImagesForType("bicho", "img_pokemones/bicho.png", "img_pokemones/bicho2.png", "img_pokemones/bicho3.png");
        }

        private void loadBackgroundImage() {
            try {
                backgroundImage = ImageIO.read(new File("img_pokemones/background.jpg"));
            } catch (IOException e) {
                System.out.println("Error loading background image: " + e.getMessage());
            }
        }

        private void loadImagesForType(String tipo, String... filenames) {
            List<BufferedImage> images = new ArrayList<>();
            for (String filename : filenames) {
                try {
                    File file = new File(filename);
                    if (file.exists()) {
                        BufferedImage originalImage = ImageIO.read(file);
                        BufferedImage resizedImage = ImageUtils.resizeImage(originalImage, 65, 65);
                        images.add(resizedImage);
                    } else {
                        System.out.println("File not found: " + file.getAbsolutePath());
                    }
                } catch (IOException e) {
                    System.out.println("Error loading image " + filename + ": " + e.getMessage());
                }
            }
            pokemonImages.put(tipo, images);
        }

        public void updatePokemonPosition(String name, int x, int y, String tipo) {
            if (!pokemones.containsKey(name)) {
                BufferedImage image = getRandomImageForType(tipo);
                pokemones.put(name, new PokemonInfo(new Point(x, y), tipo, image));
            } else {
                pokemones.get(name).setPosicion(new Point(x, y));
            }
            repaint();
        }

        public void removePokemon(String name) {
            pokemones.remove(name);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
            }
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            double scaleX = panelWidth / 100.0;
            double scaleY = panelHeight / 100.0;
            for (Map.Entry<String, PokemonInfo> entry : pokemones.entrySet()) {
                Point pos = entry.getValue().posicion;
                BufferedImage image = entry.getValue().imagen;
                if (image != null) {
                    int adjustedX = (int) (pos.x * scaleX);
                    int adjustedY = (int) (pos.y * scaleY);
                    g.drawImage(image, adjustedX, adjustedY, null);
                }
            }
            drawPokemonCount(g);
        }

        private void drawPokemonCount(Graphics g) {
            Map<String, Integer> typeCounts = new HashMap<>();
            for (PokemonInfo p : pokemones.values()) {
                typeCounts.put(p.getTipo(), typeCounts.getOrDefault(p.getTipo(), 0) + 1);
            }

            int yPosition = 20;
            int rectHeight = typeCounts.size() * 20 + 40;
            int rectWidth = 300;

            g.setColor(new Color(128, 128, 128, 180));
            g.fillRect(5, 5, rectWidth, rectHeight);

            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.setColor(Color.BLACK);
            for (Map.Entry<String, Integer> entry : typeCounts.entrySet()) {
                g.drawString(entry.getKey() + ": " + entry.getValue(), 10, yPosition);
                yPosition += 20;
            }

            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.setColor(Color.RED);
            g.drawString("Total de Pokemones: " + pokemones.size(), 10, yPosition + 20);
        }

        private BufferedImage getRandomImageForType(String tipo) {
            List<BufferedImage> images = pokemonImages.get(tipo);
            if (images != null && !images.isEmpty()) {
                return images.get(random.nextInt(images.size()));
            }
            return null;
        }
    }

    class PokemonInfo {
        private Point posicion;
        private String tipo;
        private BufferedImage imagen;

        public PokemonInfo(Point posicion, String tipo, BufferedImage imagen) {
            this.posicion = posicion;
            this.tipo = tipo;
            this.imagen = imagen;
        }

        public Point getPosicion() {
            return posicion;
        }

        public void setPosicion(Point posicion) {
            this.posicion = posicion;
        }

        public String getTipo() {
            return tipo;
        }

        public BufferedImage getImagen() {
            return imagen;
        }
    }
}


