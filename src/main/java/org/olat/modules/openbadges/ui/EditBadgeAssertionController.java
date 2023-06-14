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
import java.util.UUID;

import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.id.Identity;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.user.UserManager;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-06-05<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class EditBadgeAssertionController extends FormBasicController {

	private final BadgeAssertion badgeAssertion;
	private StaticTextElement uuidEl;
	private FormLink recipientButton;
	private Identity recipient;
	private SingleSelection badgeClassDropdown;
	private final SelectionValues badgeClassKV;
	private CloseableCalloutWindowController ccwc;
	private UserSearchController userSearchController;

	@Autowired
	private OpenBadgesManager openBadgesManager;

	@Autowired
	private UserManager userManager;

	public EditBadgeAssertionController(UserRequest ureq, WindowControl wControl, BadgeAssertion badgeAssertion) {
		super(ureq, wControl);
		this.badgeAssertion = openBadgesManager.getBadgeAssertion(badgeAssertion.getUuid());
		badgeClassKV = new SelectionValues();
		for (BadgeClass badgeClass : openBadgesManager.getBadgeClasses(null)) {
			badgeClassKV.add(SelectionValues.entry(badgeClass.getUuid(), badgeClass.getName()));
		}
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String uuid = badgeAssertion != null ? badgeAssertion.getUuid() :
				UUID.randomUUID().toString().replace("-", "");
		uuidEl = uifactory.addStaticTextElement("form.uuid", uuid, formLayout);

		FormLayoutContainer recipientButtonCont = FormLayoutContainer.createButtonLayout("recipientContainer", getTranslator());
		recipientButtonCont.setRootForm(mainForm);
		formLayout.add(recipientButtonCont);
		recipientButtonCont.setLabel("form.recipient", null);

		recipient = badgeAssertion != null ? badgeAssertion.getRecipient() : null;
		String recipientDisplayName = recipient != null ? userManager.getUserDisplayName(recipient.getUser()) : "";
		recipientButton = uifactory.addFormLink("form.recipient", recipientDisplayName, "form.recipient", recipientButtonCont, Link.BUTTON | Link.NONTRANSLATED);
		recipientButton.setIconRightCSS("o_icon o_icon_caret");

		badgeClassDropdown = uifactory.addDropdownSingleselect("form.badge.class", formLayout,
				badgeClassKV.keys(), badgeClassKV.values());
		badgeClassDropdown.addActionListener(FormEvent.ONCHANGE);

		if (badgeAssertion != null) {
			badgeClassDropdown.select(badgeAssertion.getBadgeClass().getUuid(), true);
		} else if (!badgeClassKV.isEmpty()) {
			badgeClassDropdown.select(badgeClassKV.keys()[0], true);
		}

		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		String submitLabelKey = badgeAssertion != null ? "save" : "assertion.add";
		uifactory.addFormSubmitButton(submitLabelKey, buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String badgeClassUuid = badgeClassDropdown.getSelectedKey();
		BadgeClass badgeClass = openBadgesManager.getBadgeClass(badgeClassUuid);
		if (badgeAssertion == null) {
			openBadgesManager.createBadgeAssertion(uuidEl.getValue(),  badgeClass,
					new Date(), recipient, getIdentity());
		} else {
			badgeAssertion.setRecipient(recipient);
			badgeAssertion.setBadgeClass(openBadgesManager.getBadgeClass(badgeClassDropdown.getSelectedKey()));
			openBadgesManager.updateBadgeAssertion(badgeAssertion);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == ccwc) {
			cleanUp();
		} else if (source == userSearchController) {
			if (event instanceof SingleIdentityChosenEvent singleIdentityChosenEvent) {
				recipient = singleIdentityChosenEvent.getChosenIdentity();
				String recipientDisplayName = userManager.getUserDisplayName(recipient.getUser());
				recipientButton.setI18nKey(recipientDisplayName);
			}
			ccwc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(userSearchController);
		removeAsListenerAndDispose(ccwc);
		userSearchController = null;
		ccwc = null;
	}


	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == recipientButton) {
			doSearchUser(ureq);
		}
	}

	private void doSearchUser(UserRequest ureq) {
		userSearchController = new UserSearchController(ureq, getWindowControl(), true);
		listenTo(userSearchController);

		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(),
				userSearchController.getInitialComponent(), recipientButton.getFormDispatchId(), "", true, "");
		listenTo(ccwc);
		ccwc.activate();
	}
}
