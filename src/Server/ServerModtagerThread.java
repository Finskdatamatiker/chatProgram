package Server;

import java.io.IOException;

public class ServerModtagerThread implements Runnable {

    private ServerForbindelse sforb;
    private ClientCoordinator clientCoordinator;

    public ServerModtagerThread(ServerForbindelse sforb, ClientCoordinator clientCoordinator){
        this.sforb = sforb;
        this.clientCoordinator = clientCoordinator;
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

    public void setClientCoordinator(ClientCoordinator clientCoordinator) {
        this.clientCoordinator = clientCoordinator;
    }

    @Override
    public void run() {
        while(true){
            try{
                while(true) {
                    String beskedFraKlient = sforb.getDataInputStream().readUTF();
                    clientCoordinator.behandlBesked(beskedFraKlient);
                }
            }catch (IOException io){
                System.out.println(io);
                return;
            }
        }
    }
}