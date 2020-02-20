package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Listener implements Runnable {

    //public static boolean serverRunning = true;

    private static final int PORT = 2000;
    //jeg skal 5 klienter, som hver skal have tre tråde: (en lytter, en sender, en lytter til heartbeat), så 15 i alt
    public static final int MAXTHREADS = 15;
    //vector er thread-sikker
    public static Vector<Bruger> brugere = new Vector<>();
    private ServerSocket serverSocket;
    private ExecutorService minThreadPool;


    public Listener() {
        minThreadPool = Executors.newFixedThreadPool(MAXTHREADS);

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException io) {
            System.out.println(io);
        }
        System.out.println("Venter på en klient");
    }


    public void modtagKlienter() {

        try {

                Socket nyKlient = serverSocket.accept();
                ServerForbindelse serverForbindelse = new ServerForbindelse(nyKlient);
                ServerProtokol serverProtokol = new ServerProtokol();
                ClientCoordinator clientCoordinator = new ClientCoordinator(serverForbindelse, nyKlient, serverProtokol);
                Thread clientCoordinatorThread = new Thread(clientCoordinator);
                minThreadPool.submit(clientCoordinatorThread);

        } catch (IOException io) {
            System.out.println(io);
        }

    }


    @Override
    public void run() {
        while (true) {
            modtagKlienter();
        }
    }

    public static int getPORT() {
        return PORT;
    }

    public static int getMAXTHREADS() {
        return MAXTHREADS;
    }

    public List<Bruger> getBrugere() {
        return brugere;
    }



    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public ExecutorService getMinThreadPool() {
        return minThreadPool;
    }

    public void setMinThreadPool(ExecutorService minThreadPool) {
        this.minThreadPool = minThreadPool;
    }

}