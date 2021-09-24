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
package org.olat.core.gui.control.navigation;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.AutoCreator;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.site.ui.ForbiddenCourseSiteController;

/**
 * The standard behavior for the site
 * 
 * 
 * Initial date: 19.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractSiteInstance implements SiteInstance {
	
	private static final Logger log = Tracing.createLoggerFor(AbstractSiteDefinition.class);
	private final SiteDefinition siteDef;
	
	public AbstractSiteInstance(SiteDefinition siteDef) {
		this.siteDef = siteDef;
	}

	@Override
	public final Controller createController(UserRequest ureq, WindowControl wControl) {
		SiteDefinitions siteDefinitions = CoreSpringFactory.getImpl(SiteDefinitions.class);
		SiteConfiguration config = siteDefinitions.getConfigurationSite(siteDef);
		if(config != null && StringHelper.containsNonWhitespace(config.getSecurityCallbackBeanId())) {
			String secCallbackBeanId = config.getSecurityCallbackBeanId();
			Object siteSecCallback = getSiteSecurityCallback(secCallbackBeanId);
			if (siteSecCallback instanceof SiteSecurityCallback
					&& !((SiteSecurityCallback)siteSecCallback).isAllowedToLaunchSite(ureq)) {
				return getAlternativeController(ureq, wControl, config);
			}
		}
		return createController(ureq, wControl, config);
	}
	
	private Object getSiteSecurityCallback(String secCallbackBeanId) {
		try {
			return CoreSpringFactory.getBean(secCallbackBeanId);
		} catch (Exception e) {
			log.error("Cannot find security callback: {} return administrator only security callback", secCallbackBeanId);
			return CoreSpringFactory.getBean("adminSiteSecurityCallback");
		}
	}
	
	protected abstract Controller createController(UserRequest ureq, WindowControl wControl, SiteConfiguration config);
	
	protected MainLayoutController getAlternativeController(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		String altControllerId = config.getAlternativeControllerBeanId();

		MainLayoutController c;
		if (StringHelper.containsNonWhitespace(altControllerId)) {
			AutoCreator creator = (AutoCreator)CoreSpringFactory.getBean(altControllerId);
			Controller ac = creator.createController(ureq, wControl);
			if(ac instanceof MainLayoutController) {
				c = (MainLayoutController)ac;
			} else {
				c = new LayoutMain3ColsController(ureq, wControl, ac);
			}
		} else {
			Controller ctrl = new ForbiddenCourseSiteController(ureq, wControl);			
			c = new LayoutMain3ColsController(ureq, wControl, ctrl);
		}
		return c;
	}

	public boolean isKeepState() {
		return true;
	}

	@Override
	public void reset() {
		//
	}
}
