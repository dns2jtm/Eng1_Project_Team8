package game;

public class HiddenEvent1 {
    public boolean TR = false;
    public float X = 150;
    public float Y = 490;

    public boolean AE(float DST, FiveMinuteTimer GT) {
        if (!TR && DST < 35) {
            TR = true;
            GT.AS(20);
            return true;
        }
        return false;
    }
}
