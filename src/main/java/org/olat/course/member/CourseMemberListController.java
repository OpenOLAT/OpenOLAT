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
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.group.ui.main.AbstractMemberListController;
import org.olat.group.ui.main.MemberListSecurityCallback;
import org.olat.group.ui.main.MemberRow;
import org.olat.group.ui.main.SearchMembersParams;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CourseMemberListController extends AbstractMemberListController implements FlexiTableComponentDelegate {
	
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
	protected void initBatchButtons(FormItemContainer formLayout) {
		//
	}

	@Override
	protected void initDetails() {
		detailsVC = createVelocityContainer("member_details");

		membersTable.setDetailsRenderer(detailsVC, this);
		membersTable.setMultiDetails(true);
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
}