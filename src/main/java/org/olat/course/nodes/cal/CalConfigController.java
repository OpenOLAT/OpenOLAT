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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.cal;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.CalCourseNode;
import org.olat.modules.ModuleConfiguration;

public class CalConfigController extends FormBasicController {
	
	private DateChooser dateChooser;
	private SingleSelection autoDateEl;
	
	private final ModuleConfiguration config;

	public CalConfigController(UserRequest ureq, WindowControl wControl, CalCourseNode courseNode) {
		super(ureq, wControl);
		this.config = courseNode.getModuleConfiguration();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("pane.tab.calconfig");

		boolean autoDate = CalEditController.getAutoDate(config);
		String[] keys = new String[]{"auto","selected"};
		String[] values = new String[]{translate("pane.tab.auto_date"),translate("pane.tab.manual_date")};
		autoDateEl = uifactory.addRadiosVertical("pane.tab_auto_date", "pane.tab.start_date", formLayout, keys, values);
		autoDateEl.setHelpText(translate("fhelp.start_date"));
		autoDateEl.select(autoDate ? keys[0] : keys[1], true);
		autoDateEl.addActionListener(FormEvent.ONCHANGE);
		
		Date startDate = CalEditController.getStartDate(config);
		Date selectedDate = startDate == null ? new Date() : startDate;
		dateChooser = uifactory.addDateChooser("pane.tab.start_date_chooser", null, null, formLayout);
		dateChooser.setDate(selectedDate);
		dateChooser.setVisible(!autoDate);
		dateChooser.addActionListener(FormEvent.ONCHANGE);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == autoDateEl) {
			boolean autoDate = autoDateEl.isSelected(0);
			dateChooser.setVisible(!autoDate);
			flc.setDirty(true);
			doUpdatedConfig(ureq);
		} else if (source == dateChooser) {
			doUpdatedConfig(ureq);
		}
	}

	private void doUpdatedConfig(UserRequest ureq) {
		boolean autoDate = autoDateEl.isSelected(0);
		CalEditController.setAutoDate(config, autoDate);
		
		Date startDate = dateChooser.getDate();
		CalEditController.setStartDate(config, startDate);
		
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
}