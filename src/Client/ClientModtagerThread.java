package Client;

import java.io.IOException;

public class ClientModtagerThread implements Runnable {
    /**
     * Deenne tråd tager imod beskeder fra serveren.
     * Den for forbindelsen (dermed dataoutput ph datainput knyttet til socket) og
     * ClientAfsenderThtread, som håndterer disse beskeder fra serveren, som sine felter.
     */

    private Forbindelse forb;
    private ClientAfsenderThread clientAfsenderThread;


    public ClientModtagerThread(Forbindelse forb, ClientAfsenderThread clientAfsenderThread){
        this.forb = forb;
        this.clientAfsenderThread = clientAfsenderThread;
    }

    public Forbindelse getForb() {
        return forb;
    }
    public void setForb(Forbindelse forb) {
        this.forb = forb;
    }
    public ClientAfsenderThread getClientAfsenderThread() { return clientAfsenderThread; }
    public void setClientAfsenderThread(ClientAfsenderThread clientAfsenderThread) { this.clientAfsenderThread = clientAfsenderThread; }

    @Override
    public void run() {

            try {
             while(ClientAfsenderThread.clientRunning) {
                 String beskedFraServer = forb.getDataInputStream().readUTF();
                 clientAfsenderThread.behandlBesked(beskedFraServer);
             }
            } catch (IOException io) {
                forb.lukForbindelse();
                System.out.println(io);
            }
        }
     }

