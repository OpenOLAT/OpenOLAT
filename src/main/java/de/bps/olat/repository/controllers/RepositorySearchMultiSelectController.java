/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
 */
package de.bps.olat.repository.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Roles;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryTableModel;
import org.olat.repository.SearchForm;
import org.olat.repository.controllers.RepositorySearchController;

/**
 * Description:<br>
 * RepositorySearchController with multiselection element.
 * 
 * <P>
 * Initial Date:  04.11.2008 <br>
 * @author bja
 */
public class RepositorySearchMultiSelectController extends RepositorySearchController {
	
	public static final String ACTION_MULTI_SELECT = "action.multi.select";

	/**
	 * @param selectButtonLabel
	 * @param ureq
	 * @param myWControl
	 * @param withCancel
	 * @param enableDirectLaunch
	 * @param limitType
	 */
	public RepositorySearchMultiSelectController(String selectButtonLabel, UserRequest ureq, WindowControl myWControl, boolean withCancel,
			boolean enableDirectLaunch, String limitType) {
		this(selectButtonLabel, ureq, myWControl, withCancel, enableDirectLaunch, limitType, null);
	}
	
	public RepositorySearchMultiSelectController(String selectButtonLabel, UserRequest ureq, WindowControl myWControl, boolean withCancel,
			boolean enableDirectLaunch, String limitType, String limitUser) {
		super(ureq, myWControl);
		
		setBasePackage(RepositoryEntry.class);
		
		init(selectButtonLabel, ureq, withCancel, enableDirectLaunch, limitType, limitUser);
	}
	
	private void init(String selectButtonLabel, UserRequest ureq, boolean withCancel, boolean enableDirectLaunch, String limitType, String limitUser) {
		Roles roles = ureq.getUserSession().getRoles();
		
		vc = createVelocityContainer("search");
		
		BaseSecurity secMgr = BaseSecurityManager.getInstance();
		SecurityGroup usermanagerGroup = secMgr.findSecurityGroupByName(Constants.GROUP_USERMANAGERS);
		boolean isUserManager = secMgr.isIdentityInSecurityGroup(ureq.getIdentity(), usermanagerGroup);
		
		removeAsListenerAndDispose(searchForm);
		searchForm = new SearchForm(ureq, getWindowControl(), withCancel, isUserManager||roles.isOLATAdmin(), limitType, limitUser);
		listenTo(searchForm);
		
		vc.put("searchform",searchForm.getInitialComponent());
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		if (selectButtonLabel != null) tableConfig.setPreferencesOffered(true, "repositorySearchResult");
		
		removeAsListenerAndDispose(tableCtr);
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(tableCtr);
		
		repoTableModel = new RepositoryTableModel(getTranslator());
		repoTableModel.addColumnDescriptors(tableCtr, selectButtonLabel, enableDirectLaunch);
		tableCtr.addMultiSelectAction("resource.table.select", ACTION_MULTI_SELECT);
		tableCtr.setMultiSelect(true);
		tableCtr.setTableDataModel(repoTableModel);
		tableCtr.setSortColumn(2, true);
		vc.put("repotable", tableCtr.getInitialComponent());

		vc.contextPut("isAuthor", Boolean.valueOf(roles.isAuthor()));
		vc.contextPut("withCancel", Boolean.valueOf(withCancel));
		
		enableBackToSearchFormLink(false); // default, must be enabled explicitly
		enableSearchforAllReferencalbeInSearchForm(false); // default
		
		putInitialPanel(vc);
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == tableCtr) {
			fireEvent(urequest, event);
		} else {
			super.event(urequest, source, event);
		}
	}
	
	/**
	 * 
	 * @param row
	 * @return
	 */
	public RepositoryEntry getValueAt(int row) {
		RepositoryEntry repoEntry = (RepositoryEntry)repoTableModel.getObject(row);
		return repoEntry;
	}
	
	/**
	 * Implementation normal search: find all repo entries
	 * @param ureq
	 */
	public void doSearchAll(UserRequest ureq) {
		RepositoryManager rm = RepositoryManager.getInstance();
		Set s = searchForm.getRestrictedTypes();
		List restrictedTypes = (s == null) ? null : new ArrayList(s);
		//fxdiff VCRP-1,2: access control of resources
		List entries = rm.genericANDQueryWithRolesRestriction(null, null, null, null, ureq.getIdentity(), ureq.getUserSession().getRoles(), ureq.getIdentity().getUser().getProperty("institutionalName", null));
		repoTableModel.setObjects(entries);
		tableCtr.modelChanged();
		displaySearchResults(ureq);
	}

}
