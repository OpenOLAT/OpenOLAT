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
package org.olat.modules.curriculum.ui.reports;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.export.ExportManager;
import org.olat.core.commons.services.export.ExportMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.coach.reports.ReportConfiguration;
import org.olat.modules.coach.ui.manager.ReportTemplatesController;
import org.olat.modules.coach.ui.manager.ReportTemplatesRow;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumReportConfiguration;
import org.olat.modules.curriculum.reports.CurriculumReportTask;
import org.olat.modules.curriculum.ui.CurriculumManagerRootController;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumReportTemplatesController extends ReportTemplatesController {
	
	private final ArchiveType type;
	private final Curriculum curriculum;
	private final CurriculumElement curriculumElement;

	@Autowired
	private ExportManager exportManager;
	
	public CurriculumReportTemplatesController(UserRequest ureq, WindowControl wControl,
			Curriculum curriculum, CurriculumElement curriculumElement, ArchiveType type) {
		super(ureq, wControl, Util.createPackageTranslator(ReportTemplatesController.class, ureq.getLocale(),
				Util.createPackageTranslator(CurriculumManagerRootController.class, ureq.getLocale()))
				,"manual_user/area_modules/Course_Planner_Reports/");
		this.type = type;
		this.curriculum = curriculum;
		this.curriculumElement = curriculumElement;
	}

	@Override
	protected void doRunReport(UserRequest ureq, ReportTemplatesRow row) {
		ReportConfiguration config = row.getReportConfiguration();
		if(config instanceof CurriculumReportConfiguration curriculumConfig) {
			doRunReport(curriculumConfig);
		} else {
			row.getReportConfiguration().generateReport(getIdentity(), ureq.getLocale());
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doRunReport(CurriculumReportConfiguration config) {
		String title = config.getName(getLocale());
		String fileName = StringHelper.transformDisplayNameToFileSystemName(title) + "_" +
				Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis())) + ".xlsx";
		
		OLATResource resource = null;
		if(curriculumElement != null) {
			resource = curriculumElement.getResource();
		}
		Date expirationDate = DateUtils.addDays(new Date(), 10);
		CurriculumReportTask task = new CurriculumReportTask(title, curriculum, curriculumElement, getIdentity(), getLocale(), config);
		ExportMetadata metadata = exportManager.startExport(task, title, config.getDescription(getLocale()), fileName,
				type, expirationDate, false, resource, CurriculumReportsListController.CURRICULUM_REPORT_IDENT, getIdentity());
		
		if(curriculum != null) {
			metadata = exportManager.addMetadataCurriculums(metadata, List.of(curriculum));
		}
		if(curriculumElement != null) {
			metadata = exportManager.addMetadataCurriculumElements(metadata, List.of(curriculumElement));
		}
	}
}
