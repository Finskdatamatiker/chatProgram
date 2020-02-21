package Client;

public class ClientProtokol {

    /**
     * ClientProkokollen er den samme som i serveren, men jeg vil holde packages adskilte.
     * Den kender reglerne for, hvorfor input er lovligt/ulovligt.
     * Dog er der den forskel, at her lader jeg ConsoleReader læse beskeder fra brugeren.
     */

    ConsoleReader consoleReader;

    public ClientProtokol(ConsoleReader consoleReader) {
        this.consoleReader = consoleReader;
    }

    /**
     * Metoden læser JOIN fra kunden og enten returnerer username, serverPort og serverIP i array af String
     * eller FEJL-beskeden. Egentlig kunne jeg her nøjes med at returnere username, fordi serverIP og
     * serverPort er rigtige (ellers ville beskeden ikke komme til serveren). Men jeg gemmer dem alligevel,
     * hvis jeg nu senere skulle bruge de oplysninger alligevel.
     * @return
     */

    public String[] laesJoinOgSplit() {
        String joinBesked = consoleReader.laesInputFraConsole();
        String username = "";
        String serverIp = "";
        String serverPort = "";
        String[] gemtIArray = new String[3];

        if (joinBesked.length() < 5 || !joinBesked.substring(0,5).equals("JOIN ")) {
                gemtIArray[0] = "FEJL";
                gemtIArray[1] = "FEJL";
                gemtIArray[2] = "FEJL";
                return gemtIArray;
        }
        else{
            if (joinBesked.indexOf("JOIN ") == 0) {
                joinBesked = joinBesked.replace("JOIN ", "");
            }
            if (joinBesked.contains(",")) {
                gemtIArray = joinBesked.split(",");
                username = gemtIArray[0];
            }
            if (joinBesked.contains(":")) {
                gemtIArray = joinBesked.split(":");
                serverPort = gemtIArray[1];
            }
            if (joinBesked.contains(" ")) {
                gemtIArray = joinBesked.split(" ");
                joinBesked = gemtIArray[1];
                //nu er der kun serverIp:serverPort tilbage af beskeden
                gemtIArray = joinBesked.split(":");
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
     * Metoden læser DATA-besked fra klienten og returnerer username og besked i array af String.
     * Jeg bruger split() til at adskille elementerne. Hvis beskeden ikke overholder protokollen,
     * returnerere jeg FEJL. Jeg skal gemme begge elemengter også i FEJL, fordi ellers får jeg nullpointer.
     * @return
     */
    public String[] laesDataOgSplit() {
        String besked = consoleReader.laesInputFraConsole();
        String[] gemtIArray = new String[2];

        //begge elementer, fordi ellers nullpointer
        if (besked.equals("QUIT")) {
            gemtIArray[0] = besked;
            gemtIArray[1] = besked;
            return gemtIArray;
        }
        //substring returnerer en ny String
        else if (besked.length() < 5 || !besked.substring(0,5).equals("DATA ") || !erGyldigBesked(besked) || !besked.contains(":")) {
            gemtIArray[0] = "FEJL";
            gemtIArray[1] = "FEJL";
            return gemtIArray;
        }
        else {
            //splitter username og besked med split og gemmer i array
            besked = besked.replace("DATA ", "");
            if (besked.contains(":")) gemtIArray = besked.split(":");

            return gemtIArray;
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

    public boolean erGyldigBesked(String besked){
        if(besked.length() <= 250){
            return true;
        }
        return false;
    }
}
