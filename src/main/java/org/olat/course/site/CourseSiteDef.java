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
package org.olat.course.site;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.AbstractSiteDefinition;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteDefinitions;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.gui.control.navigation.SiteSecurityCallback;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.course.site.model.CourseSiteConfiguration;
import org.olat.course.site.model.LanguageConfiguration;

/**
 * 
 * Description:<br>
 * Receives settings by Spring and creates a site depending on config.
 * 
 * <P>
 * Initial Date:  12.07.2005 <br>
 *
 * @author Felix Jost
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class CourseSiteDef extends AbstractSiteDefinition implements SiteDefinition {

	public CourseSiteDef() {
		//
	}
	
	public String getRepositorySoftKey(UserRequest ureq) {
		CourseSiteConfiguration config = getCourseSiteconfiguration();
		if(config != null) {
			LanguageConfiguration langConfig = getLanguageConfiguration(ureq, config);
			if(langConfig != null) {
				return langConfig.getRepoSoftKey();
			}
		}
		return null;
	}
	
	@Override
	public boolean isEnabled() {
		SiteConfiguration config = getSiteConfiguration();
		return config != null && config.isEnabled();
	}

	@Override
	public SiteInstance createSite(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		if(StringHelper.containsNonWhitespace(config.getSecurityCallbackBeanId())) {
			return createSite(ureq, getCourseSiteconfiguration(), config);
		} else if(!ureq.getUserSession().getRoles().isInvitee()) {
			// only for registered users and guests
			return createSite(ureq, getCourseSiteconfiguration(), config);
		}
		return null;
	}
	
	protected CourseSiteConfiguration getCourseSiteconfiguration() {
		SiteDefinitions siteModule = CoreSpringFactory.getImpl(SiteDefinitions.class);
		return siteModule.getConfigurationCourseSite1();
	}
	
	protected SiteInstance createSite(UserRequest ureq, CourseSiteConfiguration courseConfig, SiteConfiguration config) {
		if(courseConfig == null) return null;
		
		String secCallbackBeanId = config.getSecurityCallbackBeanId();
		SiteSecurityCallback siteSecCallback = (SiteSecurityCallback)CoreSpringFactory.getBean(secCallbackBeanId);
		
		UserSession usess = ureq.getUserSession();
		if(usess == null || usess.getRoles() == null) return null;
		
		Roles roles = usess.getRoles();

		boolean canSeeToolController = roles.isAuthor()//TODO roles repo
				|| roles.isAdministrator()
				|| roles.isLearnResourceManager();
		boolean showToolController = true;
		if (!canSeeToolController && !courseConfig.isToolbar()) {
			showToolController = false;
		}
		
		LanguageConfiguration langConfig = getLanguageConfiguration(ureq, courseConfig);
		if(langConfig == null) {
			return null;
		}
		String icon = courseConfig.getNavIconCssClass();
		return createCourseSiteInstance(langConfig, showToolController, siteSecCallback, icon);
	}
	
	/**
	 * Warning: there is no check against null.
	 * It's only to return the right type, CourseSie or CourseSite2.
	 */
	protected CourseSite createCourseSiteInstance(LanguageConfiguration langConfig,
			boolean showToolController, SiteSecurityCallback siteSecCallback, String icon) {
		return new CourseSite(this, langConfig.getRepoSoftKey(), showToolController,
				siteSecCallback, langConfig.getTitle(), icon);
	}
	
	protected LanguageConfiguration getLanguageConfiguration(UserRequest ureq, CourseSiteConfiguration config) {
		if(config == null || config.getConfigurations() == null) return null;
		String language = ureq.getUserSession().getLocale().getLanguage();
		
		LanguageConfiguration myLangConfig = null;
		LanguageConfiguration defaultLangConfig = null;
		for(LanguageConfiguration langConfig:config.getConfigurations()) {
			if(langConfig.isDefaultConfiguration()) {
				defaultLangConfig = langConfig;
			}
			if(language.equals(langConfig.getLanguage())) {
				myLangConfig = langConfig;
			}
		}
		
		if(myLangConfig == null) {
			myLangConfig = defaultLangConfig;
		}
		
		if(myLangConfig == null && !config.getConfigurations().isEmpty()) {
			myLangConfig = config.getConfigurations().get(0);
		}
		
		return myLangConfig;
	}
}