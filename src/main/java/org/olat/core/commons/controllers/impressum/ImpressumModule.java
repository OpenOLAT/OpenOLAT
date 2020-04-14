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
package org.olat.core.commons.controllers.impressum;

import java.io.File;
import java.nio.file.Paths;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 21.08.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("impressumModule")
public class ImpressumModule extends AbstractSpringModule implements ConfigOnOff {

	private static final String ENABLED = "impressum.enabled";
	private static final String POSITION = "impressum.position";
	private static final String CONTACT_ENABLED = "impressum.contact.enabled";
	private static final String CONTACT_MAIL = "impressum.contact.mail";
	
	@Value("${impressum.enabled:true}")
	private boolean enabled;
	@Value("${impressum.position:footer}")
	private String position;
	@Value("${impressum.contact.enabled:true}")
	private boolean contactEnabled;
	@Value("${impressum.contact.mail:webmaster@your.domain}")
	private String contactMail;
	
	@Autowired
	public ImpressumModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		String enabledObj = getStringPropertyValue(ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String positionObj = getStringPropertyValue(POSITION, true);
		if(StringHelper.containsNonWhitespace(positionObj)) {
			position = positionObj;
		}
		
		String contactEnabledObj = getStringPropertyValue(CONTACT_ENABLED, true);
		if (StringHelper.containsNonWhitespace(contactEnabledObj)) {
			contactEnabled = "true".equals(contactEnabledObj);
		}
		
		String contactMailObj = getStringPropertyValue(CONTACT_MAIL, true);
		if (StringHelper.containsNonWhitespace(contactMailObj)) {
			contactMail = contactMailObj;
		} 
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	public File getImpressumDirectory() {
		File dir = Paths.get(WebappHelper.getUserDataRoot(), "customizing", "impressum").toFile();
		if(!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
	
	public File getTermsOfUseDirectory() {
		File dir = Paths.get(WebappHelper.getUserDataRoot(), "customizing", "terms_of_use").toFile();
		if(!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
	
	public File getPrivacyPolicyDirectory() {
		File dir = Paths.get(WebappHelper.getUserDataRoot(), "customizing", "data_privacy_policy").toFile();
		if(!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(ENABLED, Boolean.toString(enabled), true);
	}
	
	public boolean isContactEnabled() {
		return contactEnabled;
	}
	
	public void setContactEnabled(boolean enabled) {
		this.contactEnabled = enabled;
		setStringProperty(CONTACT_ENABLED, Boolean.toString(contactEnabled), true);
	}
	
	public String getContactMail() {
		return contactMail;
	}
	
	public void setContactMail(String contactMail) {
		this.contactMail = contactMail;
		setStringProperty(CONTACT_MAIL, contactMail, true);
	}
	
	public Position getPosition() {
		if("top".equals(position)) {
			return Position.top;
		} else if("footer".equals(position)) {
			return Position.footer;
		}
		return null;
	}
	
	public void setPosition(String position) {
		this.position = position;
		setStringProperty(POSITION, position, true);
	}
	
	public enum Position {
		none,
		top,
		footer
	}

}
