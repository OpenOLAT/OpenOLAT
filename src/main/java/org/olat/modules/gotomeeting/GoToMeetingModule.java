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
package org.olat.modules.gotomeeting;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 21.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GoToMeetingModule extends AbstractSpringModule implements ConfigOnOff {

	public static final String GOTO_ENABLED = "goto.enabled";
	public static final String GOTO_CONSUMERKEY = "goto.training.consumerKey";
	public static final String GOTO_CONSUMERSECRET = "goto.training.consumerSecret";
	public static final String GOTO_TIMEZONEID = "goto.timezone.id";
	
	@Value("${vc.gotomeetings.enabled:false}")
	private boolean enabled;

	@Value("${vc.gotomeetings.training.consumerKey:null}")
	private String trainingConsumerKey;
	@Value("${vc.gotomeetings.training.consumerSecret:null}")
	private String trainingConsumerSecret;
	@Value("${vc.gotomeetings.timezone.id:null}")
	private String goToTimeZoneId;
	
	@Autowired
	public GoToMeetingModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		//module enabled/disabled
		String enabledObj = getStringPropertyValue(GOTO_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String consumerKeyObj = getStringPropertyValue(GOTO_CONSUMERKEY, true);
		if(StringHelper.containsNonWhitespace(consumerKeyObj)) {
			trainingConsumerKey = consumerKeyObj;
		}
		String consumerSecretObj = getStringPropertyValue(GOTO_CONSUMERSECRET, true);
		if(StringHelper.containsNonWhitespace(consumerSecretObj)) {
			trainingConsumerSecret = consumerSecretObj;
		}

		String timezoneObj = getStringPropertyValue(GOTO_TIMEZONEID, true);
		if(StringHelper.containsNonWhitespace(timezoneObj)) {
			goToTimeZoneId = timezoneObj;
		}
	}
	
	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		if(this.enabled != enabled) {
			setStringProperty(GOTO_ENABLED, Boolean.toString(enabled), true);
		}
	}

	public String getTrainingConsumerKey() {
		return trainingConsumerKey;
	}

	public void setTrainingConsumerKey(String consumerKey) {
		this.trainingConsumerKey = consumerKey;
		setStringProperty(GOTO_CONSUMERKEY, consumerKey, true);
	}

	public String getTrainingConsumerSecret() {
		return trainingConsumerSecret;
	}

	public void setTrainingConsumerSecret(String secret) {
		trainingConsumerSecret = secret;
		setStringProperty(GOTO_CONSUMERSECRET, secret, true);
	}

	public String getGoToTimeZoneId() {
		return goToTimeZoneId;
	}

	public void setGoToTimeZoneId(String goToTimeZoneId) {
		this.goToTimeZoneId = goToTimeZoneId;
		setStringProperty(GOTO_TIMEZONEID, goToTimeZoneId, true);
	}
	
	
}
