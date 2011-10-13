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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;

import com.frentix.olat.vitero.model.ViteroBooking;
import com.frentix.olat.vitero.model.ViteroGroup;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  13 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroRawBookingInformationController extends FormBasicController {
	
	private final ViteroGroup group;
	private final ViteroBooking booking;
	private final Formatter formatter;
	private static final String[] autoSignInKeys = new String[]{"on"};
	private final String[] autoSignInValues;
	
	public ViteroRawBookingInformationController(UserRequest ureq, WindowControl wControl, ViteroBooking booking,
			ViteroGroup group) {
		super(ureq, wControl);
		
		this.group = group;
		this.booking = booking;
		this.formatter = Formatter.getInstance(getLocale());
		autoSignInValues = new String[]{ translate("enabled") };
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("booking.infos");
		setFormDescription("booking.raw.intro");
		
		uifactory.addStaticTextElement("booking.id", Integer.toString(booking.getBookingId()), formLayout);
		uifactory.addStaticTextElement("booking.begin", formatter.formatDateAndTime(booking.getStart()), formLayout);
		uifactory.addStaticTextElement("booking.end", formatter.formatDateAndTime(booking.getEnd()), formLayout);
		uifactory.addStaticTextElement("booking.beginBuffer", Integer.toString(booking.getStartBuffer()), formLayout);
		uifactory.addStaticTextElement("booking.endBuffer", Integer.toString(booking.getEndBuffer()), formLayout);
		uifactory.addStaticTextElement("booking.roomSize", Integer.toString(booking.getRoomSize()), formLayout);
		uifactory.addStaticTextElement("group.numOfParticipants", Integer.toString(group.getNumOfParticipants()), formLayout);
		uifactory.addStaticTextElement("group.id", Integer.toString(group.getGroupId()), formLayout);
		uifactory.addStaticTextElement("group.name", group.getName(), formLayout);

		MultipleSelectionElement autoSignIn = uifactory.addCheckboxesHorizontal("booking.autoSignIn", formLayout, autoSignInKeys, autoSignInValues, null);
		if(booking.isAutoSignIn()) {
			autoSignIn.select(autoSignInKeys[0], true);
		}
		autoSignIn.setEnabled(false);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
