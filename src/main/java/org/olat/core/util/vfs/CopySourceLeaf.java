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
 * Initial date: 23 April 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class CopySourceLeaf implements VFSLeaf {

	private final VFSLeaf delegate;
	private final VFSMetadata metadata;
	
	public CopySourceLeaf(VFSLeaf delegate, VFSMetadata metadata) {
		this.delegate = delegate;
		this.metadata = metadata;
	}
	
	@Override
	public boolean exists() {
		return delegate != null && delegate.exists();
	}

	@Override
	public boolean isHidden() {
		return delegate != null && delegate.isHidden();
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
		return VFSStatus.YES;
	}

	@Override
	public VFSStatus canDelete() {
		return VFSStatus.NO;
	}


	@Override
	public VFSStatus canRename() {
		return VFSStatus.NO;
	}

	@Override
	public VFSStatus canWrite() {
		return VFSStatus.NO;
	}

	@Override
	public VFSSuccess delete() {
		return VFSSuccess.ERROR_FAILED;
	}

	@Override
	public VFSSuccess restore(VFSContainer targetContainer) {
		return VFSSuccess.ERROR_FAILED;
	}

	@Override
	public VFSSuccess deleteSilently() {
		return VFSSuccess.ERROR_FAILED;
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
		return delegate.getName();
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
		return delegate.isSame(vfsItem);
	}

	@Override
	public VFSSuccess rename(String newname) {
		return VFSSuccess.ERROR_FAILED;
	}

	@Override
	public VFSItem resolve(String path) {
		return delegate.resolve(delegate.getName());
	}
	
	@Override
	public VFSStatus canVersion() {
		return VFSStatus.NO;
	}

	@Override
	public VFSStatus canMeta() {
		return VFSStatus.YES;
	}

	@Override
	public VFSMetadata getMetaInfo() {
		return metadata;
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
