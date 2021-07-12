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
package org.olat.course.nodes.cl.ui.wizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.editor.NodeConfigController;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.cl.ui.CheckListEditController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StructureNodeStepController extends StepFormBasicController {

	private static final String[] onKeys = new String[] { "on" };
	private static final String[] outputKeys = new String[]{ "cutvalue", "sum"};
	
	private SingleSelection outputEl;
	private RichTextElement descriptionEl;
	private TextElement shortTitleEl, titleEl, cutValueEl;
	private MultipleSelectionElement pointsEl, passedEl;
	private SpacerElement spacer;
	
	private final GeneratorData data;
	private final boolean scoreCalculatorSupported;
	
	@Autowired
	private NodeAccessService nodeAccessService;

	public StructureNodeStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(CheckListEditController.class, getLocale(), getTranslator()));
		data = (GeneratorData)getFromRunContext("data");
		NodeAccessType nodeAccessType = (NodeAccessType)getFromRunContext("nodeAccessType");
		scoreCalculatorSupported = nodeAccessService.isScoreCalculatorSupported(nodeAccessType);
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("structurenode.configuration");
		setFormDescription("structurenode.configuration.description");

		shortTitleEl = uifactory.addTextElement("nodeConfigForm.menutitle", "nodeConfigForm.menutitle", NodeConfigController.SHORT_TITLE_MAX_LENGTH, null, formLayout);
		shortTitleEl.setMandatory(true);
		shortTitleEl.setCheckVisibleLength(true);
		
		// add the title input text element
		titleEl = uifactory.addTextElement("nodeConfigForm.displaytitle", "nodeConfigForm.displaytitle", 255, null, formLayout);
		
		// add the learning objectives rich text input element
		descriptionEl = uifactory.addRichTextElementForStringData("nodeConfigForm.description", "nodeConfigForm.description", null, 10, -1, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		descriptionEl.setMaxLength(4000);
		
		spacer = uifactory.addSpacerElement("spaceman", formLayout, false);
		
		String[] pointValues = new String[]{ translate("points.sum.checklists") };
		pointsEl = uifactory.addCheckboxesHorizontal("points", formLayout, onKeys, pointValues);
		
		String[] passedValues = new String[]{ "" };
		passedEl = uifactory.addCheckboxesHorizontal("passed", "config.passed", formLayout, onKeys, passedValues);
		passedEl.addActionListener(FormEvent.ONCHANGE);

		String[] outputValues = new String[]{
			translate("config.output.cutvalue"), translate("scform.passedtype.inherit")
		};
		outputEl = uifactory.addRadiosVertical("output", "config.output", formLayout, outputKeys, outputValues);
		outputEl.select(outputKeys[0], true);
		outputEl.addActionListener(FormEvent.ONCHANGE);
		outputEl.setVisible(false);

		cutValueEl = uifactory.addTextElement("cutvalue", "config.cutvalue", 4, null, formLayout);
		cutValueEl.setDisplaySize(5);
		cutValueEl.setVisible(false);
		
		updatePassedAndOutputUI();
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		shortTitleEl.clearError();
		if(!StringHelper.containsNonWhitespace(shortTitleEl.getValue())) {
			shortTitleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		cutValueEl.clearError();
		if(cutValueEl.isVisible() && !StringHelper.containsNonWhitespace(cutValueEl.getValue())) {
			try {
				Float.parseFloat(cutValueEl.getValue());
			} catch (NumberFormatException e) {
				cutValueEl.setErrorKey("form.error.wrongFloat", null);
				allOk &= false;
			}
		}
		
		if(outputEl.isVisible() && outputEl.isSelected(0)) {
			if(!StringHelper.containsNonWhitespace(cutValueEl.getValue())) {
				cutValueEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(passedEl == source) {
			updatePassedAndOutputUI();
		} else if(outputEl == source) {
			updatePassedAndOutputUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void updatePassedAndOutputUI() {
		spacer.setVisible(scoreCalculatorSupported);
		pointsEl.setVisible(scoreCalculatorSupported);
		passedEl.setVisible(scoreCalculatorSupported);
		
		boolean selected = passedEl.isAtLeastSelected(1);
		cutValueEl.setVisible(scoreCalculatorSupported && selected);
		outputEl.setVisible(scoreCalculatorSupported && selected);
		if(selected) {
			boolean cutValueSelected = outputEl.isSelected(0);
			cutValueEl.setVisible(scoreCalculatorSupported && cutValueSelected);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		data.setStructureTitle(titleEl.getValue());
		data.setStructureShortTitle(shortTitleEl.getValue());
		data.setStructureDescription(descriptionEl.getValue());
		
		if (scoreCalculatorSupported) {
			data.setPoints(pointsEl.isAtLeastSelected(1));
			data.setPassed(passedEl.isAtLeastSelected(1));
			if(outputEl.isVisible() && outputEl.isSelected(0)) {
				Float cutValue = new Float(Float.parseFloat(cutValueEl.getValue()));
				data.setCutValue(cutValue);
			}
		}
		
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}
}
