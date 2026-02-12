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
package org.olat.modules.curriculum.ui.importwizard;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportCurriculumsFinishStepCallback implements StepRunnerCallback {
	
	private static final Logger log = Tracing.createLoggerFor(ImportCurriculumsFinishStepCallback.class);
	
	private final ImportCurriculumsContext context;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private OrganisationService organisationService;
	
	public ImportCurriculumsFinishStepCallback(ImportCurriculumsContext context) {
		CoreSpringFactory.autowireObject(this);
		this.context = context;
	}

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		List<CurriculumImportedRow> importedRows = context.getImportedRows();
		for(CurriculumImportedRow importedRow:importedRows) {
			if(importedRow.isIgnored()
					|| importedRow.getStatus() == ImportCurriculumsStatus.NO_CHANGES
					|| importedRow.getStatus() == ImportCurriculumsStatus.ERROR
					|| importedRow.getValidationStatistics().errors() > 0) {
				log.debug("Curriculum not imported: status: {}, errors: {}, to ignore: {}",
						importedRow.getStatus(), importedRow.getValidationStatistics().errors(), importedRow.isIgnored());
				continue;
			}
			
			if(importedRow.getCurriculum() == null) {
				createCurriculum(importedRow);
			} else {
				Curriculum curriculum = curriculumService.getCurriculum(importedRow.getCurriculum());
				if(curriculum != null) {
					updateCurriculum(curriculum, importedRow);
				}
			}
			dbInstance.commit();
		}
		dbInstance.commitAndCloseSession();
		return StepsMainRunController.DONE_MODIFIED;
	}
	
	private void createCurriculum(CurriculumImportedRow importedRow) {
		String identifier = Formatter.truncateOnly(importedRow.getIdentifier(), 255);
		String displayName = Formatter.truncateOnly(importedRow.getDisplayName(), 255);
		String description = importedRow.getDescription();
		
		Organisation organisation = importedRow.getOrganisation();
		if(organisation == null) {
			organisation = organisationService.getDefaultOrganisation();
		}
		
		if(StringHelper.containsNonWhitespace(identifier)
				&& StringHelper.containsNonWhitespace(displayName)
				&& organisation != null) {
			boolean absences = "ON".equalsIgnoreCase(importedRow.getAbsences());
			curriculumService.createCurriculum(identifier, displayName, description, absences, organisation);
		} else {
			log.debug("Curriculum not imported, missing mandatory data: {}, {}, {}",
					identifier, displayName, organisation);
		}
	}
	
	private void updateCurriculum(Curriculum curriculum, CurriculumImportedRow importedRow) {
		String title = importedRow.getDisplayName();
		if(StringHelper.containsNonWhitespace(title)) {
			curriculum.setDisplayName(Formatter.truncateOnly(title, 255));
		}
		
		String description = importedRow.getDescription();
		if(StringHelper.containsNonWhitespace(description)) {
			curriculum.setDescription(description);
		}
		
		if("ON".equalsIgnoreCase(importedRow.getAbsences()) || "OFF".equalsIgnoreCase(importedRow.getAbsences())) {
			curriculum.setLecturesEnabled("ON".equalsIgnoreCase(importedRow.getAbsences()));
		}
		curriculumService.updateCurriculum(curriculum);
		dbInstance.commit();
	}

	
}
