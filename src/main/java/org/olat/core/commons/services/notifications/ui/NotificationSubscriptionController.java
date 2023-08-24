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
package org.olat.core.commons.services.notifications.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.notifications.NotificationUIFactory;
import org.olat.core.commons.services.notifications.NotificationsHandler;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.project.ProjProject;
import org.olat.repository.ui.RepositoyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * This controller shows the list of the users subscriptions and allows him to
 * manage (delete) them. This controller does not show the actual news generated
 * by the subscriptions, use the NotificationNewsController for this purpose.
 * <p>
 * Events fired by this controller:
 * <ul>
 * <li>Event.CHANGED_EVENT when a subscription has been deleted</li>
 * </ul>
 * <p>
 * Initial Date: 22.12.2009 <br>
 *
 * @author gnaegi
 */
public class NotificationSubscriptionController extends FormBasicController {

	public static final String ALL_TAB_ID = "All";

	private static final String FORMLINK_LEARNING_RESOURCE = "learningResource";
	private static final String FORMLINK_SUB_RES = "subRes";
	private static final String FORMLINK_DELETE = "delete";
	private final List<NotificationSubscriptionRow> subscriptionRows = new ArrayList<>();
	private final Identity subscriberIdentity;
	private boolean adminColumns;
	private FlexiFiltersTab allTab;
	private NotificationSubscriptionTableDataModel subscriptionsTableDataModel;
	private DialogBoxController delYesNoC;
	private FlexiTableElement tableEl;

	@Autowired
	private NotificationsManager notificationsManager;


	public NotificationSubscriptionController(
			UserRequest ureq,
			WindowControl wControl,
			Identity subscriberIdentity,
			boolean adminColumns,
			boolean fieldSet) {
		super(ureq, wControl, fieldSet ? "notificationsSubscriptionsField" : "notificationsSubscriptions");
		this.subscriberIdentity = subscriberIdentity;
		this.adminColumns = adminColumns;

		// Build the table that contains all the subscriptions
		TableGuiConfiguration tableGuiPrefs = new TableGuiConfiguration();
		tableGuiPrefs.setTableEmptyMessage(translate("subscriptions.no.subscriptions"), translate("subscriptions.no.subscriptions.hint"), "o_icon_notification");
		tableGuiPrefs.setPreferencesOffered(true, "notifications-" + adminColumns);

		initForm(ureq);
	}

	/**
	 * Update the table model
	 */
	private void updateSubscriptionsDataModel() {
		subscriptionRows.clear();

		List<String> filterSections = getFilteredSections();
		String searchString = tableEl.getQuickSearchString();

		// Load subscriptions from DB. Don't use the ureq.getIdentity() but the
		// subscriberIdentity instead to make this controller also be usable in the
		// admin environment (admins might change notifications for a user)
		List<Subscriber> subs = notificationsManager.getSubscribers(subscriberIdentity, false);
		for (Iterator<Subscriber> subIt = subs.iterator(); subIt.hasNext(); ) {
			Subscriber sub = subIt.next();
			if (!notificationsManager.isPublisherValid(sub.getPublisher())) {
				subIt.remove();
			}
		}

		for (Subscriber sub : subs) {
			NotificationSubscriptionRow row = forgeRow(sub);
			if (isExcludedBySection(filterSections, row)
					|| (!searchString.isBlank() && isExcludedBySearchString(searchString, row))) {
				continue;
			}
			subscriptionRows.add(row);
		}

		subscriptionsTableDataModel.setObjects(subscriptionRows);
		tableEl.reset(false, true, true);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, NotificationSubscriptionCols.key));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NotificationSubscriptionCols.section));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NotificationSubscriptionCols.learningResource));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NotificationSubscriptionCols.subRes));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NotificationSubscriptionCols.addDesc));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NotificationSubscriptionCols.statusToggle));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(adminColumns, NotificationSubscriptionCols.creationDate));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(adminColumns, NotificationSubscriptionCols.lastEmail));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NotificationSubscriptionCols.deleteLink));

		subscriptionsTableDataModel = new NotificationSubscriptionTableDataModel(tableColumnModel, getLocale());

		tableEl = uifactory.addTableElement(getWindowControl(), "NotificationSubscriptionTable", subscriptionsTableDataModel, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);
		tableEl.sort(new SortKey(NotificationSubscriptionCols.learningResource.sortKey(), true));

		updateSubscriptionsDataModel();
		initFiltersPresets(ureq);
		initFilters();

		tableEl.setExportEnabled(true);
	}


	private NotificationSubscriptionRow forgeRow(Subscriber sub) {
		Publisher pub = sub.getPublisher();
		String section = !pub.getResId().equals(0L)
				? NewControllerFactory.translateResourceableTypeName(pub.getResName(), getLocale())
				: NewControllerFactory.translateResourceableTypeName("Global", getLocale());
		FormLink learningResource = uifactory.addFormLink("learningResource_" + sub.getKey().toString(), FORMLINK_LEARNING_RESOURCE, "", null, flc, Link.NONTRANSLATED);
		FormLink subRes = uifactory.addFormLink("subRes_" + sub.getKey().toString(), FORMLINK_SUB_RES, "", null, flc, Link.NONTRANSLATED);
		String addDesc = "";
		FormToggle statusToggle = uifactory.addToggleButton("subscription_" + sub.getKey().toString(), null, translate("on"), translate("off"), flc);
		statusToggle.addActionListener(FormEvent.ONCHANGE);
		FormLink deleteLink = null;

		if (!pub.getResId().equals(0L)) {
			deleteLink = uifactory.addFormLink("delete_" + sub.getKey().toString(), FORMLINK_DELETE, "table.column.delete.action", null, flc, Link.LINK);
		}

		NotificationsHandler handler = notificationsManager.getNotificationsHandler(pub);
		String title = "";
		String iconCssFromHandler = "";

		if (sub.isEnabled()) {
			statusToggle.toggleOn();
		} else {
			statusToggle.toggleOff();
		}

		if (handler != null) {
			title = handler.getDisplayName(pub);
			iconCssFromHandler = handler.getIconCss();
			// If resId is 0L that means, publisher is of section global
			if (pub.getResId().equals(0L)) {
				addDesc = handler.getAdditionalDescriptionI18nKey(getLocale()) != null
						? handler.getAdditionalDescriptionI18nKey(getLocale()) : "";
			}
		}

		if (title != null) {
			title = StringHelper.escapeHtml(title);
			learningResource.setI18nKey(title);
		}
		// if current row is not a global one then check for pub.getResName and set fitting cssIcon
		// because global entries don't have any icons for learning resource
		if (!"-".equals(title)) {
			String iconCssClass;
			if ("CalendarManager.course".equals(pub.getResName())
					|| "RepositoryEntry".equals(pub.getResName())
					|| "AssessmentManager".equals(pub.getResName())
					|| "CertificatesManager".equals(pub.getResName())) {
				iconCssClass = CSSHelper.getIconCssClassFor(RepositoyUIFactory.getIconCssClass("CourseModule"));
			} else if ("CalendarManager.group".equals(pub.getResName())) {
				iconCssClass = CSSHelper.getIconCssClassFor(CSSHelper.CSS_CLASS_GROUP);
			} else if (ProjProject.TYPE.equals(pub.getResName())) {
				iconCssClass = CSSHelper.getIconCssClassFor("o_icon_proj_project");
			} else {
				iconCssClass = CSSHelper.getIconCssClassFor(RepositoyUIFactory.getIconCssClass(pub.getResName()));
			}
			learningResource.setIconLeftCSS(iconCssClass);
		}

		String resourceType = NewControllerFactory.translateResourceableTypeName(pub.getType(), getLocale());
		subRes.setI18nKey(resourceType);
		subRes.setIconLeftCSS(iconCssFromHandler);

		NotificationSubscriptionRow row = new NotificationSubscriptionRow(section, learningResource, subRes, addDesc, statusToggle,
				sub.getCreationDate(), sub.getLatestEmailed(), deleteLink, sub.getKey());
		learningResource.setUserObject(row);
		subRes.setUserObject(row);
		statusToggle.setUserObject(row);
		if (deleteLink != null) {
			deleteLink.setUserObject(row);
		}

		return row;
	}

	private List<String> getDistinctSubscriptionTypes() {
		return subscriptionRows.stream()
				.map(NotificationSubscriptionRow::getSection)
				.distinct()
				.toList();
	}

	protected final void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(3);

		allTab = FlexiFiltersTabFactory.tabWithFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.clear, List.of());
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);

		for (String section : getDistinctSubscriptionTypes()) {
			FlexiFiltersTab filterType = FlexiFiltersTabFactory.tabWithFilters(section, section,
					TabSelectionBehavior.clear, List.of());
			tabs.add(filterType);
		}

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}

	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(1);

		if (!subscriptionRows.isEmpty()) {
			SelectionValues sectionValues = new SelectionValues();

			for (String section : getDistinctSubscriptionTypes()) {
				sectionValues.add(SelectionValues.entry(section, section));
			}

			FlexiTableMultiSelectionFilter sectionFilter = new FlexiTableMultiSelectionFilter(translate("table.column.section"),
					"section", sectionValues, true);
			filters.add(sectionFilter);
		}

		tableEl.setFilters(true, filters, false, false);
		tableEl.expandFilters(true);
	}

	private List<String> getFilteredSections() {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		FlexiTableFilter sectionFilter = FlexiTableFilter.getFilter(filters, "section");
		if (sectionFilter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter) sectionFilter).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				return filterValues;
			}
		}
		return Collections.emptyList();
	}

	private boolean isExcludedBySection(List<String> filterSections, NotificationSubscriptionRow row) {
		if (!filterSections.isEmpty() && !filterSections.contains(row.getSection())) {
			return true;
		}
		if ((tableEl.getSelectedFilterTab() != null
				&& !tableEl.getSelectedFilterTab().getId().equals(ALL_TAB_ID)
				&& !tableEl.getSelectedFilterTab().getId().equals(row.getSection()))) {
			return true;
		}
		return false;
	}

	private boolean isExcludedBySearchString(String searchString, NotificationSubscriptionRow row) {
		boolean excluded = true;

		if (row.getSection().toLowerCase().contains(searchString.toLowerCase())) {
			excluded = false;
		} else if (row.getLearningResource().getI18nKey().contains(searchString)) excluded = false;
		else if (row.getSubRes().getI18nKey().contains(searchString)) excluded = false;
		else if (row.getAddDesc().contains(searchString)) excluded = false;

		return excluded;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// No need
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (tableEl == source) {
			if (event instanceof FlexiTableSearchEvent
					|| event instanceof FlexiTableFilterTabEvent) {
				updateSubscriptionsDataModel();
			}
		} else if (source instanceof FormToggle toggle && toggle.getUserObject() instanceof NotificationSubscriptionRow row) {
			Subscriber subscriber = notificationsManager.getSubscriber(row.getKey());
			notificationsManager.updateSubscriber(subscriber, toggle.isOn());
		} else if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if (cmd.equals(FORMLINK_LEARNING_RESOURCE) || cmd.equals(FORMLINK_SUB_RES) || cmd.equals(FORMLINK_DELETE)) {
				Long subscriptionKey = Long.parseLong(link.getComponent().getComponentName().replaceAll(".+?_", ""));
				Subscriber subscriber = notificationsManager.getSubscriber(subscriptionKey);
				if (FORMLINK_LEARNING_RESOURCE.equals(cmd) || FORMLINK_SUB_RES.equals(cmd)) {
					doLaunchSubscriptionResource(ureq, subscriber);
				}
				if (FORMLINK_DELETE.equals(cmd)) {
					NotificationSubscriptionRow row = (NotificationSubscriptionRow) link.getUserObject();
					delYesNoC = activateYesNoDialog(ureq, null, translate("confirm.delete", row.getLearningResource().getI18nKey()), delYesNoC);
					delYesNoC.setUserObject(subscriber);
				}
			}
		}

		super.formInnerEvent(ureq, source, event);
	}

	private void doLaunchSubscriptionResource(UserRequest ureq, Subscriber subscriber) {
		if (subscriber != null) {
			NotificationUIFactory.launchSubscriptionResource(ureq, getWindowControl(), subscriber);
		} else {
			updateSubscriptionsDataModel();
			showInfo("info.notification.deleted");
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == delYesNoC) {
			if (DialogBoxUIFactory.isYesEvent(event)) { // ok
				// Remove subscription and update data model
				Subscriber sub = (Subscriber) delYesNoC.getUserObject();
				notificationsManager.deleteSubscriber(sub.getKey());
				updateSubscriptionsDataModel();
				showInfo("info.notification.deleted");
				// Notify parent controller
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			// cleanup dialog
			delYesNoC.dispose();
			delYesNoC = null;
		}
	}
}
