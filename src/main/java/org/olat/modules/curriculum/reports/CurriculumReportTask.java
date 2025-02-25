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
package org.olat.modules.curriculum.reports;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityRef;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.export.AbstractExportTask;
import org.olat.core.commons.services.export.ExportManager;
import org.olat.core.commons.services.export.ExportMetadata;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.commons.services.taskexecutor.TaskStatus;
import org.olat.core.commons.services.taskexecutor.model.PersistentTask;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumReportConfiguration;
import org.olat.modules.curriculum.CurriculumReportConfiguration.ReportContent;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.model.CurriculumRefImpl;

/**
 * 
 * Initial date: 4 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumReportTask extends AbstractExportTask {
	
	private static final long serialVersionUID = -4740082752176076855L;

	private transient VFSLeaf exportZip;

	private String title;
	private Long doerKey;
	private Locale locale;
	private Long curriculumKey;
	private Long curriculumElementKey;
	private CurriculumReportConfiguration implementation;
	
	public CurriculumReportTask(String title, Curriculum curriculum, CurriculumElement curriculumElement,
			IdentityRef doer, Locale locale, CurriculumReportConfiguration implementation) {
		this.title = title;
		this.locale = locale;
		this.implementation = implementation;
		doerKey = doer == null ? null : doer.getKey();
		curriculumKey = curriculum == null ? null : curriculum.getKey();
		curriculumElementKey = curriculumElement == null ? null : curriculumElement.getKey();
	}

	@Override
	public boolean isDelayed() {
		return false;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public VFSLeaf getExportZip() {
		return exportZip;
	}

	@Override
	public void run() {
		final BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		final ExportManager exportManager = CoreSpringFactory.getImpl(ExportManager.class);
		final CurriculumService curriculumService = CoreSpringFactory.getImpl(CurriculumService.class);
		final TaskExecutorManager taskExecutorManager = CoreSpringFactory.getImpl(TaskExecutorManager.class);
		final VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
		
		Identity doer = doerKey == null ? null : securityManager.loadIdentityByKey(doerKey);
		Curriculum curriculum = curriculumKey == null
				? null : curriculumService.getCurriculum(new CurriculumRefImpl(curriculumKey));
		CurriculumElement curriculumElement = curriculumElementKey == null
				? null : curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElementKey));

		VFSContainer subFolder = VFSManager.olatRootContainer("/reports/curriculum/" + task.getKey() + "/");
		
		ExportMetadata metadata = exportManager.getExportMetadataByTask((PersistentTask)task);
		String vfsName = metadata.getFilename();
		exportZip = subFolder.createChildLeaf(vfsName);
		if(exportZip == null) {
			vfsName = VFSManager.rename(subFolder, vfsName);
			exportZip = subFolder.createChildLeaf(vfsName);
		} else {
			String metadataDescr = Formatter.truncate(metadata.getDescription(), 31000);
			Date expirationDate = CalendarUtils.endOfDay(DateUtils.addDays(new Date(), 10));
			fillMetadata(exportZip, title, metadataDescr, expirationDate);
		}

		if(task.getStatus() == TaskStatus.cancelled) {
			if(exportZip != null) {
				exportZip.deleteSilently();
			}
			return;
		}
		
		metadata.setFilename(exportZip.getName());
		metadata.setFilePath(exportZip.getRelPath());
		metadata.setMetadata(exportZip.getMetaInfo());
		metadata = exportManager.updateMetadata(metadata);
		
		ReportContent content = implementation.generateReport(curriculum, curriculumElement, doer, locale, exportZip);
		
		metadata = addRelations(metadata, curriculum, curriculumElement, content);
		
		TaskStatus status = taskExecutorManager.getStatus(task);
		if(status == TaskStatus.cancelled) {
			exportZip.deleteSilently();
			taskExecutorManager.delete(task);
		} else {
			vfsRepositoryService.getMetadataFor(exportZip);
		}
	}
	
	private ExportMetadata addRelations(ExportMetadata metadata, Curriculum curriculum, CurriculumElement element, ReportContent content) {
		final ExportManager exportManager = CoreSpringFactory.getImpl(ExportManager.class);

		Set<Curriculum> curriculums = new HashSet<>();
		Set<Organisation> organisations = new HashSet<>();
		Set<CurriculumElement> curriculumElements = new HashSet<>();
		
		
		if(element != null) {
			curriculumElements.add(element);
			if(element.getCurriculum() != null && element.getCurriculum().getOrganisation() != null) {
				organisations.add(curriculum.getOrganisation());
			}
		}
		if(content != null && content.curriculumElements() != null && !content.curriculumElements().isEmpty()) {
			curriculumElements.addAll(content.curriculumElements());
		}
		
		if(curriculum != null) {
			curriculums.add(curriculum);
			if(curriculum.getOrganisation() != null) {
				organisations.add(curriculum.getOrganisation());
			}
		}
		if(content != null && content.curriculums() != null && !content.curriculums().isEmpty()) {
			curriculums.addAll(content.curriculums());
		}
		
		if(!curriculums.isEmpty()) {
			metadata = exportManager.addMetadataCurriculums(metadata, new ArrayList<>(curriculums));
		}
		if(!organisations.isEmpty()) {
			metadata = exportManager.addMetadataOrganisations(metadata, new ArrayList<>(organisations));
		}
		if(!curriculumElements.isEmpty()) {
			metadata = exportManager.addMetadataCurriculumElements(metadata, new ArrayList<>(curriculumElements));
		}
		
		return metadata;
	}
}
