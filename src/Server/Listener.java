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

    /**
     * Denne klasse er den tråd, som lytter og tager imod klienter.
     */


    /**
     *  static boolean til alle whiles, hvor trådene kører, sådan at man har mulighed for
     *  at standse dem, hvis det er.
     */
    public static boolean serverRunning = true;

    private static final int PORT = 2000;
    //5 klienter + en, som lytter
    public static final int MAXTHREADS = 6;
    /**
     * vector er thread-sikker. Jeg vil have en static og final, sådan at det er den samme liste
     * hele tiden uden at jeg skal genere en ny i ClientCoordinator
     * (hvilket jeg skuklle, hvis jeg gav den som et felt).
     */
    //public static final List<Bruger> brugere = Collections.synchronizedList(new Vector<>());
    public static final Vector<Bruger> brugere = new Vector<>();
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

    /**
     * Sleep() mellem klienterne hjælper til at håndtere en større mængde tråde,
     * da testen med mange samtidige klienter, som åbnede og lukkede forbindelsen, fik jeg problemer med kø.
     * Jeg laver en ny socket til hver klient og giver klienten også en forbindelse (datatinput og datatoutputstream),
     * og serverProtokollen, som den skal kende.
     * Tråden med klienten bliver håndteret af threadpool.
     */

    public void modtagKlienter() {

        try {
               Thread.sleep(1000);
                Socket nyKlientSocket = serverSocket.accept();
                ServerForbindelse serverForbindelse = new ServerForbindelse(nyKlientSocket);
                ServerProtokol serverProtokol = new ServerProtokol();
                ClientCoordinator clientCoordinator = new ClientCoordinator(serverForbindelse, nyKlientSocket, serverProtokol);
                Thread clientCoordinatorThread = new Thread(clientCoordinator);
                minThreadPool.submit(clientCoordinatorThread);
                minThreadPool.setKeepAliveTime(30, TimeUnit.SECONDS);

        } catch (InterruptedException | IOException ie){
            System.out.println(ie);
        }

    }


    @Override
    public void run() {
        while (serverRunning) {
            modtagKlienter();
        }
    }
}