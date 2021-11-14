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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.modules.vitero.ViteroModule;
import org.olat.modules.vitero.model.ViteroBooking;
import org.olat.modules.vitero.model.ViteroGroup;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  13 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroAdminBookingRawInfosController extends FormBasicController {
	
	private final ViteroGroup group;
	private final ViteroBooking booking;
	private final Formatter formatter;
	private static final String[] onKeys = new String[]{"on"};

	
	@Autowired
	private ViteroModule viteroModule;
	
	public ViteroAdminBookingRawInfosController(UserRequest ureq, WindowControl wControl, ViteroBooking booking,
			ViteroGroup group) {
		super(ureq, wControl);
		this.group = group;
		this.booking = booking;
		formatter = Formatter.getInstance(getLocale());
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uifactory.addStaticTextElement("booking.id", Integer.toString(booking.getBookingId()), formLayout);
		uifactory.addStaticTextElement("booking.begin", formatter.formatDateAndTime(booking.getStart()), formLayout);
		uifactory.addStaticTextElement("booking.end", formatter.formatDateAndTime(booking.getEnd()), formLayout);
		uifactory.addStaticTextElement("booking.beginBuffer", Integer.toString(booking.getStartBuffer()), formLayout);
		uifactory.addStaticTextElement("booking.endBuffer", Integer.toString(booking.getEndBuffer()), formLayout);
		uifactory.addStaticTextElement("booking.roomSize", Integer.toString(booking.getRoomSize()), formLayout);
		uifactory.addStaticTextElement("group.numOfParticipants", Integer.toString(group.getNumOfParticipants()), formLayout);
		uifactory.addStaticTextElement("group.id", Integer.toString(group.getGroupId()), formLayout);
		
		String name = group.getName();
		int sepIndex = name.indexOf("_OLAT_");
		if(sepIndex > 0) {
			name = name.substring(0, sepIndex);
		}
		uifactory.addStaticTextElement("group.name", name, formLayout);
		
		String[] enabledValues = new String[]{translate("enabled")};
		MultipleSelectionElement inspireEl = uifactory.addCheckboxesHorizontal("option.inspire", formLayout, onKeys, enabledValues);
		inspireEl.setVisible(viteroModule.isInspire());
		if(viteroModule.isInspire() && booking.isInspire()) {
			inspireEl.select(onKeys[0], true);
		}
		inspireEl.setEnabled(false);

		MultipleSelectionElement autoSignIn = uifactory.addCheckboxesHorizontal("booking.autoSignIn", formLayout, onKeys, enabledValues);
		if(booking.isAutoSignIn()) {
			autoSignIn.select(onKeys[0], true);
		}
		autoSignIn.setEnabled(false);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
