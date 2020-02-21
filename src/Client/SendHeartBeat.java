package Client;

import java.io.IOException;

public class SendHeartBeat implements Runnable {

    /**
     * Denne tråd bliver laver i tråden ClientAfsenderThread og den sender heartbeat.
     * Hver gang brugeren sender en besked, bliver denne tråd bedt om at sende heartbeat til
     * serveren. Det sker ved at ClientAfsender opdaterer boolean "heartbeat" til true i denne klasse.
     * Heartbeaten sover 59 sekunder ad gangen og når den vågner, tjekker den, om heartbeat er true
     * eller false. Så hvis kunden har sendt heartbeat 55 sekunder inden, går der 59+55 sekunder,
     * inden forbindelsen lukkes. Men hvis kunden til gengæld lige har sendt en besked, går der ca.
     * 1 + 59 sekunder = et minut til at forbindelsen lukkes.
     */

    private Forbindelse forb;
    boolean heartbeat;
    String navnet;

    public SendHeartBeat(Forbindelse forb, boolean heartbeat, String navnet){
        this.heartbeat = heartbeat;
        this.forb = forb;
        this.navnet = navnet;
    }

    public boolean isHeartbeat() { return heartbeat; }
    public void setHeartbeat(boolean heartbeat) { this.heartbeat = heartbeat; }
    public String getNavnUdenHeartBeat() { return navnet; }
    public void setNavnUdenHeartBeat(String navnet) { this.navnet = navnet; }

    @Override
    public void run() {
        try{
            while(ClientAfsenderThread.clientRunning){
            Thread.sleep(59000);
                    try {
                        if(heartbeat){
                        forb.getDataOutputStream().writeUTF("IMAV");
                        heartbeat = false;}
                        else{
                            forb.getDataOutputStream().writeUTF("NO_IMAV");
                            System.out.println("Ingen heartbeat længere");
                            System.exit(0);
                        };
                    }catch (IOException io){
                        System.out.println("Forbindelsen lukkes " + io);
                        forb.lukForbindelse();
                    }
            }

        }catch (InterruptedException ie){
            System.out.println("Afbrudt søvn i heartbeat " + ie);
            Thread.currentThread().interrupt();
        }
    }
}
