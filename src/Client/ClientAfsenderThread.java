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
    private ClientModtagerThread clientModtagerThread;
    private ConsoleReader consoleReader = new ConsoleReader();


    public ClientAfsenderThread(){}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getServerPort() { return serverPort; }
    public String getServerIp() { return serverIp; }
    public Socket getClientSocket() { return clientSocket; }
    public void setClientSocket(Socket clientSocket) { this.clientSocket = clientSocket; }
    //public ClientProtokol getClientProtokol() { return clientProtokol; }
    //public void setClientProtokol(ClientProtokol clientProtokol) { this.clientProtokol = clientProtokol; }
    public Forbindelse getForb() { return forb; }
    public void setForb(Forbindelse forb) { this.forb = forb; }
    public SendHeartBeat getSendHeartBeat() { return sendHeartBeat; }
    public void setSendHeartBeat(SendHeartBeat sendHeartBeat) { this.sendHeartBeat = sendHeartBeat; }
    public ClientModtagerThread getClientModtagerThread() { return clientModtagerThread; }
    public void setClientModtagerThread(ClientModtagerThread clientModtagerThread) { this.clientModtagerThread = clientModtagerThread; }

    /**
     * Her i run denne tråd beder Clientprotokol om at læse beskeder fra console (som faktisk beder
     * ConsoleReader om at gøre det). Beskederne bliver så behandlet her. De bliver kontrolleret af
     * ClientProtokol for gyldighed. Er der fejl, bliver brugeren bedt om at indtaste igen.
     * Er der ikke fejl, beder tråden Forbindelse om at sende beskeden til serveren.
     * JOIN-beskeden skal kun køre indtil at brugeren er godkendt og derefter kører
     * DATA-beskeder i while, indtil brugeren stopper (eller der er ingen heartbeat).
     */
    public void run() {

        // ConsoleReader consoleReader = new ConsoleReader();

             System.out.println("\nVelkommen ny kunde");
             System.out.println("Du skal først blive logget ind i systemet.");
             System.out.println("Skriv venligst følgende: JOIN brugernavn, serverIp:serverPort");


             joinMetode();

      /*  boolean test = clientModtagerThread.isUsernameDuerIkke();
        System.out.println(test);*/

             dataMetode();





                       /*   String svaret = clientModtagerThread.getErUsernameAccepteret();
             System.out.println(svaret);
            if(clientModtagerThread.getErUsernameAccepteret().equals("NEJ")){
            System.out.println("Skriv et nyt brugernavn.");
            String nytBrugernavn = consoleReader.laesInputFraConsole();
            while (!username.equals(nytBrugernavn) || !clientProtokol.erGyldigBrugernavn(nytBrugernavn)) {
                System.out.println("Skriv igen et nyt username");
                nytBrugernavn = consoleReader.laesInputFraConsole();
            }
            try {
                forb.getDataOutputStream().writeUTF("N_N" + nytBrugernavn);
                forb.getDataOutputStream().flush();
            }catch (IOException io){
                System.out.println(io);
            }
        }*/

     }




     public void joinMetode(){

         try {
             consoleReader = new ConsoleReader();
             clientProtokol = new ClientProtokol(consoleReader);
             String[] joinGemtIArray = clientProtokol.laesJoinOgSplit();

             username = joinGemtIArray[0];
             String serverIP = joinGemtIArray[1];
             String serverPorten = joinGemtIArray[2];

             while (username.equals("FEJL") || !clientProtokol.erGyldigBrugernavn(username) || !serverIP.equals(serverIp) || !serverPorten.equals(serverPort)) {
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
             clientModtagerThread = new ClientModtagerThread(forb);
             Thread modtagerThread = new Thread(clientModtagerThread);
             modtagerThread.start();

             //Tjekket JOIN, som "samles" igen og sendes til serveren
             String join = "JOIN " + username + ", " + serverIp + ":" + serverPort;
             forb.getDataOutputStream().writeUTF(join);
             forb.getDataOutputStream().flush();

         } catch(IOException ue){
             System.out.println(ue);
         }
     }

     public void bedOmEtNytNavn() {

        try{
                 String nytBrugernavn = "";
                 System.out.println("Skriv et nyt brugernavn.");
                 nytBrugernavn = consoleReader.laesInputFraConsole();
                 username = nytBrugernavn;
                 System.out.println("hvad er username " + username);

                 forb.getDataOutputStream().writeUTF("N_N" + username);
                 forb.getDataOutputStream().flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


         public void dataMetode(){

        while(clientRunning) {

                clientProtokol = new ClientProtokol(consoleReader);
                System.out.println("Indtast en ny besked, " + username);
                String[] beskedIArray = clientProtokol.laesDataOgSplit();

                String user = beskedIArray[0];
                String besked = beskedIArray[1];

                if (besked.equals("QUIT")) {
                    try {
                        forb.getDataOutputStream().writeUTF("QUIT");
                        break;
                    } catch (IOException io) {
                        System.out.println(io);
                    }
                }

                while (!user.equals(username) || besked.equals("FEJL")) {
                    System.out.println("Husk protokollen: DATA brugernavn: besked");
                    beskedIArray = clientProtokol.laesDataOgSplit();
                    user = beskedIArray[0];
                    besked = beskedIArray[1];
                }

                username = user;
                String sendBeskedTilServer = "DATA " + username + ": " + besked;


                try {
                    sendHeartBeat.setHeartbeat(true);
                    forb.getDataOutputStream().writeUTF(sendBeskedTilServer);
                    forb.getDataOutputStream().flush();

                } catch (IOException io) {
                    System.out.println(io);
                }

            }
     }

     public void behandlBesked(String beskedFraServer){
         //  switch (beskedFraServer){
                  /*  case "J_ER0: fejl i JOIN-protokol":
                        System.out.println("Skriv igen: " + beskedFraServer);
                        break;*/
                /*    case "J_OK":
                        System.out.println("Serveren godkender: " + beskedFraServer);
                        usernameDuerIkke = false;
                        System.out.println(usernameDuerIkke);
                        break;
                    case "J_ER1: ugyldigt username":
                        System.out.println("Serveren afviser username, så valg et nyt navn: " + beskedFraServer);
                        usernameDuerIkke = true;
                        System.out.println(usernameDuerIkke + "her true?");
                        break;
                 /*   case "J_ER2: fejl i DATA-protokollen":
                        System.out.println("Skriv igen: " + beskedFraServer);
                        break;
                    case "J_ER3: anden fejl":
                        System.out.println(beskedFraServer);
                        break;*/
          /*          case "QUITOK":
                        System.exit(0);
                        break;
                    default:
                        System.out.println(beskedFraServer);
                        break;}*/
     }
}
