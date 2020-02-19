package Client2;

public class ClientMainTo {
        //styr med denne, at de andre også kører
        public static boolean clientRunning;

        public static void main(String[] args) {

            ClientAfsenderThread2 clientAfsenderThread2 = new ClientAfsenderThread2();
            Thread threadClient2 = new Thread(clientAfsenderThread2);
            threadClient2.start();

        }

}
