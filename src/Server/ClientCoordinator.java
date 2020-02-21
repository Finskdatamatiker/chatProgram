package Server;


import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;


public class ClientCoordinator implements Runnable {
    /**
     * Tråden, som sender beskeder til kunden
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

    public void behandlBesked(String beskedFraKlient) {

            String svar = "";

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
                            svar = "J_ER err_code: err_msg";
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

        //tilfoej bruger og send listen til alle
       public void tilfoejBruger(String navn){
            if (Listener.brugere.size() < Listener.MAXTHREADS) {
                bruger = new Bruger(navn, this);
                Listener.brugere.add(bruger);
                for (Bruger b : Listener.brugere) {
                    b.getClientCoordinator().sendBeskedTilKlient("LIST " + Listener.brugere.toString());
                }
            } else {
                sendBeskedTilKlient("J_ER err_code: err_msg");
            }
    }

    public void fjernBruger(){
        Iterator<Bruger> iterator = Listener.brugere.iterator();
        while(iterator.hasNext()){
            Bruger bruger = iterator.next();
            if(bruger.getBrugernavn().equals(getBruger().getBrugernavn())){
                iterator.remove();
            }
        }
            sendBeskedTilKlient("QUITOK");
            sforb.lukForbindelse();
        }

}





