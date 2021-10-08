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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.bulk.PassedCellRenderer;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.learningpath.ui.CurriculumLearningPathRepositoryDataModel.LearningPathRepositoryCols;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.assessment.AssessmentEntryScoring;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.olat.modules.assessment.ui.component.LearningProgressCompletionCellRenderer;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumLearningPathRepositoryListController extends FormBasicController {
	
	private static final String ORES_TYPE_IDENTITY = "Identity";
	private static final String CMD_OPEN_COURSE = "open.course";
	private static final String CMD_LEARNING_PATH = "learningPath";
	
	private FlexiTableElement tableEl;
	private CurriculumLearningPathRepositoryDataModel dataModel;

	private LearningPathIdentityController currentIdentityCtrl;
	
	private final TooledStackedPanel stackPanel;
	private final CurriculumElement curriculumElement;
	private final Identity participant;

	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private AssessmentService assessmentService;

	public CurriculumLearningPathRepositoryListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			CurriculumElement curriculumElement, Identity participant) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.stackPanel = stackPanel;
		this.curriculumElement = curriculumElement;
		this.participant = participant;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathRepositoryCols.reponame, CMD_OPEN_COURSE));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathRepositoryCols.completion,
				new LearningProgressCompletionCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathRepositoryCols.passed, new PassedCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathRepositoryCols.score, new ScoreCellRenderer()));
		
		DefaultFlexiColumnModel learningPathColumn = new DefaultFlexiColumnModel(LearningPathRepositoryCols.learningPath);
		learningPathColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(learningPathColumn);
		
		dataModel = new CurriculumLearningPathRepositoryDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setEmptyTableSettings("table.empty.repository", null, "o_CourseModule_icon");
		tableEl.setExportEnabled(true);
		
		loadModel();
	}

	private void loadModel() {
		List<RepositoryEntry> repoEntries = curriculumService.getRepositoryEntriesOfParticipantWithDescendants(curriculumElement, participant);
		List<Long> entryKeys = repoEntries.stream()
				.map(RepositoryEntry::getKey)
				.collect(Collectors.toList());
		
		List<AssessmentEntryScoring> assessmentEntries = assessmentService.loadRootAssessmentEntriesByAssessedIdentity(participant, entryKeys);
		Map<Long, AssessmentEntryScoring> identityKeyToCompletion = new HashMap<>();
		for (AssessmentEntryScoring assessmentEntry : assessmentEntries) {
			identityKeyToCompletion.put(assessmentEntry.getRepositoryEntryKey(), assessmentEntry);
		}
		
		List<CurriculumLearningPathRepositoryRow> rows = new ArrayList<>(repoEntries.size());
		for (RepositoryEntry repositoryEntry : repoEntries) {
			AssessmentEntryScoring assessmentEntry = identityKeyToCompletion.get(repositoryEntry.getKey());
			CurriculumLearningPathRepositoryRow row = new CurriculumLearningPathRepositoryRow(repositoryEntry, assessmentEntry);
			rows.add(row);
			forgeLinks(row);
		}
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	public void forgeLinks(CurriculumLearningPathRepositoryRow row) {
		RepositoryEntry entry = row.getRepositoryEntry();
		ICourse course = CourseFactory.loadCourse(entry);
		if (course != null && LearningPathNodeAccessProvider.TYPE.equals(course.getCourseConfig().getNodeAccessType().getType())) {
			FormLink learningPathLink = uifactory.addFormLink("lp_" + CodeHelper.getRAMUniqueID(), CMD_LEARNING_PATH, "", null, null, Link.NONTRANSLATED);
			learningPathLink.setIconLeftCSS("o_icon o_icon-lg o_icon_learning_path");
			learningPathLink.setUserObject(row);
			row.setLearningPathLink(learningPathLink);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (tableEl == source) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				CurriculumLearningPathRepositoryRow row = dataModel.getObject(se.getIndex());
				if (CMD_OPEN_COURSE.equals(cmd)) {
					doOpenCourse(ureq, row);
				}
			}
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if (CMD_LEARNING_PATH.equals(cmd)) {
				CurriculumLearningPathRepositoryRow row = (CurriculumLearningPathRepositoryRow)link.getUserObject();
				doOpenLearningPath(ureq, row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOpenCourse(UserRequest ureq, CurriculumLearningPathRepositoryRow row) {
		RepositoryEntry entry = row.getRepositoryEntry();
		if (entry == null) return;
		
		String businessPath = "[RepositoryEntry:" + entry.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

	private void doOpenLearningPath(UserRequest ureq, CurriculumLearningPathRepositoryRow row) {
		removeAsListenerAndDispose(currentIdentityCtrl);
		
		OLATResourceable identityOres = OresHelper.createOLATResourceableInstance(ORES_TYPE_IDENTITY, participant.getKey());
		WindowControl bwControl = addToHistory(ureq, identityOres, null);
		
		CourseEnvironment courseEnvironment = CourseFactory.loadCourse(row.getRepositoryEntry()).getCourseEnvironment();
		currentIdentityCtrl = new LearningPathIdentityController(ureq, bwControl, stackPanel, courseEnvironment, participant);
		listenTo(currentIdentityCtrl);
		String title = row.getRepositoryEntry().getDisplayname().length() > 30
				? row.getRepositoryEntry().getDisplayname().substring(0, 30) + "..."
				: row.getRepositoryEntry().getDisplayname();
		stackPanel.pushController(title, currentIdentityCtrl);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
