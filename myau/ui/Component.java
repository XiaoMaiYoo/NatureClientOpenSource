package myau.ui;

import java.util.concurrent.atomic.AtomicInteger;

public interface Component {
    public void draw(AtomicInteger var1);

    public void update(int var1, int var2);

    public void mouseDown(int var1, int var2, int var3);

    public void mouseReleased(int var1, int var2, int var3);

    public void keyTyped(char var1, int var2);

    public void setComponentStartAt(int var1);

    public int getHeight();

    public boolean isVisible();
}
