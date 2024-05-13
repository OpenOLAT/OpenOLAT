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
package org.olat.core.commons.services.vfs.ui.version;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;

/**
 * 
 * Initial date: 8 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TrashRow {
	
	private final VFSMetadata metadata;
	private final String relativePath;
	
	public TrashRow(VFSMetadata metadata) {
		this.metadata = metadata;
		this.relativePath = metadata.getRelativePath().replace("/" + VFSRepositoryService.TRASH_NAME, "");
	}

	public VFSMetadata getMetadata() {
		return metadata;
	}

	public Long getKey() {
		return metadata.getKey();
	}
	
	public long getSize() {
		return metadata.getFileSize();
	}

	public String getRelativePath() {
		return relativePath;
	}

	public String getFilename() {
		return metadata.getFilename();
	}

}
