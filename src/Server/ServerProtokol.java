package Server;

public class ServerProtokol {

    public ServerProtokol(){

    }



    public String[] laesJoinOgSplit(String besked) {
        String username = "";
        String serverIp = "";
        String serverPort = "";
        String[] gemtIArray;

        if(!besked.contains("JOIN")){
            return null;
        }

        //jeg fjerner ordet JOIN og mellemrum fra beskeden, hvis der er JOIN
        if (besked.indexOf("JOIN") == 0) {
            besked = besked.replace("JOIN ", "");
        }

        //Når jeg adskiller med komma, får jeg username på den første plads
        if (besked.contains(",")) {
            gemtIArray = besked.split(",");
            username = gemtIArray[0];
        }

        /*Når jeg adskiller med :, får jeg serverPort på den anden plads*/
        if (besked.contains(":")) {
            gemtIArray = besked.split(":");
            serverPort = gemtIArray[1];
        }

        /* Får at få server_ip, skal jeg først adskiller med mellemrum
        og det element derefter med :*/
        if (besked.contains(" ")) {
            gemtIArray = besked.split(" ");
            besked = gemtIArray[1];
            //nu er der kun serverIp:serverPort tilbage af beskeden
            gemtIArray = besked.split(":");
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

    public boolean erGyldigBrugernavn(String brugernavn){
        if(brugernavn.length() <= 12 && !brugernavn.matches("[^a-zA-Z0-9_\\-]+")){
            return true;
        }
        return false;
    }


    public String[] laesDataOgSplit(String besked) {

        String username = "";
        String beskeden = "";
        String[] gemtIArray;

        if(!besked.contains("DATA")){
            return null;
        }

        //jeg fjerner ordet DATA og mellemrum fra beskeden, hvis der er JOIN
        if (besked.indexOf("DATA") == 0) {
            besked = besked.replace("DATA ", "");
        }

        //Når jeg adskiller med komma, får jeg username på den første plads
        if (besked.contains(":")) {
            gemtIArray = besked.split(":");
            username = gemtIArray[0];
            beskeden = gemtIArray[1];
        }

        else{
            return null;
        }

        String[] infoFraData = new String[2];
        infoFraData[0] = username;
        infoFraData[1] = beskeden;
        return infoFraData;

    }

    public boolean erGyldigBesked(String besked){
        if(besked.length() <= 250){
            return true;
        }
        return false;
    }

    public String udregnNavnet(String beskedFraKlient){
        String navnUdenHeartBeat = beskedFraKlient.substring(0, beskedFraKlient.length()-7);
        return navnUdenHeartBeat;
    }
}
