package myau.util.shader;

import java.util.HashMap;
import java.util.Map;
import org.lwjgl.opengl.GL20;

public abstract class Shader {
    private static final String vertex = "#version 120\nvoid main(void) {\ngl_TexCoord[0] = gl_MultiTexCoord0;\ngl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n}";
    private final Map<String, Integer> uniformLocations = new HashMap<String, Integer>();
    protected int programId;

    private int compileShader(String source, int type) {
        int shader = GL20.glCreateShader((int)type);
        GL20.glShaderSource((int)shader, (CharSequence)source);
        GL20.glCompileShader((int)shader);
        int compile = GL20.glGetShaderi((int)shader, (int)35713);
        return compile == 0 ? -1 : shader;
    }

    private void createProgram(String fragment) {
        this.programId = GL20.glCreateProgram();
        GL20.glAttachShader((int)this.programId, (int)this.compileShader(vertex, 35633));
        GL20.glAttachShader((int)this.programId, (int)this.compileShader(fragment, 35632));
        GL20.glLinkProgram((int)this.programId);
        int programId = GL20.glGetProgrami((int)this.programId, (int)35714);
        if (programId == 0) {
            this.programId = -1;
        } else {
            this.onLink();
        }
    }

    public Shader(String string) {
        this.createProgram(string);
    }

    public int getUniformLocationCached(String name) {
        return this.uniformLocations.get(name);
    }

    public void setUniform(String name) {
        this.uniformLocations.put(name, GL20.glGetUniformLocation((int)this.programId, (CharSequence)name));
    }

    public abstract void onLink();

    public abstract void onUse();

    public void use() {
        this.onUse();
    }

    public void stop() {
        GL20.glUseProgram((int)0);
    }
}
