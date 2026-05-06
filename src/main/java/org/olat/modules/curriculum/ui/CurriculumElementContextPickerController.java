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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateWithDayFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.CurriculumElementToDoProvider;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
import org.olat.modules.curriculum.model.CurriculumElementInfosSearchParams;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.site.CurriculumElementTreeRowComparator;
import org.olat.modules.curriculum.ui.CurriculumElementContextPickerTableModel.PickerCols;
import org.olat.modules.todo.ToDoContext;
import org.olat.modules.todo.ui.ToDoContextSelectedEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 4 May 2026<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class CurriculumElementContextPickerController extends FormBasicController {

	private FlexiTableElement tableEl;
	private CurriculumElementContextPickerTableModel tableModel;

	private final Long implementationKey;
	private final Long currentElementKey;

	@Autowired
	private CurriculumService curriculumService;

	public CurriculumElementContextPickerController(UserRequest ureq, WindowControl wControl,
			Long implementationKey, Long currentElementKey) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.implementationKey = implementationKey;
		this.currentElementKey = currentElementKey;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		DefaultFlexiColumnModel displayNameCol = new DefaultFlexiColumnModel(PickerCols.displayName);
		displayNameCol.setCellRenderer(new IndentedDisplayNameRenderer());
		columnsModel.addFlexiColumnModel(displayNameCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PickerCols.externalRef));
		DateWithDayFlexiCellRenderer dateRenderer = new DateWithDayFlexiCellRenderer(getLocale());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PickerCols.beginDate, dateRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PickerCols.endDate, dateRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PickerCols.numOfCourses));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PickerCols.numOfEvents));

		tableModel = new CurriculumElementContextPickerTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCustomizeColumns(false);
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("table.curriculum.element.empty")
				.withIconCss("o_icon_curriculum_element")
				.build());
		tableEl.setSelection(true, false, true);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setElementCssClass("o_button_group o_button_group_center");
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("select", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	private void loadModel() {
		CurriculumElement implementation = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(implementationKey));
		CurriculumElementInfosSearchParams params =
				CurriculumElementInfosSearchParams.searchDescendantsOf(null, implementation);
		List<CurriculumElementInfos> infos = curriculumService.getCurriculumElementsWithInfos(params);

		List<CurriculumElementContextPickerRow> rows = new ArrayList<>(infos.size());
		Map<Long, CurriculumElementContextPickerRow> keyToRow = new HashMap<>();
		for (CurriculumElementInfos info : infos) {
			CurriculumElementContextPickerRow row = new CurriculumElementContextPickerRow(info.curriculumElement(),
					info.numOfResources(), info.numOfLectureBlocks());
			rows.add(row);
			keyToRow.put(row.getKey(), row);
		}
		for (CurriculumElementContextPickerRow row : rows) {
			if (row.getParentKey() != null) {
				row.setParent(keyToRow.get(row.getParentKey()));
			}
		}
		Collections.sort(rows, new CurriculumElementTreeRowComparator(getLocale()));

		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);

		if (currentElementKey != null) {
			for (int i = 0; i < rows.size(); i++) {
				if (currentElementKey.equals(rows.get(i).getKey())) {
					tableEl.setMultiSelectedIndex(Set.of(i));
					break;
				}
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Set<Integer> selected = tableEl.getMultiSelectedIndex();
		if (selected.isEmpty()) {
			return;
		}
		CurriculumElementContextPickerRow row = tableModel.getObject(selected.iterator().next());
		CurriculumElement element = row.getCurriculumElement();
		ToDoContext ctx = ToDoContext.of(CurriculumElementToDoProvider.TYPE, element.getCurriculum().getKey(),
				element.getKey().toString(), element.getCurriculum().getDisplayName(), element.getDisplayName());
		fireEvent(ureq, new ToDoContextSelectedEvent(ctx));
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private static class IndentedDisplayNameRenderer implements FlexiCellRenderer {

		private static final String INDENT = "&nbsp;&nbsp;&nbsp;&nbsp;";

		@Override
		public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
				FlexiTableComponent source, URLBuilder ubu, Translator translator) {
			if (!(cellValue instanceof CurriculumElementContextPickerRow pickerRow)) {
				return;
			}
			int level = pickerRow.getRecursionLevel();
			for (int i = 0; i < level; i++) {
				target.append(INDENT);
			}
			target.append(StringHelper.escapeHtml(pickerRow.getDisplayName()));
		}
	}
}
