package myau.util.shader;

import java.awt.Color;
import myau.util.shader.Shader;
import org.lwjgl.opengl.GL20;

public class GlowShader
extends Shader {
    private static final String shader = String.join((CharSequence)"\n", "#version 120", "uniform sampler2D texture;", "uniform vec4 color;", "void main() {", "vec4 st = texture2D(texture, gl_TexCoord[0].st);", "gl_FragColor = vec4(color.rgb, st.a > 0.0 ? color.a : 0.0);", "}");

    public GlowShader() {
        super(shader);
    }

    @Override
    public void onLink() {
        this.setUniform("texture");
        this.setUniform("color");
    }

    @Override
    public void onUse() {
        GL20.glUseProgram((int)this.programId);
        int texLoc = this.getUniformLocationCached("texture");
        GL20.glUniform1i((int)texLoc, (int)0);
        GL20.glUniform4f((int)texLoc, (float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public void W(Color color) {
        GL20.glUniform4f((int)this.getUniformLocationCached("color"), (float)((float)color.getRed() / 255.0f), (float)((float)color.getGreen() / 255.0f), (float)((float)color.getBlue() / 255.0f), (float)((float)color.getAlpha() / 255.0f));
    }
}
