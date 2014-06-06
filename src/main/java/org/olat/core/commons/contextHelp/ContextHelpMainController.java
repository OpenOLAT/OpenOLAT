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

package org.olat.core.commons.contextHelp;

import java.util.Locale;

import org.apache.commons.lang.ArrayUtils;
import org.olat.core.commons.chiefcontrollers.LanguageChangedEvent;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.MainPanel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.breadcrumb.BreadCrumbController;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.OlatLoggingAction;
import org.olat.core.logging.activity.StringResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;

/**
 * <h3>Description:</h3> This controller serves as the main entry point of the
 * context help display controller. The workflow is intended to run in a
 * separate window.
 * <p>
 * The controller can be triggered in two modes:
 * <ul>
 * <li>/help/de/org.olat.core/mychelp_page.html : the new, preferred style</li>
 * <li>/help/de/mychelp_page.html : legacy mode to support old external links.
 * Don't use this when creating new pages!</li>
 * </li> Those are only the entry URL's. As soon as the window is build up,
 * everything works via the framework.
 * <p>
 * To be delievered context help files must be in a _chelp directory in the
 * classpath. Links to the page are made using the velocity helper method
 * $r.contextHelp("org.olat.core", "mychelp_page.html", "help to mypage")
 * <p>
 * Each context help page must provide a translated title in the same package as
 * the page is located using the syntax 'chelp.PAGENAME.title': Example: <br>
 * <code>page: my.package._chelp.my_help.html </code><br>
 * <code>chelp.my_help.title translated in my.package._i18n</code>
 * <p>
 * Context help files can reference static resources, e.g. images. Those images
 * must be in the _static directory within the _chelp directory:<br>
 * <code>resource: my.package._chelp._static.myimage_de.png</code> <br>
 * The resource must be in the static directory for each locale. A fallback
 * mechanism checks if the resource exists and uses the fallback image when no
 * image is found. <br>
 * In the html template the resource must be addressed without the locale, e.g:
 * <code>&lt;img src='chelpStaticDirUrl/myimage.png' &gt;</code> <br>
 * This will load the resource from my.package._chelp._static.myimage_de.png
 * <p>
 * The controller listens to LanguageChangedEvent and changes the locale for is
 * children when such an event occures (fired by ContextHelpTopNavController)
 * <p>
 * The workflow features a table of contents and a page details view. The
 * navigation is implemented using a bread crumb controller. Sub-pages are also
 * supported. Links to those sub-pages must be made with<br>
 * <code>$r.contextHelpRelativeLink("my-pagename.html")</code>
 * 
 * <h3>Events thrown by this controller:</h3>
 * <ul>
 * <li>none</li>
 * </ul>
 * <p>
 * Initial Date: 04.11.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

class ContextHelpMainController extends MainLayoutBasicController implements GenericEventListener {
	private BreadCrumbController breadCrumbLayoutCtr;
	private ContextHelpTOCCrumbController startCtr;
	private EventBus eventBus;
	
	/**
	 * Constructor for the context help display. 
	 * @param ureq
	 * @param control
	 */
	public ContextHelpMainController(UserRequest ureq, WindowControl control) {
		super(ureq, control);
		String[] uriParts = ureq.getNonParsedUri().split("/");
		String lang = null;
		// get lang and set locale for help page accordingly
		lang = uriParts[0];
		Locale newLocale = I18nManager.getInstance().getLocaleOrNull(lang);
		if ( newLocale == null || !I18nModule.getEnabledLanguageKeys().contains(newLocale.toString())) {
			newLocale = I18nModule.getDefaultLocale();
		}
		if (! getLocale().toString().equals(newLocale.toString())) {
			setLocale(newLocale, true);
		}
		// Create bread crumb navigation
		breadCrumbLayoutCtr = new BreadCrumbController(ureq, control);
		listenTo(breadCrumbLayoutCtr);
		// Add translation tool start controller to bread crumb
		startCtr = new ContextHelpTOCCrumbController(ureq, control, newLocale);
		listenTo(startCtr);
		breadCrumbLayoutCtr.activateFirstCrumbController(startCtr);

		MainPanel mainPanel = new MainPanel("");
		mainPanel.pushContent(breadCrumbLayoutCtr.getInitialComponent());
		putInitialPanel(mainPanel);

		activatePageFromURL(uriParts, ureq, startCtr);
		
		// Register for events on this user session. Identity is set to null, but
		// event bus is still user-session only
		eventBus = ureq.getUserSession().getSingleUserEventCenter();
		eventBus.registerFor(this, ureq.getIdentity(), ContextHelpTopNavController.CHANGE_LANG_RESOURCE);
		
		// do logging
		if (ureq.getUserSession().getSessionInfo()!=null) {
			// context help can be called in dmz zone as well - in that case don't call log() - only call log() for logged-in users
			ThreadLocalUserActivityLogger.log(OlatLoggingAction.CS_HELP, getClass(), CoreLoggingResourceable.wrapNonOlatResource(StringResourceableType.csHelp, "-1", ArrayUtils.toString(uriParts)));
		}
	}

	/**
	 * Internal helper to parse the URL for an entry point to a specific help page
	 * @param uriParts
	 * @param ureq
	 * @param activateableController
	 */
	private void activatePageFromURL(String[] uriParts, UserRequest ureq, ContextHelpTOCCrumbController activateableController) {
		// Initialize active page
		// add context help page
		// Try to extract requested help page from URI
		String page = null, bundleName = null;
		if (uriParts.length == 2) {
			// 1) Legacy context help URL : lookup from map and if not found
			// redirect to static dispatcher to load old static help file
			page = uriParts[1];
			bundleName = ContextHelpModule.getContextHelpPagesLegacyLookupIndex().get(page);
			if (bundleName == null) {
				// Help page not found, try it with static legacy help files
				String legacyHelpUrl = StaticMediaDispatcher.createStaticURIFor("help/" + ureq.getNonParsedUri());
				DispatcherModule.redirectTo(ureq.getHttpResp(), legacyHelpUrl);
				return;
			}
			
		} else if (uriParts.length == 3) {
			// 2) Normal case: get page bundle and name
			bundleName = uriParts[1];
			page = uriParts[2];
		}

		if (page != null || bundleName != null) {
			activateableController.activatePage(ureq, bundleName, page);
		}

	}
	
	/**
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	public void event(Event event) {
		if (event instanceof LanguageChangedEvent) {
			LanguageChangedEvent langEvent = (LanguageChangedEvent) event;
			Locale newLocale = langEvent.getNewLocale();
			setLocale(newLocale, true);
			// Set new locale on bread crumb chain
			startCtr.setLocale(newLocale, langEvent.getCurrentUreq());
			// Reset all text lables on bread crumb
			breadCrumbLayoutCtr.resetCrumbTexts();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// no events to catch
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// Controllers autodisposed by BasicController		
		eventBus.deregisterFor(this, ContextHelpTopNavController.CHANGE_LANG_RESOURCE);
		eventBus = null;
	}
}
