package Client;

import java.util.Scanner;

public class ConsoleReader {

    /**
     * har scanner til at læse fra consol og verificere
     */

    private Scanner console = new Scanner(System.in);

    public ConsoleReader() {
    }

   public String laesInputFraConsole(){
        String input = console.nextLine();
        return input;
   }

}



