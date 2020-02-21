package Server;

import Client.ClientMain;

import java.io.IOException;

public class ServerModtagerThread implements Runnable {

    private ServerForbindelse sforb;
    private ClientCoordinator clientCoordinator;
    private LogBog logBog;

    public ServerModtagerThread(ServerForbindelse sforb, ClientCoordinator clientCoordinator){
        this.sforb = sforb;
        this.clientCoordinator = clientCoordinator;
        logBog = new LogBog();
    }

    public ServerForbindelse getSforb() {
        return sforb;
    }
    public void setSforb(ServerForbindelse sforb) {
        this.sforb = sforb;
    }
    public ClientCoordinator getClientCoordinator() {
        return clientCoordinator;
    }
    public void setClientCoordinator(ClientCoordinator clientCoordinator) { this.clientCoordinator = clientCoordinator; }

    @Override
    public void run() {

            try{
                while(ClientMain.clientRunning) {
                    String beskedFraKlient = sforb.getDataInputStream().readUTF();
                    logBog.addModtagenTransaktion(beskedFraKlient);
                    clientCoordinator.behandlBesked(beskedFraKlient);
                }

            } catch (IOException io) {
                System.out.println(io);
            }
    }
}
