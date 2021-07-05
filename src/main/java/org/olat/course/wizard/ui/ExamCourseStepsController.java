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
package org.olat.course.wizard.ui;

import java.util.Collection;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.course.wizard.CourseWizardService;
import org.olat.course.wizard.provider.exam.ExamCourseSteps;

/**
 * 
 * Initial date: 11 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ExamCourseStepsController extends StepFormBasicController {
	
	private static final String KEY_RETEST = "retest";
	private static final String KEY_CERTIFICATE = "cert";
	private static final String KEY_DISCLAIMER = "disclaimer";
	private static final String KEY_COACHES = "coach";
	private static final String KEY_PARTICIPANTS = "paeticipants";
	
	private MultipleSelectionElement configEl;
	private MultipleSelectionElement membersEl;

	private final ExamCourseSteps parts;
	private final ExamCourseStepsListener partsListener;

	public ExamCourseStepsController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, ExamCourseSteps parts, ExamCourseStepsListener partsListener) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(CourseWizardService.class, getLocale(), getTranslator()));
		this.parts = parts;
		this.partsListener = partsListener;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("wizard.title.exam.steps");
		
		SelectionValues configKV = new SelectionValues();
		configKV.add(SelectionValues.entry(KEY_DISCLAIMER, translate("exam.disclaimer")));
		configKV.add(SelectionValues.entry(KEY_RETEST, translate("exam.retest")));
		configKV.add(SelectionValues.entry(KEY_CERTIFICATE, translate("exam.certificate")));
		configEl = uifactory.addCheckboxesVertical("exam.config", formLayout, configKV.keys(), configKV.values(), 1);
		configEl.select(KEY_RETEST, parts.isRetest());
		configEl.select(KEY_CERTIFICATE, parts.isCertificate());
		
		SelectionValues membersKV = new SelectionValues();
		membersKV.add(SelectionValues.entry(KEY_COACHES, translate("exam.coaches")));
		membersKV.add(SelectionValues.entry(KEY_PARTICIPANTS, translate("exam.participants")));
		membersEl = uifactory.addCheckboxesVertical("exam.members", formLayout, membersKV.keys(), membersKV.values(), 1);
		membersEl.select(KEY_RETEST, parts.isRetest());
		membersEl.select(KEY_CERTIFICATE, parts.isCertificate());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Collection<String> configKeys = configEl.getSelectedKeys();
		parts.setRetest(configKeys.contains(KEY_RETEST));
		parts.setCertificate(configKeys.contains(KEY_CERTIFICATE));
		parts.setDisclaimer(configKeys.contains(KEY_DISCLAIMER));
		
		Collection<String> memberKeys = membersEl.getSelectedKeys();
		parts.setCoaches(memberKeys.contains(KEY_COACHES));
		parts.setParticipants(memberKeys.contains(KEY_PARTICIPANTS));
		
		partsListener.onStepsChanged(ureq);
		
		fireEvent(ureq, StepsEvent.STEPS_CHANGED);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	public interface ExamCourseStepsListener {
		
		void onStepsChanged(UserRequest ureq);

	}

}
