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

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.filters.VFSItemFilter;

/**
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
	
	@Override
	public boolean isHidden() {
		VFSContainer d = getDelegate();
		return d != null && d.isHidden();
	}

	public VFSContainer getDelegate() {
		return delegate;
	}
	
	protected void setDelegate(VFSContainer delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public VFSContainer getParentContainer() {
		return getDelegate().getParentContainer();
	}

	@Override
	public void setParentContainer(VFSContainer parentContainer) {
		getDelegate().setParentContainer(parentContainer);
	}

	@Override
	public List<VFSItem> getItems() {
		return getDelegate().getItems();
	}

	@Override
	public List<VFSItem> getItems(VFSItemFilter filter) {
		return getDelegate().getItems(filter);
	}

	@Override
	public VFSStatus copyFrom(VFSItem source, Identity savedBy) {
		return getDelegate().copyFrom(source, savedBy);
	}

	@Override
	public VFSStatus copyContentOf(VFSContainer container, Identity savedBy) {
		return getDelegate().copyContentOf(container, savedBy);
	}

	@Override
	public VFSStatus canWrite() {
		return getDelegate().canWrite();
	}

	@Override
	public VFSStatus canCopy() {
		return getDelegate().canCopy();
	}

	@Override
	public VFSStatus rename(String newname) {
		return VFSConstants.NO;
	}

	@Override
	public String getRelPath() {
		return getDelegate().getRelPath();
	}

	@Override
	public boolean isInPath(String path) {
		return getDelegate().isInPath(path);
	}

	@Override
	public VFSStatus delete() {
		return getDelegate().delete();
	}

	@Override
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
	@Override
	public VFSItem resolve(String path) {
		path = VFSManager.sanitizePath(path);
		if (path.equals("/")) return this;
		return getDelegate().resolve(path);
	}

	@Override
	public VFSContainer createChildContainer(String name) {
		return getDelegate().createChildContainer(name);
	}

	@Override
	public VFSLeaf createChildLeaf(String name) {
		return getDelegate().createChildLeaf(name);
	}

	@Override
	public VFSStatus canMeta() {
		return getDelegate().canMeta();
	}

	@Override
	public VFSStatus canVersion() {
		return getDelegate().canVersion();
	}

	@Override
	public VFSMetadata getMetaInfo() {
		return getDelegate().getMetaInfo();
	}

	@Override
	public String toString() {
		return "NamedContainer " + getName() + "-> " + getDelegate().toString();
	}

	@Override
	public VFSSecurityCallback getLocalSecurityCallback() {
		return getDelegate().getLocalSecurityCallback();
	}

	@Override
	public void setLocalSecurityCallback(VFSSecurityCallback secCallback) {
		getDelegate().setLocalSecurityCallback(secCallback);
	}

	@Override
	public boolean isSame(VFSItem vfsItem) {
		return getDelegate().isSame(vfsItem);
	}

	@Override
	public void setDefaultItemFilter(VFSItemFilter defaultFilter) {
		getDelegate().setDefaultItemFilter(defaultFilter);
	}

	@Override
	public VFSItemFilter getDefaultItemFilter() {
		return getDelegate().getDefaultItemFilter();
	}

}

