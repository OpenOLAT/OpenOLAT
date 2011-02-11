/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) 1999-2008 at frentix GmbH, Switzerland, http://www.frentix.com
* <p>
*/
package org.olat.admin.user.groups;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.ui.BGControllerFactory;
import org.olat.group.ui.BusinessGroupTableModel;

/**
 * Description:<br>
 * GroupOverviewController creates a model and displays a table with all groups a user is in.
 * The following rows are shown: type of group, groupname, 
 * role of user in group (participant, owner, on waiting list), date of joining the group
 * 
 * <P>
 * Initial Date:  22.09.2008 <br>
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class GroupOverviewController extends BasicController {
	private VelocityContainer vc;
	private TableController tblCtr;
	private GroupOverviewModel tableDataModel;
	private WindowControl wControl;
	private Identity identity;
	private static String TABLE_ACTION_LAUNCH ;

	public GroupOverviewController(UserRequest ureq, WindowControl control, Identity identity, Boolean canStartGroups) {
		super(ureq, control, Util.createPackageTranslator(BusinessGroupTableModel.class, ureq.getLocale()));
		this.wControl = control;
		this.identity = identity;
		if (canStartGroups){
			TABLE_ACTION_LAUNCH = "bgTblLaunch";
		} else {
			TABLE_ACTION_LAUNCH = null;
		}		
		
		vc = createVelocityContainer("groupoverview");
		buildTableController(ureq, control); 
		vc.put("table.groups", tblCtr.getInitialComponent());	
		putInitialPanel(vc);
	}

	/**
	 * @param ureq
	 * @param control
	 * @param identity
	 * @return
	 */
	private void buildTableController(UserRequest ureq, WindowControl control) {
		
		removeAsListenerAndDispose(tblCtr);
		tblCtr = new TableController(null, ureq, control, getTranslator());
		listenTo(tblCtr);
		
		tblCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.group.type", 0, null, ureq.getLocale()));
		tblCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.group.name", 1, TABLE_ACTION_LAUNCH, ureq.getLocale()));
		tblCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.user.role", 2, null, ureq.getLocale()));
		tblCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.user.joindate", 3, null, ureq.getLocale()));
				
		//build data model
		BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
		BaseSecurity sm = BaseSecurityManager.getInstance();
		List<Object[]> userGroups = new ArrayList<Object[]>();
			//loop over all kind of groups with all possible memberships
			List<String> bgTypes = new ArrayList<String>();
			bgTypes.add(BusinessGroup.TYPE_BUDDYGROUP);
			bgTypes.add(BusinessGroup.TYPE_LEARNINGROUP);
			bgTypes.add(BusinessGroup.TYPE_RIGHTGROUP);
			for (String bgType : bgTypes) {				
				List<BusinessGroup> ownedGroups = bgm.findBusinessGroupsOwnedBy(bgType, identity, null);
				List<BusinessGroup> attendedGroups = bgm.findBusinessGroupsAttendedBy(bgType, identity, null);
				List<BusinessGroup> waitingGroups = bgm.findBusinessGroupsWithWaitingListAttendedBy(bgType, identity, null);
				//using HashSet to remove duplicate entries
				HashSet<BusinessGroup> allGroups = new HashSet<BusinessGroup>();
				allGroups.addAll(ownedGroups);
				allGroups.addAll(attendedGroups);
				allGroups.addAll(waitingGroups);
				
				Iterator<BusinessGroup> iter = allGroups.iterator();
				while (iter.hasNext()) {
					Object[] groupEntry = new Object[4];
					BusinessGroup group = iter.next();
					groupEntry[0] = translate(group.getType());
					groupEntry[1] = group;
					Date joinDate = null;
					if(attendedGroups.contains(group)&&ownedGroups.contains(group)) {
						groupEntry[2] = translate("attende.and.owner");
						joinDate = sm.getSecurityGroupJoinDateForIdentity(group.getPartipiciantGroup(), identity);
					}
					else if(attendedGroups.contains(group)) {
						groupEntry[2] = translate("attende");
						joinDate = sm.getSecurityGroupJoinDateForIdentity(group.getPartipiciantGroup(), identity);
					}
					else if(ownedGroups.contains(group)) {
						groupEntry[2] = translate("owner");
						joinDate = sm.getSecurityGroupJoinDateForIdentity(group.getOwnerGroup(), identity);
					}
					else if(waitingGroups.contains(group)) {
						int waitingListPosition = bgm.getPositionInWaitingListFor(identity, group);
						groupEntry[2] = translate("waiting", String.valueOf(waitingListPosition));
						joinDate = sm.getSecurityGroupJoinDateForIdentity(group.getWaitingGroup(), identity);
					}
					groupEntry[3] = joinDate;
					
					userGroups.add(groupEntry);
					}			
			}
		tableDataModel = new GroupOverviewModel(userGroups, 4);
		tblCtr.setTableDataModel(tableDataModel);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		//
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	@SuppressWarnings("unused")
	protected void event(	UserRequest ureq, Component source, Event event) {
		// no events to catch
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == tblCtr){
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				int rowid = te.getRowId();
				BusinessGroup currBusinessGroup = tableDataModel.getBusinessGroupAtRow(rowid);
				if (actionid.equals(TABLE_ACTION_LAUNCH)) {
					BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
					currBusinessGroup = bgm.loadBusinessGroup(currBusinessGroup.getKey(), false);
					if (currBusinessGroup==null) {
						//group seems to be removed meanwhile, reload table and show error
						showError("group.removed");
						buildTableController(ureq, wControl); 
						vc.put("table.groups", tblCtr.getInitialComponent());	
					}
					else {
						BGControllerFactory.getInstance().createRunControllerAsTopNavTab(currBusinessGroup, ureq, getWindowControl(), true, null);
					}
				}
			}
		}		
	}

}
