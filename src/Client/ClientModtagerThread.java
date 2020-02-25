package Client;

import java.io.IOException;
import java.util.Scanner;

public class ClientModtagerThread implements Runnable {
    /**
     * Deenne tråd tager imod beskeder fra serveren.
     * Den får forbindelsen (dermed dataoutput ph datainput knyttet til socket) og
     * ClientAfsenderThtread (som skal bede klienten om at bekræfte
     * et nyt brugernavn, hvis der er behov for det), som sine felter.
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

    public void run() {

            try {
                while (ClientAfsenderThread.clientRunning) {
                    String beskedFraServer = forb.getDataInputStream().readUTF();
                    System.out.println(beskedFraServer);

                    if(beskedFraServer.equals("J_ER1: ugyldigt username, valg et nyt navn:")){
                        clientAfsenderThread.setBedtOmEtNytNavn(true);
                    }

                }
            } catch (IOException io) {
                forb.lukForbindelse();
                System.out.println(io);
            }
        }


}

