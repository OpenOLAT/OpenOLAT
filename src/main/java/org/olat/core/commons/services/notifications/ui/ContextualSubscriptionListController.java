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
package org.olat.core.commons.services.notifications.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.core.commons.services.help.HelpLinkSPI;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.commons.services.notifications.NotificationHelper;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionListDataModel.ContextualSubscriptionListCols;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Aug 26, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ContextualSubscriptionListController extends FormBasicController implements FlexiTableComponentDelegate {

	private static final String TAB_SEVEN_DAY = "seven_days";
	private static final String TAB_FOUR_WEEKS = "four_weeks";
	private static final String TAB_SIX_MONTHS = "six_months";

	// default filter value, last seven days
	private Date selectedFilterDate = DateUtils.addDays(new Date(), -7);
	private Map<Subscriber, SubscriptionInfo> subsInfoMap;

	private FormToggle subscribeToggle;
	private Subscriber subscriber;
	private final SubscriptionContext subscriptionContext;
	private final PublisherData publisherData;

	private FlexiTableElement tableEl;
	private ContextualSubscriptionListDataModel dataModel;

	private CloseableCalloutWindowController toolsCalloutCtrl;

	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private HelpModule helpModule;

	protected ContextualSubscriptionListController(UserRequest ureq, WindowControl wControl,
												   SubscriptionContext subscriptionContext, PublisherData publisherData) {
		super(ureq, wControl, "con_subs_overview");
		this.subscriber = notificationsManager.getSubscriber(getIdentity(), subscriptionContext);
		this.subscriptionContext = subscriptionContext;
		this.publisherData = publisherData;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		subscribeToggle = uifactory.addToggleButton("command.subscribe", "command.subscribe", null, null, flc);
		subscribeToggle.addActionListener(FormEvent.ONCHANGE);
		flc.put("toggle", subscribeToggle.getComponent());

		HelpLinkSPI provider = helpModule.getManualProvider();
		Component helpPageLink = provider.getHelpPageLink(ureq, translate("help"), translate("command.subscribe"),
				"o_icon o_icon-lg o_icon_help", "o_chelp", "manual_user/personal_menu/Personal_Tools/#subscriptions");
		flc.put("helpLink", helpPageLink);

		FormLink toolsLink = uifactory.addFormLink("tools_Link", "tools", "", null, flc, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon-lg o_icon_actions o_icon-fws");
		flc.put("toolsLink", toolsLink.getComponent());

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ContextualSubscriptionListCols.description));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ContextualSubscriptionListCols.iconCssClass));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ContextualSubscriptionListCols.date));

		dataModel = new ContextualSubscriptionListDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table.con.subs", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setPageSize(10);
		tableEl.setCssDelegate(dataModel);
		tableEl.setCustomizeColumns(true);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setShowSmallPageSize(false);
		flc.contextPut("table", tableEl);
		VelocityContainer row = createVelocityContainer("con_subs_list");
		row.setLayout("vertical");
		row.setDomReplacementWrapperRequired(false);

		tableEl.setRowRenderer(row, this);
		tableEl.setAndLoadPersistedPreferences(ureq, "notifications-subs-list");

		flc.put("table", tableEl.getComponent());

		if (subscriber == null || !subscriber.isEnabled()) {
			subscribeToggle.toggleOff();
		} else {
			subscribeToggle.toggleOn();
			loadModel();
			toggleFilterTabs(ureq);
		}
		initEmptyTableSettings();
	}

	private void loadModel() {
		subscriber = notificationsManager.getSubscriber(getIdentity(), subscriptionContext);

		if (subscriber != null) {
			subsInfoMap = initSubsInfoMap(selectedFilterDate);

			List<ContextualSubscriptionListRow> rows = new ArrayList<>();
			if (subsInfoMap.get(subscriber) != null && subscriber.isEnabled()) {
				List<SubscriptionListItem> subscriptionListItems = subsInfoMap.get(subscriber).getSubscriptionListItems();

				for (SubscriptionListItem subscriptionListItem : subscriptionListItems) {
					ContextualSubscriptionListRow row =
							new ContextualSubscriptionListRow(subscriptionListItem.getDescription(), subscriptionListItem.getIconCssClass(),
									subscriptionListItem.getLink(), subscriptionListItem.getDate(), getLocale());

					rows.add(row);
				}
			}
			dataModel.setObjects(rows);
		}
		tableEl.reset(false, true, true);
	}

	private void initEmptyTableSettings() {
		if (!subscribeToggle.isOn()) {
			tableEl.setEmptyTableSettings("subs.list.empty.message", null,
					"o_icon_notification", "command.subscribe",
					"o_icon-bell", false);
		} else {
			tableEl.setEmptyTableSettings("subs.list.empty.message", null, "o_icon_notification");
		}
	}

	private Map<Subscriber, SubscriptionInfo> initSubsInfoMap(Date compareDate) {
		if (subscriber != null) {
			return NotificationHelper.getSubscriptionMap(getLocale(), true,
					compareDate, Collections.singletonList(subscriber));
		}
		return Collections.emptyMap();
	}

	private boolean isSubSInfoMapEmpty() {
		if (initSubsInfoMap(DateUtils.addWeeks(new Date(), -4)).isEmpty()) {
			return true;
		} else return initSubsInfoMap(DateUtils.addMonth(new Date(), -6)).isEmpty();
	}

	private void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();

		FlexiFiltersTab tabSevenDays = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_SEVEN_DAY,
				translate("con.subs.list.tab.seven.days"),
				TabSelectionBehavior.reloadData,
				List.of());
		tabs.add(tabSevenDays);

		FlexiFiltersTab tabFourWeeks = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_FOUR_WEEKS,
				translate("con.subs.list.tab.four.weeks"),
				TabSelectionBehavior.reloadData,
				List.of());
		tabs.add(tabFourWeeks);

		FlexiFiltersTab tabSixMonths = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_SIX_MONTHS,
				translate("con.subs.list.tab.six.months"),
				TabSelectionBehavior.reloadData,
				List.of());
		tabs.add(tabSixMonths);

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabSevenDays);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void toggleFilterTabs(UserRequest ureq) {
		if (dataModel != null && !isSubSInfoMapEmpty() && subscribeToggle.isOn()) {
			initFilterTabs(ureq);
		} else {
			tableEl.setFilterTabs(true, Collections.emptyList());
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == subscribeToggle) {
			if (subscribeToggle.isOn()) {
				notificationsManager.subscribe(getIdentity(), subscriptionContext, publisherData);
			} else {
				notificationsManager.unsubscribe(getIdentity(), subscriptionContext);
				dataModel.setObjects(Collections.emptyList());
			}
			loadModel();
			toggleFilterTabs(ureq);
			initEmptyTableSettings();
			fireEvent(ureq, event);
		} else if (source == tableEl) {
			if (event instanceof FlexiTableFilterTabEvent selectedFilter) {
				if (selectedFilter.getTab().getId().equals(TAB_SEVEN_DAY)) {
					selectedFilterDate = DateUtils.addDays(new Date(), -7);
				} else if (selectedFilter.getTab().getId().equals(TAB_FOUR_WEEKS)) {
					selectedFilterDate = DateUtils.addWeeks(new Date(), -4);
				} else if (selectedFilter.getTab().getId().equals(TAB_SIX_MONTHS)) {
					selectedFilterDate = DateUtils.addMonth(new Date(), -6);
				}
				loadModel();
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				subscribeToggle.toggleOn();
				notificationsManager.subscribe(getIdentity(), subscriptionContext, publisherData);
				toggleFilterTabs(ureq);
				initEmptyTableSettings();
				fireEvent(ureq, event);
			}
		} else if (source instanceof FormLink link) {
			if (link.getCmd().equals("tools")) {
				doOpenTools(ureq, link.getFormDispatchId());
			}
		}
	}

	private void doOpenTools(UserRequest ureq, String dispatchID) {
		ToolsController toolsCtrl = new ToolsController(ureq, getWindowControl());
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), dispatchID, "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		return null;
	}

	private void launchPersonalNotifications(UserRequest ureq) {
		String businessPath = "[HomeSite:" + getIdentity().getKey() + "][notifications:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

	private class ToolsController extends BasicController {

		private final Link jumpSubLink;

		protected ToolsController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl);

			VelocityContainer mainVC = createVelocityContainer("tools");

			List<String> links = new ArrayList<>();
			mainVC.contextPut("links", links);

			jumpSubLink = LinkFactory.createCustomLink("personal.notifications", "jump", "personal.notifications", Link.LINK, mainVC, this);
			String businessPath = "[HomeSite:" + getIdentity().getKey() + "][notifications:0]";
			String urlFromBusinessPathString = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(businessPath);
			if (urlFromBusinessPathString != null) {
				jumpSubLink.setUrl(urlFromBusinessPathString);
			}
			links.add(jumpSubLink.getComponentName());

			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if (source == jumpSubLink) {
				closeAndCleanup();
				launchPersonalNotifications(ureq);
				fireEvent(ureq, event);
			}
		}

		private void closeAndCleanup() {
			toolsCalloutCtrl.deactivate();
			removeAsListenerAndDispose(toolsCalloutCtrl);
			toolsCalloutCtrl = null;
		}
	}
}
