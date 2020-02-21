package Server;

public class ServerProtokol {

    /**
     * ServerProkokollen er den samme som hos klienten, men jeg vil holde packages adskilte.
     * (Dog har klienten en console-reader i protokolklassen til at læse beskeder fra console.)
     * Den kender reglerne for, hvorfor input er lovligt/ulovligt.
     */

    public ServerProtokol(){}

    /**
     * Metoden læser JOIN fra kunden og enten returnerer username, serverPort og serverIP i array af String
     * eller FEJL-beskeden. Egentlig kunne jeg her nøjes med at returnere username, fordi serverIP og
     * serverPort er rigtige (ellers ville beskeden ikke komme til serveren). Men jeg gemmer dem alligevel,
     * hvis jeg nu senere skulle bruge de oplysninger alligevel.
     * @param besked
     * @return
     */
    public String[] laesJoinOgSplit(String besked) {
        String username = "";
        String serverIp = "";
        String serverPort = "";
        String[] gemtIArray = new String[3];

        if (besked.length() < 5 || !besked.substring(0,5).equals("JOIN ")) {
            gemtIArray[0] = "FEJL";
            gemtIArray[1] = "FEJL";
            gemtIArray[2] = "FEJL";
            return gemtIArray;
        }
        else{
            if (besked.indexOf("JOIN ") == 0) {
                besked = besked.replace("JOIN ", "");
            }
            if (besked.contains(",")) {
                gemtIArray = besked.split(",");
                username = gemtIArray[0];
            }
            if (besked.contains(":")) {
                gemtIArray = besked.split(":");
                serverPort = gemtIArray[1];
            }
            if (besked.contains(" ")) {
                gemtIArray = besked.split(" ");
                besked = gemtIArray[1];
                //nu er der kun serverIp:serverPort tilbage af beskeden
                gemtIArray = besked.split(":");
                serverIp = gemtIArray[0];
            }

            //alle info samlet i array
            String[] infoFraJoin = new String[3];
            infoFraJoin[0] = username;
            infoFraJoin[1] = serverIp;
            infoFraJoin[2] = serverPort;

            return infoFraJoin;
        }

    }

    /**
     * Metoden tjekker, at brugernavn lever op til protokollen.
     * I regex begrænser jeg tegnene. Plus betyder, at man tjekker flere tegn.
     * @param brugernavn
     * @return true eller false
     */
    public boolean erGyldigBrugernavn(String brugernavn){
        if(brugernavn.length() <= 12 && brugernavn.matches("[a-zA-Z0-9_\\-]+")){
            return true;
        }
        return false;
    }


    /**
     * Metoden læser DATA-besked fra klienten og returnerer username og besked i array af String.
     * Jeg bruger split() til at adskille elementerne. Hvis beskeden ikke overholder protokollen,
     * returnerere jeg FEJL. Jeg skal gemme begge elemengter også i FEJL, fordi ellers får jeg nullpointer.
     * @param besked
     * @return
     */
    public String[] laesDataOgSplit(String besked) {

        String[] gemtIArray = new String[2];

        if (besked.length() < 5 || !besked.substring(0,5).equals("DATA ") || besked.length() > 250 || !besked.contains(":")) {
            gemtIArray[0] = "FEJL";
            gemtIArray[1] = "FEJL";
        }
        else {

            besked = besked.replace("DATA ", "");
            if (besked.contains(":")) gemtIArray = besked.split(":");

        }
        return gemtIArray;

    }

}
