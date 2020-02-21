package Client;

import java.io.IOException;

public class ClientModtagerThread implements Runnable {
    /**
     * Deenne tråd tager imod beskeder fra serveren.
     */

    private Forbindelse forb;
    //denne boolean med henblik på at sende den rigtige fejlbesked til console til klienten
    public boolean erJoined = false;

    public ClientModtagerThread(Forbindelse forb){
        this.forb = forb;;
    }

    @Override
    public void run() {

        while(ClientAfsenderThread.clientRunning) {
            try {
                String beskedFraServer = forb.getDataInputStream().readUTF();
                switch (beskedFraServer){
                    case "J_OK":
                    System.out.println("Serveren godkender: " + beskedFraServer);
                    erJoined = true;
                    break;
                    //jeg mangler at lave forskellige fejlmeldinger og fejlbeskeder fra serveren
                    case "J_ER err_code: err_msg":
                        if(erJoined){
                            System.out.println("Skriv en besked således: DATA brugernavn: besked og max 250 tegn");
                        }else {
                        System.out.println("Client er ikke godkendt: " + beskedFraServer);
                        System.out.println("Skriv en ny JOIN-besked med et nyt brugernavn: JOIN username, serverIp:serverPort");}
                        break;
                    case "QUITOK":
                        System.exit(0);
                        break;
                    default:
                        System.out.println(beskedFraServer);
                        break;}
            } catch (IOException io) {
                forb.lukForbindelse();
                System.out.println(io);
                return;
            }
        }
     }


}
