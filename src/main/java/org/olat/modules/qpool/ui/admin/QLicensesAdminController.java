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
package org.olat.modules.qpool.ui.admin;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.model.QLicense;
import org.olat.modules.qpool.ui.QuestionsController;

/**
 * 
 * Manage the licenses
 * 
 * Initial date: 18.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QLicensesAdminController extends FormBasicController {
	
	private FormLink createType;
	
	private QItemTypeDataModel model;
	private FlexiTableElement tableEl;

	private CloseableModalController cmc;
	private QLicenseEditController editCtrl;
	private DialogBoxController confirmDeleteCtrl;
	
	private final QPoolService qpoolService;
	
	public QLicensesAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, null, "licenses_admin", Util.createPackageTranslator(QuestionsController.class, ureq.getLocale()));
		
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		initForm(ureq);
		reloadModel();
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.id.i18nKey(), Cols.id.ordinal(), true, "key"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.licenseKey.i18nKey(), Cols.licenseKey.ordinal(), true, "licenseKey"));
		FlexiCellRenderer delRenderer = new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("delete"), "delete-license"), null);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", Cols.deletable.ordinal(), "delete-license", delRenderer));

		model = new QItemTypeDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "licenses", model, getTranslator(), formLayout);
		tableEl.setRendererType(FlexiTableRendererType.classic);
		tableEl.setCustomizeColumns(false);
		
		createType = uifactory.addFormLink("create.license", formLayout, Link.BUTTON);
	}
	
	private void reloadModel() {
		List<QLicense> rows = qpoolService.getAllLicenses();
		model.setObjects(rows);
		tableEl.reset();	
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == createType) {
			doEdit(ureq, null);
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("delete-license".equals(se.getCommand())) {
					QLicense row = model.getObject(se.getIndex());
					doConfirmDelete(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == editCtrl) {
			if(event == Event.DONE_EVENT) {
				reloadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == confirmDeleteCtrl) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				QLicense license = (QLicense)confirmDeleteCtrl.getUserObject();
				doDelete(license);
			}
		} else if(source == cmc) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(cmc);
		editCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	
	private void doConfirmDelete(UserRequest ureq, QLicense type) {
		String title = translate("delete.license");
		String text = translate("delete.license.confirm", new String[]{ type.getLicenseKey() });
		confirmDeleteCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteCtrl);
		confirmDeleteCtrl.setUserObject(type);
	}
	
	private void doDelete(QLicense license) {
		if(qpoolService.deleteLicense(license)) {
			reloadModel();
			showInfo("item.license.deleted");
		} else {
			showError("item.license.notdeleted");
		}
	}
	
	private void doEdit(UserRequest ureq, QLicense license) {
		removeAsListenerAndDispose(editCtrl);
		editCtrl = new QLicenseEditController(ureq, getWindowControl(), license);
		listenTo(editCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				editCtrl.getInitialComponent(), true, translate("create.license"));
		cmc.activate();
		listenTo(cmc);	
	}
	
	private enum Cols {
		id("license.id"),
		licenseKey("license.key"),
		deletable("license.deletable");
		
		private final String i18nKey;
	
		private Cols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
	
	private class QItemTypeDataModel implements FlexiTableDataModel<QLicense>, TableDataModel<QLicense> {

		private FlexiTableColumnModel columnModel;
		private List<QLicense> licenses;
		
		public QItemTypeDataModel(FlexiTableColumnModel columnModel) {
			this.columnModel = columnModel;
		}
		
		@Override
		public FlexiTableColumnModel getTableColumnModel() {
			return columnModel;
		}

		@Override
		public void setTableColumnModel(FlexiTableColumnModel columnModel) {
			this.columnModel = columnModel;
		}

		@Override
		public int getColumnCount() {
			return columnModel.getColumnCount();
		}

		@Override
		public QLicense getObject(int row) {
			if(licenses != null && row >= 0 && row < licenses.size()) {
				return licenses.get(row);
			}
			return null;
		}

		@Override
		public void setObjects(List<QLicense> objects) {
			licenses = new ArrayList<QLicense>(objects);
		}

		@Override
		public Object createCopyWithEmptyList() {
			return new QItemTypeDataModel(columnModel);
		}

		@Override
		public int getRowCount() {
			return licenses == null ? 0 : licenses.size();
		}

		@Override
		public boolean isRowLoaded(int row) {
			return licenses != null && row < licenses.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			QLicense type = getObject(row);
			switch(Cols.values()[col]) {
				case id: return type.getKey();
				case licenseKey: return type.getLicenseKey();
				case deletable: return type.isDeletable();
				default: return "";
			}
		}
	}
}