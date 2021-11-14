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
package org.olat.course.config.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 27 Mar 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentResetController extends FormBasicController {
	
	public static final Event RESET_SETTING_EVENT = new Event("reset.changes");
	
	private static final String ENABLE = "enable";
	private static final String[] ENABLE_KEYS = new String [] {ENABLE};
	
	private MultipleSelectionElement recalculateAllEl;
	private MultipleSelectionElement resetPassedEl;
	private MultipleSelectionElement resetOverridenEl;
	private FormLink discardButton;

	private final boolean showResetOverriden;
	private final boolean showDiscard;

	public AssessmentResetController(UserRequest ureq, WindowControl wControl, boolean showResetOverriden, boolean showDiscard) {
		super(ureq, wControl);
		this.showResetOverriden = showResetOverriden;
		this.showDiscard = showDiscard;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("assessment.reset.desc", null);
		
		recalculateAllEl = uifactory.addCheckboxesVertical("recalculate.all",
				"assessment.reset.recalculate.all.label", formLayout, ENABLE_KEYS,
				new String[] { translate("assessment.reset.recalculate.all") }, 1);
		recalculateAllEl.select(recalculateAllEl.getKey(0), true);
		recalculateAllEl.addActionListener(FormEvent.ONCHANGE);

		resetPassedEl = uifactory.addCheckboxesVertical("reset.passed", "assessment.reset.passed.label",
				formLayout, ENABLE_KEYS, new String[] { translate("assessment.reset.passed") }, 1);

		resetOverridenEl = uifactory.addCheckboxesVertical("reset.overriden", "assessment.reset.overriden.label",
				formLayout, ENABLE_KEYS, new String[] { translate("assessment.reset.overriden") }, 1);
		resetOverridenEl.setVisible(showResetOverriden);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		if (showDiscard) {
			discardButton = uifactory.addFormLink("assessment.reset.discard", buttonsCont, Link.BUTTON);
		}
		
		updateUI();
	}
	
	private void updateUI() {
		boolean recalculate = recalculateAllEl.isAtLeastSelected(1);
		resetPassedEl.setVisible(recalculate);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == recalculateAllEl) {
			updateUI();
		} else 
		if (source == discardButton) {
			fireEvent(ureq, RESET_SETTING_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean recalculateAll = recalculateAllEl.isAtLeastSelected(0);
		boolean resetPassed = resetPassedEl.isVisible() && resetPassedEl.isAtLeastSelected(1);
		boolean resetOverriden = resetOverridenEl.isAtLeastSelected(1);
		Event confirmedEvent = new AssessmentResetEvent(recalculateAll, resetPassed, resetOverriden);
		fireEvent(ureq, confirmedEvent );
	}
	
	public static class AssessmentResetEvent extends Event {
		
		private static final long serialVersionUID = 915883316389709373L;

		private final boolean recalculateAll;
		private final boolean resetPassed;
		private final boolean resetOverriden;
		
		public AssessmentResetEvent(boolean recalculateAll, boolean resetPassed, boolean resetOverriden) {
			super("assessment.reset");
			this.resetPassed = resetPassed;
			this.resetOverriden = resetOverriden;
			this.recalculateAll = recalculateAll;
		}
		
		public boolean isRecalculateAll() {
			return recalculateAll;
		}
		
		public boolean isResetPassed() {
			return resetPassed;
		}
		
		public boolean isResetOverriden() {
			return resetOverriden;
		}
		
	}

}
