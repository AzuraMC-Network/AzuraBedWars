package cc.azuramc.bedwars.bootstrap;

import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author an5w1r@163.com
 */
class IsolatedClassLoaderTest {

    private final ClassLoader parent = IsolatedClassLoaderTest.class.getClassLoader();

    @Test
    void ownClassesWinOverParent() throws Exception {
        URL junitJar = Test.class.getProtectionDomain().getCodeSource().getLocation();
        try (IsolatedClassLoader loader = new IsolatedClassLoader(new URL[]{junitJar}, parent)) {
            Class<?> isolated = loader.loadClass(Test.class.getName());

            assertSame(loader, isolated.getClassLoader());
            assertNotSame(Test.class, isolated);
        }
    }

    @Test
    void missingClassesComeFromParent() throws Exception {
        URL junitJar = Test.class.getProtectionDomain().getCodeSource().getLocation();
        try (IsolatedClassLoader loader = new IsolatedClassLoader(new URL[]{junitJar}, parent)) {
            assertSame(PluginEntrypoint.class, loader.loadClass(PluginEntrypoint.class.getName()));
            assertSame(String.class, loader.loadClass("java.lang.String"));
        }
    }
}
