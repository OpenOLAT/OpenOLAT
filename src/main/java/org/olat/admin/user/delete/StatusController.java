/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.admin.user.delete;

import java.util.Collections;
import java.util.List;

import org.olat.admin.user.UserSearchController;
import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Controller for tab 'Delete Email Status'.
 * 
 * @author Christian Guretzki
 */
public class StatusController extends BasicController {
	
	private static final String ACTION_SINGLESELECT_CHOOSE = "ssc";
	
	private VelocityContainer myContent;
	private Panel userDeleteStatusPanel;
	private TableController tableCtr;
	private UserDeleteTableModel tdm;
	
	private boolean isAdministrativeUser;
	private Translator propertyHandlerTranslator;
	private List<OrganisationRef> manageableOrganisations;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private UserDeletionManager userDeletionManager;

	/**
	 * @param ureq
	 * @param wControl
	 * @param cancelbutton
	 */
	public StatusController(UserRequest ureq, WindowControl wControl, List<OrganisationRef> manageableOrganisations) {		
		super(ureq, wControl);
		this.manageableOrganisations = manageableOrganisations;
		Translator fallbackTrans = Util.createPackageTranslator(UserSearchController.class, getLocale());
		setTranslator(Util.createPackageTranslator(StatusController.class, getLocale(), fallbackTrans));
    //	use the PropertyHandlerTranslator	as tableCtr translator
		propertyHandlerTranslator = userManager.getPropertyHandlerTranslator(getTranslator());
		
		myContent = createVelocityContainer("deletestatus");
		
		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);

		userDeleteStatusPanel = new Panel("userDeleteStatusPanel");
		userDeleteStatusPanel.addListener(this);
		myContent.put("userDeleteStatusPanel", userDeleteStatusPanel);
		myContent.contextPut("header", getTranslator().translate("status.delete.email.header", 
				new String [] { Integer.toString(userDeletionManager.getDeleteEmailDuration()) }));

		initializeTableController(ureq);
		loadModel();
		
		putInitialPanel(myContent);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				if (te.getActionId().equals(ACTION_SINGLESELECT_CHOOSE)) {
					int rowid = te.getRowId();
					doActivate(tdm.getObject(rowid));
				}
			} 
		} 
	}
	
	private void doActivate(Identity identity) {
		userDeletionManager.setIdentityAsActiv(identity);
		loadModel();
	}

	private void initializeTableController(UserRequest ureq) {
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("error.no.user.found"));
		
		removeAsListenerAndDispose(tableCtr);
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), propertyHandlerTranslator);
		listenTo(tableCtr);
				
		tdm = new UserDeleteTableModel(Collections.emptyList(), ureq.getLocale(), isAdministrativeUser);
		tdm.addColumnDescriptors(tableCtr, null,"table.identity.deleteEmail");	
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor(ACTION_SINGLESELECT_CHOOSE, "table.header.action", translate("action.activate")));
		
		tableCtr.setMultiSelect(false);
		tableCtr.setTableDataModel(tdm);
		userDeleteStatusPanel.setContent(tableCtr.getInitialComponent());
	}

	protected void loadModel() {
		List<Identity> l = userDeletionManager.getIdentitiesInDeletionProcess(userDeletionManager.getDeleteEmailDuration(),
				manageableOrganisations);		
		tdm.setObjects(l); 
		tableCtr.setTableDataModel(tdm);			
	}
	
	@Override
	protected void doDispose() {
		//
	}

}
