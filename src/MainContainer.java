import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class MainContainer {
    private static final int TAMANO_MUNDO = 100;
    private static final int NUM_POKEMONES = 500;

    public static void main(String[] args) {
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        ContainerController cc = rt.createMainContainer(p);

        try {
            AgentController guiController = cc.createNewAgent("gui", "WorldGUI", null);
            guiController.start();

            for (int i = 0; i < NUM_POKEMONES; i++) {
                String tipo = getRandomTipo();
                int x = (int) (Math.random() * TAMANO_MUNDO);
                int y = (int) (Math.random() * TAMANO_MUNDO);
                Object[] agentArgs = new Object[]{tipo, x, y};
                AgentController ac = cc.createNewAgent("Pokemon" + i, "PokemonAgent", agentArgs);
                ac.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getRandomTipo() {
        String[] tipos = {"fuego", "agua", "electric", "planta"};
        return tipos[(int) (Math.random() * tipos.length)];
    }
}
