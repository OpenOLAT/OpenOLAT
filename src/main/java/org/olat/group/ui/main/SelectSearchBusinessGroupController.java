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
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomCssCellRenderer;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
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
public class SelectSearchBusinessGroupController extends AbstractBusinessGroupListController {
	
	private final boolean restricted;
	private final BusinessGroupSearchController searchController;
	
	public SelectSearchBusinessGroupController(UserRequest ureq, WindowControl wControl, boolean restricted) {
		super(ureq, wControl, "group_list_search");
		this.restricted = restricted;
		//search controller
		searchController = new BusinessGroupSearchController(ureq, wControl, isAdmin(), restricted, false);
		listenTo(searchController);
		mainVC.put("search", searchController.getInitialComponent());
	}

	@Override
	protected boolean canCreateBusinessGroup(UserRequest ureq) {
		return false;
	}

	@Override
	protected void initButtons(UserRequest ureq) {
		groupListCtr.setMultiSelect(true);
		groupListCtr.addMultiSelectAction("select", TABLE_ACTION_SELECT);
	}

	@Override
	protected int initColumns() {
		groupListCtr.addColumnDescriptor(new MarkColumnDescriptor(this, mainVC, getTranslator()));
		CustomCssCellRenderer nameRenderer = new BusinessGroupNameCellRenderer();
		groupListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor(Cols.name.i18n(), Cols.name.ordinal(), TABLE_ACTION_LAUNCH, getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, nameRenderer));
		groupListCtr.addColumnDescriptor(false, new DefaultColumnDescriptor(Cols.key.i18n(), Cols.key.ordinal(), null, getLocale()));
		if(groupModule.isManagedBusinessGroups()) {
			groupListCtr.addColumnDescriptor(false, new DefaultColumnDescriptor(Cols.externalId.i18n(), Cols.externalId.ordinal(), null, getLocale()));
		}
		groupListCtr.addColumnDescriptor(false, new DefaultColumnDescriptor(Cols.description.i18n(), Cols.description.ordinal(), null, getLocale()));
		groupListCtr.addColumnDescriptor( new ResourcesColumnDescriptor(this, mainVC, getTranslator()));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.tutorsCount.i18n(), Cols.tutorsCount.ordinal(), null, getLocale()));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.participantsCount.i18n(), Cols.participantsCount.ordinal(), null, getLocale()));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.freePlaces.i18n(), Cols.freePlaces.ordinal(), null, getLocale()));
		groupListCtr.addColumnDescriptor(new DefaultColumnDescriptor(Cols.waitingListCount.i18n(), Cols.waitingListCount.ordinal(), null, getLocale()));
		groupListCtr.addColumnDescriptor(new StaticColumnDescriptor(TABLE_ACTION_SELECT, "select", translate("select")));
		return 10;
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

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(state instanceof SearchEvent) {
			searchController.activate(ureq, entries, state);
			doSearch(ureq, (SearchEvent)state);
		}
	}

	protected void updateSearch(UserRequest ureq) {
		doSearch(ureq, null);
	}

	private void doSearch(UserRequest ureq, SearchEvent event) {
		long start = isLogDebugEnabled() ? System.currentTimeMillis() : 0;

		search(event);
		
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

	private void search(SearchEvent event) {
		if(event == null) {
			updateTableModel(null, false);
		} else {
			SearchBusinessGroupParams params = event.convertToSearchBusinessGroupParams(getIdentity());
			//security
			if(restricted && !params.isAttendee() && !params.isOwner() && !params.isWaiting()
					&& (params.getPublicGroups() == null || !params.getPublicGroups().booleanValue())) {
				params.setOwner(true);
				params.setAttendee(true);
				params.setWaiting(true);
			}
			updateTableModel(params, false);
		}
	}
}
