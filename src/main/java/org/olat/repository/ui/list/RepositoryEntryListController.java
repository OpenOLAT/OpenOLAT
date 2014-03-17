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

import java.util.Collections;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.manager.UserRatingsDAO;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsController;
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
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.rating.RatingFormEvent;
import org.olat.core.gui.components.rating.RatingWithAverageFormItem;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
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
	
	private FormLink listLink, tableLink, filterLink, sortLink;
	private FlexiTableElement tableEl;
	private RepositoryEntryDataModel model;
	private DefaultRepositoryEntryDataSource dataSource;
	private OrderByController sortCtrl;
	private FilterController filterCtrl;
	private CloseableModalController cmc;
	private UserCommentsController commentsCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	
	private final String mapperThumbnailUrl;
	private final MarkManager markManager;
	private final UserRatingsDAO userRatingsDao;
	
	public RepositoryEntryListController(UserRequest ureq, WindowControl wControl, SearchMyRepositoryEntryViewParams searchParams) {
		super(ureq, wControl, "repoentry_table");
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		markManager = CoreSpringFactory.getImpl(MarkManager.class);
		userRatingsDao = CoreSpringFactory.getImpl(UserRatingsDAO.class);
		mapperThumbnailUrl = registerCacheableMapper(ureq, "repositoryentryImage", new RepositoryEntryImageMapper());
		
		dataSource = new DefaultRepositoryEntryDataSource(searchParams, this);
		initForm(ureq);
	}
	
	public boolean isEmpty() {
		return dataSource.getRowCount() == 0;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		listLink = uifactory.addFormLink("switchLayoutList", "list", "table.switch.list", null, formLayout, Link.BUTTON);
		tableLink = uifactory.addFormLink("switchLayoutTable", "table", "table.switch.table", null, formLayout, Link.BUTTON);
		sortLink = uifactory.addFormLink("sortTable", "sort", "table.sort", null, formLayout, Link.BUTTON);
		filterLink = uifactory.addFormLink("filterTable", "filter", "table.filter", null, formLayout, Link.BUTTON);
		
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
		VelocityContainer row = createVelocityContainer("row_1");
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
			tableEl.setCustomizeColumns(false);
		} else if(tableLink == source) {
			tableEl.setRendererType(FlexiTableRendererType.classic);
			tableEl.setCustomizeColumns(true);
		} else if(filterLink == source) {
			doChooseFilter(ureq);
		} else if(sortLink == source) {
			doChooseSorter(ureq);
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
				if(doMark(row)) {
					link.setCustomEnabledLinkCSS("b_mark_set");
				} else {
					link.setCustomEnabledLinkCSS("b_mark_not_set");
				}
				link.getComponent().setDirty(true);
			} else if ("select".equals(cmd) || "start".equals(cmd)){
				RepositoryEntryRow row = (RepositoryEntryRow)link.getUserObject();
				doOpen(ureq, row);
			} else if ("details".equals(cmd)){
				RepositoryEntryRow row = (RepositoryEntryRow)link.getUserObject();
				doOpenDetails(row);
			} else if ("comments".equals(cmd)){
				RepositoryEntryRow row = (RepositoryEntryRow)link.getUserObject();
				doOpenComments(ureq, row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(filterCtrl == source && event == Event.CHANGED_EVENT) {
			doFilter(filterCtrl.getSelectedFilters());
			calloutCtrl.deactivate();
			cleanUp();
		} else if(sortCtrl == source && event == Event.CHANGED_EVENT) {
			doOrderBy(sortCtrl.getOrderBy());
			calloutCtrl.deactivate();
			cleanUp();
		} else if(cmc == source) {
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
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(commentsCtrl);
		calloutCtrl = null;
		commentsCtrl = null;
		cmc = null;
	}
	
	protected void doChooseSorter(UserRequest ureq) {
		removeAsListenerAndDispose(sortCtrl);
		sortCtrl = new OrderByController(ureq, getWindowControl());
		listenTo(sortCtrl);
		
		removeAsListenerAndDispose(calloutCtrl);
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), sortCtrl.getInitialComponent(),
				sortLink, null, true, null);
		listenTo(calloutCtrl);
		calloutCtrl.activate();	
	}
	
	protected void doChooseFilter(UserRequest ureq) {
		if(filterCtrl == null) {
			filterCtrl = new FilterController(ureq, getWindowControl());
			listenTo(filterCtrl);
		}
		
		removeAsListenerAndDispose(calloutCtrl);
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), filterCtrl.getInitialComponent(),
				filterLink, null, true, null);
		listenTo(calloutCtrl);
		calloutCtrl.activate();	
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
	
	protected void doOpenDetails(RepositoryEntryRow row) {
		Panel detailsPanel = row.getDetailsPanel();
		boolean visible = detailsPanel.isVisible();
		if(visible) {
			detailsPanel.setContent(null);
		} else {
			VelocityContainer content = createVelocityContainer("row_details");
			row.getDetailsPanel().setContent(content);
			content.contextPut("description", row.getDescription());
		}
		detailsPanel.setVisible(!visible);
		detailsPanel.setDirty(true);
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
			String businessPath = "[QuestionItem:" + item.getResourceableId() + "]";
			markManager.setMark(item, getIdentity(), null, businessPath);
			return true;
		}
	}

	@Override
	public void forgeMarkLink(RepositoryEntryRow row) {
		FormLink markLink = uifactory.addFormLink("mark_" + row.getKey(), "mark", "&nbsp;&nbsp;&nbsp;&nbsp;", null, null, Link.NONTRANSLATED);
		markLink.setCustomEnabledLinkCSS(row.isMarked() ? "b_mark_set" : "b_mark_not_set");
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
		FormLink startLink = uifactory.addFormLink("start_" + row.getKey(), "start", "start", null, null, Link.LINK);
		startLink.setUserObject(row);
		row.setStartLink(startLink);
	}
	
	@Override
	public void forgeDetails(RepositoryEntryRow row) {
		FormLink detailsLink = uifactory.addFormLink("details_" + row.getKey(), "details", "details", null, null, Link.LINK);
		detailsLink.setUserObject(row);
		row.setDetailsLink(detailsLink);
		
		String id = "detailsp_" + row.getKey();
		Panel detailsPanel = new Panel(id);
		detailsPanel.setVisible(false);
		row.setDetailsPanel(detailsPanel);
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		if(rowObject instanceof RepositoryEntryRow) {
			RepositoryEntryRow r = (RepositoryEntryRow)rowObject;
			Panel detailsPanel = r.getDetailsPanel();
			tableEl.getComponent().put(detailsPanel.getComponentName(), detailsPanel);
			return Collections.<Component>singletonList(detailsPanel);
		}	
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
		String css = numOfComments > 0 ? "b_comments" : "b_comments b_no_comment";
		commentsLink.setCustomEnabledLinkCSS(css);
		row.setCommentsLink(commentsLink);
	}

}