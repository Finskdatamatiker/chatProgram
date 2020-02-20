package Server;


import java.io.IOException;
import java.net.Socket;


public class ClientCoordinator implements Runnable {
    /**
     * Tråden, som sender beskeder til kunden
     */
    private ServerForbindelse sforb;
    private Socket socket;
    private ServerProtokol protokol;


    public ClientCoordinator(ServerForbindelse sforb, Socket socket, ServerProtokol protokol){
        this.sforb = sforb;
        this.socket = socket;
        this.protokol = protokol;
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

    @Override
    public void run() {

        sforb = new ServerForbindelse(socket);
        Thread serverModtagerThread = new Thread(new ServerModtagerThread(sforb, this));
        serverModtagerThread.start();
    }

    public void behandlBesked(String beskedFraKlient) {

            String svar = "Indtast en ny besked.";

            if (beskedFraKlient.contains("JOIN")) {
                String[] erDenJoin = protokol.laesJoinOgSplit(beskedFraKlient);
                String username = erDenJoin[0];

                if (Listener.brugere.size() == 0) {
                    tilfoejBruger(username);
                    svar = "J_OK";
                } else {

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
                if (beskedFraKlient.length() <= 250) {
                    String[] erDenData = protokol.laesDataOgSplit(beskedFraKlient);
                    String brugernavn = erDenData[0];
                    String beskedTilAlle = erDenData[1];

                    for (Bruger b : Listener.brugere) {
                        if (!b.getBrugernavn().equals(brugernavn))
                        b.getClientCoordinator().sendBeskedTilKlient(brugernavn + " sender besked: " + beskedTilAlle);
                    }
                }
            } else if (beskedFraKlient.equals("QUIT")) {
                sforb.lukForbindelse();

            } else if (beskedFraKlient.equals("IMAV")) {
                svar = "Tak for at du fortsat er aktiv.";

            }else if(beskedFraKlient.contains("NO_IMAV")){
                svar = "";
                String navnUdenHeartBeat = protokol.udregnNavnet(beskedFraKlient);

                for(int i = 0; i < Listener.brugere.size(); i++){
                    if (Listener.brugere.get(i).getBrugernavn().equals(navnUdenHeartBeat)){
                        Listener.brugere.remove(i);
                    }
                }
                sforb.lukForbindelse();
            } else {
                svar = "J_ER err_code: err_msg";
            }

            sendBeskedTilKlient(svar);
        }



        public void sendBeskedTilKlient (String beskedTilKlient){
            try {
                sforb.getDataOutputStream().writeUTF(beskedTilKlient);
                sforb.getDataOutputStream().flush();
            } catch (IOException io) {
                System.out.println(io);
            }
        }

        //tilfoej bruger og send listen til alle
       public void tilfoejBruger(String navn){
            if (Listener.brugere.size() < Listener.MAXTHREADS) {
                Bruger bruger = new Bruger(navn, this);
                Listener.brugere.add(bruger);
                for (Bruger b : Listener.brugere) {
                    b.getClientCoordinator().sendBeskedTilKlient("LIST " + Listener.brugere.toString());
                }
            } else {
                sendBeskedTilKlient("J_ER err_code: err_msg");
            }

    }





    public void setSforb(ServerForbindelse sforb) {
        this.sforb = sforb;
    }


    public ServerProtokol getProtokol() {
        return protokol;
    }

    public void setProtokol(ServerProtokol protokol) {
        this.protokol = protokol;
    }



}





