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
package org.olat.modules.openbadges.ui;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.commons.services.image.Size;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateItem;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.Image;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-07-03<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AwardBadgesWithPreviewController extends FormBasicController {

	private final List<Identity> recipients;
	private SingleSelection badgeDropdown;
	private final SelectionValues badgeKV;
	private StaticTextElement informationEl;

	@Autowired
	private UserManager userManager;
	@Autowired
	private OpenBadgesManager openBadgesManager;

	public AwardBadgesWithPreviewController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			List<Identity> recipients, List<BadgeClass> excludedBadgeClasses) {
		super(ureq, wControl, LAYOUT_VERTICAL);

		this.recipients = recipients;
		
		String mediaUrl = registerMapper(ureq, new BadgeClassMediaFileMapper());
		Set<BadgeClass> excludedBadgeSet = excludedBadgeClasses == null ? Set.of() : new HashSet<>(excludedBadgeClasses);
		badgeKV = new SelectionValues();
		openBadgesManager.getBadgeClassesWithSizesAndCounts(courseEntry).forEach(bcw -> {
			BadgeClass bc = bcw.badgeClass();
			if(!excludedBadgeSet.contains(bc)) {
				String url = mediaUrl + "/" + bc.getImage();
				Size targetSize = bcw.fitIn(90, 90);
				badgeKV.add(SelectionValues.entry(bc.getUuid(), bc.getNameWithScan(), new Image(url, targetSize)));
			}
		});

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_award_badge_form");
		
		badgeDropdown = uifactory.addCardSingleSelectHorizontal("form.badge", "form.badge", "form.badge", formLayout, badgeKV, true, "badges.more");
		badgeDropdown.addActionListener(FormEvent.ONCHANGE);
		if (!badgeKV.isEmpty()) {
			badgeDropdown.select(badgeKV.keys()[0], true);
		}
		badgeDropdown.setVisible(!badgeKV.isEmpty());

		EmptyStateItem emptyState = uifactory.addEmptyState("empty", null, formLayout);
		emptyState.setIconCss("o_icon o_icon_badge");
		emptyState.setMessageI18nKey("empty.badges.class");
		emptyState.setVisible(badgeKV.isEmpty());

		informationEl = uifactory.addStaticTextElement("form.information", null, "", formLayout);
		updateUI();

		FormLayoutContainer buttonCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		if(!badgeKV.isEmpty()) {
			uifactory.addFormSubmitButton("award.badge", buttonCont);
		}
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}

	private void updateUI() {
		if (badgeKV.isEmpty()) {
			return;
		}

		String badgeName = badgeDropdown.getSelectedValue();
		StringBuilder sb = new StringBuilder();
		sb.append(translate("form.award.badge.to", badgeName));
		sb.append(" ");
		for (int i = 0; i < recipients.size(); i++) {
			if (i > 0) {
				sb.append(" ,");
			}
			String recipientDisplayName = userManager.getUserDisplayName(recipients.get(i));
			sb.append("\"").append(recipientDisplayName).append("\"");
		}

		informationEl.setValue(sb.toString());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == badgeDropdown) {
			updateUI();
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		BadgeClass badgeClass = openBadgesManager.getBadgeClass(badgeDropdown.getSelectedKey());
		Date now = new Date();
		if (badgeClass != null) {
			for (Identity recipient : recipients) {
				String uuid = OpenBadgesUIFactory.createIdentifier();
				openBadgesManager.createBadgeAssertion(uuid, badgeClass, now, recipient, getIdentity());
			}
		}
		showInfo("badges.awarded", Long.toString(recipients.size()));
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private class BadgeClassMediaFileMapper implements Mapper {

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			VFSLeaf classFileLeaf = openBadgesManager.getBadgeClassVfsLeaf(relPath);
			if (classFileLeaf != null) {
				return new VFSMediaResource(classFileLeaf);
			}
			return new NotFoundMediaResource();
		}
	}
}
