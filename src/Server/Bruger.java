package Server;

public class Bruger {
    private String brugernavn;
    ClientCoordinator clientCoordinator;

    public Bruger(String brugernavn, ClientCoordinator clientCoordinator){
        this.brugernavn = brugernavn;
        this.clientCoordinator = clientCoordinator;
    }

    public String getBrugernavn() {
        return brugernavn;
    }
    public void setBrugernavn(String brugernavn) {
        this.brugernavn = brugernavn;
    }
    public ClientCoordinator getClientCoordinator() {
        return clientCoordinator;
    }
    public void setClientCoordinator(ClientCoordinator clientCoordinator) { this.clientCoordinator = clientCoordinator;}

    @Override
    public String toString() {
        return brugernavn;
    }
}
