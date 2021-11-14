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

package org.olat.course.nodes.projectbroker;

import java.util.HashMap;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerModuleConfiguration;

/**
 * 
 * @author guretzki
 */

public class ProjectEventFormController extends FormBasicController {
	private final String KEY_EVENT_TABLE_VIEW_ENABLED = "event.table.view.enabled";
	
	
	private ProjectBrokerModuleConfiguration config;
	private Map<Project.EventType, MultipleSelectionElement> projectEventElementList;

	/**
	 * Modules selection form.
	 * @param name
	 * @param config
	 */
	public ProjectEventFormController(UserRequest ureq, WindowControl wControl, ProjectBrokerModuleConfiguration config) {
		super(ureq, wControl);
		this.config = config;
		projectEventElementList = new HashMap<>();
		initForm(this.flc, this, ureq);
	}

	/**
	 * Initialize form.
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		int index = 1;
		for (Project.EventType eventType : Project.EventType.values()) {
			if (index++ > 1) {
				uifactory.addSpacerElement("event_spacer" + index, formLayout, false);
			}
			String[] keys = new String[] { "event.enabled", KEY_EVENT_TABLE_VIEW_ENABLED };
			String[] values = new String[] { translate(eventType.getI18nKey() + ".label"), translate(KEY_EVENT_TABLE_VIEW_ENABLED) };
			boolean isEventEnabled = config.isProjectEventEnabled(eventType);
			boolean isTableViewEnabled = config.isProjectEventTableViewEnabled(eventType);
			MultipleSelectionElement projectEventElement = uifactory.addCheckboxesVertical(eventType.toString(), null, formLayout, keys, values, 1);
			projectEventElement.select(keys[0], isEventEnabled);
			projectEventElement.setVisible(keys[1], isEventEnabled);
			projectEventElement.select(keys[1], isTableViewEnabled);
			projectEventElement.addActionListener(FormEvent.ONCLICK);
			projectEventElementList.put(eventType, projectEventElement);
		}
		uifactory.addFormSubmitButton("save", formLayout);		
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for (Project.EventType eventType : projectEventElementList.keySet()) {
			boolean isEventEnabled = projectEventElementList.get(eventType).isSelected(0);
			config.setProjectEventEnabled(eventType, isEventEnabled);
			if (isEventEnabled) {
				config.setProjectEventTableViewEnabled(eventType, projectEventElementList.get(eventType).isSelected(1));
			} else {
				config.setProjectEventTableViewEnabled(eventType,false);
			}
			projectEventElementList.get(eventType).setEnabled(KEY_EVENT_TABLE_VIEW_ENABLED, isEventEnabled);
		}
		fireEvent(ureq, Event.DONE_EVENT);
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		for (Project.EventType eventType : projectEventElementList.keySet()) {
			boolean isEventEnabled = projectEventElementList.get(eventType).isSelected(0);
			projectEventElementList.get(eventType).setVisible(KEY_EVENT_TABLE_VIEW_ENABLED, isEventEnabled);
		}
		this.flc.setDirty(true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
	}

}
