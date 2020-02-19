package Client2;

import java.util.Scanner;

public class ConsoleReader2 {

    /**
     * har scanner til at læse fra consol og verificere
     */

    private Scanner console = new Scanner(System.in);

    public ConsoleReader2() {
    }

   public String laesInputFraConsole(){
        String input = console.nextLine();
        return input;
   }

}



