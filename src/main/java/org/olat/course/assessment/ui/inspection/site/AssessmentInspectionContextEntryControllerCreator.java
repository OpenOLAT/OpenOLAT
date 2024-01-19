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
package org.olat.course.assessment.ui.inspection.site;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.ContextEntryControllerCreator;
import org.olat.core.id.context.DefaultContextEntryControllerCreator;
import org.olat.course.assessment.AssessmentInspectionService;
import org.olat.course.assessment.AssessmentInspection;
import org.olat.course.assessment.ui.inspection.AssessmentInspectionMainController;

/**
 * 
 * Initial date: 3 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionContextEntryControllerCreator extends DefaultContextEntryControllerCreator {

	
	@Override
	public ContextEntryControllerCreator clone() {
		return new AssessmentInspectionContextEntryControllerCreator();
	}
	
	@Override
	public Controller createController(List<ContextEntry> ces, UserRequest ureq, WindowControl wControl) {
		return createLaunchController(ureq, wControl, ces.get(0));
	}
	
	@Override
	public String getTabName(ContextEntry ce, UserRequest ureq) {
		AssessmentInspection inspection = getInspection(ureq, ce);
		return inspection == null ? "" : inspection.getConfiguration().getRepositoryEntry().getDisplayname();
	}

	private Controller createLaunchController(UserRequest ureq, WindowControl wControl, ContextEntry contextEntry) {
		AssessmentInspection inspection = getInspection(ureq, contextEntry);
		return new AssessmentInspectionMainController(ureq, wControl, inspection);
	}
	
	private AssessmentInspection getInspection(UserRequest ureq, ContextEntry contextEntry) {
		AssessmentInspection inspection = null;
		if(contextEntry != null && contextEntry.getOLATResourceable().getResourceableId().longValue() > 0) {
			Long inspectionKey = contextEntry.getOLATResourceable().getResourceableId();
			AssessmentInspectionService inspectionService = CoreSpringFactory.getImpl(AssessmentInspectionService.class);
			inspection = inspectionService.getInspectionFor(ureq.getIdentity(), ureq.getRequestTimestamp(), inspectionKey);
		}
		return inspection;
	}
}
