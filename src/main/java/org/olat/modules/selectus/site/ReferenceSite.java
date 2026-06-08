/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.site;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.navigation.DefaultNavElement;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

import org.olat.modules.selectus.ui.ApplyToApplicationMainController;
import org.olat.modules.selectus.ui.reference.ReferenceMainController;

/**
 * 
 * Initial date: 16.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferenceSite implements SiteInstance {
	
	private DefaultNavElement curNavElem;
	private NavElement origNavElem;
	
	public ReferenceSite(Locale locale) {
		Translator trans = Util.createPackageTranslator(ApplyToApplicationMainController.class, locale);
		origNavElem = new DefaultNavElement(null, trans.translate("topnav.evaluation"), trans.translate("topnav.evaluation.alt"), "o_site_evaluation");
		curNavElem = new DefaultNavElement(origNavElem);
	}

	@Override
	public NavElement getNavElement() {
		return curNavElem;
	}

	@Override
	public MainLayoutController createController(UserRequest ureq, WindowControl wControl) {
		return new ReferenceMainController(ureq, wControl);
	}

	@Override
	public boolean isKeepState() {
		return false;
	}

	@Override
	public void reset() {
		curNavElem = new DefaultNavElement(origNavElem);
	}
}