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

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Description:<br>
 * This is a PublishEvent listener, and triggers the update of the EfficiencyStatements 
 * for the published course. It only considers the events from the same JVM.
 * 
 * <P>
 * Initial Date: 11.08.2006 <br>
 * 
 * @author patrickb
 */
@Service("assessmentModule")
public class AssessmentModule extends AbstractSpringModule {
	
	@Value("${assessment.mode:enabled}")
	private String assessmentModeEnabled;
	
	@Autowired
	public AssessmentModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}

	@Override
	public void init() {
		updateProperties();
	}
	
	private void updateProperties() {
		String enabledObj = getStringPropertyValue("assessment.mode", true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			assessmentModeEnabled = enabledObj;
		}
	}

	public boolean isAssessmentModeEnabled() {
		return "enabled".equals(assessmentModeEnabled);
	}

	public void setAssessmentModeEnabled(boolean enabled) {
		assessmentModeEnabled = enabled ? "enabled" : "disabled";
		setStringProperty("assessment.mode", assessmentModeEnabled, true);
	}
}
