/**
* OLAT - Online Learning and Training<br />
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br />
* you may not use this file except in compliance with the License.<br />
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br />
* software distributed under the License is distributed on an "AS IS" BASIS, <br />
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br />
* See the License for the specific language governing permissions and <br />
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br />
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.instantMessaging.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.instantMessaging.ImPreferences;

/**
 *  Initial Date: August 08, 2005
 * 
 *  @author Alexander Schneider
 *  
 *  Comment: The user can select if his name is (in)visible on the online user list.
 */
public class OnlineListForm extends FormBasicController {
    private static final String ONLINELIST = "onlineList";
    private static final String ONLINETIME = "onlineTime";
    private static final String COURSENAME = "courseName";
    
    private SelectionElement toogleVisibility, onlineTimeSwitch, courseNameSwitch;
    
    private ImPreferences imPrefs;
    
	public OnlineListForm(UserRequest ureq, WindowControl wControl, ImPreferences imPrefs) {
		super(ureq, wControl);
		this.imPrefs = imPrefs;
		initForm (ureq);
	}
	
	protected void updateImPreferencesFromFormData(ImPreferences imPrefs){
			imPrefs.setVisibleToOthers(toogleVisibility.isSelected(0));
			imPrefs.setOnlineTimeVisible(onlineTimeSwitch.isSelected(0));
			imPrefs.setAwarenessVisible(courseNameSwitch.isSelected(0));
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled (UserRequest ureq) {
		fireEvent (ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("title.onlinelist");
		this.setFormContextHelp("org.olat.user","home-imsettings.html","help.hover.imsettings");
		
		toogleVisibility = uifactory.addCheckboxesVertical(ONLINELIST, "form.onlinelist", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		toogleVisibility.select("xx", imPrefs.isVisibleToOthers());
		toogleVisibility.addActionListener(this, FormEvent.ONCLICK);
		
		onlineTimeSwitch = uifactory.addCheckboxesVertical(ONLINETIME, "form.onlinetime", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		onlineTimeSwitch.select("xx", imPrefs.isOnlineTimeVisible());
		
		courseNameSwitch = uifactory.addCheckboxesVertical(COURSENAME, "form.coursename", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		courseNameSwitch.select("xx", imPrefs.isAwarenessVisible());
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		
		update();
	}

	private void update () {
		onlineTimeSwitch.setVisible(toogleVisibility.isSelected(0));
		courseNameSwitch.setVisible(toogleVisibility.isSelected(0));
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		update();
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
}