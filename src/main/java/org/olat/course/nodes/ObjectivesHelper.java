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

package org.olat.course.nodes;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * Description:<br>
 * 
 * @author Felix Jost
 */
public class ObjectivesHelper {

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(ObjectivesHelper.class);

	/**
	 * Creates a velocity container that displays the given learning objective
	 * 
	 * @param learningObjectives The learning objective
	 * @param ureq The user request
	 * @return the wrapper component
	 */
	public static Component createLearningObjectivesComponent(String learningObjectives, UserRequest ureq) {
		return createLearningObjectivesComponent(learningObjectives, ureq.getLocale());
	}
	//fxdiff FXOLAT-116: SCORM improvements
	public static Component createLearningObjectivesComponent(String learningObjectives, Locale locale) {
		Translator trans = Util.createPackageTranslator(ObjectivesHelper.class, locale);
		VelocityContainer vc = new VelocityContainer("learningObjs", VELOCITY_ROOT + "/objectives.html", trans, null);
		vc.contextPut("learningObjectives", learningObjectives);
		return vc;
	}
}
