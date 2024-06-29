import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class MainContainer {
    private static final int TAMANO_MUNDO = 100;
    private static final int NUM_POKEMONES = 4000;

    public static void main(String[] args) {
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.GUI, "true");
        ContainerController cc = rt.createMainContainer(p);

        AudioPlayer audioPlayer = new AudioPlayer();
        audioPlayer.play("music_pokemon/music.wav");

        try {
            for (int i = 0; i < NUM_POKEMONES; i++) {
                String tipo = getRandomTipo();
                int x = (int) (Math.random() * TAMANO_MUNDO);
                int y = (int) (Math.random() * TAMANO_MUNDO);
                Object[] agentArgs = new Object[]{tipo, x, y};
                AgentController ac = cc.createNewAgent("Pokemon" + i, "PokemonAgent", agentArgs);
                ac.start();
            }


            Thread.sleep(2000);

            Object[] guiArgs = new Object[]{TAMANO_MUNDO};
            AgentController guiController = cc.createNewAgent("gui", "WorldGUI", guiArgs);
            guiController.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getRandomTipo() {
        String[] tipos = {"fuego", "agua", "electric", "planta", "normal", "ghost", "psiquico","roca","lucha","bicho"};
        return tipos[(int) (Math.random() * tipos.length)];
    }
}
