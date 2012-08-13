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
*/
package org.olat.core.commons.contextHelp;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.breadcrumb.CrumbBasicController;
import org.olat.core.gui.translator.PackageTranslator;

/**
 * Description:<br>
 * The table of contents crumb controller shows the list of available context
 * help pages. The pages are rendered as links. If clicked, they will open up as
 * a new crumb on the bread crumb path
 * <p>
 * The controller can be activated with a page identifyer, e.g. with
 * <code>"org.olat.core:my-helppage.html"</code>
 * 
 * <P>
 * Initial Date: 04.11.2008 <br>
 * 
 * @author gnaegi
 */
class ContextHelpTOCCrumbController extends CrumbBasicController {	
	private VelocityContainer tocVC;
	private List<String> pageIdentifyers;
	private List<Link> pageLinks;

	/**
	 * Constructor to create a new context help table of contents controller
	 * 
	 * @param ureq
	 * @param control
	 * @param displayLocale
	 */
	protected ContextHelpTOCCrumbController(UserRequest ureq, WindowControl control, Locale displayLocale) {
		super(ureq, control);
		setLocale(displayLocale, true);
		tocVC = createVelocityContainer("contexthelptoc");

		pageIdentifyers = new ArrayList<String>();
		pageLinks = new ArrayList<Link>();
		pageIdentifyers.addAll(ContextHelpModule.getAllContextHelpPages());
		
		sortToc();
		
		tocVC.contextPut("pageIdentifyers", pageIdentifyers);
		
		putInitialPanel(tocVC);
	}

	/**
	 * @see org.olat.core.gui.control.generic.breadcrumb.CrumbBasicController#getCrumbLinkHooverText()
	 */
	@Override
	public String getCrumbLinkHooverText() {
		return translate("contexthelp.crumb.toc.hover");
	}

	/**
	 * @see org.olat.core.gui.control.generic.breadcrumb.CrumbBasicController#getCrumbLinkText()
	 */
	@Override
	public String getCrumbLinkText() {
		return translate("contexthelp.crumb.toc");
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		tocVC = null;
		pageIdentifyers = null;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			Link link = (Link) source;
			// Get requested page from link
			String pageIdentifyer = (String) link.getUserObject();
			activatePage(ureq, pageIdentifyer);
		}
	}
	
	/**
	 * Change the locale for this view
	 * @param locale
	 * @param ureq
	 */
	public void setLocale(Locale locale, UserRequest ureq) {
		if (tocVC == null) {
			// already disposed
			return;
		}
		// Update locale for subsequent requests
		setLocale(locale, true);
		
		sortToc();

		// Update title in view
		tocVC.setDirty(true);
		// Update next crumb in chain
		ContextHelpPageCrumbController child = (ContextHelpPageCrumbController) getChildCrumbController();
		if (child != null) {
			child.setLocale(locale, ureq);
		}	
	}
	
	private void sortToc () {
		
		Map <String,String> tmp = new HashMap<String,String>();
		Map <String,Link> links = new HashMap<String,Link>();
		List <String>titles = new ArrayList<String>();
		
		pageLinks.clear();
		
		for (int i = 0; i < pageIdentifyers.size(); i++) {
			String pageIdentifyer = pageIdentifyers.get(i);
			int splitPos = pageIdentifyer.indexOf(":");
			String bundleName = pageIdentifyer.substring(0, splitPos);
			String page = pageIdentifyer.substring(splitPos+1);
			PackageTranslator pageTans = new PackageTranslator(bundleName, getLocale());
			String pageTitle = pageTans.translate("chelp." + page.split("\\.")[0] + ".title");
			Link link = LinkFactory.createLink(i+"", tocVC, this);
			link.setCustomDisplayText(pageTitle);
			link.setUserObject(pageIdentifyer);
			pageLinks.add(link);
			tmp.put(pageTitle, pageIdentifyer);
			links.put(pageIdentifyer, link);
			titles.add(pageTitle);
		}
		
		Collator c = Collator.getInstance(getLocale());
		c.setStrength(Collator.TERTIARY);
		Collections.sort(titles, c);
		
		for (int i = 0; i < titles.size(); i++) {
			String pi = tmp.get(titles.get(i));
			tocVC.put(i+"", links.get(pi));
		}
	}

	public void activatePage(UserRequest ureq, String pageIdentifyer) {
		int splitPos = pageIdentifyer.indexOf(":");
		String bundleName = pageIdentifyer.substring(0, splitPos);
		String page = pageIdentifyer.substring(splitPos+1);
		activatePage(ureq, bundleName, page);
	}

	public void activatePage(UserRequest ureq, String bundleName, String page) {
		// Add new crumb controller now. Old one is disposed automatically
		ContextHelpPageCrumbController pageCrumController = new ContextHelpPageCrumbController(ureq, getWindowControl(), bundleName, page, getLocale());
		activateAndListenToChildCrumbController(pageCrumController);
	}

}
