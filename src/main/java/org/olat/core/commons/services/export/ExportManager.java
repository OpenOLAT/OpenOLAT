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
package org.olat.core.commons.services.export;

import java.util.List;

import org.olat.core.commons.services.export.model.ExportInfos;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;

/**
 * Only support course for the moment<br>
 * The exported ZIP file need to have a file name which ends with
 * "_taskKey.zip" @see AbstractExportTask.java
 * 
 * Initial date: 2 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface ExportManager {
	
	public static final String ROOT_FOLDER = "persistedexports";
	
	public VFSContainer getExportContainer(RepositoryEntry entry, String resSubPath);
	
	public List<ExportInfos> getResultsExport(RepositoryEntry entry, String resSubPath);
	
	public void startExport(ExportTask task, Identity creator, OLATResource resource, String resSubPath);
	
	public void cancelExport(ExportInfos export, RepositoryEntry entry, String resSubPath);
	
	public void deleteExport(ExportInfos export);

}
