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

import java.util.Date;
import java.util.List;

import org.olat.core.commons.services.export.model.ExportInfos;
import org.olat.core.commons.services.export.model.SearchExportMetadataParameters;
import org.olat.core.commons.services.taskexecutor.model.PersistentTask;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
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
	
	VFSContainer getExportContainer(RepositoryEntry entry, String resSubPath);
	
	List<ExportInfos> getResultsExport(RepositoryEntry entry, String resSubPath, SearchExportMetadataParameters params);
	
	List<ExportInfos> getResultsExport(SearchExportMetadataParameters searchParams);
	
	ExportMetadata getExportMetadataByTask(PersistentTask task);
	
	ExportMetadata getExportMetadataByKey(Long metadataKey);
	
	ExportMetadata updateMetadata(ExportMetadata metadata);
	
	/**
	 * Link some organisations
	 * 
	 * @param metadata The metadata to enrich
	 * @param organisations A list of organisations to link to the metadata
	 * @return The merged metadata
	 */
	ExportMetadata addMetadataOrganisations(ExportMetadata metadata, List<Organisation> organisations);
	
	/**
	 * Link some curriculums
	 * 
	 * @param metadata The metadata to enrich
	 * @param curriculums A list of curriculums to link to the metadata
	 * @return The merged metadata
	 */
	ExportMetadata addMetadataCurriculums(ExportMetadata metadata, List<Curriculum> curriculums);
	
	/**
	 * Link some curriculums elements
	 * 
	 * @param metadata The metadata to enrich
	 * @param curriculums A list of curriculums elements to link to the metadata
	 * @return The merged metadata
	 */
	ExportMetadata addMetadataCurriculumElements(ExportMetadata metadata, List<CurriculumElement> curriculumElements);
	
	ExportMetadata startExport(ExportTask task, String title, String description,
			String filename, ArchiveType type, Date expirationDate, boolean onlyAdministrators,
			RepositoryEntry entry, String resSubPath, Identity creator);
	
	ExportMetadata startExport(ExportTask task, String title, String description,
			String filename, ArchiveType type, Date expirationDate, boolean onlyAdministrators,
			OLATResource resource, String resSubPath, Identity creator);
	
	ExportMetadata startExport(ExportTask task, String title, String description,
			String filename, ArchiveType type, Date expirationDate, boolean onlyAdministrators,
			String resSubPath, Identity creator);
	
	void cancelExport(ExportInfos export, RepositoryEntry entry, String resSubPath);
	
	void deleteExport(ExportInfos export);
	
	void deleteExportMetadata(ExportMetadata metadata);
	
	void deleteExpiredExports();
	
	List<ExportMetadata> searchMetadata(SearchExportMetadataParameters searchParams);

}
