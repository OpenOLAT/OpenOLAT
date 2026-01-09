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
package org.olat.course.member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroupShort;
import org.olat.group.model.MemberView;
import org.olat.group.ui.main.AbstractMemberListController;
import org.olat.group.ui.main.MemberListSecurityCallback;
import org.olat.group.ui.main.MemberListTableModel;
import org.olat.group.ui.main.MemberRow;
import org.olat.group.ui.main.SearchMembersParams;
import org.olat.modules.curriculum.CurriculumElementShort;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CourseMemberListController extends AbstractMemberListController implements FlexiTableComponentDelegate {
	public static final String FILTER_BUSINESS_GROUP = "filter.business.group";
	public static final String FILTER_CURRICULUM_ELEMENT = "filter.curriculum.element";
	
	private final SearchMembersParams searchParams;
	
	private VelocityContainer detailsVC;
	
	@Autowired
	private CurriculumModule curriculumModule;

	public CourseMemberListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			RepositoryEntry repoEntry, MemberListSecurityCallback secCallback, SearchMembersParams searchParams, String infos) {
		super(ureq, wControl, repoEntry, "member_list_origin_filter", secCallback, toolbarPanel);
		this.searchParams = searchParams;
		
		if(StringHelper.containsNonWhitespace(infos)) {
			flc.contextPut("infos", infos);
		}
	}

	@Override
	protected void initChatColumn(FlexiTableColumnModel columnsModel) {
		//
	}

	@Override
	protected void initStatusColumn(FlexiTableColumnModel columnsModel) {
		//
	}
	
	@Override
	protected void initOriginColumn(FlexiTableColumnModel columnsModel) {
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberListTableModel.Cols.origin, new OriginCellRenderer()));
	}

	@Override
	protected void initGroupsColumn(FlexiTableColumnModel columnsModel) {
		//
	}

	@Override
	protected void initDetails() {
		detailsVC = createVelocityContainer("member_details");

		membersTable.setDetailsRenderer(detailsVC, this);
		membersTable.setMultiDetails(true);
	}

	@Override
	protected void initExtraFilters(List<FlexiTableExtendedFilter> filters) {
		SearchMembersParams searchParams = new SearchMembersParams(GroupRoles.owner, GroupRoles.coach, GroupRoles.participant);
		List<MemberView> memberViews = memberQueries.getRepositoryEntryMembers(repoEntry, searchParams);
		Set<BusinessGroupShort> businessGroups = new HashSet<>();
		Set<CurriculumElementShort> curriculumElements = new HashSet<>();
		for (MemberView memberView : memberViews) {
			if (memberView.getGroups() != null) {
				businessGroups.addAll(memberView.getGroups());
			}
			if (memberView.getCurriculumElements() != null) {
				curriculumElements.addAll(memberView.getCurriculumElements());
			}
		}

		SelectionValues businessGroupKV = new SelectionValues();
		businessGroups.stream().sorted(Comparator.comparing(BusinessGroupShort::getName))
				.forEach(bg -> businessGroupKV.add(SelectionValues.entry("" + bg.getKey(), bg.getName())));
		if (!businessGroupKV.isEmpty()) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.member.group"),
					FILTER_BUSINESS_GROUP, businessGroupKV, true));
		}

		SelectionValues curriculumElementKV = new SelectionValues();
		curriculumElements.stream().sorted(Comparator.comparing(CurriculumElementShort::getDisplayName))
				.forEach(ce -> curriculumElementKV.add(SelectionValues.entry("" + ce.getKey(), ce.getDisplayName())));
		if (!curriculumElementKV.isEmpty()) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.member.cpl.element"),
					FILTER_CURRICULUM_ELEMENT, curriculumElementKV, true));
		}
	}

	@Override
	protected void initOriginFiltersPresets(List<FlexiFiltersTab> tabs) {
		boolean showOriginCpl = curriculumModule.isEnabled();
		
		FlexiFiltersTab originCourseTab = FlexiFiltersTabFactory.tabWithImplicitFilters("OriginCourse", 
				translate("filter.preset.origin.course"), TabSelectionBehavior.reloadData, 
				List.of(FlexiTableFilterValue.valueOf(FILTER_ORIGIN, SearchMembersParams.Origin.repositoryEntry.name())));
		originCourseTab.setElementCssClass("o_sel_members_preset_origin_course");
		originCourseTab.setFiltersExpanded(true);
		tabs.add(originCourseTab);

		FlexiFiltersTab originGroupTab = FlexiFiltersTabFactory.tabWithImplicitFilters("OriginGroup",
				translate("filter.preset.origin.group"), TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(FILTER_ORIGIN, SearchMembersParams.Origin.businessGroup.name())));
		originGroupTab.setElementCssClass("o_sel_members_preset_origin_group");
		originGroupTab.setFiltersExpanded(true);
		tabs.add(originGroupTab);

		if (showOriginCpl) {
			FlexiFiltersTab originCplTab = FlexiFiltersTabFactory.tabWithImplicitFilters("OriginCPL",
					translate("filter.preset.origin.cpl"), TabSelectionBehavior.reloadData,
					List.of(FlexiTableFilterValue.valueOf(FILTER_ORIGIN, SearchMembersParams.Origin.curriculum.name())));
			originCplTab.setElementCssClass("o_sel_members_preset_origin_cpl");
			originCplTab.setFiltersExpanded(true);
			tabs.add(originCplTab);
		}
	}

	@Override
	protected void initOriginFilter(List<FlexiTableExtendedFilter> filters, boolean withOwners) {
		SelectionValues originValues = new SelectionValues();
		originValues.add(SelectionValues.entry(SearchMembersParams.Origin.repositoryEntry.name(), translate("filter.member.origin.course")));
		originValues.add(SelectionValues.entry(SearchMembersParams.Origin.businessGroup.name(), translate("filter.member.origin.group")));
		originValues.add(SelectionValues.entry(SearchMembersParams.Origin.curriculum.name(), translate("filter.member.origin.course.planner")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.member.origin"),
				FILTER_ORIGIN, originValues, withOwners));
	}

	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>();
		if (rowObject instanceof MemberRow memberRow && memberRow.getDetailsController() != null) {
			components.add(memberRow.getDetailsController().getInitialFormItem().getComponent());
		}
		return components;
	}

	@Override
	protected void doOpenDetails(UserRequest ureq, MemberRow row) {
		if (row == null) {
			return;
		}

		if (row.getDetailsController() != null) {
			removeAsListenerAndDispose(row.getDetailsController());
			flc.remove(row.getDetailsController().getInitialFormItem());
		}

		Identity memberIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		CourseMemberDetailsController detailsCtrl = new CourseMemberDetailsController(ureq, getWindowControl(), mainForm,
				memberIdentity, repoEntry.getKey());
		detailsCtrl.setUserObject(row);
		listenTo(detailsCtrl);
		row.setDetailsController(detailsCtrl);
		flc.add(detailsCtrl.getInitialFormItem());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source instanceof CourseMemberDetailsController detailsCtrl) {
			if (event == CourseMemberDetailsController.EDIT_EVENT) {
				MemberRow row = (MemberRow) detailsCtrl.getUserObject();
				openEdit(ureq, row);
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (membersTable == source) {
			for (String parameterKey : ureq.getParameterSet()) {
				if (parameterKey.startsWith(OriginCellRenderer.BUSINESS_GROUP_ACTION_PREFIX)) {
					String businessGroupKey = parameterKey.substring(OriginCellRenderer.BUSINESS_GROUP_ACTION_PREFIX.length());
				    doLaunchBusinessGroup(ureq, businessGroupKey);
				}
				if (parameterKey.startsWith(OriginCellRenderer.CURRICULUM_ELEMENT_ACTION_PREFIX)) {
					String curriculumElementKey = parameterKey.substring(OriginCellRenderer.CURRICULUM_ELEMENT_ACTION_PREFIX.length());
					doLaunchCurriculumElement(ureq, curriculumElementKey);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doLaunchBusinessGroup(UserRequest ureq, String businessGroupKey) {
		String businessPath = "[BusinessGroup:" + businessGroupKey + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

	private void doLaunchCurriculumElement(UserRequest ureq, String curriculumElementKey) {
		String businessPath = "[CurriculumAdmin:0][Implementations:0][CurriculumElement:" + curriculumElementKey + "][Overview:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

	@Override
	protected void doCloseDetails(MemberRow row) {
		if (row.getDetailsController() != null) {
			removeAsListenerAndDispose(row.getDetailsController());
			flc.remove(row.getDetailsController().getInitialFormItem());
			row.setDetailsController(null);
		}
	}

	@Override
	public SearchMembersParams getSearchParams() {
		return searchParams;
	}

	@Override
	protected void setExtraSearchCriteria(SearchMembersParams searchParams) {
		searchParams.setBusinessGroupKeys(getBusinessGroupKeys());
		searchParams.setCurriculumElementKeys(getCurriculumElementKeys());
	}

	private List<Long> getBusinessGroupKeys() {
		List<FlexiTableFilter> filters = membersTable.getFilters();
		FlexiTableFilter businessGroupFilter = FlexiTableFilter.getFilter(filters, FILTER_BUSINESS_GROUP);
		if (businessGroupFilter != null) {
			List<String> businessGroupValues = ((FlexiTableExtendedFilter)businessGroupFilter).getValues();
			if (businessGroupValues != null && !businessGroupValues.isEmpty()) {
				return businessGroupValues.stream().map(Long::parseLong).toList();
			}
		}
		return Collections.emptyList();
	}

	private List<Long> getCurriculumElementKeys() {
		List<FlexiTableFilter> filters = membersTable.getFilters();
		FlexiTableFilter curriculumElementFilter = FlexiTableFilter.getFilter(filters, FILTER_CURRICULUM_ELEMENT);
		if (curriculumElementFilter != null) {
			List<String> curriculumElementValues = ((FlexiTableExtendedFilter)curriculumElementFilter).getValues();
			if (curriculumElementValues != null && !curriculumElementValues.isEmpty()) {
				return curriculumElementValues.stream().map(Long::parseLong).toList();
			}
		}
		return Collections.emptyList();
	}
}