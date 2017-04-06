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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockRow;
import org.olat.modules.lecture.ui.LectureListRepositoryDataModel.BlockCols;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureListRepositoryController extends FormBasicController {

	private FormLink addLectureButton;
	private FlexiTableElement tableEl;
	private LectureListRepositoryDataModel tableModel;
	
	private CloseableModalController cmc;
	private EditLectureController editLectureCtrl;

	private RepositoryEntry entry;
	
	@Autowired
	private LectureService lectureService;
	
	public LectureListRepositoryController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, "admin_repository_lectures");
		this.entry = entry;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addLectureButton = uifactory.addFormLink("add.lecture", formLayout, Link.BUTTON);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.location));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.date, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));

		tableModel = new LectureListRepositoryDataModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
	}
	
	private void loadModel() {
		List<LectureBlock> blocks = lectureService.getLectureBlocks(entry);
		List<LectureBlockRow> rows = new ArrayList<>(blocks.size());
		for(LectureBlock block:blocks) {
			rows.add(new LectureBlockRow(block.getKey(), block.getTitle(), block.getLocation(), block.getStartDate()));
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addLectureButton == source) {
			doAddLectureBlock(ureq);
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				LectureBlockRow row = tableModel.getObject(se.getIndex());
				if("edit".equals(cmd)) {
					doEditLectureBlock(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editLectureCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		
		
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editLectureCtrl);
		removeAsListenerAndDispose(cmc);
		editLectureCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doEditLectureBlock(UserRequest ureq, LectureBlockRow row) {
		LectureBlock block = lectureService.getLectureBlock(row);
		
		editLectureCtrl = new EditLectureController(ureq, getWindowControl(), entry, block);
		listenTo(editLectureCtrl);

		cmc = new CloseableModalController(getWindowControl(), "close", editLectureCtrl.getInitialComponent(), true, translate("add.lecture"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doAddLectureBlock(UserRequest ureq) {
		editLectureCtrl = new EditLectureController(ureq, getWindowControl(), entry);
		listenTo(editLectureCtrl);

		cmc = new CloseableModalController(getWindowControl(), "close", editLectureCtrl.getInitialComponent(), true, translate("add.lecture"));
		listenTo(cmc);
		cmc.activate();
	}
}