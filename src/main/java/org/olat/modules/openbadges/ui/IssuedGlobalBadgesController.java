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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.Formatter;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.user.UserManager;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-06-05<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class IssuedGlobalBadgesController extends FormBasicController {
	private final static String CMD_SELECT = "select";

	private TableModel tableModel;
	private FlexiTableElement tableEl;
	private FormLink addLink;
	private CloseableModalController cmc;
	private IssueGlobalBadgeController issueGlobalBadgeCtrl;
	private BadgeAssertionPublicController badgeAssertionPublicController;
	private DialogBoxController confirmRevokeCtrl;
	private DialogBoxController confirmDeleteCtrl;

	@Autowired
	private OpenBadgesManager openBadgesManager;
	@Autowired
	private UserManager userManager;

	protected IssuedGlobalBadgesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "issued_global_badges");

		flc.contextPut("noGlobalBadgesAvailable", openBadgesManager.getNumberOfBadgeClasses(null) == 0);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.name, CMD_SELECT));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.recipient));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.awardedBy));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.issuedOn));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.status, new BadgeAssertionStatusRenderer()));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.revoke));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.delete));

		tableModel = new TableModel(columnModel, userManager);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 10, true,
				getTranslator(), formLayout);
		tableEl.setEmptyTableSettings("empty.badges.table", null,
				"o_icon_badge", null, null,
				false);

		addLink = uifactory.addFormLink("add", "issueGlobalBadge", "issueGlobalBadge", formLayout, Link.BUTTON);
		updateUI();
	}

	private void updateUI() {
		List<Row> rows = openBadgesManager
				.getBadgeAssertionsWithSizes(null, null, false)
				.stream()
				.map(baws -> {
					FormLink revokeLink = null;
					if (baws.badgeAssertion().getStatus() == BadgeAssertion.BadgeAssertionStatus.issued) {
						revokeLink = uifactory.addFormLink("revoke_" + baws.badgeAssertion().getKey(),
								"revoke", Cols.revoke.i18nHeaderKey(), null, flc, Link.LINK);
						revokeLink.setUserObject(baws.badgeAssertion());
					}
					FormLink deleteLink = null;
					if (baws.badgeAssertion().getStatus() == BadgeAssertion.BadgeAssertionStatus.revoked) {
						deleteLink = uifactory.addFormLink("delete_" + baws.badgeAssertion().getKey(),
								"delete", Cols.delete.i18nHeaderKey(), null, flc, Link.LINK);
						deleteLink.setUserObject(baws.badgeAssertion());
					}
					return new Row(baws, revokeLink, deleteLink);
				}).toList();
		tableModel.setObjects(rows);
		tableEl.reset();
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(issueGlobalBadgeCtrl);
		removeAsListenerAndDispose(badgeAssertionPublicController);
		cmc = null;
		issueGlobalBadgeCtrl = null;
		badgeAssertionPublicController = null;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == issueGlobalBadgeCtrl) {
			cmc.deactivate();
			cleanUp();
			updateUI();
		} else if (source == badgeAssertionPublicController) {
			cmc.deactivate();
			cleanUp();
		} else if (source == confirmRevokeCtrl) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				BadgeAssertion badgeAssertion = (BadgeAssertion) confirmRevokeCtrl.getUserObject();
				doRevoke(badgeAssertion);
			}
		} else if (source == confirmDeleteCtrl) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				BadgeAssertion badgeAssertion = (BadgeAssertion) confirmDeleteCtrl.getUserObject();
				doDelete(badgeAssertion);
			}
		} else if (source == cmc) {
			cleanUp();
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == addLink) {
			doUpload(ureq);
		} else if (source instanceof FormLink formLink) {
			if ("revoke".equals(formLink.getCmd())) {
				BadgeAssertion badgeAssertion = (BadgeAssertion) formLink.getUserObject();
				doConfirmRevoke(ureq, badgeAssertion);
			}
			if ("delete".equals(formLink.getCmd())) {
				BadgeAssertion badgeAssertion = (BadgeAssertion) formLink.getUserObject();
				doConfirmDelete(ureq, badgeAssertion);
			}
		} else if (event instanceof SelectionEvent selectionEvent) {
			Row row = tableModel.getObject(selectionEvent.getIndex());
			doOpenDetails(ureq, row.badgeAssertionWithSize.badgeAssertion());
		}
	}

	private void doOpenDetails(UserRequest ureq, BadgeAssertion badgeAssertion) {
		badgeAssertionPublicController = new BadgeAssertionPublicController(ureq, getWindowControl(), badgeAssertion.getUuid());
		listenTo(badgeAssertionPublicController);

		String title = translate("issuedGlobalBadge");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				badgeAssertionPublicController.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doUpload(UserRequest ureq) {
		Long numberOfGlobalBadgeClasses = openBadgesManager.getNumberOfBadgeClasses(null);
		if (numberOfGlobalBadgeClasses == 0) {
			showWarning("warning.no.global.badges.available");
			return;
		}

		issueGlobalBadgeCtrl = new IssueGlobalBadgeController(ureq, getWindowControl(), null);
		listenTo(issueGlobalBadgeCtrl);

		String title = translate("issueGlobalBadge");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				issueGlobalBadgeCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doConfirmRevoke(UserRequest ureq, BadgeAssertion badgeAssertion) {
		String recipientDisplayName = userManager.getUserDisplayName(badgeAssertion.getRecipient());
		String title = translate("confirm.revoke.issued.badge.title", recipientDisplayName);
		String text = translate("confirm.revoke.issued.badge", recipientDisplayName);
		confirmRevokeCtrl = activateOkCancelDialog(ureq, title, text, confirmRevokeCtrl);
		confirmRevokeCtrl.setUserObject(badgeAssertion);
	}

	private void doConfirmDelete(UserRequest ureq, BadgeAssertion badgeAssertion) {
		String recipientDisplayName = userManager.getUserDisplayName(badgeAssertion.getRecipient());
		String title = translate("confirm.delete.issued.badge.title", recipientDisplayName);
		String text = translate("confirm.delete.issued.badge", recipientDisplayName);
		confirmDeleteCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteCtrl);
		confirmDeleteCtrl.setUserObject(badgeAssertion);
	}

	private void doRevoke(BadgeAssertion badgeAssertion) {
		openBadgesManager.revokeBadgeAssertion(badgeAssertion.getKey());
		updateUI();
	}

	private void doDelete(BadgeAssertion badgeAssertion) {
		openBadgesManager.deleteBadgeAssertion(badgeAssertion);
		updateUI();
	}

	enum Cols implements FlexiSortableColumnDef {
		name("form.name"),
		recipient("form.recipient"),
		awardedBy("form.awarded.by"),
		issuedOn("form.issued.on"),
		status("form.status"),
		revoke("table.revoke"),
		delete("table.delete");

		Cols(String i18n) {
			this.i18nKey = i18n;
		}

		private final String i18nKey;

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}

	record Row(OpenBadgesManager.BadgeAssertionWithSize badgeAssertionWithSize, FormLink revokeLink, FormLink deleteLink) {}

	private class TableModel extends DefaultFlexiTableDataModel<Row> {
		private final UserManager userManager;

		public TableModel(FlexiTableColumnModel columnModel, UserManager userManager) {
			super(columnModel);
			this.userManager = userManager;
		}

		@Override
		public Object getValueAt(int row, int col) {
			BadgeAssertion badgeAssertion = getObject(row).badgeAssertionWithSize.badgeAssertion();
			return switch (Cols.values()[col]) {
				case name -> badgeAssertion.getBadgeClass().getName();
				case recipient -> userManager.getUserDisplayName(badgeAssertion.getRecipient());
				case status -> badgeAssertion.getStatus();
				case awardedBy -> userManager.getUserDisplayName(badgeAssertion.getAwardedBy());
				case issuedOn -> Formatter.getInstance(getLocale()).formatDateAndTime(badgeAssertion.getIssuedOn());
				case revoke -> getObject(row).revokeLink();
				case delete -> getObject(row).deleteLink();
			};
		}
	}
}
