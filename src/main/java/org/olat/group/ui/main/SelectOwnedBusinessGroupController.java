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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.ui.main.BusinessGroupTableModelWithType.Cols;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SelectOwnedBusinessGroupController extends AbstractBusinessGroupListController {
	
	public SelectOwnedBusinessGroupController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "select_group_list");
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
		groupListCtr.addColumnDescriptor(new BusinessGroupNameColumnDescriptor(TABLE_ACTION_LAUNCH, getLocale()));
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
	
	protected void updateOwnedGroups() {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setIdentity(getIdentity());
		params.setOwner(true);
		params.setAttendee(false);
		params.setWaiting(false);
		updateTableModel(params, false);
	}
}
