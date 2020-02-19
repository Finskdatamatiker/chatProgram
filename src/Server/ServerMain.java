package Server;


import java.util.concurrent.Executors;

public class ServerMain {


    public static void main(String[] args) {

            Listener listener = new Listener();
            Thread thread = new Thread(listener);
            thread.start();
    }
}
