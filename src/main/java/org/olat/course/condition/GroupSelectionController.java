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
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.Reset;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.FormReset;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.group.BusinessGroup;
import org.olat.group.ui.NewBGController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * Description:<br>
 * TODO: patrickb Class Description for MultiSelectColumnController
 * <P>
 * Initial Date: 15.06.2007 <br>
 * 
 * @author patrickb
 */
public class GroupSelectionController extends FormBasicController {

	private MultipleSelectionElement entrySelector;
	private FormLinkImpl createNew;
	private CourseGroupManager courseGrpMngr;
	private NewBGController groupCreateCntrllr;
	private CloseableModalController cmc;
	
	private String[] groupNames;
	private String[] groupKeys;

	public GroupSelectionController(UserRequest ureq, WindowControl wControl, String title,
			CourseGroupManager courseGrpMngr, List<Long> selectionKeys) {
		super(ureq, wControl, "group_or_area_selection");
		this.courseGrpMngr = courseGrpMngr;
		// unique names from list to array
		loadNamesAndKeys();
		initForm(ureq);
		// after initialising the element, select the entries
		for (Long selectionKey :selectionKeys) {
			entrySelector.select(selectionKey.toString(), true);
		}
	}
	
	private void loadNamesAndKeys() {
		List<BusinessGroup> groups = courseGrpMngr.getAllBusinessGroups();
		groupNames = new String[groups.size()];
		groupKeys = new String[groups.size()];
		for(int i=groups.size(); i-->0; ) {
			groupNames[i] = groups.get(i).getName();
			groupKeys[i] = groups.get(i).getKey().toString();
		}

	}

	@Override
	protected void formInnerEvent(UserRequest ureq, org.olat.core.gui.components.form.flexible.FormItem source,
			org.olat.core.gui.components.form.flexible.impl.FormEvent event) {
		if (source == createNew) {
			// user wants to create a new group -> show group create form
			removeAsListenerAndDispose(groupCreateCntrllr);
			
			RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(courseGrpMngr.getCourseResource(), false);
			groupCreateCntrllr = new NewBGController(ureq, getWindowControl(), re, true, null);
			listenTo(groupCreateCntrllr);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(
					getWindowControl(),"close",groupCreateCntrllr.getInitialComponent()
			);
			listenTo(cmc);
			cmc.activate();
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == groupCreateCntrllr) {
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				loadNamesAndKeys();
				// select new value
				entrySelector.setKeysAndValues(groupKeys, groupNames, null);
				entrySelector.select(groupCreateCntrllr.getCreatedGroup().getKey().toString(), true);
				
				//inform condition config easy about new groups -> which informs further
				fireEvent(ureq, Event.CHANGED_EVENT);
			} 
		} 
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void initForm(FormItemContainer boundTo, Controller listener, UserRequest ureq) {
		// easy creation only possible if a default group context available
		createNew = new FormLinkImpl("create");
		//is a button
		createNew.setCustomEnabledLinkCSS("b_button");
		createNew.setCustomDisabledLinkCSS("b_button b_disabled");
		// create new group/area on the right side
		boundTo.add(createNew);

		entrySelector = uifactory.addCheckboxesVertical("entries",  null, boundTo, groupKeys, groupNames, null, 1);
		// submitCancel after checkboxes
		Submit subm = new FormSubmit("subm", "apply");
		Reset reset = new FormReset("reset", "cancel");
		boundTo.add(subm);
		boundTo.add(reset);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formResetted(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	public Set<String> getSelectedEntries() {
		return entrySelector.getSelectedKeys();
	}
	
	public List<String> getSelectedNames() {
		List<String> selectedNames = new ArrayList<String>();
		for(int i=0; i<groupKeys.length; i++) {
			if(entrySelector.isSelected(i)) {
				selectedNames.add(groupNames[i]);
			}
		}
		return selectedNames;
	}
	
	public List<Long> getSelectedKeys() {
		Set<String> selectedKeys = entrySelector.getSelectedKeys();
		List<Long> groupKeys = new ArrayList<Long>();
		for(String selectedKey:selectedKeys) {
			groupKeys.add(new Long(selectedKey));
		}
		return groupKeys;
	}

}
