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
package org.olat.modules.immunityproof.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.Logger;
import org.olat.admin.user.UserSearchFlexiController;
import org.olat.admin.user.UserSearchFlexiTableModel;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.basesecurity.manager.GroupDAO;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.immunityproof.ImmunityProof;
import org.olat.modules.immunityproof.ImmunityProofModule;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 08.09.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ImmunityProofManageCommissionersController extends FormBasicController {
	
	private static final Logger log = Tracing.createLoggerFor(ImmunityProofManageCommissionersController.class);
	
	private FormLink addCommissioners;
	
	private FlexiTableElement tableEl;
	private UserSearchFlexiTableModel tableModel;
	private List<UserPropertyHandler> userPropertyHandlers;
	
	private UserSearchFlexiController userSearchController;
	private CloseableModalController cmc;
	
	@Autowired
	private GroupDAO groupDAO;
	@Autowired
	private ImmunityProofModule immunityProofModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private I18nManager i18nManager;

	public ImmunityProofManageCommissionersController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "immunity_proof_commissioners");
		
		setTranslator(Util.createPackageTranslator(ImmunityProof.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(ImmunityProofModule.USER_PROPERTY_HANDLER, isAdministrativeUser);
		
		initForm(ureq);
		loadData();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addCommissioners = uifactory.addFormLink("addCommissioners", "commissioners.add", null, formLayout, Link.BUTTON);
		addCommissioners.setIconLeftCSS("o_icon o_icon_fw o_icon_add");
		
		int colPos = 0;
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(ImmunityProofModule.USER_PROPERTY_HANDLER , userPropertyHandler);

			FlexiColumnModel col;
			if(UserConstants.FIRSTNAME.equals(propName) || UserConstants.LASTNAME.equals(propName)) {
				col = new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(),
						colPos, null, true, propName,
						new StaticFlexiCellRenderer(null, new TextFlexiCellRenderer()));
			} else {
				col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, true, propName);
			}
			columnsModel.addFlexiColumnModel(col);
			colPos++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("remove.commissioner", translate("remove.commissioner"), "remove.commissioner"));
		
		tableModel = new UserSearchFlexiTableModel(null, userPropertyHandlers, getLocale(), columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "commissionersTable", tableModel, getTranslator(), formLayout);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == addCommissioners) {
			doAddCommissioners(ureq);
		} else if (source == tableEl) {
			if (event instanceof SelectionEvent) {
				if (event.getCommand().equals("remove.commissioner")) {
					Identity commissionerToRemove = tableModel.getObject(((SelectionEvent) event).getIndex());
					
					removeCommissioner(commissionerToRemove);
					loadData();
				}
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == userSearchController) {
			List<Identity> identitiesToAdd = new ArrayList<>();
			
			if (event instanceof SingleIdentityChosenEvent) {
				identitiesToAdd.add(((SingleIdentityChosenEvent) event).getChosenIdentity());
			} else if (event instanceof MultiIdentityChosenEvent) {
				identitiesToAdd.addAll(((MultiIdentityChosenEvent) event).getChosenIdentities());
			}
			
			addNewCommissioners(identitiesToAdd);
			loadData();
						
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		if (cmc != null && cmc.isCloseable()) {
			cmc.deactivate();
		}
		
		removeAsListenerAndDispose(userSearchController);
		removeAsListenerAndDispose(cmc);
		
		userSearchController = null;
		cmc = null;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		// Nothing to do here
	}

	@Override
	protected void doDispose() {
			
	}
	
	private void loadData() {
		if (immunityProofModule.getCommissionersGroupKey() != null) {
			Group commissionersGroup = groupDAO.loadGroup(immunityProofModule.getCommissionersGroupKey());
			
			if (commissionersGroup != null) {
				tableModel.setObjects(groupDAO.getMembers(commissionersGroup, ImmunityProofModule.IMMUNITY_PROOF_COMMISSIONER_ROLE));
				tableEl.reset();
			}
		}
	}
	
	private void doAddCommissioners(UserRequest ureq) {
		userSearchController = new UserSearchFlexiController(ureq, getWindowControl(), null, true);
		listenTo(userSearchController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("cancel"), userSearchController.getInitialComponent(), true, translate("commissioners.add"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void addNewCommissioners(List<Identity> newCommissioners) {
		Group commissionersGroup = null;
		
		// Check if group exits
		if (immunityProofModule.getCommissionersGroupKey() != null) {
			commissionersGroup = groupDAO.loadGroup(immunityProofModule.getCommissionersGroupKey());
		} 

		if (commissionersGroup == null) {
			commissionersGroup = groupDAO.createGroup("immunity_proof_commissioners");
			immunityProofModule.setCommissionersGroupKey(commissionersGroup.getKey());
		}
		
		List<Identity> commissioners = groupDAO.getMembers(commissionersGroup, ImmunityProofModule.IMMUNITY_PROOF_COMMISSIONER_ROLE);
		
		for (Identity newCommissioner : newCommissioners) {
			if (commissioners.contains(newCommissioner)) {
				continue;
			}
			
			groupDAO.addMembershipTwoWay(commissionersGroup, newCommissioner, ImmunityProofModule.IMMUNITY_PROOF_COMMISSIONER_ROLE);
			
			// Send mail
			User user = newCommissioner.getUser();
			Locale userLocale = i18nManager.getLocaleOrDefault(user.getPreferences().getLanguage()); 
			sendMail(getTranslator(), mailManager, user, userLocale, true);
		}		
	}
	
	private void removeCommissioner(Identity commissionerToRemove) {
		if (commissionerToRemove == null) {
			return;
		}
		
		Group commissionersGroup = null;
		
		// Check if group exits
		if (immunityProofModule.getCommissionersGroupKey() != null) {
			commissionersGroup = groupDAO.loadGroup(immunityProofModule.getCommissionersGroupKey());
		} else {
			return;
		}
		
		// Remove membership
		groupDAO.removeMembership(commissionersGroup, commissionerToRemove);
		
		// Send mail
		User user = commissionerToRemove.getUser();
		Locale userLocale = i18nManager.getLocaleOrDefault(user.getPreferences().getLanguage()); 
		sendMail(getTranslator(), mailManager, user, userLocale, false);
	}
	
	private boolean sendMail(Translator translator, MailManager mailManager, User user, Locale userLocale, boolean added) {
		String name = "";
		String url = Settings.createServerURI() + "/url/CovidCertificates/0";
		
		if (StringHelper.containsNonWhitespace(user.getFirstName())) {
			name += user.getFirstName();
		}
		
		if (StringHelper.containsNonWhitespace(user.getLastName())) {
			name += " " + user.getLastName();
		}
		
		if (!StringHelper.containsNonWhitespace(name)) {
			name = user.getNickName();
		}
		
		String[] params = new String[] {name, url};	
		
        String subject = translator.translate("immunity.proof.commissioner." + (added ? "added":"removed") + ".mail.subject");
        String body = translator.translate("immunity.proof.commissioner." + (added ? "added":"removed") + ".mail.body", params);
        
        System.out.println("params" + params);
        System.out.println("body" + body);
        String decoratedBody = mailManager.decorateMailBody(body, userLocale);
        String recipientAddress = user.getEmail();
        Address from;
        Address[] to;

        try {
            from = new InternetAddress(WebappHelper.getMailConfig("mailSupport"));
            to = new Address[] {new InternetAddress(((recipientAddress)))};
        } catch (AddressException e) {
            log.error("Could not send COVID commissioner notification message, bad mail address", e);
            return false;
        }

        MailerResult result = new MailerResult();
        MimeMessage msg = mailManager.createMimeMessage(from, to, null, null, subject, decoratedBody, null, result);
        mailManager.sendMessage(msg, result);
        if (!result.isSuccessful()) {
            log.error("Could not send COVID commissioner message to " + recipientAddress);
            return false;
        }
        
        return true;
    }

}
