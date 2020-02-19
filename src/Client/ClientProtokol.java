package Client;

public class ClientProtokol {

    ConsoleReader consoleReader;

    public ClientProtokol(ConsoleReader consoleReader) {
        this.consoleReader = consoleReader;
    }



    public String[] laesJoinOgSplit() {
        String joinBesked = consoleReader.laesInputFraConsole();
        String username = "";
        String serverIp = "";
        String serverPort = "";
        String[] gemtIArray;

        //jeg fjerner ordet JOIN og mellemrum fra beskeden, hvis der er JOIN
        if (joinBesked.indexOf("JOIN") == 0) {
            joinBesked = joinBesked.replace("JOIN ", "");
        }

        //Når jeg adskiller med komma, får jeg username på den første plads
        if (joinBesked.contains(",")) {
            gemtIArray = joinBesked.split(",");
            username = gemtIArray[0];
        }

        /*Når jeg adskiller med :, får jeg serverPort på den anden plads*/
        if (joinBesked.contains(":")) {
            gemtIArray = joinBesked.split(":");
            serverPort = gemtIArray[1];
        }

        /* Får at få server_ip, skal jeg først adskiller med mellemrum
        og det element derefter med :*/
        if (joinBesked.contains(" ")) {
            gemtIArray = joinBesked.split(" ");
            joinBesked = gemtIArray[1];
            //nu er der kun serverIp:serverPort tilbage af beskeden
            gemtIArray = joinBesked.split(":");
            serverIp = gemtIArray[0];
        }
        else{
            return null;
        }

        //alle info samlet i array
        String[] infoFraJoin = new String[3];
        infoFraJoin[0] = username;
        infoFraJoin[1] = serverIp;
        infoFraJoin[2] = serverPort;

        return infoFraJoin;
    }

    public String[] laesDataOgSplit() {
        String besked = consoleReader.laesInputFraConsole();
        String[] gemtIArray = new String[2];

        if(besked.equals("QUIT")){
            String[] fejl = new String[1];
            fejl[0] = besked;
            return fejl;
        }

        //jeg fjerner ordet DATA og mellemrum fra beskeden, hvis der er JOIN
        if (besked.indexOf("DATA") == 0) {
            besked = besked.replace("DATA ", "");
        }

        //Når jeg adskiller med komma, får jeg username på den første plads
        if (besked.contains(":")) {
            gemtIArray = besked.split(":");
            return gemtIArray;
        }
        else if(!erGyldigBesked(besked)){
            return null;
        }
        else{
            return null;
        }
    }


    public boolean tjekOmQuit(String besked) {
        if (besked.equals("QUIT")){
            return true;
        }
        return false;
    }

    /*
     husk + til sidst for at tjekke multiple tegn
     */
    public boolean erGyldigBrugernavn(String brugernavn){
        if(brugernavn.length() <= 12 && !brugernavn.matches("[^a-zA-Z0-9_\\-]+")){
            return true;
        }
        return false;
    }

    public boolean erGyldigBesked(String besked){
        if(besked.length() <= 250){
            return true;
        }
        return false;
    }


}
