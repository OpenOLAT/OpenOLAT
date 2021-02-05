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
package org.olat.modules.portfolio.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRenderEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.BinderSecurityCallbackFactory;
import org.olat.modules.portfolio.BinderStatus;
import org.olat.modules.portfolio.model.BinderStatistics;
import org.olat.modules.portfolio.ui.event.DeleteBinderEvent;
import org.olat.modules.portfolio.ui.event.RestoreBinderEvent;
import org.olat.modules.portfolio.ui.model.BinderRow;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DeletedBinderController extends BinderListController {

	private FormLink deleteButton;
	
	private DialogBoxController confirmRestoreBinderCtrl;
	private ConfirmDeleteBinderController deleteBinderCtrl;
	
	@Autowired
	private DB dbInstance;
	
	public DeletedBinderController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl, stackPanel);
	}
	
	@Override
	public int getNumOfBinders() {
		return model.getRowCount();
	}

	@Override
	protected String getTableId() {
		return "portfolio-deleted-binder-list";
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
		model.getTableColumnModel().addFlexiColumnModel(new DefaultFlexiColumnModel("restore.binder", translate("restore.binder"), "restore"));

		tableEl.setElementCssClass("o_portfolio_listing o_portfolio_deleted_listing");
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setMultiSelect(true);
		
		deleteButton = uifactory.addFormLink("delete.binder", "delete.binder", null, formLayout, Link.BUTTON);
		deleteButton.setVisible(tableEl.getRendererType() == FlexiTableRendererType.classic);
		tableEl.setSelectAllEnable(tableEl.getRendererType() == FlexiTableRendererType.classic);
	}

	@Override
	protected void loadModel(UserRequest ureq, boolean reset) {
		List<BinderStatistics> binderRows = portfolioService.searchOwnedDeletedBinders(getIdentity());
		List<BinderRow> rows = new ArrayList<>(binderRows.size());
		for(BinderStatistics binderRow:binderRows) {
			rows.add(forgePortfolioRow(binderRow));
		}
		model.setObjects(rows);
		if(reset) {
			tableEl.reset();
		}
		tableEl.reloadData();
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(binderCtrl == source) {
			if(event instanceof RestoreBinderEvent || event instanceof DeleteBinderEvent) {
				loadModel(ureq, true);
				fireEvent(ureq, event);
			}
		} else if(deleteBinderCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doDelete(deleteBinderCtrl.getBinderStatistics());
				loadModel(ureq, true);
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmRestoreBinderCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				doRestore((BinderRow)confirmRestoreBinderCtrl.getUserObject());
				loadModel(ureq, true);
			}	
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof FlexiTableRenderEvent) {
				FlexiTableRenderEvent se = (FlexiTableRenderEvent)event;
				deleteButton.setVisible(se.getRendererType() == FlexiTableRendererType.classic
						&& model.getRowCount() > 0);
				tableEl.setSelectAllEnable(tableEl.getRendererType() == FlexiTableRendererType.classic);
			} else if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("restore".equals(cmd)) {
					BinderRow row = model.getObject(se.getIndex());
					doConfirmRestoreBinder(ureq, row);
				}
			}
		} else if(deleteButton == source) {
			doConfirmDelete(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}


	@Override
	protected void cleanUp() {
		super.cleanUp();
		removeAsListenerAndDispose(deleteBinderCtrl);
		deleteBinderCtrl = null;
	}

	private void doConfirmDelete(UserRequest ureq) {
		if(guardModalController(deleteBinderCtrl)) return;
		
		List<BinderRow> rows = getSelectedRows();
		if(rows.isEmpty()) {
			showWarning("binder.atleastone");
		} else {
			List<BinderStatistics> stats = new ArrayList<>(rows.size());
			for(BinderRow row:rows) {
				stats.add(row.getStatistics());
			}

			deleteBinderCtrl = new ConfirmDeleteBinderController(ureq, getWindowControl(), stats);
			listenTo(deleteBinderCtrl);
			
			String title = translate("delete.binder");
			cmc = new CloseableModalController(getWindowControl(), null, deleteBinderCtrl.getInitialComponent(), true, title, true);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doDelete(List<BinderStatistics> stats) {
		for(BinderStatistics stat:stats) {
			portfolioService.deleteBinder(stat);
		}
		showInfo("delete.binder.success");
	}

	@Override
	protected BinderController doOpenBinder(UserRequest ureq, BinderRef row) {
		Binder binder = portfolioService.getBinderByKey(row.getKey());
		return doOpenBinder(ureq, binder);
	}
	
	@Override
	protected BinderController doOpenBinder(UserRequest ureq, Binder binder) {
		if(binder == null) {
			showWarning("warning.portfolio.not.found");
			return null;
		} else {
			removeAsListenerAndDispose(binderCtrl);
			
			OLATResourceable binderOres = OresHelper.createOLATResourceableInstance("Binder", binder.getKey());
			WindowControl swControl = addToHistory(ureq, binderOres, null);
			BinderSecurityCallback secCallback = BinderSecurityCallbackFactory.getCallbackForDeletedBinder();
			BinderConfiguration config = BinderConfiguration.createConfig(binder);
			binderCtrl = new BinderController(ureq, swControl, stackPanel, secCallback, binder, config);
			listenTo(binderCtrl);
			stackPanel.pushController(binder.getTitle(), binderCtrl);
			return binderCtrl;
		}
	}
	
	private void doConfirmRestoreBinder(UserRequest ureq, BinderRow row) {
		String title = translate("restore.binder.confirm.title");
		String text = translate("restore.binder.confirm.descr", new String[]{ StringHelper.escapeHtml(row.getTitle()) });
		confirmRestoreBinderCtrl = activateYesNoDialog(ureq, title, text, confirmRestoreBinderCtrl);
		confirmRestoreBinderCtrl.setUserObject(row);
	}
	
	private void doRestore(BinderRow row) {
		Binder binder = portfolioService.getBinderByKey(row.getKey());
		binder.setBinderStatus(BinderStatus.open);
		portfolioService.updateBinder(binder);
		dbInstance.commit();
		showInfo("restore.binder.success");
	}
}