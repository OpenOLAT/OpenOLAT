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
package org.olat.user.ui.organisation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.OrganisationEmailDomain;
import org.olat.basesecurity.OrganisationEmailDomainSearchParams;
import org.olat.basesecurity.OrganisationService;
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
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.user.ui.organisation.OrganisationEmailDomainDataModel.OrganisationEmailDomainCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 Oct 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OrganisationEmailDomainAdminController extends FormBasicController {
	
	private static final String CMD_EDIT = "edit";
	private static final String CMD_ACTIVATE = "activate";
	private static final String CMD_DEACTIVATE = "deactivate";
	private static final String CMD_DELETE = "delete";
	
	private FormLayoutContainer dummyCont;
	private FormLink createLink;
	private FlexiTableElement tableEl;
	private OrganisationEmailDomainDataModel dataModel;
	
	private CloseableModalController cmc;
	private Controller editCtrl;
	private ConfirmationController disableConfirmationCtrl;
	private ConfirmationController deleteConfirmationCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ToolsController toolsCtrl;

	private final Organisation organisation;
	
	@Autowired
	private OrganisationService organisationService;

	protected OrganisationEmailDomainAdminController(UserRequest ureq, WindowControl wControl, Organisation organisation) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.organisation = organisation;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		dummyCont = FormLayoutContainer.createBareBoneFormLayout("dummy", getTranslator());
		dummyCont.setRootForm(mainForm);
		
		FormLayoutContainer titleCont = FormLayoutContainer.createDefaultFormLayout("title", getTranslator());
		titleCont.setFormTitle(translate("organisation.email.domains"));
		titleCont.setRootForm(mainForm);
		formLayout.add(titleCont);
		
		FormLayoutContainer buttonsTopCont = FormLayoutContainer.createButtonLayout("buttons.top", getTranslator());
		buttonsTopCont.setElementCssClass("o_button_group o_button_group_right");
		buttonsTopCont.setRootForm(mainForm);
		formLayout.add(buttonsTopCont);
		
		createLink = uifactory.addFormLink("create", buttonsTopCont, Link.BUTTON);
		createLink.setIconLeftCSS("o_icon o_icon_add");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, OrganisationEmailDomainCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganisationEmailDomainCols.organisation));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganisationEmailDomainCols.domain));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganisationEmailDomainCols.enabled));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganisationEmailDomainCols.subdomainsAllowed));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrganisationEmailDomainCols.numIdentitiesWithDomain));
		
		StickyActionColumnModel toolsColumn = new StickyActionColumnModel(OrganisationEmailDomainCols.tools);
		toolsColumn.setAlwaysVisible(true);
		toolsColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(toolsColumn);
		
		dataModel = new OrganisationEmailDomainDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
	}
	
	private void loadModel() {
		OrganisationEmailDomainSearchParams searchParams = new OrganisationEmailDomainSearchParams();
		if (organisation != null) {
			searchParams.setOrganisations(List.of(organisation));
		}
		List<OrganisationEmailDomain> emailDomains = organisationService.getEmailDomains(searchParams);
		Map<Long, Integer> emailDomainKeyToUsersCount = organisationService.getEmailDomainKeyToUsersCount(emailDomains);
		
		List<OrganisationEmailDomainRow> rows = new ArrayList<>(emailDomains.size());
		for (OrganisationEmailDomain emailDomain : emailDomains) {
			OrganisationEmailDomainRow row = new OrganisationEmailDomainRow(emailDomain);
			
			row.setNumIdentitieswithDomain(emailDomainKeyToUsersCount.getOrDefault(emailDomain.getKey(), Integer.valueOf(0)));
			
			FormLink toolsLink = uifactory.addFormLink("tools_" + row.getEmailDomain().getKey(), "tools", "", null, null, Link.NONTRANSLATED);
			toolsLink.setIconLeftCSS("o_icon o_icon-fws o_icon-lg o_icon_actions");
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
			if (event == Event.DONE_EVENT && disableConfirmationCtrl.getUserObject() instanceof OrganisationEmailDomain emailDomain) {
				doUpdateEnabled(emailDomain, false);
			}
			cmc.deactivate();
			cleanUp();
		} else if (deleteConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT && deleteConfirmationCtrl.getUserObject() instanceof OrganisationEmailDomain emailDomain) {
				doDelete(emailDomain);
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
			doEditEmailDomain(ureq, null);
		} else if (source instanceof FormLink link) {
			if ("tools".equals(link.getCmd()) && link.getUserObject() instanceof OrganisationEmailDomainRow row) {
				doOpenTools(ureq, row, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doEditEmailDomain(UserRequest ureq, OrganisationEmailDomain emailDomain) {
		if (guardModalController(editCtrl)) return;
		
		editCtrl = new OrganisationEmailDomainController(ureq, getWindowControl(), organisation, emailDomain);
		listenTo(editCtrl);
		
		String title = translate("organisation.email.domain.edit");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDisabled(UserRequest ureq, OrganisationEmailDomainRow emailDomainRow) {
		if (guardModalController(disableConfirmationCtrl)) return;
		
		disableConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(),
				translate("organisation.email.domain.disable.confirm.message",
						String.valueOf(emailDomainRow.getNumIdentitieswithDomain()),
						StringHelper.escapeHtml(emailDomainRow.getEmailDomain().getOrganisation().getDisplayName())),
				null,
				translate("organisation.email.domain.disable.confirm.button"), false);
		disableConfirmationCtrl.setUserObject(emailDomainRow.getEmailDomain());
		listenTo(disableConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), disableConfirmationCtrl.getInitialComponent(),
				true, translate("organisation.email.domain.disable.confirm.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doUpdateEnabled(OrganisationEmailDomain emailDomain, boolean enabled) {
		emailDomain.setEnabled(enabled);
		organisationService.updateOrganisationEmailDomain(emailDomain);
		loadModel();
	}
	
	private void doConfirmDelete(UserRequest ureq, OrganisationEmailDomain emailDomain) {
		if (guardModalController(deleteConfirmationCtrl)) return;
		
		deleteConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(),
				translate("organisation.email.domain.delete.confirm.message",
						StringHelper.escapeHtml(emailDomain.getDomain()),
						StringHelper.escapeHtml(emailDomain.getOrganisation().getDisplayName())),
				null,
				translate("organisation.email.domain.delete.confirm.button"), true);
		deleteConfirmationCtrl.setUserObject(emailDomain);
		listenTo(deleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteConfirmationCtrl.getInitialComponent(),
				true, translate("organisation.email.domain.delete.confirm.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDelete(OrganisationEmailDomain organisationEmailDomain) {
		organisationService.deleteOrganisationEmailDomain(organisationEmailDomain);
		loadModel();
	}
	
	private void doOpenTools(UserRequest ureq, OrganisationEmailDomainRow catalogLauncherRow, FormLink link) {
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
		
		private final OrganisationEmailDomainRow row;
		private final List<String> names = new ArrayList<>(3);

		private VelocityContainer mainVC;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, OrganisationEmailDomainRow row) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			
			addLink("edit", CMD_EDIT, "o_icon o_icon-fw o_icon_edit");
			
			if (row.getEmailDomain().isEnabled()) {
				addLink("organisation.email.domain.deactivate", CMD_DEACTIVATE, "o_icon o_icon-fw o_icon_deactivate");
			} else {
				addLink("organisation.email.domain.activate", CMD_ACTIVATE, "o_icon o_icon-fw o_icon_activate");
			}
			
			if (OrganisationEmailDomain.WILDCARD.equals(row.getEmailDomain().getDomain()) || row.getNumIdentitieswithDomain().intValue() == 0) {
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
					doEditEmailDomain(ureq, row.getEmailDomain());
				} else if (CMD_ACTIVATE.equals(cmd)) {
					doUpdateEnabled(row.getEmailDomain(), true);
				} else if (CMD_DEACTIVATE.equals(cmd)) {
					if (row.getNumIdentitieswithDomain() > 0) {
						doConfirmDisabled(ureq, row);
					} else {
						doUpdateEnabled(row.getEmailDomain(), false);
					}
				} else if (CMD_DELETE.equals(cmd)) {
					doConfirmDelete(ureq, row.getEmailDomain());
				}
			}
		}
	}

}
