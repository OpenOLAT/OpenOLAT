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
	
	protected final boolean restrictedEdit, readOnly;
	private final AssessmentItemRef assessmentItemRef;

	private static final String[] attemtpsKeys = new String[] { "y", "n", "inherit" };
	
	public AssessmentItemRefEditorController(UserRequest ureq, WindowControl wControl,
			AssessmentItemRef itemRef, boolean restrictedEdit, boolean readOnly) {
		super(ureq, wControl);
		this.assessmentItemRef = itemRef;
		this.readOnly = readOnly;
		this.restrictedEdit = restrictedEdit;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_assessment_item_options");
		if(assessmentItemRef == null) return;
		
		Integer maxAttempts = null;
		if(assessmentItemRef.getItemSessionControl() != null) {
			maxAttempts = assessmentItemRef.getItemSessionControl().getMaxAttempts();
		}
		String[] yesnoValues = new String[] { translate("yes"), translate("no"), translate("inherit") };
		limitAttemptsEl = uifactory.addRadiosHorizontal("form.imd.limittries", formLayout, attemtpsKeys, yesnoValues);
		limitAttemptsEl.addActionListener(FormEvent.ONCLICK);
		if(maxAttempts == null) {
			limitAttemptsEl.select(attemtpsKeys[2], true);
		} else if(maxAttempts.intValue() == 0) {
			limitAttemptsEl.select(attemtpsKeys[1], true);
		} else {
			limitAttemptsEl.select(attemtpsKeys[0], true);
		}
		limitAttemptsEl.setEnabled(!restrictedEdit && !readOnly);
		
		String maxAttemptsStr = maxAttempts == null ? "" : maxAttempts.toString();
		maxAttemptsEl = uifactory.addTextElement("maxAttempts", null, 8, maxAttemptsStr, formLayout);
		maxAttemptsEl.setVisible(limitAttemptsEl.isSelected(0));
		maxAttemptsEl.setEnabled(!restrictedEdit && !readOnly);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(limitAttemptsEl != null && limitAttemptsEl.isOneSelected() && limitAttemptsEl.isSelected(0) && maxAttemptsEl != null) {
			maxAttemptsEl.clearError();
			if(StringHelper.containsNonWhitespace(maxAttemptsEl.getValue())) {
				try {
					Integer.parseInt(maxAttemptsEl.getValue());
				} catch(NumberFormatException e) {
					maxAttemptsEl.setErrorKey("form.error.nointeger", null);
					allOk &= false;
				}
			}
		}
		return allOk;
	}
	
	/**
	 * This validate a mandatory double
	 * @param el
	 * @return
	 */
	protected boolean validateDouble(TextElement el) {
		boolean allOk = true;
		
		String value = el.getValue();
		el.clearError();
		if(!StringHelper.containsNonWhitespace(value)) {
			el.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else {
			try {
				Double.parseDouble(value);
			} catch (NumberFormatException e) {
				el.setErrorKey("error.double", null);
				allOk &= false;
			}
		}
		return allOk;
	}
	
	protected boolean validateMinMaxScores(TextElement minScoreEl, TextElement maxScoreEl) {
		boolean allOk = true;
		allOk &= validateDouble(minScoreEl);
		allOk &= validateDouble(maxScoreEl);
		
		if(allOk) {
			double minScore =Double.parseDouble(minScoreEl.getValue());
			double maxScore = Double.parseDouble(maxScoreEl.getValue());
			if(minScore > maxScore) {
				minScoreEl.setErrorKey("error.min.score.bigger.max", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(limitAttemptsEl == source) {
			maxAttemptsEl.setVisible(limitAttemptsEl.isOneSelected() && limitAttemptsEl.isSelected(0));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(limitAttemptsEl == null) {
			//case in question poll where there isn't an AssessmentItemRef
		} else if(limitAttemptsEl.isOneSelected() && limitAttemptsEl.isSelected(0)
				&& maxAttemptsEl != null && maxAttemptsEl.isVisible()
				&& StringHelper.isLong(maxAttemptsEl.getValue())) {
			try {
				getOrCreateItemSessionControl().setMaxAttempts(Integer.valueOf(maxAttemptsEl.getValue()));
			} catch(NumberFormatException e) {
				//do nothing
			}
		} else if(limitAttemptsEl.isOneSelected() && limitAttemptsEl.isSelected(1)) {
			getOrCreateItemSessionControl().setMaxAttempts(0);
		} else {
			getOrCreateItemSessionControl().setMaxAttempts(null);
		}
	}
	
	protected final ItemSessionControl getOrCreateItemSessionControl() {
		ItemSessionControl itemSessionControl = assessmentItemRef.getItemSessionControl();
		if(itemSessionControl == null) {
			itemSessionControl = new ItemSessionControl(assessmentItemRef);
			assessmentItemRef.setItemSessionControl(itemSessionControl);
		}
		return itemSessionControl;
	}
}
