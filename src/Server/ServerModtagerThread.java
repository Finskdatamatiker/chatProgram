package Server;

import java.io.IOException;

public class ServerModtagerThread implements Runnable {
    /**
     * Klassen repræsenterer den tråd, som læser beskeder fra klienten og sender
     * dem videre til ClientCoordinator til at blive behandlet.
     */

    private ServerForbindelse sforb;
    private ClientCoordinator clientCoordinator;
    private LogBog logBog;
    //private final Object lockPaaListen = new Object();

    public ServerModtagerThread(ServerForbindelse sforb, ClientCoordinator clientCoordinator){
        this.sforb = sforb;
        this.clientCoordinator = clientCoordinator;
        logBog = new LogBog();
    }

    public ServerForbindelse getSforb() { return sforb; }
    public void setSforb(ServerForbindelse sforb) { this.sforb = sforb; }
    public ClientCoordinator getClientCoordinator() { return clientCoordinator; }
    public void setClientCoordinator(ClientCoordinator clientCoordinator) { this.clientCoordinator = clientCoordinator; }

    @Override
    public void run() {

            try{
                while(Listener.serverRunning) {

                    String beskedFraKlient = sforb.getDataInputStream().readUTF();
                    System.out.println(beskedFraKlient);
                    logBog.addModtagenTransaktion(beskedFraKlient);

                     clientCoordinator.behandlBesked(beskedFraKlient);

                }

            } catch (IOException io) {
                System.out.println(io);
            }
    }
}
