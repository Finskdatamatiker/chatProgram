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
     * Denne klasse er servertråden, som lytter og tager imod klienter.
     * Når der kommer en klient, laves den en ny tråd til klienten (ClientCoordinator)
     * for selv at kunne lytte videre. Inde i ClientCoordinator laves der en anden tråd til
     * klienten for at tage imod beskeder fra klienten.
     */


    /**
     *  static boolean til alle whiles, hvor trådene kører, sådan at man har mulighed for
     *  at standse dem, hvis det er.
     */
    public static boolean serverRunning = true;

    private static final int PORT = 2000;
    /**
     * 5 klienter + en, som lytter
     * Egentlig laver jeg også en ekstra tråd inde i klient-tråden, sådan at jeg har en,
     * som behandler og sender beskeder og en anden, som tager imod beskeder fra klienten.
     * Men trådpuljen har fint kunnet håndtere fem klienter med i alt 6 tråde,
     * så enten tæller trådene inde i klient-trådene ikke som separate, eller også
     * får den ikke travlt, fordi jeg tester alene og dermed kan kun en klient skriver
     * ad gangen.
     * På klient-siden laver jeg tre tråde (en som tager imod beskeder, anden som behandler
     * og sender beskeder og tredje som sender heartbeat, men de er jo ikke en del af denne pulje.
     */

    public static final int MAXTHREADS = 6;
    /**
     * vector er thread-sikker. Jeg vil have en static og final, sådan at det er den samme liste
     * hele tiden uden at jeg skal genere en ny i ClientCoordinator.
     * Men Vector kan give ConcurrentModificationException, så den er ikke fejlfri med
     * multithreading. Det kan man forebygge ved ikke at bruge iterator, men looper i for-loop.
     */
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
     * da testen med mange samtidige klienter, som åbnede og lukkede forbindelsen,
     * fik jeg problemer med kø (selv om jeg prøvede at øge max-antallet af tråde i puljen.
     * Jeg laver en ny socket til hver klient og giver klienten også en forbindelse
     * (datatinput og datatoutputstream), og serverProtokollen, som den skal kende.
     * Tråde bliver håndteret af threadpool.
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