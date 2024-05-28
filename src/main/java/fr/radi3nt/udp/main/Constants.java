package fr.radi3nt.udp.main;

public class Constants {

    public static final int INACTIVE_TIMEOUT = 200;
    public static final int ACTIVE_TIMEOUT = 5;

    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
