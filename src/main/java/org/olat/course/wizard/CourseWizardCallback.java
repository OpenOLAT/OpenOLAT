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
package org.olat.course.wizard;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.course.ICourse;
import org.olat.course.wizard.ui.CertificateController;
import org.olat.course.wizard.ui.PublicationController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.wizard.InfoMetadata;
import org.olat.repository.wizard.ui.InfoMetadataController;

/**
 * 
 * Initial date: 7 Dec 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseWizardCallback implements StepRunnerCallback {
	
	public static final String RUN_CONTEXT_TEST = "test";
	public static final String RUN_CONTEXT_RETEST = "retest";
	
	private final Identity executor;
	
	public CourseWizardCallback(Identity executor) {
		this.executor = executor;
	}

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		CourseWizardService courseWizardService = CoreSpringFactory.getImpl(CourseWizardService.class);
		
		RepositoryEntry entry = (RepositoryEntry) runContext.get("repoEntry");
		ICourse course = courseWizardService.startCourseEditSession(entry);
		if (course == null) {
			return StepsMainRunController.DONE_UNCHANGED;
		}
		
		if (runContext.containsKey(InfoMetadataController.RUN_CONTEXT_KEY)) {
			InfoMetadata infoMetadata = (InfoMetadata) runContext.get(InfoMetadataController.RUN_CONTEXT_KEY);
			courseWizardService.updateRepositoryEntry(entry, infoMetadata);
		}
		
		if (runContext.containsKey(RUN_CONTEXT_TEST)) {
			IQTESTCourseNodeDefaults defaults = (IQTESTCourseNodeDefaults) runContext.get(RUN_CONTEXT_TEST);
			courseWizardService.createIQTESTCourseNode(course, defaults);
		}
		
		if (runContext.containsKey(RUN_CONTEXT_RETEST)) {
			IQTESTCourseNodeDefaults defaults = (IQTESTCourseNodeDefaults) runContext.get(RUN_CONTEXT_RETEST);
			courseWizardService.createIQTESTCourseNode(course, defaults);
		}
		
		if (runContext.containsKey(CertificateController.RUN_CONTEXT_KEY)) {
			CertificateDefaults defaults = (CertificateDefaults) runContext.get(CertificateController.RUN_CONTEXT_KEY);
			courseWizardService.setCertificateConfigs(course, defaults);
		}
		
		if (runContext.containsKey(PublicationController.RUN_CONTEXT_KEY)) {
			PublicationContext context = (PublicationContext) runContext.get(PublicationController.RUN_CONTEXT_KEY);
			if (context.isPublish()) {
				courseWizardService.publishCourse(null, course);
			}
			courseWizardService.updateEntryStatus(executor, entry, context.getStatus());
		}
		
		courseWizardService.finishCourseEditSession(course);
		
		return StepsMainRunController.DONE_MODIFIED;
	}

}
