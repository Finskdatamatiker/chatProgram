package Client2;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ClientAfsenderThread2 implements Runnable {
    /**
     * hovedtråden hos klient, som sender beskeder til klienten og som laver to andre tråde
     */

    private String username;
    private int serverPort;
    private InetAddress serverIp;
    private Socket clientSocket;
    private ClientProtokol2 clientProtokol2;
    private Forbindelse2 forb2;
    private SendHeartBeat2 sendHeartBeat2;


    public ClientAfsenderThread2(){
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public int getServerPort() { return serverPort; }
    public InetAddress getServerIp() { return serverIp; }
    public void setServerIp(InetAddress serverIp) { this.serverIp = serverIp; }
    public Socket getClientSocket() { return clientSocket; }
    public void setClientSocket(Socket clientSocket) { this.clientSocket = clientSocket; }
    public void setServerPort(int serverPort) { this.serverPort = serverPort; }
    public ClientProtokol2 getClientProtokol2() { return clientProtokol2; }
    public void setClientProtokol2(ClientProtokol2 clientProtokol2) { this.clientProtokol2 = clientProtokol2; }
    public Forbindelse2 getForb2() { return forb2; }
    public void setForb2(Forbindelse2 forb2) { this.forb2 = forb2; }

    public SendHeartBeat2 getSendHeartBeat2() {
        return sendHeartBeat2;
    }

    public void setSendHeartBeat2(SendHeartBeat2 sendHeartBeat2) {
        this.sendHeartBeat2 = sendHeartBeat2;
    }

    public void run() {

         ConsoleReader2 consoleReader2 = new ConsoleReader2();

             System.out.println("\nVelkommen ny kunde");
             System.out.println("Du skal først blive logget ind i systemet.");
             System.out.println("Skriv venligst følgende: JOIN brugernavn, serverIp:serverPort");

             try {

                 clientProtokol2 = new ClientProtokol2(consoleReader2);
                 String[] joinGemtIArray = clientProtokol2.laesJoinOgSplit();


                 while (joinGemtIArray == null) {
                     System.out.println("Husk protokollen: JOIN username, serverIP:serverPort");
                     joinGemtIArray = clientProtokol2.laesJoinOgSplit();
                 }

                 username = joinGemtIArray[0];

                 while (!clientProtokol2.erGyldigBrugernavn(username)) {
                     System.out.println("Ugyldigt brugernavn, prøv igen");
                     username = consoleReader2.laesInputFraConsole();
                 }


                 String serverIP = joinGemtIArray[1];
                 serverIp = InetAddress.getByName(serverIP);
                 serverPort = Integer.parseInt(joinGemtIArray[2]);

                 clientSocket = new Socket(serverIp, serverPort);
                 forb2 = new Forbindelse2(clientSocket);

                 sendHeartBeat2 = new SendHeartBeat2(forb2, true, username);
                 Thread sendHeartBeatThread = new Thread(sendHeartBeat2);
                 sendHeartBeatThread.start();

                 ClientModtagerThread2 clientModtagerThread2 = new ClientModtagerThread2(forb2);
                 Thread modtagerThread = new Thread(clientModtagerThread2);
                 modtagerThread.start();
                 //korjaa tämä ,jos alkaa toimia....
                 String join = "JOIN " + username + ", " + serverIP + ":" + serverPort;
                 forb2.getDataOutputStream().writeUTF(join);
                 forb2.getDataOutputStream().flush();

             } catch (IOException ue) {
                 System.out.println(ue);
             }

         //hvad skal betingelsen være her?
         while (true/*ClientMain2.clientRunning*/) {

             String besked = "";
             do {
                 System.out.println(username + ", skriv en ny besked således:  DATA username: besked");
                 String[] beskedIArray = clientProtokol2.laesDataOgSplit();

                 if (beskedIArray[0].equals("QUIT")) {
                     forb2.lukForbindelse();
                 }
                 //tjek dette
                 while (beskedIArray == null) {
                     System.out.println("Husk at max 250 tegn og og husk protokollen: DATA username: besked");
                     beskedIArray = clientProtokol2.laesDataOgSplit();
                 }

                 String usernameArray = beskedIArray[0];
                 besked = beskedIArray[1];


                 while (!usernameArray.equals(username)) {
                     System.out.println("Fejl i protokol");
                     String[] nyBesked = clientProtokol2.laesDataOgSplit();
                     besked = nyBesked[1];
                     usernameArray = nyBesked[0];
                 }

                 String sendBesked = "DATA " + usernameArray + ": " + besked;

                 try {
                     sendHeartBeat2.setHeartbeat2(true);
                     forb2.getDataOutputStream().writeUTF(sendBesked);
                     forb2.getDataOutputStream().flush();

                 } catch (IOException io) {
                     System.out.println(io);
                 }

             } while (!besked.equals("QUIT"));


         }

     }

}
