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
package org.olat.home;

import org.olat.core.commons.chiefcontrollers.BaseChiefController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.navigation.DefaultNavElement;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.StateSite;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * <h3>Description:</h3> Instantiates the genericmainctrl for a minimal home
 * site. Initial Date: 10.05.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class HomeSite implements SiteInstance {

	private DefaultNavElement curNavElem;
	private NavElement origNavElem;

	public HomeSite(UserRequest ureq) {
		Translator trans = Util.createPackageTranslator(BaseChiefController.class, ureq.getLocale());
		
		if(ureq.getUserSession().getRoles().isGuestOnly()){
			origNavElem = new DefaultNavElement(trans.translate("topnav.guesthome"), trans.translate("topnav.guesthome.alt"), "o_site_home");
		}else{
			origNavElem = new DefaultNavElement(trans.translate("topnav.home"), trans.translate("topnav.home.alt"), "o_site_home");
		}
		curNavElem = new DefaultNavElement(origNavElem);
	}

	/**
	 * @see org.olat.core.gui.control.navigation.SiteInstance#createController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public MainLayoutController createController(UserRequest ureq, WindowControl wControl) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(HomeSite.class, ureq.getIdentity().getKey());
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ureq, ores, new StateSite(this), wControl, true);
		MainLayoutController c = new HomeMainController(ureq, bwControl);
		return c;
	}

	/**
	 * @see org.olat.core.gui.control.navigation.SiteInstance#getNavElement()
	 */
	@Override
	public NavElement getNavElement() {
		return curNavElem;
	}

	/**
	 * @see org.olat.core.gui.control.navigation.SiteInstance#isKeepState()
	 */
	@Override
	public boolean isKeepState() {
		return true;
	}

	/**
	 * @see org.olat.core.gui.control.navigation.SiteInstance#reset()
	 */
	@Override
	public void reset() {
		curNavElem = new DefaultNavElement(origNavElem);
	}

}
