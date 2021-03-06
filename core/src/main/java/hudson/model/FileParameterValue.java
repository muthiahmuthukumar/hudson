/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi
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
package hudson.model;

import org.kohsuke.stapler.DataBoundConstructor;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.io.IOUtils;

import hudson.tasks.BuildWrapper;
import hudson.Launcher;
import hudson.FilePath;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * {@link ParameterValue} for {@link FileParameterDefinition}.
 *
 * <h2>Persistence</h2>
 * <p>
 * {@link DiskFileItem} is persistable via serialization,
 * (although the data may get very large in XML) so this object
 * as a whole is persistable.
 *
 * @author Kohsuke Kawaguchi
 */
public class FileParameterValue extends ParameterValue {
    private FileItem file;

    private String location;

    @DataBoundConstructor
    public FileParameterValue(String name, FileItem file) {
        this(name, file, null);
    }
    public FileParameterValue(String name, FileItem file, String description) {
        super(name, description);
        assert file!=null;
        this.file = file;
    }

    public FileParameterValue(String name, File file, String desc) {
        this(name,new FileItemImpl(file),desc);
    }

    // post initialization hook
    /*package*/ void setLocation(String location) {
        this.location = location;
    }

    @Override
    public BuildWrapper createBuildWrapper(AbstractBuild<?,?> build) {
        return new BuildWrapper() {
            public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
            	if (!StringUtils.isEmpty(file.getName())) {
            		listener.getLogger().println("Copying file to "+location);
            		build.getWorkspace().child(location).copyFrom(file);
            		file = null;
            	}
                return new Environment() {};
            }
        };
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
		return result;
	}

	/**
	 * In practice this will always be false, since location should be unique.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileParameterValue other = (FileParameterValue) obj;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		return true;
	}
	
    @Override
    public String getShortDescription() {
    	return "(FileParameterValue) " + getName() + "='" + file.getName() + "'";
    }

    /**
     * Default implementation from {@link File}.
     */
    public static final class FileItemImpl implements FileItem {
        private final File file;

        public FileItemImpl(File file) {
            this.file = file;
        }

        public InputStream getInputStream() throws IOException {
            return new FileInputStream(file);
        }

        public String getContentType() {
            return null;
        }

        public String getName() {
            return file.getName();
        }

        public boolean isInMemory() {
            return false;
        }

        public long getSize() {
            return file.length();
        }

        public byte[] get() {
            try {
                return IOUtils.toByteArray(new FileInputStream(file));
            } catch (IOException e) {
                throw new Error(e);
            }
        }

        public String getString(String encoding) throws UnsupportedEncodingException {
            return new String(get(), encoding);
        }

        public String getString() {
            return new String(get());
        }

        public void write(File to) throws Exception {
            new FilePath(file).copyTo(new FilePath(to));
        }

        public void delete() {
            file.delete();
        }

        public String getFieldName() {
            return null;
        }

        public void setFieldName(String name) {
        }

        public boolean isFormField() {
            return false;
        }

        public void setFormField(boolean state) {
        }

        public OutputStream getOutputStream() throws IOException {
            return new FileOutputStream(file);
        }
    }
}
