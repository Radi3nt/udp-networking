package fr.radi3nt.udp.reliable;

public class NoReliabilityService implements ReliabilityService {

    public static final ReliabilityService INSTANCE = new NoReliabilityService();

    @Override
    public void update() {

    }
}
