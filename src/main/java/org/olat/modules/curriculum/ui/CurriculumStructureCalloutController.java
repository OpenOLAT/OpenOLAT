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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.site.CurriculumElementTreeRowComparator;
import org.olat.modules.curriculum.ui.CurriculumComposerTableModel.ElementCols;
import org.olat.modules.curriculum.ui.component.CurriculumElementSmallCellRenderer;
import org.olat.modules.curriculum.ui.component.CurriculumStatusCellRenderer;
import org.olat.modules.curriculum.ui.event.SelectCurriculumElementRowEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumStructureCalloutController extends FormBasicController implements FlexiTableCssDelegate {
	
	private FlexiTableElement tableEl;
	private CurriculumComposerTableModel tableModel;
	
	private final boolean withRoot;
	private final CurriculumElement rootElement;
	private final CurriculumElement activeElement;
	
	@Autowired
	private CurriculumService curriculumService;
	
	public CurriculumStructureCalloutController(UserRequest ureq, WindowControl wControl,
			CurriculumElement rootElement, CurriculumElement activeElement, boolean withRoot) {
		super(ureq, wControl, "structure");
		this.withRoot = withRoot;
		this.rootElement = rootElement;
		this.activeElement = activeElement;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		TreeNodeFlexiCellRenderer treeNodeRenderer = new TreeNodeFlexiCellRenderer(new NameRenderer(), "select");
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.displayName, treeNodeRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.externalRef,
				new CurriculumElementSmallCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.status,
				new CurriculumStatusCellRenderer(getTranslator())));

		tableModel = new CurriculumComposerTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 5000, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_curriculum_structure o_table_reduced");
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setExportEnabled(false);
	}
	
	private class NameRenderer implements FlexiCellRenderer {

		@Override
		public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
				FlexiTableComponent source, URLBuilder ubu, Translator translator) {
			if(cellValue instanceof String str) {
				CurriculumElementRow elementRow = tableModel.getObject(row);
				if(activeElement != null && activeElement.equals(elementRow.getCurriculumElement())) {
					target.append("<i class='o_icon o_icon-fw o_icon_selected_dot' title=\"").append(translate("element.active"))
						  .append("\"> </i> <strong>").append(str).append("</strong>");
				} else {
					target.append(str);
				}
			}
		}
	}
	
	private void loadModel() {
		List<CurriculumElement> elements = curriculumService.getCurriculumElementsDescendants(rootElement);
		if(withRoot && rootElement != null && !elements.contains(rootElement)) {
			elements.add(rootElement);
		}
		
		List<CurriculumElementRow> rows = new ArrayList<>(elements.size());
		Map<Long, CurriculumElementRow> keyToRows = new HashMap<>();
		for(CurriculumElement element:elements) {
			CurriculumElementRow row = new CurriculumElementRow(element);
			row.setActive(activeElement != null && activeElement.equals(element));
			rows.add(row);
			keyToRows.put(element.getKey(), row);
		}
		//parent line
		for(CurriculumElementRow row:rows) {
			if(row.getParentKey() != null) {
				row.setParent(keyToRows.get(row.getParentKey()));
			}
		}
		Collections.sort(rows, new CurriculumElementTreeRowComparator(getLocale()));
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		CurriculumElementRow row = tableModel.getObject(pos);
		if(activeElement != null && activeElement.equals(row.getCurriculumElement())) {
			return "o_curriculum_element_active";
		}
		return null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				CurriculumElementRow row = tableModel.getObject(se.getIndex());
				fireEvent(ureq, new SelectCurriculumElementRowEvent(row));
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
