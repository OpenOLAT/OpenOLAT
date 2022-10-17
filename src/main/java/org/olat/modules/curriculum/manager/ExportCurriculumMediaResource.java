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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.io.HttpServletResponseOutputStream;
import org.olat.core.util.io.ShieldOutputStream;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementImpl;
import org.olat.modules.curriculum.model.CurriculumElementToRepositoryEntryRef;
import org.olat.modules.curriculum.model.CurriculumElementToRepositoryEntryRefs;
import org.olat.modules.curriculum.model.CurriculumImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExportCurriculumMediaResource implements MediaResource {
	private static final Logger log = Tracing.createLoggerFor(ExportCurriculumMediaResource.class);
	
	private final Curriculum curriculum;
	
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryHandlerFactory handlerFactory;
	
	public ExportCurriculumMediaResource(Curriculum curriculum) {
		CoreSpringFactory.autowireObject(this);
		this.curriculum = curriculum;
	}
	
	@Override
	public long getCacheControlDuration() {
		return 0;
	}
	
	@Override
	public boolean acceptRanges() {
		return false;
	}
	
	@Override
	public String getContentType() {
		return "application/zip";
	}

	@Override
	public Long getSize() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public Long getLastModified() {
		return null;
	}

	@Override
	public void release() {
		//
	}
	
	@Override
	public void prepare(HttpServletResponse hres) {
		try {
			hres.setCharacterEncoding("UTF-8");
		} catch (Exception e) {
			log.error("", e);
		}
		
		try(ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
			Curriculum loadedCurriculum = curriculumService.getCurriculum(curriculum);
			unproxy(loadedCurriculum);
			String label = loadedCurriculum.getDisplayName();
			String secureLabel = StringHelper.transformDisplayNameToFileSystemName(label);

			String file = secureLabel + ".zip";
			hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + StringHelper.urlEncodeUTF8(file));			
			hres.setHeader("Content-Description", StringHelper.urlEncodeUTF8(label));

			zout.setLevel(9);
			
			// curriculum structure
			zout.putNextEntry(new ZipEntry("curriculum.xml"));
			CurriculumXStream.toStream(loadedCurriculum, zout);
			zout.closeEntry();
			
			// curriculum element to repository entry
			List<ExportRepositoryEntry> collectedEntries = new ArrayList<>();
			for(CurriculumElement element:((CurriculumImpl)loadedCurriculum).getRootElements()) {
				collectEntries(element, collectedEntries);
			}
			CurriculumElementToRepositoryEntryRefs entryRefs = new CurriculumElementToRepositoryEntryRefs(new ArrayList<>());
			for(ExportRepositoryEntry collectedEntry:collectedEntries) {
				entryRefs.getEntryRefs().add(new CurriculumElementToRepositoryEntryRef(collectedEntry.getEntry(),
						collectedEntry.getCurriculumElement().getKey()));
			}
			zout.putNextEntry(new ZipEntry("curriculum_entries.xml"));
			CurriculumXStream.toStream(entryRefs, zout);
			zout.closeEntry();
			
			// export repository entries
			Set<Long> duplicates = new HashSet<>();
			for(ExportRepositoryEntry exportedEntry:collectedEntries) {
				if(!duplicates.contains(exportedEntry.getEntry().getKey())) {
					exportEntries(exportedEntry, zout);
					duplicates.add(exportedEntry.getEntry().getKey());
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private void exportEntries(ExportRepositoryEntry exportedEntry, ZipOutputStream zout)
	throws IOException {
		RepositoryEntry entry = exportedEntry.getEntry();
		OLATResourceable ores = entry.getOlatResource();
		RepositoryHandler handler = handlerFactory.getRepositoryHandler(entry);
		
		MediaResource mr = handler.getAsMediaResource(ores);
		zout.putNextEntry(new ZipEntry("repo_" + entry.getKey() + ".zip"));

		try(OutputStream out=new ShieldOutputStream(zout);
				InputStream in = mr.getInputStream()) {
			if(in == null) {
				HttpServletResponseOutputStream response = new HttpServletResponseOutputStream(out);
				mr.prepare(response);
			} else {
				FileUtils.copy(in, out);
			}
		} catch(Exception e) {
			log.error("", e);
		}
		zout.closeEntry();
	}
	
	private void collectEntries(CurriculumElement curriculumElement, List<ExportRepositoryEntry> collectedEntries) {
		if(curriculumElement == null) return;
		
		List<RepositoryEntry> entries = curriculumService.getRepositoryEntries(curriculumElement);
		for(RepositoryEntry entry:entries) {
			collectedEntries.add(new ExportRepositoryEntry(entry, curriculumElement));
		}

		for(CurriculumElement element:((CurriculumElementImpl)curriculumElement).getChildren()) {
			collectEntries(element, collectedEntries);
		}
	}
	
	private void unproxy(Curriculum loadedCurriculum) {
		loadedCurriculum.getOrganisation();
		for(CurriculumElement element:((CurriculumImpl)loadedCurriculum).getRootElements()) {
			unproxy(element);
		}
	}
	
	private void unproxy(CurriculumElement curriculumElement) {
		if(curriculumElement == null) return;
		
		curriculumElement.getCurriculum();
		curriculumElement.setType(Hibernate.unproxy(curriculumElement.getType(), CurriculumElementType.class));
		for(CurriculumElement element:((CurriculumElementImpl)curriculumElement).getChildren()) {
			unproxy(element);
		}
	}
	
	private static class ExportRepositoryEntry {
		
		private final RepositoryEntry entry;
		private final CurriculumElement curriculumElement;
		
		public ExportRepositoryEntry(RepositoryEntry entry, CurriculumElement curriculumElement) {
			this.entry = entry;
			this.curriculumElement = curriculumElement;
		}
		
		public RepositoryEntry getEntry() {
			return entry;
		}
		
		public CurriculumElement getCurriculumElement() {
			return curriculumElement;
		}
	}
}
