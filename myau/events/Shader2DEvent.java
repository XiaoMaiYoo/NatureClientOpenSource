package myau.events;

import myau.event.events.Event;

public class Shader2DEvent
implements Event {
    private final ShaderType shaderType;

    public Shader2DEvent(ShaderType shaderType) {
        this.shaderType = shaderType;
    }

    public ShaderType getShaderType() {
        return this.shaderType;
    }

    public static enum ShaderType {
        BLUR,
        SHADOW,
        GLOW;

    }
}
