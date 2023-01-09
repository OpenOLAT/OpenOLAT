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
package org.olat.modules.coach;

import org.olat.NewControllerFactory;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.modules.coach.site.CoachContextEntryControllerCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  8 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service
public class CoachingModule  extends AbstractSpringModule implements ConfigOnOff {

	@Value("${coaching.enabled:true}")
	private boolean enabled;
	@Value("${password.change.by.coach.allowed:false}")
	private boolean resetPasswordEnabled;
	
	@Autowired
	public CoachingModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager, "com.frentix.olat.coach.CoachingModule");
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	

	public void setEnabled(boolean enabled) {
		if(this.enabled != enabled) {
			setStringProperty("coaching.enabled", Boolean.toString(enabled), true);
		}
	}
	
	public boolean isResetPasswordEnabled() {
		return resetPasswordEnabled;
	}

	@Override
	public void init() {
		// Add controller factory extension point to launch groups
		NewControllerFactory.getInstance().addContextEntryControllerCreator("coaching",
				new CoachContextEntryControllerCreator());
		NewControllerFactory.getInstance().addContextEntryControllerCreator("CoachSite",
				new CoachContextEntryControllerCreator());

		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	private void updateProperties() {
		String enabledObj = getStringPropertyValue("coaching.enabled", true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
	}
}
