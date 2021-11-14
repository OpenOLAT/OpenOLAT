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

package org.olat.course.run.preview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
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
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.group.BusinessGroup;
import org.olat.group.area.BGArea;

/**
 * Initial Date:  14.01.2005 <br>
 *
 * @author Felix Jost
 */
public class PreviewSettingsForm extends FormBasicController {
	static final String ROLE_GLOBALAUTHOR = "role.globalauthor";
	static final String ROLE_COURSEADMIN = "role.courseadmin";
	static final String ROLE_COURSECOACH = "role.coursecoach";
	static final String ROLE_GUEST = "role.guest";
	static final String ROLE_STUDENT = "role.student";
	
	private DateChooser sdate;
	private final int NUMATTR = 5;
	private List<TextElement> attrNames  = new ArrayList<>(NUMATTR);
	private List<TextElement> attrValues = new ArrayList<>(NUMATTR);
	
	private SingleSelection roles;
	private MultipleSelectionElement groupSelector;
	private MultipleSelectionElement areaSelector;

	private final CourseGroupManager courseGroupManager;
	
	public PreviewSettingsForm(UserRequest ureq, WindowControl wControl, ICourse course) {
		super(ureq, wControl);
		courseGroupManager = course.getCourseEnvironment().getCourseGroupManager();
		initForm(ureq);	
	}

	/**
	 * @return group
	 */
	public List<Long> getGroupKeys() { 
		return getKeys(groupSelector);
	}

	/**
	 * @return area
	 */
	public List<Long> getAreaKeys() { 
		return getKeys(areaSelector);
	}
	
	/**
	 * @return date
	 */
	public Date getDate() { 
		return sdate.getDate();
	}
	
	/**
	 * @return attributes map
	 */
	public Map<String,String> getAttributesMap() {
		Map <String,String>attributesMap = new HashMap<>();
		for (int i=0; i<attrNames.size(); i++) {
			if (!attrNames.get(i).isEmpty()) {
					attributesMap.put(
							attrNames.get(i).getValue(), attrValues.get(i).getValue()
					);
			}
		}
		return attributesMap;
	}
	
	public String getRole() {
		return roles.getSelectedKey();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		sdate = uifactory.addDateChooser("sdate","form.sdate" , null, formLayout);	
		sdate.setExampleKey("form.easy.example.bdate", null);
		sdate.setDateChooserTimeEnabled(true);
		sdate.setMandatory(true);
		sdate.setValidDateCheck("form.sdate.invalid");
		//setDate must be called after the DataChooser was configured
		sdate.setDate(new Date());
		
		List<BusinessGroup> groups = courseGroupManager.getAllBusinessGroups();
		String[] groupNames = new String[groups.size()];
		String[] groupKeys = new String[groups.size()];
		for(int i=groups.size(); i-->0; ) {
			groupNames[i] = groups.get(i).getName();
			groupKeys[i] = groups.get(i).getKey().toString();
		}
		groupSelector = uifactory.addCheckboxesVertical("details.groups", formLayout, groupKeys, groupNames, 1);
		groupSelector.setVisible(groups.size() > 0);
		
		List<BGArea> areas = courseGroupManager.getAllAreas();
		String[] areaNames = new String[areas.size()];
		String[] areaKeys = new String[areas.size()];
		for(int i=areas.size(); i-->0; ) {
			areaNames[i] = areas.get(i).getName();
			areaKeys[i] = areas.get(i).getKey().toString();
		}
		areaSelector = uifactory.addCheckboxesVertical("details.areas", formLayout, areaKeys, areaNames, 1);
		areaSelector.setVisible(areas.size() > 0);

		String[] keys = {
				ROLE_STUDENT,
				ROLE_GUEST,
				ROLE_COURSECOACH,
				ROLE_COURSEADMIN,
				ROLE_GLOBALAUTHOR
		};
		String[] values = new String[keys.length];
		for (int i = 0; i < keys.length; i++) {
			values[i]=translate(keys[i]);
		}
		roles = uifactory.addRadiosVertical("roles", "form.roles", formLayout, keys, values);
		roles.select(ROLE_STUDENT, true);
		
		String page = velocity_root + "/attributes.html";
		FormLayoutContainer attrlayout = FormLayoutContainer.createCustomFormLayout("attributes", getTranslator(), page);
		formLayout.add(attrlayout);
		attrlayout.setLabel("form.attributes", null);
				
		for (int i=0; i<NUMATTR; i++) {
			TextElement name = uifactory.addTextElement("attrname"+i, null, 255, "", attrlayout);
			((AbstractComponent)name.getComponent()).setDomReplacementWrapperRequired(false);
			name.setDisplaySize(12);
			TextElement value = uifactory.addTextElement("attrvalue"+i, "form.equals", 255, "", attrlayout);
			((AbstractComponent)value.getComponent()).setDomReplacementWrapperRequired(false);
			value.setDisplaySize(12);
			attrNames.add(name);
			attrValues.add(value);		
		}
		
		uifactory.addFormSubmitButton("submit", "command.preview", formLayout);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		return sdate.getDate()!=null;
	}
	
	private List<Long> getKeys(MultipleSelectionElement element) {
		List<Long> keys = new ArrayList<>();
		if(element.isAtLeastSelected(1)) {
			Collection<String> selectedKeys = element.getSelectedKeys();
			for(String selectedKey:selectedKeys) {
				keys.add(Long.parseLong(selectedKey));
			}
		}
		return keys;
	}
}