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
package org.olat.modules.portfolio.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.olat.basesecurity.Invitation;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioElement;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.manager.InvitationDAO;
import org.olat.modules.portfolio.model.AccessRightChange;
import org.olat.modules.portfolio.model.AccessRights;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 29.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationEditRightsController extends FormBasicController {
	
	private static final String[] theKeys = new String[]{ "xx" };
	private static final String[] theValues = new String[]{ "" };
	
	private FormLink removeLink;
	private FormLink selectAll, deselectAll;
	private TextElement subjectEl, bodyEl;
	private TextElement firstNameEl, lastNameEl, mailEl;
	
	private int counter;

	private String email;
	private Binder binder;
	private Identity invitee;
	private Invitation invitation;
	private MailTemplate mailTemplate;
	private BinderAccessRightsRow binderRow;
	
	@Autowired
	private MailManager mailManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private InvitationDAO invitationDao;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private OrganisationService organisationService;
	
	public InvitationEditRightsController(UserRequest ureq, WindowControl wControl, Binder binder, String email, Identity existingInvitee) {
		super(ureq, wControl, "invitee_access_rights");
		this.email = email;
		this.binder = binder;
		if(existingInvitee != null) {
			invitee = existingInvitee;
		} else {
			invitee = userManager.findUniqueIdentityByEmail(email);
		}
		
		if(invitee != null) {
			invitation = invitationDao.findInvitation(binder.getBaseGroup(), invitee);
		} 
		if(invitation == null) {
			invitation = invitationDao.createInvitation();
			if(invitee != null) {
				invitation.setFirstName(invitee.getUser().getFirstName());
				invitation.setLastName(invitee.getUser().getLastName());
			}
		}
		
		String busLink = getInvitationLink();
		String sender = userManager.getUserDisplayName(getIdentity());
		String[] args = new String[] {
			busLink,								// 0
			sender,									// 1
			getIdentity().getUser().getFirstName(),	// 2
			getIdentity().getUser().getLastName()	// 3
		};
		
		String subject = translate("invitation.extern.mail.subject", args);
		String body = translate("invitation.extern.mail.body", args);
		mailTemplate = new MailTemplate(subject, body, null) {
			@Override
			public void putVariablesInMailContext(VelocityContext vContext, Identity recipient) {
				//
			}
		};
		
		initForm(ureq);
		loadModel();
	}
	
	public InvitationEditRightsController(UserRequest ureq, WindowControl wControl, Binder binder, Identity invitee) {
		super(ureq, wControl, "invitee_access_rights");
		this.binder = binder;
		this.invitee = invitee;
		invitation = invitationDao.findInvitation(binder.getBaseGroup(), invitee);
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_pf_invitation_form");
		FormLayoutContainer inviteeCont = FormLayoutContainer.createDefaultFormLayout("inviteeInfos", getTranslator());
		inviteeCont.setRootForm(mainForm);
		formLayout.add("inviteeInfos", inviteeCont);
		
		firstNameEl = uifactory.addTextElement("firstName", "firstName", 64, invitation.getFirstName(), inviteeCont);
		firstNameEl.setElementCssClass("o_sel_pf_invitation_firstname");
		firstNameEl.setMandatory(true);
		
		lastNameEl = uifactory.addTextElement("lastName", "lastName", 64, invitation.getLastName(), inviteeCont);
		lastNameEl.setElementCssClass("o_sel_pf_invitation_lastname");
		lastNameEl.setMandatory(true);
		
		String invitationEmail = email != null ? email : invitation.getMail();
		mailEl = uifactory.addTextElement("mail", "mail", 128, invitationEmail, inviteeCont);
		mailEl.setElementCssClass("o_sel_pf_invitation_mail");
		mailEl.setMandatory(true);
		mailEl.setNotEmptyCheck("map.share.empty.warn");
		mailEl.setEnabled(invitation.getKey() == null);
			
		if(StringHelper.containsNonWhitespace(invitation.getMail()) && MailHelper.isValidEmailAddress(invitation.getMail())) {
			List<Identity> shareWithIdentities = userManager.findIdentitiesByEmail(Collections.singletonList(invitation.getMail()));
			if (isAtLeastOneUser(shareWithIdentities)) {
				mailEl.setErrorKey("map.share.with.mail.error.olatUser", new String[]{ invitation.getMail() });
			}
		}
			
		String link = getInvitationLink();
		StaticTextElement linkEl = uifactory.addStaticTextElement("invitation.link" , link, inviteeCont);
		linkEl.setElementCssClass("o_sel_pf_invitation_url");
		linkEl.setLabel("invitation.link", null);
		
		if(mailTemplate != null) {
			subjectEl = uifactory.addTextElement("subjectElem", "mail.subject", 128, mailTemplate.getSubjectTemplate(), inviteeCont);
			subjectEl.setDisplaySize(60);
			subjectEl.setMandatory(true);
		
			bodyEl = uifactory.addTextAreaElement("bodyElem", "mail.body", -1, 15, 60, true, false, mailTemplate.getBodyTemplate(), inviteeCont);
			bodyEl.setHelpUrlForManualPage("E-Mail");
			bodyEl.setMandatory(true);
		}
		
		//binder
		MultipleSelectionElement accessEl = uifactory.addCheckboxesHorizontal("access-" + (counter++), null, formLayout, theKeys, theValues);
		accessEl.addActionListener(FormEvent.ONCHANGE);
		binderRow = new BinderAccessRightsRow(accessEl, binder);
		
		//sections
		List<Section> sections = portfolioService.getSections(binder);
		Map<Long,SectionAccessRightsRow> sectionMap = new HashMap<>();
		for(Section section:sections) {
			MultipleSelectionElement sectionAccessEl = uifactory.addCheckboxesHorizontal("access-" + (counter++), null, formLayout, theKeys, theValues);
			sectionAccessEl.addActionListener(FormEvent.ONCHANGE);
			SectionAccessRightsRow sectionRow = new SectionAccessRightsRow(sectionAccessEl, section, binderRow);
			binderRow.getSections().add(sectionRow);
			sectionMap.put(section.getKey(), sectionRow);	
		}
		
		//pages
		List<Page> pages = portfolioService.getPages(binder, null);
		for(Page page:pages) {
			Section section = page.getSection();
			SectionAccessRightsRow sectionRow = sectionMap.get(section.getKey());
			
			MultipleSelectionElement pageAccessEl = uifactory.addCheckboxesHorizontal("access-" + (counter++), null, formLayout, theKeys, theValues);
			pageAccessEl.addActionListener(FormEvent.ONCHANGE);
			PortfolioElementAccessRightsRow pageRow = new PortfolioElementAccessRightsRow(pageAccessEl, page, sectionRow);
			sectionRow.getPages().add(pageRow);
		}
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("binderRow", binderRow);
		}
		
		selectAll = uifactory.addFormLink("form.checkall", "form.checkall", null, formLayout, Link.LINK);
		selectAll.setIconLeftCSS("o_icon o_icon-sm o_icon_check_on");
		deselectAll = uifactory.addFormLink("form.uncheckall", "form.uncheckall", null, formLayout, Link.LINK);
		deselectAll.setIconLeftCSS("o_icon o_icon-sm o_icon_check_off");

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		buttonsCont.setRootForm(mainForm);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		if(invitation.getKey() != null) {
			removeLink = uifactory.addFormLink("remove.all.rights", buttonsCont, Link.BUTTON);
		}
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private String getInvitationLink() {
		return Settings.getServerContextPathURI() + "/url/BinderInvitation/" + binder.getKey() + "?invitation=" + invitation.getToken();
	}
	
	private void loadModel() {
		if(invitee != null) {
			List<AccessRights> currentRights = portfolioService.getAccessRights(binder, invitee);
			
			binderRow.applyRights(currentRights);
			for(SectionAccessRightsRow sectionRow:binderRow.getSections()) {
				sectionRow.applyRights(currentRights);
				for(PortfolioElementAccessRightsRow pageRow:sectionRow.getPages()) {
					pageRow.applyRights(currentRights);
				}
			}
			
			binderRow.recalculate();
		}
	}
	
	@Override
	protected void doDispose() {
		// 
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
	
		mailEl.clearError();
		if (mailEl != null) {
			String mail = mailEl.getValue();
			if (StringHelper.containsNonWhitespace(mail)) {
				if (MailHelper.isValidEmailAddress(mail)) {
					List<Identity> shareWithIdentities = userManager.findIdentitiesByEmail(Collections.singletonList(mail));
					if (isAtLeastOneUser(shareWithIdentities)) {
						mailEl.setErrorKey("map.share.with.mail.error.olatUser", new String[] { mail });
						allOk &= false;
					}
				} else {
					mailEl.setErrorKey("error.mail.invalid", null);
					allOk &= false;
				}
			} else {
				mailEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		firstNameEl.clearError();
		if (!StringHelper.containsNonWhitespace(firstNameEl.getValue())) {
			firstNameEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		lastNameEl.clearError();
		if (!StringHelper.containsNonWhitespace(lastNameEl.getValue())) {
			lastNameEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean isAtLeastOneUser(Collection<Identity> identites) {
		for (Identity identity: identites) {
			if (organisationService.hasRole(identity, OrganisationRoles.user)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<AccessRightChange> changes = getChanges();
		
		if(invitation.getKey() == null) {
			invitation.setFirstName(firstNameEl.getValue());
			invitation.setLastName(lastNameEl.getValue());
			invitation.setMail(mailEl.getValue());
			invitee = invitationDao.loadOrCreateIdentityAndPersistInvitation(invitation, binder.getBaseGroup(), getLocale());
			portfolioService.changeAccessRights(Collections.singletonList(invitee), changes);
			sendInvitation();
			fireEvent(ureq, Event.DONE_EVENT);
		} else {
			invitationDao.update(invitation, firstNameEl.getValue(), lastNameEl.getValue(), mailEl.getValue());
			portfolioService.changeAccessRights(Collections.singletonList(invitee), changes);
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
	
	public List<AccessRightChange> getChanges() {
		List<AccessRightChange> changes = new ArrayList<>();
		binderRow.appendChanges(changes, invitee);
		for(SectionAccessRightsRow sectionRow:binderRow.getSections()) {
			sectionRow.appendChanges(changes, invitee);
			for(PortfolioElementAccessRightsRow pageRow:sectionRow.getPages()) {
				pageRow.appendChanges(changes, invitee);
			}
		}
		return changes;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(removeLink == source) {
			doRemoveInvitation();
			fireEvent(ureq, Event.DONE_EVENT);
		} else if(selectAll == source) {
			binderRow.setAccessible();
			binderRow.recalculate();
		} else if(deselectAll == source) {
			binderRow.unsetAccessible();
			for(SectionAccessRightsRow sectionRow:binderRow.getSections()) {
				sectionRow.unsetAccessible();
				for(PortfolioElementAccessRightsRow pageRow:sectionRow.getPages()) {
					pageRow.unsetAccessible();
				}
			}
		} else if(source instanceof MultipleSelectionElement) {
			binderRow.recalculate();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doRemoveInvitation() {
		portfolioService.removeAccessRights(binder, invitee,
				PortfolioRoles.invitee, PortfolioRoles.readInvitee);
		invitationDao.deleteInvitation(invitation);
	}

	private void sendInvitation() {
		String inviteeEmail = invitee.getUser().getProperty(UserConstants.EMAIL, getLocale());
		ContactList contactList = new ContactList(inviteeEmail);
		contactList.add(inviteeEmail);

		boolean success = false;
		try {
			mailTemplate.setSubjectTemplate(subjectEl.getValue());
			mailTemplate.setBodyTemplate(bodyEl.getValue());

			MailContext context = new MailContextImpl(binder, null, getWindowControl().getBusinessControl().getAsString()); 
			MailBundle bundle = new MailBundle();
			bundle.setContext(context);
			bundle.setFromId(getIdentity());
			bundle.setContactList(contactList);
			bundle.setContent(subjectEl.getValue(), bodyEl.getValue());

			MailerResult result = mailManager.sendExternMessage(bundle, null, true);
			success = result.isSuccessful();
		} catch (Exception e) {
			logError("Error on sending invitation mail to contactlist, invalid address.", e);
		}
		if (success) {
			showInfo("invitation.mail.success");
		}	else {
			showError("invitation.mail.failure");			
		}
	}
	
	public static class BinderAccessRightsRow extends PortfolioElementAccessRightsRow {
		
		private final List<SectionAccessRightsRow> sections = new ArrayList<>();

		public BinderAccessRightsRow(MultipleSelectionElement accessEl, PortfolioElement element) {
			super(accessEl, element, null);
		}

		public List<SectionAccessRightsRow> getSections() {
			return sections;
		}
		
		@Override
		public void recalculate() {
			super.recalculate();
			
			if(sections != null) {
				if(isAccessible()) {
					for(SectionAccessRightsRow section:sections) {
						section.setAccessible();
					}
				}
				for(SectionAccessRightsRow section:sections) {
					section.recalculate();
				}
			}
		}
		
		@Override
		public void appendChanges(List<AccessRightChange> changes, Identity identity) {
			if(isAccessible()) {
				changes.add(new AccessRightChange(PortfolioRoles.readInvitee, getElement(), identity, true));
			} else if(accessRight != null) {
				changes.add(new AccessRightChange(PortfolioRoles.readInvitee, getElement(), identity, false));
			}
		}
	}
	
	public static class SectionAccessRightsRow extends PortfolioElementAccessRightsRow {
		
		private final List<PortfolioElementAccessRightsRow> pages = new ArrayList<>();
		
		public SectionAccessRightsRow(MultipleSelectionElement accessEl, PortfolioElement element, BinderAccessRightsRow parentRow) {
			super(accessEl, element, parentRow);
		}
		
		@Override
		public void recalculate() {
			super.recalculate();
			
			if(pages != null) {
				if(isAccessible()) {
					for(PortfolioElementAccessRightsRow page:pages) {
						page.setAccessible();
					}
				}
			}
		}
		
		@Override
		public void appendChanges(List<AccessRightChange> changes, Identity identity) {
			if(isAccessible() && !getParentRow().isAccessible()) {
				changes.add(new AccessRightChange(PortfolioRoles.readInvitee, getElement(), identity, true));
			} else if(accessRight != null) {
				changes.add(new AccessRightChange(PortfolioRoles.readInvitee, getElement(), identity, false));
			}
		}
		
		public List<PortfolioElementAccessRightsRow> getPages() {
			return pages;
		}
	}
	
	public static class PortfolioElementAccessRightsRow {
		
		private final PortfolioElement element;
		private final MultipleSelectionElement accessEl;
		
		protected AccessRights accessRight;
		private final PortfolioElementAccessRightsRow parentRow;
		
		public PortfolioElementAccessRightsRow(MultipleSelectionElement accessEl,
				PortfolioElement element, PortfolioElementAccessRightsRow parentRow) {
			this.element = element;
			this.accessEl = accessEl;
			this.parentRow = parentRow;
			accessEl.setUserObject(Boolean.FALSE);
		}
		
		public void recalculate() {
			//do nothing
		}
		
		public void appendChanges(List<AccessRightChange> changes, Identity identity) {
			if(accessEl.isAtLeastSelected(1) && !parentRow.isAccessible() && !parentRow.getParentRow().isAccessible()) {
				changes.add(new AccessRightChange(PortfolioRoles.readInvitee, element, identity, true));
			} else if(accessRight != null) {
				changes.add(new AccessRightChange(PortfolioRoles.readInvitee, element, identity, false));
			}
		}
		
		public void applyRights(List<AccessRights> rights) {
			for(AccessRights right:rights) {
				if(element instanceof Page) {
					if(element.getKey().equals(right.getPageKey())) {
						applyRight(right);
					}
				} else if(element instanceof Section) {
					if(element.getKey().equals(right.getSectionKey()) && right.getPageKey() == null) {
						applyRight(right);
					}
				} else if(element instanceof Binder) {
					if(element.getKey().equals(right.getBinderKey()) && right.getSectionKey() == null && right.getPageKey() == null) {
						applyRight(right);
					}
				}
			}
		}
		
		public void applyRight(AccessRights right) {
			if(right.getRole().equals(PortfolioRoles.readInvitee)) {
				accessEl.select("xx", true);
				accessRight = right;
			}
		}
		
		public String getTitle() {
			return element.getTitle();
		}
		
		public PortfolioElementAccessRightsRow getParentRow() {
			return parentRow;
		}

		public PortfolioElement getElement() {
			return element;
		}

		public MultipleSelectionElement getAccess() {
			return accessEl;
		}
		
		public boolean isAccessible() {
			return accessEl.isAtLeastSelected(1);
		}
		
		public void setAccessible() {
			accessEl.select(theKeys[0], true);
		}
		
		public void unsetAccessible() {
			accessEl.uncheckAll();
		}
	}
}
