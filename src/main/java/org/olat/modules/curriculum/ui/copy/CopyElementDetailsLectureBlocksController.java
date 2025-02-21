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
package org.olat.modules.curriculum.ui.copy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyResources;
import org.olat.modules.curriculum.ui.CurriculumComposerController;
import org.olat.modules.curriculum.ui.copy.CopyElementDetailsLectureBlocksTableModel.CopyLectureBlockCols;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CopyElementDetailsLectureBlocksController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private CopyElementDetailsLectureBlocksTableModel tableModel;

	private final CopyElementContext context;
	private final CurriculumElement curriculumElement;
	
	@Autowired
	private LectureService lectureService;
	
	public CopyElementDetailsLectureBlocksController(UserRequest ureq, WindowControl wControl, Form rootForm,
			CurriculumElement curriculumElement, CopyElementContext context) {
		super(ureq, wControl, LAYOUT_CUSTOM, "element_details_lectures_blocks", rootForm);
		setTranslator(Util.createPackageTranslator(CurriculumComposerController.class, getLocale(),
				Util.createPackageTranslator(RepositoryService.class, ureq.getLocale(), getTranslator())));
		this.context = context;
		this.curriculumElement = curriculumElement;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CopyLectureBlockCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyLectureBlockCols.activity,
				new CopySettingCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyLectureBlockCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CopyLectureBlockCols.externalId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyLectureBlockCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyLectureBlockCols.resource));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyLectureBlockCols.beginDate));

		tableModel = new CopyElementDetailsLectureBlocksTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "lecturesBlocksTable", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setEmptyTableSettings("empty.lectures.blocks", null, "o_icon_calendar", null, null, false);
	}
	
	private void loadModel() {
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setCurriculumElement(curriculumElement);
		List<LectureBlock> blocks = lectureService.getLectureBlocks(curriculumElement, true);
		List<CopyElementDetailsLectureBlocksRow> rows = new ArrayList<>(blocks.size());
		for(LectureBlock lectureBlock:blocks) {
			CopyResources copySetting = lectureBlock.getEntry() != null && lectureBlock.getEntry().getKey() != null
					? context.getCoursesEventsCopySetting()
					: (context.isStandaloneEventsCopySetting() ? CopyResources.resource : CopyResources.dont);
			String externalRef = context.evaluateIdentifier(lectureBlock.getExternalRef());
			Date startDate = context.shiftDate(lectureBlock.getStartDate());
			
			
			rows.add(new CopyElementDetailsLectureBlocksRow(lectureBlock, copySetting, startDate, externalRef));
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
