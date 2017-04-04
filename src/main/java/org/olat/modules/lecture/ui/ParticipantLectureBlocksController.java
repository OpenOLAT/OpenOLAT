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

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockAndRollCall;
import org.olat.modules.lecture.ui.ParticipantLectureBlocksDataModel.ParticipantCols;
import org.olat.modules.lecture.ui.component.AbsentPresentCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ParticipantLectureBlocksController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private ParticipantLectureBlocksDataModel tableModel;
	
	private final RepositoryEntry entry;
	
	@Autowired
	private LectureService lectureService;
	
	public ParticipantLectureBlocksController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, "participant_blocks");
		this.entry = entry;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.date, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.entry));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.lectureBlock));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.coach));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantCols.present, new AbsentPresentCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("appeal", ParticipantCols.appeal.ordinal(), "appeal",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("appeal"), "appeal"), null)));

		tableModel = new ParticipantLectureBlocksDataModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		options.setDefaultOrderBy(new SortKey(ParticipantCols.date.name(), true));
		tableEl.setSortSettings(options);
		tableEl.setAndLoadPersistedPreferences(ureq, "participant-roll-call-appeal");
	}
	
	private void loadModel() {
		List<LectureBlockAndRollCall> rollCalls = lectureService.getParticipantLectureBlocks(entry, getIdentity());
		tableModel.setObjects(rollCalls);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	

}
