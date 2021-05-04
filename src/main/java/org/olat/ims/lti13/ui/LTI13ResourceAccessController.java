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
package org.olat.ims.lti13.ui;

import java.util.List;

import org.olat.basesecurity.OrganisationRoles;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.group.BusinessGroup;
import org.olat.ims.lti13.LTI13Module;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13SharedToolDeployment;
import org.olat.ims.lti13.ui.LTI13SharedToolDeploymentsTableModel.SharedToolsCols;
import org.olat.modules.co.ContactFormController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13ResourceAccessController extends FormBasicController {
	
	private FormLink addDeploymentButton;
	private FormLink askForDeploymentButton;
	private FlexiTableElement tableEl;
	private LTI13SharedToolDeploymentsTableModel tableModel;
	
	private RepositoryEntry entry;
	private BusinessGroup businessGroup;
	private final boolean allowedToAddDeployment;

	private CloseableModalController cmc;
	private ContactFormController emailCtrl;
	private LTI13SharedToolDeploymentController viewDeploymentCtrl;
	private LTI13EditSharedToolDeploymentController addDeploymentCtrl;
	private LTI13EditSharedToolDeploymentController editDeploymentCtrl;

	@Autowired
	private LTI13Module lti13Module;
	@Autowired
	private LTI13Service lti13Service;

	public LTI13ResourceAccessController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, "access_resource");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.entry = entry;
		allowedToAddDeployment = allowedToAddDeployments(ureq, lti13Module.getDeploymentRolesListForRepositoryEntries());

		initForm(ureq);
		loadModel();
	}
	
	public LTI13ResourceAccessController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup) {
		super(ureq, wControl, "access_resource");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.businessGroup = businessGroup;
		allowedToAddDeployment = allowedToAddDeployments(ureq, lti13Module.getDeploymentRolesListForRepositoryEntries());

		initForm(ureq);
		loadModel();
	}
	
	private boolean allowedToAddDeployments(UserRequest ureq, List<OrganisationRoles> rolesAllowedTo) {
		Roles roles = ureq.getUserSession().getRoles();
		for(OrganisationRoles role:rolesAllowedTo) {
			if(roles.hasRole(role)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("access.lti13.title");
		
		if(allowedToAddDeployment) {
			addDeploymentButton = uifactory.addFormLink("add.deployment", formLayout, Link.BUTTON);
			addDeploymentButton.setIconLeftCSS("o_icon o_icon_add");
		} else {
			askForDeploymentButton = uifactory.addFormLink("ask.deployment", formLayout, Link.BUTTON);
			askForDeploymentButton.setIconLeftCSS("o_icon o_icon_add");
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SharedToolsCols.platformName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SharedToolsCols.issuer, new ServerCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SharedToolsCols.clientId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SharedToolsCols.deploymentId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("view", translate("view"), "view"));
		if(allowedToAddDeployment) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
		}
		
		tableModel = new LTI13SharedToolDeploymentsTableModel(columnsModel, getLocale());
		
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setEmptyTableMessageKey("tools.empty");
		tableEl.setAndLoadPersistedPreferences(ureq, "lti13-tools-admin");
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	private void loadModel() {
		List<LTI13SharedToolDeployment> rows;
		if(entry != null) {
			rows = lti13Service.getSharedToolDeployments(entry);
		} else if(businessGroup != null) {
			rows = lti13Service.getSharedToolDeployments(businessGroup);
		} else {
			rows = List.of();
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editDeploymentCtrl == source || addDeploymentCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(viewDeploymentCtrl == source || emailCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(viewDeploymentCtrl);
		removeAsListenerAndDispose(editDeploymentCtrl);
		removeAsListenerAndDispose(addDeploymentCtrl);
		removeAsListenerAndDispose(emailCtrl);
		removeAsListenerAndDispose(cmc);
		viewDeploymentCtrl = null;
		editDeploymentCtrl = null;
		addDeploymentCtrl = null;
		emailCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addDeploymentButton == source) {
			doAddDeployment(ureq);
		} else if(askForDeploymentButton == source) {
			doAskDeployment(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("edit".equals(se.getCommand())) {
					doEditDeployment(ureq, tableModel.getObject(se.getIndex()));
				} else if("view".equals(se.getCommand())) {
					doViewDeployment(ureq, tableModel.getObject(se.getIndex()));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doAddDeployment(UserRequest ureq) {
		if(guardModalController(addDeploymentCtrl)) return;

		addDeploymentCtrl = new LTI13EditSharedToolDeploymentController(ureq, getWindowControl(), entry, businessGroup);
		listenTo(addDeploymentCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", addDeploymentCtrl.getInitialComponent(),
				true, translate("add.tool"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doEditDeployment(UserRequest ureq, LTI13SharedToolDeployment deployment) {
		if(guardModalController(editDeploymentCtrl)) return;

		editDeploymentCtrl = new LTI13EditSharedToolDeploymentController(ureq, getWindowControl(), deployment);
		listenTo(editDeploymentCtrl);
		
		String title = translate("edit.tool", new String[] { deployment.getPlatform().getIssuer() });
		cmc = new CloseableModalController(getWindowControl(), "close", editDeploymentCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doViewDeployment(UserRequest ureq, LTI13SharedToolDeployment deployment) {
		if(guardModalController(editDeploymentCtrl)) return;
		
		viewDeploymentCtrl = new LTI13SharedToolDeploymentController(ureq, getWindowControl(), deployment);
		listenTo(viewDeploymentCtrl);
		
		String title = translate("view.deployment.tool", new String[] { deployment.getPlatform().getIssuer() });
		cmc = new CloseableModalController(getWindowControl(), "close", viewDeploymentCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doAskDeployment(UserRequest ureq) {
		if(guardModalController(emailCtrl)) return;
		
		ContactMessage cmsg = new ContactMessage(getIdentity());
		
		if(entry != null) {
			List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(entry);
			String uri = BusinessControlFactory.getInstance().getAsAuthURIString(entries, true);
			
			String[] args = new String[] {
				entry.getDisplayname(),
				entry.getExternalRef(),
				uri	
			};
			cmsg.setSubject(translate("mail.deployment.entry.subject", args));
			cmsg.setBodyText(translate("mail.deployment.entry.body", args));
		} else if(businessGroup != null) {
			List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(businessGroup);
			String uri = BusinessControlFactory.getInstance().getAsAuthURIString(entries, true);
			
			String[] args = new String[] {
				businessGroup.getName(),
				uri
			};
			cmsg.setSubject(translate("mail.deployment.group.subject", args));
			cmsg.setBodyText(translate("mail.deployment.group.body", args));
		}
		
		String mailAddress = WebappHelper.getMailConfig("mailSupport");
		ContactList contact = new ContactList(mailAddress);
		contact.add(mailAddress);
		cmsg.addEmailTo(contact);
		
		emailCtrl = new ContactFormController(ureq, getWindowControl(), true, false, false, cmsg);
		listenTo(emailCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", emailCtrl.getInitialComponent(),
				true, translate("ask.deployment"));
		cmc.activate();
		listenTo(cmc);
	}
}