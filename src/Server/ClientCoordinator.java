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
     * @param beskedFraKlient som serverModtagerThread læser
     */

    public void behandlBesked(String beskedFraKlient) {

            String svar = "";

            if(beskedFraKlient.substring(0,3).equals("N_N")) {
                String nytNavn = beskedFraKlient.substring(3);
                boolean eksistererBruger = false;
                for (Bruger b : Listener.brugere) {
                    String navn = b.getBrugernavn();
                    if (navn.equals(nytNavn)) {
                        eksistererBruger = true;
                    }

                    if (eksistererBruger || !protokol.erGyldigBrugernavn(nytNavn)) {
                        //svar = "J_ER err_code: err_msg";
                        svar = "J_ER1: ugyldigt username";
                    } else {
                        System.out.println("kommer jeg her overhovedet " + nytNavn);
                        b.setBrugernavn(nytNavn);
                        svar = "J_OK";
                    }
                }
            }

            if (beskedFraKlient.contains("JOIN")) {
                String[] erDenJoin = protokol.laesJoinOgSplit(beskedFraKlient);
                String username = erDenJoin[0];

                //Hvis listen er tom til at starte med
                if (Listener.brugere.size() == 0) {
                    tilfoejBruger(username);
                    svar = "J_OK";
                }
                else {
                    boolean erBrugerPaaListen = false;
                    for (Bruger b : Listener.brugere) {
                        String navn = b.getBrugernavn();
                        if (navn.equals(username)) erBrugerPaaListen = true;}

                        if (erBrugerPaaListen || !protokol.erGyldigBrugernavn(username)) {
                            //svar = "J_ER err_code: err_msg";
                            svar = "J_ER1: ugyldigt username";
                        } else {
                            tilfoejBruger(username);
                            svar = "J_OK";
                        }
                }
            } else if (beskedFraKlient.contains("DATA")) {

                    String[] erDenData = protokol.laesDataOgSplit(beskedFraKlient);
                    String brugernavn = erDenData[0];
                    String beskedTilAlle = erDenData[1];

                    if(beskedTilAlle.equals("FEJL")){
                        svar = "Husk protokollen: DATA brugernavn: besked";
                    }
                    //besked, som sendes til alle bortset fra afsenderen
                    for (Bruger b : Listener.brugere) {
                        String beskeden = brugernavn + " sender besked: " + beskedTilAlle;
                        logBog.addAfsendtTransaktion(beskeden);
                        if (!b.getBrugernavn().equals(brugernavn))
                        b.getClientCoordinator().sendBeskedTilKlient(beskeden);
                    }

            } else if (beskedFraKlient.equals("QUIT") || beskedFraKlient.equals("NO_IMAV")) {
                svar = "";
                fjernBruger();

            } else if (beskedFraKlient.equals("IMAV")) {
                svar = "Tak for at du fortsat er aktiv.";

            } else {
                svar = "J_ER err_code: err_msg";
            }
            //svar til kunden
            logBog.addAfsendtTransaktion(svar);
            sendBeskedTilKlient(svar);
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
     *  Jeg looper med iterator-objektet
     */

       public void tilfoejBruger(String navn){

           bruger = new Bruger(navn, this);
           Listener.brugere.add(bruger);

           Iterator<Bruger> iterator = Listener.brugere.iterator();
           Bruger brugeren;
           while(iterator.hasNext()){
               brugeren = iterator.next();
               brugeren.getClientCoordinator().sendBeskedTilKlient("LIST " + Listener.brugere.toString());
               }
           }

    /**
     * Sletter en inaktiv eller stoppet klient fra listen.
     * Sender en bekræftelse til klienten om at det er OK at slukke.
     * QUITOK er dermed tilføjet til protokollen.
     * Jeg looper med iterator-objektet.
      */

        public void fjernBruger(){
           Iterator<Bruger> iterator = Listener.brugere.iterator();
           Bruger brugeren = null;
           while(iterator.hasNext()){
              brugeren = iterator.next();
              if(brugeren.getBrugernavn().equals(bruger.getBrugernavn())){
                iterator.remove();
            }
        }
            sendBeskedTilKlient("QUITOK");
            sforb.lukForbindelse();
        }

}





