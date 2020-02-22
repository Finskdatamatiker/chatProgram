package Client;

import java.io.IOException;

public class ClientModtagerThread implements Runnable {
    /**
     * Deenne tråd tager imod beskeder fra serveren.
     * De bliver printet til brugeren, men det er faktisk ClientAfsenderThread,
     * som tjekker, at klienten overholder protokollen ud fra det, som brugeren
     * indtaster til console.
     */

    private Forbindelse forb;

    private ClientAfsenderThread clientAfsenderThread;
    private boolean usernameDuerIkke = false;


    public ClientModtagerThread(Forbindelse forb){
        this.forb = forb;;
    }

    public Forbindelse getForb() {
        return forb;
    }

    public void setForb(Forbindelse forb) {
        this.forb = forb;
    }


    public boolean isUsernameDuerIkke() {
        return usernameDuerIkke;
    }

    public void setUsernameDuerIkke(boolean usernameDuerIkke) {
        this.usernameDuerIkke = usernameDuerIkke;
    }

    @Override
    public void run() {

        while(ClientAfsenderThread.clientRunning) {
            try {
                String beskedFraServer = forb.getDataInputStream().readUTF();
                clientAfsenderThread.behandlBesked(beskedFraServer);


              //  switch (beskedFraServer){
                  /*  case "J_ER0: fejl i JOIN-protokol":
                        System.out.println("Skriv igen: " + beskedFraServer);
                        break;*/
                /*    case "J_OK":
                        System.out.println("Serveren godkender: " + beskedFraServer);
                        usernameDuerIkke = false;
                        System.out.println(usernameDuerIkke);
                        break;
                    case "J_ER1: ugyldigt username":
                        System.out.println("Serveren afviser username, så valg et nyt navn: " + beskedFraServer);
                        usernameDuerIkke = true;
                        System.out.println(usernameDuerIkke + "her true?");
                        break;
                 /*   case "J_ER2: fejl i DATA-protokollen":
                        System.out.println("Skriv igen: " + beskedFraServer);
                        break;
                    case "J_ER3: anden fejl":
                        System.out.println(beskedFraServer);
                        break;*/
          /*          case "QUITOK":
                        System.exit(0);
                        break;
                    default:
                        System.out.println(beskedFraServer);
                        break;}*/
            } catch (IOException io) {
                forb.lukForbindelse();
                System.out.println(io);
                return;
            }
        }
     }


}
