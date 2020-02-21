package Server;

public class ServerMain {


    public static void main(String[] args) {

            Listener listener = new Listener();
            Thread thread = new Thread(listener);
            thread.start();

    }
}
