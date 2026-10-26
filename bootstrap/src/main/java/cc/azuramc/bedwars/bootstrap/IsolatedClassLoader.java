package cc.azuramc.bedwars.bootstrap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Child first, classes from its own URLs win over the parent
 *
 * @author an5w1r@163.com
 */
final class IsolatedClassLoader extends URLClassLoader {

    /** Routes bundled library logs to the server logging backend when it has one */
    private static final String PARENT_FIRST_PREFIX = "org.slf4j.";

    static {
        ClassLoader.registerAsParallelCapable();
    }

    IsolatedClassLoader(@NotNull URL @NotNull [] urls, @NotNull ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> loaded = findLoadedClass(name);
            if (loaded == null && name.startsWith(PARENT_FIRST_PREFIX)) {
                loaded = findParentClass(name);
            }
            if (loaded == null) {
                loaded = findOwnClass(name);
            }
            if (loaded == null) {
                loaded = getParent().loadClass(name);
            }
            if (resolve) {
                resolveClass(loaded);
            }
            return loaded;
        }
    }

    @Override
    @Nullable
    public URL getResource(String name) {
        URL own = findResource(name);
        if (own != null) {
            return own;
        }
        return getParent().getResource(name);
    }

    @Nullable
    private Class<?> findParentClass(@NotNull String name) {
        try {
            return getParent().loadClass(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @Nullable
    private Class<?> findOwnClass(@NotNull String name) {
        try {
            return findClass(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
