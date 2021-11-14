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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.vitero.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.UserConstants;
import org.olat.modules.vitero.manager.ViteroManager;
import org.olat.modules.vitero.manager.VmsNotAvailableException;
import org.olat.modules.vitero.model.ViteroBooking;
import org.olat.modules.vitero.model.ViteroStatus;
import org.olat.modules.vitero.model.ViteroUser;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  13 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroAdminBookingMembersController extends BasicController {
	
	private final ViteroBooking booking;
	private final ViteroManager viteroManager;

	private final TableController tableCtr;
	private final VelocityContainer mainVC;
	
	public ViteroAdminBookingMembersController(UserRequest ureq, WindowControl wControl, ViteroBooking booking) {
		super(ureq, wControl);

		this.booking = booking;
		viteroManager = (ViteroManager)CoreSpringFactory.getBean("viteroManager");

		mainVC = createVelocityContainer("vms_user_admin");

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("users.empty"), null, "o_icon_user");
		
		Translator trans = UserManager.getInstance().getPropertyHandlerTranslator(getTranslator());
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), trans);
		listenTo(tableCtr);
		
		tableCtr.addColumnDescriptor(getColumnDescriptor(Col.firstName.ordinal(), UserConstants.FIRSTNAME, ureq.getLocale()));
		tableCtr.addColumnDescriptor(getColumnDescriptor(Col.lastName.ordinal(), UserConstants.LASTNAME, ureq.getLocale()));
		tableCtr.addColumnDescriptor(getColumnDescriptor(Col.email.ordinal(), UserConstants.EMAIL, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor("signout", "table.action", translate("signout")));
		
		tableCtr.addMultiSelectAction("signout", "signout");
		tableCtr.setMultiSelect(true);
		
		loadModel();
		mainVC.put("userTable", tableCtr.getInitialComponent());
		
		putInitialPanel(mainVC);
	}
	
	private ColumnDescriptor getColumnDescriptor(int pos, String attrName, Locale locale) {
		List<UserPropertyHandler> userPropertyHandlers = UserManager.getInstance().getAllUserPropertyHandlers();
		for(UserPropertyHandler handler:userPropertyHandlers) {
			if(handler.getName().equals(attrName)) {
				return handler.getColumnDescriptor(pos, null, locale);
			}
		}
		return null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == tableCtr) {
			if(event instanceof TableEvent) {
				TableEvent e = (TableEvent)event;
				int row = e.getRowId();
				ViteroUser identity = (ViteroUser)tableCtr.getTableDataModel().getObject(row);
				if("signout".equals(e.getActionId())) {
					signOut(Collections.singletonList(identity));
				}
			} else if(event instanceof TableMultiSelectEvent) {
				TableMultiSelectEvent e = (TableMultiSelectEvent)event;
				List<ViteroUser> identities = new ArrayList<>();
				for (int i = e.getSelection().nextSetBit(0); i >= 0; i = e.getSelection().nextSetBit(i + 1)) {
					ViteroUser identity = (ViteroUser)tableCtr.getTableDataModel().getObject(i);
					identities.add(identity);
				}
				if("signout".equals(e.getAction())) {
					signOut(identities);
				}
			}
			
		}
		super.event(ureq, source, event);
	}
	
	private void signOut(List<ViteroUser> members) {
		try {
			for(ViteroUser member:members) {
				ViteroStatus status = viteroManager.removeFromRoom(booking, member.getUserId());
				if(status.isOk()) {
					showInfo("signout.ok");
				} else {
					showInfo("signout.nok");
					break;
				}
			}
			loadModel();
		} catch (VmsNotAvailableException e) {
			showError(VmsNotAvailableException.I18N_KEY);
		}
	}

	private void loadModel() {
		try {
			List<ViteroUser> users = viteroManager.getUsersOf(booking);
			tableCtr.setTableDataModel(new UserToGroupDataModel(users));
			
			int numOfFreePlaces = booking.getRoomSize() - users.size();
			mainVC.contextPut("freePlaces", new String[]{Integer.toString(numOfFreePlaces)});
		} catch (VmsNotAvailableException e) {
			showError(VmsNotAvailableException.I18N_KEY);
		}
	}

	public class UserToGroupDataModel implements TableDataModel<ViteroUser> {
		
		private List<ViteroUser> members;
		
		public UserToGroupDataModel() {
			//
		}
		
		public UserToGroupDataModel( List<ViteroUser> members) {
			this.members = members;
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public int getRowCount() {
			return members == null ? 0 : members.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			ViteroUser identity = getObject(row);
			switch(Col.values()[col]) {
				case firstName: return identity.getFirstName();
				case lastName: return identity.getLastName();
				case email: return identity.getEmail();
				default: {/* do nothing */}
			}
			return null;
		}

		@Override
		public ViteroUser getObject(int row) {
			return members.get(row);
		}

		@Override
		public void setObjects(List<ViteroUser> objects) {
			this.members = objects;
		}

		@Override
		public Object createCopyWithEmptyList() {
			return new UserToGroupDataModel();
		}
	}
	
	public enum Col {
		firstName,
		lastName,
		email,
		sign,
	}
}