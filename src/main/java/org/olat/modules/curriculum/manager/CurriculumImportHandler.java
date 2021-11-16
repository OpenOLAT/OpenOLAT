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
package org.olat.modules.curriculum.manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.core.util.PathUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementImpl;
import org.olat.modules.curriculum.model.CurriculumElementToRepositoryEntryRef;
import org.olat.modules.curriculum.model.CurriculumElementToRepositoryEntryRefs;
import org.olat.modules.curriculum.model.CurriculumImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 19 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CurriculumImportHandler {
	
	private static final Logger log = Tracing.createLoggerFor(CurriculumImportHandler.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	
	public String getCurriculumName(File archive) {
		try (FileSystem fileSystem=FileSystems.newFileSystem(archive.toPath(), (ClassLoader)null)) {
			Path curriculumXml = fileSystem.getPath("/curriculum.xml");
			Curriculum curriculum = CurriculumXStream.curriculumFromPath(curriculumXml);
			if(curriculum != null) {
				return curriculum.getDisplayName();
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return null;
	}

	public boolean importCurriculum(File archive, String curriculumName, Organisation organisation, Identity author, Locale locale) {
		try (FileSystem fileSystem=FileSystems.newFileSystem(archive.toPath(), (ClassLoader)null)) {
			Path curriculumXml = fileSystem.getPath("/curriculum.xml");
			Curriculum curriculum = CurriculumXStream.curriculumFromPath(curriculumXml);
			if(curriculum == null) {
				return false;
			}
			if(StringHelper.containsNonWhitespace(curriculumName)) {
				curriculum.setDisplayName(curriculumName);
			}
			Map<Long,CurriculumElement> archiveKeyToCurriculumElements = new HashMap<>();
			importCurriculumStructure(curriculum, organisation, archiveKeyToCurriculumElements);
			
			Path curriculumEntriesXml = fileSystem.getPath("/curriculum_entries.xml");
			CurriculumElementToRepositoryEntryRefs entryRefs = CurriculumXStream.entryRefsFromPath(curriculumEntriesXml);
			if(entryRefs != null) {
				importEntries(entryRefs, archiveKeyToCurriculumElements, organisation, fileSystem, author, locale);
			}
			return true;
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}
	
	private void importEntries(CurriculumElementToRepositoryEntryRefs entryRefs,
			Map<Long,CurriculumElement> archiveKeyToCurriculumElements, Organisation organisation,
			FileSystem fileSystem, Identity author, Locale locale) throws IOException {
		List<CurriculumElementToRepositoryEntryRef> entriesRefs = entryRefs.getEntryRefs();
		Map<Long,RepositoryEntry> archivedRepositoryEntryKeys = new HashMap<>();
		
		for(CurriculumElementToRepositoryEntryRef entryRef:entriesRefs) {
			CurriculumElement element = archiveKeyToCurriculumElements.get(entryRef.getCurriculumElementKey());
			if(element == null) {
				continue;
			}
			
			RepositoryEntry entry;
			if(archivedRepositoryEntryKeys.containsKey(entryRef.getRepositoryEntryKey())) {
				entry = archivedRepositoryEntryKeys.get(entryRef.getRepositoryEntryKey());
			} else {
				entry = importRepositoryEntry(entryRef, organisation, fileSystem, author, locale);
			}
			if(entry != null) {
				curriculumService.addRepositoryEntry(element, entry, false);
			}
			archivedRepositoryEntryKeys.put(entryRef.getRepositoryEntryKey(), entry);
		}
	}
	
	private RepositoryEntry importRepositoryEntry(CurriculumElementToRepositoryEntryRef archivedRef, Organisation organisation,
			FileSystem fileSystem, Identity author, Locale locale) throws IOException {
		String zipName = "repo_" + archivedRef.getRepositoryEntryKey() + ".zip";
		Path curriculumXml = fileSystem.getPath("/" + zipName);
		
		RepositoryEntry importedEntry = null;
		if(Files.exists(curriculumXml)) {
			File tmpArchive = new File(WebappHelper.getTmpDir(), UUID.randomUUID() + zipName);
			tmpArchive.mkdirs();
			PathUtils.copyFileToDir(curriculumXml, tmpArchive.getParentFile(), tmpArchive.getName());

			for(String type:repositoryHandlerFactory.getSupportedTypes()) {
				RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(type);
				ResourceEvaluation eval = handler.acceptImport(tmpArchive, tmpArchive.getName());
				if(eval != null && eval.isValid()) {
					importedEntry = handler.importResource(author, archivedRef.getRepositoryEntryInitialAuthor(),
							archivedRef.getRepositoryEntryDisplayname(), archivedRef.getRepositoryEntryDescription(),
							true, organisation, locale, tmpArchive, zipName);
					dbInstance.commit();
					if("CourseModule".equals(importedEntry.getOlatResource().getResourceableTypeName())) {
						ICourse course = CourseFactory.loadCourse(importedEntry);
						CourseFactory.publishCourse(course, RepositoryEntryStatusEnum.preparation, false, false, author, locale);
					}
				}
			}
			
			if(!Files.deleteIfExists(tmpArchive.toPath())) {
				log.warn("Cannot delete {}", tmpArchive.getAbsolutePath());
			}
			dbInstance.commitAndCloseSession();
		}
		return importedEntry;
	}

	private void importCurriculumStructure(Curriculum archiveCurriculum, Organisation organisation, Map<Long,CurriculumElement> archiveKeyToCurriculumElements) {
		Curriculum curriculum = curriculumService
				.createCurriculum(archiveCurriculum.getIdentifier(), archiveCurriculum.getDisplayName(), archiveCurriculum.getDescription(), organisation);
		curriculum.setDegree(archiveCurriculum.getDegree());
		curriculum.setStatus(archiveCurriculum.getStatus());
		curriculum = curriculumService.updateCurriculum(curriculum);
		
		List<CurriculumElementType> elementTypes = curriculumService.getCurriculumElementTypes();
		for(CurriculumElement rootElement:((CurriculumImpl)archiveCurriculum).getRootElements()) {
			importCurriculumElements(curriculum, rootElement, null, elementTypes, archiveKeyToCurriculumElements);
		}
	}

	private void importCurriculumElements(Curriculum curriculum, CurriculumElement archivedElement,
			CurriculumElement parentElement, List<CurriculumElementType> elementTypes, Map<Long,CurriculumElement> archiveKeyToCurriculumElements) {
		if(archivedElement == null) return;
		
		CurriculumElementType elementType = findType(archivedElement.getType(), elementTypes);
		
		CurriculumElement element = curriculumService.createCurriculumElement(archivedElement.getIdentifier(),
				archivedElement.getDisplayName(), archivedElement.getElementStatus(), archivedElement.getBeginDate(),
				archivedElement.getEndDate(), parentElement, elementType, archivedElement.getCalendars(),
				archivedElement.getLectures(), archivedElement.getLearningProgress(), curriculum);
		element.setElementStatus(archivedElement.getElementStatus());
		
		archiveKeyToCurriculumElements.put(archivedElement.getKey(), element);
		
		for(CurriculumElement childElement:((CurriculumElementImpl)archivedElement).getChildren()) {
			importCurriculumElements(curriculum, childElement, element, elementTypes, archiveKeyToCurriculumElements);
		}
	}
	
	private CurriculumElementType findType(CurriculumElementType archivedType, List<CurriculumElementType> elementTypes) {
		if(archivedType == null) return null;
		
		for(CurriculumElementType elementType:elementTypes) {
			if(elementType.getIdentifier() != null && elementType.getIdentifier().equalsIgnoreCase(archivedType.getIdentifier())) {
				return elementType;
			}
		}
		
		for(CurriculumElementType elementType:elementTypes) {
			if(elementType.getDisplayName() != null && elementType.getDisplayName().equalsIgnoreCase(archivedType.getDisplayName())) {
				return elementType;
			}
		}
		
		CurriculumElementType newElementType = curriculumService.createCurriculumElementType(archivedType.getIdentifier(),
				archivedType.getDisplayName(), archivedType.getDescription(), archivedType.getExternalId());
		elementTypes.add(newElementType);
		dbInstance.commit();
		return newElementType;
	}
}
