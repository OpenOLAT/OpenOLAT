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
package org.olat.group.ui.main;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.table.BooleanColumnDescriptor;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.table.CustomCssCellRenderer;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.ui.main.BusinessGroupTableModelWithType.Cols;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class AdminBusinessGroupsController extends AbstractBusinessGroupListController {

	private final BusinessGroupSearchController searchController;

	public AdminBusinessGroupsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "admin_group_list");
		if(isAdmin()) {
			searchController = new BusinessGroupSearchController(ureq, wControl, true, false, true);
		} else {
			searchController = new BusinessGroupSearchController(ureq, wControl, false, true, false);
		}
		//search controller
		listenTo(searchController);
		mainVC.put("searchPanel", searchController.getInitialComponent());
	}

	@Override
	protected void initButtons(UserRequest ureq) {
		initButtons(ureq, true);
		groupListCtr.setMultiSelect(true);
		groupListCtr.addMultiSelectAction("table.duplicate", TABLE_ACTION_DUPLICATE);
		groupListCtr.addMultiSelectAction("table.merge", TABLE_ACTION_MERGE);
		groupListCtr.addMultiSelectAction("table.users.management", TABLE_ACTION_USERS);
		groupListCtr.addMultiSelectAction("table.config", TABLE_ACTION_CONFIG);
		groupListCtr.addMultiSelectAction("table.email", TABLE_ACTION_EMAIL);
		groupListCtr.addMultiSelectAction("table.delete", TABLE_ACTION_DELETE);
	}

	@Override
	protected int initColumns() {
		CustomCellRenderer markRenderer = new BGMarkCellRenderer(this, mainVC, getTranslator());
		groupListCtr.addColumnDescriptor(false, new CustomRenderColumnDescriptor(Cols.mark.i18n(), Cols.resources.ordinal(), null, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, markRenderer));
		CustomCssCellRenderer nameRenderer = new BusinessGroupNameCellRenderer();
		groupListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Cols.name.i18n(), Cols.name.ordinal(), TABLE_ACTION_LAUNCH, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, nameRenderer));
		groupListCtr.addColumnDescriptor(false, new DefaultColumnDescriptor(Cols.key.i18n(), Cols.key.ordinal(), null, getLocale()));
		groupListCtr.addColumnDescriptor(false, new DefaultColumnDescriptor(Cols.description.i18n(), Cols.description.ordinal(), null, getLocale()));
		CustomCellRenderer resourcesRenderer = new BGResourcesCellRenderer(this, mainVC, getTranslator());
		groupListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Cols.resources.i18n(), Cols.resources.ordinal(), null, getLocale(),  ColumnDescriptor.ALIGNMENT_LEFT, resourcesRenderer));
		CustomCellRenderer acRenderer = new BGAccessControlledCellRenderer();
		groupListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Cols.accessTypes.i18n(), Cols.accessTypes.ordinal(), null, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, acRenderer));
		groupListCtr.addColumnDescriptor(false, new DefaultColumnDescriptor(Cols.firstTime.i18n(), Cols.firstTime.ordinal(), null, getLocale()));
		groupListCtr.addColumnDescriptor(false, new DefaultColumnDescriptor(Cols.lastTime.i18n(), Cols.lastTime.ordinal(), null, getLocale()));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.lastUsage.i18n(), Cols.lastUsage.ordinal(), null, getLocale()));
		CustomCellRenderer roleRenderer = new BGRoleCellRenderer(getLocale());
		groupListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Cols.role.i18n(), Cols.role.ordinal(), null, getLocale(),  ColumnDescriptor.ALIGNMENT_LEFT, roleRenderer));
		groupListCtr.addColumnDescriptor(false, new BooleanColumnDescriptor(Cols.allowLeave.i18n(), Cols.allowLeave.ordinal(), TABLE_ACTION_LEAVE, translate("table.header.leave"), null));
		groupListCtr.addColumnDescriptor(new BooleanColumnDescriptor(Cols.allowDelete.i18n(), Cols.allowDelete.ordinal(), TABLE_ACTION_DELETE, translate("table.header.delete"), null));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.accessControlLaunch.i18n(), Cols.accessControlLaunch.ordinal(), TABLE_ACTION_ACCESS, getLocale()));
		return 9;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(state instanceof SearchEvent) {
			searchController.activate(ureq, entries, state);
			doSearch(ureq, (SearchEvent)state);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == searchController) {
			if(event instanceof SearchEvent) {
				doSearch(ureq, (SearchEvent)event);
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doSearch(UserRequest ureq, SearchEvent event) {
		long start = isLogDebugEnabled() ? System.currentTimeMillis() : 0;

		SearchBusinessGroupParams params = event.convertToSearchBusinessGroupParams(getIdentity());
		params.setOwner(false);
		params.setAttendee(false);
		params.setWaiting(false);
		updateTableModel(params, false);
		
		//back button
		ContextEntry currentEntry = getWindowControl().getBusinessControl().getCurrentContextEntry();
		if(currentEntry != null) {
			currentEntry.setTransientState(event);
		}
		addToHistory(ureq, this);

		if(isLogDebugEnabled()) {
			logDebug("Group search takes (ms): " + (System.currentTimeMillis() - start), null);
		}
	}
}
