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
 * 13.09.2012 by frentix GmbH, http://www.frentix.com
 * <p>
 **/


package org.olat.social.shareLink;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings.CalloutOrientation;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.lightbox.LightboxController;
import org.olat.core.helpers.Settings;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.HistoryPoint;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.social.SocialModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <h3>Description:</h3>
 * <p>
 * This controller displays a "Share" link that opens a callout with the
 * business path link to the current page. The callout offers copying the
 * link, showing a QR code and, if configured, a few social networks.
 * Besides that, a separate link lets the user set the current page as the
 * personal landing page.
 * <p>
 * The list of the social networks can be configured in the SocialModule and
 * the olat.properties.
 * <p>
 * <h3>Events thrown by this controller:</h3>
 * <p>
 * none
 * <p>
 * Initial Date: 13.09.2012 <br>
 *
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class ShareLinkController extends BasicController {

	private final VelocityContainer mainVC;
	private final Link shareLink;
	private final Link landingPageLink;

	private ShareLinkListController shareListCtrl;
	private CloseableCalloutWindowController shareCalloutCtrl;
	private ShareQrCodeController qrCodeCtrl;
	private LightboxController qrLightboxCtrl;

	private String shareUrl;
	private String shareTitle;

	@Autowired
	private SocialModule socialModule;

	/**
	 * @param ureq
	 * @param wControl
	 * @param iconOnly true: the Share link shows the icon only; false: the icon
	 *            and its translated label are shown
	 * @param withLandingPageLink true: show the "Set as landing page" link next
	 *            to Share; false: hide it. The link is shown only if the
	 *            condition is true and the user is authenticated and not a
	 *            guest.
	 */
	public ShareLinkController(UserRequest ureq, WindowControl wControl, boolean iconOnly, boolean withLandingPageLink) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("shareLink");

		int presentation = iconOnly ? Link.LINK : Link.BUTTON;

		shareLink = LinkFactory.createCustomLink("share", "share", "share.social", presentation, mainVC, this);
		shareLink.setIconLeftCSS("o_icon o_icon_share o_icon-lg");
		if (iconOnly) {
			shareLink.setCustomDisplayText("");
		} else {
			shareLink.setElementCssClass("o_button_ghost");
		}
		shareLink.setTitle("share.social");
		shareLink.setAriaDialogOpener();

		UserSession usess = ureq.getUserSession();
		boolean isUser = usess.isAuthenticated() && !usess.getRoles().isGuestOnly();
		if (withLandingPageLink && isUser) {
			landingPageLink = LinkFactory.createCustomLink("landingpage", "setLandingPage", "landingpage.set.current", presentation, mainVC, this);
			landingPageLink.setIconLeftCSS("o_icon o_icon_landingpage o_icon-lg");
			if (iconOnly) {
				landingPageLink.setCustomDisplayText("");
			} else {
				landingPageLink.setElementCssClass("o_button_ghost");
			}
			landingPageLink.setTitle("landingpage.set.current");
		} else {
			landingPageLink = null;
		}

		putInitialPanel(mainVC);
	}

	/**
	 * Overrides the shared URL, which otherwise defaults to the URL of the
	 * current view (the user's last history point).
	 *
	 * @param shareUrl the URL to share, or null to restore the default
	 */
	public void setShareUrl(String shareUrl) {
		this.shareUrl = shareUrl;
	}

	/**
	 * Overrides the shared title, which otherwise defaults to the window title.
	 *
	 * @param shareTitle the title to share, or null to restore the default
	 */
	public void setShareTitle(String shareTitle) {
		this.shareTitle = shareTitle;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == shareLink) {
			doOpenShareCallout(ureq);
		} else if (source == landingPageLink) {
			doSetLandingPage(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == shareListCtrl) {
			if (ShareLinkListController.QR_EVENT == event) {
				String url = getShareUrl(ureq);
				shareCalloutCtrl.deactivate();
				cleanUpCallout();
				doOpenQrLightbox(ureq, url);
			}
		} else if (source == shareCalloutCtrl) {
			if (CloseableCalloutWindowController.CLOSE_WINDOW_EVENT == event) {
				cleanUpCallout();
			}
		} else if (source == qrLightboxCtrl) {
			if (Event.CLOSE_EVENT == event) {
				cleanUpLightbox();
			}
		}
	}

	private void doOpenShareCallout(UserRequest ureq) {
		cleanUpCallout();

		String url = getShareUrl(ureq);
		String title = getShareTitle();
		shareListCtrl = new ShareLinkListController(ureq, getWindowControl(), socialModule.getEnabledShareLinkButtons(), url, title);
		listenTo(shareListCtrl);

		CalloutSettings settings = new CalloutSettings(true, CalloutOrientation.bottomOrTop, false, null);
		// set explicitly, the fallback in CloseableCalloutWindowController would use the untranslated i18n key
		settings.setAriaLabel(translate("share.social"));
		shareCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				shareListCtrl.getInitialComponent(), shareLink, "", true, "", settings);
		listenTo(shareCalloutCtrl);
		shareCalloutCtrl.activate();
	}

	private void doOpenQrLightbox(UserRequest ureq, String url) {
		qrCodeCtrl = new ShareQrCodeController(ureq, getWindowControl(), url);
		listenTo(qrCodeCtrl);
		qrLightboxCtrl = new LightboxController(ureq, getWindowControl(), qrCodeCtrl);
		listenTo(qrLightboxCtrl);
		qrLightboxCtrl.activate();
	}

	private void doSetLandingPage(UserRequest ureq) {
		UserSession usess = ureq.getUserSession();
		if (usess == null || !usess.isAuthenticated()) return;

		HistoryPoint p = usess.getLastHistoryPoint();
		if (p != null && StringHelper.containsNonWhitespace(p.getBusinessPath())) {
			List<ContextEntry> ces = p.getEntries();
			String landingPage = BusinessControlFactory.getInstance().getAsURIString(ces, true);
			int start = landingPage.indexOf("/url/");
			if (start != -1) {
				// start with / after /url
				landingPage = landingPage.substring(start + 4);
			}
			// update user prefs
			usess.getGuiPreferences().putAndSave(WindowManager.class, "landing-page", landingPage);
			showInfo("landingpage.set.message");
		}
	}

	private String getShareUrl(UserRequest ureq) {
		if (StringHelper.containsNonWhitespace(shareUrl)) {
			return shareUrl;
		}
		HistoryPoint p = ureq.getUserSession().getLastHistoryPoint();
		if (p != null && StringHelper.containsNonWhitespace(p.getBusinessPath())) {
			return BusinessControlFactory.getInstance().getAsURIString(p.getEntries(), true);
		}
		return Settings.getServerContextPathURI();
	}

	private String getShareTitle() {
		if (StringHelper.containsNonWhitespace(shareTitle)) {
			return shareTitle;
		}
		String title = getWindowControl().getWindowBackOffice().getWindow().getTitle().getValue();
		return StringHelper.containsNonWhitespace(title) ? title : Settings.getApplicationName();
	}

	private void cleanUpCallout() {
		removeAsListenerAndDispose(shareCalloutCtrl);
		removeAsListenerAndDispose(shareListCtrl);
		shareCalloutCtrl = null;
		shareListCtrl = null;
	}

	private void cleanUpLightbox() {
		removeAsListenerAndDispose(qrLightboxCtrl);
		removeAsListenerAndDispose(qrCodeCtrl);
		qrLightboxCtrl = null;
		qrCodeCtrl = null;
	}

	@Override
	protected void doDispose() {
		cleanUpCallout();
		cleanUpLightbox();
		super.doDispose();
	}
}
