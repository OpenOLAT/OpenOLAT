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
package de.tuchemnitz.wizard.workflows.coursecreation;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailerResult;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.wizard.AccessAndProperties;
import org.olat.repository.wizard.RepositoryWizardProvider;
import org.springframework.stereotype.Service;

import de.tuchemnitz.wizard.workflows.coursecreation.model.CourseCreationConfiguration;
import de.tuchemnitz.wizard.workflows.coursecreation.steps.CcStep00;

/**
 * 
 * Initial date: 4 Dec 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class SimpleCourseWizardProvider implements RepositoryWizardProvider {

	@Override
	public String getType() {
		return "simple.course";
	}

	@Override
	public String getDisplayName(Locale locale) {
		Translator translator = Util.createPackageTranslator(CourseCreationHelper.class, locale);
		return translator.translate("simple.course.name");
	}

	@Override
	public String getSupportedResourceType() {
		return CourseModule.ORES_TYPE_COURSE;
	}

	@Override
	public StepsMainRunController createWizardController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, Identity executor) {
		Translator translator = Util.createPackageTranslator(CourseCreationHelper.class, ureq.getLocale());
		
		ICourse course = CourseFactory.loadCourse(entry);
		String extLink = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + entry.getKey();
		CourseCreationConfiguration courseConfig = new CourseCreationConfiguration(course.getCourseTitle(), extLink);
		CourseCreationHelper ccHelper = new CourseCreationHelper(ureq.getLocale(), entry, courseConfig, course);

		StepRunnerCallback finishCallback = (uureq, control, runContext) -> {
			AccessAndProperties accessAndProps = (AccessAndProperties) runContext.get("accessAndProperties");
			courseConfig.setAccessAndProperties(accessAndProps);

			ccHelper.finalizeWorkflow(uureq);
			control.setInfo(CourseCreationMailHelper.getSuccessMessageString(uureq));

			MailerResult mr = CourseCreationMailHelper.sentNotificationMail(uureq, ccHelper.getConfiguration());
			Roles roles = uureq.getUserSession().getRoles();
			boolean detailedErrorOuput = roles.isAdministrator() || roles.isSystemAdmin();
			MailHelper.printErrorsAndWarnings(mr, control, detailedErrorOuput, uureq.getLocale());
			
			return StepsMainRunController.DONE_MODIFIED;
		};

		Step start = new CcStep00(ureq, courseConfig, entry);
		return new StepsMainRunController(ureq, wControl, start, finishCallback, null,
				translator.translate("coursecreation.title"), "o_sel_course_create_wizard");
	}

}
