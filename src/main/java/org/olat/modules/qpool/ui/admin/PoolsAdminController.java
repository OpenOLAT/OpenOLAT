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
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
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
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
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
	private FormLink exportButton;
	
	private PoolDataModel model;
	private FlexiTableElement poolTable;
	
	private GroupController groupCtrl;
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private PoolEditController poolEditCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private ConfirmDeletePoolController confirmDeleteCtrl;
	
	@Autowired
	private QPoolService qpoolService;
	
	public PoolsAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, null, "pools_admin", Util.createPackageTranslator(QuestionsController.class, ureq.getLocale()));

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initButtonsForm(formLayout);
		initTableForm(formLayout);
	}
	
	private void initButtonsForm(FormItemContainer formLayout) {
		createPool = uifactory.addFormLink("create.pool", formLayout, Link.BUTTON);
		
		DropdownItem moreDropdown = uifactory.addDropdownMenuMore("more.menu", formLayout, getTranslator());
		moreDropdown.setEmbbeded(true);
		moreDropdown.setButton(true);
		
		exportButton = uifactory.addFormLink("export.pool.metadata", formLayout, Link.LINK); 
		exportButton.setIconLeftCSS("o_icon o_icon-fw o_icon_export");
		moreDropdown.addElement(exportButton);
	}
	
	private void initTableForm(FormItemContainer formLayout) {
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
		
		ActionsColumnModel actionsCol = new ActionsColumnModel(Cols.tools);
        actionsCol.setCellRenderer(new ActionsCellRenderer(getTranslator()));
		columnsModel.addFlexiColumnModel(actionsCol);

		model = new PoolDataModel(columnsModel, getTranslator());
		poolTable = uifactory.addTableElement(getWindowControl(), "pools", model, getTranslator(), formLayout);
		poolTable.setCustomizeColumns(false);
		
		poolTable.setRendererType(FlexiTableRendererType.classic);
		reloadModel();
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
		} else if(source == exportButton) {
			doExportMembers(ureq);
		} else if(source == poolTable) {
			if(event instanceof SelectionEvent se) {
				if("edit-pool".equals(se.getCommand())) {
					Pool row = model.getObject(se.getIndex());
					doEditPool(ureq, row);
				} else if("delete-pool".equals(se.getCommand())) {
					Pool row = model.getObject(se.getIndex());
					doConfirmDelete(ureq, row);
				} else if("owners-pool".equals(se.getCommand())) {
					Pool row = model.getObject(se.getIndex());
					doManageOwners(ureq, row);
				} else if(ActionsCellRenderer.CMD_ACTIONS.equals(se.getCommand())) {
					String targetId = ActionsCellRenderer.getId(se.getIndex());
					Pool row = model.getObject(se.getIndex());
					doOpenTools(ureq, row, targetId);
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
			if(event instanceof IdentitiesAddEvent identitiesAddedEvent) { 
				List<Identity> list = identitiesAddedEvent.getAddIdentities();
				qpoolService.addOwners(list, Collections.singletonList(selectedPool));
				identitiesAddedEvent.getAddedIdentities().addAll(list);
			} else if (event instanceof IdentitiesRemoveEvent identitiesRemoveEvent) {
				List<Identity> list = identitiesRemoveEvent.getRemovedIdentities();
				qpoolService.removeOwners(list, Collections.singletonList(selectedPool));
			}
		} else if(source == confirmDeleteCtrl) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				reloadModel();
				fireEvent(ureq, new QPoolEvent(QPoolEvent.POOL_DELETED));
			}
			cleanUp();
		} else if(source == toolsCtrl) {
			calloutCtrl.deactivate();
			cleanUp();
		} else if(source == cmc || source == calloutCtrl) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(poolEditCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(groupCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeleteCtrl = null;
		poolEditCtrl = null;
		calloutCtrl = null;
		groupCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doConfirmDelete(UserRequest ureq, Pool pool) {
		confirmDeleteCtrl = new ConfirmDeletePoolController(ureq, getWindowControl(), pool);
		listenTo(confirmDeleteCtrl);
		
		String title = translate("delete.pool");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				confirmDeleteCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);	
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
		if(pool instanceof PoolImpl poolImpl) {
			groupCtrl = new GroupController(ureq, getWindowControl(), true, true, false, false,
					false, false, poolImpl.getOwnerGroup());
			groupCtrl.setUserObject(pool);
			listenTo(groupCtrl);
	
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					groupCtrl.getInitialComponent(), true, translate("pool.owners"));
			cmc.activate();
			listenTo(cmc);
		}
	}

	private void doExportMembers(UserRequest ureq) {
		ResultInfos<Pool> pools = qpoolService.getPools(0, -1);
		doExportMembers(ureq, pools.getObjects());
	}
	
	private void doExportMembers(UserRequest ureq, List<Pool> pools) {
		Roles roles = ureq.getUserSession().getRoles();
		PoolMembersExport export = new PoolMembersExport("Pool_members", pools, roles, getTranslator());
		ureq.getDispatchResult().setResultingMediaResource(export);
	}
	
	private void doOpenTools(UserRequest ureq, Pool row, String targetId) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(calloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);
	
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), targetId, "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private enum Cols implements FlexiSortableColumnDef {
		id("pool.key"),
		publicPool("pool.public"),
		name("pool.name"),
		tools("action.more");
		
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
			return this != tools;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
	
	private static class PoolDataModel extends DefaultFlexiTableDataModel<Pool> implements SortableFlexiTableDataModel<Pool> {

		private final static Cols[] COLS = Cols.values();
		
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
		public Object getValueAt(int row, int col) {
			Pool pool = getObject(row);
			return getValueAt(pool, col);
		}
		
		@Override
		public Object getValueAt(Pool pool, int col) {
			return switch(COLS[col]) {
				case id -> pool.getKey();
				case publicPool -> Boolean.valueOf(pool.isPublicPool());
				case name -> pool.getName();
				case tools -> Boolean.TRUE;
				default -> "ERROR";
			};
		}
	}
	
	private class ToolsController extends BasicController {

		private Link exportLink;
		
		private final Pool row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, Pool row) {
			super(ureq, wControl);
			this.row = row;

			VelocityContainer mainVC = createVelocityContainer("tool_pools");
			
			exportLink = LinkFactory.createLink("export.pool.metadata", "export", getTranslator(), mainVC, this, Link.LINK);
			exportLink.setIconLeftCSS("o_icon o_icon-fw o_icon_export");
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			if(exportLink == source) {
				doExportMembers(ureq, List.of(row));
			}
		}
	}
}
