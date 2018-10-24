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
package org.olat.modules.quality;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.modules.quality.site.QualityContextEntryControllerCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 04.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QualityModule extends AbstractSpringModule implements ConfigOnOff {
	
	private static final String QUALITY_ENABLED = "quality.enabled";
	private static final String SUGGESTION_ENABLED = "quality.suggestion.enabled";
	private static final String SUGGESTION_EMAIL_ADDRESSES = "quality.suggestion.addresses";
	private static final String SUGGESTION_EMAIL_SUBJECT = "quality.suggestion.email.subject";
	private static final String SUGGESTION_EMAIL_BODY = "quality.suggestion.email.body";
	private static final String DELIMITER = ",";
	
	@Value("${quality.enabled:false}")
	private boolean enabled;
	
	@Value("${quality.suggestion:false}")
	private boolean suggestionEnabled;
	private String suggestionEmailAddresses;
	private String suggestionEmailSubject;
	private String suggestionEmailBody;

	@Autowired
	public QualityModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}
	
	@Override
	public void init() {
		NewControllerFactory.getInstance().addContextEntryControllerCreator("QualitySite",
				new QualityContextEntryControllerCreator(this));
		
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}

	private void updateProperties() {
		String enabledObj = getStringPropertyValue(QUALITY_ENABLED, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String suggestionEnabledObj = getStringPropertyValue(SUGGESTION_ENABLED, true);
		if (StringHelper.containsNonWhitespace(suggestionEnabledObj)) {
			suggestionEnabled = "true".equals(suggestionEnabledObj);
		}
		
		String suggestionEmailAddressesObj = getStringPropertyValue(SUGGESTION_EMAIL_ADDRESSES, true);
		if (StringHelper.containsNonWhitespace(suggestionEmailAddressesObj)) {
			suggestionEmailAddresses = suggestionEmailAddressesObj;
		}
		
		String suggestionEmailSubjectObj = getStringPropertyValue(SUGGESTION_EMAIL_SUBJECT, true);
		if (StringHelper.containsNonWhitespace(suggestionEmailSubjectObj)) {
			suggestionEmailSubject = suggestionEmailSubjectObj;
		}
		
		String suggestionEmailBodyObj = getStringPropertyValue(SUGGESTION_EMAIL_BODY, true);
		if (StringHelper.containsNonWhitespace(suggestionEmailBodyObj)) {
			suggestionEmailBody = suggestionEmailBodyObj;
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(QUALITY_ENABLED, Boolean.toString(enabled), true);
	}
	
	public boolean isSuggestionEnabled() {
		return suggestionEnabled;
	}
	
	public void setSuggestionEnabled(boolean suggestionEnabled) {
		this.suggestionEnabled = suggestionEnabled;
		setStringProperty(SUGGESTION_ENABLED, Boolean.toString(suggestionEnabled), true);
	}

	public List<String> getSuggestionEmailAddresses() {
		if (!StringHelper.containsNonWhitespace(suggestionEmailAddresses)) return Collections.emptyList();
		
		return Arrays.asList(suggestionEmailAddresses.split(DELIMITER));
	}

	public void setSuggestionEmailAddresses(List<String> emailAddresses) {
		String joinedAddresses = emailAddresses.stream().collect(Collectors.joining(DELIMITER));
		this.suggestionEmailAddresses = joinedAddresses;
		setStringProperty(SUGGESTION_EMAIL_ADDRESSES, joinedAddresses, true);
	}

	public String getSuggestionEmailSubject() {
		return suggestionEmailSubject;
	}

	public void setSuggestionEmailSubject(String suggestionEmailSubject) {
		this.suggestionEmailSubject = suggestionEmailSubject;
		setStringProperty(SUGGESTION_EMAIL_SUBJECT, suggestionEmailSubject, true);
	}

	public String getSuggestionEmailBody() {
		return suggestionEmailBody;
	}

	public void setSuggestionEmailBody(String suggestionEmailBody) {
		this.suggestionEmailBody = suggestionEmailBody;
		setStringProperty(SUGGESTION_EMAIL_BODY, suggestionEmailBody, true);
	}

}
