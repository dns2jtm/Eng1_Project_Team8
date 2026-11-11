package game;

public class FiveMinuteTimer {
    private double SL = 300;
    private long LT = System.nanoTime();

    public void UT(boolean RUN) {
        long NOW = System.nanoTime();
        if (RUN) {
            double EL = (NOW - LT) / 1_000_000_000.0;
            SL = Math.max(0, SL - EL);
        }
        LT = NOW;
    }

    public void AS(int SEC) {
        SL = Math.min(300, SL + SEC);
    }

    public void RS(int SEC) {
        SL = Math.max(0, SL - SEC);
    }

    public int GSL() {
        return (int) Math.ceil(SL);
    }

    public boolean IF() {
        return SL <= 0;
    }
}
