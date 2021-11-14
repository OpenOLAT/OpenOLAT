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
package org.olat.modules.portfolio.ui.shared;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableReduceEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.BinderSecurityCallbackFactory;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.PageUserInformations;
import org.olat.modules.portfolio.PageUserStatus;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.model.AccessRights;
import org.olat.modules.portfolio.model.AssessedPage;
import org.olat.modules.portfolio.model.SearchSharePagesParameters;
import org.olat.modules.portfolio.ui.PageRunController;
import org.olat.modules.portfolio.ui.PortfolioHomeController;
import org.olat.modules.portfolio.ui.event.ClosePageEvent;
import org.olat.modules.portfolio.ui.event.DonePageEvent;
import org.olat.modules.portfolio.ui.renderer.PageTitleCellRenderer;
import org.olat.modules.portfolio.ui.renderer.SharedPageStatusCellRenderer;
import org.olat.modules.portfolio.ui.renderer.StatusCellRenderer;
import org.olat.modules.portfolio.ui.shared.SharedPagesDataModel.SharePageCols;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharedPagesController extends FormBasicController implements Activateable2 {
	
	protected static final String USER_PROPS_ID = PortfolioHomeController.class.getCanonicalName();
	
	public static final int USER_PROPS_OFFSET = 500;
	
	private FlexiTableElement tableEl;
	private SharedPagesDataModel model;
	private final TooledStackedPanel stackPanel;
	
	private int counter;
	private final List<PageStatus> filters;
	private final PageStatus defaultFilter;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	private PageRunController pageCtrl;
	private SearchSharePagesParameters searchParams;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public SharedPagesController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			SearchSharePagesParameters searchParams, List<PageStatus> filters, PageStatus defaultFilter) {
		super(ureq, wControl, "shared_bookmark");
		setTranslator(Util.createPackageTranslator(PortfolioHomeController.class, getLocale()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));

		this.filters = filters;
		this.defaultFilter = defaultFilter;
		this.stackPanel = stackPanel;
		this.searchParams = searchParams;
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		stackPanel.addListener(this);

		initForm(ureq);
		loadModel(true, true);
	}
	
	public int getRowCount() {
		return model.getRowCount();
	}

	@Override
	protected void doDispose() {
		stackPanel.removeListener(this);
        super.doDispose();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SharePageCols.bookmark));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SharePageCols.userInfosStatus,
				new SharedPageStatusCellRenderer(getTranslator())));
		FlexiCellRenderer titleRenderer =  new BooleanCellRenderer(
				new StaticFlexiCellRenderer("select", new PageTitleCellRenderer()), new PageTitleCellRenderer());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, true, "select", null,
				SharePageCols.pageName.ordinal(), "select", true, SharePageCols.pageName.sortKey(),
				FlexiColumnModel.ALIGNMENT_LEFT, titleRenderer));
		
		SortKey defaultSortKey = null;
		// followed by the users fields
		int colPos = USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);

			String propName = userPropertyHandler.getName();
			if(defaultSortKey == null) {
				defaultSortKey = new SortKey(propName, true);
			}
			
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, true, propName));
			colPos++;
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SharePageCols.lastChanges));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SharePageCols.pageStatus,
				new StatusCellRenderer(getTranslator())));
		
		model = new SharedPagesDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setElementCssClass("o_binder_shared_bookmark_pages_listing");
		tableEl.setEmptyTableMessageKey("table.sEmptyTable");
		tableEl.setPageSize(24);
		tableEl.setAndLoadPersistedPreferences(ureq, "shared-bookmark-pages-v2");
		
		if(filters != null && !filters.isEmpty()) {
			List<FlexiTableFilter> tableFilters = new ArrayList<>(filters.size());
			for(PageStatus status:filters) {
				String label = translate(status.i18nKey());
				tableFilters.add(new FlexiTableFilter(label, status.name(), "o_icon ".concat(status.iconClass())));
			}
			tableFilters.add(FlexiTableFilter.SPACER);
			tableFilters.add(new FlexiTableFilter(translate("filter.show.all"), "all", true));
			tableEl.setFilters("Filters", tableFilters, true);
			if(defaultFilter != null) {
				tableEl.setSelectedFilterKey(defaultFilter.name());
			}
		}
	}
	
	protected void loadModel() {
		loadModel(false, false);
	}
	
	private void loadModel(boolean resetPage, boolean resetInternal) {
		searchParams.setSearchString(tableEl.getQuickSearchString());
		List<AssessedPage> sharedItems = portfolioService
				.searchSharedPagesWith(getIdentity(), searchParams);
		
		List<SharedPageRow> rows = new ArrayList<>(sharedItems.size());
		for(AssessedPage sharedItem:sharedItems) {
			SharedPageRow row = new SharedPageRow(sharedItem.getOwner(), userPropertyHandlers, getLocale());
			row.setPageKey(sharedItem.getPageKey());
			row.setPageTitle(sharedItem.getPageTitle());
			row.setStatus(sharedItem.getPageStatus());
			row.setMark(sharedItem.isMarked());
			row.setUserStatus(sharedItem.getUserStatus());
			row.setLastChanges(sharedItem.getLastModified());
			
			FormLink markLink = uifactory.addFormLink("mark_" + (counter++), "mark", "", null, flc, Link.NONTRANSLATED);
			markLink.setIconLeftCSS(sharedItem.isMarked() ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
			markLink.setTitle(translate(sharedItem.isMarked() ? "details.bookmark.remove" : "details.bookmark"));
			markLink.setUserObject(row);
			row.setBookmarkLink(markLink);
			rows.add(row);
		}
		
		model.setObjects(rows);
		tableEl.reset(resetPage, resetInternal, true);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String name = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Entry".equalsIgnoreCase(name)) {
			Long pageKey = entries.get(0).getOLATResourceable().getResourceableId();
			List<SharedPageRow> filteredRows = model.getObjects();
			for(SharedPageRow filteredRow:filteredRows) {
				if(pageKey.equals(filteredRow.getPageKey())) {
					doSelectedPage(ureq, filteredRow);
					return;
				}
			}
			
			List<SharedPageRow> allRows = model.getBackups();
			for(SharedPageRow row:allRows) {
				if(pageKey.equals(row.getPageKey())) {
					tableEl.setSelectedFilterKey(null);
					tableEl.reset(true, true, true);
					doSelectedPage(ureq, row);
					return;
				}
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				SharedPageRow row = model.getObject(se.getIndex());
				if("select".equals(cmd)) {
					Activateable2 activeateable = doSelectedPage(ureq, row);
					if(activeateable != null) {
						activeateable.activate(ureq, null, null);
					}
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				FlexiTableSearchEvent se = (FlexiTableSearchEvent)event;
				if(FlexiTableReduceEvent.QUICK_SEARCH.equals(se.getCommand())
						|| FormEvent.RESET.getCommand().equals(se.getCommand())) {
					loadModel(true, true);
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("mark".equals(link.getCmd())) {
				SharedPageRow row = (SharedPageRow)link.getUserObject();
				toggleBookmark(row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(stackPanel == source) {
			if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				if(pe.getController() == pageCtrl) {
					loadModel(false, false);
					if(pageCtrl != null && pageCtrl.getPage() != null) {
						stackPanel.popUserObject(new SharedPageAuthor(pageCtrl.getPage().getKey()));
					}
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(pageCtrl == source) {
			if(event instanceof ClosePageEvent) {
				loadModel(false, false);
				stackPanel.popController(pageCtrl);
			} else if(event instanceof DonePageEvent) {
				loadModel(false, false);
			}
		}
		super.event(ureq, source, event);
	}
	
	private void toggleBookmark(SharedPageRow row) {
		Page page = portfolioService.getPageByKey(row.getPageKey());
		PageUserInformations infos = portfolioService.getPageUserInfos(page, getIdentity(), PageUserStatus.incoming);
		infos.setMark(!infos.isMark());
		infos = portfolioService.updatePageUserInfos(infos);
		if(searchParams.isBookmarkOnly() && !infos.isMark()) {
			List<SharedPageRow> rows = model.getObjects();
			rows.remove(row);
			model.setObjects(rows);
			tableEl.reset(false, false, true);
		} else {
			row.getBookmarkLink().setIconLeftCSS(infos.isMark() ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
		}
	}

	private Activateable2 doSelectedPage(UserRequest ureq, SharedPageRow row) {
		removeAsListenerAndDispose(pageCtrl);
		
		Page reloadedPage = portfolioService.getPageByKey(row.getPageKey());
		if(reloadedPage == null) {
			loadModel(true, true);
			showWarning("warning.page.not.found");
			return null;
		}
		Binder binder = reloadedPage.getSection().getBinder();
		
		OLATResourceable pageOres = OresHelper.createOLATResourceableInstance("Entry", reloadedPage.getKey());
		WindowControl swControl = addToHistory(ureq, pageOres, null);
		
		List<AccessRights> rights = portfolioService.getAccessRights(binder, getIdentity());
		BinderSecurityCallback secCallback = BinderSecurityCallbackFactory.getCallbackForCoach(binder, rights);
		pageCtrl = new PageRunController(ureq, swControl, stackPanel, secCallback, reloadedPage, false);
		listenTo(pageCtrl);
		
		if(row.getIdentityKey() != null) {
			String author = userManager.getUserDisplayName(row.getIdentityKey());
			stackPanel.pushController(author, null, new SharedPageAuthor(row.getPageKey()));
		}
		stackPanel.pushController(reloadedPage.getTitle(), pageCtrl);
		return pageCtrl;
	}
	
	private static final class SharedPageAuthor {
		
		private final Long pageKey;
		
		public SharedPageAuthor(Long pageKey) {
			this.pageKey = pageKey;
		}

		@Override
		public int hashCode() {
			return pageKey.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof SharedPageAuthor) {
				SharedPageAuthor spa = (SharedPageAuthor)obj;
				return pageKey.equals(spa.pageKey);
			}
			return false;
		}
	}
}
