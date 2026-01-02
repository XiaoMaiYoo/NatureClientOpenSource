package myau.ui.dataset;

public abstract class Slider {
    public abstract double getInput();

    public abstract double getMin();

    public abstract double getMax();

    public abstract void setValue(double var1);

    public abstract String getName();

    public abstract String getValueString();

    public abstract double getIncrement();

    public abstract boolean isVisible();
}
