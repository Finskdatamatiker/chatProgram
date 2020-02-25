package Client;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientAfsenderThread implements Runnable {
    /**
     * Denne klasse er hovedtråden, som sender beskeder til klienten og som laver to andre tråde
     * (den ene læser beskeder (ClientModtagerThread) og den anden sender heartbeat).
     * Klassen beder klienten om at joine chatten (metoden joinMetode()).
     * Protokollen skal overholdes, så JOIN tjekkes, inden den sendes til serveren.
     * Denne tråd beder ConsolerReader om at læse beskeder fra console og
     * ClientProtokol om at tjekke protokollen.
     * Derefter sender den beskeder til serveren.
     */

    /**
     * static boolean til alle whiles, hvor trådene kører, sådan at man har mulighed for
     * at standse dem, hvis det er.
     */
    public static boolean clientRunning = true;
    private String username;
    /*ServerPort som String, fordi scanneren læser hele input som String,
     så det er nemmest også at læse serverPort som en String*/
    private final String serverPort = "2000";
    //localHost
    private final String serverIp = "127.0.0.1";
    private Socket clientSocket;
    private Forbindelse forb;
    private SendHeartBeat sendHeartBeat;
    private ConsoleReader consoleReader = new ConsoleReader();
    /**
     * Dette felt fortæller, om brugeren bliver bedt om at indtaste et nyt brugernavn efter JOIN.
     * ClientModtagerThread modtager besked fra serveren, hvor serveren beder klienten om
     * at indtaste et nyt navn. ClientModtagerThread sætter dette felt til true.
     * Det er dog ClientAfsenderThread, som har ansvar for at bede klienten om at bekræfte
     * det nye navn og sende en ny JOIN i metoden bedOmEtNytNavn().Så sætter den feltet til false igen.
     */
    private boolean bedtOmEtNytNavn = false;

    public ClientAfsenderThread() {
    }

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
    public ConsoleReader getConsoleReader() { return consoleReader; }
    public void setConsoleReader(ConsoleReader consoleReader) { this.consoleReader = consoleReader; }
    public boolean isBedtOmEtNytNavn() { return bedtOmEtNytNavn; }
    public void setBedtOmEtNytNavn(boolean bedtOmEtNytNavn) { this.bedtOmEtNytNavn = bedtOmEtNytNavn; }

    /**
     * Her i run() laver en socket, forbindelse (dvs. datainput og dataoutput til socket),
     * ClientModtagerThread (som læser input fra serveren) og kalder på joinMetode, som
     * joiner klienten til chatten. Derefter bliver beskeder fra console læst og
     * tjekket i protokollen i metoden dataMetode(), inden data bliver sendt til serveren.
     * (serveren sender beskeder så videre til andre brugere osv.)
     */
    public void run() {

        try {
            clientSocket = new Socket(serverIp, Integer.parseInt(serverPort));
        } catch (IOException e) {
            e.printStackTrace();
        }

        forb = Forbindelse.givForbindelse(clientSocket);

        ClientModtagerThread clientModtagerThread = new ClientModtagerThread(forb, this);
        Thread threadModtager = new Thread(clientModtagerThread);

        joinMetode();

        threadModtager.start();

        while (clientRunning) {
            dataMetode();
        }

    }

    /**
     * Denne metode bliver kørt først. Klienten bliver bedt om at indtaste JOIN-besked.
     * Er der fejl, bliver brugeren bedt om at indtaste igen.
     * Protokol-klassen opdeler input således, at denne metode kan regne ud,
     * om der er protokol-fejl i input.
     * Her laves der også heartbeat-tråden til klienten.
     * Den tjekkede JOIN sendes så til serveren.
     */

    public void joinMetode() {

        System.out.println("\nVelkommen");
        System.out.println("Du skal først blive logget ind i systemet.");
        System.out.println("Skriv venligst følgende: JOIN brugernavn, serverIp:serverPort");


        try {
            ClientProtokol clientProtokol = new ClientProtokol(consoleReader);
            String[] joinGemtIArray = clientProtokol.laesJoinOgSplit();
            username = joinGemtIArray[0];
            String serverIP = joinGemtIArray[1];
            String serverPorten = joinGemtIArray[2];

            if (username.equals("QUIT")) {
                try {
                    forb.getDataOutputStream().writeUTF("QUIT");
                    System.out.println("Forbindelsen lukkes");
                    System.exit(0);

                } catch (IOException io) {
                    System.out.println(io);
                }
            } else {
                while (username.equals("FEJL") || !clientProtokol.erGyldigBrugernavn(username) || !serverIP.equals(serverIp) || !serverPorten.equals(serverPort)) {
                    System.out.println("Skriv besked: JOIN username, serverIP:serverPort");
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
            }
        } catch (IOException ue) {
            System.out.println(ue);
        }

    }


    /**
     * Denne metode bruges til at sende beskeder til serveren (som sender dem videre til
     * andre brugere).
     * Brugeren indtaster beskeden på console.
     * Metoden beder protokollen om at tjekke, hvilke elementer beskeden indeholder
     * og om der er fejl. Er der fejl, bedes klienten i denne metode om at overholde protokollen.
     * Hvis klienten skriver QUIT, sendes der en besked til server om det og forbindelsen lukkes.
     * Hvis ClientModtagerThread har fået en besked om, at brugernavn allerede var optaget,
     * senderen serveren besked om, at brugeren skal indtaste et nyt navn. Denne metode beder
     * metoden bedOmEtNytNavn() om at bekræfte det nye navn og sende en ny JOIN.
     * Ellers bliver DATA-beskeden sendt til serveren.
     */

    public void dataMetode() {

        ClientProtokol clientProtokol = new ClientProtokol(consoleReader);

        System.out.println("Indtast en ny besked.");
        String[] beskedIArray = clientProtokol.laesDataOgSplit();

        String user = beskedIArray[0];
        String besked = beskedIArray[1];

        if (besked.equals("QUIT")) {
            try {
                forb.getDataOutputStream().writeUTF("QUIT");
                System.out.println("forbindelsen lukkes");
                System.exit(0);
            } catch (IOException io) {
                System.out.println(io);
            }
        }

        else if (bedtOmEtNytNavn) {
                bedOmEtNytNavn();
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
     * Denne metode beder brugeren om at bekræfte det nye brugernavn og den sender en ny JOIN.
     * ClientModtagerThread sætter feltet "bedtOmEtNytNavn" til true, hvis sen modtager en
     * besked fra serveren om at brugernavnet skal ændres.
     * Metoden beder brugeren om at bekræfte det nye navn og en ny JOIN sendes til serveren.
     */
    public void bedOmEtNytNavn() {

        try {
            bedtOmEtNytNavn = true;
            System.out.println("Bekræft det nye brugernavn.");
            username = consoleReader.laesInputFraConsole();
            String sendNyJoin = "JOIN " + username + ", " + serverIp + ":" + serverPort;
            forb.getDataOutputStream().writeUTF(sendNyJoin);
            forb.getDataOutputStream().flush();
            bedtOmEtNytNavn = false;
        } catch (IOException io) {
            System.out.println(io);
        }

    }


    }



