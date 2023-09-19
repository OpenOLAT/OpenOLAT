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
package org.olat.course.run;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.FormToggle.Presentation;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 10 Oct 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CoursePaginationController extends FormBasicController {
	
	public static final Event NEXT_EVENT = new Event("next");
	public static final Event PREVIOUS_EVENT = new Event("previous");
	public static final Event CONFIRMED_EVENT = new Event("confirmed");
	public static final Event UNCONFIRMED_EVENT = new Event("unconfirmed");

	private FormLink previousButton;
	private FormLink nextButton;
	private FormToggle confirmToggle;
	
	public CoursePaginationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "pagination");
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		confirmToggle = uifactory.addToggleButton("confirm", null, translate("command.assessment.done"), translate("command.assessment.mark.done"), formLayout);
		confirmToggle.setPresentation(Presentation.BUTTON_XSMALL);
		confirmToggle.setElementCssClass("o_course_pagination_confirmation");
		
		previousButton = uifactory.addFormLink("previous", "previous", "", null, formLayout, Link.BUTTON_XSMALL | Link.NONTRANSLATED);
		previousButton.setDomReplacementWrapperRequired(false);
		previousButton.setIconLeftCSS("o_icon o_icon-fw o_icon_course_previous");
		previousButton.setLinkTitle(translate("command.previous"));
		previousButton.setAriaLabel(translate("command.previous"));
		
		nextButton = uifactory.addFormLink("next", "next", "", null, formLayout, Link.BUTTON_XSMALL | Link.NONTRANSLATED);
		nextButton.setDomReplacementWrapperRequired(false);
		nextButton.setIconRightCSS("o_icon o_icon-fw o_icon_course_next");
		nextButton.setLinkTitle(translate("command.next"));
		nextButton.setAriaLabel(translate("command.next"));
	}
	
	public void enableLargeStyleRendering() {
		// Change button text 
		previousButton.setI18nKey(translate("command.previous"));		
		nextButton.setI18nKey(translate("command.next"));
		
		// Button style
		confirmToggle.setPresentation(Presentation.BUTTON);
		String styleEnabled = "btn btn-link";
		String styleDisabled = styleEnabled + (" o_disabled disabled");			
		previousButton.getComponent().setCustomEnabledLinkCSS(styleEnabled);
		previousButton.getComponent().setCustomDisabledLinkCSS(styleDisabled);
		nextButton.getComponent().setCustomEnabledLinkCSS(styleEnabled);
		nextButton.getComponent().setCustomDisabledLinkCSS(styleDisabled);
	}

	public void updateNextPreviousUI(boolean previousEnabled, boolean nextEnabled) {
		previousButton.setEnabled(previousEnabled);
		nextButton.setEnabled(nextEnabled);
	}

	public void updateAssessmentConfirmUI(boolean confirmVisible, boolean doConfirm) {
		if (doConfirm) {
			confirmToggle.toggleOff();
		} else {
			confirmToggle.toggleOn();
		}
		confirmToggle.setVisible(confirmVisible);
		flc.setDirty(true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(previousButton == source) {
			doPrevious(ureq);
		} else if(nextButton == source) {
			doNext(ureq);
		} else if (confirmToggle == source) {
			doConfirm(ureq, confirmToggle.isOn());
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doPrevious(UserRequest ureq) {
		fireEvent(ureq, PREVIOUS_EVENT);
	}

	private void doNext(UserRequest ureq) {
		fireEvent(ureq, NEXT_EVENT);
	}

	private void doConfirm(UserRequest ureq, boolean done) {
		if (done) {
			fireEvent(ureq, CONFIRMED_EVENT);
		} else {
			fireEvent(ureq, UNCONFIRMED_EVENT);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
