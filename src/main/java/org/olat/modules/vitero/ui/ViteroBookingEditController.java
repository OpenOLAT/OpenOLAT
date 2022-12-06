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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.vitero.ViteroModule;
import org.olat.modules.vitero.manager.ViteroManager;
import org.olat.modules.vitero.manager.VmsNotAvailableException;
import org.olat.modules.vitero.model.ViteroBooking;
import org.olat.modules.vitero.model.ViteroStatus;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  7 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroBookingEditController extends FormBasicController {

	private static final String[] enabledKeys = new String[]{"on"};
	

	private TextElement groupName;
	private DateChooser beginChooser;
	private DateChooser endChooser;
	private SingleSelection beginBufferEl;
	private SingleSelection endBufferEl;
	private SingleSelection roomSizeEl;
	private MultipleSelectionElement inspireEl;
	private MultipleSelectionElement autoSignIn;
	
	private static final String[] bufferKeys = new String[]{"0", "15", "30", "45", "60"};
	private static final String[] bufferValues = bufferKeys;
	private final String[] roomSizes;
	private static final String[] autoSignInKeys = new String[]{"on"};
	private final String[] autoSignInValues;
	
	private final BusinessGroup group;
	private final OLATResourceable ores;
	private final String subIdentifier;
	private final ViteroBooking booking;
	
	@Autowired
	private ViteroModule viteroModule;
	@Autowired
	private ViteroManager viteroManager;
	
	public ViteroBookingEditController(UserRequest ureq, WindowControl wControl, BusinessGroup group, OLATResourceable ores,
			String subIdentifier, ViteroBooking booking) {
		super(ureq, wControl);
		
		this.group = group;
		this.ores = ores;
		this.subIdentifier = subIdentifier; 
		this.booking = booking;
		
		List<Integer> sizes;
		try {
			sizes = viteroManager.getLicencedRoomSizes();
		} catch (VmsNotAvailableException e) {
			showError(VmsNotAvailableException.I18N_KEY);
			sizes = Collections.emptyList();
		}
		if(Settings.isDebuging() && sizes.isEmpty()) {
			roomSizes = new String[]{ "22" };
		} else {
			roomSizes = new String[sizes.size()];
			int i=0;
			for(Integer size:sizes) {
				roomSizes[i++] = size.toString();
			}
		}

		autoSignInValues = new String[]{ translate("enabled") };
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		boolean editable = booking.getBookingId() <= 0;
		if(editable) {
			setFormWarning("new.booking.warning");
		}
		
		String name = booking.getGroupName();
		groupName = uifactory.addTextElement("group.name", "group.name", 32, name, formLayout);
		groupName.setMandatory(true);
		groupName.setEnabled(editable);
		
		if(StringHelper.containsNonWhitespace(booking.getExternalId())) {
			uifactory.addStaticTextElement("external.id", booking.getExternalId(), formLayout);
		}
		
		//begin
		beginChooser = uifactory.addDateChooser("booking.begin", null, formLayout);
		beginChooser.setDisplaySize(21);
		beginChooser.setDateChooserTimeEnabled(true);
		beginChooser.setMandatory(true);
		beginChooser.setDate(booking.getStart());
		beginChooser.setEnabled(editable);
		//end
		endChooser = uifactory.addDateChooser("booking.end", null, formLayout);
		endChooser.setDisplaySize(21);
		endChooser.setDateChooserTimeEnabled(true);
		endChooser.setMandatory(true);
		endChooser.setDate(booking.getEnd());
		endChooser.setEnabled(editable);
		
		//buffer start
		beginBufferEl = uifactory.addDropdownSingleselect("booking.beginBuffer", formLayout, bufferKeys, bufferValues, null);
		beginBufferEl.select(Integer.toString(booking.getStartBuffer()), true);
		beginBufferEl.setEnabled(editable);
		
		//buffer end
		endBufferEl = uifactory.addDropdownSingleselect("booking.endBuffer", formLayout, bufferKeys, bufferValues, null);
		endBufferEl.select(Integer.toString(booking.getEndBuffer()), true);
		endBufferEl.setEnabled(editable);
		
		//room size
		roomSizeEl = uifactory.addDropdownSingleselect("booking.roomSize", formLayout, roomSizes, roomSizes, null);
		if(booking.getRoomSize() > 0 && isRoomSizeAvailable(booking.getRoomSize())) {
			roomSizeEl.select(Integer.toString(booking.getRoomSize()), true);
		}
		roomSizeEl.setEnabled(editable);
		
		String[] enabledValues = new String[]{translate("enabled")};
		inspireEl = uifactory.addCheckboxesHorizontal("option.inspire", formLayout, enabledKeys, enabledValues);
		inspireEl.setVisible(viteroModule.isInspire());
		if(viteroModule.isInspire() && booking.isInspire()) {
			inspireEl.select(enabledKeys[0], true);
		}
		inspireEl.setEnabled(editable);
		
		autoSignIn = uifactory.addCheckboxesHorizontal("booking.autoSignIn", formLayout, autoSignInKeys, autoSignInValues);
		if(booking.isAutoSignIn()) {
			autoSignIn.select(autoSignInKeys[0], true);
		}

		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonCont);
		uifactory.addFormSubmitButton("ok", buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}
	
	private boolean isRoomSizeAvailable(int roomSize) {
		String roomSizeStr = Integer.toString(roomSize);
		for(int i=roomSizes.length; i-->0; ) {
			if(roomSizes[i].equals(roomSizeStr)) {
				return true;
			}
		}
		return false;
	}
	
	public ViteroBooking getUserObject() {
		return booking;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		String name = groupName.getValue();
		groupName.clearError();
		if(StringHelper.containsNonWhitespace(name)) {
			if(name.contains("_")) {
				groupName.setErrorKey("error.bookingName");
				allOk &= false;
			}
		} else {
			groupName.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}

		Date begin = beginChooser.getDate();
		if(beginChooser.isEnabled()) {
			beginChooser.clearError();
			if(begin == null) {
				beginChooser.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else if(new Date().after(begin)) {
				beginChooser.setErrorKey("error.bookingInPast");
				allOk &= false;
			}
		}
		
		if(endChooser.isEnabled()) {
			Date end = endChooser.getDate();
			endChooser.clearError();
			if(end == null) {
				endChooser.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else if(new Date().after(begin) || end.before(begin)) {
				beginChooser.setErrorKey("error.bookingInPast");
				allOk &= false;
			}
		}
		
		roomSizeEl.clearError();
		if(!roomSizeEl.isOneSelected()) {
			roomSizeEl.setErrorKey("form.legende.mandatory");
			allOk = false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String name = groupName.getValue();
		booking.setGroupName(name);

		Date begin = beginChooser.getDate();
		booking.setStart(begin);
		
		int beginBuffer = 0;
		if(beginBufferEl.isOneSelected()) {
			beginBuffer = Integer.parseInt(beginBufferEl.getSelectedKey());
		}
		booking.setStartBuffer(beginBuffer);
		
		Date end = endChooser.getDate();
		booking.setEnd(end);
		
		int endBuffer = 0;
		if(endBufferEl.isOneSelected()) {
			endBuffer = Integer.parseInt(endBufferEl.getSelectedKey());
		}
		booking.setEndBuffer(endBuffer);
		
		int roomSize = -1;
		if(roomSizeEl.isOneSelected()) {
			roomSize = Integer.parseInt(roomSizeEl.getSelectedKey());
		}
		booking.setRoomSize(roomSize);
		
		boolean auto = autoSignIn.isMultiselect() && autoSignIn.isSelected(0);
		booking.setAutoSignIn(auto);
		
		boolean inspire = inspireEl.isVisible() && inspireEl.isAtLeastSelected(1);
		booking.setInspire(inspire);
		
		try {
			if(booking.getBookingId() >= 0) {
				ViteroBooking updatedBooking = viteroManager.updateBooking(group, ores, subIdentifier, booking);
				if(updatedBooking != null) {
					showInfo("booking.ok");
					fireEvent(ureq, Event.DONE_EVENT);
				} else {
					showError("error.unkown");
				}
			} else {
				ViteroStatus status = viteroManager.createBooking(group, ores, subIdentifier, booking);
				if(status.isOk()) {
					showInfo("booking.ok");
					fireEvent(ureq, Event.DONE_EVENT);
				} else {
					showError(status.getError().i18nKey());
				}
			}
		} catch (VmsNotAvailableException e) {
			showError(VmsNotAvailableException.I18N_KEY);
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}