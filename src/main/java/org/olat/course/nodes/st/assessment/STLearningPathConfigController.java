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
package org.olat.course.nodes.st.assessment;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.course.editor.NodeEditController;
import org.olat.course.learningpath.ui.LearningPathNodeConfigController;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.st.STCourseNodeEditController;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 3 Feb 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class STLearningPathConfigController extends FormBasicController {

	private SingleSelection sequenceEl;
	
	private final ModuleConfiguration moduleConfig;

	public STLearningPathConfigController(UserRequest ureq, WindowControl wControl, ModuleConfiguration moduleConfig) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(STCourseNodeEditController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(LearningPathNodeConfigController.class, getLocale(), getTranslator()));
		this.moduleConfig = moduleConfig;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("config.title");
		setFormContextHelp("Learning Path");
		SelectionValues sequenceKV = new SelectionValues();
		sequenceKV.add(SelectionValues.entry(STCourseNode.CONFIG_LP_SEQUENCE_VALUE_SEQUENTIAL, translate("config.sequence.sequential")));
		sequenceKV.add(SelectionValues.entry(STCourseNode.CONFIG_LP_SEQUENCE_VALUE_WITHOUT, translate("config.sequence.without")));
		sequenceEl = uifactory.addRadiosHorizontal("config.sequence", formLayout, sequenceKV.keys(), sequenceKV.values());
		sequenceEl.addActionListener(FormEvent.ONCHANGE);
		String sequenceKey = moduleConfig.getStringValue(STCourseNode.CONFIG_LP_SEQUENCE_KEY, STCourseNode.CONFIG_LP_SEQUENCE_DEFAULT);
		sequenceEl.select(sequenceKey, true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == sequenceEl) {
			doSetSequence(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doSetSequence(UserRequest ureq) {
		String sequenceKey = sequenceEl.getSelectedKey();
		moduleConfig.setStringValue(STCourseNode.CONFIG_LP_SEQUENCE_KEY, sequenceKey);
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
