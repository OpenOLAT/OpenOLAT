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
import org.olat.core.CoreSpringFactory;
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
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.rating.RatingFormEvent;
import org.olat.core.gui.components.rating.RatingWithAverageFormItem;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryManager;
import org.olat.repository.SearchMyRepositoryEntryViewParams;
import org.olat.repository.SearchMyRepositoryEntryViewParams.Filter;
import org.olat.repository.SearchMyRepositoryEntryViewParams.OrderBy;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.olat.repository.ui.list.RepositoryEntryDataModel.Cols;

/**
 * 
 * Initial date: 20.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RepositoryEntryListController extends FormBasicController
	implements Activateable2, RepositoryEntryDataSourceUIFactory, FlexiTableComponentDelegate {

	private final List<Link> filters = new ArrayList<>();
	private final List<Link> orderBy = new ArrayList<>();
	
	private FormLink listLink, tableLink;
	private FlexiTableElement tableEl;
	private RepositoryEntryDataModel model;
	private DefaultRepositoryEntryDataSource dataSource;
	private CloseableModalController cmc;
	private UserCommentsController commentsCtrl;
	private final BreadcrumbPanel stackPanel;
	private RepositoryEntryDetailsController detailsCtrl;
	
	private final String mapperThumbnailUrl;
	private final MarkManager markManager;
	private final UserRatingsDAO userRatingsDao;
	
	public RepositoryEntryListController(UserRequest ureq, WindowControl wControl,
			SearchMyRepositoryEntryViewParams searchParams, BreadcrumbPanel stackPanel) {
		super(ureq, wControl, "repoentry_table");
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		markManager = CoreSpringFactory.getImpl(MarkManager.class);
		userRatingsDao = CoreSpringFactory.getImpl(UserRatingsDAO.class);
		mapperThumbnailUrl = registerCacheableMapper(ureq, "repositoryentryImage", new RepositoryEntryImageMapper());
		
		this.stackPanel = stackPanel;
		
		dataSource = new DefaultRepositoryEntryDataSource(searchParams, this);
		initForm(ureq);
		initFilters();
		initSorters();
	}
	
	public boolean isEmpty() {
		return dataSource.getRowCount() == 0;
	}

	private void initFilters() {
		VelocityContainer filterVc = createVelocityContainer("filters");
		filterVc.setDomReplacementWrapperRequired(false);
		flc.put("filters", filterVc);
		//lifecycle
		initFilter("current.courses", "filter.current.courses", Filter.currentCourses, filterVc);
		initFilter("upcoming.courses", "filter.upcoming.courses", Filter.upcomingCourses, filterVc);
		initFilter("old.courses", "filter.old.courses", Filter.oldCourses, filterVc);
		//membership
		initFilter("as.participant", "filter.booked.participant", Filter.asParticipant, filterVc);
		initFilter("as.coach", "filter.booked.coach", Filter.asCoach, filterVc);
		initFilter("as.author", "filter.booked.author", Filter.asAuthor, filterVc);
		initFilter("not.booked", "filter.not.booked", Filter.notBooked, filterVc);
		
		//efficiency statment
		initFilter("passed", "filter.passed", Filter.passed, filterVc);
		initFilter("not.passed", "filter.not.passed", Filter.notPassed, filterVc);
		initFilter("without", "filter.without.passed.infos", Filter.withoutPassedInfos, filterVc);
	}
	
	private void initFilter(String name, String i18nKey, Filter filter, VelocityContainer filterVc) {
		Link notPassedLink = LinkFactory.createCustomLink(name, name, i18nKey, Link.LINK, filterVc, this);
		notPassedLink.setUserObject(filter);
		filters.add(notPassedLink);
	}
	
	private void initSorters() {
		VelocityContainer orderByVc = createVelocityContainer("orderby");
		orderByVc.setDomReplacementWrapperRequired(false);
		flc.put("orderBys", orderByVc);
		
		initOrderBy("orderby.automatic", OrderBy.automatic, orderByVc);
		initOrderBy("orderby.favorit", OrderBy.favorit, orderByVc);
		initOrderBy("orderby.lastVisited", OrderBy.lastVisited, orderByVc);
		initOrderBy("orderby.score", OrderBy.score, orderByVc);
		initOrderBy("orderby.passed", OrderBy.passed, orderByVc);
		
		initOrderBy("orderby.title", OrderBy.title, orderByVc);
		initOrderBy("orderby.lifecycle", OrderBy.lifecycle, orderByVc);
		initOrderBy("orderby.author", OrderBy.author, orderByVc);
		initOrderBy("orderby.creationDate", OrderBy.creationDate, orderByVc);
		initOrderBy("orderby.lastModified", OrderBy.lastModified, orderByVc);
		initOrderBy("orderby.rating", OrderBy.rating, orderByVc);
	}
	
	private void initOrderBy(String name, OrderBy order, VelocityContainer filterVc) {
		Link notPassedLink = LinkFactory.createCustomLink(name, name, name, Link.LINK, filterVc, this);
		notPassedLink.setUserObject(order);
		orderBy.add(notPassedLink);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		listLink = uifactory.addFormLink("switchLayoutList", "list", "", null, formLayout, Link.BUTTON + Link.NONTRANSLATED);
		listLink.setIconCSS("o_icon o_icon_list o_icon-lg");
		listLink.setLinkTitle(translate("table.switch.list"));
		listLink.setActive(true);
		tableLink = uifactory.addFormLink("switchLayoutTable", "table", "", null, formLayout, Link.BUTTON + Link.NONTRANSLATED);
		tableLink.setIconCSS("o_icon o_icon_table o_icon-lg");
		tableLink.setLinkTitle(translate("table.switch.table"));
		tableLink.setActive(false);			
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.key.i18nKey(), Cols.key.ordinal(), false, null));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.mark.i18nKey(), Cols.mark.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.displayName.i18nKey(), Cols.displayName.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lifecycleLabel.i18nKey(), Cols.lifecycleLabel.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lifecycleSoftkey.i18nKey(), Cols.lifecycleSoftkey.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lifecycleStart.i18nKey(), Cols.lifecycleStart.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lifecycleEnd.i18nKey(), Cols.lifecycleEnd.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.select.i18nKey(), Cols.select.ordinal(), false, null));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.details.i18nKey(), Cols.details.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.start.i18nKey(), Cols.start.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.ratings.i18nKey(), Cols.ratings.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.comments.i18nKey(), Cols.comments.ordinal()));

		model = new RepositoryEntryDataModel(dataSource, columnsModel);
		tableEl = uifactory.addTableElement(ureq, getWindowControl(), "table", model, 20, getTranslator(), formLayout);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setSearchEnabled(true);
		tableEl.setCustomizeColumns(false);
		tableEl.setElementCssClass("o_coursetable o_rendertype_custom");
		VelocityContainer row = createVelocityContainer("row_1");
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		tableEl.setRowRenderer(row, this);
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
		if(listLink == source) {
			tableEl.setRendererType(FlexiTableRendererType.custom);
			tableEl.setElementCssClass("o_coursetable o_rendertype_custom");
			tableEl.setCustomizeColumns(false);
			listLink.setActive(true);
			tableLink.setActive(false);			
		} else if(tableLink == source) {
			tableEl.setRendererType(FlexiTableRendererType.classic);
			tableEl.setElementCssClass("o_coursetable o_rendertype_classic");
			tableEl.setCustomizeColumns(true);
			listLink.setActive(false);
			tableLink.setActive(true);
		} else if(source instanceof RatingWithAverageFormItem && event instanceof RatingFormEvent) {
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
				link.setIconCSS(marked ? "o_icon o_icon_bookmark o_icon-lg" : "o_icon o_icon_bookmark_add o_icon-lg");
				link.getComponent().setDirty(true);
				row.setMarked(marked);
			} else if ("select".equals(cmd) || "start".equals(cmd)){
				RepositoryEntryRow row = (RepositoryEntryRow)link.getUserObject();
				doOpen(ureq, row);
			} else if ("details".equals(cmd)){
				RepositoryEntryRow row = (RepositoryEntryRow)link.getUserObject();
				doOpenDetails(ureq, row);
			} else if ("comments".equals(cmd)){
				RepositoryEntryRow row = (RepositoryEntryRow)link.getUserObject();
				doOpenComments(ureq, row);
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
					String iconCss = filter.getIconCSS();
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
		String iconCss = link.getIconCSS();
		if(StringHelper.containsNonWhitespace(iconCss)) {
			link.setIconCSS(null);
		} else {
			link.setIconCSS("o_icon o_icon_check o_icon-fw");
		}
	}
	
	private void removeCheck(Link link) {
		String iconCss = link.getIconCSS();
		if(StringHelper.containsNonWhitespace(iconCss)) {
			link.setIconCSS(null);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		 if(cmc == source) {
			if(commentsCtrl != null) {
				RepositoryEntryRow row = (RepositoryEntryRow)commentsCtrl.getUserObject();
				long numOfComments = commentsCtrl.getCommentsCount();
				String css = numOfComments > 0 ? "b_comments" : "b_comments b_no_comment";
				row.getCommentsLink().setCustomEnabledLinkCSS(css);
				String title = "(" + numOfComments + ")";
				row.getCommentsLink().setI18nKey(title);
				row.getCommentsLink().getComponent().setDirty(true);
			}
			cleanUp();
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
		String businessPath = "[RepositoryEntry:" + row.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
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
		FormLink markLink = uifactory.addFormLink("mark_" + row.getKey(), "mark", "", null, null, Link.NONTRANSLATED);
		markLink.setIconCSS(row.isMarked() ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
		markLink.setUserObject(row);
		row.setMarkLink(markLink);
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
		if(row.getAccessTypes() != null && !row.getAccessTypes().isEmpty() && !row.isMember()) {
			label = "book";
		} else {
			label = "start";
		}
		FormLink startLink = uifactory.addFormLink("start_" + row.getKey(), "start", label, null, null, Link.LINK);
		startLink.setUserObject(row);
		startLink.setCustomEnabledLinkCSS("o_start");
		startLink.setIconCSS("o_icon o_icon_start");
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
		Float averageRating = row.getAverageRating();
		long numOfRatings = row.getNumOfRatings();

		float ratingValue = myRating == null ? 0f : myRating.floatValue();
		float averageRatingValue = averageRating == null ? 0f : averageRating.floatValue();
		RatingWithAverageFormItem ratingCmp
			= new RatingWithAverageFormItem("rat_" + row.getKey(), ratingValue, averageRatingValue, 5, numOfRatings);
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
		commentsLink.setIconCSS(css);
		row.setCommentsLink(commentsLink);
	}
}