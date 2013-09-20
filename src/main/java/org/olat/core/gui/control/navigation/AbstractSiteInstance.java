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

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.AutoCreator;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.generic.messages.MessageController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.site.CourseSite;

/**
 * The standard behavior for the site
 * 
 * 
 * Initial date: 19.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractSiteInstance implements SiteInstance {
	
	private final SiteDefinition siteDef;
	
	public AbstractSiteInstance(SiteDefinition siteDef) {
		this.siteDef = siteDef;
	}
	
	@Override
	public abstract NavElement getNavElement();

	@Override
	public final MainLayoutController createController(UserRequest ureq, WindowControl wControl) {
		SiteDefinitions siteDefinitions = CoreSpringFactory.getImpl(SiteDefinitions.class);
		SiteConfiguration config = siteDefinitions.getConfigurationSite(siteDef);
		if(StringHelper.containsNonWhitespace(config.getSecurityCallbackBeanId())) {
			String secCallbackBeanId = config.getSecurityCallbackBeanId();
			SiteSecurityCallback siteSecCallback = (SiteSecurityCallback)CoreSpringFactory.getBean(secCallbackBeanId);
			if (siteSecCallback != null && !siteSecCallback.isAllowedToLaunchSite(ureq)) {
				return getAlternativeController(ureq, wControl, config);
			}
		}
		return createController(ureq, wControl, config);
	}
	
	protected abstract MainLayoutController createController(UserRequest ureq, WindowControl wControl, SiteConfiguration config);
	
	protected MainLayoutController getAlternativeController(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		String altControllerId = config.getAlternativeControllerBeanId();

		MainLayoutController c;
		if (StringHelper.containsNonWhitespace(altControllerId)) {
			AutoCreator creator = (AutoCreator)CoreSpringFactory.getBean(altControllerId);
			c = (MainLayoutController)creator.createController(ureq, wControl);
		} else {
			Translator pT = Util.createPackageTranslator(CourseSite.class, ureq.getLocale());
			MessageController ctrl = MessageUIFactory.createErrorMessage(ureq, wControl,
					pT.translate("course.site.no.access.title"), pT.translate("course.site.no.access.text"));			
			c = new LayoutMain3ColsController(ureq, wControl, null, null, ctrl.getInitialComponent(), null);
		}
		return c;
	}
	

	@Override
	public boolean isKeepState() {
		return true;
	}

	@Override
	public void reset() {
		//
	}
}
