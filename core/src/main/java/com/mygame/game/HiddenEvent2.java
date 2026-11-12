package game;

public class HiddenEvent2 {
    public boolean TR = false;
    public float X = 700;
    public float Y = 110;

    public boolean AE(float DST, FiveMinuteTimer GT) {
        if (!TR && DST < 35) {
            TR = true;
            GT.RS(15);
            return true;
        }
        return false;
    }
}
