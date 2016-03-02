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
package org.olat.ims.qti21.ui.editor.interactions;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.ItemSessionControl;

/**
 * 
 * Initial date: 02.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AssessmentItemRefEditorController extends FormBasicController {
	
	private TextElement maxAttemptsEl;
	private SingleSelection limitAttemptsEl;
	
	private final AssessmentItemRef assessmentItemRef;

	private static final String[] yesnoKeys = new String[] { "y", "n" };
	
	public AssessmentItemRefEditorController(UserRequest ureq, WindowControl wControl, AssessmentItemRef itemRef) {
		super(ureq, wControl);
		this.assessmentItemRef = itemRef;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(assessmentItemRef == null) return;
		
		String maxAttempts = "";
		if(assessmentItemRef.getItemSessionControl() != null) {
			ItemSessionControl itemSessionControl = assessmentItemRef.getItemSessionControl();
			if(itemSessionControl.getMaxAttempts() != null) {
				maxAttempts = assessmentItemRef.getItemSessionControl().getMaxAttempts().toString();
			}
		}
		String[] yesnoValues = new String[] { translate("yes"), translate("no") };
		limitAttemptsEl = uifactory.addRadiosHorizontal("form.imd.limittries", formLayout, yesnoKeys, yesnoValues);
		limitAttemptsEl.addActionListener(FormEvent.ONCLICK);
		limitAttemptsEl.select(maxAttempts.length() > 0 ? yesnoKeys[0] : yesnoKeys[1], true);
		
		maxAttemptsEl = uifactory.addTextElement("maxAttempts", null, 8, maxAttempts, formLayout);
		maxAttemptsEl.setVisible(limitAttemptsEl.isSelected(0));

	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		if(maxAttemptsEl != null) {
			maxAttemptsEl.clearError();
			if(StringHelper.containsNonWhitespace(maxAttemptsEl.getValue())) {
				try {
					Integer.parseInt(maxAttemptsEl.getValue());
				} catch(NumberFormatException e) {
					maxAttemptsEl.setErrorKey("error.integer", null);
					allOk &= false;
				}
			}
		}
		return allOk & super.validateFormLogic(ureq);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(limitAttemptsEl == source) {
			maxAttemptsEl.setVisible(limitAttemptsEl.isSelected(0));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(maxAttemptsEl != null && maxAttemptsEl.isVisible() && StringHelper.isLong(maxAttemptsEl.getValue())) {
			ItemSessionControl itemSessionControl = assessmentItemRef.getItemSessionControl();
			if(itemSessionControl == null) {
				itemSessionControl = new ItemSessionControl(assessmentItemRef);
				assessmentItemRef.setItemSessionControl(itemSessionControl);
			}
			
			try {
				itemSessionControl.setMaxAttempts(new Integer(maxAttemptsEl.getValue()));
			} catch(NumberFormatException e) {
				//do nothing
			}
		} else {
			ItemSessionControl itemSessionControl = assessmentItemRef.getItemSessionControl();
			if(itemSessionControl != null) {
				itemSessionControl.setMaxAttempts(null);
			}
		}
	}
}
