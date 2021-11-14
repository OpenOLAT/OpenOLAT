/**
 * <a href=“http://www.openolat.org“>
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
 * 31.08.2015 by frentix GmbH, http://www.frentix.com
 * <p>
 **/

package org.olat.login;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;

/**
 * <h3>Description:</h3>
 * <p>
 * The about displays some info about the product. Can be used as popup window
 * using the activate methods or as normal controller
 * <p>
 * Initial Date: 31.08.2015 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class AboutController extends BasicController {
	private CloseableModalController cmc;
	private Link closeLink;

	public AboutController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		VelocityContainer aboutVC = createVelocityContainer("about");
		// add license text
		String licenses = "Not found";
		try(InputStream licensesStream = AboutController.class.getResourceAsStream("../../../NOTICE.TXT")) {		    
			licenses = IOUtils.toString(licensesStream, "UTF-8");
		} catch (IOException e) {
			logError("Error while reading NOTICE.TXT", e);
		}
		aboutVC.contextPut("licenses", licenses);
		// close link after about text
		closeLink = LinkFactory.createButton("close", aboutVC, this);
		closeLink.setPrimary(true);

		putInitialPanel(aboutVC);
	}

	/**
	 * Open a modal dialog which can be closed by user. 
	 */
	public void activateAsModalDialog() {
		if(cmc == null) {
			cmc = new CloseableModalController(getWindowControl(), "close", getInitialComponent());
			listenTo(cmc);
			cmc.activate();
		}
	}

	/**
	 * Manually close modal dialog. Normally you don't have to call this method.
	 */
	public void deactivateModalDialog() {
		if (cmc != null) {
			cmc.deactivate();
			removeAsListenerAndDispose(cmc);
			cmc = null;
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == closeLink) {
			deactivateModalDialog();
			fireEvent(ureq, Event.CLOSE_EVENT);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(cmc == source) {
			fireEvent(ureq, Event.CLOSE_EVENT);
		}
	}

	/**
	 * Factory method to create a link for the about menu. The reason for having
	 * this here is so that we can simply keep the corresponding i18n keys in
	 * one place
	 * 
	 * @param locale
	 * @param listener
	 * @param withIcon true: use oo icon on left side (for use in menu); false no icon
	 * @param withBuildInfo true: add build info to hover title (if available); false no build info
	 * @return
	 */
	public static final Link aboutLinkFactory(String id, Locale locale, Controller listener, boolean withIcon, boolean withBuildInfo) {
		Translator aboutTrans = Util.createPackageTranslator(AboutController.class, locale);
		Link aboutLink = LinkFactory
				.createLink(id, "menu.about", "about", "menu.about", aboutTrans, null, listener, Link.LINK + Link.NONTRANSLATED);
		aboutLink.setCustomDisplayText(aboutTrans.translate("menu.about"));
		if (withIcon) {			
			aboutLink.setIconLeftCSS("o_icon o_icon_openolat o_icon-fw");
		}
		if (withBuildInfo) {
			String title = aboutTrans.translate("menu.about.alt");
			String rev = WebappHelper.getRevisionNumber();
			if (rev != null) {
				String change = WebappHelper.getChangeSet();
				aboutLink.setTitle(title + " Build (" + rev + ":" + change+ ")");								
			} else {
				aboutLink.setTitle(title);				
			}			
		}
		return aboutLink;
	}
}
