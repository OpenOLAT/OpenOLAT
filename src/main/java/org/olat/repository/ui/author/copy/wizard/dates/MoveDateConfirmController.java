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
package org.olat.repository.ui.author.copy.wizard.dates;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.ui.author.copy.wizard.CopyCourseStepsStep;

/**
 * Initial date: 26.05.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class MoveDateConfirmController extends FormBasicController {

	private static final String REMEMBER_ME = "remember_me";
	
	private DateChooser sourceDateChooser;
	
	private MultipleSelectionElement dontAskAgainEl;
	
	private Date initialDate;
	private Date newDate;
	
	private FormLink dontApplyToAllLink;
	private FormLink applyToAllAfterCurrentDateLink;
	
	public MoveDateConfirmController(UserRequest ureq, WindowControl wControl, DateChooser sourceDateChooser) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		
		setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
		
		this.sourceDateChooser = sourceDateChooser;
		this.initialDate = sourceDateChooser.getInitialDate();
		this.newDate = sourceDateChooser.getDate();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] dateArguments = new String[3];
		dateArguments[0] = StringHelper.formatLocaleDate(initialDate.getTime(), getLocale());
		dateArguments[1] = StringHelper.formatLocaleDate(newDate.getTime(), getLocale());
		
		long difference = newDate.getTime() - initialDate.getTime();
		long differenceDays = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS);
		dateArguments[2] = String.valueOf(differenceDays);
		
		setFormDescription("dates.confirm.move.others", dateArguments);
		
		SelectionValue rememberMyChoice = new SelectionValue(REMEMBER_ME, translate("dates.do.not.ask.again"));
		SelectionValues dontAskAgainOptions = new SelectionValues(rememberMyChoice);
		
		dontAskAgainEl = uifactory.addCheckboxesHorizontal("dates.do.not.ask.again", "no.text", formLayout, dontAskAgainOptions.keys(), dontAskAgainOptions.values());
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("dates.update.all", buttonLayout);
		applyToAllAfterCurrentDateLink = uifactory.addFormLink("dates.update.after.current", buttonLayout, Link.BUTTON);
		applyToAllAfterCurrentDateLink.setI18nKey("dates.update.after.current", new String[] { dateArguments[0] });
		dontApplyToAllLink = uifactory.addFormLink("dates.update.none", buttonLayout, Link.BUTTON);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		MoveDatesEvent event = new MoveDatesEvent(sourceDateChooser, true, dontAskAgainEl.isKeySelected(REMEMBER_ME), false);
		fireEvent(ureq, event);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == dontApplyToAllLink) {
			MoveDatesEvent moveDatesEvent = new MoveDatesEvent(sourceDateChooser, false, dontAskAgainEl.isKeySelected(REMEMBER_ME), false);
			fireEvent(ureq, moveDatesEvent);
		} else if (source == applyToAllAfterCurrentDateLink) {
			MoveDatesEvent moveDatesEvent = new MoveDatesEvent(sourceDateChooser, true, dontAskAgainEl.isKeySelected(REMEMBER_ME), true);
			fireEvent(ureq, moveDatesEvent);
		}
	}

	@Override
	protected void doDispose() {
		
	}

}
