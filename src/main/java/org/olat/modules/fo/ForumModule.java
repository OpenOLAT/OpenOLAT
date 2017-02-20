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
package org.olat.modules.fo;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 10.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("forumModule")
public class ForumModule extends AbstractSpringModule {
	
	@Value("${forum.anonymous.posting.enabled:disabled}")
	private String anonymousPostingWithPseudonymEnabled;
	@Value("${forum.course.default:disabled}")
	private String courseForumDefault;
	@Value("${forum.group.default:disabled}")
	private String groupForumDefault;
	@Value("${forum.message.default:disabled}")
	private String messageForumDefault;
	
	@Autowired
	public ForumModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		initFromChangedProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		String anonymousPostingObj = getStringPropertyValue("forum.anonymous.posting.enabled", true);
		if(StringHelper.containsNonWhitespace(anonymousPostingObj)) {
			anonymousPostingWithPseudonymEnabled = anonymousPostingObj;
		}
		String courseDefaultObj = getStringPropertyValue("forum.course.default", true);
		if(StringHelper.containsNonWhitespace(courseDefaultObj)) {
			courseForumDefault = courseDefaultObj;
		}
		String groupDefaultObj = getStringPropertyValue("forum.group.default", true);
		if(StringHelper.containsNonWhitespace(groupDefaultObj)) {
			groupForumDefault = groupDefaultObj;
		}
		
		String messageDefaultObj = getStringPropertyValue("forum.message.default", true);
		if(StringHelper.containsNonWhitespace(messageDefaultObj)) {
			messageForumDefault = messageDefaultObj;
		}
	}
	
	public boolean isAnonymousPostingWithPseudonymEnabled() {
		return "enabled".equals(anonymousPostingWithPseudonymEnabled);
	}
	
	public void setAnonymousPostingWithPseudonymEnabled(boolean enabled) {
		anonymousPostingWithPseudonymEnabled = enabled ? "enabled" : "disabled";
		setStringProperty("forum.anonymous.posting.enabled", enabled ? "enabled" : "disabled", true);
	}
	
	public boolean isPseudonymForCourseEnabledByDefault() {
		return "enabled".equals(courseForumDefault);
	}
	
	public void setPseudonymForCourseEnabledByDefault(boolean enabled) {
		courseForumDefault = enabled ? "enabled" : "disabled";
		setStringProperty("forum.course.default", enabled ? "enabled" : "disabled", true);
	}
	
	public boolean isPseudonymForGroupEnabledByDefault() {
		return "enabled".equals(groupForumDefault);
	}
	
	public void setPseudonymForGroupEnabledByDefault(boolean enabled) {
		groupForumDefault = enabled ? "enabled" : "disabled";
		setStringProperty("forum.group.default", enabled ? "enabled" : "disabled", true);
	}
	
	public boolean isPseudonymForMessageEnabledByDefault() {
		return "enabled".equals(messageForumDefault);
	}
	
	public void setPseudonymForMessageEnabledByDefault(boolean enabled) {
		messageForumDefault = enabled ? "enabled" : "disabled";
		setStringProperty("forum.message.default", enabled ? "enabled" : "disabled", true);
	}
}