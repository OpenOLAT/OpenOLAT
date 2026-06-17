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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionBrowserEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import java.util.Collection;

import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.manager.CurriculumElementToDoProvider;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.modules.curriculum.ui.member.AbstractMembersController;
import org.olat.user.PortraitSize;
import org.olat.user.PortraitUser;
import org.olat.user.UserManager;
import org.olat.user.UserPortraitComponent;
import org.olat.user.UserPortraitFactory;
import org.olat.user.UserPortraitService;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 29 Apr 2026<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementToDoMemberController extends FormBasicController {

	private static final String FILTER_ROLE = "Role";
	private static final List<String> ORDERED_ROLES = List.of(
			OrganisationRoles.administrator.name(),
			CurriculumRoles.curriculummanager.name(),
			CurriculumRoles.curriculumowner.name(),
			CurriculumRoles.curriculumelementowner.name(),
			CurriculumRoles.owner.name());

	private FlexiTableElement tableEl;
	private MemberDataModel dataModel;

	private final Collection<CurriculumElement> elements;
	private final List<UserPropertyHandler> userPropertyHandlers;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CurriculumElementToDoProvider curriculumElementToDoProvider;
	@Autowired
	private UserPortraitService userPortraitService;

	public CurriculumElementToDoMemberController(UserRequest ureq, WindowControl wControl,
			Collection<CurriculumElement> elements) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.elements = elements;

		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(
				AbstractMembersController.usageIdentifyer, isAdministrativeUser);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		DefaultFlexiColumnModel portraitCol = new DefaultFlexiColumnModel(MemberCols.portrait);
		portraitCol.setExportable(false);
		portraitCol.setIconHeader("o_icon o_icon_profile");
		portraitCol.setHeaderTooltip(translate("user.portrait"));
		columnsModel.addFlexiColumnModel(portraitCol);

		int colIndex = AbstractMembersController.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler handler = userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(AbstractMembersController.usageIdentifyer, handler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible,
					handler.i18nColumnDescriptorLabelKey(), colIndex, true, "userProp-" + colIndex));
			colIndex++;
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.role));

		dataModel = new MemberDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "members", dataModel, 20, false, getTranslator(),
				formLayout);
		tableEl.setSelection(true, true, true);
		tableEl.setSelectAllEnable(true);

		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(MemberCols.role.name(), true));
		tableEl.setSortSettings(sortOptions);

		initFilters();
		initFiltersPresets(ureq);

		loadModel();

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setElementCssClass("o_button_group o_button_group_center");
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("select", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	private void initFilters() {
		SelectionValues rolesValues = new SelectionValues();
		for (String role : ORDERED_ROLES) {
			rolesValues.add(SelectionValues.entry(role, translate("role." + role)));
		}
		FlexiTableMultiSelectionFilter rolesFilter = new FlexiTableMultiSelectionFilter(translate("filter.roles"),
				FILTER_ROLE, rolesValues, true);
		tableEl.setFilters(true, List.of(rolesFilter), false, false);
	}

	private void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();

		FlexiFiltersTab allTab = FlexiFiltersTabFactory.tabWithImplicitFilters("all", translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		tabs.add(allTab);

		FlexiFiltersTab coursePlannersTab = FlexiFiltersTabFactory.tabWithImplicitFilters(
				CurriculumRoles.curriculummanager.name(), translate("role.curriculummanager"),
				TabSelectionBehavior.nothing,
				List.of(FlexiTableFilterValue.valueOf(FILTER_ROLE, List.of(CurriculumRoles.curriculummanager.name()))));
		tabs.add(coursePlannersTab);

		FlexiFiltersTab productOwnersTab = FlexiFiltersTabFactory.tabWithImplicitFilters(
				CurriculumRoles.curriculumowner.name(), translate("role.curriculumowner"),
				TabSelectionBehavior.nothing,
				List.of(FlexiTableFilterValue.valueOf(FILTER_ROLE, List.of(CurriculumRoles.curriculumowner.name()))));
		tabs.add(productOwnersTab);

		FlexiFiltersTab elementOwnersTab = FlexiFiltersTabFactory.tabWithImplicitFilters(
				CurriculumRoles.curriculumelementowner.name(), translate("role.curriculumelementowner"),
				TabSelectionBehavior.nothing,
				List.of(FlexiTableFilterValue.valueOf(FILTER_ROLE, List.of(CurriculumRoles.curriculumelementowner.name()))));
		tabs.add(elementOwnersTab);

		FlexiFiltersTab courseOwnersTab = FlexiFiltersTabFactory.tabWithImplicitFilters(
				CurriculumRoles.owner.name(), translate("role.owner"),
				TabSelectionBehavior.nothing,
				List.of(FlexiTableFilterValue.valueOf(FILTER_ROLE, List.of(CurriculumRoles.owner.name()))));
		tabs.add(courseOwnersTab);

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}

	private void loadModel() {
		List<CurriculumMember> members = curriculumElementToDoProvider.getCandidates(elements);

		Map<Long, Identity> identityByKey = new HashMap<>();
		Map<Long, Set<String>> rolesByKey = new HashMap<>();
		for (CurriculumMember member : members) {
			Long key = member.getIdentity().getKey();
			identityByKey.put(key, member.getIdentity());
			rolesByKey.computeIfAbsent(key, k -> new HashSet<>())
					.add(member.getRole());
		}

		Map<Long, PortraitUser> portraitUsersByKey = userPortraitService
				.createPortraitUsers(getLocale(), identityByKey.values()).stream()
				.collect(Collectors.toMap(PortraitUser::getIdentityKey, Function.identity()));

		List<MemberRow> rows = identityByKey.entrySet().stream()
				.map(e -> {
					Set<String> rowRoles = rolesByKey.get(e.getKey());
					String translatedRoles = ORDERED_ROLES.stream()
							.filter(rowRoles::contains)
							.map(role -> translate("role." + role))
							.collect(Collectors.joining(", "));
					MemberRow row = new MemberRow(e.getValue(), userPropertyHandlers, rowRoles, translatedRoles, getLocale());
					forgePortrait(row, portraitUsersByKey.get(e.getKey()));
					return row;
				})
				.toList();

		dataModel.setObjects(rows);
		dataModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(true, true, true);
	}

	private void forgePortrait(MemberRow row, PortraitUser portraitUser) {
		UserPortraitComponent portraitComp = UserPortraitFactory
				.createUserPortrait("portrait_" + row.getIdentityKey(), tableEl, getLocale());
		portraitComp.setPortraitUser(portraitUser);
		portraitComp.setSize(PortraitSize.small);
		row.setPortraitComp(portraitComp);
	}

	public List<Identity> getSelectedIdentities() {
		Set<Integer> selectedIndices = tableEl.getMultiSelectedIndex();
		return selectedIndices.stream().map(index -> dataModel.getObject(index).getIdentity()).toList();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (tableEl == source && event instanceof FlexiTableFilterTabEvent) {
			dataModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
			tableEl.reset(true, true, true);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<String> keys = getSelectedIdentities().stream()
				.map(id -> id.getKey().toString())
				.toList();
		fireEvent(ureq, new ObjectSelectionBrowserEvent(keys));
	}

	private static final class MemberRow extends UserPropertiesRow {

		private final Identity identity;
		private final Set<String> roles;
		private final String translatedRoles;
		private UserPortraitComponent portraitComp;

		public MemberRow(Identity identity, List<UserPropertyHandler> handlers, Set<String> roles,
				String translatedRoles, Locale locale) {
			super(identity, handlers, locale);
			this.identity = identity;
			this.roles = roles;
			this.translatedRoles = translatedRoles;
		}

		public Identity getIdentity() {
			return identity;
		}

		public boolean hasRole(String role) {
			return roles.contains(role);
		}

		public String getRoles() {
			return translatedRoles;
		}

		public UserPortraitComponent getPortraitComp() {
			return portraitComp;
		}

		public void setPortraitComp(UserPortraitComponent portraitComp) {
			this.portraitComp = portraitComp;
		}
	}

	private static class MemberDataModel extends DefaultFlexiTableDataModel<MemberRow>
			implements FilterableFlexiTableModel {

		private List<MemberRow> backupRows = List.of();

		public MemberDataModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}

		@Override
		public void setObjects(List<MemberRow> objects) {
			backupRows = objects;
			super.setObjects(objects);
		}

		@Override
		public void filter(String quickSearch, List<FlexiTableFilter> filters) {
			List<String> roles = getRolesFilter(filters);
			if (roles.isEmpty()) {
				super.setObjects(backupRows);
			} else {
				List<MemberRow> filtered = backupRows.stream()
						.filter(row -> roles.stream().anyMatch(row::hasRole))
						.toList();
				super.setObjects(filtered);
			}
		}

		private List<String> getRolesFilter(List<FlexiTableFilter> filters) {
			FlexiTableFilter rolesFilter = FlexiTableFilter.getFilter(filters, FILTER_ROLE);
			if (rolesFilter instanceof FlexiTableExtendedFilter extendedFilter) {
				List<String> values = extendedFilter.getValues();
				if (values != null && !values.isEmpty()) {
					return values;
				}
			}
			return List.of();
		}

		@Override
		public Object getValueAt(int row, int col) {
			MemberRow member = getObject(row);
			if (col >= AbstractMembersController.USER_PROPS_OFFSET) {
				return member.getIdentityProp(col - AbstractMembersController.USER_PROPS_OFFSET);
			}
			return switch (col) {
			case 0 -> member.getPortraitComp();
			case 1 -> member.getRoles();
			default -> null;
			};
		}
	}

	private enum MemberCols implements FlexiColumnDef {
		portrait("table.header.portrait"), role("table.header.role");

		private final String i18nKey;

		private MemberCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}

}
