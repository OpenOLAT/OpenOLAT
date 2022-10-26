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
package org.olat.core.commons.services.vfs;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;

import java.util.List;

/**
 * Initial date: 2022-09-30<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public interface VFSTranscodingService {

	OLATResourceable ores = OresHelper.lookupType(VFSTranscodingService.class);

	String masterFilePrefix = "._oo_master_";
	boolean isLocalTranscodingEnabled();
	List<VFSMetadata> getMetadatasInNeedForTranscoding();
	List<VFSMetadata> getMetadatasWithUnresolvedTranscodingStatus();

	/**
	 * Returns the VFS item of the destination of a transcoding job. This represents an mp4 representation
	 * of the master file of a recording, for instance.
	 *
	 * @param vfsMetadata The VFS metadata of the recording.
	 *
	 * @return A VFS item of a file that is compatible with all browsers.
	 */
	VFSItem getDestinationItem(VFSMetadata vfsMetadata);

	String getDirectoryString(VFSItem vfsItem);

	void setStatus(VFSMetadata vfsMetadata, int status);

	void itemSavedWithTranscoding(VFSLeaf leaf, Identity savedBy);

	void startTranscodingProcess();

	void fileDoneEvent(VFSMetadata vfsMetadata);

	void deleteMasterFile(VFSItem item);

	String getHandbrakeCliExecutable();
}
