package Client;

import java.util.Scanner;

public class ConsoleReader {

    /**
     * Klassen har scanner til at læse brugerens beskeder fra consol
     * Scanneren læser hele linjen som String
     */

    private Scanner console = new Scanner(System.in);

    public ConsoleReader() {}

   public String laesInputFraConsole(){
        String input = console.nextLine();
        return input;
   }

}



