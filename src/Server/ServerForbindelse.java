package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerForbindelse {

    /**
     * Jeg skal bruge mange forbindelser, dvs. en til hver klient, så den kan IKKE laves til singleton som hos klienten.
     *  Forbindelsen laver datatinputstream og dataoutputstream vil socket og står for at lukke forbindelsen,
     *  når den bliver bedt om at gøre det.
     */
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public ServerForbindelse(Socket socket){

        this.socket = socket;
        try{
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        }catch (IOException io){
            System.out.println(io);
            lukForbindelse();
        }
    }

    public Socket getSocket() { return socket; }
    public void setSocket(Socket socket) { this.socket = socket; }
    public DataInputStream getDataInputStream() { return dataInputStream; }
    public void setDataInputStream(DataInputStream dataInputStream) { this.dataInputStream = dataInputStream; }
    public DataOutputStream getDataOutputStream() { return dataOutputStream; }
    public void setDataOutputStream(DataOutputStream dataOutputStream) { this.dataOutputStream = dataOutputStream; }


    public void lukForbindelse (){
        try {
                dataOutputStream.flush();
                dataInputStream.close();
                dataOutputStream.close();
                socket.close();

        }catch (IOException io){
            System.out.println(io);
        }
    }

}
