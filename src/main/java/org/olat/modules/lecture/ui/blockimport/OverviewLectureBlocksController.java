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
package org.olat.modules.lecture.ui.blockimport;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TimeFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.modules.lecture.ui.LectureListRepositoryController;
import org.olat.modules.lecture.ui.blockimport.OverviewLectureBlocksDataModel.BlockCols;
import org.olat.modules.lecture.ui.blockimport.component.ParticipantsRenderer;
import org.olat.modules.lecture.ui.blockimport.component.ShorterRenderer;
import org.olat.modules.lecture.ui.blockimport.component.StatusRenderer;
import org.olat.modules.lecture.ui.blockimport.component.TeachersRenderer;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * 
 * Initial date: 12 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OverviewLectureBlocksController extends StepFormBasicController {
	
	private FlexiTableElement tableEl;
	private OverviewLectureBlocksDataModel tableModel;
	
	private ImportedLectureBlocks importedLectureBlocks;
	
	@Autowired
	private UserManager userManager;

	public OverviewLectureBlocksController(UserRequest ureq, WindowControl wControl,
			ImportedLectureBlocks importedLectureBlocks, StepsRunContext runContext, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		setTranslator(Util.createPackageTranslator(LectureListRepositoryController.class, getLocale(), getTranslator()));
		
		this.importedLectureBlocks = importedLectureBlocks;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.status, new StatusRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.externalId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.plannedLectures));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.date, new DateFlexiCellRenderer(getLocale())));
		TimeFlexiCellRenderer timeRenderer = new TimeFlexiCellRenderer(getLocale());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.startTime, timeRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.endTime, timeRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.compulsory));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.teachers, new TeachersRenderer(userManager)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.participants, new ParticipantsRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.location));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.description, new ShorterRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.preparation, new ShorterRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.comment, new ShorterRenderer()));

		tableModel = new OverviewLectureBlocksDataModel(columnsModel); 
		tableModel.setObjects(importedLectureBlocks.getLectureBlocks());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 50, true, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}
}
