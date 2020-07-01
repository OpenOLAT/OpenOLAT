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
package org.olat.modules.library.site;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.navigation.AbstractSiteInstance;
import org.olat.core.gui.control.navigation.DefaultNavElement;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.StateSite;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.library.ui.LibraryMainController;

/**
 * This site represents the library (Bibliothek) tab.
 * 
 * <P>
 * Initial Date: Jun 16, 2009 <br>
 * 
 * @author gwassmann
 */
public class LibrarySite extends AbstractSiteInstance {
	
	private static final OLATResourceable libraryOres = OresHelper.createOLATResourceableInstance(LibrarySite.class, 0l);
	private static final String libraryBusinessPath = OresHelper.toBusinessPath(libraryOres);

	private NavElement origNavElem;
	private NavElement curNavElem;

	/**
	 * @param loc
	 */
	public LibrarySite(SiteDefinition siteDef, Locale loc) {
		super(siteDef);
		Translator trans = Util.createPackageTranslator(LibraryMainController.class, loc);
		origNavElem = new DefaultNavElement(libraryBusinessPath, trans.translate("site.title"),
				trans.translate("site.title.alt"), "f_site_library");
		curNavElem = new DefaultNavElement(origNavElem);
	}

	@Override
	protected MainLayoutController createController(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ureq, libraryOres, new StateSite(this), wControl, true);
		return new LibraryMainController(ureq, bwControl);
	}

	@Override
	public NavElement getNavElement() {
		return curNavElem;
	}

	@Override
	public void reset() {
		curNavElem = new DefaultNavElement(origNavElem);
	}
}