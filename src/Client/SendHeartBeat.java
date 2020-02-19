package Client;

import java.io.IOException;

public class SendHeartBeat implements Runnable {

    private Forbindelse forb;
    boolean heartbeat;
    String navnet;

    public SendHeartBeat(Forbindelse forb, boolean heartbeat, String navnet){
        this.heartbeat = heartbeat;
        this.forb = forb;
        this.navnet = navnet;
    }

    public boolean isHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(boolean heartbeat) {
        this.heartbeat = heartbeat;
    }

    public String getNavnUdenHeartBeat() {
        return navnet;
    }

    public void setNavnUdenHeartBeat(String navnet) {
        this.navnet = navnet;
    }

    @Override
    public void run() {
        try{
            while(true){
            Thread.sleep(59000);
                    try {
                        if(heartbeat){
                        forb.getDataOutputStream().writeUTF("IMAV");
                        heartbeat = false;}
                        else{
                            forb.getDataOutputStream().writeUTF(navnet + "NO_IMAV");
                            System.out.println("ingen heartbeat længere");
                        };
                    }catch (IOException io){
                        System.out.println("Forbindelsen lukkes " + io);
                        forb.lukForbindelse();
                    }
            }

        }catch (InterruptedException ie){
            System.out.println("afbrudt søvn i heartbeat " + ie);
            Thread.currentThread().interrupt();
        }
    }
}
