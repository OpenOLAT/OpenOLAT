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
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.core.commons.services.notifications.NotificationHelper;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
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
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
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

	private FlexiTableElement tableEl;
	private ContextualSubscriptionListDataModel dataModel;

	private List<Subscriber> subscribers;
	private List<Subscription> subscriptions;
	private final List<PublisherDecorated> publishers;

	private CloseableCalloutWindowController toolsCalloutCtrl;

	@Autowired
	private NotificationsManager notificationsManager;

	protected ContextualSubscriptionListController(UserRequest ureq, WindowControl wControl, List<PublisherDecorated> publishers) {
		super(ureq, wControl, "con_subs_overview");
		this.publishers = List.copyOf(publishers);
		
		List<Publisher> list = publishers.stream()
				.map(PublisherDecorated::publisher)
				.toList();
		subscribers = notificationsManager.getSubscribers(getIdentity(), list);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Map<Publisher,Subscriber> pubSubs = subscribers.stream()
				.collect(Collectors.toMap(Subscriber::getPublisher, s -> s, (u, v) -> u));
		
		subscriptions = new ArrayList<>();
		for(PublisherDecorated labelledPublisher:publishers) {
			Subscriber subscriber = pubSubs.get(labelledPublisher.publisher());
			initSubscribtion(labelledPublisher, subscriber);
		}
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("toggles", subscriptions);
		}

		FormLink toolsLink = uifactory.addFormLink("toolsLink", "tools", "", null, flc, Link.NONTRANSLATED);
		toolsLink.setTitle(translate("action.more"));
		toolsLink.setIconLeftCSS("o_icon o_icon-lg o_icon_actions o_icon-fws");

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

		loadModel();
		toggleFilterTabs(ureq);
		initEmptyTableSettings();
	}
	
	private void initSubscribtion(PublisherDecorated labelledPublisher, Subscriber subscriber) {
		Publisher publisher = labelledPublisher.publisher();
		if(subscriber == null) {
			subscriber = notificationsManager.createDisabledSubscriberIfAbsent(getIdentity(), publisher);
		}
		
		FormToggle subscribeToggle = uifactory.addToggleButton("command.subscribe." + publisher.getKey(), "command.subscribe", translate("on"), translate("off"), flc);
		subscribeToggle.addActionListener(FormEvent.ONCHANGE);
		subscribeToggle.toggle(subscriber != null && subscriber.isEnabled());
		String name = labelledPublisher.translated()
				? labelledPublisher.subscribeI18nLabel()
				: translate(labelledPublisher.subscribeI18nLabel());
		Subscription subscription = new Subscription(subscribeToggle, name, publisher, subscriber);
		subscribeToggle.setUserObject(subscription);
		subscriptions.add(subscription);
	}

	private void loadModel() {
		List<ContextualSubscriptionListRow> rows = new ArrayList<>();
		for(Subscriber subscriber:subscribers) {
			Map<Subscriber, SubscriptionInfo> subsInfoMap = initSubsInfoMap(selectedFilterDate);
			if (subsInfoMap.get(subscriber) != null) {
				List<SubscriptionListItem> subscriptionListItems = subsInfoMap.get(subscriber).getSubscriptionListItems();
	
				for (SubscriptionListItem subscriptionListItem : subscriptionListItems) {
					ContextualSubscriptionListRow row =
							new ContextualSubscriptionListRow(subscriptionListItem.getDescription(), subscriptionListItem.getIconCssClass(),
									subscriptionListItem.getLink(), subscriptionListItem.getDate(), getLocale());
	
					rows.add(row);
				}
			}
		}
		dataModel.setObjects(rows);
		tableEl.reset(false, true, true);
	}

	private void initEmptyTableSettings() {
		tableEl.setEmptyTableSettings("subs.list.empty.message", null, "o_icon_notification");
	}

	private Map<Subscriber, SubscriptionInfo> initSubsInfoMap(Date compareDate) {
		if (subscribers != null && !subscribers.isEmpty()) {
			return NotificationHelper.getSubscriptionMap(getLocale(), true,
					compareDate, subscribers);
		}
		return Collections.emptyMap();
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
		initFilterTabs(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormToggle subscribeToggle && subscribeToggle.getUserObject() instanceof Subscription subscription) {
			doToggleSubscription(subscribeToggle, subscription);
			loadModel();
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
				for(Subscription subscription:subscriptions) {
					subscription.subscribeToggle().toggleOn();
					Subscriber subscriber = notificationsManager.subscribe(getIdentity(), subscription.publisher());
					subscription.subscribeToggle().setUserObject(new Subscription(subscription.subscribeToggle(),
							subscription.name(), subscription.publisher(), subscriber));
				}

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
	
	private void doToggleSubscription(FormToggle subscribeToggle, Subscription subscription) {
		Subscriber subscriber = subscription.subscriber();
		Publisher publisher = subscription.publisher();
		
		if (subscribeToggle.isOn()) {
			if (subscriber != null) {
				notificationsManager.updateSubscriber(subscriber, true); // enable it
				subscriber = notificationsManager.getSubscriber(getIdentity(), subscription.publisher());
			} else {
				subscriber = notificationsManager.subscribe(getIdentity(), subscription.publisher()); // fallback
			}
			
			// Enable automatically all sub-contexts if the root context is enabled
			for(Subscription otherSubscription:subscriptions) {
				if(otherSubscription.publisher().getParentPublisher() != null
						&& publisher.equals(otherSubscription.publisher().getParentPublisher())) {
					Subscriber otherSubscriber = otherSubscription.subscriber();
					if(otherSubscriber == null || !otherSubscriber.isEnabled()) {
						otherSubscriber = notificationsManager.subscribe(getIdentity(), otherSubscription.publisher());
						otherSubscription.subscribeToggle().setUserObject(new Subscription(otherSubscription.subscribeToggle(),
								otherSubscription.name(), otherSubscription.publisher(), otherSubscriber));
						otherSubscription.subscribeToggle().toggleOn();
					}
				}
			}
		} else {
			if (subscriber != null) {
				notificationsManager.updateSubscriber(subscriber, false); // disable it
			} else {
				// should not happen, but safe fallback
				notificationsManager.unsubscribe(getIdentity(), subscription.publisher());
			}
		}
		
		subscribeToggle.setUserObject(new Subscription(subscribeToggle, subscription.name(), subscription.publisher(), subscriber));
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
	
	public record Subscription(FormToggle subscribeToggle, String name, Publisher publisher, Subscriber subscriber) {
		
		public String getComponentName() {
			return subscribeToggle.getComponent().getComponentName();
		}
	}
}
