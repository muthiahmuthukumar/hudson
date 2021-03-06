/*
 * The MIT License
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.console;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Hudson;
import hudson.model.Run;
import hudson.util.TimeUnit2;
import org.jvnet.tiger_types.Types;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;

/**
 * Entry point to the {@link ConsoleAnnotator} extension point. This class creates a new instance
 * of {@link ConsoleAnnotator} that starts a new console annotation session.
 *
 * <p>
 * {@link ConsoleAnnotatorFactory}s are used whenever a browser requests console output (as opposed to when
 * the console output is being produced &mdash; for that see {@link ConsoleNote}.)
 *
 * <p>
 * {@link ConsoleAnnotator}s returned by {@link ConsoleAnnotatorFactory} are asked to start from
 * an arbitrary line of the output, because typically browsers do not request the entire console output.
 * Because of this, {@link ConsoleAnnotatorFactory} is generally suitable for peep-hole local annotation
 * that only requires a small contextual information, such as keyword coloring, URL hyperlinking, and so on.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.349
 */
public abstract class ConsoleAnnotatorFactory<T> implements ExtensionPoint {
    /**
     * Called when a console output page is requested to create a stateful {@link ConsoleAnnotator}.
     *
     * <p>
     * This method can be invoked concurrently by multiple threads.
     *
     * @param context
     *      The model object that owns the console output, such as {@link Run}.
     *      This method is only called when the context object if assignable to
     *      {@linkplain #type() the advertised type}.
     * @return
     *      null if this factory is not going to participate in the annotation of this console.
     */
    public abstract ConsoleAnnotator newInstance(T context);

    /**
     * For which context type does this annotator work?
     */
    public Class type() {
        Type type = Types.getBaseClass(getClass(), ConsoleAnnotator.class);
        if (type instanceof ParameterizedType)
            return Types.erasure(Types.getTypeArgument(type,0));
        else
            return Object.class;
    }

    /**
     * Returns true if this descriptor has a JavaScript to be inserted on applicable console page.
     */
    public boolean hasScript() {
        return getScriptJs() !=null;
    }

    private URL getScriptJs() {
        Class c = getClass();
        return c.getClassLoader().getResource(c.getName().replace('.','/').replace('$','/')+"/script.js");
    }

    /**
     * Serves the JavaScript file associated with this console annotator factory.
     */
    @WebMethod(name="script.js")
    public void doScriptJs(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        rsp.serveFile(req,getScriptJs(), TimeUnit2.DAYS.toMillis(1));
    }

    /**
     * All the registered instances.
     */
    public static ExtensionList<ConsoleAnnotatorFactory> all() {
        return Hudson.getInstance().getExtensionList(ConsoleAnnotatorFactory.class);
    }
}
