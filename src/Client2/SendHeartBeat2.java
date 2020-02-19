package Client2;

import java.io.IOException;

public class SendHeartBeat2 implements Runnable {

    private Forbindelse2 forb;
    boolean heartbeat2 = true;
    String navnet;

    public SendHeartBeat2(Forbindelse2 forb, boolean heartbeat2, String navnet){
        this.forb = forb;
        this.heartbeat2 = heartbeat2;
        this.navnet = navnet;
    }

    public boolean isHeartbeat2() {
        return heartbeat2;
    }

    public void setHeartbeat2(boolean heartbeat2) {
        this.heartbeat2 = heartbeat2;
    }

    public String getNavnet() {
        return navnet;
    }

    public void setNavnet(String navnet) {
        this.navnet = navnet;
    }

    @Override
    public void run() {
        try{
            while(true) {
                Thread.sleep(59000);
                try {
                    if (heartbeat2) {
                        forb.getDataOutputStream().writeUTF("IMAV");
                        heartbeat2 = false;
                    } else {

                        forb.getDataOutputStream().writeUTF(navnet + "NO_IMAV");
                        System.out.println("ingen heartbeat længere");
                    }
                    ;
                } catch (IOException io) {
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
