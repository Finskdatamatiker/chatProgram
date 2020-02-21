package Client;
public class ClientMain {

    //styr med denne, at de andre også kører
    public static boolean clientRunning = true;

    public static void main(String[] args) {

        ClientAfsenderThread clientAfsenderThread = new ClientAfsenderThread();
        Thread threadClient = new Thread(clientAfsenderThread);
        threadClient.start();

    }
}
