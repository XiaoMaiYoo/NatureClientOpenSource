package myau.ui.dataset.impl;

import myau.enums.ChatColors;
import myau.property.properties.PercentProperty;
import myau.ui.dataset.Slider;

public class PercentageSlider
extends Slider {
    private final PercentProperty property;

    public PercentageSlider(PercentProperty property) {
        this.property = property;
    }

    @Override
    public double getInput() {
        return ((Integer)this.property.getValue()).intValue();
    }

    @Override
    public double getMin() {
        return this.property.getMinimum().intValue();
    }

    @Override
    public double getMax() {
        return this.property.getMaximum().intValue();
    }

    @Override
    public void setValue(double value) {
        this.property.setValue(new Double(value).intValue());
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
        return 1.0;
    }

    @Override
    public boolean isVisible() {
        return this.property.isVisible();
    }
}
