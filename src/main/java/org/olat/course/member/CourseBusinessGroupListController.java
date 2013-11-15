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

import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.model.BusinessGroupSelectionEvent;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.ui.main.AbstractBusinessGroupListController;
import org.olat.group.ui.main.BGAccessControlledCellRenderer;
import org.olat.group.ui.main.BGTableItem;
import org.olat.group.ui.main.BusinessGroupNameColumnDescriptor;
import org.olat.group.ui.main.BusinessGroupTableModelWithType.Cols;
import org.olat.group.ui.main.BusinessGroupViewFilter;
import org.olat.group.ui.main.SelectBusinessGroupController;
import org.olat.group.ui.main.UnmanagedGroupFilter;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.resource.OLATResource;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CourseBusinessGroupListController extends AbstractBusinessGroupListController {
	
	public static String TABLE_ACTION_UNLINK = "tblUnlink";
	public static String TABLE_ACTION_MULTI_UNLINK = "tblMultiUnlink";
	
	private final RepositoryEntry re;
	private final Link createGroup;
	private final Link addGroup;

	private DialogBoxController confirmRemoveResource;
	private DialogBoxController confirmRemoveMultiResource;
	private SelectBusinessGroupController selectController;
	
	public CourseBusinessGroupListController(UserRequest ureq, WindowControl wControl, RepositoryEntry re) {
		super(ureq, wControl, "group_list", re);
		this.re = re;
		
		boolean managed = RepositoryEntryManagedFlag.isManaged(re, RepositoryEntryManagedFlag.groups);
		createGroup = LinkFactory.createButton("group.create", mainVC, this);
		createGroup.setVisible(!managed);
		mainVC.put("createGroup", createGroup);
		addGroup = LinkFactory.createButton("group.add", mainVC, this);
		addGroup.setVisible(!managed);
		mainVC.put("addGroup", addGroup);
	}

	@Override
	protected void initButtons(UserRequest ureq) {
		initButtons(ureq, true);
		groupListCtr.setMultiSelect(true);
		
		RepositoryEntry re = (RepositoryEntry)getUserObject();
		boolean managed = RepositoryEntryManagedFlag.isManaged(re, RepositoryEntryManagedFlag.groups);
		if(!managed) {
			groupListCtr.addMultiSelectAction("table.duplicate", TABLE_ACTION_DUPLICATE);
			groupListCtr.addMultiSelectAction("table.merge", TABLE_ACTION_MERGE);
		}
		groupListCtr.addMultiSelectAction("table.users.management", TABLE_ACTION_USERS);
		groupListCtr.addMultiSelectAction("table.config", TABLE_ACTION_CONFIG);
		groupListCtr.addMultiSelectAction("table.email", TABLE_ACTION_EMAIL);
		if(!managed) {
			groupListCtr.addMultiSelectAction("table.header.remove", TABLE_ACTION_MULTI_UNLINK);
		}
	}

	@Override
	protected int initColumns() {
		RepositoryEntry re = (RepositoryEntry)getUserObject();
		boolean managed = RepositoryEntryManagedFlag.isManaged(re, RepositoryEntryManagedFlag.groups);
		
		groupListCtr.addColumnDescriptor(new BusinessGroupNameColumnDescriptor(TABLE_ACTION_LAUNCH, getLocale()));
		groupListCtr.addColumnDescriptor(false, new DefaultColumnDescriptor(Cols.key.i18n(), Cols.key.ordinal(), null, getLocale()));
		if(groupModule.isManagedBusinessGroups()) {
			groupListCtr.addColumnDescriptor(false, new DefaultColumnDescriptor(Cols.externalId.i18n(), Cols.externalId.ordinal(), null, getLocale()));
		}
		groupListCtr.addColumnDescriptor(false, new DefaultColumnDescriptor(Cols.description.i18n(), Cols.description.ordinal(), null, getLocale()));
		groupListCtr.addColumnDescriptor(new ResourcesColumnDescriptor(this, mainVC, getTranslator()));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.tutorsCount.i18n(), Cols.tutorsCount.ordinal(), null, getLocale()));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.participantsCount.i18n(), Cols.participantsCount.ordinal(), null, getLocale()));
		DefaultColumnDescriptor freeplacesCol = new DefaultColumnDescriptor(Cols.freePlaces.i18n(), Cols.freePlaces.ordinal(), TABLE_ACTION_LAUNCH, getLocale());
		freeplacesCol.setEscapeHtml(EscapeMode.none);
		groupListCtr.addColumnDescriptor(freeplacesCol);
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.waitingListCount.i18n(), Cols.waitingListCount.ordinal(), null, getLocale()));
		CustomCellRenderer acRenderer = new BGAccessControlledCellRenderer();
		groupListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Cols.accessTypes.i18n(), Cols.accessTypes.ordinal(), null, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, acRenderer));
		groupListCtr.addColumnDescriptor(new StaticColumnDescriptor(TABLE_ACTION_EDIT, "table.header.edit", translate("table.header.edit")));
		if(!managed) {
			groupListCtr.addColumnDescriptor(new RemoveActionColumnDescriptor("table.header.remove", Cols.wrapper.ordinal(), getTranslator()));
		}
		return 11;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == createGroup) {
			doCreate(ureq, getWindowControl(), re);
		} else if (source == addGroup) {
			doSelectGroups(ureq);
		} else {
			super.event(ureq, source, event);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof BusinessGroupSelectionEvent) {
			BusinessGroupSelectionEvent selectionEvent = (BusinessGroupSelectionEvent)event;
			List<BusinessGroup> selectedGroups = selectionEvent.getGroups();
			cmc.deactivate();
			cleanUpPopups();
			addGroupsToCourse(selectedGroups);
		} else if (source == groupListCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if(TABLE_ACTION_UNLINK.equals(actionid)) {
					Long businessGroupKey = groupListModel.getObject(te.getRowId()).getBusinessGroupKey();
					BusinessGroup group = businessGroupService.loadBusinessGroup(businessGroupKey);
					String text = getTranslator().translate("group.remove", new String[] {
							StringHelper.escapeHtml(group.getName()),
							StringHelper.escapeHtml(re.getDisplayname())
					});
					confirmRemoveResource = activateYesNoDialog(ureq, null, text, confirmRemoveResource);
					confirmRemoveResource.setUserObject(group);
				}
			} else if (event instanceof TableMultiSelectEvent) {
				TableMultiSelectEvent te = (TableMultiSelectEvent)event;
				if(TABLE_ACTION_MULTI_UNLINK.equals(te.getAction())) {
					List<BGTableItem> selectedItems = groupListModel.getObjects(te.getSelection());
					if(selectedItems.isEmpty()) {
						showWarning("error.select.one");
					} else {
						doConfirmRemove(ureq, selectedItems);
					}
				}
			}
		} else if (source == confirmRemoveResource) {
			if (DialogBoxUIFactory.isYesEvent(event)) { // yes case
				BusinessGroup group = (BusinessGroup)confirmRemoveResource.getUserObject();
				doRemoveBusinessGroups(Collections.singletonList(group));
			}
		} else if (source == confirmRemoveMultiResource) {
			if (DialogBoxUIFactory.isYesEvent(event)) { // yes case
				@SuppressWarnings("unchecked")
				List<BGTableItem> selectedItems = (List<BGTableItem>)confirmRemoveMultiResource.getUserObject();
				List<BusinessGroup> groups = toBusinessGroups(ureq, selectedItems, false);
				doRemoveBusinessGroups(groups);
			}
		}

		super.event(ureq, source, event);
	}
	
	private void doConfirmRemove(UserRequest ureq, List<BGTableItem> selectedItems) {
		StringBuilder sb = new StringBuilder();
		StringBuilder managedSb = new StringBuilder();
		for(BGTableItem item:selectedItems) {
			String gname = item.getBusinessGroupName() == null ? "???" : StringHelper.escapeHtml(item.getBusinessGroupName());
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
		mainVC.setDirty(true);
	}

	@Override
	protected void reloadModel() {
		updateTableModel(new SearchBusinessGroupParams(), false);
	}

	@Override
	protected OLATResource getResource() {
		return re.getOlatResource();
	}
}