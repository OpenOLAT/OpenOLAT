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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.olat.NewControllerFactory;
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

	private static final String FORMLINK_COURSE_GROUP = "courseGroup";
	private static final String FORMLINK_SUB_RES = "subRes";
	private static final String FORMLINK_DELETE = "delete";
	private static final String PUB_RES_NAME_GLOBAL = "Global";
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

		List<String> filterSubTypes = getFilterSubTypes();
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
			if (isExcludedBySubType(filterSubTypes, row)
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
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NotificationSubscriptionCols.subType));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NotificationSubscriptionCols.courseGroup));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NotificationSubscriptionCols.subRes));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NotificationSubscriptionCols.addDesc));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NotificationSubscriptionCols.statusToggle));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(adminColumns, NotificationSubscriptionCols.creationDate));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(adminColumns, NotificationSubscriptionCols.lastEmail));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NotificationSubscriptionCols.deleteLink));

		subscriptionsTableDataModel = new NotificationSubscriptionTableDataModel(tableColumnModel);

		tableEl = uifactory.addTableElement(getWindowControl(), "NotificationSubscriptionTable", subscriptionsTableDataModel, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);

		updateSubscriptionsDataModel();
		initFiltersPresets(ureq);
		initFilters();

		tableEl.setExportEnabled(true);
	}


	private NotificationSubscriptionRow forgeRow(Subscriber sub) {
		Publisher pub = sub.getPublisher();
		String subType = !pub.getResId().equals(0L)
				? NewControllerFactory.translateResourceableTypeName(pub.getResName(), getLocale())
				: NewControllerFactory.translateResourceableTypeName(PUB_RES_NAME_GLOBAL, getLocale());
		FormLink courseGroup = uifactory.addFormLink("courseGroup_" + sub.getKey().toString(), FORMLINK_COURSE_GROUP, "", null, flc, Link.NONTRANSLATED);
		FormLink subRes = uifactory.addFormLink("subRes_" + sub.getKey().toString(), FORMLINK_SUB_RES, "", null, flc, Link.NONTRANSLATED);
		String addDesc = "";
		FormToggle statusToggle = uifactory.addToggleButton("subscription_" + sub.getKey().toString(), "", "&nbsp;&nbsp;", flc, null, null);
		statusToggle.addActionListener(FormEvent.ONCHANGE);
		FormLink deleteLink = null;

		if (!pub.getResName().equals(PUB_RES_NAME_GLOBAL)) {
			deleteLink = uifactory.addFormLink("delete_" + sub.getKey().toString(), FORMLINK_DELETE, "table.column.delete.action", null, flc, Link.LINK);
		}

		NotificationsHandler handler = notificationsManager.getNotificationsHandler(pub);
		String title = "";
		String iconCss = "";

		if (sub.isEnabled()) {
			statusToggle.toggleOn();
		} else {
			statusToggle.toggleOff();
		}

		if (handler != null) {
			title = handler.getDisplayName(pub);
			iconCss = handler.getIconCss();
			// If resId is 0L that means, publisher is of subType global
			if (pub.getResId().equals(0L)) {
				addDesc = handler.getAdditionalDescriptionI18nKey(getLocale());
			}
		}

		courseGroup.setI18nKey(title);
		if (!title.equals("-")) {
			courseGroup.setIconLeftCSS(CSSHelper.getIconCssClassFor(RepositoyUIFactory.getIconCssClass(pub.getResName())));
		}

		String resourceType = NewControllerFactory.translateResourceableTypeName(pub.getType(), getLocale());
		subRes.setI18nKey(resourceType);
		subRes.setIconLeftCSS(iconCss);

		NotificationSubscriptionRow row = new NotificationSubscriptionRow(subType, courseGroup, subRes, addDesc, statusToggle,
				sub.getCreationDate(), sub.getLatestEmailed(), deleteLink, sub.getKey());
		courseGroup.setUserObject(row);
		subRes.setUserObject(row);
		statusToggle.setUserObject(row);
		if (deleteLink != null) {
			deleteLink.setUserObject(row);
		}

		return row;
	}

	private List<String> getSubscriptionDistincTypes() {
		return subscriptionRows.stream()
				.map(NotificationSubscriptionRow::getSubType)
				.distinct()
				.toList();
	}

	protected final void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(3);

		allTab = FlexiFiltersTabFactory.tabWithFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.clear, List.of());
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);

		for (String subType : getSubscriptionDistincTypes()) {
			FlexiFiltersTab filterType = FlexiFiltersTabFactory.tabWithFilters(subType, subType,
					TabSelectionBehavior.clear, List.of());
			tabs.add(filterType);
		}

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}

	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(1);

		if (!subscriptionRows.isEmpty()) {
			SelectionValues subTypeValues = new SelectionValues();

			for (String subType : getSubscriptionDistincTypes()) {
				subTypeValues.add(SelectionValues.entry(subType, subType));
			}

			FlexiTableMultiSelectionFilter subTypeFilter = new FlexiTableMultiSelectionFilter(translate("table.column.sub.type"),
					"subType", subTypeValues, true);
			filters.add(subTypeFilter);
		}

		tableEl.setFilters(true, filters, false, false);
		tableEl.expandFilters(true);
	}

	private List<String> getFilterSubTypes() {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		FlexiTableFilter subTypeFilter = FlexiTableFilter.getFilter(filters, "subType");
		if (subTypeFilter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter) subTypeFilter).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				return filterValues;
			}
		}
		return null;
	}

	private boolean isExcludedBySubType(List<String> filterSubTypes, NotificationSubscriptionRow row) {
		if (filterSubTypes != null && !filterSubTypes.contains(row.getSubType())) {
			return true;
		}
		if ((tableEl.getSelectedFilterTab() != null
				&& !tableEl.getSelectedFilterTab().getId().equals(ALL_TAB_ID)
				&& !tableEl.getSelectedFilterTab().getId().equals(row.getSubType()))) {
			return true;
		}
		return false;
	}

	private boolean isExcludedBySearchString(String searchString, NotificationSubscriptionRow row) {
		boolean excluded = true;
		if (row.getSubType().contains(searchString)) {
			excluded = false;
		} else if (row.getCourseGroup().getI18nKey().contains(searchString)) excluded = false;
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
		}
		if (source instanceof FormToggle toggle) {
			Long subscriptionKey = Long.parseLong(toggle.getComponent().getComponentName().replaceAll(".+?_", ""));
			Subscriber subscriber = notificationsManager.getSubscriber(subscriptionKey);
			subscriber.setLastModified(new Date());
			subscriber.setEnabled(toggle.isOn());
		}
		if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if (cmd.equals(FORMLINK_COURSE_GROUP) || cmd.equals(FORMLINK_SUB_RES) || cmd.equals(FORMLINK_DELETE)) {
				Long subscriptionKey = Long.parseLong(link.getComponent().getComponentName().replaceAll(".+?_", ""));
				Subscriber subscriber = notificationsManager.getSubscriber(subscriptionKey);
				if (FORMLINK_COURSE_GROUP.equals(cmd)) {
					doLaunchSubscriptionResource(ureq, subscriber);
				}
				if (FORMLINK_SUB_RES.equals(cmd)) {
					if (subscriber.getPublisher().getType().equals("LearningRes")) {
						// TODO do nothing?
					} else {
						doLaunchSubscriptionResource(ureq, subscriber);
					}
				}
				if (FORMLINK_DELETE.equals(cmd)) {
					delYesNoC = activateYesNoDialog(ureq, null, translate("confirm.delete"), delYesNoC);
					delYesNoC.setUserObject(subscriber);
				}
			}
		}

		super.formInnerEvent(ureq, source, event);
	}

	private void doLaunchSubscriptionResource(UserRequest ureq, Subscriber subscriber) {
		NotificationUIFactory.launchSubscriptionResource(ureq, getWindowControl(), subscriber);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == delYesNoC) {
			if (DialogBoxUIFactory.isYesEvent(event)) { // ok
				// Remove subscription and update data model
				Subscriber sub = (Subscriber) delYesNoC.getUserObject();
				notificationsManager.unsubscribe(sub);
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
