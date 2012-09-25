/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.course.assessment;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.StackedController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.course.ICourse;

/**
 * Description:<br>
 * TODO: patrickb Class Description for AssessmentUIFactory
 * 
 * <P>
 * Initial Date:  29.06.2010 <br>
 * @author patrickb
 */
public class AssessmentUIFactory {

	private static AssessmentControllerCreator controllerCreator = null;
	
	AssessmentUIFactory(AssessmentControllerCreator specificControllerCreator) {
		AssessmentUIFactory.controllerCreator = specificControllerCreator;
	}
	
	
	public static Activateable2 createAssessmentMainController(UserRequest ureq, WindowControl wControl, StackedController stackPanel, OLATResourceable ores, IAssessmentCallback assessmentCallback) {
		return controllerCreator.createAssessmentMainController(ureq, wControl, stackPanel, ores, assessmentCallback);
	}
	
	public static Controller createQTIArchiveWizardController(boolean dummyMode, UserRequest ureq, List nodesTableObjectArrayList, ICourse course,
			WindowControl windowControl) {
		return controllerCreator.createQTIArchiveWizardController(dummyMode, ureq, nodesTableObjectArrayList, course, windowControl);
	}
	
	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param course
	 * @return null if no contextualSubscriptionController is available
	 */
	public static Controller createContextualSubscriptionController(UserRequest ureq, WindowControl wControl, ICourse course){
		return controllerCreator.createContextualSubscriptionController(ureq, wControl, course);
	}
	
}
