/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.repository.ui.list;

import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.social.shareLink.ShareLinkController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 18 Aug 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class InfoPageHeaderController extends BasicController {

	private static final String CMD_MARK = "mark";

	private Link markLink;
	private ShareLinkController shareCtrl;

	private final InfoPageData data;
	private final boolean guestOnly;

	@Autowired
	private MarkManager markManager;

	/**
	 * @param shareUrl the URL to share for the current entry point, or null to
	 *            hide the share action (e.g. the curriculum element preview in
	 *            the editor)
	 */
	public InfoPageHeaderController(UserRequest ureq, WindowControl wControl, InfoPageData data, String shareUrl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(),
				Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator())));
		setVelocityRoot(Util.getPackageVelocityRoot(InfoPageHeaderController.class));
		this.data = data;

		guestOnly = ureq.getUserSession().getRoles() == null || ureq.getUserSession().getRoles().isGuestOnly();

		VelocityContainer mainVC = createVelocityContainer("details_header");
		putInitialPanel(mainVC);

		mainVC.contextPut("iconCssClass", data.getIconCssClass());
		mainVC.contextPut("externalRef", data.getExternalRef());
		mainVC.contextPut("translatedTechnicalType", data.getTranslatedTechnicalType());
		mainVC.contextPut("title", data.getTitle());
		mainVC.contextPut("teaser", data.getTeaser());
		mainVC.contextPut("taxonomyLevelTags", TaxonomyUIFactory.getTags(getTranslator(), data.getTaxonomyLevels()));

		if (getIdentity() != null && !guestOnly && data.hasBookmark()) {
			boolean marked = markManager.isMarked(data.getBookmarkOres(), getIdentity(), null);
			markLink = LinkFactory.createCustomLink(CMD_MARK, CMD_MARK, "details.bookmark", Link.BUTTON, mainVC, this);
			markLink.setElementCssClass("o_button_ghost");
			decorateMarkLink(marked);
		}

		if (StringHelper.containsNonWhitespace(shareUrl)) {
			shareCtrl = new ShareLinkController(ureq, wControl, false, false);
			shareCtrl.setShareUrl(shareUrl);
			shareCtrl.setShareTitle(data.getTitle());
			listenTo(shareCtrl);
			mainVC.put("share", shareCtrl.getInitialComponent());
		}

		if (data.getEducationalType() != null) {
			String educationalType = translate(RepositoyUIFactory.getI18nKey(data.getEducationalType()));
			mainVC.contextPut("educationalType", educationalType);
		}
	}

	private void decorateMarkLink(boolean marked) {
		markLink.setIconLeftCSS(marked ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
		markLink.setCustomDisplayText(translate(marked ? "details.bookmark.remove" : "details.bookmark"));
	}

	private void doMark() {
		OLATResourceable ores = data.getBookmarkOres();
		if (markManager.isMarked(ores, getIdentity(), null)) {
			markManager.removeMark(ores, getIdentity(), null);
			decorateMarkLink(false);
		} else {
			markManager.setMark(ores, getIdentity(), null, data.getBookmarkBusinessPath());
			decorateMarkLink(true);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == markLink) {
			doMark();
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		//
	}

}
