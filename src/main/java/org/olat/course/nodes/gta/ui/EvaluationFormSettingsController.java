/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.MSCourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 13 mai 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormSettingsController extends FormBasicController {
	
	private SingleSelection scoreTypeEl;
	
	private final String evalConfigurationProperty;
	private final ModuleConfiguration moduleConfiguration;

	public EvaluationFormSettingsController(UserRequest ureq, WindowControl wControl, ModuleConfiguration moduleConfiguration, String evalConfigurationProperty) {
		super(ureq, wControl);
		this.moduleConfiguration = moduleConfiguration;
		this.evalConfigurationProperty = evalConfigurationProperty;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues scorePK = new SelectionValues();
		scorePK.add(SelectionValues.entry(MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM, translate("score.evaluation.points.sum")));
		scorePK.add(SelectionValues.entry(MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_AVG, translate("score.evaluation.points.avg")));
		scoreTypeEl = uifactory.addRadiosVertical("scoring", "score.evaluation.scoring", formLayout,
				scorePK.keys(), scorePK.values());
		
		String val = moduleConfiguration.getStringValue(evalConfigurationProperty);
		if(StringHelper.containsNonWhitespace(val) && scorePK.containsKey(val)) {
			scoreTypeEl.select(val, true);
		} else {
			scoreTypeEl.select(MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM, true);
		}
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	public String getSelectedScoring() {
		return scoreTypeEl.isOneSelected() ? scoreTypeEl.getSelectedKey() : null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(!scoreTypeEl.isOneSelected()) {
			scoreTypeEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String selectedScore = scoreTypeEl.getSelectedKey();
		moduleConfiguration.setStringValue(evalConfigurationProperty, selectedScore);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
