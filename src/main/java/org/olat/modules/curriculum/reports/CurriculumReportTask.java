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
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.export.ExportManager;
import org.olat.core.commons.services.export.ExportMetadata;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.coach.reports.AbstractReportTask;
import org.olat.modules.coach.reports.ReportConfiguration;
import org.olat.modules.coach.reports.ReportGenerationException;
import org.olat.modules.coach.reports.ReportParameters;
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
public class CurriculumReportTask extends AbstractReportTask {
	
	private static final long serialVersionUID = -4740082752176076855L;

	private final Long curriculumKey;
	private final Long curriculumElementKey;
	
	public CurriculumReportTask(String title, Curriculum curriculum, CurriculumElement curriculumElement,
			IdentityRef doer, Locale locale, String templateId, ReportParameters params) {
		super(title, doer, locale, templateId, params);
		curriculumKey = curriculum == null ? null : curriculum.getKey();
		curriculumElementKey = curriculumElement == null ? null : curriculumElement.getKey();
	}

	@Override
	protected VFSContainer getOutputFolder(Identity doer) {
		return VFSManager.olatRootContainer("/reports/curriculum/" + task.getKey() + "/");
	}

	@Override
	protected void generateReport(ReportConfiguration template, Identity doer, ReportParameters parameters,
			Locale locale, VFSLeaf output, ExportMetadata metadata) {
		if(!(template instanceof CurriculumReportConfiguration curriculumTemplate)) {
			throw new ReportGenerationException("Not a Course Planner report template: " + getTemplateId());
		}
		
		final CurriculumService curriculumService = CoreSpringFactory.getImpl(CurriculumService.class);
		Curriculum curriculum = curriculumKey == null
				? null : curriculumService.getCurriculum(new CurriculumRefImpl(curriculumKey));
		CurriculumElement curriculumElement = curriculumElementKey == null
				? null : curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElementKey));
		
		ReportContent content = curriculumTemplate.generateReport(curriculum, curriculumElement, doer, parameters, locale, output);
		addRelations(metadata, curriculum, curriculumElement, content);
	}
	
	private ExportMetadata addRelations(ExportMetadata metadata, Curriculum curriculum, CurriculumElement element, ReportContent content) {
		final ExportManager exportManager = CoreSpringFactory.getImpl(ExportManager.class);

		Set<Curriculum> curriculums = new HashSet<>();
		Set<Organisation> organisations = new HashSet<>();
		Set<CurriculumElement> curriculumElements = new HashSet<>();
		
		
		if(element != null) {
			curriculumElements.add(element);
			if(element.getCurriculum() != null && element.getCurriculum().getOrganisation() != null) {
				organisations.add(element.getCurriculum().getOrganisation());
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
