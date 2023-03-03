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
package org.olat.modules.project.site;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.AbstractSiteInstance;
import org.olat.core.gui.control.navigation.DefaultNavElement;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.StateSite;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.modules.project.ui.ProjectUIFactory;
import org.olat.modules.project.ui.ProjectsSiteController;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * Initial date: 21 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 */
public class ProjectsSite extends AbstractSiteInstance {
	
	private static final OLATResourceable ores = OresHelper.createOLATResourceableType(ProjectBCFactory.TYPE_PROJECTS);
	private static final String catalogBusinessPath = OresHelper.toBusinessPath(ores);
	
	private NavElement origNavElem;
	private NavElement curNavElem;

	public ProjectsSite(SiteDefinition siteDef, Locale loc) {
		super(siteDef);
		Translator trans = Util.createPackageTranslator(ProjectUIFactory.class, loc);
		origNavElem = new DefaultNavElement(catalogBusinessPath, trans.translate("topnav.project"),
				trans.translate("topnav.project.alt"), "o_site_project");
		origNavElem.setAccessKey("p".charAt(0));
		curNavElem = new DefaultNavElement(origNavElem);
	}

	@Override
	public NavElement getNavElement() {
		return curNavElem;
	}

	@Override
	protected Controller createController(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ureq, ores, new StateSite(this), wControl, true);
		return new ProjectsSiteController(ureq, bwControl);
	}

	@Override
	public void reset() {
		curNavElem = new DefaultNavElement(origNavElem);
	}
}
