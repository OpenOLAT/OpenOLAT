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
package org.olat.modules.webFeed.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.controllers.navigation.Dated;
import org.olat.core.commons.controllers.navigation.NavigationEvent;
import org.olat.core.commons.controllers.navigation.YearNavigationController;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.commons.services.tag.ui.component.FlexiTableTagFilter;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.date.DateComponentFactory;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableTextFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.rating.RatingFormItem;
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
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.LockResult;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.portfolio.ui.wizard.CollectArtefactController;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.FeedSecurityCallback;
import org.olat.modules.webFeed.FeedViewHelper;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.modules.webFeed.model.ItemImpl;
import org.olat.modules.webFeed.portfolio.BlogEntryMedia;
import org.olat.modules.webFeed.portfolio.BlogEntryMediaHandler;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Mai 21, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class FeedItemListController extends FormBasicController implements FlexiTableComponentDelegate {

	private List<Item> feedItems;
	private List<FeedItemRow> itemRows;
	private Item currentItem;
	private Feed feedRss;
	private List<Long> filteredItemKeys;

	private LockResult lock;
	private final FeedSecurityCallback feedSecCallback;
	private final FeedUIFactory feedUIFactory;
	private final VelocityContainer vcMain;
	private final FormLayoutContainer rightColFlc;
	private final FormLayoutContainer itemFlc;
	private final FormLayoutContainer customItemFlc;
	private final VelocityContainer vcInfo;
	private final FeedItemDisplayConfig displayConfig;
	private final FeedViewHelper helper;
	private FormLink bulkDeleteButton;
	private FormToggle toggleTimelineTags;

	private FlexiTableElement tableEl;
	private FeedItemTableModel tableModel;

	private final YearNavigationController naviCtrl;
	private FormBasicController itemFormCtrl;
	private FeedItemController feedItemCtrl;
	private CloseableModalController cmc;
	private DialogBoxController deleteDialogBoxCtrl;
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private CollectArtefactController collectorCtrl;


	@Autowired
	private UserManager userManager;
	@Autowired
	private FeedManager feedManager;
	@Autowired
	private PortfolioV2Module portfolioModule;
	@Autowired
	private BlogEntryMediaHandler blogMediaHandler;
	@Autowired
	private CommentAndRatingService commentAndRatingService;


	/**
	 * ListCtrl is part of the whole view of feeds, on top is the vcInfo (info/metadata), below is the table (this ctrl)
	 * On the right side is the yearNavCtrl. All three parts are combined in vcMain (FeedMainCtrl)
	 *
	 * @param ureq
	 * @param wControl
	 * @param feed
	 * @param feedSecCallback
	 * @param feedUIFactory
	 * @param vcMain
	 * @param vcInfo
	 * @param displayConfig
	 * @param helper
	 */
	protected FeedItemListController(UserRequest ureq, WindowControl wControl, Feed feed,
									 FeedSecurityCallback feedSecCallback, FeedUIFactory feedUIFactory,
									 VelocityContainer vcMain, VelocityContainer vcInfo,
									 FeedItemDisplayConfig displayConfig, FeedViewHelper helper) {
		super(ureq, wControl, "feed_overview");
		// using because each feed type has its own translations
		setTranslator(feedUIFactory.getTranslator());
		this.feedRss = feed;
		this.feedItems = feedManager.loadFilteredAndSortedItems(feed, filteredItemKeys, feedSecCallback, ureq.getIdentity());
		this.feedSecCallback = feedSecCallback;
		this.feedUIFactory = feedUIFactory;
		this.vcMain = vcMain;
		this.vcInfo = vcInfo;
		this.displayConfig = displayConfig;
		this.helper = helper;

		String rightColPage = velocity_root + "/right_column.html";
		rightColFlc = FormLayoutContainer.createCustomFormLayout("right_column", getTranslator(), rightColPage);

		String itemPage = feedUIFactory.getItemPagePath();
		itemFlc = FormLayoutContainer.createCustomFormLayout("items", getTranslator(), itemPage);
		itemFlc.setRootForm(mainForm);

		String customItemPage = feedUIFactory.getCustomItemsPagePath();
		customItemFlc = FormLayoutContainer.createCustomFormLayout("feed_entries", getTranslator(), customItemPage);
		customItemFlc.setRootForm(mainForm);

		customItemFlc.contextPut("helper", helper);
		customItemFlc.contextPut("feed", feedRss);
		customItemFlc.contextPut("callback", feedSecCallback);

		naviCtrl = new YearNavigationController(ureq, wControl, getTranslator(), feedItems);
		listenTo(naviCtrl);
		if (displayConfig == null) {
			displayConfig = new FeedItemDisplayConfig(true, true, true);
		}
		if (displayConfig.isShowDateNavigation()) {
			rightColFlc.put("navi", naviCtrl.getInitialComponent());
		}

		initForm(ureq);
		initMultiSelectionTools(flc);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add(itemFlc);
		formLayout.add(customItemFlc);
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FeedItemTableModel.ItemsCols.title, "openEntry"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FeedItemTableModel.ItemsCols.status, new FeedItemStatusRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FeedItemTableModel.ItemsCols.publishDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FeedItemTableModel.ItemsCols.author));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FeedItemTableModel.ItemsCols.tags));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FeedItemTableModel.ItemsCols.changedFrom));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FeedItemTableModel.ItemsCols.rating.i18nHeaderKey(), FeedItemTableModel.ItemsCols.rating.ordinal(), true, FeedItemTableModel.ItemsCols.rating.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FeedItemTableModel.ItemsCols.comments));

		if (feedRss.isInternal() && feedSecCallback.mayEditItems()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.feed.header.actions", FeedItemTableModel.ItemsCols.toolsLink.ordinal(), "tools",
					new ToolsCellRenderer(translate("feed.item.tool.actions"), "tools")));
		}

		uifactory.addSpacerElement("spacer", formLayout, false);
		tableModel = new FeedItemTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 10, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setCssDelegate(tableModel);
		tableEl.setCustomizeColumns(true);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		customItemFlc.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(customItemFlc.getFormItemComponent(), this);
		tableEl.setAndLoadPersistedPreferences(ureq, "feed-item-list");
		tableEl.setSearchEnabled(true);

		if (feedRss.isInternal() && feedSecCallback.mayCreateItems()) {
			// add new entries only for internal feeds
			FormLink addEntry = uifactory.addFormLink("feed.add.item", "feed.add.item", "feed.add.item", "", formLayout, Link.BUTTON);
			addEntry.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
			addEntry.setElementCssClass("o_sel_feed_add_item");
			tableEl.setEmptyTableSettings("table.empty.message", null, null, "feed.add.item", "o_icon_add", false);
		} else {
			uifactory.addSpacerElement("spacer.external", formLayout, true);
		}

		loadModel(ureq);
		initFilterTabs(ureq);
		initFilters();

		if (!itemRows.isEmpty()) {
			toggleTimelineTags = uifactory.addToggleButton("toggle.timeline.tags", "toggle.timeline.tags", null, null, formLayout);
			toggleTimelineTags.addActionListener(FormEvent.ONCHANGE);
			// Persist the toggle setting per user as guiPref
			Boolean isToggleOn = (Boolean) ureq.getUserSession().getGuiPreferences().get(FeedItemListController.class, "timeline-tags-toggle");
			boolean toggleOn = isToggleOn != null && isToggleOn;
			toggleTimelineTags.toggle(toggleOn);
			// add to info.html
			vcInfo.put("toggle", toggleTimelineTags.getComponent());
			if (toggleOn) {
				// if toggle is on, then add the right column (yearNavCtrl) to the main view
				vcMain.put("rightColumn", rightColFlc.getFormItemComponent());
			}
		}
	}

	protected void initMultiSelectionTools(FormLayoutContainer formLayout) {
		bulkDeleteButton = uifactory.addFormLink("bulk.delete", "delete", "delete", formLayout, Link.BUTTON);
		bulkDeleteButton.setIconLeftCSS("o_icon o_icon-fw o_icon_trash");
		tableEl.addBatchButton(bulkDeleteButton);
	}

	public void loadModel(UserRequest ureq) {
		itemRows = new ArrayList<>();

		feedItems = feedManager.loadFilteredAndSortedItems(feedRss, filteredItemKeys, feedSecCallback, ureq.getIdentity());

		for (Item item : feedItems) {
			Long commentsAmount = commentAndRatingService.countComments(item.getFeed(), item.getGuid());
			FormLink commentLink = uifactory.addFormLink("comments_" + item.getGuid(), "openEntry", String.valueOf(commentsAmount), null, null, Link.NONTRANSLATED);
			commentLink.setIconLeftCSS("o_icon o_icon_comments_none o_icon-lg");

			FormLink feedEntryLink = uifactory.addFormLink("title_" + item.getGuid(), "openEntry", item.getTitle(), null, null, Link.NONTRANSLATED);
			FeedItemRow row = new FeedItemRow(item, feedEntryLink, commentLink, getTranslator());
			// add Date component
			if (item.getDate() != null) {
				DateComponentFactory.createDateComponentWithYear("dateComp." + item.getGuid(), item.getDate(), customItemFlc.getFormItemComponent());
			}

			feedEntryLink.setUserObject(row);
			commentLink.setUserObject(row);

			forgeRatings(row);
			if (feedRss.isInternal()) {
				createButtonsForFeedItem(feedRss, row);
			}

			itemRows.add(row);
		}

		applyFilters(itemRows);
		itemRows = itemRows.stream().sorted(Comparator.comparing(i -> i.getItem().getLastModified(), Comparator.nullsLast(Comparator.reverseOrder()))).toList();

		tableModel.setObjects(itemRows);
		tableEl.reset(false, true, true);
	}

	private void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();

		FlexiFiltersTab allTab = FlexiFiltersTabFactory.tabWithFilters("all", translate("filter.all"),
				TabSelectionBehavior.clear, List.of());
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);

		FlexiFiltersTab myItemsTab = FlexiFiltersTabFactory.tabWithImplicitFilters("owned", translate("filter.my.entries"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(FeedItemFilter.OWNED, "owned")));
		tabs.add(myItemsTab);

		FlexiFiltersTab draftTab = FlexiFiltersTabFactory.tabWithImplicitFilters("drafts", translate("filter.drafts"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(FeedItemFilter.STATUS, FeedItemStatusEnum.draft.name())));
		tabs.add(draftTab);

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}

	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();

		if (tableEl.getSelectedFilterTab().getId().equals("all")) {
			SelectionValues myEntriesValues = new SelectionValues();
			myEntriesValues.add((SelectionValues.entry("owned", translate("table.filter.my.entries"))));
			FlexiTableOneClickSelectionFilter myEntriesFilter = new FlexiTableOneClickSelectionFilter(translate("table.filter.my.entries"),
					FeedItemFilter.OWNED.name(), myEntriesValues, true);
			filters.add(myEntriesFilter);
		}

		// TODO: Add Tag values
		FlexiTableTagFilter tagsFilter = new FlexiTableTagFilter(translate("table.filter.tags"),
				FeedItemFilter.TAGS.name(), Collections.emptyList(), true);
		filters.add(tagsFilter);

		FlexiTableTextFilter authorFilter = new FlexiTableTextFilter(translate("table.filter.author"),
				FeedItemFilter.AUTHORS.name(), true);
		filters.add(authorFilter);

		FlexiTableDateRangeFilter publishDateFilter = new FlexiTableDateRangeFilter(translate("table.filter.publish.date"),
				FeedItemFilter.PUBLISHDATE.name(), true, true, translate("table.filter.publish.date.from"),
				translate("table.filter.publish.date.to"), getLocale());
		filters.add(publishDateFilter);

		SelectionValues statusValues = new SelectionValues();
		List<FeedItemRow> feedItemRows = tableModel.getObjects();
		List<FeedItemStatusEnum> feedItemStatusEnumList = feedItemRows.stream().map(FeedItemRow::getStatus).toList().stream().distinct().toList();

		for (FeedItemStatusEnum statusEnum : feedItemStatusEnumList) {
			statusValues.add(SelectionValues.entry(statusEnum.name(), translate("feed.item." + statusEnum.name())));
		}

		FlexiTableMultiSelectionFilter statusFilter = new FlexiTableMultiSelectionFilter(translate("table.filter.status"),
				FeedItemFilter.STATUS.name(), statusValues, true);
		filters.add(statusFilter);

		tableEl.setFilters(true, filters, false, false);
		tableEl.expandFilters(false);
	}

	private void applyFilters(List<FeedItemRow> itemRows) {
		String quickSearchString = tableEl.getQuickSearchString().toLowerCase();
		if (StringHelper.containsNonWhitespace(quickSearchString)) {
			itemRows.removeIf(r -> !r.getItem().getTitle().toLowerCase().contains(quickSearchString)
					&& (r.getStatus() == null || !r.getStatus().name().toLowerCase().contains(quickSearchString))
					&& (r.getAuthor() == null || !r.getAuthor().toLowerCase().contains(quickSearchString)));
		}


		List<FlexiTableFilter> filters = tableEl.getFilters();

		for (FlexiTableFilter filter : filters) {
			if (FeedItemFilter.OWNED.name().equals(filter.getFilter())) {
				String value = filter.getValue();
				filteredItemKeys = new ArrayList<>();
				if (value != null && value.equals("owned")) {
					itemRows.removeIf(r -> !r.getAuthor().equals(UserManager.getInstance().getUserDisplayName(getIdentity().getUser())));
				}
			}
			if (FeedItemFilter.TAGS.name().equals(filter.getFilter())) {
				List<String> values = ((FlexiTableTagFilter) filter).getValues();
				if (values != null && !values.isEmpty()) {
					Set<Long> selectedTagKeys = values.stream().map(Long::valueOf).collect(Collectors.toSet());
					// TODO: Tag Impl
				}
			}
			if (FeedItemFilter.AUTHORS.name().equals(filter.getFilter())) {
				String value = filter.getValue();
				if (StringHelper.containsNonWhitespace(value)) {
					String valueLowerCase = value.toLowerCase();
					itemRows.removeIf(row -> !row.getAuthor().toLowerCase().contains(valueLowerCase));
				}
			}
			if (FeedItemFilter.PUBLISHDATE.name().equals(filter.getFilter())) {
				FlexiTableDateRangeFilter.DateRange dateRange = ((FlexiTableDateRangeFilter) filter).getDateRange();
				if (dateRange != null) {
					Date filterStart = DateUtils.setTime(dateRange.getStart(), 0, 0, 0);
					if (filterStart != null) {
						itemRows.removeIf(row -> row.getPublishDate() == null || !filterStart.before(row.getPublishDate()));
					}
					Date filterEnd = DateUtils.setTime(dateRange.getEnd(), 23, 59, 59);
					if (filterEnd != null) {
						itemRows.removeIf(row -> row.getPublishDate() == null || !filterEnd.after(row.getPublishDate()));
					}
				}
			}
			if (FeedItemFilter.STATUS.name().equals(filter.getFilter())) {
				List<String> values = ((FlexiTableExtendedFilter) filter).getValues();
				if (values != null && !values.isEmpty()) {
					itemRows.removeIf(row -> !values.contains(row.getStatus().name()));
				}
			}
		}
	}

	private void forgeRatings(FeedItemRow itemRow) {
		List<UserRating> allRatings = commentAndRatingService.getAllRatings(itemRow.getItem().getFeed(), itemRow.getItem().getGuid());
		UserRating userRating = commentAndRatingService.getRating(getIdentity(), itemRow.getItem().getFeed(), itemRow.getItem().getGuid());
		double averageRating = userRating == null ? 0.0 : Math.round(allRatings.stream().map(UserRating::getRating).mapToDouble(a -> a).average().orElse(0.0) * 10.0) / 10.0;

		float averageRatingValue = (float) averageRating;
		RatingFormItem ratingCmp = uifactory.addRatingItem("rat_" + itemRow.getItem().getKey(), null, averageRatingValue, 5, false, null);
		itemRow.setRatingFormItem(ratingCmp);
		ratingCmp.setUserObject(itemRow);
	}

	private void doOpenTools(UserRequest ureq, Item feedItem, String dispatchID) {
		toolsCtrl = new ToolsController(ureq, getWindowControl(), feedItem);
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), dispatchID, "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}

	public void displayFeedItem(UserRequest ureq, Item feedItem) {
		// remove toggle for individual item view
		vcInfo.remove("toggle");

		Feed reloadedFeed = feedManager.loadFeed(feedItem.getFeed());
		feedItem = feedManager.loadItem(feedItem.getKey());
		if (feedItem != null) {
			feedItemCtrl = new FeedItemController(ureq, getWindowControl(), feedItem, reloadedFeed, helper, feedUIFactory, feedSecCallback,
					displayConfig, itemFlc.getFormItemComponent());
			listenTo(feedItemCtrl);
			vcMain.put("selected_feed_item", feedItemCtrl.getInitialComponent());
		}
	}

	private void createButtonsForFeedItem(Feed feed, FeedItemRow feedItemRow) {
		String guid = feedItemRow.getItem().getGuid();
		String toolsId = "o-tools-".concat(guid);
		FormLink toolsLink = uifactory.addFormLink(toolsId, "tools", "", null, customItemFlc, Link.NONTRANSLATED);
		toolsLink.setTitle(translate("feed.item.tool.actions"));
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
		toolsLink.setGhost(true);
		toolsLink.setUserObject(feedItemRow.getItem());
	}

	private void doOpenCollector(UserRequest ureq, Item feedItem) {
		String businessPath = BusinessControlFactory.getInstance()
				.getAsString(getWindowControl().getBusinessControl());
		businessPath += "[item=" + feedItem.getKey() + ":0]";
		BlogEntryMedia media = new BlogEntryMedia(feedRss, feedItem);

		collectorCtrl = new CollectArtefactController(ureq, getWindowControl(), media, blogMediaHandler, businessPath);
		collectorCtrl.addControllerListener(this);
		listenTo(collectorCtrl);

		String title = "Media";
		cmc = new CloseableModalController(getWindowControl(), null, collectorCtrl.getInitialComponent(), true, title, true);
		cmc.addControllerListener(this);
		cmc.activate();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	/**
	 * Private helper to remove any temp media files created for this feed
	 *
	 * @param tmpItem
	 */
	private void cleanupTmpItemMediaDir(Item tmpItem) {
		if(tmpItem == null) return;
		String guid = tmpItem.getGuid();
		if (guid == null) return;

		// delete the dir only if the item is not saved.
		Long key = tmpItem.getKey();
		if (key == null) {
			feedManager.deleteItem(tmpItem);
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		// reload feed for this event and make sure the updated feed object is
		// in the view
		feedRss = feedManager.loadFeed(feedRss);
		feedItems = feedManager.loadFilteredAndSortedItems(feedRss, filteredItemKeys, feedSecCallback, ureq.getIdentity());

		if (source == itemFormCtrl) {
			if (event.equals(Event.CHANGED_EVENT) || event.equals(Event.CANCELLED_EVENT)) {
				if (event.equals(Event.CHANGED_EVENT)) {
					FileElement mediaFile = currentItem.getMediaFile();
					if (feedManager.getItemContainer(currentItem) == null) {
						// Ups, deleted in the meantime by someone else
						// remove the item from the naviCtrl
						naviCtrl.remove(currentItem);
					} else {
						if (!feedItems.contains(currentItem)) {
							// Add the modified item if it is not part of the
							// feed
							feedRss = feedManager.createItem(feedRss, currentItem, mediaFile);
							if (feedRss != null) {
								// add it to the navigation controller
								naviCtrl.add(currentItem);
								feedItems = feedManager.loadFilteredAndSortedItems(feedRss, filteredItemKeys,
										feedSecCallback, ureq.getIdentity());
								if (feedItems != null && feedItems.size() == 1) {
									// Set the base URI of the feed for the
									// current user. All users
									// have unique URIs.
									helper.setURIs(currentItem.getFeed());
								}
							}
						} else {
							// Write item file
							currentItem = feedManager.updateItem(currentItem, mediaFile);
							ThreadLocalUserActivityLogger.log(FeedLoggingAction.FEED_ITEM_EDIT, getClass(),
									LoggingResourceable.wrap(currentItem));
						}
					}
					// update the view
					if (itemFormCtrl != null) {
						displayFeedItem(ureq, currentItem);
						itemFormCtrl.getInitialComponent().setDirty(true);
					}

				} else {
					// at Event.CANCELLED_EVENT -> Check if this item has ever been added to the feed.
					// If not, remove the temp dir
					cleanupTmpItemMediaDir(currentItem);
				}
				// release the lock
				feedManager.releaseLock(lock);

				// Dispose the cmc and the podcastFormCtr.
				cmc.deactivate();
				cleanUp();
			} else if (event.equals(Event.DONE_EVENT)) {
				cmc.deactivate();
				cleanUp();
			}
		} else if (source == cmc) {
			// In case the form is closed by X -> Check if this item has ever been added to the feed.
			// If not, remove the temp dir
			cleanupTmpItemMediaDir(currentItem);
		} else if (source == collectorCtrl && (event.equals(Event.DONE_EVENT) || event.equals(Event.CANCELLED_EVENT))) {
			cmc.deactivate();
			cleanUp();
		} else if (source == deleteDialogBoxCtrl && DialogBoxUIFactory.isYesEvent(event)) {
			// The user confirmed that the item(s) shall be deleted
			List<Item> feedItemsToDelete = (List<Item>) ((DialogBoxController) source).getUserObject();
			doDeleteFeedItem(feedItemsToDelete);
		} else if (source == feedItemCtrl) {
			if (event == Event.BACK_EVENT) {
				vcMain.remove("selected_feed_item");
				if (toggleTimelineTags != null) {
					vcInfo.put("toggle", toggleTimelineTags.getComponent());
				}
				removeAsListenerAndDispose(feedItemCtrl);
				feedItemCtrl = null;
			} else if (feedItemCtrl != null && event instanceof FeedItemEvent feedItemEvent) {
				if (Objects.equals(feedItemEvent.getCommand(), FeedItemEvent.EDIT_FEED_ITEM)) {
					doEditFeedItem(ureq, feedItemEvent.getItem());
				} else if (Objects.equals(feedItemEvent.getCommand(), FeedItemEvent.DELETE_FEED_ITEM)) {
					doConfirmDeleteFeedItem(ureq, List.of(feedItemEvent.getItem()));
				} else if (Objects.equals(feedItemEvent.getCommand(), FeedItemEvent.ARTEFACT_FEED_ITEM)) {
					doOpenCollector(ureq, feedItemEvent.getItem());
				}
			}
		} else if (source == naviCtrl && event instanceof NavigationEvent navEvent) {
			List<? extends Dated> selItems = navEvent.getSelectedItems();
			filteredItemKeys = new ArrayList<>();
			for (Dated selItem : selItems) {
				if (selItem instanceof Item item) {
					filteredItemKeys.add(item.getKey());
				}
			}
		}
		loadModel(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormToggle toggle) {
			if (toggle.isOn()) {
				ureq.getUserSession().getGuiPreferences().putAndSave(FeedItemListController.class, "timeline-tags-toggle", true);
				vcMain.put("rightColumn", rightColFlc.getFormItemComponent());
			} else {
				ureq.getUserSession().getGuiPreferences().putAndSave(FeedItemListController.class, "timeline-tags-toggle", false);
				vcMain.remove(rightColFlc.getFormItemComponent());
			}
		} else if (source instanceof FormLink link) {
			if (link.getCmd().equals("openEntry")) {
				FeedItemRow row = (FeedItemRow) link.getUserObject();
				displayFeedItem(ureq, row.getItem());
			} else if (link.getCmd().equals("feed.add.item")) {
				doAddFeedItem(ureq);
			} else if (source == bulkDeleteButton) {
				List<Item> items = tableEl.getMultiSelectedIndex().stream().map(i -> tableModel.getObject(i).getItem()).toList();
				doConfirmDeleteFeedItem(ureq, items);
			} else if (link.getCmd().equals("tools")) {
				doOpenTools(ureq, (Item) link.getUserObject(), link.getFormDispatchId());
			}
		} else if (source == tableEl) {
			if (event instanceof FlexiTableSearchEvent
					|| event instanceof FlexiTableFilterTabEvent) {
				loadModel(ureq);
			} else if (event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				FeedItemRow row = tableModel.getObject(se.getIndex());
				if ("tools".equals(cmd)) {
					doOpenTools(ureq, row.getItem(), "o-tools-".concat(row.getItem().getGuid()));
				}
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				// empty table button
				doAddFeedItem(ureq);
			}
		}
	}

	private void doAddFeedItem(UserRequest ureq) {
		currentItem = new ItemImpl(feedRss);
		currentItem.setDraft(true);
		currentItem.setAuthorKey(ureq.getIdentity().getKey());
		// Generate new GUID for item, needed for media files that are
		// stored relative to the GUID
		currentItem.setGuid(CodeHelper.getGlobalForeverUniqueID());

		itemFormCtrl = feedUIFactory.createItemFormController(ureq, getWindowControl(), currentItem);
		activateModalDialog(itemFormCtrl, translate("feed.item.edit"));
	}

	private void activateModalDialog(FormBasicController ctrl, String title) {
		listenTo(ctrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), ctrl.getInitialComponent(),
				true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doEditFeedItem(UserRequest ureq, Item feedItem) {
		removeAsListenerAndDispose(itemFormCtrl);

		// check if still available, maybe deleted by other user in the meantime
		if (feedItems.contains(feedItem)) {
			lock = feedManager.acquireLock(feedRss, feedItem, getIdentity());
			if (lock.isSuccess()) {
				// reload to prevent stale object, then launch editor
				currentItem = feedManager.loadItem(feedItem.getKey());
				itemFormCtrl = feedUIFactory.createItemFormController(ureq, getWindowControl(), currentItem);
				activateModalDialog(itemFormCtrl, translate("feed.item.edit"));
			} else {
				String fullName = userManager.getUserDisplayName(lock.getOwner());
				showInfo("feed.item.is.being.edited.by", fullName);
			}
		}
	}

	private void doDeleteFeedItem(List<Item> itemsToDelete) {
		for (Item itemToDelete : itemsToDelete) {
			lock = feedManager.acquireLock(feedRss, itemToDelete, getIdentity());
			if (lock.isSuccess()) {
				// remove the item from the naviCtr
				naviCtrl.remove(itemToDelete);
				// remove the item from the table
				feedItems.remove(itemToDelete);
				// permanently remove item
				feedRss = feedManager.deleteItem(itemToDelete);

				// do logging
				ThreadLocalUserActivityLogger.log(FeedLoggingAction.FEED_ITEM_DELETE, getClass(),
						LoggingResourceable.wrap(itemToDelete));
			} else {
				String fullName = userManager.getUserDisplayName(lock.getOwner());
				showInfo("feed.item.is.being.edited.by", new String[]{fullName, itemToDelete.getTitle()});
				break;
			}
		}

		feedManager.releaseLock(lock);
		lock = null;
	}

	private void doConfirmDeleteFeedItem(UserRequest ureq, List<Item> feedItems) {
		if (feedItems.size() == 1) {
			deleteDialogBoxCtrl = activateYesNoDialog(ureq, null, translate("feed.item.confirm.delete"), deleteDialogBoxCtrl);
			deleteDialogBoxCtrl.setUserObject(feedItems);
		} else {
			// for bulk
			deleteDialogBoxCtrl = activateYesNoDialog(ureq, null, translate("feed.item.confirm.delete.bulk"), deleteDialogBoxCtrl);
			deleteDialogBoxCtrl.setUserObject(feedItems);
		}
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = new ArrayList<>(2);
		if (rowObject instanceof FeedItemRow feedItemRow && feedItemRow.getFeedEntryLink() != null) {
			cmps.add(feedItemRow.getFeedEntryLink().getComponent());
		}
		return cmps;
	}

	private void cleanUp() {
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	private static class ToolsCellRenderer extends StaticFlexiCellRenderer {

		public ToolsCellRenderer(String label, String action) {
			super(null, action, false, false, null, "o_icon o_icon_actions o_icon-fw o_icon-lg", label);
		}

		@Override
		protected String getId(Object cellValue, int row, FlexiTableComponent source) {
			FeedItemRow feedItemRow = (FeedItemRow) source.getFormItem().getTableDataModel().getObject(row);
			return "o-tools-".concat(feedItemRow.getItem().getGuid());
		}
	}

	private class ToolsController extends BasicController {

		private final Link editLink;
		private final Link deleteLink;
		private final Link artefactLink;
		private final Item feedItem;


		protected ToolsController(UserRequest ureq, WindowControl wControl, Item feedItem) {
			super(ureq, wControl);
			this.feedItem = feedItem;

			VelocityContainer mainVC = createVelocityContainer("tools");


			List<String> links = new ArrayList<>();
			mainVC.contextPut("links", links);
			editLink = LinkFactory.createLink("feed.item.edit", "edit", getTranslator(), mainVC, this, Link.LINK);
			editLink.setTitle("feed.item.edit");
			editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
			deleteLink = LinkFactory.createLink("feed.item.delete", "delete", getTranslator(), mainVC, this, Link.LINK);
			deleteLink.setTitle("feed.item.delete");
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_trash");

			// create artefactLink only if feed is internal, the entry is the users own and if portfolioModule is enabled
			if (feedRss.isInternal()
					&& getIdentity().getKey() != null
					&& getIdentity().getKey().equals(feedItem.getAuthorKey())
					&& portfolioModule.isEnabled()) {
				artefactLink = LinkFactory.createLink("feed.item.artefact", "artefact", getTranslator(), mainVC, this, Link.LINK);
				artefactLink.setTitle("feed.item.artefact");
				artefactLink.setIconLeftCSS("o_icon o_icon-fw o_icon_eportfolio_add");
			} else {
				artefactLink = null;
			}

			if (artefactLink != null) {
				links.add(artefactLink.getComponentName());
			}
			links.add(editLink.getComponentName());
			links.add("-");
			links.add(deleteLink.getComponentName());

			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if (source == editLink) {
				close();
				doEditFeedItem(ureq, feedItem);
			} else if (source == deleteLink) {
				close();
				doConfirmDeleteFeedItem(ureq, List.of(feedItem));
			} else if (source == artefactLink) {
				close();
				doOpenCollector(ureq, feedItem);
			}
		}

		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}

}
