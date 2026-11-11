package game;

public class Item2 {
    public boolean U = false;

    public boolean UI(float DST) {
        if (!U && DST < 180) {
            U = true;
            return true;
        }
        return false;
    }
}
