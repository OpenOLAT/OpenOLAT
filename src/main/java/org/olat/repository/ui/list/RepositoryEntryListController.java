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
package org.olat.repository.ui.list;

import java.util.ArrayList;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.manager.UserRatingsDAO;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsController;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSort;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.rating.RatingFormEvent;
import org.olat.core.gui.components.rating.RatingWithAverageFormItem;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CorruptedCourseException;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams.Filter;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams.OrderBy;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.olat.repository.ui.list.RepositoryEntryDataModel.Cols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RepositoryEntryListController extends FormBasicController
	implements Activateable2, RepositoryEntryDataSourceUIFactory, FlexiTableComponentDelegate {

	private final List<Link> filters = new ArrayList<>();
	private final List<Link> orderBy = new ArrayList<>();
	
	private boolean startExtendedSearch;

	private final String name;
	private FlexiTableElement tableEl;
	private RepositoryEntryDataModel model;
	private DefaultRepositoryEntryDataSource dataSource;
	private SearchMyRepositoryEntryViewParams searchParams;
	
	private CloseableModalController cmc;
	private UserCommentsController commentsCtrl;
	private final BreadcrumbPanel stackPanel;
	private RepositoryEntryDetailsController detailsCtrl;
	private RepositoryEntrySearchController searchCtrl;
	
	private final String mapperThumbnailUrl;
	@Autowired
	private MarkManager markManager;
	@Autowired
	private UserRatingsDAO userRatingsDao;
	
	private final boolean guestOnly;
	
	public RepositoryEntryListController(UserRequest ureq, WindowControl wControl,
			SearchMyRepositoryEntryViewParams searchParams, boolean load, 
			boolean startExtendedSearch, String name, BreadcrumbPanel stackPanel) {
		super(ureq, wControl, "repoentry_table");
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		mapperThumbnailUrl = registerCacheableMapper(ureq, "repositoryentryImage", new RepositoryEntryImageMapper());
		this.name = name;
		this.stackPanel = stackPanel;
		this.startExtendedSearch = startExtendedSearch;
		guestOnly = ureq.getUserSession().getRoles().isGuestOnly();
		
		this.searchParams = searchParams;
		dataSource = new DefaultRepositoryEntryDataSource(searchParams, this);
		initForm(ureq);
		initFilters();
		initSorters();
		
		if(load) {
			tableEl.sort(OrderBy.automatic.name(), true);
		}
	}
	
	public boolean isEmpty() {
		return dataSource.getRowCount() == 0;
	}

	private void initFilters() {
		List<FlexiTableFilter> filters = new ArrayList<>(14);
		filters.add(new FlexiTableFilter(translate("filter.current.courses"), Filter.currentCourses.name()));
		filters.add(new FlexiTableFilter(translate("filter.upcoming.courses"), Filter.upcomingCourses.name()));
		filters.add(new FlexiTableFilter(translate("filter.old.courses"), Filter.oldCourses.name()));
		filters.add(FlexiTableFilter.SPACER);
		filters.add(new FlexiTableFilter(translate("filter.booked.participant"), Filter.asParticipant.name()));
		filters.add(new FlexiTableFilter(translate("filter.booked.coach"), Filter.asCoach.name()));
		filters.add(new FlexiTableFilter(translate("filter.booked.author"), Filter.asAuthor.name()));
		filters.add(new FlexiTableFilter(translate("filter.not.booked"), Filter.notBooked.name()));
		filters.add(FlexiTableFilter.SPACER);
		filters.add(new FlexiTableFilter(translate("filter.passed"), Filter.passed.name()));
		filters.add(new FlexiTableFilter(translate("filter.not.passed"), Filter.notPassed.name()));
		filters.add(new FlexiTableFilter(translate("filter.without.passed.infos"), Filter.withoutPassedInfos.name()));
		tableEl.setFilters(null, filters);
	}
	
	private void initSorters() {
		List<FlexiTableSort> sorters = new ArrayList<>();
		sorters.add(new FlexiTableSort(translate("orderby.automatic"), OrderBy.automatic.name()));
		sorters.add(new FlexiTableSort(translate("orderby.favorit"), OrderBy.favorit.name()));
		sorters.add(new FlexiTableSort(translate("orderby.lastVisited"), OrderBy.lastVisited.name()));
		sorters.add(new FlexiTableSort(translate("orderby.score"), OrderBy.score.name()));
		sorters.add(new FlexiTableSort(translate("orderby.passed"), OrderBy.passed.name()));
		sorters.add(FlexiTableSort.SPACER);
		sorters.add(new FlexiTableSort(translate("orderby.title"), OrderBy.title.name()));
		sorters.add(new FlexiTableSort(translate("orderby.lifecycle"), OrderBy.lifecycle.name()));
		sorters.add(new FlexiTableSort(translate("orderby.author"), OrderBy.author.name()));
		sorters.add(new FlexiTableSort(translate("orderby.creationDate"), OrderBy.creationDate.name()));
		sorters.add(new FlexiTableSort(translate("orderby.lastModified"), OrderBy.lastModified.name()));
		sorters.add(new FlexiTableSort(translate("orderby.rating"), OrderBy.rating.name()));
		
		FlexiTableSortOptions options = new FlexiTableSortOptions(sorters);
		options.setDefaultOrderBy(new SortKey(OrderBy.automatic.name(), true));
		tableEl.setSortSettings(options);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		searchCtrl = new RepositoryEntrySearchController(ureq, getWindowControl(), !startExtendedSearch, mainForm);
		searchCtrl.setEnabled(false);
		listenTo(searchCtrl);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.key.i18nKey(), Cols.key.ordinal(), false, null));
		if(!guestOnly) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.mark.i18nKey(), Cols.mark.ordinal()));
		}
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel(Cols.displayName.i18nKey(), Cols.displayName.ordinal(),
				"select", new StaticFlexiCellRenderer("select", new TextFlexiCellRenderer())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lifecycleLabel.i18nKey(), Cols.lifecycleLabel.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lifecycleSoftkey.i18nKey(), Cols.lifecycleSoftkey.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lifecycleStart.i18nKey(), Cols.lifecycleStart.ordinal(),
				new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lifecycleEnd.i18nKey(), Cols.lifecycleEnd.ordinal(),
				new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.select.i18nKey(), Cols.select.ordinal(), false, null));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.details.i18nKey(), Cols.details.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.start.i18nKey(), Cols.start.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.ratings.i18nKey(), Cols.ratings.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.comments.i18nKey(), Cols.comments.ordinal()));

		model = new RepositoryEntryDataModel(dataSource, columnsModel);
		tableEl = uifactory.addTableElement(ureq, getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setSearchEnabled(true);
		tableEl.setExtendedSearch(searchCtrl);
		tableEl.setCustomizeColumns(false);
		tableEl.setElementCssClass("o_coursetable");
		tableEl.setEmtpyTableMessageKey("table.sEmptyTable");
		VelocityContainer row = createVelocityContainer("row_1");
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		tableEl.setRowRenderer(row, this);
		tableEl.setAndLoadPersistedPreferences(ureq, "re-list-" + name);
		
		if(startExtendedSearch) {
			tableEl.expandExtendedSearch(ureq);
		}
	}

	@Override
	public String getMapperThumbnailUrl() {
		return mapperThumbnailUrl;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {	
		if(source instanceof RatingWithAverageFormItem && event instanceof RatingFormEvent) {
			RatingFormEvent ratingEvent = (RatingFormEvent)event;
			RatingWithAverageFormItem ratingItem = (RatingWithAverageFormItem)source;
			RepositoryEntryRow row = (RepositoryEntryRow)ratingItem.getUserObject();
			doRating(row, ratingEvent.getRating());
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			
			if("mark".equals(cmd)) {
				RepositoryEntryRow row = (RepositoryEntryRow)link.getUserObject();
				boolean marked = doMark(row);
				link.setIconLeftCSS(marked ? "o_icon o_icon_bookmark o_icon-lg" : "o_icon o_icon_bookmark_add o_icon-lg");
				link.getComponent().setDirty(true);
				row.setMarked(marked);
			} else if ("start".equals(cmd)){
				RepositoryEntryRow row = (RepositoryEntryRow)link.getUserObject();
				doOpen(ureq, row);
			} else if ("details".equals(cmd)){
				RepositoryEntryRow row = (RepositoryEntryRow)link.getUserObject();
				doOpenDetails(ureq, row);
			} else if ("select".equals(cmd)) {
				RepositoryEntryRow row = (RepositoryEntryRow)link.getUserObject();
				if (row.isMember()) {
					doOpen(ureq, row);					
				} else {
					doOpenDetails(ureq, row);
				}
			} else if ("comments".equals(cmd)){
				RepositoryEntryRow row = (RepositoryEntryRow)link.getUserObject();
				doOpenComments(ureq, row);
			}
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				RepositoryEntryRow row = model.getObject(se.getIndex());
				if("select".equals(cmd)) {
					if (row.isMember()) {
						doOpen(ureq, row);					
					} else {
						doOpenDetails(ureq, row);
					}
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link) {
			Link link = (Link)source;
			Object uo = link.getUserObject();
			if(uo instanceof OrderBy) {
				OrderBy sort = (OrderBy)uo;
				for(Link order:orderBy) {
					removeCheck(order);
				}
				toggleCheck(link);
				doOrderBy(sort);
				flc.setDirty(true);
			} else if(uo instanceof Filter) {
				toggleCheck(link);
				List<Filter> selectedFilters = new ArrayList<>();
				for(Link filter:filters) {
					String iconCss = filter.getIconLeftCSS();
					if(StringHelper.containsNonWhitespace(iconCss)) {
						selectedFilters.add((Filter)filter.getUserObject());
					}
				}
				doFilter(selectedFilters);
				flc.setDirty(true);
			}
		}
		super.event(ureq, source, event);
	}
	
	private void toggleCheck(Link link) {
		String iconCss = link.getIconLeftCSS();
		if(StringHelper.containsNonWhitespace(iconCss)) {
			link.setIconLeftCSS(null);
		} else {
			link.setIconLeftCSS("o_icon o_icon_check o_icon-fw");
		}
	}
	
	private void removeCheck(Link link) {
		String iconCss = link.getIconLeftCSS();
		if(StringHelper.containsNonWhitespace(iconCss)) {
			link.setIconLeftCSS(null);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		 if(cmc == source) {
			if(commentsCtrl != null) {
				RepositoryEntryRow row = (RepositoryEntryRow)commentsCtrl.getUserObject();
				long numOfComments = commentsCtrl.getCommentsCount();
				
				String css = numOfComments > 0 ? "o_icon o_icon_comments o_icon-lg" : "o_icon o_icon_comments_none o_icon-lg";
				row.getCommentsLink().setCustomEnabledLinkCSS("o_comments");
				row.getCommentsLink().setIconLeftCSS(css);
				String title = "(" + numOfComments + ")";
				row.getCommentsLink().setI18nKey(title);
				row.getCommentsLink().getComponent().setDirty(true);
			}
			cleanUp();
		} else if(commentsCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
				cleanUp();
			}
		} else if(searchCtrl == source) {
			if(event instanceof SearchEvent) {
				SearchEvent se = (SearchEvent)event;
				doSearch(se);
			} else if(event == Event.CANCELLED_EVENT) {
				searchParams.setIdAndRefs(null);
				searchParams.setAuthor(null);
				searchParams.setText(null);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc) {
		//do not update the 
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(commentsCtrl);
		commentsCtrl = null;
		cmc = null;
	}
	
	private void doSearch(SearchEvent se) {
		searchParams.setIdAndRefs(se.getId());
		searchParams.setAuthor(se.getAuthor());
		searchParams.setText(se.getDisplayname());
		tableEl.reset();
	}
	
	protected void doFilter(List<Filter> filters) {
		dataSource.setFilters(filters);
		tableEl.reset();
	}
	
	protected void doOrderBy(OrderBy orderBy) {
		dataSource.setOrderBy(orderBy);
		tableEl.reset();
	}
	
	protected void doRating(RepositoryEntryRow row, float rating) {
		OLATResourceable ores = row.getRepositoryEntryResourceable();
		userRatingsDao.updateRating(getIdentity(), ores, null, Math.round(rating));
	}
	
	protected void doOpen(UserRequest ureq, RepositoryEntryRow row) {
		try {
			String businessPath = "[RepositoryEntry:" + row.getKey() + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} catch (CorruptedCourseException e) {
			logError("Course corrupted: " + row.getKey() + " (" + row.getOLATResourceable().getResourceableId() + ")", e);
			showError("cif.error.corrupted");
		}
	}
	
	protected void doOpenDetails(UserRequest ureq, RepositoryEntryRow row) {
		removeAsListenerAndDispose(detailsCtrl);
		
		detailsCtrl = new RepositoryEntryDetailsController(ureq, getWindowControl(), row);
		listenTo(detailsCtrl);
		
		String displayName = row.getDisplayName();
		stackPanel.pushController(displayName, detailsCtrl);
	}
	
	protected void doOpenComments(UserRequest ureq, RepositoryEntryRow row) {
		if(commentsCtrl != null) return;
		
		boolean anonym = ureq.getUserSession().getRoles().isGuestOnly();
		CommentAndRatingSecurityCallback secCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), false, anonym);
		commentsCtrl = new UserCommentsController(ureq, getWindowControl(), row.getRepositoryEntryResourceable(), null, secCallback);
		commentsCtrl.setUserObject(row);
		listenTo(commentsCtrl);
		cmc = new CloseableModalController(getWindowControl(), "close", commentsCtrl.getInitialComponent(), true, translate("comments"));
		listenTo(cmc);
		cmc.activate();
	}
	
	protected boolean doMark(RepositoryEntryRow row) {
		OLATResourceable item = OresHelper.createOLATResourceableInstance("RepositoryEntry", row.getKey());
		if(markManager.isMarked(item, getIdentity(), null)) {
			markManager.removeMark(item, getIdentity(), null);
			return false;
		} else {
			String businessPath = "[RepositoryEntry:" + item.getResourceableId() + "]";
			markManager.setMark(item, getIdentity(), null, businessPath);
			return true;
		}
	}

	@Override
	public void forgeMarkLink(RepositoryEntryRow row) {
		if(!guestOnly) {
			FormLink markLink = uifactory.addFormLink("mark_" + row.getKey(), "mark", "", null, null, Link.NONTRANSLATED);
			markLink.setIconLeftCSS(row.isMarked() ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
			markLink.setUserObject(row);
			row.setMarkLink(markLink);
		}
	}
	
	@Override
	public void forgeSelectLink(RepositoryEntryRow row) {
		String name = row.getDisplayName();
		FormLink selectLink = uifactory.addFormLink("select_" + row.getKey(), "select", name, null, null, Link.NONTRANSLATED);
		selectLink.setUserObject(row);
		row.setSelectLink(selectLink);
	}

	@Override
	public void forgeStartLink(RepositoryEntryRow row) {
		String label;
		boolean isStart = true;
		if(!row.isMembersOnly() && row.getAccessTypes() != null && !row.getAccessTypes().isEmpty() && !row.isMember()) {
			label = "book";
			isStart = false;
		} else {
			label = "start";
		}
		FormLink startLink = uifactory.addFormLink("start_" + row.getKey(), "start", label, null, null, Link.LINK);
		startLink.setUserObject(row);
		startLink.setCustomEnabledLinkCSS(isStart ? "o_start btn-block" : "o_book btn-block");
		startLink.setIconRightCSS("o_icon o_icon_start");
		row.setStartLink(startLink);
	}	
	
	@Override
	public void forgeDetails(RepositoryEntryRow row) {
		FormLink detailsLink = uifactory.addFormLink("details_" + row.getKey(), "details", "details", null, null, Link.LINK);
		detailsLink.setCustomEnabledLinkCSS("o_details");
		detailsLink.setUserObject(row);
		row.setDetailsLink(detailsLink);
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		return null;
	}

	@Override
	public void forgeRatings(RepositoryEntryRow row) {
		Integer myRating = row.getMyRating();
		Double averageRating = row.getAverageRating();
		long numOfRatings = row.getNumOfRatings();

		float ratingValue = myRating == null ? 0f : myRating.floatValue();
		float averageRatingValue = averageRating == null ? 0f : averageRating.floatValue();
		RatingWithAverageFormItem ratingCmp
			= new RatingWithAverageFormItem("rat_" + row.getKey(), ratingValue, averageRatingValue, 5, numOfRatings);
		ratingCmp.setEnabled(!guestOnly);
		row.setRatingFormItem(ratingCmp);
		ratingCmp.setUserObject(row);
	}

	@Override
	public void forgeComments(RepositoryEntryRow row) {
		long numOfComments = row.getNumOfComments();
		String title = "(" + numOfComments + ")";
		FormLink commentsLink = uifactory.addFormLink("comments_" + row.getKey(), "comments", title, null, null, Link.NONTRANSLATED);
		commentsLink.setUserObject(row);
		String css = numOfComments > 0 ? "o_icon o_icon_comments o_icon-lg" : "o_icon o_icon_comments_none o_icon-lg";
		commentsLink.setCustomEnabledLinkCSS("o_comments");
		commentsLink.setIconLeftCSS(css);
		row.setCommentsLink(commentsLink);
	}

	@Override
	public Translator getTranslator() {
		return super.getTranslator();
	}
	
}