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

import java.io.File;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryModule;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;

/**
 * <P>
 * Initial Date:  23.06.2005 <br>
 *
 * @author Felix Jost
 */
public abstract class LocalImpl implements VFSItem, JavaIOItem {

	private File basefile;
	private VFSContainer parentContainer;
	private VFSSecurityCallback securityCallback;

	/**
	 * @param basefile
	 */
	protected LocalImpl(File basefile, VFSContainer parent) {
		this.basefile = basefile;
		this.parentContainer = parent;
	}

	@Override
	public VFSContainer getParentContainer() {
		return parentContainer;
	}

	@Override
	public void setParentContainer(VFSContainer parentContainer) {
		this.parentContainer = parentContainer;
	}

	@Override
	public VFSStatus canDelete() {
		VFSContainer inheritingContainer = VFSManager.findInheritingSecurityCallbackContainer(this);
		if (inheritingContainer != null && !inheritingContainer.getLocalSecurityCallback().canDelete())
			return VFSConstants.NO_SECURITY_DENIED;
		return (basefile.canWrite() ? VFSConstants.YES : VFSConstants.NO);
	}

	@Override
	public VFSStatus canCopy() {
		VFSContainer inheritingContainer = VFSManager.findInheritingSecurityCallbackContainer(this);
		if (inheritingContainer != null && !inheritingContainer.getLocalSecurityCallback().canCopy())
			return VFSConstants.NO_SECURITY_DENIED;
		return VFSConstants.YES;
	}

	@Override
	public VFSStatus canRename() {
		VFSContainer inheritingContainer = VFSManager.findInheritingSecurityCallbackContainer(this);
		if (inheritingContainer != null && !inheritingContainer.getLocalSecurityCallback().canWrite())
			return VFSConstants.NO_SECURITY_DENIED;
		return VFSConstants.YES;
	}

	@Override
	public VFSStatus canWrite() {
		return VFSConstants.NO;
	}

	@Override
	public boolean exists() {
		return basefile != null && basefile.exists();
	}

	@Override
	public boolean isHidden() {
		return basefile != null && basefile.isHidden();
	}

	@Override
	public String getName() {
		return basefile.getName();
	}
	
	/**
	 * Be aware that the returned base file reference might change, do not hold a
	 * local reference to it in your code! Due to a bug in Java after renaming a
	 * LocalImpl file the base file will be a new object with a new reference!
	 * 
	 * @return the current base file
	 */
	@Override
	public File getBasefile() {
		return basefile;
	}
	
	/**
	 * Used only to overcome the java rename bug
	 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4094022
	 * @param newBasefile
	 */
	protected void setBasefile(File newBasefile) {
		basefile = newBasefile;
	}
	
	/**
	 * @return The last modified of the file
	 */
	@Override
	public long getLastModified() {
		long lm = basefile.lastModified();
		// file returns zero -> we return -1 (see interface docu)
		return lm == 0L? VFSConstants.UNDEFINED : lm;
	}
	
	@Override
	public VFSStatus canMeta() {
		return VFSRepositoryModule.canMeta(getBasefile());
	}

	@Override
	public VFSMetadata getMetaInfo() {
		if(canMeta() == VFSConstants.YES) {
			return CoreSpringFactory.getImpl(VFSRepositoryService.class).getMetadataFor(getBasefile());
		}
		return null;
	}

	@Override
	public void setLocalSecurityCallback(VFSSecurityCallback securityCallback) {
		this.securityCallback = securityCallback;
	}

	@Override
	public VFSSecurityCallback getLocalSecurityCallback() {
		return securityCallback;
	}

	@Override
	public boolean isSame(VFSItem vfsItem) {
		if (!(vfsItem instanceof LocalImpl)) return false;
		return getBasefile().equals(((LocalImpl)vfsItem).getBasefile());
	}
	
}

