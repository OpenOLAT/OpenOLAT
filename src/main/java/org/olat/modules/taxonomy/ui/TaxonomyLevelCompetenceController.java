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
package org.olat.modules.taxonomy.ui;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyCompetenceAuditLog;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelManagedFlag;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyLevelCompetenceTableModel.CompetenceCols;
import org.olat.modules.taxonomy.ui.component.TaxonomyCompetenceTypeRenderer;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Oct 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyLevelCompetenceController extends FormBasicController {
	
	protected static final String USER_PROPS_ID = TaxonomyLevelCompetenceController.class.getCanonicalName();
	
	public static final int USER_PROPS_OFFSET = 500;
	
	private FlexiTableElement tableEl;
	private TaxonomyLevelCompetenceTableModel tableModel;
	private FormLink addManageButton, addTeachButton, addHaveButton, addTargetButton, okButton;
	
	private CloseableModalController cmc;
	private UserSearchController userSearchCtrl;

	private TaxonomyLevel taxonomyLevel;
	private boolean isAdministrativeUser;
	private List<UserPropertyHandler> userPropertyHandlers;

	@Autowired
	private UserManager userManager;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public TaxonomyLevelCompetenceController(UserRequest ureq, WindowControl wControl, TaxonomyLevel taxonomyLevel) {
		super(ureq, wControl, "level_competences");
		this.taxonomyLevel = taxonomyLevel;
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(!TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.manageComptence)) {
			addManageButton = uifactory.addFormLink("add.competence.manage", formLayout, Link.BUTTON);
		}
		if(!TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.teachComptence)) {
			addTeachButton = uifactory.addFormLink("add.competence.teach", formLayout, Link.BUTTON);
		}
		if(!TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.haveComptence)) {
			addHaveButton = uifactory.addFormLink("add.competence.have", formLayout, Link.BUTTON);
		}
		if(!TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.targetComptence)) {
			addTargetButton = uifactory.addFormLink("add.competence.target", formLayout, Link.BUTTON);
		}
		
		okButton = uifactory.addFormLink("ok", formLayout, Link.BUTTON);
		
		// table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CompetenceCols.username));
		}
		
		int colPos = USER_PROPS_OFFSET;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);

			FlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, true, propName);
			columnsModel.addFlexiColumnModel(col);
			colPos++;
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CompetenceCols.type, new TaxonomyCompetenceTypeRenderer(getTranslator())));
		
		tableModel = new TaxonomyLevelCompetenceTableModel(columnsModel); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
	}
	
	private void loadModel() {
		List<TaxonomyCompetence> competences = taxonomyService.getTaxonomyLevelCompetences(taxonomyLevel);
		List<TaxonomyLevelCompetenceRow> rows = competences.stream()
				.map(c -> new TaxonomyLevelCompetenceRow(c, userPropertyHandlers, getLocale()))
				.collect(Collectors.toList());
		tableModel.setObjects(rows);
		tableEl.reset(false, true, true);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addManageButton == source) {
			doSearchUsersToAdd(ureq, TaxonomyCompetenceTypes.manage);
		} else if(addTeachButton == source) {
			doSearchUsersToAdd(ureq, TaxonomyCompetenceTypes.teach);
		} else if(addHaveButton == source) {
			doSearchUsersToAdd(ureq, TaxonomyCompetenceTypes.have);
		} else if(addTargetButton == source) {
			doSearchUsersToAdd(ureq, TaxonomyCompetenceTypes.target);
		} else if(okButton == source) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(userSearchCtrl == source) {
			if (event instanceof SingleIdentityChosenEvent) {
				SingleIdentityChosenEvent singleEvent = (SingleIdentityChosenEvent)event;
				Identity choosenIdentity = singleEvent.getChosenIdentity();
				if (choosenIdentity != null) {
					List<Identity> toAdd = Collections.singletonList(choosenIdentity);
					doAddCompetence(toAdd, (TaxonomyCompetenceTypes)userSearchCtrl.getUserObject());
					loadModel();
				}
			} else if (event instanceof MultiIdentityChosenEvent) {
				MultiIdentityChosenEvent multiEvent = (MultiIdentityChosenEvent)event;
				List<Identity> toAdd = multiEvent.getChosenIdentities();
				if(toAdd.size() > 0) {
					doAddCompetence(toAdd, (TaxonomyCompetenceTypes)userSearchCtrl.getUserObject());
					loadModel();
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(userSearchCtrl);
		removeAsListenerAndDispose(cmc);
		userSearchCtrl = null;
		cmc = null;
	}

	private void doSearchUsersToAdd(UserRequest ureq, TaxonomyCompetenceTypes comptenceType) {
		if(userSearchCtrl != null) return;
		
		userSearchCtrl = new UserSearchController(ureq, getWindowControl(), true, true, false);
		userSearchCtrl.setUserObject(comptenceType);
		listenTo(userSearchCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), userSearchCtrl.getInitialComponent(),
				true, translate("add.competence." + comptenceType.name()));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddCompetence(List<Identity> identities, TaxonomyCompetenceTypes comptenceType) {
		Taxonomy taxonomy = taxonomyLevel.getTaxonomy();
		for(Identity identity:identities) {
			TaxonomyCompetence competence = taxonomyService.addTaxonomyLevelCompetences(taxonomyLevel, identity, comptenceType);
			String after = taxonomyService.toAuditXml(competence);
			taxonomyService.auditLog(TaxonomyCompetenceAuditLog.Action.addCompetence, null, after, null, taxonomy, competence, identity, getIdentity());
		}
	}
}