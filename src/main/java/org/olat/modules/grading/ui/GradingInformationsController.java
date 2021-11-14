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
package org.olat.modules.grading.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
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
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.grading.GraderToIdentity;
import org.olat.modules.grading.GradingService;
import org.olat.modules.grading.RepositoryEntryGradingConfiguration;
import org.olat.modules.grading.ui.GradingInformationsTableModel.GInfosCol;
import org.olat.modules.grading.ui.component.GraderAbsenceLeaveCellRenderer;
import org.olat.modules.grading.ui.component.GraderStatusCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.user.AbsenceLeave;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradingInformationsController extends FormBasicController {
	
	public static final String USER_PROPS_ID = GradersListController.USER_PROPS_ID;

	public static final int USER_PROPS_OFFSET = 500;
	
	private FormLink openLink;
	private FormLink contactLink;
	private FlexiTableElement tableEl;
	private GradingInformationsTableModel tableModel;
	
	private RepositoryEntry testEntry;
	private List<UserPropertyHandler> userPropertyHandlers;
	
	private CloseableModalController cmc;
	private ContactFormController contactCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private GradingService gradingService;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private RepositoryService repositoryService;

	public GradingInformationsController(UserRequest ureq, WindowControl wControl, RepositoryEntry testEntry) {
		super(ureq, wControl, "grading_infos");
		
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.testEntry = testEntry;
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		contactLink = uifactory.addFormLink("contact", "configuration.informations.resource.contact", null, formLayout, Link.LINK);
		contactLink.setVisible(false);
		openLink = uifactory.addFormLink("open", "configuration.informations.resource.open", null, formLayout, Link.LINK);
		openLink.setVisible(false);
		
		initGradersTable(formLayout);
	}
	
	protected void initGradersTable(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		int colPos = USER_PROPS_OFFSET;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);

			FlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, true, propName);
			columnsModel.addFlexiColumnModel(col);
			colPos++;
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GInfosCol.status, new GraderStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GInfosCol.absence, new GraderAbsenceLeaveCellRenderer(getTranslator())));

		tableModel = new GradingInformationsTableModel(columnsModel, userPropertyHandlers, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "graders", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
	}
	
	public void reloadRepositoryEntry(RepositoryEntry entry) {
		this.testEntry = entry;
		loadModel();
	}
	
	private void loadModel() {
		List<GradingInformationsRow> rows;
		if(testEntry == null) {
			rows = new ArrayList<>();
			flc.contextRemove("visibility");
			flc.contextRemove("notification");
			flc.contextRemove("gradingPeriod");
			flc.contextRemove("firstReminder");
			flc.contextRemove("secondReminder");
			contactLink.setVisible(false);
			openLink.setVisible(false);
		} else {
			rows = loadModelFromEntry(testEntry);
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private List<GradingInformationsRow> loadModelFromEntry(RepositoryEntry entry) {
		RepositoryEntryGradingConfiguration configuration = gradingService.getOrCreateConfiguration(entry);
		
		String visibilityValue;
		switch(configuration.getIdentityVisibilityEnum()) {
			case anonymous: visibilityValue = translate("configuration.assessed.identity.anonyme"); break;
			case nameVisible: visibilityValue = translate("configuration.assessed.identity.name.visible"); break;
			default: visibilityValue = "ERROR"; break;
		}
		flc.contextPut("visibility", visibilityValue);
		
		String notificationValue;
		switch(configuration.getNotificationTypeEnum()) {
			case afterTestSubmission: notificationValue = translate("configuration.notification.afterTestSubmission"); break;
			case onceDay: notificationValue = translate("configuration.notification.onceDay"); break;
			default: notificationValue = "ERROR"; break;
		}
		flc.contextPut("notification", notificationValue);
		flc.contextPut("gradingPeriod", configuration.getGradingPeriod());
		flc.contextPut("firstReminder", configuration.getFirstReminder());
		flc.contextPut("secondReminder", configuration.getSecondReminder());

		flc.contextPut("externalId", entry.getOlatResource().getResourceableId());
		
		List<Identity> owners = repositoryService.getMembers(entry, RepositoryEntryRelationType.defaultGroup, GroupRoles.owner.name());
		contactLink.setVisible(!owners.isEmpty());
		StringBuilder sb = new StringBuilder(128);
		for(Identity owner:owners) {
			if(sb.length() > 0) sb.append(", ");
			
			String firstName = owner.getUser().getProperty(UserConstants.FIRSTNAME, getLocale());
			String lastName = owner.getUser().getProperty(UserConstants.LASTNAME, getLocale());
			if(StringHelper.containsNonWhitespace(firstName)) {
				sb.append(firstName);
			}
			if(StringHelper.containsNonWhitespace(lastName)) {
				sb.append(" ").append(lastName);
			}
		}
		flc.contextPut("owners", sb.toString());

		openLink.setVisible(true);
		flc.contextPut("resourceDisplayname", entry.getDisplayname());
		
		List<GraderToIdentity> graders = gradingService.getGraders(entry);
		Map<Long,GradingInformationsRow> rows = graders.stream()
				.map(grader -> new GradingInformationsRow(grader.getIdentity(), grader.getGraderStatus()))
				.collect(Collectors.toMap(GradingInformationsRow::getKey, Function.identity(), (u, v) -> u));
		List<AbsenceLeave> gradersAbsenceLeaves = gradingService.getGradersAbsenceLeaves(entry);			
		for(AbsenceLeave graderAbsenceLeave:gradersAbsenceLeaves) {
			GradingInformationsRow row = rows.get(graderAbsenceLeave.getIdentity().getKey());
			if(row != null) {
				row.addAbsenceLeave(graderAbsenceLeave);
			}
		}
		return new ArrayList<>(rows.values());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(contactCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeControllerListener(contactCtrl);
		removeAsListenerAndDispose(cmc);
		contactCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(openLink == source) {
			doOpen(ureq);
		} else if(contactLink == source) {
			doContact(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOpen(UserRequest ureq) {
		if(testEntry == null) return;
		
		String businessPath = "[RepositoryEntry:" + testEntry.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doContact(UserRequest ureq) {
		ContactMessage cmsg = new ContactMessage(getIdentity());
		List<Identity> owners = repositoryService.getMembers(testEntry, RepositoryEntryRelationType.defaultGroup, GroupRoles.owner.name());
		ContactList ownersList = new ContactList(translate("configuration.informations.resource.owners"));
		ownersList.addAllIdentites(owners);
		cmsg.addEmailTo(ownersList);

		contactCtrl = new ContactFormController(ureq, getWindowControl(), true, false, false, cmsg);
		contactCtrl.getAndRemoveTitle();
		listenTo(contactCtrl);
		
		String title = translate("contact.owners.title");
		cmc = new CloseableModalController(getWindowControl(), "close", contactCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
}
