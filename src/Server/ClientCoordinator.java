package Server;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;

public class ClientCoordinator implements Runnable {
    /**
     * Klassen er tråden, som behandler modtagne beskeder og giver et svar til kunden.
     * Den repræsenterer én klient ad gangen og får socket til klienten fra Listener.
     * Klassen laver en anden tråd, som læser beskederne fra klienten (ServerModtagerThread).
     * Klassen tilføjer brugeren til brugerlisten, sletter inaktive brugere og giver svaret til
     * kunden i overenstemmelse med reglerne i klassen ServerProtokol.
     * Klassen tilføjer også alle transaktioner til LogBog.
     *
     * TO_DO: Jeg har endnu ikke kunnet håndtere det at slette en bruger, som
     * IKKE giver QUIT besked, men bare lukker forbindelsen uden advarsel.
     * Forbindelsen lukkes, men brugeren figurerer fortsat på listen.
     */
    private ServerForbindelse sforb;
    private Socket socket;
    private ServerProtokol protokol;
    private LogBog logBog;
    private Bruger bruger;


    public ClientCoordinator(ServerForbindelse sforb, Socket socket, ServerProtokol protokol){
        this.sforb = sforb;
        this.socket = socket;
        this.protokol = protokol;
        logBog = new LogBog();
    }

    public ServerForbindelse getSforb() {
        return sforb;
    }
    public Socket getSocket() {
        return socket;
    }
    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    public void setSforb(ServerForbindelse sforb) { this.sforb = sforb; }
    public ServerProtokol getProtokol() { return protokol; }
    public void setProtokol(ServerProtokol protokol) { this.protokol = protokol; }
    public LogBog getLogBog() { return logBog; }
    public void setLogBog(LogBog logBog) { this.logBog = logBog; }
    public Bruger getBruger() { return bruger; }
    public void setBruger(Bruger bruger) { this.bruger = bruger; }

    @Override
    public void run() {

          Thread serverModtagerThread = new Thread(new ServerModtagerThread(sforb, this));
          serverModtagerThread.start();
        }

    /**
     * Her bliver der behandlet de forskellige beskeder, som serverModtager-tråden læser
     *  NO_IMAV er tilføjet til protokollen for manglende heeartbeat, fordi datatoutputstream forudsætter,
     *  at man først lukker fra serversiden. Så når klienten lukker, giver serveren ok og klientens program lukkes.
     *  N_N står for nyt navn og bruges, når brugeren har indtastet et brugernavn, som allerede eksisterer.
     * @param beskedFraKlient som serverModtagerThread læser
     */

    public void behandlBesked(String beskedFraKlient) {

        //iterator, dvs. foreach giver ConcurrentModificationException

            if (beskedFraKlient.substring(0, 3).equals("N_N")) {
                String nytNavn = beskedFraKlient.substring(3);
                for(int i = 0; i < Listener.brugere.size(); i++){

                    String navn = Listener.brugere.get(i).getBrugernavn();
                    if (navn.equals(nytNavn) || !protokol.erGyldigBrugernavn(nytNavn)) {
                        sendBeskedTilKlient("J_ER1: ugyldigt username");
                    } else {
                         bruger.setBrugernavn(nytNavn);
                        sendBeskedTilKlient("J_OK");
                    }
                 }

            } else if (beskedFraKlient.contains("JOIN")) {
                String[] erDenJoin = protokol.laesJoinOgSplit(beskedFraKlient);
                String username = erDenJoin[0];

                    for(int i = 0; i < Listener.brugere.size(); i++){
                        String navn = Listener.brugere.get(i).getBrugernavn();
                        if (navn.equals(username) || !protokol.erGyldigBrugernavn(username)) {
                            sendBeskedTilKlient("J_ER1: ugyldigt username");
                        }
                    }
                        tilfoejBruger(username);
                       sendBeskedTilKlient("J_OK");

            } else if (beskedFraKlient.contains("DATA")) {
                String[] erDenData = protokol.laesDataOgSplit(beskedFraKlient);
                String brugernavn = erDenData[0];
                String beskedTilAlle = erDenData[1];

                if (beskedTilAlle.equals("FEJL")) {
                    sendBeskedTilKlient("J_ER2: fejl i DATA-protokol");

                } else {

                        String beskeden = brugernavn + " sender besked:" + beskedTilAlle;
                        logBog.addAfsendtTransaktion(beskeden);
                        for(int i = 0; i < Listener.brugere.size(); i++){
                            if(!Listener.brugere.get(i).getBrugernavn().equals(brugernavn)){
                            Listener.brugere.get(i).getClientCoordinator().sendBeskedTilKlient(beskeden);
                            }
                            else{
                                Listener.brugere.get(i).getClientCoordinator().sendBeskedTilKlient("TAK");
                            }
                    }
                }

            } else if (beskedFraKlient.equals("QUIT") || beskedFraKlient.equals("NO_IMAV")) {
                fjernBruger();

            } else if (beskedFraKlient.equals("IMAV")) {
                sendBeskedTilKlient("Tak for at du fortsat er aktiv.");

            } else {
                sendBeskedTilKlient("J_ER3: ukendt fejl");
            }
        }


        public void sendBeskedTilKlient (String beskedTilKlient){
            try {
                sforb.getDataOutputStream().writeUTF(beskedTilKlient);
                logBog.addAfsendtTransaktion(beskedTilKlient);
                sforb.getDataOutputStream().flush();
            }  catch (IOException io) {
                System.out.println(io);
                sforb.lukForbindelse();
            }
        }

    /**
     *  Tilfoejer en bruger og sender en LIST over nye brugere til alle.
     */

       public void tilfoejBruger(String navn) {

               bruger = new Bruger(navn, this);
               Listener.brugere.add(bruger);
                for(int i = 0; i < Listener.brugere.size(); i++) {
                    Listener.brugere.get(i).getClientCoordinator().sendBeskedTilKlient("LIST " + Listener.brugere.toString());
                }
       }

    /**
     * Sletter en inaktiv eller stoppet klient fra listen.
     * Sender en bekræftelse til klienten om at det er OK at slukke.
     * QUITOK er dermed tilføjet til protokollen.
      */

        public void fjernBruger() {

            for(int i = 0; i < Listener.brugere.size(); i++) {
                if(Listener.brugere.get(i).getBrugernavn().equals(bruger.getBrugernavn())){
                    Listener.brugere.remove(i);
                }
            }
                sendBeskedTilKlient("QUITOK");
                sforb.lukForbindelse();

        }
}





