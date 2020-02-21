package Client;

import java.io.IOException;
import java.net.Socket;

public class ClientAfsenderThread implements Runnable {
    /**
     * hovedtråden hos klient, som sender beskeder til klienten og som laver to andre tråde
     */

    private String username;
    //String, fordi scanneren læser hele input som String, så det er nemmest også at læse serverPort som en String
    private final String serverPort = "2000";
    //localHost
    private final String serverIp = "127.0.0.1";
    private Socket clientSocket;
    private ClientProtokol clientProtokol;
    private Forbindelse forb;
    private SendHeartBeat sendHeartBeat;


    public ClientAfsenderThread(){
    }

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

         //hvad skal betingelsen være her?
         while (true) {

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
