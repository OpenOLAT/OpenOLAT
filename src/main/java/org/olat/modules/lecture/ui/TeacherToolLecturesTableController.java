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
package org.olat.modules.lecture.ui;

import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TimeFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.lecture.model.LectureBlockRow;
import org.olat.modules.lecture.ui.TeacherOverviewDataModel.TeachCols;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer;

/**
 * 
 * Initial date: 13 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeacherToolLecturesTableController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private TeacherOverviewDataModel tableModel;
	
	private final String emptyI18nKey;
	
	public TeacherToolLecturesTableController(UserRequest ureq, WindowControl wControl, String emptyI18nKey) {
		super(ureq, wControl, "teacher_view_table");
		this.emptyI18nKey = emptyI18nKey;
		initForm(ureq);
	}
	
	public int getRowCount() {
		return tableModel.getRowCount();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.date, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.startTime, new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.endTime, new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.entry, "open.course"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.lectureBlock));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.location));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.status, new LectureBlockStatusCellRenderer(getTranslator())));
		
		tableModel = new TeacherOverviewDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(TeachCols.date.name(), false));
		tableEl.setSortSettings(sortOptions);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setEmtpyTableMessageKey(emptyI18nKey);
		//TODO absence tableEl.setAndLoadPersistedPreferences(ureq, "lecture-teacher-overview");
	}
	
	protected void loadModel(List<LectureBlockRow> blocks) {
		tableModel.setObjects(blocks);
		tableEl.reset(true, true, true);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				LectureBlockRow row = tableModel.getObject(se.getIndex());
				if("open.course".equals(cmd)) {
					doOpenCourse(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenCourse(UserRequest ureq, LectureBlockRow row) {
		Long repoKey = row.getLectureBlock().getEntry().getKey();
		String businessPath = "[RepositoryEntry:" + repoKey + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
}
