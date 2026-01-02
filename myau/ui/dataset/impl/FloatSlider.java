package myau.ui.dataset.impl;

import myau.enums.ChatColors;
import myau.property.properties.FloatProperty;
import myau.ui.dataset.Slider;

public class FloatSlider
extends Slider {
    private final FloatProperty property;

    public FloatSlider(FloatProperty property) {
        this.property = property;
    }

    @Override
    public double getInput() {
        return ((Float)this.property.getValue()).floatValue();
    }

    @Override
    public double getMin() {
        return this.property.getMinimum().floatValue();
    }

    @Override
    public double getMax() {
        return this.property.getMaximum().floatValue();
    }

    @Override
    public void setValue(double value) {
        this.property.setValue(Float.valueOf(new Double(value).floatValue()));
    }

    @Override
    public String getName() {
        return this.property.getName().replace("-", " ");
    }

    @Override
    public String getValueString() {
        return ChatColors.formatColor(this.property.formatValue());
    }

    @Override
    public double getIncrement() {
        return 0.1;
    }

    @Override
    public boolean isVisible() {
        return this.property.isVisible();
    }
}
