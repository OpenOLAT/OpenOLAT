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
package org.olat.course.editor.overview;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.ICourse;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.editor.EditorMainController;
import org.olat.course.editor.overview.OverviewDataModel.OverviewCols;
import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeNode;

/**
 * 
 * Initial date: 16 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OverviewListController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private OverviewDataModel dataModel;

	private final ICourse course;

	public OverviewListController(UserRequest ureq, WindowControl wControl, ICourse course) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(EditorMainController.class, getLocale(), getTranslator()));
		this.course = course;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initTable(ureq, formLayout);
	}

	private void initTable(UserRequest ureq, FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		IndentedNodeRenderer intendedNodeRenderer = new IndentedNodeRenderer();
		intendedNodeRenderer.setIndentationEnabled(false);
		FlexiCellRenderer nodeRenderer = new TreeNodeFlexiCellRenderer(intendedNodeRenderer);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.node, nodeRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.shortTitle));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.longTitle));
		DefaultFlexiColumnModel learningObjectivesModel = new DefaultFlexiColumnModel(OverviewCols.learningObjectives);
		learningObjectivesModel.setCellRenderer(new TextFlexiCellRenderer(EscapeMode.none));
		learningObjectivesModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(learningObjectivesModel);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewCols.display));
		
		dataModel = new OverviewDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 250, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "course-editor-overview");
		tableEl.setEmtpyTableMessageKey("table.empty");
		tableEl.setBordered(true);
		
		loadModel();
	}

	private void loadModel() {
		TreeNode rootNode = course.getEditorTreeModel().getRootNode();
		List<OverviewRow> rows = new ArrayList<>();
		forgeRows(rows, rootNode, 0, null);
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private void forgeRows(List<OverviewRow> rows, INode node, int recursionLevel, OverviewRow parent) {
		if (node instanceof CourseEditorTreeNode) {
			CourseEditorTreeNode editorNode = (CourseEditorTreeNode)node;
			OverviewRow row = forgeRow(editorNode, recursionLevel, parent);
			rows.add(row);
			
			int childCount = editorNode.getChildCount();
			for (int i = 0; i < childCount; i++) {
				INode child = editorNode.getChildAt(i);
				forgeRows(rows, child, ++recursionLevel, row);
			}
		}
	}

	private OverviewRow forgeRow(CourseEditorTreeNode editorNode, int recursionLevel, OverviewRow parent) {
		CourseNode courseNode = editorNode.getCourseNode();
		OverviewRow row = new OverviewRow(courseNode, recursionLevel);
		row.setTranslatedDisplayOption(getTranslatedDisplayOption(courseNode));
		row.setParent(parent);
		return row;
	}

	private String getTranslatedDisplayOption(CourseNode courseNode) {
		String displayOption = courseNode.getDisplayOption();
		if (displayOption == null) return null;
		
		switch(displayOption) {
		case CourseNode.DISPLAY_OPTS_SHORT_TITLE_DESCRIPTION_CONTENT: return translate("nodeConfigForm.short_title_desc_content");
		case CourseNode.DISPLAY_OPTS_TITLE_DESCRIPTION_CONTENT: return translate("nodeConfigForm.title_desc_content");
		case CourseNode.DISPLAY_OPTS_SHORT_TITLE_CONTENT: return translate("nodeConfigForm.short_title_content");
		case CourseNode.DISPLAY_OPTS_TITLE_CONTENT: return translate("nodeConfigForm.title_content");
		case CourseNode.DISPLAY_OPTS_CONTENT: return translate("nodeConfigForm.content_only");
		default:
			// nothing
		}
		return null;
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
