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

import static org.olat.core.gui.translator.TranslatorHelper.translateAll;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.InfoCourseNode;
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
public class InfoConfigController extends FormBasicController {

	
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
	
	private static final String ROLE_COACH = "config.role.coach";
	private static final String ROLE_PARTICIPANT = "config.role.participant";
	private static final String[] ADMIN_KEYS = new String[] {
			ROLE_COACH
	};
	private static final String[] EDIT_KEYS = new String[] {
			ROLE_COACH,
			ROLE_PARTICIPANT
	};
	
	private final InfoCourseNode courseNode;
	private final ModuleConfiguration config;
	
	private SingleSelection durationSelection;
	private SingleSelection lengthSelection;
	private MultipleSelectionElement autoSubscribeSelection;
	private MultipleSelectionElement adminRolesEl;
	private MultipleSelectionElement editRolesEl;

	public InfoConfigController(UserRequest ureq, WindowControl wControl, InfoCourseNode courseNode) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.courseNode = courseNode;
		this.config = courseNode.getModuleConfiguration();
		autoSubscribeValues[0] = translate("pane.tab.infos_config.auto_subscribe.on");
		initForm(ureq);
	}
	
	public static List<String> getAllowedValues() {
		return Arrays.asList(maxDurationValues);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer generalCont = FormLayoutContainer.createDefaultFormLayout("general", getTranslator());
		generalCont.setFormTitle(translate("pane.tab.infos_config.title"));
		generalCont.setFormContextHelp("Administration and Organisation#_mitteilung_zugang");
		generalCont.setElementCssClass("o_sel_course_info_form");
		formLayout.add(generalCont);
		
		String page = velocity_root + "/editShow.html";
		final FormLayoutContainer showLayout = FormLayoutContainer.createCustomFormLayout("pane.tab.infos_config.shown", getTranslator(), page);
		showLayout.setLabel("pane.tab.infos_config.shown", null);
		generalCont.add(showLayout);

		durationSelection = uifactory.addDropdownSingleselect("pane.tab.infos_config.max_duration", showLayout, maxDurationValues, maxDurationValues, null);
		durationSelection.setLabel("pane.tab.infos_config.max", null);
		durationSelection.setElementCssClass("o_sel_course_info_duration");
		durationSelection.addActionListener(FormEvent.ONCHANGE);
		String durationStr = (String)config.get(InfoCourseNodeConfiguration.CONFIG_DURATION);
		if(StringHelper.containsNonWhitespace(durationStr)) {
			durationSelection.select(durationStr, true);
		} else {
			durationSelection.select("30", true);
		}

		lengthSelection = uifactory.addDropdownSingleselect("pane.tab.infos_config.max_shown", null, showLayout, maxLengthValues, maxLengthValues, null);
		lengthSelection.setElementCssClass("o_sel_course_info_length");
		lengthSelection.setLabel("pane.tab.infos_config.max", null);
		lengthSelection.addActionListener(FormEvent.ONCHANGE);
		String lengthStr = (String)config.get(InfoCourseNodeConfiguration.CONFIG_LENGTH);
		if(StringHelper.containsNonWhitespace(lengthStr)) {
			lengthSelection.select(lengthStr, true);
		} else {
			lengthSelection.select("5", true);
		}
		
		autoSubscribeSelection = uifactory.addCheckboxesHorizontal("auto_subscribe", generalCont, autoSubscribeKeys, autoSubscribeValues);
		autoSubscribeSelection.addActionListener(FormEvent.ONCHANGE);
		String autoSubscribeStr = (String)config.get(InfoCourseNodeConfiguration.CONFIG_AUTOSUBSCRIBE);
		if("on".equals(autoSubscribeStr) || !StringHelper.containsNonWhitespace(autoSubscribeStr)) {
			autoSubscribeSelection.select("on", true);
		}
		
		if (!courseNode.hasCustomPreConditions()) {
			FormLayoutContainer rightsCont = FormLayoutContainer.createDefaultFormLayout("rights", getTranslator());
			formLayout.add(rightsCont);
			rightsCont.setFormTitle(translate("config.rights"));
			
			adminRolesEl = uifactory.addCheckboxesVertical("config.admin", rightsCont, ADMIN_KEYS,
					translateAll(getTranslator(), ADMIN_KEYS), 1);
			adminRolesEl.select(ROLE_COACH, config.getBooleanSafe(InfoCourseNode.CONFIG_KEY_ADMIN_BY_COACH));
			adminRolesEl.addActionListener(FormEvent.ONCHANGE);
			
			editRolesEl = uifactory.addCheckboxesVertical("config.edit", rightsCont, EDIT_KEYS,
					translateAll(getTranslator(), EDIT_KEYS), 1);
			editRolesEl.select(ROLE_COACH, config.getBooleanSafe(InfoCourseNode.CONFIG_KEY_EDIT_BY_COACH));
			editRolesEl.select(ROLE_PARTICIPANT,
					config.getBooleanSafe(InfoCourseNode.CONFIG_KEY_EDIT_BY_PARTICIPANT));
			editRolesEl.addActionListener(FormEvent.ONCHANGE);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == durationSelection) {
			doUpdatedConfig(ureq);
		} else if (source == lengthSelection) {
			doUpdatedConfig(ureq);
		} else if (source == autoSubscribeSelection) {
			doUpdatedConfig(ureq);
		} else if (source == adminRolesEl) {
			doUpdatedConfig(ureq);
		} else if (source == editRolesEl) {
			doUpdatedConfig(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doUpdatedConfig(UserRequest ureq) {
		String durationStr = durationSelection.getSelectedKey();
		config.set(InfoCourseNodeConfiguration.CONFIG_DURATION, durationStr);
		
		String lengthStr = lengthSelection.getSelectedKey();
		config.set(InfoCourseNodeConfiguration.CONFIG_LENGTH, lengthStr);
		
		String autoSubscribeStr = autoSubscribeSelection.isSelected(0) ? "on" : "off";
		config.set(InfoCourseNodeConfiguration.CONFIG_AUTOSUBSCRIBE, autoSubscribeStr);
		
		if (adminRolesEl != null) {
			Collection<String> selectedAdminKeys = adminRolesEl.getSelectedKeys();
			config.setBooleanEntry(InfoCourseNode.CONFIG_KEY_ADMIN_BY_COACH, selectedAdminKeys.contains(ROLE_COACH));
		}
		
		if (editRolesEl != null) {
			Collection<String> selectedEditKeys = editRolesEl.getSelectedKeys();
			config.setBooleanEntry(InfoCourseNode.CONFIG_KEY_EDIT_BY_COACH, selectedEditKeys.contains(ROLE_COACH));
			config.setBooleanEntry(InfoCourseNode.CONFIG_KEY_EDIT_BY_PARTICIPANT, selectedEditKeys.contains(ROLE_PARTICIPANT));
		}
		
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
