package hudson.util;

import hudson.FilePath;
import hudson.Util;

import java.io.Serializable;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Used to build up an argument in the classpath format.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.300
 */
public class ClasspathBuilder implements Serializable {
    private final List<String> args = new ArrayList<String>();

    /**
     * Adds a single directory or a jar file.
     */
    public ClasspathBuilder add(File f) {
        return add(f.getAbsolutePath());
    }

    /**
     * Adds a single directory or a jar file.
     */
    public ClasspathBuilder add(FilePath f) {
        return add(f.getRemote());
    }

    /**
     * Adds a single directory or a jar file.
     */
    public ClasspathBuilder add(String path) {
        args.add(path);
        return this;
    }

    /**
     * Adds all the files that matches the given glob in the directory.
     *
     * @see FilePath#list(String)  
     */
    public ClasspathBuilder addAll(FilePath base, String glob) throws IOException, InterruptedException {
        for(FilePath item : base.list(glob))
            add(item);
        return this;
    }

    /**
     * Returns the string representation of the classpath.
     */
    @Override
    public String toString() {
        return Util.join(args,File.pathSeparator);
    }
}
