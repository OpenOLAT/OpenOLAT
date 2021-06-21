/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.JavaIOItem;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;

/**
 * An implementation of the VFSLEaf for a pure java.io.File with
 * an absolute path.
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class VFSJavaIOFile implements VFSLeaf, JavaIOItem {
	
	private static final Logger log = Tracing.createLoggerFor(VFSJavaIOFile.class);

	private final String name;
	private final File file;
	
	public VFSJavaIOFile(URI uri) {
		this(new File(uri));
	}
	
	public VFSJavaIOFile(File file) {
		this(file.getName(), file);
	}
	
	public VFSJavaIOFile(String name, File file) {
		this.name = name;
		this.file = file;
	}

	@Override
	public boolean exists() {
		return file != null && file.exists();
	}

	@Override
	public boolean isHidden() {
		return file != null && file.isHidden();
	}

	@Override
	public File getBasefile() {
		return file;
	}

	@Override
	public VFSItem resolve(String path) {
		return null;
	}

	@Override
	public VFSContainer getParentContainer() {
		return null;
	}

	@Override
	public void setParentContainer(VFSContainer parentContainer) {
		//
	}
	
	@Override
	public String getRelPath() {
		Path bFile = getBasefile().toPath();
		Path bcRoot = FolderConfig.getCanonicalRootPath();
		if(bFile.startsWith(bcRoot)) {
			String relPath = bcRoot.relativize(bFile).toString();
			return "/" + relPath;
		}
		return null;
	}

	@Override
	public VFSStatus rename(String newname) {
		return VFSConstants.NO;
	}

	@Override
	public VFSStatus delete() {
		return VFSConstants.NO;
	}

	@Override
	public VFSStatus deleteSilently() {
		return VFSConstants.NO;
	}

	@Override
	public VFSStatus canRename() {
		return VFSConstants.NO;
	}

	@Override
	public VFSStatus canDelete() {
		return VFSConstants.NO;
	}

	@Override
	public String getName() {
		return name == null ? file.getName() : name;
	}

	@Override
	public long getLastModified() {
		return file.lastModified();
	}

	@Override
	public VFSStatus canCopy() {
		return VFSConstants.NO;
	}

	@Override
	public VFSStatus canWrite() {
		return VFSConstants.NO;
	}

	@Override
	public VFSSecurityCallback getLocalSecurityCallback() {
		return null;
	}

	@Override
	public void setLocalSecurityCallback(VFSSecurityCallback secCallback) {
		//
	}

	@Override
	public VFSStatus canMeta() {
		return VFSConstants.NO;
	}

	@Override
	public VFSStatus canVersion() {
		return VFSConstants.NO;
	}

	@Override
	public VFSMetadata getMetaInfo() {
		return null;
	}

	@Override
	public boolean isSame(VFSItem vfsItem) {
		return false;
	}

	@Override
	public InputStream getInputStream() {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	@Override
	public long getSize() {
		return file.length();
	}

	@Override
	public OutputStream getOutputStream(boolean append) {
		try {
			return new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			log.error("", e);
			return null;
		}
	}
}