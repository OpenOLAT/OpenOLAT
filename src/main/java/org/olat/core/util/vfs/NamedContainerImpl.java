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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
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

	private VFSContainer delegate;

	/**
	 * @param name
	 * @param delegate
	 */
	public NamedContainerImpl (String name, VFSContainer delegate) {
		super(name);
		this.delegate = delegate;
	}

	@Override
	public boolean exists() {
		VFSContainer d = getDelegate();
		return d != null && d.exists();
	}

	public VFSContainer getDelegate() {
		return delegate;
	}
	
	protected void setDelegate(VFSContainer delegate) {
		this.delegate = delegate;
	}
	
	/**
	 * @see org.olat.core.util.vfs.VFSItem#getParent()
	 */
	public VFSContainer getParentContainer() {
		return getDelegate().getParentContainer();
	}
	
	/**
	 * @see org.olat.core.util.vfs.VFSItem#setParentContainer(org.olat.core.util.vfs.VFSContainer)
	 */
	public void setParentContainer(VFSContainer parentContainer) {
		getDelegate().setParentContainer(parentContainer);
	}
	
	/**
	 * @see org.olat.core.util.vfs.VFSContainer#getItems()
	 */
	public List<VFSItem> getItems() {
		//FIXME:fj:b add as listener to "change ownergroup" event, so that the access may be denied, if ownergroup of repoitem has changed.
		return getDelegate().getItems();
	}
	
	/**
	 * @see org.olat.core.util.vfs.VFSContainer#getItems(org.olat.core.util.vfs.filters.VFSItemFilter)
	 */
	public List<VFSItem> getItems(VFSItemFilter filter) {
		return getDelegate().getItems(filter);
	}
	
	/**
	 * @see org.olat.core.util.vfs.VFSContainer#copyFrom(org.olat.core.util.vfs.VFSItem)
	 */
	public VFSStatus copyFrom(VFSItem source) {
		return getDelegate().copyFrom(source);
	}


	/**
	 * @see org.olat.core.util.vfs.VFSContainer#canWrite()
	 */
	public VFSStatus canWrite() {
		return getDelegate().canWrite();
	}

	/**
	 * @see org.olat.core.util.vfs.VFSContainer#canCopy()
	 */
	public VFSStatus canCopy() {
		return getDelegate().canCopy();
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
		return getDelegate().delete();
	}

	/**
	 * @see org.olat.core.util.vfs.VFSItem#getLastModified()
	 */
	public long getLastModified() {
		return getDelegate().getLastModified();
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
		return getDelegate().resolve(path);
	}
	
	/**
	 * @see org.olat.core.util.vfs.VFSContainer#createChildContainer(java.lang.String)
	 */
	public VFSContainer createChildContainer(String name) {
		return getDelegate().createChildContainer(name);
	}
	
	/**
	 * @see org.olat.core.util.vfs.VFSContainer#createChildLeaf(java.lang.String)
	 */
	public VFSLeaf createChildLeaf(String name) {
		return getDelegate().createChildLeaf(name);
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "NamedContainer "+getName()+ "-> "+getDelegate().toString();
	}

	/**
	 * @see org.olat.core.util.vfs.VFSItem#getLocalSecurityCallback()
	 */
	public VFSSecurityCallback getLocalSecurityCallback() {
		return getDelegate().getLocalSecurityCallback();
	}

	/**
	 * @see org.olat.core.util.vfs.VFSItem#setLocalSecurityCallback(org.olat.core.util.vfs.callbacks.VFSSecurityCallback)
	 */
	public void setLocalSecurityCallback(VFSSecurityCallback secCallback) {
		getDelegate().setLocalSecurityCallback(secCallback);
	}

	/**
	 * @see org.olat.core.util.vfs.VFSItem#isSame(org.olat.core.util.vfs.VFSItem)
	 */
	public boolean isSame(VFSItem vfsItem) {
		return getDelegate().isSame(vfsItem);
	}

	/**
	 * @see org.olat.core.util.vfs.VFSContainer#setDefaultItemFilter(org.olat.core.util.vfs.filters.VFSItemFilter)
	 */
	public void setDefaultItemFilter(VFSItemFilter defaultFilter) {
		getDelegate().setDefaultItemFilter(defaultFilter);
	}

	/**
	 * @see org.olat.core.util.vfs.VFSContainer#getDefaultItemFilter()
	 */
	public VFSItemFilter getDefaultItemFilter() {
		return getDelegate().getDefaultItemFilter();
	}

}

