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
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.progressbar.ProgressBarItem;
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

	private FormLink previousButton;
	private FormLink nextButton;
	private FormLink confirmButton;
	private ProgressBarItem progressBar;
	
	public CoursePaginationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "pagination");
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		previousButton = uifactory.addFormLink("previous", "previous", "", null, formLayout, Link.BUTTON_XSMALL | Link.NONTRANSLATED);
		previousButton.setDomReplacementWrapperRequired(false);
		previousButton.setIconLeftCSS("o_icon o_icon_previous_page");
		
		progressBar = uifactory.addProgressBar("progress", null, formLayout);
		progressBar.setMax(1);
		progressBar.setLabelAlignment(LabelAlignment.none);
		progressBar.setWidthInPercent(true);
		
		confirmButton = uifactory.addFormLink("confirm", "confirm", "command.assessment.done", null, formLayout, Link.BUTTON_XSMALL);
		confirmButton.setIconLeftCSS("o_icon o_icon_status_done");
		
		nextButton = uifactory.addFormLink("next", "next", "", null, formLayout, Link.BUTTON_XSMALL | Link.NONTRANSLATED);
		nextButton.setDomReplacementWrapperRequired(false);
		nextButton.setIconLeftCSS("o_icon o_icon_next_page");
	}

	public void updateNextPreviousUI(boolean previousEnabled, boolean nextEnabled) {
		previousButton.setEnabled(previousEnabled);
		nextButton.setEnabled(nextEnabled);
	}

	public void updateAssessmentConfirmUI(boolean cornfirmVisible) {
		confirmButton.setVisible(cornfirmVisible);
		flc.setDirty(true);
	}
	
	public void updateProgressUI(float actual) {
		progressBar.setActual(actual);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(previousButton == source) {
			doPrevious(ureq);
		} else if(nextButton == source) {
			doNext(ureq);
		} else if (confirmButton == source) {
			doConfirm(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doPrevious(UserRequest ureq) {
		fireEvent(ureq, PREVIOUS_EVENT);
	}

	private void doNext(UserRequest ureq) {
		fireEvent(ureq, NEXT_EVENT);
	}

	private void doConfirm(UserRequest ureq) {
		fireEvent(ureq, CONFIRMED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
