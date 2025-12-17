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
package org.olat.course.nodes.ms.manager;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.util.WindowControlMocker;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.course.ICourse;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeExport;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeExportTask;
import org.olat.course.assessment.ui.tool.IdentitiesList;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.forms.EvaluationFormProvider;

/**
 * 
 * Initial date: Dec 15, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class MSAssessmentExportTask extends AssessmentCourseNodeExportTask {

	private static final long serialVersionUID = -253208918198631735L;

	private final EvaluationFormProvider evaluationFormProvider;

	public MSAssessmentExportTask(OLATResourceable courseRes, CourseNode courseNode, IdentitiesList identities,
			String title, String description, boolean withPdfs, Locale locale, EvaluationFormProvider evaluationFormProvider) {
		super(courseRes, courseNode, identities, title, description, withPdfs, locale);
		this.evaluationFormProvider = evaluationFormProvider;
	}

	@Override
	protected AssessmentCourseNodeExport createExport(ICourse course, List<Identity> identities) {
		return new MSAssessmentExport(task.getCreator(), course.getCourseEnvironment(), courseNode,
				identities, withNonParticipants, withPdfs, locale, new WindowControlMocker(), evaluationFormProvider);
	}

}
