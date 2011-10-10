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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package com.frentix.olat.vitero.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.course.editor.NodeEditController;

import com.frentix.olat.vitero.model.ViteroBooking;

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
	private final List<BookingDisplay> bookingDisplays = new ArrayList<BookingDisplay>();

	private CloseableModalController cmc;
	private ViteroBookingEditController bookingController;

	public ViteroBookingsEditController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "edit");

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutContainer = (FormLayoutContainer)formLayout;
			layoutContainer.contextPut("bookingDisplays", bookingDisplays);
		}
		
		newButton = uifactory.addFormLink("vc.booking.new", formLayout, Link.BUTTON);
		uifactory.addFormSubmitButton("subm", formLayout);
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == newButton) {
			newBooking(ureq);
		} else if (source instanceof FormLink) {
			for(BookingDisplay display: bookingDisplays) {
				if(display.getDeleteButton() == source) {
					removeBooking(ureq, display);
					break;
				}
			}
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
		}
	}

	protected void removeBooking(UserRequest ureq, BookingDisplay bookingDisplay) {
		
	}

	protected void newBooking(UserRequest ureq) {
		removeAsListenerAndDispose(bookingController);
		bookingController = new ViteroBookingEditController(ureq, getWindowControl());			
		listenTo(bookingController);
		
		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), bookingController.getInitialComponent(), true, translate("vc.booking.title"));
		listenTo(cmc);
		cmc.activate();
	}

	public class BookingDisplay {

		private final ViteroBooking meeting;
		private DateChooser calenderBegin;
		private TextElement durationEl;
		private FormLink deleteButton;
		
		public BookingDisplay(ViteroBooking meeting) {
			this.meeting = meeting;
		}

		public ViteroBooking getMeeting() {
			return meeting;
		}

		public DateChooser getCalenderBegin() {
			return calenderBegin;
		}

		public void setCalenderBegin(DateChooser calenderBegin) {
			this.calenderBegin = calenderBegin;
		}

		public TextElement getDurationEl() {
			return durationEl;
		}

		public void setDurationEl(TextElement durationEl) {
			this.durationEl = durationEl;
		}

		public FormLink getDeleteButton() {
			return deleteButton;
		}

		public void setDeleteButton(FormLink deleteButton) {
			this.deleteButton = deleteButton;
		}
	}
}