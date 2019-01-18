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
package org.olat.ims.qti21.ui.editor.overview;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.DoubleAdder;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.ui.assessment.components.QuestionTypeFlexiCellRenderer;
import org.olat.ims.qti21.ui.editor.AssessmentTestComposerController;
import org.olat.ims.qti21.ui.editor.events.SelectEvent;
import org.olat.ims.qti21.ui.editor.events.SelectEvent.SelectionTarget;
import org.olat.ims.qti21.ui.editor.overview.AssessmentTestOverviewDataModel.PartCols;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.SectionPart;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.node.test.TimeLimits;
import uk.ac.ed.ph.jqtiplus.provision.BadResourceException;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

/**
 * 
 * Initial date: 15 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestOverviewConfigurationController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private AssessmentTestOverviewDataModel tableModel;
	private final TooledStackedPanel toolbar;
	
	private final RepositoryEntry testEntry;
	private final ResolvedAssessmentTest resolvedAssessmentTest;
	
	@Autowired
	private UserManager userManager;
	
	public AssessmentTestOverviewConfigurationController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar,
			RepositoryEntry testEntry, ResolvedAssessmentTest resolvedAssessmentTest) {
		super(ureq, wControl, "overview", Util.createPackageTranslator(AssessmentTestComposerController.class, ureq.getLocale()));
		this.testEntry = testEntry;
		this.resolvedAssessmentTest = resolvedAssessmentTest;
		this.toolbar = toolbar;
		initForm(ureq);
		initialPanel.setCssClass("o_edit_mode");
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initInfosForm(formLayout);
		initTableForm(formLayout, ureq);
	}
	
	private void initInfosForm(FormItemContainer formLayout) {
		FormLayoutContainer infosLayout = FormLayoutContainer.createDefaultFormLayout("test.infos", getTranslator());
		formLayout.add("infos", infosLayout);
		infosLayout.setRootForm(mainForm);
		
		AssessmentTest assessmentTest = resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful();
		
		String title = assessmentTest.getTitle();
		TextElement titleEl = uifactory.addTextElement("title", "form.metadata.title", 255, title, infosLayout);
		titleEl.setEnabled(false);
		
		String initalAuthor = testEntry.getInitialAuthor() == null ? "-" : testEntry.getInitialAuthor();
		if(testEntry.getInitialAuthor() != null) {
			initalAuthor = userManager.getUserDisplayName(initalAuthor);
		}
		TextElement creatorEl = uifactory.addTextElement("initialAuthor", "form.metadata.initialAuthor", 255, StringHelper.escapeHtml(initalAuthor), infosLayout);
		creatorEl.setEnabled(false);
		DateChooser creationEl = uifactory.addDateChooser("form.metadata.creationDate", testEntry.getCreationDate(), infosLayout);
		creationEl.setEnabled(false);
		Double maxScore = QtiNodesExtractor.extractMaxScore(assessmentTest);
		String maxScoreStr = AssessmentHelper.getRoundedScore(maxScore);
		TextElement maxScoreEl = uifactory.addTextElement("max.score", "max.score", 255, maxScoreStr, infosLayout);
		maxScoreEl.setEnabled(false);
		Double cutValue = QtiNodesExtractor.extractCutValue(assessmentTest);
		String cutValueStr = AssessmentHelper.getRoundedScore(cutValue);
		TextElement cutValueEl = uifactory.addTextElement("cut.value", "cut.value", 255, cutValueStr, infosLayout);
		cutValueEl.setEnabled(false);
		
		TimeLimits timeLimits = assessmentTest.getTimeLimits();
		
		long maxInSeconds = -1;
		String timeMaxHour = "";
		String timeMaxMinute = "";
		if(timeLimits != null && timeLimits.getMaximum() != null && timeLimits.getMaximum().longValue() > 0) {
			maxInSeconds = timeLimits.getMaximum().longValue();
			timeMaxHour = Long.toString(maxInSeconds / 3600);
			timeMaxMinute = Long.toString((maxInSeconds % 3600) / 60);
		}
		
		if(StringHelper.containsNonWhitespace(timeMaxHour) || StringHelper.containsNonWhitespace(timeMaxMinute)) {
			String page = velocity_root + "/max_time_limit.html";
			FormLayoutContainer maxTimeCont = FormLayoutContainer.createCustomFormLayout("time.limit.cont", getTranslator(), page);
			infosLayout.add(maxTimeCont);
			maxTimeCont.setLabel("time.limit.max", null);
			
			timeMaxHour = timeMaxHour.equals("0") ? "" : timeMaxHour;
			TextElement maxTimeHourEl = uifactory.addTextElement("time.limit.hour", "time.limit.max", 4, timeMaxHour, maxTimeCont);
			maxTimeHourEl.setDomReplacementWrapperRequired(false);
			maxTimeHourEl.setDisplaySize(4);
			maxTimeHourEl.setEnabled(false);
			
			TextElement maxTimeMinuteEl = uifactory.addTextElement("time.limit.minute", "time.limit.max", 4, timeMaxMinute, maxTimeCont);
			maxTimeMinuteEl.setDomReplacementWrapperRequired(false);
			maxTimeMinuteEl.setDisplaySize(4);
			maxTimeMinuteEl.setEnabled(false);
		}
	}
	
	private void initTableForm(FormItemContainer formLayout, UserRequest ureq) {	
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		// title
		DefaultFlexiColumnModel titleCol = new DefaultFlexiColumnModel(PartCols.title, SelectionTarget.description.name(), new HierarchicalPartCellRenderer());
		titleCol.setAlwaysVisible(true);
		tableColumnModel.addFlexiColumnModel(titleCol);
		// score
		DefaultFlexiColumnModel scoreCol = new DefaultFlexiColumnModel(PartCols.maxScore, SelectionTarget.maxpoints.name());
		scoreCol.setCellRenderer(new AssessmentSectionScoreCellRenderer(SelectionTarget.maxpoints.name()));
		tableColumnModel.addFlexiColumnModel(scoreCol);
		// max attempts
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PartCols.attempts,
				SelectionTarget.attempts.name(), new MaxAttemptsCellRenderer(getTranslator())));
		// skipping allowed
		DefaultFlexiColumnModel skippingCol = new DefaultFlexiColumnModel(PartCols.skipping, SelectionTarget.expert.name());
		skippingCol.setCellRenderer(new TestAndSectionCellRenderer(SelectionTarget.expert.name(), new OptionCellRenderer(getTranslator())));
		tableColumnModel.addFlexiColumnModel(skippingCol);
		// comment allowed
		DefaultFlexiColumnModel commentCol = new DefaultFlexiColumnModel(PartCols.comment, SelectionTarget.expert.name());
		commentCol.setCellRenderer(new TestAndSectionCellRenderer(SelectionTarget.expert.name(), new OptionCellRenderer(getTranslator())));
		tableColumnModel.addFlexiColumnModel(commentCol);
		// review allowed
		DefaultFlexiColumnModel reviewCol = new DefaultFlexiColumnModel(PartCols.review, SelectionTarget.expert.name());
		reviewCol.setCellRenderer(new TestAndSectionCellRenderer(SelectionTarget.expert.name(), new OptionCellRenderer(getTranslator())));
		tableColumnModel.addFlexiColumnModel(reviewCol);
		// solution
		DefaultFlexiColumnModel solutionCol = new DefaultFlexiColumnModel(PartCols.solution, SelectionTarget.expert.name());
		solutionCol.setCellRenderer(new TestAndSectionCellRenderer(SelectionTarget.expert.name(), new OptionCellRenderer(getTranslator())));
		tableColumnModel.addFlexiColumnModel(solutionCol);
		// type
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, PartCols.type, new QuestionTypeFlexiCellRenderer(getTranslator())));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, PartCols.identifier));
		DefaultFlexiColumnModel feedbackCol = new DefaultFlexiColumnModel(PartCols.feedback, SelectionTarget.feedback.name(), new YesNoCellRenderer(getTranslator()));
		feedbackCol.setDefaultVisible(false);
		tableColumnModel.addFlexiColumnModel(feedbackCol);

		tableModel = new AssessmentTestOverviewDataModel(tableColumnModel);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "overview", tableModel, 32000, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_sel_qti_configuration_overview");
		tableEl.setExportEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "assessment-test-config-overview");
	}

	@Override
	protected void doDispose() {
		toolbar.setCssClass("");
	}
	
	private void loadModel() {
		List<ControlObjectRow> rows = new ArrayList<>();
		
		AssessmentTest test = resolvedAssessmentTest.getTestLookup().getRootNodeHolder().getRootNode();
		rows.add(ControlObjectRow.valueOf(test));
		
		List<TestPart> parts = test.getTestParts();
		if(parts.size() == 1) {
			List<AssessmentSection> sections = parts.get(0).getAssessmentSections();
			for(AssessmentSection section:sections) {
				loadModel(section, rows);
			}
		} else {
			for(int i=0; i<parts.size(); i++) {
				loadModel(parts.get(i), (i+1), rows);
			}
		}

		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private void loadModel(TestPart part, int pos, List<ControlObjectRow> rows) {
		ControlObjectRow partRow = ControlObjectRow.valueOf(part, pos);
		rows.add(partRow);

		boolean someMaxScore = false;
		DoubleAdder atomicMaxScore = new DoubleAdder();
		
		List<AssessmentSection> sections = part.getAssessmentSections();
		for(AssessmentSection section:sections) {
			Double maxScore = loadModel(section, rows);
			if(maxScore != null) {
				someMaxScore = true;
				atomicMaxScore.add(maxScore.doubleValue());
			}
		}
		
		if(someMaxScore) {
			partRow.setMaxScore(Double.valueOf(atomicMaxScore.sum()));
		}
	}
	
	private Double loadModel(AssessmentSection section, List<ControlObjectRow> rows) {
		ControlObjectRow sectionRow = ControlObjectRow.valueOf(section);
		rows.add(sectionRow);
		
		boolean someMaxScore = false;
		DoubleAdder atomicMaxScore = new DoubleAdder();
		for(SectionPart part: section.getSectionParts()) {
			
			Double maxScore = null;
			if(part instanceof AssessmentItemRef) {
				maxScore = loadModel((AssessmentItemRef)part, rows);
			} else if(part instanceof AssessmentSection) {
				maxScore = loadModel((AssessmentSection) part, rows);
			}
			
			if(maxScore != null) {
				someMaxScore = true;
				atomicMaxScore.add(maxScore.doubleValue());
			}
		}
		
		if(someMaxScore) {
			sectionRow.setMaxScore(Double.valueOf(atomicMaxScore.sum()));
		}
		return sectionRow.getMaxScore();
	}
	
	private Double loadModel(AssessmentItemRef itemRef, List<ControlObjectRow> rows) {
		Double maxScore = null;
		ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
		if(resolvedAssessmentItem == null || resolvedAssessmentItem.getItemLookup() == null
				|| resolvedAssessmentItem.getItemLookup().getRootNodeHolder() == null) {
			rows.add(ControlObjectRow.errorOf(itemRef));
		} else {
			BadResourceException ex = resolvedAssessmentItem.getItemLookup().getBadResourceException();
			if(ex != null) {
				rows.add(ControlObjectRow.errorOf(itemRef));
			} else {
				AssessmentItem assessmentItem = resolvedAssessmentItem.getItemLookup().getRootNodeHolder().getRootNode();
				ControlObjectRow row = ControlObjectRow.valueOf(itemRef, assessmentItem);
				maxScore = row.getMaxScore();
				rows.add(row);
				
			}
		}
		return maxScore;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if(StringHelper.containsNonWhitespace(se.getCommand())) {
					SelectionTarget target = SelectionTarget.valueOf(se.getCommand());
					ControlObjectRow row = tableModel.getObject(se.getIndex());
					fireEvent(ureq, new SelectEvent(row.getControlObject(), target));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private static class TestAndSectionCellRenderer implements FlexiCellRenderer {
		
		private final FlexiCellRenderer delegate;
		private final StaticFlexiCellRenderer actionRenderer;
		
		public TestAndSectionCellRenderer(String action, FlexiCellRenderer delegate) {
			this.delegate = delegate;
			actionRenderer = new StaticFlexiCellRenderer(action, delegate);
		}

		@Override
		public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
				FlexiTableComponent source, URLBuilder ubu, Translator translator) {
			ControlObjectRow object = (ControlObjectRow)source.getFlexiTableElement().getTableDataModel().getObject(row);
			if(object.getControlObject() instanceof AssessmentItemRef) {
				delegate.render(renderer, target, cellValue, row, source, ubu, translator);
			} else {
				actionRenderer.render(renderer, target, cellValue, row, source, ubu, translator);
			}	
		}
	}
}
