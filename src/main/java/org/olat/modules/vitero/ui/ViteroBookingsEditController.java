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
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.RedirectMediaResource;
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

	private int count = 0;
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
	
	protected void reloadModel() {
		try {
			bookingDisplays.clear(); 
			List<ViteroBooking> bookings = viteroManager.getBookings(group, ores, subIdentifier);
			for(ViteroBooking booking:bookings) {
				BookingDisplay display = forgeDisplay(booking);
				bookingDisplays.add(display);
			}
			flc.contextPut("bookingDisplays", bookingDisplays);
		} catch (VmsNotAvailableException e) {
			showError(VmsNotAvailableException.I18N_KEY);
		}
	}
	
	private BookingDisplay forgeDisplay(ViteroBooking booking) {
		BookingDisplay display = new BookingDisplay(booking);
		if(!readOnly) {
			display.setDeleteButton(uifactory.addFormLink("delete_" + count++, "delete", "delete", flc, Link.BUTTON));
			display.setEditButton(uifactory.addFormLink("edit_" + count++, "edit", "edit", flc, Link.BUTTON));
		}
		display.setUsersButton(uifactory.addFormLink("users_" + count++, "users", "users", flc, Link.BUTTON));
		
		FormLink groupButton = uifactory.addFormLink("group_" + count++, "group.open", "group.open", flc, Link.BUTTON);
		groupButton.getComponent().setTarget("_blank");
		
		String linkId = "group_" + count++;
		Link groupLink = LinkFactory.createLink(linkId, linkId, "group.open", "group.open", getTranslator(), flc.getFormItemComponent(), this, Link.BUTTON);
		groupLink.setTarget("_blank");
		groupLink.setUserObject(display);
		display.setGroupButton(groupLink);
		return display;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link) {
			Link link = (Link)source;
			if("group.open".equals(link.getCommand()) && link.getUserObject() instanceof BookingDisplay) {
				BookingDisplay bookingDisplay = (BookingDisplay)link.getUserObject();
				openGroup(ureq, bookingDisplay.getMeeting());
			}
		}
		super.event(ureq, source, event);
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
					MediaResource redirect = new RedirectMediaResource(url);
					ureq.getDispatchResult().setResultingMediaResource(redirect);
				}
			} else {
				showWarning("booking.group.warning");

			}
		} catch (VmsNotAvailableException e) {
			showError(VmsNotAvailableException.I18N_KEY);
		}
	}
	
	protected void occupiedRooms(UserRequest ureq) {
		removeAsListenerAndDispose(roomsOverviewController);

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
		private Link groupButton;
		
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

		public Link getGroupButton() {
			return groupButton;
		}

		public void setGroupButton(Link groupButton) {
			this.groupButton = groupButton;
		}
	}
}