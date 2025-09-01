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
package org.olat.core.commons.services.export.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.export.ExportManager;
import org.olat.core.commons.services.export.ExportMetadata;
import org.olat.core.commons.services.export.ExportMetadataToCurriculum;
import org.olat.core.commons.services.export.ExportMetadataToCurriculumElement;
import org.olat.core.commons.services.export.ExportMetadataToOrganisation;
import org.olat.core.commons.services.export.ExportTask;
import org.olat.core.commons.services.export.model.ExportInfos;
import org.olat.core.commons.services.export.model.SearchExportMetadataParameters;
import org.olat.core.commons.services.taskexecutor.Task;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.commons.services.taskexecutor.TaskStatus;
import org.olat.core.commons.services.taskexecutor.model.PersistentTask;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 15 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ExportManagerImpl implements ExportManager {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ExportMetadataDAO exportMetadataDao;
	@Autowired
	private TaskExecutorManager taskExecutorManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private ExportMetadataToCurriculumDAO exportMetadataToCurriculumDao;
	@Autowired
	private ExportMetadataToOrganisationDAO exportMetadataToOrganisationDao;
	@Autowired
	private ExportMetadataToCurriculumElementDAO exportMetadataToCurriculumElementDao;
	
	@Override
	public VFSContainer getExportContainer(RepositoryEntry entry, String resSubPath) {
		if("CourseModule".equals(entry.getOlatResource().getResourceableTypeName())) {
			ICourse course = CourseFactory.loadCourse(entry);
			CourseEnvironment courseEnv = course.getCourseEnvironment();
			VFSContainer rootFolder = VFSManager.getOrCreateContainer(courseEnv.getCourseBaseContainer(), ROOT_FOLDER);
			return VFSManager.getOrCreateContainer(rootFolder, resSubPath);
		}
		return null;
	}
	
	@Override
	public ExportMetadata getExportMetadataByTask(PersistentTask task) {
		return exportMetadataDao.getMetadataByTask(task);
	}
	
	@Override
	public ExportMetadata getExportMetadataByKey(Long metadataKey) {
		return exportMetadataDao.getMetadataByKey(metadataKey);
	}

	@Override
	public ExportMetadata updateMetadata(ExportMetadata metadata) {
		return exportMetadataDao.updateMetadata(metadata);
	}

	@Override
	public ExportMetadata addMetadataOrganisations(ExportMetadata metadata, List<Organisation> organisations) {
		Set<ExportMetadataToOrganisation> currentRelations = metadata.getOrganisations();
		List<Organisation> currentOrganisations = currentRelations.stream()
				.map(ExportMetadataToOrganisation::getOrganisation)
				.toList();
		
		boolean needMerge = false;
		for(Organisation organisation:organisations) {
			if(!currentOrganisations.contains(organisation)) {
				ExportMetadataToOrganisation rel = exportMetadataToOrganisationDao.createMetadataToOrganisation(metadata, organisation);
				currentRelations.add(rel);
				needMerge = true;
			}
		}
		
		return needMerge ? updateMetadata(metadata) : metadata;
	}

	@Override
	public ExportMetadata addMetadataCurriculums(ExportMetadata metadata, List<Curriculum> curriculums) {
		Set<ExportMetadataToCurriculum> currentRelations = metadata.getCurriculums();
		List<Curriculum> currentCurriculums = currentRelations.stream()
				.map(ExportMetadataToCurriculum::getCurriculum)
				.toList();
		
		boolean needMerge = false;
		for(Curriculum curriculum:curriculums) {
			if(!currentCurriculums.contains(curriculum)) {
				ExportMetadataToCurriculum rel = exportMetadataToCurriculumDao.createMetadataToCurriculum(metadata, curriculum);
				currentRelations.add(rel);
				needMerge = true;
			}
		}
		
		return needMerge ? updateMetadata(metadata) : metadata;
	}

	@Override
	public ExportMetadata addMetadataCurriculumElements(ExportMetadata metadata, List<CurriculumElement> curriculumElements) {
		Set<ExportMetadataToCurriculumElement> currentRelations = metadata.getCurriculumElements();
		List<CurriculumElement> currentCurriculums = currentRelations.stream()
				.map(ExportMetadataToCurriculumElement::getCurriculumElement)
				.toList();
		
		boolean needMerge = false;
		for(CurriculumElement curriculumElement:curriculumElements) {
			if(!currentCurriculums.contains(curriculumElement)) {
				ExportMetadataToCurriculumElement rel = exportMetadataToCurriculumElementDao.createMetadataToCurriculumElement(metadata, curriculumElement);
				currentRelations.add(rel);
				needMerge = true;
			}
		}
		
		return needMerge ? updateMetadata(metadata) : metadata;
	}

	@Override
	public ExportMetadata startExport(ExportTask task, String title, String description, 
			String filename, ArchiveType type, Date expirationDate, boolean onlyAdministrators,
			RepositoryEntry entry, String resSubPath, Identity creator) {
		final OLATResource resource = entry == null ? null : entry.getOlatResource();
		return taskExecutorManager.execute(task, creator, resource, resSubPath, null,
				(id, persistentTask) -> exportMetadataDao.createMetadata(title, description, filename, type,
						expirationDate, onlyAdministrators, entry, resource, resSubPath, id, persistentTask));
	}
	
	@Override
	public ExportMetadata startExport(ExportTask task, String title, String description, 
			String filename, ArchiveType type, Date expirationDate, boolean onlyAdministrators,
			OLATResource resource, String resSubPath, Identity creator) {
		return taskExecutorManager.execute(task, creator, resource, resSubPath, null,
				(id, persistentTask) -> exportMetadataDao.createMetadata(title, description, filename, type,
						expirationDate, onlyAdministrators, null, resource, resSubPath, id, persistentTask));
	}

	@Override
	public ExportMetadata startExport(ExportTask task, String title, String description, String filename, ArchiveType type, Date expirationDate, boolean onlyAdministrators, String resSubPath, Identity creator) {
		return taskExecutorManager.execute(task, creator, null, resSubPath, null,
				(id, persistentTask) -> exportMetadataDao.createMetadata(title, description, filename, type,
						expirationDate, onlyAdministrators, null, null, resSubPath, id, persistentTask));
	}

	@Override
	public void deleteExports(RepositoryEntryRef re) {
		SearchExportMetadataParameters params = new SearchExportMetadataParameters(re, null, List.of(ArchiveType.values()));
		List<ExportMetadata> exportMetadataList = this.searchMetadata(params);
		for(ExportMetadata exportMetadata:exportMetadataList) {
			deleteExport(exportMetadata);
		}
	}

	@Override
	public void deleteExport(ExportInfos export) {
		VFSLeaf exportFile = export.getZipLeaf();
		if(exportFile != null) {
			exportFile.deleteSilently();
		}
	}

	@Override
	public void deleteExportMetadata(ExportMetadata metadata) {
		Task task = metadata.getTask();
		if(task != null && (task.getStatus() == TaskStatus.newTask || task.getStatus() == TaskStatus.inWork)) {
			Task cancelledTask = taskExecutorManager.cancel(task);
			if(cancelledTask == null) {
				deleteExport(metadata);
			}
		} else {
			deleteExport(metadata);
		}
	}
	
	@Override
	public void deleteExpiredExports() {
		List<ExportMetadata> expiredMetadataList = exportMetadataDao.expiredExports(new Date());
		for(ExportMetadata expiredMetadata:expiredMetadataList) {
			deleteExport(expiredMetadata);
			dbInstance.commit();
		}
		dbInstance.commitAndCloseSession();
	}

	protected void deleteExport(ExportMetadata metadata) {
		String filePath = metadata.getFilePath();
		String filename = metadata.getFilename();
		// Delete export first, foreign key to VFS metadata
		exportMetadataToCurriculumDao.deleteRelations(metadata);
		exportMetadataToCurriculumElementDao.deleteRelations(metadata);
		exportMetadataToOrganisationDao.deleteRelations(metadata);
		dbInstance.commit();
		exportMetadataDao.deleteMetadata(metadata);
		dbInstance.commit();
		
		if(StringHelper.containsNonWhitespace(filePath) && StringHelper.containsNonWhitespace(filename)) {
			VFSLeaf leaf = VFSManager.olatRootLeaf(filePath);
			if(leaf != null) {
				VFSMetadata vfsMetadata = leaf.getMetaInfo();
				if(vfsMetadata == null || !exportMetadataDao.metadataInUse(vfsMetadata)) {
					leaf.deleteSilently();
				}
			}
		}
	}

	@Override
	public void cancelExport(ExportInfos export, RepositoryEntry entry, String resSubPath) {
		Task task = export.getTask();
		if(task != null) {
			Task cancelledTask = taskExecutorManager.cancel(task);
			if(cancelledTask == null) {
				if(export.getZipLeaf() != null) {
					deleteExport(export);
				} else {
					// try to clean the mess eventually
					String taskEnding = "_" + task.getKey() + ".zip";
					VFSContainer exportContainer = getExportContainer(entry, resSubPath);
					List<VFSItem> zipItems = exportContainer.getItems(new ZIPLeafFilter());
					for(VFSItem zipItem:zipItems) {
						if(zipItem.getName().endsWith(taskEnding)) {
							zipItem.deleteSilently();
						}
					}
				}
			}
		} else {
			deleteExport(export);
		}
	}
	
	@Override
	public List<ExportMetadata> searchMetadata(SearchExportMetadataParameters params) {
		return exportMetadataDao.searchMetadatas(params);
	}

	@Override
	public List<ExportInfos> getResultsExport(SearchExportMetadataParameters params) {
		List<ExportInfos> exports = new ArrayList<>();
		List<ExportMetadata> exportMetadataList = exportMetadataDao.searchMetadatas(params);
		for(ExportMetadata data:exportMetadataList) {
			exports.add(new ExportInfos(data));
		}
		return exports;
	}

	@Override
	public List<ExportInfos> getResultsExport(RepositoryEntry courseEntry, String resSubPath, SearchExportMetadataParameters params) {
		List<ExportInfos> exports = new ArrayList<>();
		
		Set<String> archiveNames = new HashSet<>();
		params.setRepositoryEntries(List.of(courseEntry));
		params.setResSubPath(resSubPath);
		
		List<ExportMetadata> exportMetadataList = exportMetadataDao.searchMetadatas(params);
		for(ExportMetadata data:exportMetadataList) {
			exports.add(new ExportInfos(data));
			archiveNames.add(data.getFilename());
		}
		
		if(params.getArchiveTypes().contains(ArchiveType.QTI21)) {
			VFSContainer exportContainer = getExportContainer(courseEntry, resSubPath);
			if(exportContainer != null) {
				List<VFSItem> zipItems = exportContainer.getItems(new ZIPLeafFilter());
				for(VFSItem zipItem:zipItems) {
					if(!archiveNames.contains(zipItem.getName()) && zipItem instanceof VFSLeaf zipLeaf) {
						VFSMetadata metadata = vfsRepositoryService.getMetadataFor(zipLeaf);
						ExportInfos resultsExport = new ExportInfos(zipLeaf, metadata);
						exports.add(resultsExport);
					}
				}
			}
		}
		
		return exports;
	}
	
	private static class ZIPLeafFilter implements VFSItemFilter {
		@Override
		public boolean accept(VFSItem vfsItem) {
			String name = vfsItem.getName();
			return !name.startsWith(".") && !name.equals("__MACOSX") && name.endsWith(".zip")
					&& vfsItem instanceof VFSLeaf;
		}
	}
}
