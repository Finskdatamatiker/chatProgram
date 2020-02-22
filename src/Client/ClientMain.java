package Client;
public class ClientMain {

    public static void main(String[] args) {

        ClientAfsenderThread clientAfsenderThread = new ClientAfsenderThread();
        Thread threadClient = new Thread(clientAfsenderThread);
        threadClient.start();

    }
}
