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
package org.olat.course.member;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.course.member.CourseCurriculumElementListTableModel.ElementsCols;
import org.olat.course.member.component.DefaultElementCellRenderer;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
import org.olat.modules.curriculum.model.CurriculumElementInfosSearchParams;
import org.olat.modules.curriculum.ui.CurriculumComposerController;
import org.olat.modules.curriculum.ui.component.CurriculumStatusCellRenderer;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CourseCurriculumElementListController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private CourseCurriculumElementListTableModel tableModel;
	
	private final RepositoryEntryRef entry;
	
	@Autowired
	private CurriculumService curriculumService;
	
	public CourseCurriculumElementListController(UserRequest ureq, WindowControl wControl, RepositoryEntryRef entry) {
		super(ureq, wControl, "course_elements", Util
				.createPackageTranslator(CurriculumComposerController.class, ureq.getLocale()));
		this.entry = entry;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ElementsCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementsCols.displayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementsCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ElementsCols.externalId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementsCols.curriculum));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementsCols.defaultElement,
				new DefaultElementCellRenderer(getTranslator())));
		DateFlexiCellRenderer dateCellRenderer = new DateFlexiCellRenderer(getLocale());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementsCols.beginDate, dateCellRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementsCols.endDate, dateCellRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementsCols.numOfOwners));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementsCols.numOfCoaches));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementsCols.numOfParticipants));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementsCols.status,
				new CurriculumStatusCellRenderer(getTranslator())));
		
		tableModel = new CourseCurriculumElementListTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, true, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setSearchEnabled(true);
		tableEl.setExportEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "course-curriculum-elements-v1");
	}
	
	protected void loadModel() {
		CurriculumElementInfosSearchParams searchParams = CurriculumElementInfosSearchParams.searchElementsOf(null, entry);
		List<CurriculumElementInfos> infos = curriculumService.getCurriculumElementsWithInfos(searchParams);
		CurriculumElement defaultElement = curriculumService.getDefaultCurriculumElement(entry);
		List<CourseCurriculumElementRow> rows = infos.stream()
				.map(info -> new CourseCurriculumElementRow(info.curriculumElement(), info.curriculum(),
						info.curriculumElement().equals(defaultElement),
						info.numOfParticipants(), info.numOfCoaches(), info.numOfOwners()))
				.toList();
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
