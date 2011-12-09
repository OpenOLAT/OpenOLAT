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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.course.ICourse;
import org.olat.course.condition.GroupOrAreaSelectionController;

/**
 * Description:<br>
 * TODO: Felix Jost Class Description for PreviewSettingsForm
 * 
 * <P>
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
	
	private TextElement group, area;
	private FormLink groupChooserLink, areaChooserLink;

	private final int NUMATTR = 5;
	private List <TextElement> attrNames  = new ArrayList<TextElement>(NUMATTR);
	private List <TextElement> attrValues = new ArrayList<TextElement>(NUMATTR);
	
	private SingleSelection roles;
	
	private ICourse course;
	//private GroupAndAreaSelectController chooser;
	private GroupOrAreaSelectionController groupChooser, areaChooser;
	
	private CloseableModalController cmc;
	
	public PreviewSettingsForm(UserRequest ureq, WindowControl wControl, ICourse course) {
		super(ureq, wControl);
		this.course = course;
		initForm(ureq);	
	}


	/**
	 * @return group
	 */
	public String getGroup() { return group.getValue(); }

	/**
	 * @return area
	 */
	public String getArea() { return area.getValue(); }
	
	/**
	 * @return date
	 */
	public Date getDate() { return sdate.getDate(); }
	
	/**
	 * @return attributes map
	 */
	public Map getAttributesMap() {
		Map <String,String>attributesMap = new HashMap<String,String>();
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
		
		sdate = uifactory.addDateChooser("sdate","form.sdate" , "", formLayout);
				
		sdate.setExampleKey("form.easy.example.bdate", null);
		sdate.setDateChooserDateFormat("%d.%m.%Y %H:%M");
		sdate.setCustomDateFormat("dd.MM.yyyy HH:mm");
		sdate.setDateChooserTimeEnabled(true);
		sdate.setMandatory(true);
		sdate.setValidDateCheck("form.sdate.invalid");
		//setDate must be called after the DataChooser was configured
		sdate.setDate(new Date());
		
		FormLayoutContainer groupLayout = FormLayoutContainer.createHorizontalFormLayout("groupChooser", getTranslator());
		groupLayout.setLabel("form.group", null);
		formLayout.add(groupLayout);
		group = uifactory.addTextElement("group", null, 255, "", groupLayout);
		groupChooserLink = uifactory.addFormLink("choose", groupLayout,"b_form_genericchooser");
	
		
		FormLayoutContainer areaLayout = FormLayoutContainer.createHorizontalFormLayout("areaChooser", getTranslator());
		areaLayout.setLabel("form.area", null);
		formLayout.add(areaLayout);
		area = uifactory.addTextElement("area", null, 255, "", areaLayout);
		areaChooserLink = uifactory.addFormLink("choose", areaLayout,"b_form_genericchooser");
		
		
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
		
		FormLayoutContainer attrlayout = FormLayoutContainer.createVerticalFormLayout("attributes", getTranslator());
		formLayout.add(attrlayout);
		attrlayout.setLabel("form.attributes", null);
				
		for (int i=0; i<NUMATTR; i++) {
			FormLayoutContainer attrgrp = FormLayoutContainer.createHorizontalFormLayout("attrgrp"+i, getTranslator());
			attrlayout.add(attrgrp);

			TextElement name = uifactory.addTextElement("attrname"+i, null, 255, "", attrgrp);
			name.setDisplaySize(12);
			TextElement value = uifactory.addTextElement("attrvalue"+i, "form.equals", 255, "", attrgrp);
			value.setDisplaySize(12);
			attrNames.add(name);
			attrValues.add(value);		
		}
		
		uifactory.addFormSubmitButton("submit", "command.preview", formLayout);
	}
	
	@Override
	protected void  formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == groupChooserLink) {
			removeAsListenerAndDispose (groupChooser);
			groupChooser = new GroupOrAreaSelectionController(
					0, //group
					getWindowControl(), ureq,
					"group", course.getCourseEnvironment().getCourseGroupManager(),
					group.getValue()
			);
			listenTo(groupChooser);
				
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(
				getWindowControl(),
				translate("close"),
				groupChooser.getInitialComponent()
			);
			listenTo(cmc);
			cmc.activate();
			
		} else if (source == areaChooserLink) {
			removeAsListenerAndDispose (areaChooser);
			areaChooser = new GroupOrAreaSelectionController(
					1, // area
					getWindowControl(), ureq,
					"area", course.getCourseEnvironment().getCourseGroupManager(),
					area.getValue()
			);
			listenTo(areaChooser);
			
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(
				getWindowControl(),
				translate("close"),
				areaChooser.getInitialComponent()
			);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == groupChooser) {
			cmc.deactivate();
			group.setValue(StringHelper.formatAsCSVString(groupChooser.getSelectedEntries()));

		} else if (source == areaChooser) {
			cmc.deactivate();
			area.setValue(StringHelper.formatAsCSVString(areaChooser.getSelectedEntries()));
		}
	}
	@Override
	protected void doDispose() {
		//
	}
	
	protected boolean validateFormLogic(UserRequest ureq) {
		return sdate.getDate()!=null;
	}
}

