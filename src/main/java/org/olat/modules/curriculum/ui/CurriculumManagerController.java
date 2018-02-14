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
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.modules.curriculum.ui.CurriculumManagerDataModel.CurriculumCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumManagerController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private FormLink addCurriculumButton;
	private CurriculumManagerDataModel tableModel;
	
	private CloseableModalController cmc;
	private EditCurriculumController newCurriculumCtrl;
	
	@Autowired
	private CurriculumService curriculumService;
	
	public CurriculumManagerController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "manage_curriculum");
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addCurriculumButton = uifactory.addFormLink("add.curriculum", formLayout, Link.BUTTON);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CurriculumCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.displayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CurriculumCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select"));
		
		tableModel = new CurriculumManagerDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setEmtpyTableMessageKey("table.curriculum.empty");
		tableEl.setAndLoadPersistedPreferences(ureq, "cur-curriculum-manage");
	}
	
	private void loadModel() {
		CurriculumSearchParameters params = new CurriculumSearchParameters();
		List<Curriculum> curriculums = curriculumService.getCurriculums(params);
		List<CurriculumRow> rows = curriculums.stream()
				.map(this::forgeRow).collect(Collectors.toList());
		tableModel.setObjects(rows);
		tableEl.reset(false, true, true);
	}
	
	private CurriculumRow forgeRow(Curriculum curriculum) {
		CurriculumRow row = new CurriculumRow(curriculum);
		
		return row;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(newCurriculumCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
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
		removeAsListenerAndDispose(newCurriculumCtrl);
		removeAsListenerAndDispose(cmc);
		newCurriculumCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addCurriculumButton == source) {
			doAddCurriculum(ureq);
		} else if(tableEl == source) {
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
	
	private void doAddCurriculum(UserRequest ureq) {
		if(newCurriculumCtrl != null) return;

		newCurriculumCtrl = new EditCurriculumController(ureq, getWindowControl());
		listenTo(newCurriculumCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", newCurriculumCtrl.getInitialComponent(), true, translate("add.curriculum"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSelectCurriculum(UserRequest ureq, CurriculumRow row) {
		
	}
}
