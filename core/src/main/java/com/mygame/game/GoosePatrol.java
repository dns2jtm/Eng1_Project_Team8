package game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class GoosePatrol extends ApplicationAdapter {
    private ShapeRenderer SR;
    private SpriteBatch SB;
    private BitmapFont BF;

    private float PX = 400;
    private float PY = 100;
    private float G1X = 250, G1Y = 350;
    private float G2X = 650, G2Y = 350;
    private boolean G1Gone = false;
    private boolean G2Gone = false;

    private Item1 II1 = new Item1();
    private Item2 II2 = new Item2();
    private HiddenEvent1 HE1 = new HiddenEvent1();
    private HiddenEvent2 HE2 = new HiddenEvent2();
    private FiveMinuteTimer GT = new FiveMinuteTimer();
    private UsageCounters GC = new UsageCounters();

    private boolean PA = true;
    private boolean GO = false;
    private String MSG = "Press P to start";

    @Override
    public void create() {
        Gdx.graphics.setWindowedMode(800, 600);
        SR = new ShapeRenderer();
        SB = new SpriteBatch();
        BF = new BitmapFont();
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) && !GO) {
            PA = !PA;
        }
        GT.UT(!PA && !GO);

        if (!PA && !GO) {
            MP();
            CI();
            CE();
            MG();
            if (G1Gone && G2Gone) {
                GO = true;
                MSG = "YOU WIN! Both geese are gone.";
            } else if (GT.IF()) {
                GO = true;
                MSG = "TIME UP!";
            }
        }
        DG();
    }

    private void MP() {
        float ST = 200 * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) PY += ST;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) PY -= ST;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) PX -= ST;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) PX += ST;
        PX = Math.max(20, Math.min(780, PX));
        PY = Math.max(20, Math.min(530, PY));
    }

    private float DST(float X, float Y) {
        float DX = PX - X;
        float DY = PY - Y;
        return (float) Math.sqrt(DX * DX + DY * DY);
    }

    private void CI() {

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) && !G1Gone) {
            if (II1.UI(DST(G1X, G1Y))) {
                G1Gone = true;
                GC.AIU();
                MSG = "You fed a goose!";
            } else {
                MSG = "Food already used, or too far from goose 1.";
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && !G2Gone) {
            if (II2.UI(DST(G2X, G2Y))) {
                G2Gone = true;
                GC.AIU();
                MSG = "You scared a goose!";
            } else {
                MSG = "Whistle already used, or too far from goose 2.";
            }
        }
    }

    private void CE() {

        if (HE1.AE(DST(HE1.X, HE1.Y), GT)) {
            GC.AET();
            MSG = "Lucky shortcut: +20 seconds!";
        }
        if (HE2.AE(DST(HE2.X, HE2.Y), GT)) {
            GC.AET();
            MSG = "Slippery path: -15 seconds!";
        }
    }

    private void MG() {
        float ST = 35 * Gdx.graphics.getDeltaTime();
        if (!G1Gone) {
            float D = DST(G1X, G1Y);
            if (D > 1) {
                G1X += (PX - G1X) / D * ST;
                G1Y += (PY - G1Y) / D * ST;
            }
        }
        if (!G2Gone) {
            float D = DST(G2X, G2Y);
            if (D > 1) {
                G2X += (PX - G2X) / D * ST;
                G2Y += (PY - G2Y) / D * ST;
            }
        }
    }

    private void DG() {
        Gdx.gl.glClearColor(0.12f, 0.32f, 0.17f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        SR.begin(ShapeRenderer.ShapeType.Filled);
        SR.setColor(Color.FOREST);
        SR.rect(10, 10, 780, 530);
        SR.setColor(Color.BLUE);
        SR.circle(PX, PY, 15);
        SR.setColor(Color.WHITE);
        if (!G1Gone) SR.circle(G1X, G1Y, 18);
        if (!G2Gone) SR.circle(G2X, G2Y, 18);

        SR.end();

        SB.begin();
        BF.setColor(Color.WHITE);
        int T = GT.GSL();
        BF.draw(SB, String.format("Time: %02d:%02d", T / 60, T % 60), 20, 585);
        BF.draw(SB, "Items used: " + GC.IU + "   Events: " + GC.ET, 170, 585);
        BF.draw(SB, "WASD/arrows move | 1 food | 2 whistle | P pause", 20, 555);
        BF.draw(SB, MSG, 20, 40);
        if (PA && !GO) BF.draw(SB, "PAUSED - press P to play", 305, 300);
        if (GO) BF.draw(SB, MSG, 300, 300);
        SB.end();
    }

    @Override
    public void dispose() {
        SR.dispose();
        SB.dispose();
        BF.dispose();
    }
}
