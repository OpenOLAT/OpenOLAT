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
package org.olat.modules.catalog.site;

import java.util.Locale;

import org.olat.core.commons.chiefcontrollers.BaseChiefController;
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
import org.olat.modules.catalog.ui.CatalogSiteMainController;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * Initial date: 16.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CatalogSite extends AbstractSiteInstance {
	
	private static final OLATResourceable catalogOres = OresHelper.createOLATResourceableInstance("Catalog", 0l);
	private static final String catalogBusinessPath = OresHelper.toBusinessPath(catalogOres);
	
	private NavElement origNavElem;
	private NavElement curNavElem;

	public CatalogSite(SiteDefinition siteDef, Locale loc) {
		super(siteDef);
		Translator trans = Util.createPackageTranslator(BaseChiefController.class, loc);
		origNavElem = new DefaultNavElement(catalogBusinessPath, trans.translate("topnav.catalog"),
				trans.translate("topnav.catalog.alt"), "o_site_catalog");
		origNavElem.setAccessKey("c".charAt(0));
		curNavElem = new DefaultNavElement(origNavElem);
	}

	@Override
	public NavElement getNavElement() {
		return curNavElem;
	}

	@Override
	protected Controller createController(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		// for existing controller which are part of the main olat -> use the controllerfactory
		
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(catalogOres));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ureq, catalogOres, new StateSite(this), wControl, true);
		return new CatalogSiteMainController(ureq, bwControl);
	}

	@Override
	public boolean isKeepState() {
		return true;
	}

	@Override
	public void reset() {
		curNavElem = new DefaultNavElement(origNavElem);
	}
}
