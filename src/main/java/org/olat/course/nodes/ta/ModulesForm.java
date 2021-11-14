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

package org.olat.course.nodes.ta;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.TACourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * Initial Date:  30.08.2004
 * @author Mike Stock
 */

public class ModulesForm extends FormBasicController {

	private ModuleConfiguration config;
	private SelectionElement task, dropbox, returnbox, scoring, solution;
	
	/**
	 * Modules selection form.
	 * @param name
	 * @param config
	 */
	public ModulesForm(UserRequest ureq, WindowControl wControl, ModuleConfiguration config) {
		super(ureq, wControl);
		this.config = config;
		initForm(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// 
	}
	
	@Override
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof SelectionElement) {
			if (atLeastOneIsChecked((SelectionElement)source)) {
				fireEvent (ureq, new Event(source.getName()+":"+((SelectionElement)source).isSelected(0)));
			}
		}
	}
	
	private boolean atLeastOneIsChecked (SelectionElement cb) {
		
		setFormInfo(null);
		
		if (task.isSelected(0) ||
		    dropbox.isSelected(0) ||
		    returnbox.isSelected(0) ||
		    scoring.isSelected(0) ||
		    solution.isSelected(0)) return true;
		
		setFormInfo("atleastone");
		cb.select("xx", true);
		return false;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("form.modules.title");

		task = uifactory.addCheckboxesHorizontal("task", "form.modules.task", formLayout, new String[]{"xx"}, new String[]{""});
		dropbox = uifactory.addCheckboxesHorizontal("dropbox", "form.modules.dropbox", formLayout, new String[]{"xx"}, new String[]{""});
		returnbox = uifactory.addCheckboxesHorizontal("returnbox", "form.modules.returnbox", formLayout, new String[]{"xx"}, new String[]{""});
		scoring = uifactory.addCheckboxesHorizontal("scoring", "form.modules.scoring", formLayout, new String[]{"xx"}, new String[]{""});
		solution = uifactory.addCheckboxesHorizontal("solution", "form.modules.sample", formLayout, new String[]{"xx"}, new String[]{""});
	
		Boolean cv;
		
		cv = (Boolean) config.get(TACourseNode.CONF_TASK_ENABLED);
		task.select("xx", cv == null ? false : cv.booleanValue());
		
		cv = (Boolean)config.get(TACourseNode.CONF_DROPBOX_ENABLED);
		dropbox.select("xx", cv == null ? false : cv.booleanValue());
		
		cv = (Boolean)config.get(TACourseNode.CONF_RETURNBOX_ENABLED);
		returnbox.select("xx", cv == null ? false : cv.booleanValue());
		
		cv = (Boolean)config.get(TACourseNode.CONF_SCORING_ENABLED);
		scoring.select("xx", cv == null ? false : cv.booleanValue());
		
		cv = (Boolean)config.get(TACourseNode.CONF_SOLUTION_ENABLED);
		solution.select("xx", cv == null ? false : cv.booleanValue());
		
		task.addActionListener(FormEvent.ONCLICK);
		dropbox.addActionListener(FormEvent.ONCLICK);
		returnbox.addActionListener(FormEvent.ONCLICK);
		scoring.addActionListener(FormEvent.ONCLICK);
		solution.addActionListener(FormEvent.ONCLICK);
	}

}
