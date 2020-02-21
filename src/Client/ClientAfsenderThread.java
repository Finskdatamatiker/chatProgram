package Client;

import java.io.IOException;
import java.net.Socket;

public class ClientAfsenderThread implements Runnable {
    /**
     * Denne klasse er tråden, som sender beskeder til klienten og som laver to andre tråde
     * (den ene læser beskeder og den anden sender heartbeat).
     */


    /**
     *  static boolean til alle whiles, hvor trådene kører, sådan at man har mulighed for
     *  at standse dem, hvis det er.
     */
    public static boolean clientRunning = true;
    private String username;
    /*ServerPort som String, fordi scanneren læser hele input som String,
     så det er nemmest også at læse serverPort som en String*/
    private final String serverPort = "2000";
    //localHost
    private final String serverIp = "127.0.0.1";
    private Socket clientSocket;
    private ClientProtokol clientProtokol;
    private Forbindelse forb;
    private SendHeartBeat sendHeartBeat;


    public ClientAfsenderThread(){}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getServerPort() { return serverPort; }
    public String getServerIp() { return serverIp; }
    public Socket getClientSocket() { return clientSocket; }
    public void setClientSocket(Socket clientSocket) { this.clientSocket = clientSocket; }
    public ClientProtokol getClientProtokol() { return clientProtokol; }
    public void setClientProtokol(ClientProtokol clientProtokol) { this.clientProtokol = clientProtokol; }
    public Forbindelse getForb() { return forb; }
    public void setForb(Forbindelse forb) { this.forb = forb; }
    public SendHeartBeat getSendHeartBeat() { return sendHeartBeat; }
    public void setSendHeartBeat(SendHeartBeat sendHeartBeat) { this.sendHeartBeat = sendHeartBeat; }

    /**
     * Her i run denne tråd beder Clientprotokol om at læse beskeder fra console (som faktisk beder
     * ConsoleReader om at gøre det). Beskederne bliver så behandlet her. De bliver kontrolleret af
     * ClientProtokol for gyldighed. Er der fejl, bliver brugeren bedt om at indtaste igen.
     * Er der ikke fejl, beder tråden Forbindelse om at sende beskeden til serveren.
     * JOIN-beskeden skal kun køre indtil at brugeren er godkendt og derefter kører
     * DATA-beskeder i while, indtil brugeren stopper (eller der er ingen heartbeat).
     */
    public void run() {

         ConsoleReader consoleReader = new ConsoleReader();

             System.out.println("\nVelkommen ny kunde");
             System.out.println("Du skal først blive logget ind i systemet.");
             System.out.println("Skriv venligst følgende: JOIN brugernavn, serverIp:serverPort");

             try {

                 clientProtokol = new ClientProtokol(consoleReader);
                 String[] joinGemtIArray = clientProtokol.laesJoinOgSplit();

                 username = joinGemtIArray[0];
                 String serverIP = joinGemtIArray[1];
                 String serverPorten = joinGemtIArray[2];

                  while(username.equals("FEJL") || !clientProtokol.erGyldigBrugernavn(username) || !serverIP.equals(serverIp) || !serverPorten.equals(serverPort)){
                         System.out.println("Husk protokollen: JOIN username, serverIP:serverPort");
                         joinGemtIArray = clientProtokol.laesJoinOgSplit();
                         username = joinGemtIArray[0];
                         serverIP = joinGemtIArray[1];
                         serverPorten = joinGemtIArray[2];
                  }

                 clientSocket = new Socket(serverIp, Integer.parseInt(serverPorten));
                 forb = Forbindelse.givForbindelse(clientSocket);

                 //laver tråd for heartbeat
                 sendHeartBeat = new SendHeartBeat(forb, true, username);
                 Thread sendHeartBeatTHread = new Thread(sendHeartBeat);
                 sendHeartBeatTHread.start();

                 //laver tråd, der modtager beskeder fra serveren
                 ClientModtagerThread clientModtagerThread = new ClientModtagerThread(forb);
                 Thread modtagerThread = new Thread(clientModtagerThread);
                 modtagerThread.start();

                 //Tjekket JOIN, som "samles" igen og sendes til serveren
                 String join = "JOIN " + username + ", " + serverIp + ":" + serverPort;
                 forb.getDataOutputStream().writeUTF(join);
                 forb.getDataOutputStream().flush();

             } catch (IOException ue) {
                 System.out.println(ue);
             }

         while (clientRunning) {

                 System.out.println("Indtast en ny besked, " + username);
                 String[] beskedIArray = clientProtokol.laesDataOgSplit();

                 String user = beskedIArray[0];
                 String besked = beskedIArray[1];

                 if (besked.equals("QUIT")) {
                     try {
                         forb.getDataOutputStream().writeUTF("QUIT");
                         break;
                     }catch (IOException io){
                         System.out.println(io);
                     }
                 }

                 while (!user.equals(username) || besked.equals("FEJL")) {
                     System.out.println("Husk protokollen: DATA brugernavn: besked");
                     beskedIArray = clientProtokol.laesDataOgSplit();
                     user = beskedIArray[0];
                     besked = beskedIArray[1];
                 }

                 String sendBeskedTilServer = "DATA " + user + ": " + besked;

                 try {
                     sendHeartBeat.setHeartbeat(true);
                     forb.getDataOutputStream().writeUTF(sendBeskedTilServer);
                     forb.getDataOutputStream().flush();

                 } catch (IOException io) {
                     System.out.println(io);
                 }
         }
     }
}
