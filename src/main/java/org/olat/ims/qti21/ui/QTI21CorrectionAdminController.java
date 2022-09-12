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
package org.olat.ims.qti21.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.ims.qti21.QTI21Module;
import org.olat.ims.qti21.QTI21Module.CorrectionWorkflow;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.09.2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QTI21CorrectionAdminController extends FormBasicController {
	
	private static final String[] onKeys = new String[]{ "on" };
	private static final String KEY_VISIBLE = "visible";
	private static final String KEY_HIDDEN = "hidden";
	
	private MultipleSelectionElement anonymCorrectionWorkflowEl;
	private SingleSelection resultsVisibilityAfterCorrectionEl;
	
	@Autowired
	private QTI21Module qti21Module;
	
	public QTI21CorrectionAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.correction.title");
		
		anonymCorrectionWorkflowEl = uifactory.addCheckboxesHorizontal("correction.workflow", "correction.workflow", formLayout,
				onKeys, new String[] { translate("correction.workflow.anonymous") });
		if(qti21Module.getCorrectionWorkflow() == CorrectionWorkflow.anonymous) {
			anonymCorrectionWorkflowEl.select(onKeys[0], true);
		}
		
		SelectionValues visibilitySV = new SelectionValues();
		visibilitySV.add(new SelectionValue(KEY_HIDDEN, translate("results.user.visibility.hidden"), translate("results.user.visibility.hidden.desc"), "o_icon o_icon_results_hidden", null, true));
		visibilitySV.add(new SelectionValue(KEY_VISIBLE, translate("results.user.visibility.visible"), translate("results.user.visibility.visible.desc"), "o_icon o_icon_results_visible", null, true));
		resultsVisibilityAfterCorrectionEl = uifactory.addCardSingleSelectHorizontal("results.user.visibility",
				formLayout, visibilitySV.keys(), visibilitySV.values(), visibilitySV.descriptions(), visibilitySV.icons());
		if(qti21Module.isResultsVisibleAfterCorrectionWorkflow()) {
			resultsVisibilityAfterCorrectionEl.select(KEY_VISIBLE, true);
		} else {
			resultsVisibilityAfterCorrectionEl.select(KEY_HIDDEN, true);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		resultsVisibilityAfterCorrectionEl.clearError();
		if(!resultsVisibilityAfterCorrectionEl.isOneSelected()) {
			resultsVisibilityAfterCorrectionEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		CorrectionWorkflow correctionWf = anonymCorrectionWorkflowEl.isAtLeastSelected(1)
				? CorrectionWorkflow.anonymous : CorrectionWorkflow.named;
		qti21Module.setCorrectionWorkflow(correctionWf);
		qti21Module.setResultsVisibleAfterCorrectionWorkflow(resultsVisibilityAfterCorrectionEl.isKeySelected(KEY_VISIBLE));
	}
	
}