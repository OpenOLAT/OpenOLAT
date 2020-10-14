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
* <p>
*/ 

package org.olat.core.commons.services.webdav;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("webdavModule")
public class WebDAVModule extends AbstractSpringModule implements ConfigOnOff {

	private static final String WEBDAV_ENABLED = "webdav.enabled";
	private static final String WEBDAV_LINKS_ENABLED = "webdav.links.enabled";
	private static final String DIGEST_AUTH_ENABLED = "auth.digest.enabled";
	private static final String TERMS_FOLDERS_ENABLED = "webdav.termsfolders.enabled";
	private static final String LEARNERS_BOOKMARKS_COURSE = "webdav.learners.bookmarks.courses";
	private static final String LEARNERS_PARTICIPATING_COURSES = "webdav.learners.participating.courses";
	private static final String PREPEND_COURSE_REFERENCE_TO_TITLE = "webdav.prepend.course.reference.to.title";
	private static final String CURRICULUM_ELEMENTS_FOLDERS_ENABLED = "webdav.curriculumelements.folders.enabled";
	private static final String MANAGED_FOLDERS_ENABLED = "webdav.managed.folders.enabled";
	private static final String USER_AGENT_EXCLUSION_LIST = "webdav.user.agent.exclusion.list";
	
	@Autowired
	private List<WebDAVProvider> webdavProviders;

	@Value("${webdav.enabled:true}")
	private boolean enabled;
	@Value("${webdav.links.enabled:true}")
	private boolean linkEnabled;
	@Value("${auth.digest.enabled:true}")
	private boolean digestAuthenticationEnabled;
	@Value("${webdav.termsfolders.enabled:true}")
	private boolean termsFoldersEnabled;
	@Value("${webdav.managed.folders.enabled:false}")
	private boolean managedFoldersEnabled;
	@Value("${webdav.curriculumelements.folders.enabled:false}")
	private boolean curriculumElementFoldersEnabled;
	@Value("${webdav.prepend.course.reference.to.title:false}")
	private boolean prependCourseReferenceToTitle;
	@Value("${webdav.basic.authentication.black.list}")
	private String basicAuthenticationExclusionList;
	@Value("${webdav.user.agent.black.list}")
	private String userAgentExclusionList;
	
	@Value("${webdav.learners.bookmarks.enabled:true}")
	private boolean enableLearnersBookmarksCourse;
	/**
	 * Enable courses in WebDAV for participants and coaches
	 */
	@Value("${webdav.learners.participatingCourses.enabled:true}")
	private boolean enableLearnersParticipatingCourses;
	
	@Autowired
	public WebDAVModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}
	
	@Override
	public void init() {
		//module enabled/disabled
		String enabledObj = getStringPropertyValue(WEBDAV_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String linkEnabledObj = getStringPropertyValue(WEBDAV_LINKS_ENABLED, true);
		if(StringHelper.containsNonWhitespace(linkEnabledObj)) {
			linkEnabled = "true".equals(linkEnabledObj);
		}
		
		String digestEnabledObj = getStringPropertyValue(DIGEST_AUTH_ENABLED, true);
		if(StringHelper.containsNonWhitespace(digestEnabledObj)) {
			digestAuthenticationEnabled = "true".equals(digestEnabledObj);
		}

		String termsFoldersEnabledObj = getStringPropertyValue(TERMS_FOLDERS_ENABLED, true);
		if(StringHelper.containsNonWhitespace(termsFoldersEnabledObj)) {
			termsFoldersEnabled = "true".equals(termsFoldersEnabledObj);
		}
		
		String managedFoldersEnabledObj = getStringPropertyValue(MANAGED_FOLDERS_ENABLED, true);
		if(StringHelper.containsNonWhitespace(managedFoldersEnabledObj)) {
			managedFoldersEnabled = "true".equals(managedFoldersEnabledObj);
		}
		
		String curriculumElementsFoldersEnabledObj = getStringPropertyValue(CURRICULUM_ELEMENTS_FOLDERS_ENABLED, true);
		if(StringHelper.containsNonWhitespace(curriculumElementsFoldersEnabledObj)) {
			curriculumElementFoldersEnabled = "true".equals(curriculumElementsFoldersEnabledObj);
		}
		
		String learnersBookmarksCourseObj = getStringPropertyValue(LEARNERS_BOOKMARKS_COURSE, true);
		if(StringHelper.containsNonWhitespace(learnersBookmarksCourseObj)) {
			enableLearnersBookmarksCourse = "true".equals(learnersBookmarksCourseObj);
		}
		String learnersParticipatingCoursesObj = getStringPropertyValue(LEARNERS_PARTICIPATING_COURSES, true);
		if(StringHelper.containsNonWhitespace(learnersParticipatingCoursesObj)) {
			enableLearnersParticipatingCourses = "true".equals(learnersParticipatingCoursesObj);
		}
		String prependCourseReferenceToTitleObj = getStringPropertyValue(PREPEND_COURSE_REFERENCE_TO_TITLE, true);
		if(StringHelper.containsNonWhitespace(prependCourseReferenceToTitleObj)) {
			prependCourseReferenceToTitle = "true".equals(prependCourseReferenceToTitleObj);
		}
		
		userAgentExclusionList = getStringPropertyValue(USER_AGENT_EXCLUSION_LIST, userAgentExclusionList);
		if("oo_empty_oo".equals(userAgentExclusionList)) {
			userAgentExclusionList = null;
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
		this.enabled = enabled;
		String enabledStr = enabled ? "true" : "false";
		setStringProperty(WEBDAV_ENABLED, enabledStr, true);
	}
	
	public boolean isLinkEnabled() {
		return linkEnabled;
	}

	public void setLinkEnabled(boolean linkEnabled) {
		this.linkEnabled = linkEnabled;
		String enabledStr = linkEnabled ? "true" : "false";
		setStringProperty(WEBDAV_LINKS_ENABLED, enabledStr, true);
	}

	public boolean isDigestAuthenticationEnabled() {
		return digestAuthenticationEnabled;
	}
	
	public void setDigestAuthenticationEnabled(boolean digestAuthenticationEnabled) {
		this.digestAuthenticationEnabled = digestAuthenticationEnabled;
		String enabledStr = digestAuthenticationEnabled ? "true" : "false";
		setStringProperty(DIGEST_AUTH_ENABLED, enabledStr, true);
	}
	
	public boolean isTermsFoldersEnabled() {
		return termsFoldersEnabled;
	}

	public void setTermsFoldersEnabled(boolean termsFoldersEnabled) {
		this.termsFoldersEnabled = termsFoldersEnabled;
		String enabledStr = termsFoldersEnabled ? "true" : "false";
		setStringProperty(TERMS_FOLDERS_ENABLED, enabledStr, true);
	}

	public boolean isManagedFoldersEnabled() {
		return managedFoldersEnabled;
	}

	public void setManagedFoldersEnabled(boolean enabled) {
		managedFoldersEnabled = enabled;
		setStringProperty(MANAGED_FOLDERS_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isCurriculumElementFoldersEnabled() {
		return curriculumElementFoldersEnabled;
	}

	public void setCurriculumElementFoldersEnabled(boolean enabled) {
		this.curriculumElementFoldersEnabled = enabled;
		setStringProperty(CURRICULUM_ELEMENTS_FOLDERS_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isEnableLearnersBookmarksCourse() {
		return enableLearnersBookmarksCourse;
	}

	public void setEnableLearnersBookmarksCourse(boolean enabled) {
		this.enableLearnersBookmarksCourse = enabled;
		setStringProperty(LEARNERS_BOOKMARKS_COURSE, enabled ? "true" : "false", true);
	}

	public boolean isEnableLearnersParticipatingCourses() {
		return enableLearnersParticipatingCourses;
	}

	public void setEnableLearnersParticipatingCourses(boolean enabled) {
		this.enableLearnersParticipatingCourses = enabled;
		setStringProperty(LEARNERS_PARTICIPATING_COURSES, enabled ? "true" : "false", true);
	}
	
	public boolean isPrependCourseReferenceToTitle() {
		return prependCourseReferenceToTitle;
	}
	
	public void setPrependCourseReferenceToTitle(boolean enabled) {
		this.prependCourseReferenceToTitle = enabled;
		setStringProperty(PREPEND_COURSE_REFERENCE_TO_TITLE, enabled ? "true" : "false", true);
	}

	public String[] getBasicAuthenticationExclusionList() {
		if(StringHelper.containsNonWhitespace(basicAuthenticationExclusionList)) {
			return basicAuthenticationExclusionList.split("[,]");
		}
		return new String[0];
	}

	public void setBasicAuthenticationExclusionList(String userAgents) {
		this.basicAuthenticationExclusionList = userAgents;
	}

	public String[] getUserAgentExclusionListArray() {
		if(StringHelper.containsNonWhitespace(userAgentExclusionList)) {
			return userAgentExclusionList.split("[,]");
		}
		return new String[0];
	}

	public String getUserAgentExclusionList() {
		return userAgentExclusionList;
	}

	public void setUserAgentExclusionList(String userAgents) {
		this.userAgentExclusionList = userAgents;
		if(StringHelper.containsNonWhitespace(userAgents)) {
			setStringProperty(USER_AGENT_EXCLUSION_LIST, userAgents, true);
		} else {
			setStringProperty(USER_AGENT_EXCLUSION_LIST, "oo_empty_oo", true);
		}
	}

	/**
	 * Return an unmodifiable map
	 * @return
	 */
	public Map<String, WebDAVProvider> getWebDAVProviders() {
		Map<String,WebDAVProvider> providerMap = new HashMap<>();
		if(webdavProviders != null) {
			for(WebDAVProvider webdavProvider:webdavProviders) {
				providerMap.put(webdavProvider.getMountPoint(), webdavProvider);
			}
		}
		return providerMap; 
	}
}