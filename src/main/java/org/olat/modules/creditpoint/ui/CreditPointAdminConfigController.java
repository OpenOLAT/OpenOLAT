/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.creditpoint.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateTimeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.modules.creditpoint.CreditPointModule;
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointSystemStatus;
import org.olat.modules.creditpoint.model.CreditPointSystemInfos;
import org.olat.modules.creditpoint.ui.CreditPointSystemTableModel.SystemCols;
import org.olat.modules.creditpoint.ui.component.CreditPointExpirationCellRenderer;
import org.olat.modules.creditpoint.ui.component.CreditPointSystemStatusRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CreditPointAdminConfigController extends FormBasicController {
	
	private FormToggle enableEl;
	private FormLink addSystemButton;
	private FlexiTableElement tableEl;
	private CreditPointSystemTableModel tableModel;
	private FormLayoutContainer tableCont;

	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private CloseableCalloutWindowController calloutCtrl;
	private CreditPointSystemEditController systemEditCtrl;
	
	@Autowired
	private CreditPointModule creditPointModule;
	@Autowired
	private CreditPointService creditPointService;
	
	public CreditPointAdminConfigController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		
		initForm(ureq);
		loadModel();
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer enableCont = uifactory.addDefaultFormLayout("enable", null, formLayout);
		enableCont.setFormContextHelp("manual_admin/administration/Payment_Credit_Points/");
		initSettingsForm(enableCont);

		String page = velocity_root + "/systems.html";
		tableCont = uifactory.addCustomFormLayout("system", null, page, formLayout);
		initSystemsTableForm(tableCont);
	}
	
	private void initSettingsForm(FormLayoutContainer formLayout) {
		formLayout.setFormTitle(translate("admin.config.title"));
		
		enableEl = uifactory.addToggleButton("credit.point.enable", "credit.point.enable", translate("on"), translate("off"), formLayout);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		enableEl.toggle(creditPointModule.isEnabled());
	}
	
	private void initSystemsTableForm(FormLayoutContainer formLayout) {
		addSystemButton = uifactory.addFormLink("add.system", formLayout, Link.BUTTON);
		addSystemButton.setIconLeftCSS("o_icon o_icon_add");
	
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SystemCols.id));
		DefaultFlexiColumnModel nameCol = new DefaultFlexiColumnModel(SystemCols.name);
		nameCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(nameCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SystemCols.label));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SystemCols.expiration,
				new CreditPointExpirationCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SystemCols.usage));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SystemCols.status,
				new CreditPointSystemStatusRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SystemCols.creationDate,
				new DateTimeFlexiCellRenderer(getLocale())));
		
        ActionsColumnModel actionsCol = new ActionsColumnModel(SystemCols.tools);
        actionsCol.setCellRenderer(new ActionsCellRenderer(getTranslator()));
		columnsModel.addFlexiColumnModel(actionsCol);
		
		tableModel = new CreditPointSystemTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
	}
	
	private void loadModel() {
		List<CreditPointSystemInfos> systems = creditPointService.getCreditPointSystemsWithInfos();
		systems = systems.stream()
				.filter(sys -> sys.system().getStatus() != CreditPointSystemStatus.deleted)
				.collect(Collectors.toList());
		
		List<CreditPointSystemRow> rows = new ArrayList<>(systems.size());
		for(CreditPointSystemInfos system:systems) {
			rows.add(new CreditPointSystemRow(system));
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(systemEditCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(toolsCtrl == source) {
			if(event == Event.CLOSE_EVENT) {
				calloutCtrl.deactivate();
				cleanUp();
			}
		} else if(cmc == source || calloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(systemEditCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		systemEditCtrl = null;
		calloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source) {
			creditPointModule.setEnabled(enableEl.isOn());
			updateUI();
		} else if(addSystemButton == source) {
			doAddCreditPointSystem(ureq);
		} else if(tableEl == source) {
			if (event instanceof SelectionEvent se) {
				if(ActionsCellRenderer.CMD_ACTIONS.equals(se.getCommand())) {
					String targetId = ActionsCellRenderer.getId(se.getIndex());
					CreditPointSystemRow selectedRow = tableModel.getObject(se.getIndex());
					doOpenTools(ureq, selectedRow, targetId);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateUI() {
		boolean enabled = enableEl.isOn();
		tableCont.setVisible(enabled);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doAddCreditPointSystem(UserRequest ureq) {
		systemEditCtrl = new CreditPointSystemEditController(ureq, getWindowControl(), null);
		listenTo(systemEditCtrl);
		
		String title = translate("add.system");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), systemEditCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditCreditPointSystem(UserRequest ureq, CreditPointSystem creditPointSystem) {
		systemEditCtrl = new CreditPointSystemEditController(ureq, getWindowControl(), creditPointSystem);
		listenTo(systemEditCtrl);
		
		String title = translate("add.system");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), systemEditCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenTools(UserRequest ureq, CreditPointSystemRow creditPointSystem, String targetId) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(calloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), creditPointSystem);
		listenTo(toolsCtrl);
	
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), targetId, "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doUpdateStatus(CreditPointSystemRow row, CreditPointSystemStatus newStatus) {
		CreditPointSystem system = creditPointService.loadCreditPointSystem(row.getSystem());
		if(system != null) {
			if(newStatus == CreditPointSystemStatus.deleted && row.getUsage() > 0) {
				showWarning("warning.system.in.user", new String[] {
						StringHelper.escapeHtml(row.getName()), Long.toString(row.getUsage())
					});
			} else {
				system.setStatus(newStatus);
				creditPointService.updateCreditPointSystem(system);
			}
		}
		loadModel();
	}
	
	private class ToolsController extends BasicController {

		private Link editLink;
		private Link deleteLink;
		private Link reactivateLink;
		private Link inactivateLink;
		
		private VelocityContainer mainVC;
		
		private final CreditPointSystemRow creditPointSystem;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CreditPointSystemRow creditPointSystem) {
			super(ureq, wControl);
			this.creditPointSystem = creditPointSystem;
			
			mainVC = createVelocityContainer("systems_tools");
			
			if(creditPointSystem.getUsage() == 0) {
				editLink = LinkFactory.createLink("edit", "edit", getTranslator(), mainVC, this, Link.LINK);
				editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
			}
			
			CreditPointSystemStatus status = creditPointSystem.getStatus();
			if(status == CreditPointSystemStatus.inactive) {
				reactivateLink = LinkFactory.createLink("activate", "activate", getTranslator(), mainVC, this, Link.LINK);
				reactivateLink.setIconLeftCSS("o_icon o_icon-fw o_icon_check");
			}

			if(status == CreditPointSystemStatus.active) {
				inactivateLink = LinkFactory.createLink("inactivate", "inactivate", getTranslator(), mainVC, this, Link.LINK);
				inactivateLink.setIconLeftCSS("o_icon o_icon-fw o_icon_check_disabled");
			}
			
			if(status == CreditPointSystemStatus.active || status == CreditPointSystemStatus.inactive) {
				deleteLink = LinkFactory.createLink("delete", "delete", getTranslator(), mainVC, this, Link.LINK);
				deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			}
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(editLink == source) {
				fireEvent(ureq, Event.CLOSE_EVENT);
				doEditCreditPointSystem(ureq, creditPointSystem.getSystem());
			} else if(inactivateLink == source) {
				fireEvent(ureq, Event.CLOSE_EVENT);
				doUpdateStatus(creditPointSystem, CreditPointSystemStatus.inactive);
			} else if(reactivateLink == source) {
				fireEvent(ureq, Event.CLOSE_EVENT);
				doUpdateStatus(creditPointSystem, CreditPointSystemStatus.active);
			} else if(deleteLink == source) {
				fireEvent(ureq, Event.CLOSE_EVENT);
				doUpdateStatus(creditPointSystem, CreditPointSystemStatus.deleted);
			}
		}
	}
}
