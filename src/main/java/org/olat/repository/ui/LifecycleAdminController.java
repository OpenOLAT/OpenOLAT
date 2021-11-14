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
package org.olat.repository.ui;

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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.ui.LifecycleDataModel.LCCols;

/**
 * 
 * Initial date: 10.06.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LifecycleAdminController extends FormBasicController {
	
	private FormLink createLifeCycle;
	private FlexiTableElement tableEl;
	private LifecycleDataModel model;

	private CloseableModalController cmc;
	private LifecycleEditController editCtrl;
	private DialogBoxController confirmDeleteCtrl;
	
	private final RepositoryEntryLifecycleDAO reLifecycleDao;
	
	public LifecycleAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "lifecycles_admin");
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		
		reLifecycleDao = CoreSpringFactory.getImpl(RepositoryEntryLifecycleDAO.class);
		
		initForm(ureq);
		reloadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LCCols.softkey.i18nKey(), LCCols.softkey.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LCCols.label.i18nKey(), LCCols.label.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LCCols.validFrom.i18nKey(), LCCols.validFrom.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LCCols.validTo.i18nKey(), LCCols.validTo.ordinal()));

		FlexiCellRenderer delRenderer = new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("delete"), "delete-cycle"), null);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", LCCols.delete.ordinal(), "delete-cycle", delRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate(LCCols.edit.i18nKey()), "edit-lifecycle"));

		model = new LifecycleDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "cycles", model, getTranslator(), formLayout);
		tableEl.setRendererType(FlexiTableRendererType.classic);
		tableEl.setCustomizeColumns(false);
		
		createLifeCycle = uifactory.addFormLink("create.lifecycle", formLayout, Link.BUTTON);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == createLifeCycle) {
			doEdit(ureq, null);
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("delete-lifecycle".equals(se.getCommand())) {
					RepositoryEntryLifecycle row = model.getObject(se.getIndex());
					doConfirmDelete(ureq, row);
				} else if("edit-lifecycle".equals(se.getCommand())) {
					RepositoryEntryLifecycle row = model.getObject(se.getIndex());
					doEdit(ureq, row);
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
				RepositoryEntryLifecycle lifecycle = (RepositoryEntryLifecycle)confirmDeleteCtrl.getUserObject();
				doDelete(lifecycle);
			}
		} else if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(cmc);
		editCtrl = null;
		cmc = null;
	}
	
	private void reloadModel() {
		List<RepositoryEntryLifecycle> lifecycles = reLifecycleDao.loadPublicLifecycle();
		model.setObjects(lifecycles);
		tableEl.reset();
	}

	private void doConfirmDelete(UserRequest ureq, RepositoryEntryLifecycle lifecycle) {
		String title = translate("delete.lifecycle");
		String text = translate("delete.lifecycle.confirm", new String[]{ lifecycle.getSoftKey(), lifecycle.getLabel() });
		confirmDeleteCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteCtrl);
		confirmDeleteCtrl.setUserObject(lifecycle);
	}
	
	private void doDelete(RepositoryEntryLifecycle lifecycle) {
		reLifecycleDao.deleteLifecycle(lifecycle);
		reloadModel();
	}
	
	private void doEdit(UserRequest ureq, RepositoryEntryLifecycle lifecycle) {
		removeAsListenerAndDispose(editCtrl);
		editCtrl = new LifecycleEditController(ureq, getWindowControl(), lifecycle);
		listenTo(editCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				editCtrl.getInitialComponent(), true, translate("edit.lifecycle"));
		cmc.activate();
		listenTo(cmc);	
	}
}
