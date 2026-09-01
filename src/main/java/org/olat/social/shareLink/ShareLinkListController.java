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
 * 14.08.2026 by frentix GmbH, http://www.frentix.com
 * <p>
 **/

package org.olat.social.shareLink;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.ExternalLink;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;

/**
 * <h3>Description:</h3>
 * <p>
 * Content of the Share callout: one horizontal row of icon-only links, in
 * the order configured for the share link buttons.
 * <p>
 * <h3>Events thrown by this controller:</h3>
 * <p>
 * QR_EVENT: the user clicked "Show QR code"
 * <p>
 * Initial Date: 14.08.2026 <br>
 *
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 */
public class ShareLinkListController extends BasicController {

	public static final Event QR_EVENT = new Event("qr");

	private Link qrLink;

	public ShareLinkListController(UserRequest ureq, WindowControl wControl, List<String> enabledLinks, String url, String title) {
		super(ureq, wControl);
		VelocityContainer mainVC = createVelocityContainer("shareLinkList");

		List<String> linkNames = new ArrayList<>();
		mainVC.contextPut("links", linkNames);

		for (String name : enabledLinks) {
			if ("link".equals(name)) {
				ExternalLink copyLink = LinkFactory.createExternalLink("copyLink", "copyLink", StringHelper.escapeForHtmlAttribute(url));
				copyLink.setTarget(null);
				copyLink.setIconLeftCSS("o_icon o_icon_copy o_icon-lg");
				copyLink.setTooltip(translate("share.link"));
				mainVC.put(copyLink.getComponentName(), copyLink);
				linkNames.add(copyLink.getComponentName());
				mainVC.contextPut("copyUrl", url);
				mainVC.contextPut("copyInfoTitle", translate("info.header"));
				mainVC.contextPut("copyLinkCopiedMessage", translate("share.link.copied"));

				qrLink = LinkFactory.createCustomLink("qr.link", "qr", "", Link.LINK + Link.NONTRANSLATED, mainVC, this);
				qrLink.setIconLeftCSS("o_icon o_icon_qrcode o_icon-lg");
				qrLink.setTitle(translate("share.qrcode"));
				linkNames.add(qrLink.getComponentName());
				continue;
			}

			ExternalLink networkLink = switch (name) {
				case "linkedin" -> createNetworkLink("linkedin", "o_icon_linkedin", "share.linkedin",
						"https://www.linkedin.com/feed/?shareActive&text=" + encode(title) + "%0A" + encode(url));
				case "facebook" -> createNetworkLink("facebook", "o_icon_facebook", "share.facebook",
						"https://www.facebook.com/sharer.php?u=" + encode(url) + "&t=" + encode(title));
				case "twitter" -> createNetworkLink("twitter", "o_icon_twitter", "share.twitter",
						"https://twitter.com/share?url=" + encode(url) + "&text=" + encode(title));
				case "mail" -> createNetworkLink("mail", "o_icon_mailto", "share.mail",
						"mailto:?subject=" + encode(title) + "&body=" + encode(url));
				default -> null;
			};
			if (networkLink != null) {
				mainVC.put(networkLink.getComponentName(), networkLink);
				linkNames.add(networkLink.getComponentName());
			}
		}

		putInitialPanel(mainVC);
	}

	private ExternalLink createNetworkLink(String name, String iconCss, String i18nKey, String url) {
		ExternalLink link = LinkFactory.createExternalLink(name, name, StringHelper.escapeForHtmlAttribute(url));
		link.setIconLeftCSS("o_icon " + iconCss + " o_icon-lg");
		link.setTooltip(translate(i18nKey));
		return link;
	}

	private String encode(String value) {
		// URLEncoder is form encoding (space -> '+'), a URI component needs space -> '%20'
		return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == qrLink) {
			fireEvent(ureq, QR_EVENT);
		}
	}
}
