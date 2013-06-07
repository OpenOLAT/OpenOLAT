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

package org.olat.portfolio.ui.structel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.Invitation;
import org.olat.basesecurity.Policy;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.textboxlist.ResultMapProvider;
import org.olat.core.gui.components.textboxlist.TextBoxListComponent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.Emailer;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.manager.EPMapPolicy;
import org.olat.portfolio.manager.EPMapPolicy.Type;
import org.olat.portfolio.model.structel.EPStructuredMap;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.user.UserManager;

/**
 * 
 * Description:<br>
 * Manage the list of share policies
 * 
 * <P>
 * Initial Date:  4 nov. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EPShareListController extends FormBasicController {
	
	private final List<PolicyWrapper> policyWrappers = new ArrayList<PolicyWrapper>();
	private final PortfolioStructureMap map;
	private final EPFrontendManager ePFMgr;
	private final BaseSecurity securityManager;
	private final UserManager userManager;
	private final BusinessGroupService businessGroupService;
	private final String[] targetKeys = EPMapPolicy.Type.names();
	private final String[] targetValues = new String[targetKeys.length];
	protected final List<BusinessGroup> groupList = new ArrayList<BusinessGroup>();
	
	private FormLink addPolicyButton;
	
	public EPShareListController(UserRequest ureq, WindowControl wControl, PortfolioStructureMap map) {
		super(ureq, wControl, "shareList");
		
		this.map = map;
		ePFMgr = CoreSpringFactory.getImpl(EPFrontendManager.class);
		securityManager = BaseSecurityManager.getInstance();
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		userManager = UserManager.getInstance();
		for(int i=targetKeys.length; i-->0; ) {
			targetValues[i] = translate("map.share.to." + targetKeys[i]);
		}
		
		if(map instanceof EPStructuredMap && ((EPStructuredMap)map).getTargetResource() != null) {
			policyWrappers.add(new TutorPolicyWrapper());
		}

		for(EPMapPolicy policy:ePFMgr.getMapPolicies(map)) {
			policyWrappers.add(new PolicyWrapper(policy));
		}
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addPolicyButton = uifactory.addFormLink("map.share.add.policy", flc, Link.BUTTON);
		
		initPolicyUI();
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("ok_cancel", getTranslator());
		buttonLayout.setRootForm(mainForm);
		uifactory.addFormSubmitButton("ok", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		flc.add("ok_cancel", buttonLayout);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		// process all form-input fields and update data-model
		secureListBox();
		
		String genericError = null ;
		
		for (PolicyWrapper policyWrapper : policyWrappers) {
			Type type = policyWrapper.getType();
			if(type == null) {
				continue;//tutor implicit rule
			}
			
			TextElement mailEl = policyWrapper.getMailEl();
			if (mailEl != null) {
				String mail = mailEl.getValue();
				if (StringHelper.containsNonWhitespace(mail)) {
					if (MailHelper.isValidEmailAddress(mail)) {
						SecurityGroup allUsers = securityManager.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
						Identity currentIdentity = userManager.findIdentityByEmail(mail);
						if (currentIdentity != null && securityManager.isIdentityInSecurityGroup(currentIdentity, allUsers)) {
							mailEl.setErrorKey("map.share.with.mail.error.olatUser", new String[] { mail });
							allOk &= false;
						}
					} else {
						mailEl.setErrorKey("map.share.with.mail.error", null);
						allOk &= false;
					}
				} else if (type.equals(Type.invitation)) {
					genericError = translate("map.share.error.invite");
					allOk &= false;
				}
			} else if (type.equals(Type.group)) {
				List<BusinessGroup> groups = policyWrapper.getGroups();
				if (groups.size() == 0) {
					genericError = translate("map.share.error.group");
					allOk &= false;
				}
			} else if (type.equals(Type.user)) {
				List<Identity> idents = policyWrapper.getIdentities();
				if (idents.size() == 0) {
					genericError = translate("map.share.error.user");
					allOk &= false;
				}
			} 
			if ((policyWrapper.getFromChooser() != null && policyWrapper.getFromChooser().hasError())
					|| (policyWrapper.getToChooser() != null && policyWrapper.getToChooser().hasError())){
				genericError = translate("map.share.date.invalid");
				allOk &= false;
			}
			if (policyWrapper.getFrom() != null && policyWrapper.getTo() != null && policyWrapper.getFrom().after(policyWrapper.getTo())) {
				// show invalid date warning
				policyWrapper.getFromChooser().setErrorKey("from.date.behind.to", null);
				policyWrapper.getFromChooser().showError(true);

				genericError = translate("from.date.behind.to");
				allOk &= false;
			}
			FormLayoutContainer cmp = (FormLayoutContainer) flc.getFormComponent(policyWrapper.getComponentName());
			String errorCompName = policyWrapper.calc("errorpanel");
			StaticTextElement errTextEl = (StaticTextElement) cmp.getFormComponent(errorCompName);
			if (genericError != null && errTextEl != null) {
				errTextEl.setValue(genericError);
			}
		}

		return allOk && super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// process all form-input fields and update data-model (the policyWrappers)
		secureListBox();
		
		List<EPMapPolicy> mapPolicies = new ArrayList<EPMapPolicy>();
		for(PolicyWrapper wrapper:policyWrappers) {
			if(wrapper.getType() == null) continue;
			
			mapPolicies.add(wrapper.getMapPolicy());
			if (wrapper.getType().equals(EPMapPolicy.Type.invitation)){
				// always send an invitation mail for invited-non-olat users
				sendInvitation(ureq, wrapper);
			}
		}
		ePFMgr.updateMapPolicies(map, mapPolicies);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		// process all form-input fields and update data-model
		secureListBox();
		
		// dont allow any manipulation as long as errors exist!! else some wrong
		// policy might be persisted. check with validateFormLogic()
		if (source == addPolicyButton) {
			if (validateFormLogic(ureq)) {
				addPolicyWrapper(null);
				initPolicyUI();
			}
		} else if (source instanceof FormLink && source.getUserObject() instanceof PolicyWrapper) {
			FormLink link = (FormLink) source;
			PolicyWrapper wrapper = (PolicyWrapper) link.getUserObject();
			if (link.getName().startsWith("map.share.policy.add")) {
				if (validateFormLogic(ureq)) {
					addPolicyWrapper(wrapper);
					initPolicyUI();
				}
			} else if (link.getName().startsWith("map.share.policy.delete")) {
				removePolicyWrapper(wrapper);
				initPolicyUI();
			} else if (link.getName().startsWith("map.share.policy.invite")) {
				if (validateFormLogic(ureq)) {
					sendInvitation(ureq, wrapper);
					initPolicyUI();
				}
			}
		} else if (source instanceof SingleSelection && source.getUserObject() instanceof PolicyWrapper) {
			SingleSelection selection = (SingleSelection) source;
			if (selection.isOneSelected()) {
				String type = selection.getSelectedKey();
				PolicyWrapper wrapper = (PolicyWrapper) selection.getUserObject();
				changeType(wrapper, type);
			}
			initPolicyUI();
		} 
	}

	/**
	 * sends a link to the map to permitted users by email
	 * 
	 * @param ureq
	 * @param wrapper
	 */
	private void sendInvitation(UserRequest ureq, PolicyWrapper wrapper){
		EPMapPolicy.Type shareType = wrapper.getType();
		List<Identity> identitiesToMail = new ArrayList<Identity>();
		Invitation invitation = null;
		if (shareType.equals(EPMapPolicy.Type.allusers)) {
			return;
		}
		else if (shareType.equals(EPMapPolicy.Type.invitation)){
			invitation = wrapper.getInvitation();
		} else if (shareType.equals(EPMapPolicy.Type.group)){
			List<BusinessGroup> groups = wrapper.getGroups();
			for (BusinessGroup businessGroup : groups) {
				List<Identity> partIdents = securityManager.getIdentitiesOfSecurityGroup(businessGroup.getPartipiciantGroup());
				identitiesToMail.addAll(partIdents);
				List<Identity> ownerIdents = securityManager.getIdentitiesOfSecurityGroup(businessGroup.getOwnerGroup());
				identitiesToMail.addAll(ownerIdents);
			}
		}	else if (shareType.equals(EPMapPolicy.Type.user)){
			identitiesToMail = wrapper.getIdentities();
		}
		
		wrapper.setInvitationSend(true);

		ContactList contactList = null;
		if(identitiesToMail.size() == 1) {
			contactList = new ContactList(identitiesToMail.get(0).getUser().getProperty(UserConstants.EMAIL, ureq.getLocale()));
		} else {
			contactList = new ContactList(translate("map.share.invitation.mail.list"));
		}
		contactList.addAllIdentites(identitiesToMail);
		
		String busLink = "";
		if (invitation!=null) {
			contactList.add(invitation.getMail());
			busLink = getInvitationLink(invitation, map);
		} else {
			BusinessControlFactory bCF = BusinessControlFactory.getInstance();
			ContextEntry mapCE = bCF.createContextEntry(map.getOlatResource());
			ArrayList<ContextEntry> cEList = new ArrayList<ContextEntry>();
			cEList.add(mapCE);
			busLink = bCF.getAsURIString(cEList, true); 
		}		
		
		Emailer mailer = new Emailer(ureq.getLocale());
		boolean success = false;
		try {
			ArrayList<ContactList> clList = new ArrayList<ContactList>();
			clList.add(contactList);
			String first = getIdentity().getUser().getProperty(UserConstants.FIRSTNAME, null);
			String last = getIdentity().getUser().getProperty(UserConstants.LASTNAME, null);
			String sender = first + " " + last;
			String[] bodyArgs = new String[]{busLink, sender};
			//fxdiff VCRP-16: intern mail system
			MailContext context = new MailContextImpl(map.getOlatResource(), null, getWindowControl().getBusinessControl().getAsString()); 
			success = mailer.sendEmail(context, clList, translate("map.share.invitation.mail.subject"), translate("map.share.invitation.mail.body", bodyArgs), null);
		} catch (AddressException e) {
			logError("Error on sending invitation mail to contactlist, invalid address.", e);
		} catch (MessagingException e) {
			logError("Error on sending invitation mail to contactlist", e);
		}
		if (success) {
			showInfo("map.share.invitation.mail.success");
		}	else {
			showError("map.share.invitation.mail.failure");			
		}
	}
	
	/**
	 * loops over all PolicyWrappers and updates the datamodel according to the
	 * current form-values
	 */
	protected void secureListBox() {
		if(isLogDebugEnabled())
			logDebug(" 'securing' ListBox -->  updating policyWrappers with field values...",null);
			
		for(PolicyWrapper policyWrapper:policyWrappers) {
			TextBoxListElement userList = policyWrapper.getUserListBox();
			if(userList != null) {
				List<String> values = userList.getValueList();
				List<Identity> identities = new ArrayList<Identity>();
				for(String value:values) {
					// check again, if there actually is an identity for the
					// current username
					Identity id = getIdentityByLogin(value);
					if(id != null) {
						identities.add(id);
					}
				}
				policyWrapper.setIdentities(identities);
			}
			TextBoxListElement groupListBox = policyWrapper.getGroupListBox();
			if(groupListBox != null) {
				List<String> values = groupListBox.getValueList();
				List<BusinessGroup> selectedGroups = new ArrayList<BusinessGroup>();
				for(BusinessGroup group:groupList) {
					if(values.contains(group.getKey().toString())) {
						selectedGroups.add(group);
					}
				}
				policyWrapper.setGroups(selectedGroups);	
			}
			TextElement firstNameEl = policyWrapper.getFirstNameEl();
			if(firstNameEl != null) {
				policyWrapper.getInvitation().setFirstName(firstNameEl.getValue());
			}
			TextElement lastNameEl = policyWrapper.getLastNameEl();
			if(lastNameEl != null) {
				policyWrapper.getInvitation().setLastName(lastNameEl.getValue());
			}
			TextElement mailEl = policyWrapper.getMailEl();
			if(mailEl != null) {
				policyWrapper.getInvitation().setMail(mailEl.getValue());
			}
			if(policyWrapper.getFromChooser() != null) {
				policyWrapper.setFrom(policyWrapper.getFromChooser().getDate());
			}
			if(policyWrapper.getToChooser() != null) {
				policyWrapper.setTo(policyWrapper.getToChooser().getDate());
			}
		}
	}

	
	/**
	 * creates the custom formLayoutContainer and adds a form-component for every
	 * PolicyWrapper, according to its type.
	 * 
	 */
	protected void initPolicyUI() {
		String template = Util.getPackageVelocityRoot(this.getClass()) + "/sharePolicy.html";

		for(PolicyWrapper policyWrapper:policyWrappers) {
			String cmpName = policyWrapper.getComponentName();
			if(cmpName != null && flc.getFormComponent(cmpName) != null) {
				flc.remove(cmpName);
			}
			
			cmpName = UUID.randomUUID().toString();
			policyWrapper.setComponentName(cmpName);
			FormLayoutContainer container = FormLayoutContainer.createCustomFormLayout(cmpName, getTranslator(), template);
			container.contextPut("wrapper", policyWrapper);
			container.setRootForm(mainForm);

			if(policyWrapper.getType() != null) {
				SingleSelection type = uifactory.addDropdownSingleselect("map.share.target." + cmpName, "map.share.target", container, targetKeys, targetValues, null);
				type.addActionListener(this, FormEvent.ONCHANGE);
				type.setUserObject(policyWrapper);
				type.select(policyWrapper.getType().name(), true);
				switch(policyWrapper.getType()) {
					case user:
						Map<String,String> initialUsers = policyWrapper.getIdentitiesValue();
						TextBoxListElement userListBox = uifactory.addTextBoxListElement("map.share.with." + cmpName, "map.share.to.user", "map.share.to.user.hint", initialUsers, container, getTranslator());
						userListBox.setUserObject(policyWrapper);
						userListBox.setMapperProvider(new UserMapperProvider());
						userListBox.setAllowNewValues(false);
						userListBox.setAllowDuplicates(false);
						userListBox.setMaxResults(15);
						
						policyWrapper.setUserListBox(userListBox);
						break;
					case group:
						Map<String,String> initialGroups = policyWrapper.getGroupsValues();
						TextBoxListElement groupListBox = uifactory.addTextBoxListElement("map.share.with." + cmpName, "map.share.to.group", "map.share.to.group.hint", initialGroups, container, getTranslator());
						groupListBox.setUserObject(policyWrapper);
						groupListBox.setMapperProvider(new GroupMapperProvider());
						groupListBox.setAllowNewValues(false);
						groupListBox.setAllowDuplicates(false);
						groupListBox.setMaxResults(15);
						policyWrapper.setGroupListBox(groupListBox);
						break;
					case invitation:
						Invitation invitation = policyWrapper.getInvitation();
						if(invitation == null) {
							invitation = securityManager.createAndPersistInvitation();
							policyWrapper.setInvitation(invitation);
						}
						FormLayoutContainer invitationContainer =
							FormLayoutContainer.createDefaultFormLayout("map.share.with." + cmpName, getTranslator());
						invitationContainer.contextPut("wrapper", policyWrapper);
						invitationContainer.setRootForm(mainForm);
						container.add("map.share.with." + cmpName, invitationContainer);
						uifactory.addSpacerElement("map.share.with.spacer." + cmpName, invitationContainer, true);
						
						TextElement firstNameEl = 
							uifactory.addTextElement("map.share.with.firstName." + cmpName, "map.share.with.firstName", 64, invitation.getFirstName(), invitationContainer);
						firstNameEl.setMandatory(true);
						firstNameEl.setNotEmptyCheck("map.share.empty.warn");
						TextElement lastNameEl = 
							uifactory.addTextElement("map.share.with.lastName." + cmpName, "map.share.with.lastName", 64, invitation.getLastName(), invitationContainer);
						lastNameEl.setMandatory(true);
						lastNameEl.setNotEmptyCheck("map.share.empty.warn");
						TextElement mailEl = 
							uifactory.addTextElement("map.share.with.mail." + cmpName, "map.share.with.mail", 128, invitation.getMail(), invitationContainer);
						mailEl.setMandatory(true);
						mailEl.setNotEmptyCheck("map.share.empty.warn");
						
						if(StringHelper.containsNonWhitespace(invitation.getMail()) && MailHelper.isValidEmailAddress(invitation.getMail())) {
							SecurityGroup allUsers = securityManager.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
							Identity currentIdentity = userManager.findIdentityByEmail(invitation.getMail());
							if(currentIdentity != null && securityManager.isIdentityInSecurityGroup(currentIdentity, allUsers)) {
								mailEl.setErrorKey("map.share.with.mail.error.olatUser", new String[]{invitation.getMail()});
							}
						}
						
						policyWrapper.setFirstNameEl(firstNameEl);
						policyWrapper.setLastNameEl(lastNameEl);
						policyWrapper.setMailEl(mailEl);
						
						String link = getInvitationLink(invitation, map);
						StaticTextElement linkEl = uifactory.addStaticTextElement("map.share.with.link." + cmpName, link, invitationContainer);
						linkEl.setLabel("map.share.with.link", null);
						break;
					case allusers:
						String text = translate("map.share.with.allOlatUsers");
						uifactory.addStaticTextElement("map.share.with." + cmpName, text, container);
						break;	
				}
			}
			
			if(policyWrapper instanceof TutorPolicyWrapper) {
				String text = translate("map.share.with.tutor");
				uifactory.addStaticTextElement("map.share.text." + cmpName, text, container);
			} else {
				DateChooser fromChooser = uifactory.addDateChooser("map.share.from." + cmpName, "map.share.from", null, container);
				fromChooser.setDate(policyWrapper.getFrom());
				fromChooser.setValidDateCheck("map.share.date.invalid");
				policyWrapper.setFromChooser(fromChooser);
				DateChooser toChooser = uifactory.addDateChooser("map.share.to." + cmpName, "map.share.to", null, container);
				toChooser.setDate(policyWrapper.getTo());
				toChooser.setValidDateCheck("map.share.date.invalid");
				policyWrapper.setToChooser(toChooser);
	
				FormLink addLink = uifactory.addFormLink("map.share.policy.add." + cmpName, "map.share.policy.add", null, container, Link.BUTTON_SMALL);
				addLink.setUserObject(policyWrapper);
				FormLink removeLink = uifactory.addFormLink("map.share.policy.delete." + cmpName, "map.share.policy.delete", null, container, Link.BUTTON_SMALL);
				removeLink.setUserObject(policyWrapper);
				if (!policyWrapper.getType().equals(EPMapPolicy.Type.allusers)){
					FormLink inviteLink = uifactory.addFormLink("map.share.policy.invite." + cmpName, "map.share.policy.invite", null, container, Link.BUTTON_XSMALL);
					inviteLink.setUserObject(policyWrapper);
					inviteLink.setEnabled(!policyWrapper.isInvitationSend());
				}
				StaticTextElement genErrorPanel = uifactory.addStaticTextElement("errorpanel." + cmpName, "", container);
				genErrorPanel.setUserObject(policyWrapper);
			}
			
			policyWrapper.setComponentName(cmpName);
			
			
			flc.add(container);
			flc.contextPut("wrapper", policyWrapper);
		}
		flc.contextPut("wrappers", policyWrappers);
	}
	
	private String getInvitationLink(Invitation invitation, PortfolioStructure theMap){
		return Settings.getServerContextPathURI() + "/url/MapInvitation/" + theMap.getKey() + "?invitation=" + invitation.getToken();
	}
	
	protected void changeType(PolicyWrapper wrapper, String type) {
		wrapper.setType(EPMapPolicy.Type.valueOf(type));
	}
	
	protected void removePolicyWrapper(PolicyWrapper wrapper) {
		policyWrappers.remove(wrapper);
		flc.remove(wrapper.getComponentName());
	}
	
	protected void addPolicyWrapper(PolicyWrapper wrapper) {
		if(wrapper == null) {
			policyWrappers.add(new PolicyWrapper());
		} else {
			int index = policyWrappers.indexOf(wrapper);
			if(index+1 >= policyWrappers.size()) {
				policyWrappers.add(new PolicyWrapper());
			} else {
				policyWrappers.add(index+1, new PolicyWrapper());
			}
		}
	}
	
	protected Identity getIdentityByLogin(String login) {
		for(PolicyWrapper wrapper:policyWrappers) {
			List<Identity> ids = wrapper.getIdentities();
			if(ids != null && !ids.isEmpty()) {
				for(Identity id:ids) {
					if(id.getName().equals(login)) {
						return id;
					}
				}
			}
		}
		
		List<Identity> ids = searchUsers(login, null, false);
		for(Identity id:ids) {
			if(id.getName().equals(login)) {
				return id;
			}
		}
		return null;
	}
	
	protected List<Identity> searchUsers(String login, Map<String, String> userPropertiesSearch, boolean userPropertiesAsIntersectionSearch) {
	  return securityManager.getVisibleIdentitiesByPowerSearch(
			(login.equals("") ? null : login),
			userPropertiesSearch, userPropertiesAsIntersectionSearch,	// in normal search fields are intersected
			null, null, null, null, null);
	}
	
	protected String formatIdentity(Identity ident) {
		User u = ident.getUser();
		String login = ident.getName();//TODO username
		String first = u.getProperty(UserConstants.FIRSTNAME, null);
		String last = u.getProperty(UserConstants.LASTNAME, null);
		return login + ": " + last + " " + first;
	}
	
	public class UserMapperProvider implements ResultMapProvider {

		@Override
		public void getAutoCompleteContent(String searchValue, Map<String, String> resMap) {
			Map<String, String> userProperties = new HashMap<String, String>();
			userProperties.put(UserConstants.FIRSTNAME, searchValue);
			userProperties.put(UserConstants.LASTNAME, searchValue);
			userProperties.put(UserConstants.EMAIL, searchValue);
			if (StringHelper.containsNonWhitespace(searchValue)) {
				List<Identity> res = searchUsers(searchValue,	userProperties, false);
				int maxEntries = 14;
				boolean hasMore = false;
				for (Identity ident:res) {
					maxEntries--;
					String login = ident.getName();//TODO username
					resMap.put(formatIdentity(ident), login);
					if(maxEntries <= 0) {
						hasMore = true;
						break;
					}
				}
				if(hasMore){
					resMap.put(TextBoxListComponent.MORE_RESULTS_INDICATOR,TextBoxListComponent.MORE_RESULTS_INDICATOR);
				}
			}
		}
	}
	
	public class GroupMapperProvider implements ResultMapProvider {
		
		public GroupMapperProvider() {
			if(groupList.isEmpty()) {
				SearchBusinessGroupParams params = new SearchBusinessGroupParams(getIdentity(), true, true);
				groupList.addAll(businessGroupService.findBusinessGroups(params, null, 0, -1));
			}
		}

		@Override
		public void getAutoCompleteContent(String searchValue, Map<String, String> resMap) {
			if (StringHelper.containsNonWhitespace(searchValue)) {
				String searchValueLower = searchValue.toLowerCase();
				for(BusinessGroup group:groupList) {
					if(group.getName() != null && group.getName().toLowerCase().indexOf(searchValueLower) >= 0) {
						resMap.put(group.getName(), group.getKey().toString());
					}
				}
			}
		}
	}
	
	public class TutorPolicyWrapper extends PolicyWrapper {

		@Override
		public Type getType() {
			return null;
		}

		@Override
		public Date getTo() {
			return null;
		}

		@Override
		public DateChooser getFromChooser() {
			return null;
		}

		@Override
		public String calc(String cmpName) {
			if("map.share.target".equals(cmpName) || "map.share.with".equals(cmpName) ) {
				return "xxx";
			}
			return super.calc(cmpName);
		}
	}
	
	public class PolicyWrapper {
		private EPMapPolicy mapPolicy;
		private String componentName;
		private TextBoxListElement userListBox;
		private TextBoxListElement groupListBox;
		private DateChooser fromChooser;
		private DateChooser toChooser;
		private TextElement firstNameEl;
		private TextElement lastNameEl;
		private TextElement mailEl;
		private boolean invitationSend = false;;
		
		public PolicyWrapper() {
			this.mapPolicy = new EPMapPolicy();
		}
		
		public PolicyWrapper(EPMapPolicy mapPolicy) {
			this.mapPolicy = mapPolicy;
		}
		
		public EPMapPolicy getMapPolicy() {
			return mapPolicy;
		}

		public String getComponentName() {
			return componentName;
		}
		
		public void setComponentName(String componentName) {
			this.componentName = componentName;
		}
		
		public boolean isInvitationSend() {
			return invitationSend;
		}

		public void setInvitationSend(boolean invitationSend) {
			this.invitationSend = invitationSend;
		}

		public Invitation getInvitation() {
			return mapPolicy.getInvitation();
		}
		
		public void setInvitation(Invitation invitation) {
			mapPolicy.setInvitation(invitation);
		}
		
		public List<Policy> getPolicies() {
			return mapPolicy.getPolicies();
		}
		
		public void setPolicies(List<Policy> policies) {
			mapPolicy.setPolicies(policies);
		}
		
		public void addPolicy(Policy policy) {
			mapPolicy.addPolicy(policy);
		}

		public Date getTo() {
			return mapPolicy.getTo();
		}

		public void setTo(Date to) {
			mapPolicy.setTo(to);
		}

		public Date getFrom() {
			return mapPolicy.getFrom();
		}

		public void setFrom(Date from) {
			mapPolicy.setFrom(from);
		}

		public EPMapPolicy.Type getType() {
			return mapPolicy.getType();
		}

		public void setType(EPMapPolicy.Type type) {
			if(!type.equals(mapPolicy.getType())) {
				mapPolicy.setType(type);
				mapPolicy.getPolicies().clear();
			}
		}
		
		public Map<String,String> getIdentitiesValue() {
			if(mapPolicy.getIdentities() == null) return new HashMap<String,String>();
			
			Map<String,String> values = new HashMap<String,String>();
			for(Identity identity:mapPolicy.getIdentities()) {
				String login = identity.getName();//TODO username
				values.put(formatIdentity(identity), login);
			}
			return values;
		}

		public List<Identity> getIdentities() {
			return mapPolicy.getIdentities();
		}

		public void setIdentities(List<Identity> identities) {
			mapPolicy.setIdentities(identities);
		}
		
		public Map<String,String> getGroupsValues() {
			if(mapPolicy.getGroups() == null) return new HashMap<String,String>();
			
			Map<String,String> values = new HashMap<String,String>();
			for(BusinessGroup group:mapPolicy.getGroups()) {
				values.put(group.getName(), group.getKey().toString());
			}
			return values;
		}

		public List<BusinessGroup> getGroups() {
			return mapPolicy.getGroups();
		}

		public void setGroups(List<BusinessGroup> groups) {
			mapPolicy.setGroups(groups);
		}
		
		public void addGroup(BusinessGroup group) {
			mapPolicy.addGroup(group);
		}
		
		public String calc(String cmpName) {
			return cmpName + "." + componentName;
		}

		public TextBoxListElement getUserListBox() {
			return userListBox;
		}

		public void setUserListBox(TextBoxListElement userListBox) {
			this.groupListBox = null;
			this.userListBox = userListBox;
		}

		public TextBoxListElement getGroupListBox() {
			return groupListBox;
		}

		public void setGroupListBox(TextBoxListElement groupListBox) {
			this.userListBox = null;
			this.groupListBox = groupListBox;
		}

		public DateChooser getFromChooser() {
			return fromChooser;
		}

		public void setFromChooser(DateChooser fromChooser) {
			this.fromChooser = fromChooser;
		}

		public DateChooser getToChooser() {
			return toChooser;
		}

		public void setToChooser(DateChooser toChooser) {
			this.toChooser = toChooser;
		}

		public TextElement getFirstNameEl() {
			return firstNameEl;
		}

		public void setFirstNameEl(TextElement firstNameEl) {
			this.firstNameEl = firstNameEl;
		}

		public TextElement getLastNameEl() {
			return lastNameEl;
		}

		public void setLastNameEl(TextElement lastNameEl) {
			this.lastNameEl = lastNameEl;
		}

		public TextElement getMailEl() {
			return mailEl;
		}

		public void setMailEl(TextElement mailEl) {
			this.mailEl = mailEl;
		}
	}
}
