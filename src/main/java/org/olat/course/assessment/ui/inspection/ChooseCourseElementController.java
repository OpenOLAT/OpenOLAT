/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.inspection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.assessment.ui.inspection.CourseElementTableModel.ElementsCols;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.IQTESTCourseNode;

/**
 * 
 * Initial date: 18 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChooseCourseElementController extends StepFormBasicController {

	private FlexiTableElement tableEl;
	private CourseElementTableModel tableModel;
	
	private final CreateInspectionContext context;
	
	public ChooseCourseElementController(UserRequest ureq, WindowControl wControl,
			CreateInspectionContext context, StepsRunContext runContext, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "select_node");
		this.context = context;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		IndentedNodeRenderer intendedNodeRenderer = new IndentedNodeRenderer();
		intendedNodeRenderer.setIndentationEnabled(false);
		FlexiCellRenderer nodeRenderer = new TreeNodeFlexiCellRenderer(intendedNodeRenderer, "open");
		DefaultFlexiColumnModel nodeModel = new DefaultFlexiColumnModel(ElementsCols.node, "open", nodeRenderer);
		nodeModel.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(nodeModel);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementsCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementsCols.longTitle));
			
		Translator trans = Util.createPackageTranslator(CourseNodeConfiguration.class, getLocale(), getTranslator());
		tableModel = new CourseElementTableModel(columnsModel, trans);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "elements", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setSelection(true, false, false);
		tableEl.setCustomizeColumns(true);
	}
	
	private void loadModel() {
		ICourse course = CourseFactory.loadCourse(context.getCourseEntry());
		CourseNode rootNode = course.getRunStructure().getRootNode();

		List<CourseElementRow> rows = new ArrayList<>();
		loadModelRecursive(rootNode, null, 0, rows);
		filterModel(rows);
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private void loadModelRecursive(CourseNode courseNode, CourseElementRow parentRow, int recursionLevel, List<CourseElementRow> rows) {
		CourseElementRow row = new CourseElementRow(courseNode, parentRow);
		rows.add(row);
		
		int childCount = courseNode.getChildCount();
		for (int i = 0; i < childCount; i++) {
			INode child = courseNode.getChildAt(i);
			if(child instanceof CourseNode childNode) {
				loadModelRecursive(childNode, row, recursionLevel+1, rows);
			}
		}
	}
	
	private void filterModel(List<CourseElementRow> rows) {
		Set<FlexiTreeTableNode> toRetains = new HashSet<>();
		
		for(CourseElementRow row:rows) {
			if(row.getCourseNode() instanceof IQTESTCourseNode) {
				for(FlexiTreeTableNode aRow=row; aRow != null; aRow = aRow.getParent()) {
					toRetains.add(aRow);
				}
			}
		}

		if(!toRetains.isEmpty()) {
			rows.retainAll(toRetains);
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		tableEl.clearError();
		if(context.getCourseNode() == null && tableEl.getMultiSelectedIndex().isEmpty()) {
			tableEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		if(selectedIndexes.size() == 1) {
			int index = selectedIndexes.iterator().next().intValue();
			CourseElementRow row = tableModel.getObject(index);
			if(row != null) {
				context.setCourseNode(row.getCourseNode());
			}
		}
		
		if(context.getCourseNode() != null) {
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		} else {
			tableEl.setErrorKey("form.legende.mandatory");
		}
	}
}
