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

import java.util.List;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.export.model.CurriculumReportBlocParameters;
import org.olat.core.commons.services.export.model.SearchExportMetadataParameters;
import org.olat.core.commons.services.export.ui.ExportsListController;
import org.olat.core.commons.services.export.ui.ExportsListSettings;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.ui.CurriculumManagerRootController;

/**
 * 
 * Initial date: 29 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumReportsListController extends ExportsListController {
	
	public static final String CURRICULUM_REPORT_IDENT = "curriculum";
	
	private final ArchiveType type;
	private final Curriculum curriculum;
	private final CurriculumElement curriculumElement;
	private final List<Curriculum> adminsCurriculums;

	public CurriculumReportsListController(UserRequest ureq, WindowControl wControl, 
			List<Curriculum> adminsCurriculums, Curriculum curriculum, CurriculumElement curriculumElement,
			ArchiveType type, ExportsListSettings options) {
		super(ureq, wControl, null, null, true, options, "export_list", Util
				.createPackageTranslator(CurriculumManagerRootController.class, ureq.getLocale()));
		this.type = type;
		this.curriculum = curriculum;
		this.adminsCurriculums = adminsCurriculums;
		this.curriculumElement = curriculumElement;
		loadModel();
	}
	
	@Override
	public SearchExportMetadataParameters getSearchParams() {
		SearchExportMetadataParameters params = new SearchExportMetadataParameters(null, null, List.of(type));
		if(curriculumElement != null) {
			params.setReportSubParameters(new CurriculumReportBlocParameters(getIdentity(), null, List.of(curriculumElement)));
		} else if(curriculum != null) {
			params.setReportSubParameters(new CurriculumReportBlocParameters(getIdentity(), List.of(curriculum), null));
		} else if(adminsCurriculums != null && !adminsCurriculums.isEmpty()) {
			params.setReportSubParameters(new CurriculumReportBlocParameters(getIdentity(), adminsCurriculums, null));
		} else {
			params.setOrganisationRoles(getIdentity(), List.of(OrganisationRoles.curriculummanager));
		}
		return params;
	}
}
