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
package org.olat.course.learningpath.ui;

import static org.olat.core.util.ArrayHelper.emptyStrings;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ObligationEditController extends FormBasicController implements Controller {
	
	private final Formatter formatter;
	private StaticTextElement infoEl;
	private MultipleSelectionElement obligationEl;
	private FormLayoutContainer buttonLayout;
	private FormLink resetOverwriteLink;
	
	private final AssessmentEntry assessmentEntry;
	private final Overridable<AssessmentObligation> obligation;
	private final boolean canEdit;
	
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private UserManager userManager;

	public ObligationEditController(UserRequest ureq, WindowControl wControl, AssessmentEntry assessmentEntry,
			boolean canEdit) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.assessmentEntry = assessmentEntry;
		this.canEdit = canEdit;
		this.obligation = assessmentEntry.getObligation();
		this.formatter = Formatter.getInstance(getLocale());
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		infoEl = uifactory.addStaticTextElement("info", null, null, formLayout);
		
		obligationEl = uifactory.addCheckboxesHorizontal("override.obligation", formLayout, emptyStrings(), emptyStrings());
		obligationEl.addActionListener(FormEvent.ONCHANGE);
		
		buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		resetOverwriteLink = uifactory.addFormLink("override.reset", buttonLayout, Link.BUTTON);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		
		updateUI();
	}
	
	private void updateUI() {
		boolean overridden = obligation.isOverridden();
		AssessmentObligation original = overridden? obligation.getOriginal(): obligation.getCurrent();
		
		if (overridden) {
			String[] args = new String[] {
					userManager.getUserDisplayName(obligation.getModBy()),
					formatter.formatDateAndTime(obligation.getModDate())
				};
			String infoText = AssessmentObligation.mandatory.equals(original)
					? translate("override.obligation.optional.info", args)
					: translate("override.obligation.mandatory.info", args);
			infoEl.setValue(infoText);
		}
		
		String[] keys;
		String[] values;
		if (AssessmentObligation.mandatory.equals(original)) {
			keys = new String[] { AssessmentObligation.optional.name() };
			values = new String[] { translate("override.obligation.optional.do") };
		} else {
			keys = new String[] { AssessmentObligation.mandatory.name() };
			values = new String[] { translate("override.obligation.mandatory.do") };
		}
		obligationEl.setKeysAndValues(keys, values);
		obligationEl.select(keys[0], overridden);
		
		infoEl.setVisible(overridden);
		obligationEl.setVisible(canEdit);
		buttonLayout.setVisible(canEdit);
		resetOverwriteLink.setVisible(canEdit && overridden);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == resetOverwriteLink) {
			doReset();
		} else if (source == obligationEl) {
			doSetObligation();
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doReset() {
		obligation.reset();
		updateUI();
	}

	private void doSetObligation() {
		boolean override = obligationEl.isAtLeastSelected(1);
		if (override) {
			AssessmentObligation selectedObligation = AssessmentObligation.valueOf(obligationEl.getKey(0));
			obligation.override(selectedObligation, getIdentity(), new Date());
		} else {
			obligation.reset();
		}
		updateUI();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		assessmentEntry.setObligation(obligation);
		assessmentService.updateAssessmentEntry(assessmentEntry);
		
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, FormEvent.CANCELLED_EVENT);
	}

	@Override
	protected void doDispose() {
		//
	}

}
