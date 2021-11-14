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
*/
package org.olat.course.condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.group.area.BGArea;
import org.olat.group.ui.NewAreaController;

/**
 * Initial Date: 15.06.2007 <br>
 * @author patrickb
 */
public class AreaSelectionController extends FormBasicController {

	private MultipleSelectionElement entrySelector;
	private FormLink createNew;
	private final CourseGroupManager courseGrpMngr;
	private NewAreaController areaCreateCntrllr;
	private CloseableModalController cmc;
	private final boolean createEnable;

	private String[] areaNames;
	private String[] areaKeys;

	public AreaSelectionController(UserRequest ureq, WindowControl wControl,
			boolean allowCreate, CourseGroupManager courseGrpMngr, List<Long> selectionKeys) {
		super(ureq, wControl, "group_or_area_selection");
		this.courseGrpMngr = courseGrpMngr;
		this.createEnable = allowCreate;

		loadNamesAndKeys();
		initForm(ureq);
		
		for (Long selectionKey: selectionKeys) {
			entrySelector.select(selectionKey.toString(), true);
		}
	}
	
	private void loadNamesAndKeys() {
		List<BGArea> areas = courseGrpMngr.getAllAreas();
		areaNames = new String[areas.size()];
		areaKeys = new String[areas.size()];
		for(int i=areas.size(); i-->0; ) {
			areaNames[i] = areas.get(i).getName();
			areaKeys[i] = areas.get(i).getKey().toString();
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == createNew) {
			//user wants to create a new area -> show new area create form
			removeAsListenerAndDispose(areaCreateCntrllr);
			areaCreateCntrllr = new NewAreaController(ureq, getWindowControl(), courseGrpMngr.getCourseResource(), true, null);
			listenTo(areaCreateCntrllr);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(),"close",areaCreateCntrllr.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == areaCreateCntrllr) {
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				loadNamesAndKeys();
				// select new value
				entrySelector.setKeysAndValues(areaKeys, areaNames);
				entrySelector.select(areaCreateCntrllr.getCreatedArea().getKey().toString(), true);
				
				//inform condition config easy about new groups -> which informs further
				fireEvent(ureq, Event.CHANGED_EVENT);
			} 
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(createEnable) {
			// easy creation only possible if a default group context available
			createNew = uifactory.addFormLink("create", formLayout, Link.BUTTON);
		}

		entrySelector = uifactory.addCheckboxesVertical("entries",  null, formLayout, areaKeys, areaNames, 1);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("subm", "apply", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	public List<String> getSelectedNames() {
		List<String> selectedNames = new ArrayList<>();
		for(int i=0; i<areaKeys.length; i++) {
			if(entrySelector.isSelected(i)) {
				selectedNames.add(areaNames[i]);
			}
		}
		return selectedNames;
	}
	
	public List<Long> getSelectedKeys() {
		Collection<String> selectedKeys = entrySelector.getSelectedKeys();
		List<Long> groupKeys = new ArrayList<>();
		for(String selectedKey:selectedKeys) {
			groupKeys.add(Long.valueOf(selectedKey));
		}
		return groupKeys;
	}
}