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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.editor.EditorMainController;
import org.olat.course.learningpath.ui.LearningPathNodeConfigController;
import org.olat.course.noderight.ui.NodeRightsController;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.repository.ui.author.copy.wizard.CopyCourseStepsStep;
import org.olat.repository.ui.author.copy.wizard.DateWithLabel;

/**
 * Initial date: 04.01.2022<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class MoveAllDatesController extends FormBasicController {

	private static final String DATE = "date";
	private static final String DAYS = "days";
	
	private SingleSelection shiftModeEl;
	private DateChooser newEarliestDateEl;
	private IntegerElement moveAmountEl;
	
	private final CopyCourseContext context;
	private final DateWithLabel earliestDate;
	private long currentDateDifference;
	
	public MoveAllDatesController(UserRequest ureq, WindowControl wControl, CopyCourseContext context, DateWithLabel earliestDate) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(EditorMainController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(LearningPathNodeConfigController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(CourseNode.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(NodeRightsController.class, getLocale(), getTranslator()));
		this.context = context;
		this.earliestDate = earliestDate;
		
		initForm(ureq);
		switchMode();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValue date = new SelectionValue(DATE, translate("shift.date"));
		SelectionValue days = new SelectionValue(DAYS, translate("shift.days"));
		SelectionValues options = new SelectionValues(date, days);
		
		shiftModeEl = uifactory.addRadiosHorizontal("shift.to", formLayout, options.keys(), options.values());
		shiftModeEl.select(DATE, true);
		shiftModeEl.addActionListener(FormEvent.ONCHANGE);
		
		String earliestDateText = StringHelper.formatLocaleDate(earliestDate.getDate().getTime(), getLocale());
		earliestDateText += " - ";
		earliestDateText += earliestDate.getCourseNodeIdentifier() + " (" + earliestDate.getLabel() + ")";
		
		uifactory.addStaticTextElement("shift.earliest", earliestDateText, formLayout);
		
		newEarliestDateEl = uifactory.addDateChooser("shift.new.date", earliestDate.getDate(), formLayout);
		newEarliestDateEl.setInitialDate(newEarliestDateEl.getDate());
		
		moveAmountEl = uifactory.addIntegerElement("dates.shift.days.label", 0, formLayout);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("butonLayout", getTranslator());
		buttonLayout.setRootForm(mainForm);
		formLayout.add(buttonLayout);
		
		uifactory.addFormSubmitButton("shift.all.dates", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == shiftModeEl) {
			switchMode();
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (shiftModeEl.getSelectedKey().equals(DATE)) {
			long countDays = DateUtils.countDays(earliestDate.getDate(), newEarliestDateEl.getDate());
			currentDateDifference = 86400000L * countDays;
		} else {
			currentDateDifference = 86400000L * moveAmountEl.getIntValue();
		}
		context.setDateDifference(context.getDateDifference() + currentDateDifference);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void switchMode() {
		if (shiftModeEl.isKeySelected(DATE)) {
			newEarliestDateEl.setVisible(true);
			moveAmountEl.setVisible(false);
		} else {
			newEarliestDateEl.setVisible(false);
			moveAmountEl.setVisible(true);
		}
	}

	public long getCurrentDateDifference() {
		return currentDateDifference;
	}
	
}
