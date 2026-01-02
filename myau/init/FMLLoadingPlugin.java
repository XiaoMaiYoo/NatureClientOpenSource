package myau.init;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class FMLLoadingPlugin
implements IMixinConfigPlugin {
    private static final List<FMLLoadingPlugin> mixinPlugins = new ArrayList<FMLLoadingPlugin>();
    private String mixinPackage;
    private List<String> mixins = null;

    public static List<FMLLoadingPlugin> getMixinPlugins() {
        return mixinPlugins;
    }

    @Override
    public void onLoad(String mixinPackage) {
        this.mixinPackage = mixinPackage;
        mixinPlugins.add(this);
    }

    public URL getBaseUrlForClassUrl(URL classUrl) {
        String string = classUrl.toString();
        if (classUrl.getProtocol().equals("jar")) {
            try {
                return new URL(string.substring(4).split("!")[0]);
            }
            catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        if (string.endsWith(".class")) {
            try {
                return new URL(string.replace("\\", "/").replace(this.getClass().getCanonicalName().replace(".", "/") + ".class", ""));
            }
            catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return classUrl;
    }

    public String getMixinPackage() {
        return this.mixinPackage;
    }

    public String getMixinBaseDir() {
        return this.mixinPackage.replace(".", "/");
    }

    public void tryAddMixinClass(String className) {
        String norm = (className.endsWith(".class") ? className.substring(0, className.length() - ".class".length()) : className).replace("\\", "/").replace("/", ".");
        if (norm.startsWith(this.getMixinPackage() + ".") && !norm.endsWith(".")) {
            this.mixins.add(norm.substring(this.getMixinPackage().length() + 1));
        }
    }

    @Override
    public List<String> getMixins() {
        Path file;
        if (this.mixins != null) {
            return this.mixins;
        }
        this.mixins = new ArrayList<String>();
        URL classUrl = this.getClass().getProtectionDomain().getCodeSource().getLocation();
        System.out.println("Found classes at " + classUrl);
        try {
            file = Paths.get(this.getBaseUrlForClassUrl(classUrl).toURI());
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Base directory found at " + file);
        if (Files.isDirectory(file, new LinkOption[0])) {
            this.walkDir(file);
        } else {
            this.walkJar(file);
        }
        System.out.println("Found mixins: " + this.mixins);
        return this.mixins;
    }

    private void walkDir(Path classRoot) {
        System.out.println("Trying to find mixins from directory");
        try (Stream<Path> classes = Files.walk(classRoot.resolve(this.getMixinBaseDir()), new FileVisitOption[0]);){
            classes.map(it -> classRoot.relativize((Path)it).toString()).forEach(this::tryAddMixinClass);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void walkJar(Path file) {
        System.out.println("Trying to find mixins from jar file");
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(file, new OpenOption[0]));){
            ZipEntry next;
            while ((next = zis.getNextEntry()) != null) {
                this.tryAddMixinClass(next.getName());
                zis.closeEntry();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }
}
