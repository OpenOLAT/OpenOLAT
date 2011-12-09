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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.assessment;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable;
import org.olat.core.id.OLATResourceable;

import de.bps.course.assessment.AssessmentMainController;

import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentControllerCreatorOlat;
import org.olat.course.assessment.IAssessmentCallback;
import de.bps.ims.qti.export.QTIArchiveWizardController;

public class AssessmentControllerCreatorOnyx extends AssessmentControllerCreatorOlat {

	/**
	 * @see org.olat.course.assessment.AssessmentMainControllerCreator#create(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl, org.olat.core.id.OLATResourceable, org.olat.course.assessment.IAssessmentCallback)
	 */
	@Override
	public Activateable createAssessmentMainController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, IAssessmentCallback assessmentCallback) {
		return new AssessmentMainController(ureq, wControl, ores, assessmentCallback);
	}
	
	/**
	 * @see org.olat.course.assessment.AssessmentControllerCreator#createQTIArchiveWizardController(boolean, org.olat.core.gui.UserRequest, java.util.List, org.olat.course.ICourse, org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public Controller createQTIArchiveWizardController(boolean dummyMode, UserRequest ureq, List nodesTableObjectArrayList, ICourse course,
			WindowControl wControl) {
		return new QTIArchiveWizardController(dummyMode, ureq, nodesTableObjectArrayList, course, wControl);
	}
}
