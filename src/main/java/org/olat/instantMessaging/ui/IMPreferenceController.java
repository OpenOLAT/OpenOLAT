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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.instantMessaging.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.instantMessaging.ImPreferences;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.model.Presence;

/**
 * Initial Date:  August 08, 2005
 *
 * @author Alexander Schneider
 */
public class IMPreferenceController extends FormBasicController {

  private SingleSelection statusList;
	private SelectionElement toogleVisibility;
  private String[] keys, values;
	
	private final Identity changeableIdentity;
	private final InstantMessagingService imService;
	
	/**
	 * Constructor for the change instant messaging controller
	 * @param ureq The user request
	 * @param wControl The current window controller
	 * @param changeableIdentity
	 */
	public IMPreferenceController(UserRequest ureq, WindowControl wControl, Identity changeableIdentity) {
		super(ureq, wControl);
		
		this.changeableIdentity = changeableIdentity;
		imService = CoreSpringFactory.getImpl(InstantMessagingService.class);

		keys = new String[] {
        Presence.available.name(),
        Presence.dnd.name(),
        Presence.unavailable.name()
		};

		values = new String[] {
		    translate("presence.available"),
        translate("presence.dnd"),
        translate("presence.unavailable")
		};
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("title.roster");
		setFormContextHelp("manual_user/personal/Chat/#settings");
		
		ImPreferences imPrefs = imService.getImPreferences(changeableIdentity);
		
		toogleVisibility = uifactory.addCheckboxesVertical("online_list", "form.onlinelist", formLayout, new String[]{"xx"}, new String[]{null}, 1);
		toogleVisibility.select("xx", imPrefs.isVisibleToOthers());
		toogleVisibility.addActionListener(FormEvent.ONCHANGE);
		
		statusList = uifactory.addRadiosVertical("status_list", "form.defaultstatus", formLayout, keys, values);
		if(StringHelper.containsNonWhitespace(imPrefs.getRosterDefaultStatus())) {
			statusList.select(imPrefs.getRosterDefaultStatus(), true);
		}
		statusList.addActionListener(FormEvent.ONCHANGE);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == toogleVisibility) {
		  imService.updateImPreferences(changeableIdentity, toogleVisibility.isSelected(0));
		} else if (source == statusList) {
		   imService.updateStatus(changeableIdentity, statusList.getSelectedKey());
		}
	}
}
