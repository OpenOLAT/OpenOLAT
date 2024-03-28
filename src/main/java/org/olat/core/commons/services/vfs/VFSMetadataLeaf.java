/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.vfs;

import java.io.InputStream;
import java.io.OutputStream;

import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;

/**
 * 
 * Initial date: 27 Mar 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class VFSMetadataLeaf extends VFSMetadataItem implements VFSLeaf {

	protected VFSMetadataLeaf(VFSRepositoryService vfsRepositoryService, VFSMetadata vfsMetadata,
			VFSContainer parentContainer, VFSSecurityCallback secCallback) {
		super(vfsRepositoryService, vfsMetadata, parentContainer, secCallback);
	}

	@Override
	public InputStream getInputStream() {
		if (getItem() instanceof VFSLeaf vfsLeaf) {
			return vfsLeaf.getInputStream();
		}
		return null;
	}

	@Override
	public long getSize() {
		if (getMetaInfo() == null) {
			return 0l;
		}
		return getMetaInfo().getFileSize();
	}

	@Override
	public OutputStream getOutputStream(boolean append) {
		if (getItem() instanceof VFSLeaf vfsLeaf) {
			return vfsLeaf.getOutputStream(append);
		}
		return null;
	}

}
