package Client;

import java.io.IOException;
import java.net.Socket;

public class ClientAfsenderThread implements Runnable {
    /**
     * Denne klasse er hovedtråden, som sender beskeder til klienten og som laver to andre tråde
     * (den ene læser beskeder og den anden sender heartbeat).
     * Klassen beder klienten om at joine chatten (metoden joinMetode()) og ellers tager den
     * imod beskeder fra serveren. Serveren skal godkende alle handlinger, brugeren foretager sig.
     * Når beskeden fra serveren er fx J_OK (dvs. join lykkedes) eller TAK for besked,
     * kan klienten sende en ny besked. Ellers får den printet de andre beskeder på console.
     *
     * Protokollen skal overholdes, og det er både klienten og serveren, som laver tjek.
     * Der er tilføjet fire ekstra ting til protokollen: TAK (tak for beskeden fra server
     * til klienten), N_N (nyt brugernavn, hvis man prøver at bruge et navn, som allerede er taget)
     * og QUITOK (server giver klienten lov til at lukke - ellers driller dataStreamOutput, hvis
     * klienten bare lukker uautoriseret) og NO-IMAV for manglende heartbeat (så ved serveren,
     * at klienten er død).
     *
     * MEN: chatten er ikke synkron, dvs. brugeren får opdateret alle andres beskeder,
     * kun når den selv sender en besked. Så opdatering sker ikke løbende, men kun hver gang, når
     * brugeren selv aktiverer console. Console venter på brugerens input og printer al input
     * siden sidste besked, når brugeren har indtastet det ønskede input.
     * Ved ikke, om dette kunne kodes anderledes.
     * To console måske? Den ene til at skrive og den anden til at vise beskeder fra serveren?
     *
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
    private ConsoleReader consoleReader = new ConsoleReader();


    public ClientAfsenderThread(){}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getServerPort() { return serverPort; }
    public String getServerIp() { return serverIp; }
    public Socket getClientSocket() { return clientSocket; }
    public void setClientSocket(Socket clientSocket) { this.clientSocket = clientSocket; }
    public Forbindelse getForb() { return forb; }
    public void setForb(Forbindelse forb) { this.forb = forb; }
    public SendHeartBeat getSendHeartBeat() { return sendHeartBeat; }
    public void setSendHeartBeat(SendHeartBeat sendHeartBeat) { this.sendHeartBeat = sendHeartBeat; }


    /**
     * Her i run denne tråd laver en socket, forbindelse (dvs. datainput og dataoutput til socket),
     * ClientModtagerThread (som læser input fra serveren) og kalder på joinMetode, som
     * joiner klienten til chatten.
     */
    public void run() {

        try {
            clientSocket = new Socket(serverIp, Integer.parseInt(serverPort));
        } catch (IOException e) {
            e.printStackTrace();
        }

             forb = Forbindelse.givForbindelse(clientSocket);
             ClientModtagerThread clientModtagerThread = new ClientModtagerThread(forb, this);
             Thread thread = new Thread(clientModtagerThread);
             joinMetode();
             thread.start();

     }


    /**
     *   I denne metode bliver klienten bedt om at indtaste JOIN-besked.
     *   Er der fejl, bliver brugeren bedt om at indtaste igen.
     *   Protokol-klassen opdeler input således, at denne metode kan regne ud,
     *   om der er protokol-fejl i input.
     *   Her laves dre også heartbeat-tråden til klienten.
     *   Den tjekkede JOIN sendes så til serveren.
     */

     public void joinMetode(){

         System.out.println("\nVelkommen");
         System.out.println("Du skal først blive logget ind i systemet.");
         System.out.println("Skriv venligst følgende: JOIN brugernavn, serverIp:serverPort");

         try {

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

             //laver tråd for heartbeat
             sendHeartBeat = new SendHeartBeat(forb, true, username);
             Thread sendHeartBeatTHread = new Thread(sendHeartBeat);
             sendHeartBeatTHread.start();

             //Tjekket JOIN, som "samles" igen og sendes til serveren
             String join = "JOIN " + username + ", " + serverIp + ":" + serverPort;
             forb.getDataOutputStream().writeUTF(join);
             forb.getDataOutputStream().flush();

         } catch(IOException ue){
             System.out.println(ue);
         }

     }

    /**
     * Denne metode bruges, hvis brugernavnet allerede er optaget.
     */

    public void bedOmEtNytNavn() {

        try{
                 String nytBrugernavn = "";
                 System.out.println("Skriv et nyt brugernavn.");
                 nytBrugernavn = consoleReader.laesInputFraConsole();
                 username = nytBrugernavn;

                 forb.getDataOutputStream().writeUTF("N_N" + username);
                 forb.getDataOutputStream().flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Denne metode bruges til sende nye beskeder til de andre i chatten.
     * Brugeren indtaster beskeden på console.
     * Metoden beder protokollen om at tjekke, hvilke elementer beskeden indeholder
     * og om der er fejl.Så ved denne metode, hvad den skal gøre.
     * Er der fejl, bedes klienten om at overholde protokollen.
     * Skulle klienten skrive QUIT, sendes der en besked til server om det, som accepteter og lukker forbindelsen.
     */

     public void dataMetode(){

                clientProtokol = new ClientProtokol(consoleReader);

                System.out.println("Indtast en ny besked, " + username);
                String[] beskedIArray = clientProtokol.laesDataOgSplit();

                String user = beskedIArray[0];
                String besked = beskedIArray[1];

                if (besked.equals("QUIT")) {
                    try {
                        forb.getDataOutputStream().writeUTF("QUIT");
                    } catch (IOException io) {
                        System.out.println(io);
                    }
                }

                else {
                    while (!user.equals(username) || besked.equals("FEJL")) {
                        System.out.println("Husk protokollen: DATA brugernavn: besked");
                        beskedIArray = clientProtokol.laesDataOgSplit();
                        user = beskedIArray[0];
                        besked = beskedIArray[1];
                    }

                    username = user;
                    String sendBeskedTilServer = "DATA " + username + ":" + besked;

                    try {
                        sendHeartBeat.setHeartbeat(true);
                        forb.getDataOutputStream().writeUTF(sendBeskedTilServer);
                        forb.getDataOutputStream().flush();

                    } catch (IOException io) {
                        System.out.println(io);
                    }
                }
     }

    /**
     * Denne metode behandler beskeder fra serveren, som den får fra ClientModtagerThread.
     * dataMetode() bliver kaldt på, efter at klienten har fået J_OK for join eller TAK for besked
     * sådan at klienten kan sende en ny besked.
     * Men når klienten får besked fra andre eller listen over brugere, når der kommer en ny klient til,
     * behøver kunden ikke at foretage sig noget. Beskeden bliver bare printet.
     * QUITOK er kvittering fra serveren om, at systemet må lukkes.
     * Protokol-fejl er en sikkerhedsforanstaltning, fordi klienten tjekkes også selv,
     * at den ikke sender noget ulovligt af sted.
     * @param beskedFraServer
     */

     public void behandlBesked(String beskedFraServer) {

         if(beskedFraServer.equals("QUITOK")){
             System.exit(0);}

        if(beskedFraServer. equals("J_ER0: fejl i JOIN-protokol")){
            System.out.println("Skriv igen: " + beskedFraServer);
            joinMetode();
        }
        if(beskedFraServer.equals("J_OK")) {
            System.out.println(beskedFraServer);
            dataMetode();

        }else if(beskedFraServer.contains("LIST")){
            System.out.println(beskedFraServer);

        }else if(beskedFraServer.contains("sender besked")){
            System.out.println(beskedFraServer);

        }else if(beskedFraServer.equals("TAK")){
            System.out.println(beskedFraServer + " for din besked");
            dataMetode();
         }
        else if(beskedFraServer.equals("J_ER1: ugyldigt username")) {
            /**
             * Klienten når at få printet listen, inden denne ændring er gemt  dvs. klienten
             * ser sit ugyldige navn på listen, men når den næste klient kommer på, er listen opdateret.
             * Har ikke kunnet løse den - i koden på serversiden ændrer jeg navnet først på listen
             * og derefter sender den, men alligevel sker det.
            */
            System.out.println("Serveren afviser username, så valg et nyt navn: " + beskedFraServer);
            bedOmEtNytNavn();

        }else if(beskedFraServer.equals("J_ER2: fejl i DATA-protokollen")) {
            System.out.println("Skriv igen: " + beskedFraServer);
            dataMetode();

        }else if(beskedFraServer.equals("J_ER3: anden fejl")) {
            System.out.println(beskedFraServer);
        }

        else{
            System.out.println(beskedFraServer);

             }
         }
}
