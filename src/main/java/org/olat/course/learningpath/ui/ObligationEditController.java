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
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.course.CourseFactory;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.evaluation.ConfigObligationEvaluator;
import org.olat.course.learningpath.obligation.ExceptionalObligation;
import org.olat.course.learningpath.obligation.ExceptionalObligationHandler;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.SingleUserObligationContext;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ObligationEditController extends FormBasicController implements Controller {
	
	private static final ConfigObligationEvaluator OBLIGATION_EVALUATOR = new ConfigObligationEvaluator();
	
	private final Formatter formatter;
	private StaticTextElement infoEl;
	private SingleSelection obligationEl;
	private FormLayoutContainer buttonLayout;
	private FormLink resetOverwriteLink;
	
	private final RepositoryEntry courseEntry;
	private final UserCourseEnvironment userCourseEnv;
	private final boolean canEdit;
	private final LearningPathConfigs learningPathConfigs;
	private final AssessmentEntry assessmentEntry;
	private final Overridable<AssessmentObligation> obligation;
	
	@Autowired
	private LearningPathService learningPathService;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private UserManager userManager;

	public ObligationEditController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			CourseNode courseNode, UserCourseEnvironment userCourseEnv, boolean canEdit) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.courseEntry = courseEntry;
		this.userCourseEnv = userCourseEnv;
		this.canEdit = canEdit;
		this.learningPathConfigs = learningPathService.getConfigs(courseNode);
		this.assessmentEntry = assessmentService.loadAssessmentEntry(
				userCourseEnv.getIdentityEnvironment().getIdentity(), courseEntry, courseNode.getIdent());
		this.obligation = assessmentEntry.getObligation();
		this.formatter = Formatter.getInstance(getLocale());
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Default obligation
		String defaultObligationText = null;
		switch (learningPathConfigs.getObligation()) {
		case mandatory:
			defaultObligationText = translate("override.obligation.default.mandatory");
			break;
		case optional:
			defaultObligationText = translate("override.obligation.default.optional");
			break;
		case excluded:
			defaultObligationText = translate("override.obligation.default.excluded");
			break;
		default:
			break;
		}
		uifactory.addStaticTextElement("default.obligation", null, defaultObligationText, formLayout);
		
		// Obligation exceptions
		List<ExceptionalObligation> exceptionalObligations = OBLIGATION_EVALUATOR.filterExceptionalObligations(
				userCourseEnv.getIdentityEnvironment().getIdentity(),
				CourseFactory.loadCourse(courseEntry).getRunStructure(),
				userCourseEnv.getScoreAccounting(), 
				learningPathConfigs.getExceptionalObligations(),
				learningPathConfigs.getObligation(),
				new SingleUserObligationContext());
		
		if (!exceptionalObligations.isEmpty()) {
			Set<AssessmentObligation> assessmentObligations = exceptionalObligations.stream()
					.map(ExceptionalObligation::getObligation)
					.collect(Collectors.toSet());
			AssessmentObligation mostImportantExceptionalObligation = OBLIGATION_EVALUATOR.getMostImportantExceptionalObligation(assessmentObligations, learningPathConfigs.getObligation());
			
			String exeptionalObligationText = exceptionalObligations.stream()
					.filter(eo -> mostImportantExceptionalObligation == eo.getObligation())
					.map(this::getDisplayText)
					.filter(Objects::nonNull)
					.collect(Collectors.joining("<br>"));
			
			String exceptionalObligationLabel = null;
			switch (mostImportantExceptionalObligation) {
			case mandatory:
				exceptionalObligationLabel = "override.obligation.exceptions.mandatory";
				break;
			case optional:
				exceptionalObligationLabel = "override.obligation.exceptions.optional";
				break;
			case excluded:
				exceptionalObligationLabel = "override.obligation.exceptions.excluded";
				break;
			default:
				break;
			}
			uifactory.addStaticTextElement(exceptionalObligationLabel, exeptionalObligationText, formLayout);
		}
		
		// Override
		obligationEl = uifactory.addRadiosVertical("override.obligation", formLayout, emptyStrings(), emptyStrings());
		obligationEl.setAllowNoSelection(true);
		obligationEl.addActionListener(FormEvent.ONCHANGE);
		
		infoEl = uifactory.addStaticTextElement("info", null, null, formLayout);
		
		buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		resetOverwriteLink = uifactory.addFormLink("override.reset", buttonLayout, Link.BUTTON);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	private String getDisplayText(ExceptionalObligation exceptionalObligation) {
		ExceptionalObligationHandler handler = learningPathService.getExceptionalObligationHandler(exceptionalObligation.getType());
		String text = null;
		if (handler != null) {
			text = handler.getDisplayText(getTranslator(), exceptionalObligation, courseEntry);
		}
		return text;
	}

	private void updateUI() {
		boolean overridden = obligation.isOverridden();
		if (overridden) {
			String[] args = new String[] {
					userManager.getUserDisplayName(obligation.getModBy()),
					formatter.formatDateAndTime(obligation.getModDate())
				};
			String infoText = null;
			switch (obligation.getCurrent()) {
			case mandatory:
				infoText = translate("override.obligation.mandatory.info", args);
				break;
			case optional:
				infoText = translate("override.obligation.optional.info", args);
				break;
			case excluded:
				infoText = translate("override.obligation.excluded.info", args);
				break;
			default:
				break;
			}
			infoEl.setValue(infoText);
		}
		
		AssessmentObligation original = overridden? obligation.getOriginal(): obligation.getCurrent();
		SelectionValues obligationKV = new SelectionValues();
		if (!AssessmentObligation.mandatory.equals(original)) {
			obligationKV.add(new SelectionValue(AssessmentObligation.mandatory.name(), translate("config.obligation.mandatory")));
		} if (!AssessmentObligation.optional.equals(original)) {
			obligationKV.add(new SelectionValue(AssessmentObligation.optional.name(), translate("config.obligation.optional")));
		} if (!AssessmentObligation.excluded.equals(original)) {
			obligationKV.add(new SelectionValue(AssessmentObligation.excluded.name(), translate("config.obligation.excluded")));
		}
		obligationEl.setKeysAndValues(obligationKV.keys(), obligationKV.values(), null);
		if (overridden && obligationEl.containsKey(obligation.getCurrent().name())) {
			obligationEl.select(obligation.getCurrent().name(), true);
		}
		
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
		boolean override = obligationEl.isOneSelected();
		if (override) {
			AssessmentObligation selectedObligation = AssessmentObligation.valueOf(obligationEl.getSelectedKey());
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
