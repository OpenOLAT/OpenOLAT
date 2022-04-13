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
package org.olat.modules.grade.ui.wizard;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.olat.modules.grade.Breakpoint;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.modules.grade.ui.wizard.GradeChangeTableModel.GradeChangeCols;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 Mar 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractGradeListController extends StepFormBasicController {
	
	private final List<UserPropertyHandler> userPropertyHandlers;
	protected GradeChangeTableModel dataModel;
	protected FlexiTableElement tableEl;
	
	private final RepositoryEntry courseEntry;
	private final CourseNode courseNode;
	private final GradeScale gradeScale;
	private final List<Breakpoint> breakpoints;

	@Autowired
	private GradeService gradeService;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	protected BaseSecurity securityManager;

	public AbstractGradeListController(UserRequest ureq, WindowControl wControl, Form form, StepsRunContext runContext) {
		super(ureq, wControl, form, runContext, LAYOUT_BAREBONE, null);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		setTranslator(Util.createPackageTranslator(GradeUIFactory.class, getLocale(), getTranslator()));
		
		this.courseEntry = (RepositoryEntry)getFromRunContext(GradeScaleAdjustCallback.KEY_COURSE_ENTRY);
		this.courseNode = (CourseNode)getFromRunContext(GradeScaleAdjustCallback.KEY_COURSE_NODE);
		
		gradeScale = (GradeScale)getFromRunContext(GradeScaleAdjustCallback.KEY_GRADE_SCALE);
		breakpoints = getListFromRunContext(GradeScaleAdjustCallback.KEY_BREAKPOINTS, Breakpoint.class);
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(GradeChangeTableModel.USAGE_IDENTIFIER, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}
	
	protected abstract boolean isShowCurrentGrade();
	protected abstract boolean isMultiSelect();
	protected abstract boolean filter(AssessmentEntry assessmentEntry);

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int colIndex = GradeChangeTableModel.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(GradeChangeTableModel.USAGE_IDENTIFIER, userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, null, true, "userProp-" + colIndex));
			if(!options.hasDefaultOrderBy()) {
				options.setDefaultOrderBy(new SortKey("userProp-" + colIndex, true));
			}
			colIndex++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GradeChangeCols.score, new ScoreCellRenderer()));
		if (isShowCurrentGrade()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GradeChangeCols.grade, new TextFlexiCellRenderer(EscapeMode.none)));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GradeChangeCols.newGrade));
		
		dataModel = new GradeChangeTableModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setSortSettings(options);
		tableEl.setMultiSelect(isMultiSelect());
		tableEl.setSelectAllEnable(isMultiSelect());
	}

	private void loadModel() {
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
		NavigableSet<GradeScoreRange> gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale.getGradeSystem(),
				breakpoints, BigDecimal.valueOf(assessmentConfig.getMinScore().doubleValue()),
				BigDecimal.valueOf(assessmentConfig.getMaxScore().doubleValue()), getLocale());

		List<AssessmentEntry> assessmentEntries = assessmentService.loadAssessmentEntriesBySubIdentWithStatus(courseEntry, courseNode.getIdent(), null, false, false);
		List<GradeChangeRow> rows = new ArrayList<>(assessmentEntries.size());
		for (AssessmentEntry assessmentEntry : assessmentEntries) {
			if (filter(assessmentEntry)) {
				GradeChangeRow row = new GradeChangeRow(assessmentEntry.getIdentity(), userPropertyHandlers, getLocale());
				row.setScore(assessmentEntry.getScore());
				row.setGrade(GradeUIFactory.translatePerformanceClass(getTranslator(),
						assessmentEntry.getPerformanceClassIdent(), assessmentEntry.getGrade(),
						assessmentEntry.getGradeSystemIdent()));
				
				GradeScoreRange range = gradeService.getGradeScoreRange(gradeScoreRanges, Float.valueOf(assessmentEntry.getScore().floatValue()));
				row.setNewGrade(range.getGrade());
				
				rows.add(row);
			}
		}
		
		dataModel.setObjects(rows);
		tableEl.reset();
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

}
