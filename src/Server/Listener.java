package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Listener implements Runnable {

    //public static boolean serverRunning = true;

    private static final int PORT = 2000;
    //5 klienter + en, som lytter
    public static final int MAXTHREADS = 6;
    //vector er thread-sikker
    public static Vector<Bruger> brugere = new Vector<>();
    private ServerSocket serverSocket;
    private ThreadPoolExecutor minThreadPool;


    public Listener() {
        minThreadPool = (ThreadPoolExecutor)Executors.newCachedThreadPool();
        minThreadPool.setMaximumPoolSize(MAXTHREADS);

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException io) {
            System.out.println(io);
        }
        System.out.println("Venter på en klient");
    }


    public ServerSocket getServerSocket() { return serverSocket; }
    public void setServerSocket(ServerSocket serverSocket) { this.serverSocket = serverSocket; }
    public ExecutorService getMinThreadPool() { return minThreadPool; }
    public void setMinThreadPool(ThreadPoolExecutor minThreadPool) { this.minThreadPool = minThreadPool; }


    public void modtagKlienter() {

        try {
               Thread.sleep(1000);
                Socket nyKlientSocket = serverSocket.accept();
                //ServerForbindelse serverForbindelse = ServerForbindelse.givServerForbindelse(nyKlientSocket);
                ServerForbindelse serverForbindelse = new ServerForbindelse(nyKlientSocket);
                ServerProtokol serverProtokol = new ServerProtokol();
                ClientCoordinator clientCoordinator = new ClientCoordinator(serverForbindelse, nyKlientSocket, serverProtokol);
                Thread clientCoordinatorThread = new Thread(clientCoordinator);
                minThreadPool.submit(clientCoordinatorThread);
                minThreadPool.setKeepAliveTime(30, TimeUnit.SECONDS);

        } catch (InterruptedException ie){
            System.out.println(ie);
        }
        catch (IOException io) {
            System.out.println(io);
        }

    }


    @Override
    public void run() {
        while (true) {
            modtagKlienter();

        }
    }

}