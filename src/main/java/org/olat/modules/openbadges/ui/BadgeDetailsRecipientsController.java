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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.criteria.BadgeCondition;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-07-13<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgeDetailsRecipientsController extends FormBasicController {

	private final static String CMD_SELECT = "select";

	private final Long badgeClassKey;
	private TableModel tableModel;
	private FlexiTableElement tableEl;
	private CloseableModalController cmc;
	private BadgeAssertionPublicController badgeAssertionPublicController;
	private CloseableCalloutWindowController calloutCtrl;
	private ToolsController toolsCtrl;
	private DialogBoxController confirmRevokeCtrl;

	@Autowired
	private UserManager userManager;
	@Autowired
	private OpenBadgesManager openBadgesManager;

	public BadgeDetailsRecipientsController(UserRequest ureq, WindowControl wControl, Long badgeClassKey) {
		super(ureq, wControl, FormBasicController.LAYOUT_BAREBONE);
		this.badgeClassKey = badgeClassKey;

		initForm(ureq);
		loadData(null);
		initFilters();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.recipient, CMD_SELECT));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.issuedOn));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.version));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.verification, new BadgeVerificationCellRenderer()));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.status, new BadgeAssertionStatusRenderer(openBadgesManager)));
		columnModel.addFlexiColumnModel(new ActionsColumnModel(Cols.tools));

		tableModel = new TableModel(columnModel, userManager, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 10, true,
				getTranslator(), formLayout);
		tableEl.setEmptyTableMessageKey("form.recipients.none");
		tableEl.setAndLoadPersistedPreferences(ureq, "badge-assertions-recipients");
	}

	void loadData(List<FlexiTableFilter> filters) {
		BadgeClass badgeClass = openBadgesManager.getBadgeClassByKey(badgeClassKey);

		List<Row> rows = openBadgesManager
				.getBadgeAssertions(badgeClass, true)
				.stream()
				.map(this::mapBadgeAssertionToRow)
				.toList();
		
		if (filters == null) {
			tableModel.setObjects(rows);
		} else {
			List<Row> filteredRows = rows.stream().filter(r -> {
				String version = r.badgeAssertion.getBadgeClass().getVersion();
				String status = r.badgeAssertion.getStatus().name();

				for (FlexiTableFilter filter : filters) {
					boolean matchFound = false;
					if (filter instanceof FlexiTableMultiSelectionFilter multiSelectionFilter) {
						if (multiSelectionFilter.getValues() == null || multiSelectionFilter.getValues().isEmpty()) {
							continue;
						}
						if (Filter.VERSION.name().equals(filter.getFilter())) {
							for (String value : multiSelectionFilter.getValues()) {
								if (version.equals(value)) {
									matchFound = true;
									break;
								}
							}
						} else if (Filter.STATUS.name().equals(filter.getFilter())) {
							for (String value : multiSelectionFilter.getValues()) {
								if (status.equals(value)) {
									matchFound = true;
									break;
								}
							}
						}
					}
					if (!matchFound) {
						return false;
					}
				}
				return true;
			}).toList();
			tableModel.setObjects(filteredRows);
		}
		
		tableEl.reset();
	}

	private Row mapBadgeAssertionToRow(BadgeAssertion badgeAssertion) {
		FormLink toolLink = ActionsColumnModel.createLink(uifactory, getTranslator());
		toolLink.setUserObject(badgeAssertion);
		return new Row(badgeAssertion, toolLink);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		SelectionValues statusKV = OpenBadgesUIFactory.badgeAssertionStatusKV(getTranslator());
		
		SelectionValues versionKV = new SelectionValues();
		Set<String> versions = tableModel.getObjects().stream()
				.map(r -> r.badgeAssertion.getBadgeClass().getVersion()).collect(Collectors.toSet());
		versions.forEach(version -> versionKV.add(SelectionValues.entry(version, version)));
		
		filters.add(new FlexiTableMultiSelectionFilter(translate(Filter.VERSION.getI18nKey()),
				Filter.VERSION.name(), versionKV, true));
		filters.add(new FlexiTableMultiSelectionFilter(translate(Filter.STATUS.getI18nKey()),
				Filter.STATUS.name(), statusKV, true));
		
		tableEl.setFilters(true, filters, true, true);
	}
	
	public void setVersionFilter(String version) {
		FlexiTableFilter versionFilter = FlexiTableFilter.getFilter(tableEl.getFilters(), Filter.VERSION.name());
		if (versionFilter instanceof FlexiTableMultiSelectionFilter multiSelectionFilter) {
			tableEl.setFilterValue(multiSelectionFilter, version);
			loadData(tableEl.getFilters());
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == cmc) {
			cleanUp();
		} else if (source == badgeAssertionPublicController) {
			cmc.deactivate();
			cleanUp();
		} else if (source == toolsCtrl) {
			if (calloutCtrl != null) {
				calloutCtrl.deactivate();
			}
			cleanUp();
		} else if (source == confirmRevokeCtrl) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				BadgeAssertion badgeAssertion = (BadgeAssertion) confirmRevokeCtrl.getUserObject();
				doRevoke(badgeAssertion);
			}
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(badgeAssertionPublicController);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		cmc = null;
		badgeAssertionPublicController = null;
		calloutCtrl = null;
		toolsCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof SelectionEvent selectionEvent) {
				String command = selectionEvent.getCommand();
				Row row = tableModel.getObject(selectionEvent.getIndex());
				if (CMD_SELECT.equals(command)) {
					doSelect(ureq, row);
				}
			} else if (event instanceof FlexiTableSearchEvent searchEvent
					&& FlexiTableSearchEvent.FILTER.equals(searchEvent.getCommand())) {
				loadData(searchEvent.getFilters());
			}
		} else if (source instanceof FormLink link) {
			if ("tools".equals(link.getCmd()) && link.getUserObject() instanceof BadgeAssertion badgeAssertion) {
				doOpenTools(ureq, link, badgeAssertion);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doOpenTools(UserRequest ureq, FormLink link, BadgeAssertion badgeAssertion) {
		toolsCtrl = new ToolsController(ureq, getWindowControl(), badgeAssertion);
		listenTo(toolsCtrl);

		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}

	private void doSelect(UserRequest ureq, Row row) {
		BadgeAssertion badgeAssertion = row.badgeAssertion();
		badgeAssertionPublicController = new BadgeAssertionPublicController(ureq, getWindowControl(), badgeAssertion.getUuid());
		listenTo(badgeAssertionPublicController);

		String title = translate("issuedBadge");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				badgeAssertionPublicController.getInitialComponent(), true, title);
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

	private void doViewBadgeInfo(UserRequest ureq, BadgeAssertion badgeAssertion) {
		badgeAssertionPublicController = new BadgeAssertionPublicController(ureq, getWindowControl(), badgeAssertion.getUuid());
		listenTo(badgeAssertionPublicController);

		String title = translate("form.view.badge.info");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				badgeAssertionPublicController.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doRevoke(BadgeAssertion badgeAssertion) {
		openBadgesManager.revokeBadgeAssertion(badgeAssertion.getKey());
		loadData(null);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	public record Condition(BadgeCondition badgeCondition, boolean first, Translator translator, RepositoryEntry courseEntry) {
		@Override
		public String toString() {
			return badgeCondition.toString(translator, courseEntry);
		}
	}

	enum Cols implements FlexiSortableColumnDef {
		recipient("form.recipient", true),
		issuedOn("form.issued.on", true),
		version("form.version", true),
		status("form.status", true),
		verification("verification", true),
		tools("action.more", false);

		Cols(String i18n, boolean sortable) {
			this.i18nKey = i18n;
			this.sortable = sortable;
		}

		private final String i18nKey;
		private final boolean sortable;

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return sortable;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}

	record Row(BadgeAssertion badgeAssertion, FormLink toolLink) {
	}

	private class TableModel extends DefaultFlexiTableDataModel<Row> implements SortableFlexiTableDataModel<Row> {
		private final UserManager userManager;
		private final Translator translator;

		public TableModel(FlexiTableColumnModel columnModel, UserManager userManager, Translator translator) {
			super(columnModel);
			this.userManager = userManager;
			this.translator = translator;
		}

		@Override
		public Object getValueAt(int row, int col) {
			Row rowObject = getObject(row);
			return getValueAt(rowObject, col);
		}

		@Override
		public void sort(SortKey sortKey) {
			if (sortKey != null) {
				List<Row> rows = new BadgeAssertionSortDelegate(sortKey, this, translator.getLocale()).sort();
				super.setObjects(rows);
			}
		}

		@Override
		public Object getValueAt(Row row, int col) {
			BadgeAssertion badgeAssertion = row.badgeAssertion();
			return switch (Cols.values()[col]) {
				case recipient -> userManager.getUserDisplayName(badgeAssertion.getRecipient());
				case status -> badgeAssertion;
				case verification -> badgeAssertion.getBadgeClass().getVerificationMethod();
				case issuedOn -> Formatter.getInstance(getLocale()).formatDateAndTime(badgeAssertion.getIssuedOn());
				case version -> OpenBadgesUIFactory.versionString(translator, badgeAssertion.getBadgeClass(), true, false);
				case tools -> row.toolLink();
			};
		}
	}
	
	private class BadgeAssertionSortDelegate extends SortableFlexiTableModelDelegate<Row> {

		public BadgeAssertionSortDelegate(SortKey orderBy, SortableFlexiTableDataModel<Row> tableModel, Locale locale) {
			super(orderBy, tableModel, locale);
		}

		@Override
		protected void sort(List<Row> rows) {
			if (Cols.values()[getColumnIndex()] == Cols.status) {
				rows.sort(Comparator.comparing(this::statusColumnString));
			}
			super.sort(rows);
		}
		
		private String statusColumnString(Row row) {
			if (openBadgesManager.isBadgeAssertionExpired(row.badgeAssertion)) {
				return translate("expired");
			} else {
				return translate("assertion.status." + row.badgeAssertion.getStatus().name());
			}
		}
	}
	
	private enum Filter {
		VERSION("form.version"),
		STATUS("form.status");
		
		private final String i18nKey;
		
		Filter(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String getI18nKey() {
			return i18nKey;
		}
	}

	private class ToolsController extends BasicController {

		private final Link viewInfoLink;
		private final Link revokeLink;
		private final BadgeAssertion badgeAssertion;

		protected ToolsController(UserRequest ureq, WindowControl wControl, BadgeAssertion badgeAssertion) {
			super(ureq, wControl);
			this.badgeAssertion = badgeAssertion;

			VelocityContainer mainVC = createVelocityContainer("tools");

			viewInfoLink = LinkFactory.createLink("form.view.badge.info", "viewBadgeInfo", getTranslator(), mainVC, this, Link.LINK);
			mainVC.put("form.view.badge.info", viewInfoLink);

			revokeLink = LinkFactory.createLink("table.revoke", "revoke", getTranslator(), mainVC, this, Link.LINK);
			revokeLink.setVisible(badgeAssertion.getStatus() == BadgeAssertion.BadgeAssertionStatus.issued);
			mainVC.put("tool.revoke", revokeLink);

			putInitialPanel(mainVC);
		}


		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			if (source == revokeLink) {
				doConfirmRevoke(ureq, badgeAssertion);
			} else if (source == viewInfoLink) {
				doViewBadgeInfo(ureq, badgeAssertion);
			}
		}
	}
}
