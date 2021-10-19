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
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.evaluation.ExceptionalObligationEvaluator;
import org.olat.course.learningpath.obligation.ExceptionalObligation;
import org.olat.course.learningpath.obligation.ExceptionalObligationHandler;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.ObligationEvaluator;
import org.olat.course.run.scoring.SingleUserObligationContext;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.ObligationOverridable;
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
	
	private final Formatter formatter;
	private StaticTextElement infoEl;
	private SingleSelection obligationEl;
	private FormLayoutContainer buttonLayout;
	private FormLink resetOverwriteLink;
	
	private final RepositoryEntry courseEntry;
	private final boolean canEdit;
	private final LearningPathConfigs learningPathConfigs;
	private final ObligationEvaluator obligationEvaluator;
	private final AssessmentEntry assessmentEntry;
	private final ObligationOverridable obligation;
	private final List<ExceptionalObligation> exceptionalObligations;
	private AssessmentObligation mostImportantExceptionalObligation;
	
	
	@Autowired
	private LearningPathService learningPathService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private UserManager userManager;

	public ObligationEditController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			CourseNode courseNode, UserCourseEnvironment userCourseEnv, boolean canEdit) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.courseEntry = courseEntry;
		this.canEdit = canEdit;
		ICourse course = CourseFactory.loadCourse(courseEntry);
		CourseEditorTreeNode editorTreeNode = course.getEditorTreeModel().getCourseEditorNodeById(courseNode.getIdent());
		this.learningPathConfigs = learningPathService.getConfigs(courseNode, editorTreeNode.getParent());
		this.obligationEvaluator = courseAssessmentService.getEvaluators(courseNode, course.getCourseConfig()).getObligationEvaluator();
		this.assessmentEntry = assessmentService.loadAssessmentEntry(
				userCourseEnv.getIdentityEnvironment().getIdentity(), courseEntry, courseNode.getIdent());
		this.obligation = assessmentEntry.getObligation();
		
		ExceptionalObligationEvaluator exceptionalObligationEvaluator = new ExceptionalObligationEvaluator(
				userCourseEnv.getIdentityEnvironment().getIdentity(),
				userCourseEnv.getCourseEnvironment().getRunStructure(), userCourseEnv.getScoreAccounting());
		exceptionalObligationEvaluator.setObligationContext(new SingleUserObligationContext());
		this.exceptionalObligations = exceptionalObligationEvaluator.filterExceptionalObligations(
				learningPathConfigs.getExceptionalObligations(),
				learningPathConfigs.getObligation());
		if (!exceptionalObligations.isEmpty()) {
			Set<AssessmentObligation> assessmentObligations = exceptionalObligations.stream()
					.map(ExceptionalObligation::getObligation)
					.collect(Collectors.toSet());
			mostImportantExceptionalObligation = obligationEvaluator
					.getMostImportantExceptionalObligation(assessmentObligations, learningPathConfigs.getObligation());
		}
		
		this.formatter = Formatter.getInstance(getLocale());
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Default obligation
		String translatedObligation = translateObligation(learningPathConfigs.getObligation());
		String defaultObligationText = translate("override.obligation.default", translatedObligation);
		uifactory.addStaticTextElement("default.obligation", null, defaultObligationText, formLayout);
		
		// Obligation exceptions
		if (!exceptionalObligations.isEmpty()) {
			String exeptionalObligationText = exceptionalObligations.stream()
					.filter(eo -> mostImportantExceptionalObligation == eo.getObligation())
					.map(this::getDisplayText)
					.filter(Objects::nonNull)
					.collect(Collectors.joining("<br>"));
			
			StaticTextElement exceptionalObligationEl = uifactory.addStaticTextElement("override.obligation.exceptions",
					exeptionalObligationText, formLayout);
			String exceptionalObligationLabel = translate("override.obligation.exceptions",
					translateObligation(mostImportantExceptionalObligation));
			exceptionalObligationEl.setLabel(exceptionalObligationLabel, null, false);
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
	
	private String translateObligation(AssessmentObligation obligation) {
		switch (obligation) {
		case mandatory: return translate("override.obligation.mandatory");
		case optional: return translate("override.obligation.optional");
		case excluded: return translate("override.obligation.excluded");
		case evaluated: return translate("override.obligation.evaluated");
		default: return "-";
		}
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
		// getCurrent() may be not evaluated at this point!
		AssessmentObligation current = obligation.getCurrentConfig() != null? obligation.getCurrentConfig(): obligation.getCurrent();
		if (overridden) {
			String[] args = new String[] {
					translateObligation(current),
					userManager.getUserDisplayName(obligation.getModBy()),
					formatter.formatDateAndTime(obligation.getModDate())
			};
			String infoText = translate("override.obligation.info", args);
			infoEl.setValue(infoText);
		}
		
		AssessmentObligation original = overridden ? obligation.getOriginal() : current;
		SelectionValues obligationKV = new SelectionValues();
		if ((!AssessmentObligation.mandatory.equals(original)) && learningPathConfigs.getAvailableObligations().contains(AssessmentObligation.mandatory)) {
			obligationKV.add(new SelectionValue(AssessmentObligation.mandatory.name(), translate("config.obligation.mandatory")));
		} if ((!AssessmentObligation.optional.equals(original)) && learningPathConfigs.getAvailableObligations().contains(AssessmentObligation.optional)) {
			obligationKV.add(new SelectionValue(AssessmentObligation.optional.name(), translate("config.obligation.optional")));
		} if ((!AssessmentObligation.excluded.equals(original)) && learningPathConfigs.getAvailableObligations().contains(AssessmentObligation.excluded)) {
			obligationKV.add(new SelectionValue(AssessmentObligation.excluded.name(), translate("config.obligation.excluded")));
		} if ((!AssessmentObligation.evaluated.equals(original))  && learningPathConfigs.getAvailableObligations().contains(AssessmentObligation.evaluated)) {
			obligationKV.add(new SelectionValue(AssessmentObligation.evaluated.name(), translate("config.obligation.evaluated")));
		}
		obligationEl.setKeysAndValues(obligationKV.keys(), obligationKV.values(), null);
		if (overridden && obligationEl.containsKey(current.name())) {
			obligationEl.select(current.name(), true);
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
