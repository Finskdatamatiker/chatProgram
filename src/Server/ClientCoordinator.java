package Server;

import java.io.IOException;
import java.net.Socket;
/**
 * Klassen Listener importeret, fordi jeg bruger en statisk variabel derfra
 */
import static Server.Listener.*;

public class ClientCoordinator implements Runnable {
    /**
     * Klassen er tråden, som behandler modtagne beskeder (ServerModtagerThread beder den om det),
     * og giver et svar til kunden.
     * Den repræsenterer én klient ad gangen og får socket til klienten fra Listener.
     * Klassen laver en anden tråd, som læser beskederne fra klienten (ServerModtagerThread).
     * Klassen tilføjer brugeren til brugerlisten, sletter inaktive brugere og giver svaret til
     * kunden i overenstemmelse med reglerne i klassen ServerProtokol.
     * Klassen tilføjer også alle transaktioner til LogBog.
     *
     * TO_DO: Jeg har endnu ikke kunnet håndtere det at slette en bruger, som
     * IKKE giver QUIT besked, men bare lukker forbindelsen uden advarsel.
     * Forbindelsen lukkes, men brugeren figurerer fortsat på listen.
     * Dette giver en connection.reset advarsel.
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
     *  NO_IMAV er tilføjet til protokollen for manglende heeartbeat fra klienten.
     *  FEJL er tilføjet til internt brug og er ikke en del af protokollen som sådan.
     * @param beskedFraKlient som serverModtagerThread læser
     */

    public void behandlBesked(String beskedFraKlient) {

        /*iterator, dvs. foreach giver ConcurrentModificationException
          så jeg looper i for-loop*/

                if (beskedFraKlient.contains("JOIN")) {
                String[] erDenJoin = protokol.laesJoinOgSplit(beskedFraKlient);
                String username = erDenJoin[0];

                //hvis ingen brugere endnu
                if(Listener.brugere.size() == 0){
                    tilfoejBruger(username);
                    sendBeskedTilKlient("J_OK");
                }
                else {
                    /*
                      Ellers tjekker vi, om brugeren allerede er på listen.
                      Hvis ikke, tilføjes brugeren til listen.
                      Klienten får en besked.
                     */
                        String beskedTilKlienten = "J_OK";
                        for (int i = 0; i < brugere.size(); i++) {
                            String navn = brugere.get(i).getBrugernavn();

                            if (navn.equals(username) || !protokol.erGyldigBrugernavn(username)) {
                                beskedTilKlienten = "J_ER1: ugyldigt username, valg et nyt navn:";
                            }
                        }

                        if(beskedTilKlienten.equals("J_ER1: ugyldigt username, valg et nyt navn:")){
                            sendBeskedTilKlient(beskedTilKlienten);
                        }else {
                            tilfoejBruger(username);
                            sendBeskedTilKlient(beskedTilKlienten);
                        }
                 }

                //her behandles DATA-beskeder
            } else if (beskedFraKlient.contains("DATA")) {
                String[] erDenData = protokol.laesDataOgSplit(beskedFraKlient);
                String brugernavn = erDenData[0];
                String beskedTilAlle = erDenData[1];

                if (beskedTilAlle.equals("FEJL")) {
                    sendBeskedTilKlient("J_ER2: fejl i DATA-protokol");

                } else {
                        //alle andre end afsenderen får beskeden
                        String beskeden = brugernavn + " sender besked:" + beskedTilAlle;
                        logBog.addAfsendtTransaktion(beskeden);
                        for(int i = 0; i < brugere.size(); i++){
                            if(!brugere.get(i).getBrugernavn().equals(brugernavn)){
                             brugere.get(i).getClientCoordinator().sendBeskedTilKlient(beskeden);
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

    /**
     * Metoden skriver besked til kunden og tilføjer beskeden til logbogen.
     * @param beskedTilKlient
     */
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
               brugere.add(bruger);
                for(int i = 0; i < brugere.size(); i++) {
                    brugere.get(i).getClientCoordinator().sendBeskedTilKlient("LIST " + brugere.toString());
                }
       }

    /**
     * Sletter en inaktiv eller stoppet klient fra listen.
     * Elementerne er unikke på listen, fordi jeg ikke accepteret brugere med
     * det samme navn, så jeg risikerer ikke at slette flere brugere, selv om remove
     * bliver advaret som suspect.
     * Brugere får en opdateret LIST.
      */

        public void fjernBruger() {

            for(int i = 0; i < brugere.size(); i++) {
                if(brugere.get(i).getBrugernavn().equals(bruger.getBrugernavn())){
                    brugere.remove(i);
                }
            }
                sforb.lukForbindelse();

        }
}





