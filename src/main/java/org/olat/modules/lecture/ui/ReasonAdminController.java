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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.Reason;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReasonAdminController extends FormBasicController {
	
	private FormLink addReasonButton;
	private ReasonDataModel dataModel;
	private FlexiTableElement tableEl;
	
	private CloseableModalController cmc;
	private EditReasonController editReasonCtrl;
	
	@Autowired
	private LectureService lectureService;
	
	public ReasonAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "admin_reason");
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addReasonButton = uifactory.addFormLink("add.reason", formLayout, Link.BUTTON);
		addReasonButton.setIconLeftCSS("o_icon o_icon_add_item");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ReasonCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReasonCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReasonCols.description));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
		
		dataModel = new ReasonDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
	}
	
	private void loadModel() {
		List<Reason> reasons = lectureService.getAllReasons();
		dataModel.setObjects(reasons);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editReasonCtrl == source) {
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
		removeAsListenerAndDispose(editReasonCtrl);
		removeAsListenerAndDispose(cmc);
		editReasonCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addReasonButton == source) {
			doAddReason(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				Reason row = dataModel.getObject(se.getIndex());
				if("edit".equals(cmd)) {
					doEditReason(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doEditReason(UserRequest ureq, Reason reason) {
		editReasonCtrl = new EditReasonController(ureq, getWindowControl(), reason);
		listenTo(editReasonCtrl);
		
		String title = translate("edit.reason");
		cmc = new CloseableModalController(getWindowControl(), "close", editReasonCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddReason(UserRequest ureq) {
		editReasonCtrl = new EditReasonController(ureq, getWindowControl());
		listenTo(editReasonCtrl);
		
		String title = translate("add.reason");
		cmc = new CloseableModalController(getWindowControl(), "close", editReasonCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private class ReasonDataModel extends DefaultFlexiTableDataModel<Reason>
	implements SortableFlexiTableDataModel<Reason> {
		
		public ReasonDataModel(FlexiTableColumnModel columnsModel) {
			super(columnsModel);
		}

		@Override
		public void sort(SortKey orderBy) {
			List<Reason> rows = new SortableFlexiTableModelDelegate<Reason>(orderBy, this, getLocale()).sort();
			super.setObjects(rows);
		}

		@Override
		public Object getValueAt(int row, int col) {
			Reason reason = getObject(row);
			return getValueAt(reason, col);
		}

		@Override
		public Object getValueAt(Reason row, int col) {
			switch(ReasonCols.values()[col]) {
				case id: return row.getKey();
				case title: return row.getTitle();
				case description: return row.getDescription();
				default: return null;
			}
		}

		@Override
		public DefaultFlexiTableDataModel<Reason> createCopyWithEmptyList() {
			return new ReasonDataModel(getTableColumnModel());
		}
	}
	
	public enum ReasonCols implements FlexiSortableColumnDef {
		id("reason.id"),
		title("reason.title"),
		description("reason.description");
		
		private final String i18nKey;
		
		private ReasonCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
