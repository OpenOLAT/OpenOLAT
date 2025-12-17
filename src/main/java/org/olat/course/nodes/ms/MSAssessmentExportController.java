/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.nodes.ms;

import java.util.Date;

import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.ui.tool.AssessmentExportController;
import org.olat.course.assessment.ui.tool.IdentitiesList;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ms.manager.MSAssessmentExportTask;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.forms.EvaluationFormProvider;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: Dec 15, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class MSAssessmentExportController extends AssessmentExportController {

	private final EvaluationFormProvider evaluationFormProvider;
	private final ArchiveType archiveType;

	public MSAssessmentExportController(UserRequest ureq, WindowControl wControl, CourseEnvironment courseEnv,
			CourseNode courseNode, IdentitiesList identities, EvaluationFormProvider evaluationFormProvider,
			ArchiveType archiveType) {
		super(ureq, wControl, courseEnv, courseNode, identities);
		this.evaluationFormProvider = evaluationFormProvider;
		this.archiveType = archiveType;
	}

	@Override
	protected void doStartExport(OLATResource resource, RepositoryEntry entry, String title, String description,
			String filename, Date expirationDate, boolean withPdfs) {
		MSAssessmentExportTask task = new MSAssessmentExportTask(resource, courseNode, identities, title, description,
				withPdfs, getLocale(), evaluationFormProvider);
		
		exportManager.startExport(task, title, description, filename, archiveType, expirationDate, false, entry,
				courseNode.getIdent(), getIdentity());
	}

}
