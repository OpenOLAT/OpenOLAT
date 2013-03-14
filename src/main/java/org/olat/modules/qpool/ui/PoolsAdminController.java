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
package org.olat.modules.qpool.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.ui.PoolDataModel.Cols;

/**
 * 
 * Initial date: 21.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PoolsAdminController extends FormBasicController {
	
	private FormLink createPool;
	
	private PoolDataModel model;
	private FlexiTableElement poolTable;
	private CloseableModalController cmc;
	private PoolEditController poolEditCtrl;
	private DialogBoxController confirmDeleteCtrl;
	
	private final QPoolService qpoolService;
	
	public PoolsAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "pools_admin");
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		initForm(ureq);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.id.i18nKey(), Cols.id.ordinal(), true, "key"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.name.i18nKey(), Cols.name.ordinal(), true, "name"));
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel("edit", translate("edit"), "edit-pool"));
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel("delete", translate("delete"), "delete-pool"));

		model = new PoolDataModel(columnsModel, new PoolSource(), getTranslator());
		poolTable = uifactory.addTableElement(ureq, "pools", model, model, 20, false, getTranslator(), formLayout);
		poolTable.setRendererType(FlexiTableRendererType.classic);
		
		createPool = uifactory.addFormLink("create.pool", formLayout, Link.BUTTON);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == createPool) {
			doEditPool(ureq, null);
		} else if(source == poolTable) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("edit-pool".equals(se.getCommand())) {
					PoolRow row = model.getObject(se.getIndex());
					doEditPool(ureq, row.getPool());
				} else if("delete-pool".equals(se.getCommand())) {
					PoolRow row = model.getObject(se.getIndex());
					doConfirmDelete(ureq, row.getPool());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == poolEditCtrl) {
			if(event == Event.DONE_EVENT) {
				Pool pool = poolEditCtrl.getPool();
				if(pool == null) {
					doCreate(ureq, poolEditCtrl.getName());
				} else {
					doEdit(ureq, pool, poolEditCtrl.getName());
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == confirmDeleteCtrl) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				Pool pool = (Pool)confirmDeleteCtrl.getUserObject();
				doDelete(ureq, pool);
			}
		} else if(source == cmc) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(poolEditCtrl);
		removeAsListenerAndDispose(cmc);
		poolEditCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doConfirmDelete(UserRequest ureq, Pool pool) {
		String title = translate("delete.pool");
		String text = translate("delete.pool.confirm", new String[]{ pool.getName() });
		confirmDeleteCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteCtrl);
		confirmDeleteCtrl.setUserObject(pool);
	}
	
	private void doDelete(UserRequest ureq, Pool pool) {
		qpoolService.deletePool(pool);
		poolTable.reset();
		fireEvent(ureq, new QPoolEvent(QPoolEvent.POOL_DELETED));
	}
	
	private void doEditPool(UserRequest ureq, Pool pool) {
		removeAsListenerAndDispose(poolEditCtrl);
		poolEditCtrl = new PoolEditController(ureq, getWindowControl(), pool);
		listenTo(poolEditCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				poolEditCtrl.getInitialComponent(), true, translate("edit.pool"));
		cmc.activate();
		listenTo(cmc);	
	}
	
	private void doCreate(UserRequest ureq, String name) {
		qpoolService.createPool(getIdentity(), name);
		poolTable.reset();
		fireEvent(ureq, new QPoolEvent(QPoolEvent.POOL_CREATED));
	}
	
	private void doEdit(UserRequest ureq, Pool pool, String name) {
		pool.setName(name);
		qpoolService.updatePool(pool);
		poolTable.reset();
		fireEvent(ureq, new QPoolEvent(QPoolEvent.POOL_CREATED));
	}
	
	private PoolRow forgeRow(Pool pool) {
		PoolRow row = new PoolRow(pool);
		return row;
	}
	
	public class PoolSource {

		public int getNumOfItems() {
			return qpoolService.countPools();
		}

		public ResultInfos<PoolRow> getRows(int firstResult, int maxResults, SortKey... orderBy) {
			ResultInfos<Pool> pools = qpoolService.getPools(firstResult, maxResults, orderBy);
			List<PoolRow> rows = new ArrayList<PoolRow>(pools.getObjects().size());
			for(Pool pool:pools.getObjects()) {
				rows.add(forgeRow(pool));
			}
			return new DefaultResultInfos<PoolRow>(pools.getNextFirstResult(), pools.getCorrectedRowCount(), rows);
		}
	}
}
