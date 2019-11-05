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
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.nodes.INode;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.learningpath.manager.LearningPathCourseTreeModelBuilder;
import org.olat.course.learningpath.ui.LearningPathDataModel.LearningPathCols;
import org.olat.course.run.scoring.StatusCompletionEvaluator;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathListController extends FormBasicController {
	
	private static final StatusCompletionEvaluator STATUS_COMPLETION_EVALUATOR = new StatusCompletionEvaluator();

	private FlexiTableElement tableEl;
	private LearningPathDataModel dataModel;
	
	private final UserCourseEnvironment userCourseEnv;
	
	public LearningPathListController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.userCourseEnv = userCourseEnv;
		initForm(ureq);
	}
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		IndentedNodeRenderer intendedNodeRenderer = new IndentedNodeRenderer();
		intendedNodeRenderer.setIndentationEnabled(false);
		FlexiCellRenderer nodeRenderer = new TreeNodeFlexiCellRenderer(intendedNodeRenderer);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathCols.node, nodeRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathCols.obligation));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathCols.duration));
		DefaultFlexiColumnModel firstVisitColumnModel = new DefaultFlexiColumnModel(LearningPathCols.firstVisit);
		firstVisitColumnModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(firstVisitColumnModel);
		DefaultFlexiColumnModel lastVisitColumnModel = new DefaultFlexiColumnModel(LearningPathCols.lastVisit);
		lastVisitColumnModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(lastVisitColumnModel);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathCols.status));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathCols.fullyAssessedDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathCols.progress));

		dataModel = new LearningPathDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 250, false, getTranslator(), formLayout);
		tableEl.setEmtpyTableMessageKey("table.empty");
		tableEl.setBordered(true);
		tableEl.setNumOfRowsEnabled(false);
		
		loadModel();
	}

	void loadModel() {
		LearningPathCourseTreeModelBuilder learningPathCourseTreeModelBuilder = new LearningPathCourseTreeModelBuilder(userCourseEnv);
		GenericTreeModel learningPathTreeModel = learningPathCourseTreeModelBuilder.build();
		List<LearningPathRow> rows = forgeRows(learningPathTreeModel);
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private List<LearningPathRow> forgeRows(GenericTreeModel learningPathTreeModel) {
		List<LearningPathRow> rows = new ArrayList<>();
		TreeNode rootNode = learningPathTreeModel.getRootNode();
		forgeRowAndChildren(rows, rootNode, null);
		return rows;
	}

	private void forgeRowAndChildren(List<LearningPathRow> rows, INode iNode, LearningPathRow parent) {
		if (iNode instanceof LearningPathTreeNode) {
			LearningPathTreeNode learningPathNode = (LearningPathTreeNode) iNode;
			forgeRowAndChildren(rows, learningPathNode, parent);
		}
	}

	private void forgeRowAndChildren(List<LearningPathRow> rows, LearningPathTreeNode learningPathNode,
			LearningPathRow parent) {
		LearningPathRow row = forgeRow(learningPathNode, parent);
		rows.add(row);

		int childCount = learningPathNode.getChildCount();
		for (int childIndex = 0; childIndex < childCount; childIndex++) {
			INode child = learningPathNode.getChildAt(childIndex);
			forgeRowAndChildren(rows, child, row);
		}
	}

	private LearningPathRow forgeRow(LearningPathTreeNode treeNode, LearningPathRow parent) {
		ProgressBar progressBar = new ProgressBar("progress-" + treeNode.getIdent());
		progressBar.setMax(1.0f);
		progressBar.setWidthInPercent(true);
		progressBar.setPercentagesEnabled(true);
		progressBar.setLabelAlignment(LabelAlignment.none);
		float actual = getActual(treeNode);
		progressBar.setActual(actual);
		LearningPathRow row = new LearningPathRow(treeNode, progressBar);
		row.setParent(parent);
		return row;
	}

	private float getActual(LearningPathTreeNode treeNode) {
		float actual = 0;
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(treeNode.getCourseNode());
		boolean hasCompletion = !Mode.none.equals(assessmentConfig.getCompletionMode());
		if (hasCompletion) {
			actual = treeNode.getCompletion() != null ? treeNode.getCompletion().floatValue() : 0.0f;
		} else {
			Double statusCompletion = STATUS_COMPLETION_EVALUATOR.getCompletion(treeNode.getFullyAssessed(),
					treeNode.getAssessmentStatus());
			actual = statusCompletion != null? statusCompletion.floatValue(): 0;
			
		}
		return actual;
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
