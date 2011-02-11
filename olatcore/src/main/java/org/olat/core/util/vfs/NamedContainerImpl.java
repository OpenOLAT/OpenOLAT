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

import java.util.List;

import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.filters.VFSItemFilter;

/**
 * Description:<br>
 * TODO: Felix Jost Class Description for VirtualContainerImpl
 * 
 * <P>
 * Initial Date:  23.06.2005 <br>
 *
 * @author Felix Jost
 */
public class NamedContainerImpl extends AbstractVirtualContainer {

	final VFSContainer delegate;

	/**
	 * @param name
	 * @param delegate
	 */
	public NamedContainerImpl (String name, VFSContainer delegate) {
		super(name);
		this.delegate = delegate;
	}
	
	public VFSContainer getDelegate() {
		return delegate;
	}
	
	/**
	 * @see org.olat.core.util.vfs.VFSItem#getParent()
	 */
	public VFSContainer getParentContainer() {
		return delegate.getParentContainer();
	}
	
	/**
	 * @see org.olat.core.util.vfs.VFSItem#setParentContainer(org.olat.core.util.vfs.VFSContainer)
	 */
	public void setParentContainer(VFSContainer parentContainer) {
		delegate.setParentContainer(parentContainer);
	}
	
	/**
	 * @see org.olat.core.util.vfs.VFSContainer#getItems()
	 */
	public List getItems() {
		//FIXME:fj:b add as listener to "change ownergroup" event, so that the access may be denied, if ownergroup of repoitem has changed.
		return delegate.getItems();
	}
	
	/**
	 * @see org.olat.core.util.vfs.VFSContainer#getItems(org.olat.core.util.vfs.filters.VFSItemFilter)
	 */
	public List getItems(VFSItemFilter filter) {
		return delegate.getItems(filter);
	}
	
	/**
	 * @see org.olat.core.util.vfs.VFSContainer#copyFrom(org.olat.core.util.vfs.VFSItem)
	 */
	public VFSStatus copyFrom(VFSItem source) {
		return delegate.copyFrom(source);
	}


	/**
	 * @see org.olat.core.util.vfs.VFSContainer#canWrite()
	 */
	public VFSStatus canWrite() {
		return delegate.canWrite();
	}

	/**
	 * @see org.olat.core.util.vfs.VFSContainer#canCopy()
	 */
	public VFSStatus canCopy() {
		return delegate.canCopy();
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
		return delegate.delete();
	}

	/**
	 * @see org.olat.core.util.vfs.VFSItem#getLastModified()
	 */
	public long getLastModified() {
		return delegate.getLastModified();
	}

	/**
	 * @see org.olat.core.util.vfs.VFSItem#resolveFile(java.lang.String) 
	 * <br />
	 * Be aware that this method can return tricky values:
	 * <ul>
	 *   <li>If the path is '/', the named container itself is returned</li>
	 *   <li>for child elements, the item of the delegate object is returned</li>
	 * </ul>
	 * In the second case, the returned item does not know anymore that it
	 * was embedded in a named container. Thus, the isSame() method on the
	 * root element of the resolved item is not the same as this object.
	 */
	public VFSItem resolve(String path) {
		path = VFSManager.sanitizePath(path);
		if (path.equals("/")) return this;
		return delegate.resolve(path);
	}
	
	/**
	 * @see org.olat.core.util.vfs.VFSContainer#createChildContainer(java.lang.String)
	 */
	public VFSContainer createChildContainer(String name) {
		return delegate.createChildContainer(name);
	}
	
	/**
	 * @see org.olat.core.util.vfs.VFSContainer#createChildLeaf(java.lang.String)
	 */
	public VFSLeaf createChildLeaf(String name) {
		return delegate.createChildLeaf(name);
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "NamedContainer "+getName()+ "-> "+delegate.toString();
	}

	/**
	 * @see org.olat.core.util.vfs.VFSItem#getLocalSecurityCallback()
	 */
	public VFSSecurityCallback getLocalSecurityCallback() {
		return delegate.getLocalSecurityCallback();
	}

	/**
	 * @see org.olat.core.util.vfs.VFSItem#setLocalSecurityCallback(org.olat.core.util.vfs.callbacks.VFSSecurityCallback)
	 */
	public void setLocalSecurityCallback(VFSSecurityCallback secCallback) {
		delegate.setLocalSecurityCallback(secCallback);
	}

	/**
	 * @see org.olat.core.util.vfs.VFSItem#isSame(org.olat.core.util.vfs.VFSItem)
	 */
	public boolean isSame(VFSItem vfsItem) {
		return delegate.isSame(vfsItem);
	}

	/**
	 * @see org.olat.core.util.vfs.VFSContainer#setDefaultItemFilter(org.olat.core.util.vfs.filters.VFSItemFilter)
	 */
	public void setDefaultItemFilter(VFSItemFilter defaultFilter) {
		delegate.setDefaultItemFilter(defaultFilter);
	}

	/**
	 * @see org.olat.core.util.vfs.VFSContainer#getDefaultItemFilter()
	 */
	public VFSItemFilter getDefaultItemFilter() {
		return delegate.getDefaultItemFilter();
	}

}

