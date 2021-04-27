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
import java.util.List;

import org.olat.core.commons.services.mark.Mark;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.model.BusinessGroupQueryParams;
import org.olat.group.model.BusinessGroupRow;
import org.olat.group.model.BusinessGroupSelectionEvent;
import org.olat.group.model.StatisticsBusinessGroupRow;
import org.olat.group.ui.main.AbstractBusinessGroupListController;
import org.olat.group.ui.main.BGAccessControlledCellRenderer;
import org.olat.group.ui.main.BGResourcesCellRenderer;
import org.olat.group.ui.main.BGTableItem;
import org.olat.group.ui.main.BusinessGroupListFlexiTableModel.Cols;
import org.olat.group.ui.main.BusinessGroupNameCellRenderer;
import org.olat.group.ui.main.BusinessGroupViewFilter;
import org.olat.group.ui.main.SearchEvent;
import org.olat.group.ui.main.SelectBusinessGroupController;
import org.olat.group.ui.main.UnmanagedGroupFilter;
import org.olat.ims.lti13.LTI13Service;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryRef;

/**
 * List the business groups of all types linked to a course.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CourseBusinessGroupListController extends AbstractBusinessGroupListController {
	
	public static final String TABLE_ACTION_UNLINK = "tblUnlink";
	public static final String TABLE_ACTION_MULTI_UNLINK = "tblMultiUnlink";
	
	private final RepositoryEntry re;
	private final boolean groupManagementRight;
	private FormLink createGroup, addGroup, removeGroups;

	private DialogBoxController confirmRemoveResource;
	private DialogBoxController confirmRemoveMultiResource;
	private SelectBusinessGroupController selectController;
	
	public CourseBusinessGroupListController(UserRequest ureq, WindowControl wControl, RepositoryEntry re,
			boolean groupManagementRight, boolean readOnly) {
		super(ureq, wControl, "group_list", false, false, readOnly, "course", true, re);
		this.re = re;
		this.groupManagementRight = groupManagementRight;
	}

	@Override
	protected void initButtons(FormItemContainer formLayout, UserRequest ureq) {
		initButtons(formLayout, ureq, !readOnly, false, false);
		
		tableEl.setMultiSelect(!readOnly);
		tableEl.setSelectAllEnable(!readOnly);
		
		boolean managed = RepositoryEntryManagedFlag.isManaged(re, RepositoryEntryManagedFlag.groups);
		if(!managed && !readOnly) {
			duplicateButton = uifactory.addFormLink("table.duplicate", TABLE_ACTION_DUPLICATE, "table.duplicate", null, formLayout, Link.BUTTON);
			tableEl.addBatchButton(duplicateButton);
			mergeButton = uifactory.addFormLink("table.merge", TABLE_ACTION_MERGE, "table.merge", null, formLayout, Link.BUTTON);
			tableEl.addBatchButton(mergeButton);
		}
		if(!readOnly) {
			usersButton = uifactory.addFormLink("table.users.management", TABLE_ACTION_USERS, "table.users.management", null, formLayout, Link.BUTTON);
			tableEl.addBatchButton(usersButton);
			configButton = uifactory.addFormLink("table.config", TABLE_ACTION_CONFIG, "table.config", null, formLayout, Link.BUTTON);
			tableEl.addBatchButton(configButton);
			emailButton = uifactory.addFormLink("table.email", TABLE_ACTION_EMAIL, "table.email", null, formLayout, Link.BUTTON);
			tableEl.addBatchButton(emailButton);
		}

		if(!managed && !readOnly) {
			removeGroups = uifactory.addFormLink("table.header.remove", TABLE_ACTION_MULTI_UNLINK, "table.header.remove", null, formLayout, Link.BUTTON);
			tableEl.addBatchButton(removeGroups);
		}

		createGroup = uifactory.addFormLink("group.create", formLayout, Link.BUTTON);
		createGroup.setElementCssClass("o_sel_course_new_group");
		createGroup.setVisible(!managed && !readOnly);
		createGroup.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		addGroup = uifactory.addFormLink("group.add", formLayout, Link.BUTTON);
		addGroup.setElementCssClass("o_sel_course_select_group");
		addGroup.setVisible(!managed && !readOnly);
		addGroup.setIconLeftCSS("o_icon o_icon-fw o_icon_add_search");
	}

	@Override
	protected FlexiTableColumnModel initColumnModel() {
		boolean managed = RepositoryEntryManagedFlag.isManaged(re, RepositoryEntryManagedFlag.groups);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		//group name
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.name.i18n(), Cols.name.ordinal(), TABLE_ACTION_LAUNCH,
				true, Cols.name.name(), new StaticFlexiCellRenderer(TABLE_ACTION_LAUNCH, new BusinessGroupNameCellRenderer())));
		//id and reference
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.key.i18n(), Cols.key.ordinal(), true, Cols.key.name()));
		if(groupModule.isManagedBusinessGroups()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.externalId.i18n(), Cols.externalId.ordinal(),
					true, Cols.externalId.name()));
		}
		//description
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.description.i18n(), Cols.description.ordinal(),
				false, null, FlexiColumnModel.ALIGNMENT_LEFT, new TextFlexiCellRenderer(EscapeMode.antisamy)));
		//courses
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.resources.i18n(), Cols.resources.ordinal(),
				true, Cols.resources.name(), FlexiColumnModel.ALIGNMENT_LEFT, new BGResourcesCellRenderer(flc)));
		//stats
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.tutorsCount.i18n(), Cols.tutorsCount.ordinal(),
				true, Cols.tutorsCount.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.participantsCount.i18n(), Cols.participantsCount.ordinal(),
				true, Cols.participantsCount.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.freePlaces.i18n(), Cols.freePlaces.ordinal(),
				true, Cols.freePlaces.name(), FlexiColumnModel.ALIGNMENT_LEFT, new TextFlexiCellRenderer(EscapeMode.none)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.waitingListCount.i18n(), Cols.waitingListCount.ordinal(),
				true, Cols.waitingListCount.name()));
		//access
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.accessTypes.i18n(), Cols.accessTypes.ordinal(),
				true, Cols.accessTypes.name(), FlexiColumnModel.ALIGNMENT_LEFT, new BGAccessControlledCellRenderer()));

		if(!readOnly) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.edit", translate("table.header.edit"), TABLE_ACTION_EDIT));
		}
		if(!managed && !readOnly) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.remove", Cols.unlink.ordinal(), TABLE_ACTION_UNLINK,
					new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("table.header.remove"), TABLE_ACTION_UNLINK), null)));
		}
		return columnsModel;
	}

	@Override
	protected List<BGTableItem> searchTableItems(BusinessGroupQueryParams params) {
		List<StatisticsBusinessGroupRow> rows = businessGroupService.findBusinessGroupsFromRepositoryEntry(params, getIdentity(), getResource());
		List<BGTableItem> items = new ArrayList<>(rows.size());
		for(StatisticsBusinessGroupRow row:rows) {
			BusinessGroupMembership membership = row.getMember();
			Boolean allowLeave =  membership != null;
			Boolean allowDelete = isAdmin() ? Boolean.TRUE : (membership == null ? null : Boolean.valueOf(membership.isOwner()));
			
			FormLink markLink = uifactory.addFormLink("mark_" + row.getKey(), "mark", "", null, null, Link.NONTRANSLATED);
			markLink.setIconLeftCSS(row.isMarked() ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
			
			BGTableItem item = new BGTableItem(row, markLink, allowLeave, allowDelete);
			item.setNumOfOwners(row.getNumOfCoaches());
			item.setNumOfParticipants(row.getNumOfParticipants());
			item.setNumWaiting(row.getNumWaiting());
			item.setNumOfPendings(row.getNumPending());
			items.add(item);
		}
		return items;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == createGroup) {
			doCreate(ureq, getWindowControl(), re);
		} else if (source == addGroup) {
			doSelectGroups(ureq);
		} else if(source == removeGroups) {
			List<BusinessGroupRow> selectedItems = getSelectedItems();
			if(selectedItems.isEmpty()) {
				showWarning("error.select.one");
			} else {
				doConfirmRemove(ureq, selectedItems);
			}
		} else if (source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent te = (SelectionEvent) event;
				String cmd = te.getCommand();
				if(TABLE_ACTION_UNLINK.equals(cmd)) {
					if(te.getIndex() >= 0 && te.getIndex() < groupTableModel.getRowCount()) {
						BGTableItem tableItem = groupTableModel.getObject(te.getIndex());
						if(tableItem != null && tableItem.getBusinessGroupKey() != null) {
							doConfirmUnlink(ureq, tableItem.getBusinessGroupKey());
						}
					}
				}
			}
		} 

		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof BusinessGroupSelectionEvent) {
			BusinessGroupSelectionEvent selectionEvent = (BusinessGroupSelectionEvent)event;
			List<BusinessGroup> selectedGroups = selectionEvent.getGroups();
			cmc.deactivate();
			cleanUpPopups();
			addGroupsToCourse(selectedGroups);
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if (source == confirmRemoveResource) {
			if (DialogBoxUIFactory.isYesEvent(event)) { // yes case
				BusinessGroup group = (BusinessGroup)confirmRemoveResource.getUserObject();
				doRemoveBusinessGroups(Collections.singletonList(group));
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if (source == confirmRemoveMultiResource) {
			if (DialogBoxUIFactory.isYesEvent(event)) { // yes case
				@SuppressWarnings("unchecked")
				List<BusinessGroupRow> selectedItems = (List<BusinessGroupRow>)confirmRemoveMultiResource.getUserObject();
				List<BusinessGroup> groups = toBusinessGroups(ureq, selectedItems, false);
				doRemoveBusinessGroups(groups);
			}
		}

		super.event(ureq, source, event);
	}

	@Override
	protected void doCreate(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		ureq.getUserSession().putEntry("wild_card_new", Boolean.TRUE);
		super.doCreate(ureq, wControl, entry);
	}

	@Override
	protected void doAccess(UserRequest ureq, BusinessGroup group) {
		ureq.getUserSession().putEntry("wild_card_" + group.getKey(), Boolean.TRUE);
		super.doAccess(ureq, group);
	}

	@Override
	protected void doLaunch(UserRequest ureq, BusinessGroup group) {
		ureq.getUserSession().putEntry("wild_card_" + group.getKey(), Boolean.TRUE);
		super.doLaunch(ureq, group);
	}

	@Override
	protected void doEdit(UserRequest ureq, BusinessGroup group) {
		ureq.getUserSession().putEntry("wild_card_" + group.getKey(), Boolean.TRUE);
		super.doEdit(ureq, group);
	}
	
	private void doConfirmUnlink(UserRequest ureq, Long businessGroupKey) {
		BusinessGroup group = businessGroupService.loadBusinessGroup(businessGroupKey);
		if(group == null) {
			groupTableModel.removeBusinessGroup(businessGroupKey);
			tableEl.reset();
		} else {
			String text = getTranslator().translate("group.remove", new String[] {
					StringHelper.escapeHtml(group.getName()),
					StringHelper.escapeHtml(re.getDisplayname())
			});
			confirmRemoveResource = activateYesNoDialog(ureq, null, text, confirmRemoveResource);
			confirmRemoveResource.setUserObject(group);
		}
	}

	private void doConfirmRemove(UserRequest ureq, List<BusinessGroupRow> selectedItems) {
		StringBuilder sb = new StringBuilder();
		StringBuilder managedSb = new StringBuilder();
		for(BusinessGroupRow item:selectedItems) {
			String gname = item.getName() == null ? "???" : StringHelper.escapeHtml(item.getName());
			if(BusinessGroupManagedFlag.isManaged(item.getManagedFlags(), BusinessGroupManagedFlag.resources)) {
				if(managedSb.length() > 0) managedSb.append(", ");
				managedSb.append(gname);
			} else {
				if(sb.length() > 0) sb.append(", ");
				sb.append(gname);
			}
		}
		
		if(managedSb.length() > 0) {
			showWarning("error.managed.group", managedSb.toString());
		} else {
			String text = getTranslator().translate("group.remove", new String[] { 
					sb.toString(),
					StringHelper.escapeHtml(re.getDisplayname())
			});
			confirmRemoveMultiResource = activateYesNoDialog(ureq, null, text, confirmRemoveResource);
			confirmRemoveMultiResource.setUserObject(selectedItems);
		}
	}
	
	@Override
	protected void cleanUpPopups() {
		super.cleanUpPopups();
		removeAsListenerAndDispose(selectController);
		selectController = null;
	}
	
	private void doRemoveBusinessGroups(List<BusinessGroup> groups) {
		businessGroupService.removeResourceFrom(groups, re);
		reloadModel();
	}

	protected void doSelectGroups(UserRequest ureq) {
		removeAsListenerAndDispose(selectController);
		BusinessGroupViewFilter filter = new UnmanagedGroupFilter(BusinessGroupManagedFlag.resources);
		selectController = new SelectBusinessGroupController(ureq, getWindowControl(), filter);
		listenTo(selectController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				selectController.getInitialComponent(), true, translate("select.group"));
		cmc.activate();
		listenTo(cmc);
	}
	
	protected void addGroupsToCourse(List<BusinessGroup> groups) {
		List<RepositoryEntry> resources = Collections.singletonList(re);
		businessGroupService.addResourcesTo(groups, resources);
		reloadModel();
	}
	
	@Override
	protected BusinessGroupQueryParams getSearchParams(SearchEvent event) {
		BusinessGroupQueryParams params = event.convertToBusinessGroupQueriesParams();
		params.setRepositoryEntry(re);
		return params;
	}

	@Override
	protected BusinessGroupQueryParams getDefaultSearchParams() {
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		params.setTechnicalTypes(List.of(BusinessGroup.BUSINESS_TYPE, LTI13Service.LTI_GROUP_TYPE));
		params.setRepositoryEntry(re);
		return params;
	}
	
	@Override
	protected boolean filterEditableGroupKeys(UserRequest ureq, List<Long> groupKeys) {
		if(groupManagementRight) {
			return false;
		}
		return super.filterEditableGroupKeys(ureq, groupKeys);
	}

	@Override
	protected void reloadModel() {
		doDefaultSearch();
	}

	@Override
	protected RepositoryEntryRef getResource() {
		return re;
	}
}