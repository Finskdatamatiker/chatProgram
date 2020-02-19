package Client2;

import java.io.IOException;

public class ClientModtagerThread2 implements Runnable {

    private Forbindelse2 forb2;
    //dette med henblik på at sende den rigtieg fejlbesked til console til klienten
    public boolean erJoined = false;

    public ClientModtagerThread2(Forbindelse2 forb2){
        this.forb2 = forb2;;
    }

    @Override
    public void run() {

        //betingelsen her? Main.isRunning?
        //er det ok at lukke forbindelsen, eller hvordan tvinger jeg til at starte forfra?
        for(;;) {
            try {
                String beskedFraServer = forb2.getDataInputStream().readUTF();
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
                System.out.println("Exception i input fra serveren til klient" + io);
                return;
            }
        }
     }


}
