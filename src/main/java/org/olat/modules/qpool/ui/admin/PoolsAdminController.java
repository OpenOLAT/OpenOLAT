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

import java.util.Collections;
import java.util.List;

import org.olat.admin.securitygroup.gui.GroupController;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.admin.securitygroup.gui.IdentitiesRemoveEvent;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.CSSIconFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.model.PoolImpl;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.events.QPoolEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Manage the list of pools
 * 
 * Initial date: 21.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PoolsAdminController extends FormBasicController {
	
	private FormLink createPool;
	
	private PoolDataModel model;
	private FlexiTableElement poolTable;
	
	private GroupController groupCtrl;
	private CloseableModalController cmc;
	private PoolEditController poolEditCtrl;
	private DialogBoxController confirmDeleteCtrl;
	
	@Autowired
	private QPoolService qpoolService;
	
	public PoolsAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, null, "pools_admin", Util.createPackageTranslator(QuestionsController.class, ureq.getLocale()));

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.publicPool,
				new BooleanCellRenderer(
						new CSSIconFlexiCellRenderer("o_icon_pool_public"),
						new CSSIconFlexiCellRenderer("o_icon_pool_private"))
		));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit-pool"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("pool.owners", translate("pool.owners"), "owners-pool"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete-pool"));

		model = new PoolDataModel(columnsModel, getTranslator());
		poolTable = uifactory.addTableElement(getWindowControl(), "pools", model, getTranslator(), formLayout);
		poolTable.setCustomizeColumns(false);
		
		poolTable.setRendererType(FlexiTableRendererType.classic);
		reloadModel();
		
		createPool = uifactory.addFormLink("create.pool", formLayout, Link.BUTTON);
	}
	
	private void reloadModel() {
		ResultInfos<Pool> pools = qpoolService.getPools(0,	-1);
		model.setObjects(pools.getObjects());
		poolTable.reset();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == createPool) {
			doEditPool(ureq, null);
		} else if(source == poolTable) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("edit-pool".equals(se.getCommand())) {
					Pool row = model.getObject(se.getIndex());
					doEditPool(ureq, row);
				} else if("delete-pool".equals(se.getCommand())) {
					Pool row = model.getObject(se.getIndex());
					doConfirmDelete(ureq, row);
				} else if("owners-pool".equals(se.getCommand())) {
					Pool row = model.getObject(se.getIndex());
					doManageOwners(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == poolEditCtrl) {
			if(event == Event.DONE_EVENT) {
				reloadModel();
				fireEvent(ureq, new QPoolEvent(QPoolEvent.POOL_CREATED));
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == groupCtrl) {
			Pool selectedPool = (Pool)groupCtrl.getUserObject();
			if(event instanceof IdentitiesAddEvent ) { 
				IdentitiesAddEvent identitiesAddedEvent = (IdentitiesAddEvent) event;
				List<Identity> list = identitiesAddedEvent.getAddIdentities();
        qpoolService.addOwners(list, Collections.singletonList(selectedPool));
        identitiesAddedEvent.getAddedIdentities().addAll(list);
			} else if (event instanceof IdentitiesRemoveEvent) {
				IdentitiesRemoveEvent identitiesRemoveEvent = (IdentitiesRemoveEvent) event;
				List<Identity> list = identitiesRemoveEvent.getRemovedIdentities();
        qpoolService.removeOwners(list, Collections.singletonList(selectedPool));
			}
		}	else if(source == confirmDeleteCtrl) {
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
		removeAsListenerAndDispose(groupCtrl);
		removeAsListenerAndDispose(cmc);
		poolEditCtrl = null;
		groupCtrl = null;
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
		reloadModel();
		fireEvent(ureq, new QPoolEvent(QPoolEvent.POOL_DELETED));
	}
	
	private void doEditPool(UserRequest ureq, Pool pool) {
		removeAsListenerAndDispose(poolEditCtrl);
		poolEditCtrl = new PoolEditController(ureq, getWindowControl(), pool);
		listenTo(poolEditCtrl);
		
		String title = pool == null ? translate("create.pool") : translate("edit.pool");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				poolEditCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);	
	}
	
	private void doManageOwners(UserRequest ureq, Pool pool) {
		if(pool instanceof PoolImpl) {
			PoolImpl poolImpl = (PoolImpl)pool;
			groupCtrl = new GroupController(ureq, getWindowControl(), true, true, false, true,
					false, false, poolImpl.getOwnerGroup());
			groupCtrl.setUserObject(pool);
			listenTo(groupCtrl);
	
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					groupCtrl.getInitialComponent(), true, translate("pool.owners"));
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private enum Cols implements FlexiSortableColumnDef {
		id("pool.key"),
		publicPool("pool.public"),
		name("pool.name");
		
		private final String i18nKey;
	
		private Cols(String i18nKey) {
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
	
	private static class PoolDataModel extends DefaultFlexiTableDataModel<Pool> implements SortableFlexiTableDataModel<Pool> {

		private FlexiTableColumnModel columnModel;
		private final Translator translator;
		
		public PoolDataModel(FlexiTableColumnModel columnModel, Translator translator) {
			super(columnModel);
			this.translator = translator;
		}
		
		@Override
		public void sort(SortKey orderBy) {
			if(orderBy != null) {
				List<Pool> views = new SortableFlexiTableModelDelegate<>(orderBy, this, translator.getLocale()).sort();
				super.setObjects(views);
			}
		}

		@Override
		public PoolDataModel createCopyWithEmptyList() {
			return new PoolDataModel(columnModel, translator);
		}
	
		@Override
		public Object getValueAt(int row, int col) {
			Pool pool = getObject(row);
			return getValueAt(pool, col);
		}
		
		@Override
		public Object getValueAt(Pool pool, int col) {
			switch(Cols.values()[col]) {
				case id: return pool.getKey();
				case publicPool: return Boolean.valueOf(pool.isPublicPool());
				case name: return pool.getName();
				default: return "";
			}
		}
	}
}
