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
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.course.editor.NodeEditController;
import org.olat.group.BusinessGroup;
import org.olat.modules.vitero.manager.ViteroManager;
import org.olat.modules.vitero.manager.VmsNotAvailableException;
import org.olat.modules.vitero.model.ViteroBooking;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date: 6 oct. 2011 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroBookingsEditController extends FormBasicController {

	private FormLink newButton;
	private FormLink occupiedRoomsLink;
	private final List<BookingDisplay> bookingDisplays = new ArrayList<>();

	private CloseableModalController cmc;
	private DialogBoxController dialogCtr;
	private DialogBoxController warningGroupCtr;
	private ViteroBookingEditController bookingController;
	private ViteroRoomsOverviewController roomsOverviewController;
	private ViteroUserToGroupController usersController;
	private VelocityContainer viteroGroupVC;
	
	private final boolean readOnly;
	private final String resourceName;
	private final BusinessGroup group;
	private final OLATResourceable ores;
	private final String subIdentifier;
	@Autowired
	private ViteroManager viteroManager;

	public ViteroBookingsEditController(UserRequest ureq, WindowControl wControl, BusinessGroup group, OLATResourceable ores,
			String subIdentifier, String resourceName, boolean readOnly) {
		super(ureq, wControl, "edit");
		
		this.group = group;
		this.ores = ores;
		this.readOnly = readOnly;
		this.subIdentifier = subIdentifier;
		this.resourceName = resourceName;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		reloadModel();
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons-cont", getTranslator());
		formLayout.add(buttonLayout);
		newButton = uifactory.addFormLink("new", buttonLayout, Link.BUTTON);
		newButton.setVisible(!readOnly);
		occupiedRoomsLink = uifactory.addFormLink("roomsOverview", buttonLayout, Link.BUTTON);
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}
	
	protected void reloadModel() {
		try {
			bookingDisplays.clear(); 
			List<ViteroBooking> bookings = viteroManager.getBookings(group, ores, subIdentifier);
			int i=0;
			for(ViteroBooking booking:bookings) {
				BookingDisplay display = new BookingDisplay(booking);
				if(!readOnly) {
					display.setDeleteButton(uifactory.addFormLink("delete_" + i++, "delete", "delete", flc, Link.BUTTON));
					display.setEditButton(uifactory.addFormLink("edit_" + i++, "edit", "edit", flc, Link.BUTTON));
				}
				display.setUsersButton(uifactory.addFormLink("users_" + i++, "users", "users", flc, Link.BUTTON));
				display.setGroupButton(uifactory.addFormLink("group_" + i++, "group.open", "group.open", flc, Link.BUTTON));
				bookingDisplays.add(display);
			}
			flc.contextPut("bookingDisplays", bookingDisplays);
		} catch (VmsNotAvailableException e) {
			showError(VmsNotAvailableException.I18N_KEY);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == newButton) {
			newBooking(ureq);
		} else if (source == occupiedRoomsLink) {
			occupiedRooms(ureq);
		} else if (source instanceof FormLink) {
			for(BookingDisplay display: bookingDisplays) {
				if(display.getDeleteButton() == source) {
					confirmDeleteBooking(ureq, display);
					break;
				} else if(display.getEditButton() == source) {
					ViteroBooking viteroBooking = display.getMeeting();
					editBooking(ureq, viteroBooking);
					break;
				} else if(display.getUsersButton() == source) {
					ViteroBooking viteroBooking = display.getMeeting();
					usersBooking(ureq, viteroBooking);
					break;
				} else if(display.getGroupButton() == source) {
					ViteroBooking viteroBooking = display.getMeeting();
					openGroup(ureq, viteroBooking);
					break;
				}
			}
			reloadModel();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == cmc ) {
			removeAsListenerAndDispose(bookingController);
			removeAsListenerAndDispose(cmc);
		} else if (source == bookingController) {
			if(Event.DONE_EVENT.equals(event)) {
				fireEvent(ureq, event);
			}
			cmc.deactivate();
			removeAsListenerAndDispose(bookingController);
			removeAsListenerAndDispose(cmc);
			reloadModel();
		} else if(source == dialogCtr) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				ViteroBooking booking = (ViteroBooking)dialogCtr.getUserObject();
				deleteBooking(booking);
			}
		} else if (source == warningGroupCtr) {
			removeAsListenerAndDispose(warningGroupCtr);
			warningGroupCtr = null;
		}
	}
	
	protected void openGroup(UserRequest ureq, ViteroBooking booking) {
		try {
			if(viteroManager.isUserOf(booking, getIdentity())) {
				String url = viteroManager.getURLToGroup(ureq.getIdentity(), booking);
				if(url == null) {
					showError("error.sessionCodeNull");
				} else {
					viteroGroupVC = createVelocityContainer("opengroup");
					viteroGroupVC.contextPut("groupUrl", url);
					removeAsListenerAndDispose(cmc);
					cmc = new CloseableModalController(getWindowControl(), translate("close"), viteroGroupVC);
					listenTo(cmc);
					cmc.activate();
				}
			} else {
				String title = translate("booking.group");
				String text = translate("booking.group.warning");
				List<String> buttonLabels = Collections.singletonList(translate("ok"));
				warningGroupCtr = activateGenericDialog(ureq, title, text, buttonLabels, warningGroupCtr);
			}
		} catch (VmsNotAvailableException e) {
			showError(VmsNotAvailableException.I18N_KEY);
		}
	}
	
	protected void occupiedRooms(UserRequest ureq) {
		removeAsListenerAndDispose(bookingController);

		try {
			roomsOverviewController = new ViteroRoomsOverviewController(ureq, getWindowControl());			
			listenTo(roomsOverviewController);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), roomsOverviewController.getInitialComponent(), true, translate("roomsOverview"));
			listenTo(cmc);
			cmc.activate();
		} catch (VmsNotAvailableException e) {
			showError(VmsNotAvailableException.I18N_KEY);
		}
	}
	
	protected void deleteBooking(ViteroBooking booking) {
		try {
			if(viteroManager.deleteBooking(booking)) {
				showInfo("delete.ok");
			} else {
				showError("delete.nok");
			}
			reloadModel();
		} catch (VmsNotAvailableException e) {
			showError(VmsNotAvailableException.I18N_KEY);
		}
	}

	protected void confirmDeleteBooking(UserRequest ureq, BookingDisplay bookingDisplay) {
		String title = translate("delete");
		String text = translate("delete.confirm");
		dialogCtr = activateOkCancelDialog(ureq, title, text, dialogCtr);
		dialogCtr.setUserObject(bookingDisplay.getMeeting());
	}
	
	protected void newBooking(UserRequest ureq) {
		try {
			ViteroBooking viteroBooking = viteroManager.createBooking(resourceName);
			editBooking(ureq, viteroBooking);
		} catch (VmsNotAvailableException e) {
			showError(VmsNotAvailableException.I18N_KEY);
		}
	}
	
	protected void editBooking(UserRequest ureq, ViteroBooking viteroBooking) {
		removeAsListenerAndDispose(bookingController);

		bookingController = new ViteroBookingEditController(ureq, getWindowControl(), group, ores, subIdentifier, viteroBooking);			
		listenTo(bookingController);
		
		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), bookingController.getInitialComponent(), true, translate("edit"));
		listenTo(cmc);
		cmc.activate();
	}
	
	protected void usersBooking(UserRequest ureq, ViteroBooking viteroBooking) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(usersController);

		usersController = new ViteroUserToGroupController(ureq, getWindowControl(), group, ores, viteroBooking, readOnly);			
		listenTo(usersController);
		
		String title = translate("users.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), usersController.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	public class BookingDisplay {
		private final ViteroBooking meeting;
		private FormLink deleteButton;
		private FormLink editButton;
		private FormLink usersButton;
		private FormLink groupButton;
		
		public BookingDisplay(ViteroBooking meeting) {
			this.meeting = meeting;
		}

		public ViteroBooking getMeeting() {
			return meeting;
		}
		
		public String getGroupName() {
			String name = meeting.getGroupName();
			if(StringHelper.containsNonWhitespace(name)) {
				return name;
			}
			return "";
		}

		public Date getBegin() {
			return meeting.getStart();
		}
		
		public Date getEnd() {
			return meeting.getEnd();
		}

		public FormLink getDeleteButton() {
			return deleteButton;
		}

		public void setDeleteButton(FormLink deleteButton) {
			this.deleteButton = deleteButton;
		}

		public FormLink getEditButton() {
			return editButton;
		}

		public void setEditButton(FormLink editButton) {
			this.editButton = editButton;
		}

		public FormLink getUsersButton() {
			return usersButton;
		}

		public void setUsersButton(FormLink usersButton) {
			this.usersButton = usersButton;
		}

		public FormLink getGroupButton() {
			return groupButton;
		}

		public void setGroupButton(FormLink groupButton) {
			this.groupButton = groupButton;
		}
	}
}