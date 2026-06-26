/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.grade.ui.wizard;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.DefaultStepsRunContext;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.grade.Breakpoint;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.GradeSystem;
import org.olat.modules.grade.GradeSystemType;
import org.olat.modules.grade.PerformanceClass;
import org.olat.modules.grade.ui.GradeScaleVisualizationController;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.modules.grade.ui.PerformanceClassBreakpointRow;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Nov 18, 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class GradeApplyConfirmationController extends FormBasicController {

	private GradeApplayBulkListController gradeApplayListCtrl;
	private GradeScaleVisualizationController vizCtrl;

	private final StepsRunContext runContext;
	private final List<Identity> identities;
	private final RepositoryEntry courseEntry;
	private final CourseNode courseNode;
	private final GradeScale gradeScale;
	private final List<Breakpoint> breakpoints;

	@Autowired
	private GradeService gradeService;
	@Autowired
	private AssessmentService assessmentService;

	public GradeApplyConfirmationController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			CourseNode courseNode, GradeScale gradeScale, List<Breakpoint> breakpoints, List<Identity> identities) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.identities = identities;
		this.courseEntry = courseEntry;
		this.courseNode = courseNode;
		this.gradeScale = gradeScale;
		this.breakpoints = breakpoints;

		runContext = new DefaultStepsRunContext();
		runContext.put(GradeScaleAdjustCallback.KEY_COURSE_ENTRY, courseEntry);
		runContext.put(GradeScaleAdjustCallback.KEY_COURSE_NODE, courseNode);
		runContext.put(GradeScaleAdjustCallback.KEY_GRADE_SCALE, gradeScale);
		runContext.put(GradeScaleAdjustCallback.KEY_BREAKPOINTS, breakpoints);

		initForm(ureq);
	}

	public List<Identity> getIdentities() {
		return identities;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		GradeSystem gradeSystem = gradeScale.getGradeSystem();
		NavigableSet<GradeScoreRange> gradeScoreRanges = gradeService.getGradeScoreRanges(
				gradeSystem, breakpoints, gradeScale.getMinScore(), gradeScale.getMaxScore(), getLocale());
		Map<Integer, Long> scoreToCount = buildScoreToCount();

		vizCtrl = new GradeScaleVisualizationController(ureq, getWindowControl(), mainForm, true);
		listenTo(vizCtrl);
		formLayout.add("viz", vizCtrl.getInitialFormItem());
		if (GradeSystemType.numeric == gradeSystem.getType()) {
			vizCtrl.setNumericData(gradeSystem, breakpoints, gradeScoreRanges, scoreToCount);
			vizCtrl.setMode(true);
		} else {
			vizCtrl.setReadOnlyTextRows(buildReadOnlyTextRows(gradeSystem, gradeScoreRanges));
			vizCtrl.setTextData(gradeSystem, breakpoints, gradeScoreRanges, scoreToCount);
			vizCtrl.setMode(false);
		}

		Set<Long> identityKeys = identities.stream().map(Identity::getKey).collect(Collectors.toSet());
		gradeApplayListCtrl = new GradeApplayBulkListController(ureq, getWindowControl(), mainForm, runContext, identityKeys);
		listenTo(gradeApplayListCtrl);
		formLayout.add(gradeApplayListCtrl.getInitialFormItem());

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setElementCssClass("o_block_large_top o_button_group_right");
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("apply", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	private Map<Integer, Long> buildScoreToCount() {
		Set<Long> identityKeySet = identities.stream().map(Identity::getKey).collect(Collectors.toSet());
		List<AssessmentEntry> entries = assessmentService.loadAssessmentEntriesBySubIdentWithStatus(
				courseEntry, courseNode.getIdent(), null, false, false);
		Map<Integer, Long> result = new HashMap<>();
		for (AssessmentEntry entry : entries) {
			if (entry.getIdentity() != null && identityKeySet.contains(entry.getIdentity().getKey()) && entry.getScore() != null) {
				Integer score = Integer.valueOf(Math.round(entry.getScore().floatValue()));
				result.merge(score, Long.valueOf(1L), Long::sum);
			}
		}
		return result;
	}

	private List<PerformanceClassBreakpointRow> buildReadOnlyTextRows(GradeSystem gradeSystem,
			NavigableSet<GradeScoreRange> gradeScoreRanges) {
		List<PerformanceClass> performanceClasses = gradeService.getPerformanceClasses(gradeSystem);
		Collections.sort(performanceClasses);

		List<PerformanceClassBreakpointRow> rows = new ArrayList<>(performanceClasses.size());
		for (PerformanceClass performanceClass : performanceClasses) {
			PerformanceClassBreakpointRow row = new PerformanceClassBreakpointRow(performanceClass, null);
			String translatedName = GradeUIFactory.translatePerformanceClass(getTranslator(), performanceClass);
			row.setTranslatedName(translatedName);

			BigDecimal lowerBound = gradeScoreRanges.stream()
					.filter(r -> performanceClass.getIdentifier().equals(r.getPerformanceClassIdent()))
					.map(GradeScoreRange::getLowerBound)
					.min(BigDecimal::compareTo)
					.orElse(gradeScale.getMinScore());

			// lb_* TextElements must be registered in numericCont so the FlexiTable can
			// render them as form components; they are displayed in the text layout column.
			TextElement lowerBoundEl = uifactory.addTextElement(
					"lb_ro_" + performanceClass.getBestToLowest(), null, 10,
					lowerBound != null ? GradeUIFactory.THREE_DIGITS.format(lowerBound) : "",
					vizCtrl.getNumericCont());
			lowerBoundEl.setDisplaySize(10);
			lowerBoundEl.setEnabled(false);
			lowerBoundEl.setUserObject(row);
			row.setLowerBoundEl(lowerBoundEl);
			rows.add(row);
		}
		return rows;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	public static final class GradeApplayBulkListController extends AbstractGradeListController {
		
		private final Set<Long> identityKeys;

		public GradeApplayBulkListController(UserRequest ureq, WindowControl wControl, Form form,
				StepsRunContext runContext, Set<Long> identityKeys) {
			super(ureq, wControl, form, runContext);
			this.identityKeys = identityKeys;
			loadModel();
		}

		@Override
		protected boolean isShowCurrentValues() {
			return false;
		}

		@Override
		protected boolean isMultiSelect() {
			return false;
		}

		@Override
		protected boolean filter(AssessmentEntry assessmentEntry) {
			return assessmentEntry.getIdentity() != null && identityKeys.contains(assessmentEntry.getIdentity().getKey());
		}
		
	}

}
