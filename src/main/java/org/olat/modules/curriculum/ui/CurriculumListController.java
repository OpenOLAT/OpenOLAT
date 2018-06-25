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
package org.olat.modules.curriculum.ui;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumManagerDataModel.CurriculumCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is a list of curriculum for the ssite "My courses" for
 * participants.
 * 
 * Initial date: 11 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumListController extends FormBasicController implements Activateable2 {
	
	private FlexiTableElement tableEl;
	private CurriculumManagerDataModel tableModel;
	
	private final BreadcrumbedStackedPanel stackPanel;
	
	private CurriculumElementListController elementlistCtrl;
	
	@Autowired
	private CurriculumService curriculumService;
	
	public CurriculumListController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel) {
		super(ureq, wControl, "curriculum_list");
		this.stackPanel = stackPanel;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CurriculumCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.displayName, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.identifier, "select"));
		DefaultFlexiColumnModel toolsCol = new DefaultFlexiColumnModel(CurriculumCols.tools);
		toolsCol.setExportable(false);
		toolsCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(toolsCol);
		
		tableModel = new CurriculumManagerDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setEmtpyTableMessageKey("table.curriculum.empty");
		tableEl.setAndLoadPersistedPreferences(ureq, "cur-curriculum-list");
	}
	
	private void loadModel() {
		List<Curriculum> curriculums = curriculumService.getMyCurriculums(getIdentity());
		List<CurriculumRow> rows = curriculums.stream()
				.map(curriculum -> new CurriculumRow(curriculum)).collect(Collectors.toList());
		tableModel.setObjects(rows);
		tableEl.reset(false, false, true);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			if(tableModel.getRowCount() == 1) {
				CurriculumRow row = tableModel.getObject(0);
				if(elementlistCtrl == null || !elementlistCtrl.getCurriculum().getKey().equals(row.getKey())) {
					doSelectCurriculum(ureq, row);
				}
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("select".equals(cmd)) {
					CurriculumRow row = tableModel.getObject(se.getIndex());
					doSelectCurriculum(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSelectCurriculum(UserRequest ureq, CurriculumRow row) {
		stackPanel.popUpToController(this);
	
		elementlistCtrl = new CurriculumElementListController(ureq, getWindowControl(), row);
		listenTo(elementlistCtrl);
		stackPanel.pushController(row.getDisplayName(), elementlistCtrl);
	}

}
