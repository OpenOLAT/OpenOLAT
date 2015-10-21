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

package org.olat.course.nodes.info;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Description:<br>
 * Panel for the configuration of the info messages course node
 * 
 * <P>
 * Initial Date:  3 aug. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoConfigForm extends FormBasicController {

	
	private static final String[] maxDurationValues = new String[] {
		"5", "10", "30", "90", "365", "\u221E"
	};
	
	private static final String[] maxLengthValues = new String[] {
		"1", "2", "3", "4", "5", "7", "10", "25", "\u221E"
	};
	
	private static final String[] autoSubscribeKeys = new String[] {
		"on"
	};
	
	private final String[] autoSubscribeValues = new String[] {
		null
	};
	
	private final ModuleConfiguration config;
	
	private SingleSelection durationSelection;
	private SingleSelection lengthSelection;
	private MultipleSelectionElement autoSubscribeSelection;
	
	public InfoConfigForm(UserRequest ureq, WindowControl wControl, ModuleConfiguration config) {
		super(ureq, wControl);
		
		this.config = config;
		autoSubscribeValues[0] = translate("pane.tab.infos_config.auto_subscribe.on");
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_course_info_form");
		setFormTitle("pane.tab.infos_config.title");
		setFormContextHelp("Administration and Organisation#_mitteilung_zugang");

		String page = velocity_root + "/editShow.html";
		final FormLayoutContainer showLayout = FormLayoutContainer.createCustomFormLayout("pane.tab.infos_config.shown", getTranslator(), page);
		showLayout.setLabel("pane.tab.infos_config.shown", null);
		formLayout.add(showLayout);

		durationSelection = uifactory.addDropdownSingleselect("pane.tab.infos_config.max_duration", showLayout, maxDurationValues, maxDurationValues, null);
		durationSelection.setLabel("pane.tab.infos_config.max", null);
		durationSelection.setElementCssClass("o_sel_course_info_duration");
		String durationStr = (String)config.get(InfoCourseNodeConfiguration.CONFIG_DURATION);
		if(StringHelper.containsNonWhitespace(durationStr)) {
			durationSelection.select(durationStr, true);
		} else {
			durationSelection.select("30", true);
		}

		lengthSelection = uifactory.addDropdownSingleselect("pane.tab.infos_config.max_shown", null, showLayout, maxLengthValues, maxLengthValues, null);
		lengthSelection.setElementCssClass("o_sel_course_info_length");
		lengthSelection.setLabel("pane.tab.infos_config.max", null);
		String lengthStr = (String)config.get(InfoCourseNodeConfiguration.CONFIG_LENGTH);
		if(StringHelper.containsNonWhitespace(lengthStr)) {
			lengthSelection.select(lengthStr, true);
		} else {
			lengthSelection.select("5", true);
		}
		
		autoSubscribeSelection = uifactory.addCheckboxesHorizontal("auto_subscribe", formLayout, autoSubscribeKeys, autoSubscribeValues);
		String autoSubscribeStr = (String)config.get(InfoCourseNodeConfiguration.CONFIG_AUTOSUBSCRIBE);
		if("on".equals(autoSubscribeStr) || !StringHelper.containsNonWhitespace(autoSubscribeStr)) {
			autoSubscribeSelection.select("on", true);
		}
		
		uifactory.addFormSubmitButton("save", formLayout);
	}
	
	protected ModuleConfiguration getUpdatedConfig() {
		String durationStr = durationSelection.getSelectedKey();
		config.set(InfoCourseNodeConfiguration.CONFIG_DURATION, durationStr);
		
		String lengthStr = lengthSelection.getSelectedKey();
		config.set(InfoCourseNodeConfiguration.CONFIG_LENGTH, lengthStr);
		
		String autoSubscribeStr = autoSubscribeSelection.isSelected(0) ? "on" : "off";
		config.set(InfoCourseNodeConfiguration.CONFIG_AUTOSUBSCRIBE, autoSubscribeStr);
		return config;
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}
}
