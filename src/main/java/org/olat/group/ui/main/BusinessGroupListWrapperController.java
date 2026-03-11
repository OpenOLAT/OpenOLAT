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
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;

/**
 * Initial date: 29.01.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class BusinessGroupListWrapperController extends BasicController implements Activateable2 {
	
	private GroupAcceptReservationsController groupAcceptReservationsCtrl;
	private BusinessGroupListController businessGroupListController;
	
	private VelocityContainer wrapper;

	public BusinessGroupListWrapperController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		wrapper = createVelocityContainer("group_list_wrapper");
		
		groupAcceptReservationsCtrl = new GroupAcceptReservationsController(ureq, wControl);
		listenTo(groupAcceptReservationsCtrl);
		if (groupAcceptReservationsCtrl.hasReservations()) {
			wrapper.put("groupAcceptReservations", groupAcceptReservationsCtrl.getInitialComponent());
		}
		
		businessGroupListController = new BusinessGroupListController(ureq, wControl, "my");
		listenTo(businessGroupListController);
		
		wrapper.put("myGroups", businessGroupListController.getInitialComponent());

		putInitialPanel(wrapper);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			if(!businessGroupListController.hasTab()) {
				businessGroupListController.selectFilterTab(ureq, businessGroupListController.getBookmarksGroupsTab());
				if(businessGroupListController.isEmpty()) {
					businessGroupListController.selectFilterTab(ureq, businessGroupListController.getMyGroupsTab());
				}
			} // else do nothing
		} else {
			businessGroupListController.activate(ureq, entries, state);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		fireEvent(ureq, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == groupAcceptReservationsCtrl) {
			businessGroupListController.reloadModel();
			
			if (!groupAcceptReservationsCtrl.hasReservations()) {
				wrapper.remove(groupAcceptReservationsCtrl.getInitialComponent());
			}
		}
	}

}
