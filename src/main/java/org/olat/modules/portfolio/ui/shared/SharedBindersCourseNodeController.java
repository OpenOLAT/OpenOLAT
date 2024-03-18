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
package org.olat.modules.portfolio.ui.shared;

import java.util.ArrayList;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.ContentRoles;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.model.AssessedBinder;
import org.olat.modules.portfolio.model.AssessedBinderSection;
import org.olat.modules.portfolio.model.SharedItemRow;
import org.olat.modules.portfolio.ui.PortfolioHomeController;
import org.olat.modules.portfolio.ui.renderer.AssessmentEntryCellRenderer;
import org.olat.modules.portfolio.ui.renderer.SelectSectionsCellRenderer;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: March 12, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class SharedBindersCourseNodeController extends FormBasicController {

	private final List<UserPropertyHandler> userPropertyHandlers;
	private final String subIdent;

	private FlexiTableElement tableEl;
	private SharedBindersDataModel model;

	private DialogBoxController confirmLeaveCtrl;

	@Autowired
	private UserManager userManager;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private BaseSecurityModule securityModule;

	public SharedBindersCourseNodeController(UserRequest ureq, WindowControl wControl, Form mainForm, String subIdent) {
		super(ureq, wControl, LAYOUT_CUSTOM, "shared_with_me_cn", mainForm);
		this.subIdent = subIdent;
		setTranslator(Util.createPackageTranslator(PortfolioHomeController.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));

		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(SharedBindersController.USER_PROPS_ID, isAdministrativeUser);

		initForm(ureq);
		loadModel();
		initFiltersPresets(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SharedBindersDataModel.ShareItemCols.binderKey, "select"));

		SortKey defaultSortKey = null;
		// followed by the users fields
		int colPos = SharedBindersController.USER_PROPS_OFFSET;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			String propName = userPropertyHandler.getName();
			if (defaultSortKey == null) {
				defaultSortKey = new SortKey(propName, true);
			}

			boolean visible = userManager.isMandatoryUserProperty(SharedBindersController.USER_PROPS_ID, userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, true, propName));
			colPos++;
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SharedBindersDataModel.ShareItemCols.lastModified));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SharedBindersDataModel.ShareItemCols.recentLaunch));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SharedBindersDataModel.ShareItemCols.openSections, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SharedBindersDataModel.ShareItemCols.selectSections, new SelectSectionsCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SharedBindersDataModel.ShareItemCols.newlyPublishedPage));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SharedBindersDataModel.ShareItemCols.draftPage));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SharedBindersDataModel.ShareItemCols.inRevisionPage));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SharedBindersDataModel.ShareItemCols.closedPage));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SharedBindersDataModel.ShareItemCols.grading,
				new AssessmentEntryCellRenderer(getTranslator())));
		StaticFlexiCellRenderer selectRenderer = new StaticFlexiCellRenderer(translate("select"), "select");
		selectRenderer.setIconRightCSS("o_icon-sw o_icon_start");
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, true, "select", null, -1, "select", false, null,
				FlexiColumnModel.ALIGNMENT_LEFT, selectRenderer));
		StaticFlexiCellRenderer leaveRenderer = new StaticFlexiCellRenderer(translate("leave"), "leave");
		leaveRenderer.setIconRightCSS("o_icon-sw o_icon_delete");
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, false, "leave", null, -2, "leave", false, null,
				FlexiColumnModel.ALIGNMENT_LEFT, leaveRenderer));

		model = new SharedBindersDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 25, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setElementCssClass("o_binder_shared_items_listing");
		tableEl.setEmptyTableMessageKey("table.sEmptyTable");
		tableEl.setAndLoadPersistedPreferences(ureq, "shared-items-v2");

		FlexiTableSortOptions options = new FlexiTableSortOptions();
		if (defaultSortKey != null) {
			options.setDefaultOrderBy(defaultSortKey);
		}
		tableEl.setSortSettings(options);
	}

	private void loadModel() {
		List<AssessedBinder> assessedBinders = portfolioService.searchSharedBindersWith(getIdentity(), null);
		List<SharedItemRow> rows = new ArrayList<>(assessedBinders.size());
		for (AssessedBinder assessedBinder : assessedBinders) {
			Binder binder = portfolioService.getBinderByKey(assessedBinder.getBinderKey());
			// check if binder is related to current course element, if not: skip
			if (binder == null || binder.getSubIdent() == null || !binder.getSubIdent().equals(subIdent)) {
				continue;
			}

			// check if any filter is applied and if then skip this row, instead of adding it to the table
			if (tableEl.getSelectedFilterTab() != null
					&& (tableEl.getSelectedFilterTab().getId().equals(SharedBindersDataModel.NEW_TAB_ID)
					&& assessedBinder.getNumOfNewlyPublishedPages() < 1
					|| (tableEl.getSelectedFilterTab().getId().equals(SharedBindersDataModel.EMPTY_SECTIONS)
					&& assessedBinder.getNumOfOpenSections() < 1))) {
				continue;
			}

			SharedItemRow row = getSharedItemRow(assessedBinder);
			rows.add(row);
		}

		model.setObjects(rows);
		tableEl.reset();
		tableEl.reloadData();
	}

	private SharedItemRow getSharedItemRow(AssessedBinder assessedBinder) {
		SharedItemRow row = new SharedItemRow(assessedBinder.getAssessedIdentity(), userPropertyHandlers, getLocale());
		row.setBinderTitle(assessedBinder.getBinderTitle());
		row.setBinderKey(assessedBinder.getBinderKey());
		row.setLastModified(assessedBinder.getLastModified());
		row.setEntryDisplayName(assessedBinder.getEntryDisplayname());
		row.setAssessmentEntry(assessedBinder);
		row.setRecentLaunch(assessedBinder.getRecentLaunch());
		List<AssessedBinderSection> sections = assessedBinder.getSections();

		row.setSections(sections);
		row.setNumOfOpenSections(assessedBinder.getNumOfOpenSections());
		row.setNumOfDraftPages(assessedBinder.getNumOfDraftPages());
		row.setNumOfInRevisionPages(assessedBinder.getNumOfInRevisionPages());
		row.setNumOfClosedPages(assessedBinder.getNumOfClosedPages());
		row.setNumOfNewlyPublishedPages(assessedBinder.getNumOfNewlyPublishedPages());
		return row;
	}

	private void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();

		// filter: show all
		FlexiFiltersTab allTab = FlexiFiltersTabFactory.tabWithFilters(SharedBindersDataModel.ALL_TAB_ID, translate("filter.show.all"),
				TabSelectionBehavior.clear, List.of());
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);

		FlexiFiltersTab openTab = FlexiFiltersTabFactory.tabWithFilters(SharedBindersDataModel.EMPTY_SECTIONS, translate("filter.sections.open"),
				TabSelectionBehavior.clear, List.of());
		openTab.setFiltersExpanded(true);
		tabs.add(openTab);

		FlexiFiltersTab newTab = FlexiFiltersTabFactory.tabWithFilters(SharedBindersDataModel.NEW_TAB_ID, translate("filter.show.new"),
				TabSelectionBehavior.clear, List.of());
		newTab.setFiltersExpanded(true);
		tabs.add(newTab);

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (confirmLeaveCtrl == source
				&& (DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event))) {
			SharedItemRow row = (SharedItemRow) confirmLeaveCtrl.getUserObject();
			doLeaveBinder(row);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (tableEl == source) {
			if (event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				SharedItemRow row = model.getObject(se.getIndex());
				if ("select".equals(cmd)) {
					String resourceUrl;
					resourceUrl = "[HomeSite:" + getIdentity().getKey() + "][PortfolioV2:0][SharedWithMe:0][Binder:" + row.getBinderKey() + "]";

					BusinessControl bc = BusinessControlFactory.getInstance().createFromString(resourceUrl);
					WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
					NewControllerFactory.getInstance().launch(ureq, bwControl);
				} else if ("leave".equals(cmd)) {
					doConfirmLeaveBinder(ureq, row);
				}
			} else if ("ONCLICK".equals(event.getCommand())) {
				String row = ureq.getParameter("select-section");
				String expand = ureq.getParameter("expand-section");
				if (StringHelper.isLong(row)) {
					int index = Integer.parseInt(row);
					SharedItemRow itemRow = model.getObject(index);
					String sectionParam = ureq.getParameter("section");
					int sectionIndex = Integer.parseInt(sectionParam);
					String resourceUrl;
					resourceUrl = "[HomeSite:" + getIdentity().getKey() + "][PortfolioV2:0][SharedWithMe:0][Binder:" + itemRow.getBinderKey() + "][Entries:0][Section:" + itemRow.getSections().get(sectionIndex).getSectionKey() + "]";

					BusinessControl bc = BusinessControlFactory.getInstance().createFromString(resourceUrl);
					WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
					NewControllerFactory.getInstance().launch(ureq, bwControl);
				} else if (StringHelper.containsNonWhitespace(expand)) {
					doExpandSections(ureq);
				}
			}
			if (event instanceof FlexiTableSearchEvent
					|| event instanceof FlexiTableFilterTabEvent) {
				loadModel();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doLeaveBinder(SharedItemRow row) {
		Binder binder = portfolioService.getBinderByKey(row.getBinderKey());
		portfolioService.removeAccessRights(binder, getIdentity(),
				ContentRoles.coach, ContentRoles.reviewer, ContentRoles.readInvitee, ContentRoles.invitee);
		loadModel();
	}

	private void doConfirmLeaveBinder(UserRequest ureq, SharedItemRow row) {
		String title = translate("leave");
		String text = translate("leave.explain");
		confirmLeaveCtrl = activateOkCancelDialog(ureq, title, text, confirmLeaveCtrl);
		confirmLeaveCtrl.setUserObject(row);
		listenTo(confirmLeaveCtrl);
	}

	private void doExpandSections(UserRequest ureq) {
		try {
			String row = ureq.getParameter("expand-section");
			int index = Integer.parseInt(row);
			SharedItemRow itemRow = model.getObject(index);
			if (itemRow != null) {
				itemRow.setExpandSections(!itemRow.isExpandSections());
			}
			tableEl.getComponent().setDirty(true);
		} catch (NumberFormatException e) {
			logError("", e);
		}
	}

	public SharedBindersDataModel getModel() {
		return model;
	}
}
