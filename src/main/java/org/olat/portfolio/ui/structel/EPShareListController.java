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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.Invitation;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.BusinessGroupSelectionEvent;
import org.olat.group.ui.main.SelectBusinessGroupController;
import org.olat.login.LoginModule;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.manager.EPMapPolicy;
import org.olat.portfolio.manager.EPMapPolicy.Type;
import org.olat.portfolio.manager.InvitationDAO;
import org.olat.portfolio.model.structel.EPStructuredMap;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

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
	
	private final List<EPSharePolicyWrapper> policyWrappers = new ArrayList<>();
	
	private PortfolioStructureMap map;
	
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private EPFrontendManager ePFMgr;
	@Autowired
	private InvitationDAO invitationDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private MailManager mailManager;
	
	private final String[] targetKeys = EPMapPolicy.Type.names();
	private final String[] targetValues = new String[targetKeys.length];
	private final AtomicInteger cmpSuffixGenerator = new AtomicInteger(1);

	private CloseableModalController cmc;
	private UserSearchController selectUserCtrl;
	private SelectBusinessGroupController selectGroupCtrl;
	
	private FormLink addPolicyButton;
	
	
	
	public EPShareListController(UserRequest ureq, WindowControl wControl, PortfolioStructureMap map) {
		super(ureq, wControl, "shareList");
		
		this.map = map;
		for(int i=targetKeys.length; i-->0; ) {
			targetValues[i] = translate("map.share.to." + targetKeys[i]);
		}
		
		if(map instanceof EPStructuredMap && ((EPStructuredMap)map).getTargetResource() != null) {
			policyWrappers.add(new TutorEPSharePolicyWrapper());
		}

		for(EPMapPolicy policy:ePFMgr.getMapPolicies(map)) {
			policyWrappers.add(new EPSharePolicyWrapper(policy));
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
		boolean allOk = super.validateFormLogic(ureq);
		
		// process all form-input fields and update data-model
		secureListBox();
		
		String genericError = null ;
		
		for (EPSharePolicyWrapper policyWrapper : policyWrappers) {
			Type type = policyWrapper.getType();
			if(type == null) {
				continue;//tutor implicit rule
			}
			
			TextElement mailEl = policyWrapper.getMailEl();
			if (mailEl != null) {
				String mail = mailEl.getValue();
				if (StringHelper.containsNonWhitespace(mail)) {
					if (MailHelper.isValidEmailAddress(mail)) {
						List<Identity> shareWithIdentities = userManager.findIdentitiesByEmail(Collections.singletonList(mail));
						if(isAtLeastOneUser(shareWithIdentities)) {
							mailEl.setErrorKey("error.invitation.mail.used", new String[] { mail });
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
				if (groups.isEmpty()) {
					genericError = translate("map.share.error.group");
					allOk &= false;
				}
			} else if (type.equals(Type.user)) {
				List<Identity> idents = policyWrapper.getIdentities();
				if (idents.isEmpty()) {
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
			StaticTextElement errTextEl = policyWrapper.getErrorEl();
			if (genericError != null && errTextEl != null) {
				errTextEl.setValue(genericError);
			}
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// process all form-input fields and update data-model (the policyWrappers)
		secureListBox();
		
		List<EPMapPolicy> mapPolicies = new ArrayList<>();
		for(EPSharePolicyWrapper wrapper:policyWrappers) {
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
				addEPSharePolicyWrapper(null);
				initPolicyUI();
			}
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink) source;
			Object userObject = link.getUserObject();
			if(userObject instanceof EPSharePolicyWrapper) {
				EPSharePolicyWrapper wrapper = (EPSharePolicyWrapper)userObject;
				if (link.getName().startsWith("map.share.policy.add")) {
					if (validateFormLogic(ureq)) {
						addEPSharePolicyWrapper(wrapper);
						initPolicyUI();
					}
				} else if (link.getName().startsWith("map.share.policy.delete")) {
					removeEPSharePolicyWrapper(wrapper);
					initPolicyUI();
				} else if (link.getName().startsWith("map.share.policy.invite")) {
					if (validateFormLogic(ureq)) {
						sendInvitation(ureq, wrapper);
						initPolicyUI();
					}
				} else if (link.getName().startsWith("choose.group")) {
					doSelectGroup(ureq, wrapper);
				} else if (link.getName().startsWith("choose.identity")) {
					doSelectIdentity(ureq, wrapper);
				}
			} else if(userObject instanceof EPShareGroupWrapper) {
				EPShareGroupWrapper wrapper = (EPShareGroupWrapper)userObject;
				wrapper.remove();
				initPolicyUI();
			} else if(userObject instanceof EPShareUserWrapper) {
				EPShareUserWrapper wrapper = (EPShareUserWrapper)userObject;
				wrapper.remove();
				initPolicyUI();
			}
		} else if (source instanceof SingleSelection && source.getUserObject() instanceof EPSharePolicyWrapper) {
			SingleSelection selection = (SingleSelection) source;
			if (selection.isOneSelected()) {
				String type = selection.getSelectedKey();
				EPSharePolicyWrapper wrapper = (EPSharePolicyWrapper) selection.getUserObject();
				changeType(wrapper, type);
			}
			initPolicyUI();
		} 
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(source == selectGroupCtrl) {
			cmc.deactivate();
			secureListBox();
			if(event instanceof BusinessGroupSelectionEvent) {
				BusinessGroupSelectionEvent bge = (BusinessGroupSelectionEvent)event;
				List<BusinessGroup> groups = bge.getGroups();
				if(groups.size() > 0) {
					EPSharePolicyWrapper policyWrapper = (EPSharePolicyWrapper)selectGroupCtrl.getUserObject();
					policyWrapper.getGroups().addAll(groups);
					initPolicyUI();
				}
			}
		} else if(source.equals(selectUserCtrl)){
			cmc.deactivate();
			secureListBox();
			EPSharePolicyWrapper policyWrapper = (EPSharePolicyWrapper)selectUserCtrl.getUserObject();
			if (event instanceof SingleIdentityChosenEvent) {
				SingleIdentityChosenEvent foundEvent = (SingleIdentityChosenEvent) event;
				Identity chosenIdentity = foundEvent.getChosenIdentity();
				if (chosenIdentity != null) {
					policyWrapper.getIdentities().add(chosenIdentity);
				}
			} else if(event instanceof MultiIdentityChosenEvent) {
				MultiIdentityChosenEvent foundEvent = (MultiIdentityChosenEvent) event;
				List<Identity> chosenIdentities = foundEvent.getChosenIdentities();
				if (chosenIdentities != null && !chosenIdentities.isEmpty()) {
					policyWrapper.getIdentities().addAll(chosenIdentities);
				}
			}
			initPolicyUI();
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(selectGroupCtrl);
		removeAsListenerAndDispose(cmc);
		selectGroupCtrl = null;
		cmc = null;
	}

	protected void doSelectGroup(UserRequest ureq, EPSharePolicyWrapper wrapper) {
		removeAsListenerAndDispose(selectGroupCtrl);
		selectGroupCtrl = new SelectBusinessGroupController(ureq, getWindowControl());
		selectGroupCtrl.setUserObject(wrapper);
		listenTo(selectGroupCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				selectGroupCtrl.getInitialComponent(), true, translate("choose.group"));
		cmc.activate();
		listenTo(cmc);
	}
	
	protected void doSelectIdentity(UserRequest ureq, EPSharePolicyWrapper wrapper) {
		removeAsListenerAndDispose(selectUserCtrl);
		selectUserCtrl = new UserSearchController(ureq, getWindowControl());
		selectUserCtrl.setUserObject(wrapper);
		listenTo(selectUserCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				selectUserCtrl.getInitialComponent(), true, translate("choose.identity"));
		cmc.activate();
		listenTo(cmc);
	}

	/**
	 * sends a link to the map to permitted users by email
	 * 
	 * @param ureq
	 * @param wrapper
	 */
	private void sendInvitation(UserRequest ureq, EPSharePolicyWrapper wrapper){
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
			List<Identity> members = businessGroupService.getMembers(groups, GroupRoles.coach.name(), GroupRoles.participant.name());
			identitiesToMail.addAll(members);
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
			ArrayList<ContextEntry> cEList = new ArrayList<>();
			cEList.add(mapCE);
			busLink = bCF.getAsURIString(cEList, true); 
		}		
		
		boolean success = false;
		try {
			String first = getIdentity().getUser().getProperty(UserConstants.FIRSTNAME, null);
			String last = getIdentity().getUser().getProperty(UserConstants.LASTNAME, null);
			String sender = first + " " + last;
			String[] bodyArgs = new String[]{busLink, sender};

			MailContext context = new MailContextImpl(map.getOlatResource(), null, getWindowControl().getBusinessControl().getAsString()); 
			MailBundle bundle = new MailBundle();
			bundle.setContext(context);
			bundle.setFrom(WebappHelper.getMailConfig("mailReplyTo"));
			bundle.setContactList(contactList);
			bundle.setContent(translate("map.share.invitation.mail.subject"), translate("map.share.invitation.mail.body", bodyArgs));

			MailerResult result = mailManager.sendMessage(bundle);
			success = result.isSuccessful();
		} catch (Exception e) {
			logError("Error on sending invitation mail to contactlist, invalid address.", e);
		}
		if (success) {
			showInfo("map.share.invitation.mail.success");
		}	else {
			showError("map.share.invitation.mail.failure");			
		}
	}
	
	/**
	 * loops over all EPSharePolicyWrappers and updates the datamodel according to the
	 * current form-values
	 */
	protected void secureListBox() {
		if(isLogDebugEnabled())
			logDebug(" 'securing' ListBox -->  updating policyWrappers with field values...");
			
		for(EPSharePolicyWrapper policyWrapper:policyWrappers) {
			if(policyWrapper.getUserListBox() != null) {
				List<Identity> identities = policyWrapper.getIdentities();
				policyWrapper.setIdentities(identities);
			}
			if(policyWrapper.getGroups() != null) {
				List<BusinessGroup> selectedGroups = policyWrapper.getGroups();
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
	 * EPSharePolicyWrapper, according to its type.
	 * 
	 */
	protected void initPolicyUI() {
		String template = Util.getPackageVelocityRoot(this.getClass()) + "/sharePolicy.html";

		for(EPSharePolicyWrapper policyWrapper:policyWrappers) {
			String cmpName = policyWrapper.getComponentName();
			if(cmpName != null && flc.getFormComponent(cmpName) != null) {
				flc.remove(cmpName);
			}
			
			cmpName = Integer.toString(cmpSuffixGenerator.getAndIncrement());
			policyWrapper.setComponentName(cmpName);
			FormLayoutContainer container = FormLayoutContainer.createCustomFormLayout(cmpName, getTranslator(), template);
			container.contextPut("wrapper", policyWrapper);
			container.setRootForm(mainForm);

			if(policyWrapper.getType() != null) {
				String[] curatedTargetKeys;
				String[] curatedTargetValues;
				if(!Type.invitation.equals(policyWrapper.getType()) && !loginModule.isInvitationEnabled()) {
					curatedTargetKeys = new String[targetKeys.length - 1];
					curatedTargetValues = new String[targetKeys.length - 1];
					
					int pos = 0;
					for(int i=targetKeys.length; i-->0; ) {
						if(!targetKeys[i].equals(Type.invitation.name())) {
							curatedTargetKeys[pos] = targetKeys[i];
							curatedTargetValues[pos++] = targetValues[i];
						}
					}
				} else {
					curatedTargetKeys = targetKeys;
					curatedTargetValues = targetValues;
				}

				SingleSelection type = uifactory.addDropdownSingleselect("map.share.target." + cmpName, "map.share.target", container, curatedTargetKeys, curatedTargetValues, null);
				type.addActionListener(FormEvent.ONCHANGE);
				type.setUserObject(policyWrapper);
				type.select(policyWrapper.getType().name(), true);
				switch(policyWrapper.getType()) {
					case user:
						createContainerForUser(policyWrapper, cmpName, container);
						break;
					case group:
						createContainerForGroup(policyWrapper, cmpName, container);
						break;
					case invitation:
						Invitation invitation = policyWrapper.getInvitation();
						if(invitation == null) {
							invitation = invitationDao.createAndPersistInvitation();
							policyWrapper.setInvitation(invitation);
						}
						
						createContainerForInvitation(invitation, policyWrapper, cmpName, container);
						break;
					case allusers:
						String text = translate("map.share.with.allOlatUsers");
						uifactory.addStaticTextElement("map.share.with." + cmpName, text, container);
						break;	
				}
			}
			
			if(policyWrapper instanceof TutorEPSharePolicyWrapper) {
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
	
				FormLink addLink = uifactory.addFormLink("map.share.policy.add." + cmpName, "map.share.policy.add", null, container, Link.BUTTON);
				addLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
				addLink.setUserObject(policyWrapper);
				FormLink removeLink = uifactory.addFormLink("map.share.policy.delete." + cmpName, "map.share.policy.delete", null, container, Link.BUTTON);
				removeLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
				removeLink.setUserObject(policyWrapper);
				if (!policyWrapper.getType().equals(EPMapPolicy.Type.allusers)){
					FormLink inviteLink = uifactory.addFormLink("map.share.policy.invite." + cmpName, "map.share.policy.invite", null, container, Link.BUTTON);
					inviteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_share");
					inviteLink.setUserObject(policyWrapper);
					inviteLink.setEnabled(!policyWrapper.isInvitationSend() && (loginModule.isInvitationEnabled() || !policyWrapper.getType().equals(Type.invitation)));
				}
				StaticTextElement genErrorPanel = uifactory.addStaticTextElement("errorpanel." + cmpName, "", container);
				genErrorPanel.setUserObject(policyWrapper);
				policyWrapper.setErrorEl(genErrorPanel);
			}
			
			policyWrapper.setComponentName(cmpName);
			
			
			flc.add(container);
			flc.contextPut("wrapper", policyWrapper);
		}
		flc.contextPut("wrappers", policyWrappers);
	}
	
	private void createContainerForUser(EPSharePolicyWrapper policyWrapper, String cmpName, FormLayoutContainer container) {
		String page = velocity_root + "/shareWithUsers.html";
		FormLayoutContainer userListBox = FormLayoutContainer.createCustomFormLayout("map.share.with." + cmpName, getTranslator(), page);
		userListBox.contextPut("wrapper", policyWrapper);
		userListBox.setRootForm(mainForm);
		container.add("map.share.with." + cmpName, userListBox);
		
		List<Identity> identities = policyWrapper.getIdentities();
		List<EPShareUserWrapper> groupWrappers = new ArrayList<>();
		for(Identity identity: identities) {
			FormLink rmLink = uifactory.addFormLink("rm-" + identity.getKey(), "", null, userListBox, Link.NONTRANSLATED + Link.LINK);
			rmLink.setIconLeftCSS("o_icon o_icon_remove");
			EPShareUserWrapper gWrapper = new EPShareUserWrapper(policyWrapper, identity, rmLink);
			rmLink.setUserObject(gWrapper);	
			groupWrappers.add(gWrapper);
		}
		userListBox.contextPut("identities", groupWrappers);
		policyWrapper.setUserListBox(userListBox);

		FormLink chooseUsersLink = uifactory.addFormLink("choose.identity", "choose.identity", null, userListBox, Link.BUTTON);
		chooseUsersLink.setElementCssClass("o_form_groupchooser");
		chooseUsersLink.setUserObject(policyWrapper);	
	}
	
	private void createContainerForGroup(EPSharePolicyWrapper policyWrapper, String cmpName, FormLayoutContainer container) {
		String page = velocity_root + "/shareWithGroups.html";
		FormLayoutContainer groupListBox = FormLayoutContainer.createCustomFormLayout("map.share.with." + cmpName, getTranslator(), page);
		groupListBox.contextPut("wrapper", policyWrapper);
		groupListBox.setRootForm(mainForm);
		container.add("map.share.with." + cmpName, groupListBox);
		
		List<BusinessGroup> groups = policyWrapper.getGroups();
		List<EPShareGroupWrapper> groupWrappers = new ArrayList<>();
		for(BusinessGroup group: groups) {
			FormLink rmGroupLink = uifactory.addFormLink("rm-" + group.getKey(), "", null, groupListBox, Link.NONTRANSLATED + Link.LINK);
			rmGroupLink.setCustomEnabledLinkCSS("o_icon o_icon_remove");
			EPShareGroupWrapper gWrapper = new EPShareGroupWrapper(policyWrapper, group, rmGroupLink);
			rmGroupLink.setUserObject(gWrapper);	
			groupWrappers.add(gWrapper);
		}
		groupListBox.contextPut("groups", groupWrappers);
		policyWrapper.setGroupListBox(groupListBox);

		FormLink chooseGroupsLink = uifactory.addFormLink("choose.group", "choose.group", null, groupListBox,Link.BUTTON);
		chooseGroupsLink.setElementCssClass("o_form_groupchooser");
		chooseGroupsLink.setUserObject(policyWrapper);	
	}
	
	private void createContainerForInvitation(Invitation invitation, EPSharePolicyWrapper policyWrapper, String cmpName, FormLayoutContainer container) {
		FormLayoutContainer invitationContainer =
				FormLayoutContainer.createDefaultFormLayout("map.share.with." + cmpName, getTranslator());
		invitationContainer.contextPut("wrapper", policyWrapper);
		invitationContainer.setRootForm(mainForm);
		container.add("map.share.with." + cmpName, invitationContainer);
		
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
			List<Identity> shareWithIdentities = userManager.findIdentitiesByEmail(Collections.singletonList(invitation.getMail()));
			if(isAtLeastOneUser(shareWithIdentities)) {
				mailEl.setErrorKey("map.share.with.mail.error.olatUser", new String[]{invitation.getMail()});
			}
		}
		
		policyWrapper.setFirstNameEl(firstNameEl);
		policyWrapper.setLastNameEl(lastNameEl);
		policyWrapper.setMailEl(mailEl);
		
		String link = getInvitationLink(invitation, map);
		StaticTextElement linkEl = uifactory.addStaticTextElement("map.share.with.link." + cmpName, link, invitationContainer);
		linkEl.setLabel("map.share.with.link", null);
	}
	
	private boolean isAtLeastOneUser(Collection<Identity> identites) {
		for (Identity identity: identites) {
			if (organisationService.hasRole(identity, OrganisationRoles.user)) {
				return true;
			}
		}
		return false;
	}
	
	private String getInvitationLink(Invitation invitation, PortfolioStructure theMap){
		return Settings.getServerContextPathURI() + "/url/MapInvitation/" + theMap.getKey() + "?invitation=" + invitation.getToken();
	}
	
	protected void changeType(EPSharePolicyWrapper wrapper, String type) {
		wrapper.setType(EPMapPolicy.Type.valueOf(type));
	}
	
	protected void removeEPSharePolicyWrapper(EPSharePolicyWrapper wrapper) {
		policyWrappers.remove(wrapper);
		flc.remove(wrapper.getComponentName());
	}
	
	protected void addEPSharePolicyWrapper(EPSharePolicyWrapper wrapper) {
		if(wrapper == null) {
			policyWrappers.add(new EPSharePolicyWrapper());
		} else {
			int index = policyWrappers.indexOf(wrapper);
			if(index+1 >= policyWrappers.size()) {
				policyWrappers.add(new EPSharePolicyWrapper());
			} else {
				policyWrappers.add(index+1, new EPSharePolicyWrapper());
			}
		}
	}

	public class TutorEPSharePolicyWrapper extends EPSharePolicyWrapper {
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
}
