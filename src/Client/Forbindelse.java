package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Forbindelse {
    /**
     * Klassen laver datainput og dataoutputstream til klienten
     * Klassen er singleton, fordi der kun skal være én forbindelse per klient
     */

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private static Forbindelse singletonForbindelse;

    private Forbindelse(Socket socket){
        this.socket = socket;

        try{
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        }catch (IOException io){
            if(socket.isClosed()) {

                lukForbindelse();
            }
            System.out.println(io);
        }
    }

    public Socket getSocket() { return socket; }
    public void setSocket(Socket socket) { this.socket = socket; }
    public DataInputStream getDataInputStream() { return dataInputStream; }
    public void setDataInputStream(DataInputStream dataInputStream) { this.dataInputStream = dataInputStream; }
    public DataOutputStream getDataOutputStream() { return dataOutputStream; }
    public void setDataOutputStream(DataOutputStream dataOutputStream) { this.dataOutputStream = dataOutputStream; }

    public static Forbindelse givForbindelse(Socket socket){

        if(singletonForbindelse == null){
            singletonForbindelse = new Forbindelse(socket);
        }
        return singletonForbindelse;
    }


    public void lukForbindelse(){
        try{
            System.out.println("Forbindelsen er lukket.");
            dataInputStream.close();
            dataOutputStream.flush();
            dataOutputStream.close();
            socket.close();
        }catch (IOException io){
            System.out.println("Forbindelsen er lukket: " + io);
        }
    }
}
