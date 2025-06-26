/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.resource.accesscontrol.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController.ButtonType;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.BillingAddress;
import org.olat.resource.accesscontrol.BillingAddressSearchParams;
import org.olat.resource.accesscontrol.ui.BillingAddressDataModel.BillingAddressCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 31 Oct 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class BillingAddressListController extends FormBasicController {
	
	private static final String CMD_EDIT = "edit";
	private static final String CMD_ACTIVATE = "activate";
	private static final String CMD_DEACTIVATE = "deactivate";
	private static final String CMD_DELETE = "delete";
	
	private FormLink createLink;
	private FlexiTableElement tableEl;
	private BillingAddressDataModel dataModel;
	
	private CloseableModalController cmc;
	private Controller editCtrl;
	private ConfirmationController disableConfirmationCtrl;
	private ConfirmationController deleteConfirmationCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ToolsController toolsCtrl;

	private final Organisation organisation;
	private final Identity addressIdentity;
	
	@Autowired
	private ACService accessService;

	public BillingAddressListController(UserRequest ureq, WindowControl wControl, Organisation organisation, Identity addressIdentity) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.organisation = organisation;
		this.addressIdentity = addressIdentity;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer titleCont = FormLayoutContainer.createDefaultFormLayout("title", getTranslator());
		titleCont.setElementCssClass("o_sel_billing_address_list");
		titleCont.setFormTitle(translate("billing.addresses"));
		titleCont.setRootForm(mainForm);
		formLayout.add(titleCont);
		
		FormLayoutContainer buttonsTopCont = FormLayoutContainer.createButtonLayout("buttons.top", getTranslator());
		buttonsTopCont.setElementCssClass("o_button_group o_button_group_right");
		buttonsTopCont.setRootForm(mainForm);
		formLayout.add(buttonsTopCont);
		
		createLink = uifactory.addFormLink("create", buttonsTopCont, Link.BUTTON);
		createLink.setElementCssClass("o_sel_billing_address_add");
		createLink.setIconLeftCSS("o_icon o_icon_add");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if (ureq.getUserSession().getRoles().isAdministrator()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BillingAddressCols.id));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BillingAddressCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BillingAddressCols.nameLine1));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BillingAddressCols.nameLine2));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BillingAddressCols.addressLine1));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BillingAddressCols.addressLine2));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BillingAddressCols.addressLine2));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BillingAddressCols.addressLine3));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BillingAddressCols.addressLine4));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BillingAddressCols.poBox));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BillingAddressCols.region));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BillingAddressCols.zip));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BillingAddressCols.city));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BillingAddressCols.country));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BillingAddressCols.status, new BillingAddressStatusRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BillingAddressCols.numOrders));
		
		StickyActionColumnModel toolsColumn = new StickyActionColumnModel(BillingAddressCols.tools);
		toolsColumn.setAlwaysVisible(true);
		toolsColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(toolsColumn);
		
		dataModel = new BillingAddressDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "billing-addresses");
	}
	
	private void loadModel() {
		BillingAddressSearchParams searchParams = new BillingAddressSearchParams();
		if (organisation != null) {
			searchParams.setOrganisations(List.of(organisation));
		}
		if (addressIdentity != null) {
			searchParams.setIdentityKeys(List.of(addressIdentity));
		}
		List<BillingAddress> billingAddresss = accessService.getBillingAddresses(searchParams);
		Map<Long, Long> billingAddressKeyToOffersCount = accessService.getBillingAddressKeyToOrderCount(billingAddresss);
		
		List<BillingAddressRow> rows = new ArrayList<>(billingAddresss.size());
		for (BillingAddress billingAddress : billingAddresss) {
			BillingAddressRow row = new BillingAddressRow(billingAddress);
			
			row.setNumOrders(billingAddressKeyToOffersCount.getOrDefault(billingAddress.getKey(), Long.valueOf(0)));
			
			FormLink toolsLink = uifactory.addFormLink("tools_" + row.getBillingAddress().getKey(), "tools", "", null, null, Link.NONTRANSLATED);
			toolsLink.setIconLeftCSS("o_icon o_icon-fws o_icon-lg o_icon_actions");
			toolsLink.setTitle(translate("table.action"));
			toolsLink.setUserObject(row);
			row.setToolsLink(toolsLink);
			
			rows.add(row);
		}
		
		dataModel.setObjects(rows);
		tableEl.reset(false, false, true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (editCtrl == source) {
			loadModel();
			cmc.deactivate();
			cleanUp();
		} else if (disableConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT && disableConfirmationCtrl.getUserObject() instanceof BillingAddress billingAddress) {
				doUpdateEnabled(billingAddress, false);
			}
			cmc.deactivate();
			cleanUp();
		} else if (deleteConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT && deleteConfirmationCtrl.getUserObject() instanceof BillingAddress billingAddress) {
				doDelete(billingAddress);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			loadModel();
			cleanUp();
		} else if (toolsCalloutCtrl == source) {
			cleanUp();
		} else if (toolsCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (toolsCalloutCtrl != null) {
					toolsCalloutCtrl.deactivate();
					cleanUp();
				}
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(disableConfirmationCtrl);
		removeAsListenerAndDispose(deleteConfirmationCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		editCtrl = null;
		disableConfirmationCtrl = null;
		deleteConfirmationCtrl = null;
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == createLink) {
			doEditBillingAddress(ureq, null);
		} else if (source instanceof FormLink link) {
			if ("tools".equals(link.getCmd()) && link.getUserObject() instanceof BillingAddressRow row) {
				doOpenTools(ureq, row, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doEditBillingAddress(UserRequest ureq, BillingAddress billingAddress) {
		if (guardModalController(editCtrl)) return;
		
		editCtrl = new BillingAddressController(ureq, getWindowControl(), billingAddress, organisation, addressIdentity);
		listenTo(editCtrl);
		
		String title = translate("billing.address.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDisabled(UserRequest ureq, BillingAddressRow billingAddressRow) {
		if (guardModalController(disableConfirmationCtrl)) return;
		
		disableConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(),
				translate("billing.address.disable.confirm.message",
						String.valueOf(billingAddressRow.getNumOrders()),
						StringHelper.escapeHtml(billingAddressRow.getBillingAddress().getIdentifier())),
				null,
				translate("billing.address.disable.confirm.button"), ButtonType.regular);
		disableConfirmationCtrl.setUserObject(billingAddressRow.getBillingAddress());
		listenTo(disableConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), disableConfirmationCtrl.getInitialComponent(),
				true, translate("billing.address.disable.confirm.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doUpdateEnabled(BillingAddress billingAddress, boolean enabled) {
		billingAddress.setEnabled(enabled);
		accessService.updateBillingAddress(billingAddress);
		loadModel();
	}
	
	private void doConfirmDelete(UserRequest ureq, BillingAddress billingAddress) {
		if (guardModalController(deleteConfirmationCtrl)) return;
		
		deleteConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(),
				translate("billing.address.delete.confirm.message", StringHelper.escapeHtml(billingAddress.getIdentifier())),
				null,
				translate("billing.address.delete.confirm.button"), ButtonType.danger);
		deleteConfirmationCtrl.setUserObject(billingAddress);
		listenTo(deleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteConfirmationCtrl.getInitialComponent(),
				true, translate("billing.address.delete.confirm.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDelete(BillingAddress billingAddress) {
		accessService.deleteBillingAddress(billingAddress);
		loadModel();
	}
	
	private void doOpenTools(UserRequest ureq, BillingAddressRow catalogLauncherRow, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), catalogLauncherRow);
		listenTo(toolsCtrl);
	
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private class ToolsController extends BasicController {
		
		private final BillingAddressRow row;
		private final List<String> names = new ArrayList<>(3);

		private VelocityContainer mainVC;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, BillingAddressRow row) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			
			addLink("edit", CMD_EDIT, "o_icon o_icon-fw o_icon_edit");
			
			if (row.getBillingAddress().isEnabled()) {
				addLink("billing.address.deactivate", CMD_DEACTIVATE, "o_icon o_icon-fw o_icon_deactivate");
			} else {
				addLink("billing.address.activate", CMD_ACTIVATE, "o_icon o_icon-fw o_icon_activate");
			}
			
			if (row.getNumOrders().intValue() == 0) {
				names.add("-");
				addLink("delete", CMD_DELETE, "o_icon o_icon-fw o_icon_delete_item");
			}
			mainVC.contextPut("links", names);
			
			putInitialPanel(mainVC);
		}
		
		private void addLink(String name, String cmd, String iconCSS) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
			names.add(name);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			this.fireEvent(ureq, Event.DONE_EVENT);
			if (source instanceof Link link) {
				String cmd = link.getCommand();
				if (CMD_EDIT.equals(cmd)) {
					doEditBillingAddress(ureq, row.getBillingAddress());
				} else if (CMD_ACTIVATE.equals(cmd)) {
					doUpdateEnabled(row.getBillingAddress(), true);
				} else if (CMD_DEACTIVATE.equals(cmd)) {
					if (row.getNumOrders() > 0) {
						doConfirmDisabled(ureq, row);
					} else {
						doUpdateEnabled(row.getBillingAddress(), false);
					}
				} else if (CMD_DELETE.equals(cmd)) {
					doConfirmDelete(ureq, row.getBillingAddress());
				}
			}
		}
	}

}
