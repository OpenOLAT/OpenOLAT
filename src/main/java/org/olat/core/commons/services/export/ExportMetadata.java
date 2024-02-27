/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.export;

import java.util.Date;

import org.olat.core.commons.services.taskexecutor.model.PersistentTask;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 21 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface ExportMetadata extends ModifiedInfo, CreateInfo {
	
	Long getKey();
	
	ArchiveType getArchiveType();

	void setArchiveType(ArchiveType archiveType);

	String getTitle();

	void setTitle(String title);
	
	String getDescription();

	void setDescription(String description);

	String getFilename();

	void setFilename(String filename);

	String getFilePath();

	void setFilePath(String filePath);

	boolean isOnlyAdministrators();

	void setOnlyAdministrators(boolean onlyAdministrators);

	Date getExpirationDate();

	void setExpirationDate(Date expirationDate);

	RepositoryEntry getEntry();
	
	String getSubIdent();

	PersistentTask getTask();
	
	Identity getCreator();
	
	VFSMetadata getMetadata();

	void setMetadata(VFSMetadata metadata);

}
