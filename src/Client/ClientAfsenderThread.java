package Client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ClientAfsenderThread implements Runnable {
    /**
     * hovedtråden hos klient, som sender beskeder til klienten og som laver to andre tråde
     */

    private String username;
    private int serverPort;
    private InetAddress serverIp;
    private Socket clientSocket;
    private ClientProtokol clientProtokol;
    private Forbindelse forb;
    private SendHeartBeat sendHeartBeat;


    public ClientAfsenderThread(){
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public int getServerPort() { return serverPort; }
    public InetAddress getServerIp() { return serverIp; }
    public void setServerIp(InetAddress serverIp) { this.serverIp = serverIp; }
    public Socket getClientSocket() { return clientSocket; }
    public void setClientSocket(Socket clientSocket) { this.clientSocket = clientSocket; }
    public void setServerPort(int serverPort) { this.serverPort = serverPort; }
    public ClientProtokol getClientProtokol() { return clientProtokol; }
    public void setClientProtokol(ClientProtokol clientProtokol) { this.clientProtokol = clientProtokol; }
    public Forbindelse getForb() { return forb; }
    public void setForb(Forbindelse forb) { this.forb = forb; }

    public SendHeartBeat getSendHeartBeat() {
        return sendHeartBeat;
    }

    public void setSendHeartBeat(SendHeartBeat sendHeartBeat) {
        this.sendHeartBeat = sendHeartBeat;
    }

    public void run() {

         ConsoleReader consoleReader = new ConsoleReader();

             System.out.println("\nVelkommen ny kunde");
             System.out.println("Du skal først blive logget ind i systemet.");
             System.out.println("Skriv venligst følgende: JOIN brugernavn, serverIp:serverPort");

             try {

                 clientProtokol = new ClientProtokol(consoleReader);
                 String[] joinGemtIArray = clientProtokol.laesJoinOgSplit();


                 while (joinGemtIArray == null) {
                     System.out.println("Husk protokollen: JOIN username, serverIP:serverPort");
                     joinGemtIArray = clientProtokol.laesJoinOgSplit();
                 }

                 username = joinGemtIArray[0];

                 while (!clientProtokol.erGyldigBrugernavn(username)) {
                     System.out.println("Ugyldigt brugernavn, prøv igen");
                     username = consoleReader.laesInputFraConsole();
                 }


                 String serverIP = joinGemtIArray[1];
                 serverIp = InetAddress.getByName(serverIP);
                 serverPort = Integer.parseInt(joinGemtIArray[2]);

                 clientSocket = new Socket(serverIp, serverPort);
                 forb = new Forbindelse(clientSocket);

                 sendHeartBeat = new SendHeartBeat(forb, true, username);
                 Thread sendHeartBeatTHread = new Thread(sendHeartBeat);
                 sendHeartBeatTHread.start();

                 ClientModtagerThread clientModtagerThread = new ClientModtagerThread(forb);
                 Thread modtagerThread = new Thread(clientModtagerThread);
                 modtagerThread.start();
                 //korjaa tämä ,jos alkaa toimia....
                 String join = "JOIN " + username + ", " + serverIP + ":" + serverPort;
                 forb.getDataOutputStream().writeUTF(join);
                 forb.getDataOutputStream().flush();

             } catch (IOException ue) {
                 System.out.println(ue);
             }


         //hvad skal betingelsen være her?
         while (true/*ClientMain2.clientRunning*/) {

             String besked = "";
             do {
                 System.out.println(username + ", skriv en ny besked således:  DATA username: besked");
                 String[] beskedIArray = clientProtokol.laesDataOgSplit();

                 if (beskedIArray[0].equals("QUIT")) {
                     forb.lukForbindelse();
                 }
                 //tjek dette
                 while (beskedIArray == null) {
                     System.out.println("Husk at max 250 tegn og og husk protokollen: DATA username: besked");
                     beskedIArray = clientProtokol.laesDataOgSplit();
                 }

                 String usernameArray = beskedIArray[0];
                 besked = beskedIArray[1];


                 while (!usernameArray.equals(username)) {
                     System.out.println("Fejl i protokol");
                     String[] nyBesked = clientProtokol.laesDataOgSplit();
                     besked = nyBesked[1];
                     usernameArray = nyBesked[0];
                 }

                 String sendBesked = "DATA " + usernameArray + ": " + besked;

                 try {
                     sendHeartBeat.setHeartbeat(true);
                     forb.getDataOutputStream().writeUTF(sendBesked);
                     forb.getDataOutputStream().flush();


                 } catch (IOException io) {
                     System.out.println(io);
                 }

             } while (!besked.equals("QUIT"));



         }

     }

}
