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
package org.olat.course.wizard.provider.exam;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.CourseModule;
import org.olat.course.wizard.CourseWizardCallback;
import org.olat.course.wizard.CourseWizardService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.wizard.RepositoryWizardProvider;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 7 Dec 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ExamCourseWizardProvider implements RepositoryWizardProvider {

	@Override
	public String getType() {
		return "exam.course";
	}

	@Override
	public String getDisplayName(Locale locale) {
		Translator translator = Util.createPackageTranslator(CourseWizardService.class, locale);
		return translator.translate("exam.course.name");
	}

	@Override
	public String getSupportedResourceType() {
		return CourseModule.ORES_TYPE_COURSE;
	}

	@Override
	public StepsMainRunController createWizardController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, Identity executor) {
		Translator translator = Util.createPackageTranslator(CourseWizardService.class, ureq.getLocale());
		ExamCourseSteps examCourseSteps = new ExamCourseSteps();
		Step start = new ExamCourseSetpsStep(ureq, entry, examCourseSteps);
		CourseWizardCallback finish = new CourseWizardCallback(executor);
		finish.setBeforeExecution(new ExamCourseBeforeExecution(examCourseSteps));
		return new StepsMainRunController(ureq, wControl, start, finish, null, translator.translate("exam.course.name"), null);
	}

}
