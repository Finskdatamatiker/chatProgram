package Client;

import java.io.IOException;

public class ClientModtagerThread implements Runnable {

    private Forbindelse forb;
    //dette med henblik på at sende den rigtieg fejlbesked til console til klienten
    public boolean erJoined = false;

    public ClientModtagerThread(Forbindelse forb){
        this.forb = forb;;
    }

    @Override
    public void run() {

        //betingelsen her? Main.isRunning?
        //er det ok at lukke forbindelsen, eller hvordan tvinger jeg til at starte forfra?
        for(;;) {
            try {
                String beskedFraServer = forb.getDataInputStream().readUTF();
                switch (beskedFraServer){
                    case "J_OK":
                    System.out.println("Client er godkendt: " + beskedFraServer);
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
                    default:
                        System.out.println(beskedFraServer);
                        break;}
            } catch (IOException io) {
                System.out.println("Exception i input fra serveren til klient " + io);
                return;
            }
        }
     }


}
