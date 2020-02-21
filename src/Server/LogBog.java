package Server;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class LogBog {
    /**
     * Klassen gemmer alle modtagne og afsendte transaktioner med timestamp
     */

    private ArrayList<String> logModtagneTransaktioner = new ArrayList<>();
    private ArrayList<String> logAfsendteTransaktioner = new ArrayList<>();

    public LogBog(){}

    public ArrayList<String> getLogModtagneTransaktioner() {
        return logModtagneTransaktioner;
    }
    public void setLogModtagneTransaktioner(ArrayList<String> logModtagneTransaktioner) { this.logModtagneTransaktioner = logModtagneTransaktioner; }
    public ArrayList<String> getLogAfsendteTransaktioner() { return logAfsendteTransaktioner; }
    public void setLogAfsendteTransaktioner(ArrayList<String> logAfsendteTransaktioner) { this.logAfsendteTransaktioner = logAfsendteTransaktioner; }

    public void addModtagenTransaktion(String transaktion){
        LocalDateTime localDateTime = LocalDateTime.now();
        String transaktionMedTimestamp = transaktion + " timestamp: " + localDateTime;
        logModtagneTransaktioner.add(transaktionMedTimestamp);
    }

    public void addAfsendtTransaktion(String transaktion){
        LocalDateTime localDateTime = LocalDateTime.now();
        String transaktionMedTimestamp = transaktion + " timestamp: " + localDateTime;
        logAfsendteTransaktioner.add(transaktionMedTimestamp);
    }

    @Override
    public String toString() {
        return
                "logModtagneTransaktioner: " + logModtagneTransaktioner +
                ", logAfsendteTransaktioner: " + logAfsendteTransaktioner;
    }
}
