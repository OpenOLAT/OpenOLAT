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
package org.olat.core.util.vfs;

import java.io.InputStream;
import java.io.OutputStream;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;

/**
 * <h3>Description:</h3>
 * The named leaf takes an existing VFSLeaf and wraps it with another name. This
 * is handy to display items using another name than the real filesystem name.
 * Most methods are delegated to the original VFSLeaf
 * <p>
 * Initial Date: 30.05.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class NamedLeaf implements VFSLeaf {
	protected final String name;
	protected final VFSLeaf delegate;
	
	/**
	 * Constructor
	 * @param name Name under which the leaf should appear
	 * @param delegate The delegate leaf
	 */
	public NamedLeaf(String name, VFSLeaf delegate) {
		this.name = name;
		this.delegate = delegate;
	}
	
	@Override
	public boolean exists() {
		return delegate != null && delegate.exists();
	}

	//fxdiff FXOLAT-125: virtual file system for CP
	public VFSLeaf getDelegate() {
		return delegate;
	}

	@Override
	public InputStream getInputStream() {
		return delegate.getInputStream();
	}

	@Override
	public OutputStream getOutputStream(boolean append) {
		return delegate.getOutputStream(append);
	}

	@Override
	public long getSize() {
		return delegate.getSize();
	}

	@Override
	public VFSStatus canCopy() {
		return delegate.canCopy();
	}

	@Override
	public VFSStatus canDelete() {
		return delegate.canDelete();
	}


	@Override
	public VFSStatus canRename() {
		// renaming is not supported
		return VFSConstants.NO;
	}

	@Override
	public VFSStatus canWrite() {
		return delegate.canWrite();
	}

	@Override
	public VFSStatus delete() {
		return delegate.delete();
	}

	@Override
	public VFSStatus deleteSilently() {
		return delegate.deleteSilently();
	}

	@Override
	public long getLastModified() {
		return delegate.getLastModified();
	}

	@Override
	public VFSSecurityCallback getLocalSecurityCallback() {
		return delegate.getLocalSecurityCallback();
	}

	@Override
	public String getName() {
		// use the name of the wrapper
		return name;
	}

	@Override
	public String getRelPath() {
		return delegate.getRelPath();
	}

	@Override
	public VFSContainer getParentContainer() {
		return delegate.getParentContainer();
	}

	@Override
	public boolean isSame(VFSItem vfsItem) {
		// test on delegate item and not on wrapper 
		return delegate.isSame(vfsItem);
	}

	@Override
	public VFSStatus rename(String newname) {
		throw new RuntimeException("unsupported");
	}

	@Override
	public VFSItem resolve(String path) {
		return delegate.resolve(delegate.getName());
	}

	@Override
	public VFSStatus canMeta() {
		return delegate.canMeta();
	}

	@Override
	public VFSMetadata getMetaInfo() {
		return delegate.getMetaInfo();
	}

	@Override
	public void setLocalSecurityCallback(VFSSecurityCallback secCallback) {
		delegate.setLocalSecurityCallback(secCallback);
	}

	@Override
	public void setParentContainer(VFSContainer parentContainer) {
		delegate.setParentContainer(parentContainer);
	}

}
