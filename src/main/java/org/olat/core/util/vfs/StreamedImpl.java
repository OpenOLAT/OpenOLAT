/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.util.vfs;

import java.io.InputStream;
import java.io.OutputStream;

import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;

/**
 * Description:<br>
 * TODO: Felix Jost Class Description for StreamedImpl
 * 
 * <P>
 * Initial Date:  24.06.2005 <br>
 *
 * @author Felix Jost
 */
public class StreamedImpl implements VFSLeaf {

	private final String name;
	private final InputStream inStream;
	private VFSContainer parentContainer;
	private VFSSecurityCallback securityCallback;

	/**
	 * @param name
	 * @param inStream
	 */
	public StreamedImpl(String name, VFSContainer parentContainer, InputStream inStream) {
		this.name = name;
		this.parentContainer = parentContainer;
		this.inStream = inStream;
	}
	
	public VFSContainer getParentContainer() {
		return parentContainer;
	}
	
	public void setParentContainer(VFSContainer parentContainer) {
		this.parentContainer = parentContainer;
	}
	/**
	 * @see org.olat.core.util.vfs.VFSLeaf#getInputStream()
	 */
	public InputStream getInputStream() {
		return inStream;
	}

	/**
	 * @see org.olat.core.util.vfs.VFSItem#getLastModified()
	 */
	public long getLastModified() {
		// not known
		return VFSConstants.UNDEFINED;
	}

	/**
	 * @see org.olat.core.util.vfs.VFSLeaf#getSize()
	 */
	public long getSize() {
		// not known
		return VFSConstants.UNDEFINED;
	}

	/**
	 * @see org.olat.core.util.vfs.VFSLeaf#getOutputStream(boolean)
	 */
	public OutputStream getOutputStream(boolean append) {
		throw new RuntimeException("cannot write to an inputstream - vfsleaf");
	}

	/**
	 * @see org.olat.core.util.vfs.VFSItem#canRename()
	 */
	public VFSStatus canRename() {
		return VFSConstants.NO;
	}

	/**
	 * @see org.olat.core.util.vfs.VFSItem#canDelete()
	 */
	public VFSStatus canDelete() {
		return VFSConstants.NO;
	}

	/**
	 * @see org.olat.core.util.vfs.VFSItem#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see org.olat.core.util.vfs.VFSItem#rename(java.lang.String)
	 */
	public VFSStatus rename(String newname) {
		throw new RuntimeException("unsupported");
	}


	/**
	 * @see org.olat.core.util.vfs.VFSItem#delete()
	 */
	public VFSStatus delete() {
		throw new RuntimeException("unsupported");
	}

	/**
	 * @see org.olat.core.util.vfs.VFSItem#canCopy()
	 */
	public VFSStatus canCopy() {
		return VFSConstants.YES;
	}

	/**
	 * @see org.olat.core.util.vfs.VFSItem#canCopyTo()
	 */
	public VFSStatus canWrite() {
		return VFSConstants.NO;
	}

	/**
	 * @see org.olat.core.util.vfs.VFSItem#resolveFile(java.lang.String)
	 */
	public VFSItem resolve(String path) {
		// not found
		return null;
	}

	/**
	 * @see org.olat.core.util.vfs.VFSItem#getLocalSecurityCallback()
	 */
	public VFSSecurityCallback getLocalSecurityCallback() {
		return securityCallback;
	}

	/**
	 * @param secCallback
	 */
	public void setLocalSecurityCallback(VFSSecurityCallback secCallback) {
		this.securityCallback = secCallback;
	}

	/**
	 * @see org.olat.core.util.vfs.VFSItem#isSame(org.olat.core.util.vfs.VFSItem)
	 */
	public boolean isSame(VFSItem vfsItem) {
		if (!(vfsItem instanceof StreamedImpl)) return false;
		return inStream.equals(((StreamedImpl)vfsItem).inStream);
	}
}

