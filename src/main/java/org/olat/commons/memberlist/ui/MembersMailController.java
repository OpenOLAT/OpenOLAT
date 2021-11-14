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
package org.olat.commons.memberlist.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.modules.bc.FileUploadController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailLoggingAction;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailModule;
import org.olat.core.util.mail.MailerResult;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.members.Member;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MembersMailController extends FormBasicController {
	
	private static final String[] keys = new String[]{ "on" };
	
	private RichTextElement bodyEl;
	private FileElement attachmentEl;
	private FormLink addMemberButton;
	private TextElement subjectEl, externalAddressesEl;
	private MultipleSelectionElement ownerEl, coachEl, participantEl, waitingEl, individualEl, externalEl, copyFromEl;
	private FormLayoutContainer uploadCont, individualMemberCont;
	
	private CloseableModalController cmc;
	private SelectMembersController selectMemberCtrl;
	
	private int counter = 0;
	private long attachmentSize = 0l;
	private File attachementTempDir;
	private final CourseEnvironment courseEnv;
	private final int contactAttachmentMaxSizeInMb;
	private final List<Member> selectedMembers = new ArrayList<>();
	private final List<Attachment> attachments = new ArrayList<>();
	private final List<Member> ownerList, coachList, participantList, waitingList;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private MailManager mailService;
	@Autowired
	private MailModule mailModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryService repositoryService;
	
	public MembersMailController(UserRequest ureq, WindowControl wControl, Translator translator, CourseEnvironment courseEnv,
			List<Member> ownerList, List<Member> coachList, List<Member> participantList, List<Member> waitingList, String bodyTemplate) {
		super(ureq, wControl, Util.createPackageTranslator(translator, MailHelper.class, ureq.getLocale()));
		
		
		this.courseEnv = courseEnv;
		this.ownerList = ownerList;
		this.coachList = coachList;
		this.participantList = participantList;
		this.waitingList = waitingList;
		this.contactAttachmentMaxSizeInMb = mailModule.getMaxSizeForAttachement();
		initForm(ureq);
		
		// preset body template if set
		if (StringHelper.containsNonWhitespace(bodyTemplate)) {
			bodyEl.setValue(bodyTemplate);			
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String fullName = userManager.getUserDisplayName(getIdentity());
		if(StringHelper.containsNonWhitespace(fullName)) {
			fullName = "[" + fullName + "]";
		}
		TextElement fromEl = uifactory.addTextElement("from", "email.from", 255, fullName, formLayout);
		fromEl.setEnabled(false);
		
		uifactory.addSpacerElement("space-1", formLayout, false);
		
		String to = "send.mail.to";

		if(ownerList != null && ownerList.size() > 0) {
			String[] values = new String[] { translate("contact.all.owners") };
			ownerEl = uifactory.addCheckboxesHorizontal("contact.all.owners", to, formLayout, keys, values);
			ownerEl.setElementCssClass("o_sel_cmembers_mail_owner");
			to = null;
		}
		if(coachList != null && coachList.size() > 0) {
			String[] values = new String[] { translate("contact.all.coaches") };
			coachEl = uifactory.addCheckboxesHorizontal("contact.all.coaches", to, formLayout, keys, values);
			coachEl.setElementCssClass("o_sel_cmembers_mail_coach");
			to = null;
		}
		if(participantList != null && participantList.size() > 0) {
			String[] values = new String[] { translate("contact.all.participants") };
			participantEl = uifactory.addCheckboxesHorizontal("contact.all.participants", to, formLayout, keys, values);
			participantEl.setElementCssClass("o_sel_cmembers_mail_participant");
			to = null;
		}
		if(waitingList != null && waitingList.size() > 0) {
			String[] values = new String[] { translate("contact.all.waiting") };
			waitingEl = uifactory.addCheckboxesHorizontal("contact.all.waiting", to, formLayout, keys, values);
			waitingEl.setElementCssClass("o_sel_cmembers_mail_waiting");
			to = null;
		}
		
		if((ownerList != null && ownerList.size() > 0)
				|| (coachList != null && coachList.size() > 0)
				|| (participantList != null && participantList.size() > 0)
				|| (waitingList != null && waitingList.size() > 0)) {
			String[] values = new String[] { translate("contact.individual") };
			individualEl = uifactory.addCheckboxesHorizontal("contact.individual", to, formLayout, keys, values);
			individualEl.addActionListener(FormEvent.ONCHANGE);
			to = null;

			String attachmentPage = velocity_root + "/individual_members.html";
			individualMemberCont = FormLayoutContainer.createCustomFormLayout("contact.individual.list", getTranslator(), attachmentPage);
			formLayout.add(individualMemberCont);
			individualMemberCont.setRootForm(mainForm);
			individualMemberCont.setVisible(false);
			individualMemberCont.contextPut("selectedMembers", selectedMembers);
			
			addMemberButton = uifactory.addFormLink("add.member", "add", "", "", individualMemberCont, Link.NONTRANSLATED);
			addMemberButton.setIconLeftCSS("o_icon o_icon-lg o_icon_table_large");
			addMemberButton.setDomReplacementWrapperRequired(false);
			addMemberButton.getComponent().setSuppressDirtyFormWarning(true);
		}

		String[] extValues = new String[] { translate("contact.external") };
		externalEl = uifactory.addCheckboxesHorizontal("contact.external", to, formLayout, keys, extValues);
		externalEl.setElementCssClass("o_sel_cmembers_mail_external");
		externalEl.addActionListener(FormEvent.ONCHANGE);
		
		externalAddressesEl = uifactory.addTextAreaElement("contact.external.list", null, 4096, 3, 60, false, false, "", formLayout);
		externalAddressesEl.setExampleKey("contact.external.list.example", null);
		externalAddressesEl.setElementCssClass("o_sel_cmembers_external_mail");
		externalAddressesEl.setVisible(false);

		uifactory.addSpacerElement("space-2", formLayout, false);
		
		subjectEl = uifactory.addTextElement("subject", "mail.subject", 255, "", formLayout);
		subjectEl.setElementCssClass("o_sel_cmembers_mail_subject");
		subjectEl.setDisplaySize(255);
		subjectEl.setMandatory(true);
		bodyEl = uifactory.addRichTextElementForStringDataMinimalistic("body", "mail.body", "", 15, 8, formLayout, getWindowControl());
		bodyEl.setElementCssClass("o_sel_cmembers_mail_body");
		bodyEl.setMandatory(true);
		bodyEl.getEditorConfiguration().setRelativeUrls(false);
		bodyEl.getEditorConfiguration().setRemoveScriptHost(false);
		
		attachmentEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "file_upload_1", "contact.attachment", formLayout);
		attachmentEl.addActionListener(FormEvent.ONCHANGE);
		attachmentEl.setExampleKey("contact.attachment.maxsize", new String[]{ Integer.toString(contactAttachmentMaxSizeInMb) });
		
		String attachmentPage = velocity_root + "/attachments.html";
		uploadCont = FormLayoutContainer.createCustomFormLayout("file_upload_inner", getTranslator(), attachmentPage);
		uploadCont.setRootForm(mainForm);
		uploadCont.setVisible(false);
		uploadCont.contextPut("attachments", attachments);
		formLayout.add(uploadCont);
		
		String[] copyValues = new String[] { "" };
		copyFromEl = uifactory.addCheckboxesHorizontal("copy.from", "contact.cp.from", formLayout, keys, copyValues);
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		uifactory.addFormSubmitButton("email.send", buttonGroupLayout);
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
	}
	
	@Override
	protected void doDispose() {
		if(attachementTempDir != null && attachementTempDir.exists()) {
			FileUtils.deleteDirsAndFiles(attachementTempDir, true, true);
		}
        super.doDispose();
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		subjectEl.clearError();
		if(!StringHelper.containsNonWhitespace(subjectEl.getValue())) {
			subjectEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		bodyEl.clearError();
		if(!StringHelper.containsNonWhitespace(bodyEl.getValue())) {
			bodyEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		allOk &= validateRecipients();
		
		return allOk;
	}
	
	private boolean validateRecipients() {
		boolean allOk = true;
		
		boolean atLeastOne = false;
		
		externalAddressesEl.clearError();
		if(externalEl != null && externalEl.isAtLeastSelected(1)) {
			String value = externalAddressesEl.getValue();
			StringBuilder errors = new StringBuilder();
			if(StringHelper.containsNonWhitespace(value)) {
				for(StringTokenizer tokenizer= new StringTokenizer(value, ",\r\n", false); tokenizer.hasMoreTokens(); ) {
					String email = tokenizer.nextToken().trim();
					if(!MailHelper.isValidEmailAddress(email)) {
						if(errors.length() > 0) errors.append(", ");
						errors.append(email);
					}
				}
				atLeastOne |= true;
			}
			
			if(errors.length() > 0) {
				externalAddressesEl.setErrorKey("mailhelper.error.addressinvalid", new String[]{ errors.toString() });
				allOk &= false;
			}
		}
		
		
		if(ownerEl != null) ownerEl.clearError();
		if(coachEl != null) coachEl.clearError();
		if(participantEl != null) participantEl.clearError();
		if(waitingEl != null) waitingEl.clearError();
		if(individualEl != null) individualEl.clearError();
		
		if((ownerEl != null && ownerEl.isAtLeastSelected(1))
				|| (coachEl != null && coachEl.isAtLeastSelected(1))
				|| (participantEl != null && participantEl.isAtLeastSelected(1))
				|| (waitingEl != null && waitingEl.isAtLeastSelected(1))) {
			atLeastOne |= true;
		}
		
		//check if there is an individual email
		if(!atLeastOne && individualEl != null && individualEl.isAtLeastSelected(1) && !selectedMembers.isEmpty()) {
			atLeastOne |= true;
		}
		
		if(!atLeastOne) {
			if(externalEl != null && externalEl.isAtLeastSelected(1) && !StringHelper.containsNonWhitespace(externalAddressesEl.getValue())) {
				externalEl.setErrorKey("at.least.one.recipient", null);
			} else if(individualEl != null && individualEl.isAtLeastSelected(1) && selectedMembers.isEmpty()) {
				individualEl.setErrorKey("at.least.one.recipient", null);
			} else if(ownerEl != null && !ownerEl.isAtLeastSelected(1)) {
				ownerEl.setErrorKey("at.least.one.recipient", null);
			} else if(coachEl != null && !coachEl.isAtLeastSelected(1)) {
				coachEl.setErrorKey("at.least.one.recipient", null);
			} else if(participantEl != null && !participantEl.isAtLeastSelected(1)) {
				participantEl.setErrorKey("at.least.one.recipient", null);
			} else if(waitingEl != null && !waitingEl.isAtLeastSelected(1)) {
				waitingEl.setErrorKey("at.least.one.recipient", null);
			} else if (individualEl != null && !individualEl.isAtLeastSelected(1)) {
				individualEl.setErrorKey("at.least.one.recipient", null);
			} else if (externalEl != null && !externalEl.isAtLeastSelected(1)) {
				externalEl.setErrorKey("at.least.one.recipient", null);
			}
		}
		
		return allOk && atLeastOne;
	}
	
	private File[] getAttachments() {
		File[] atttachmentArr = new File[attachments.size()];
		for(int i=attachments.size(); i-->0; ) {
			atttachmentArr[i] = attachments.get(i).getFile();
		}
		return atttachmentArr;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == externalEl) {
			externalAddressesEl.setVisible(externalEl.isAtLeastSelected(1));
		} else if(source == individualEl) {
			individualMemberCont.setVisible(individualEl.isAtLeastSelected(1));
			flc.setDirty(true);
		} else if(source == attachmentEl) {
			doUploadAttachement();
		} else if(source == addMemberButton) {
			doChooseMember(ureq);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("delete".equals(cmd)) {
				Attachment attachment = (Attachment)link.getUserObject();
				doDeleteAttachment(attachment);
			} else if("remove".equals(cmd)) {
				Member member = (Member)link.getUserObject();
				doRemoveIndividualMember(member);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == selectMemberCtrl) {
			if(event == Event.DONE_EVENT) {
				List<Member> moreSelectedMembers = selectMemberCtrl.getSelectedMembers();
				doAddSelectedMembers(moreSelectedMembers);
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(selectMemberCtrl);
		removeAsListenerAndDispose(cmc);
		selectMemberCtrl = null;
		cmc = null;
	}

	private void doAddSelectedMembers(List<Member> moreSelectedMembers) {
		if(moreSelectedMembers == null || moreSelectedMembers.isEmpty()) return;
		
		for(Member selectedMember:selectedMembers) {
			if(selectedMember.getRemoveLink() != null) {
				individualMemberCont.remove(selectedMember.getRemoveLink());
			}
		}
		selectedMembers.clear();
		
		for(Member member:moreSelectedMembers) {
			if(selectedMembers.contains(member)) continue;

			String removeLinkName = "remove_" + (++counter);
			FormLink removeLink = uifactory.addFormLink(removeLinkName, "remove", "", null, individualMemberCont, Link.NONTRANSLATED);
			removeLink.setUserObject(member);
			removeLink.setIconLeftCSS("o_icon o_icon_remove");
			individualMemberCont.add(removeLink);
			member.setRemoveLink(removeLink);
			selectedMembers.add(member);
		}
	}
	
	private void doRemoveIndividualMember(Member member) {
		selectedMembers.remove(member);
		if(member.getRemoveLink() != null) {
			individualMemberCont.remove(member.getRemoveLink());
		}
		individualMemberCont.setDirty(true);
	}

	private void doChooseMember(UserRequest ureq) {
		if(guardModalController(selectMemberCtrl)) return;
		
		List<Member> owners = ownerList;
		List<Member> coaches = coachList;
		List<Member> participants = participantList;
		List<Member> waiting = waitingList;
		if(ownerEl != null && ownerEl.isAtLeastSelected(1)) {
			owners = null;
		}
		if(coachEl != null && coachEl.isAtLeastSelected(1)) {
			coaches = null;
		}
		if(participantEl != null && participantEl.isAtLeastSelected(1)) {
			participants = null;
		}
		if(waitingEl != null && waitingEl.isAtLeastSelected(1)) {
			waiting = null;
		}
		
		if(owners == null && coaches == null && participants == null) {
			showWarning("already.all.selected");
		} else {
			selectMemberCtrl = new SelectMembersController(ureq, getWindowControl(), getTranslator(), 
					selectedMembers, owners, coaches, participants, waiting);
			listenTo(selectMemberCtrl);
			
			String title = translate("select.members");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), selectMemberCtrl.getInitialComponent(), true, title);
			cmc.suppressDirtyFormWarningOnClose();
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doDeleteAttachment(Attachment attachment) {
		attachmentSize -= attachment.getFile().length();
		FileUtils.deleteFile(attachment.getFile());
		attachments.remove(attachment);
		uploadCont.setVisible(!attachments.isEmpty());
		uploadCont.setDirty(true);
	}
	
	private void doUploadAttachement() {
		if(attachementTempDir == null) {
			attachementTempDir = FileUtils.createTempDir("attachements", null, null);
		}
		
		long size = attachmentEl.getUploadSize();
		String filename = attachmentEl.getUploadFileName();
		if(size + attachmentSize > (contactAttachmentMaxSizeInMb  * 1024 * 1024)) {
			showWarning("contact.attachment,maxsize", Integer.toString(contactAttachmentMaxSizeInMb));
			attachmentEl.reset();
		} else {
			File attachment = attachmentEl.moveUploadFileTo(attachementTempDir);
			attachmentEl.reset();
			if(attachment == null) {
				logError("Could not move contact-form attachment to " + attachementTempDir.getAbsolutePath(), null);
				setTranslator(Util.createPackageTranslator(FileUploadController.class, getLocale(), getTranslator()));
				showError("FileMoveCopyFailed","");
			} else {
				attachmentSize += size;
				FormLink removeFile = uifactory.addFormLink("delete_" + (++counter), "delete", "delete", null, uploadCont, Link.LINK);
				removeFile.setIconLeftCSS("o_icon o_icon-fw o_icon_delete");
				String css = CSSHelper.createFiletypeIconCssClassFor(filename);
				Attachment wrapper = new Attachment(attachment, attachment.getName(), css, removeFile);
				removeFile.setUserObject(wrapper);
				attachments.add(wrapper);
				uploadCont.setVisible(true);
			}
		}
	}
	
	private void doSend(UserRequest ureq) {
		List<ContactList> contactList = new ArrayList<>();
		if (courseEnv == null) {
			if(coachEl != null && coachEl.isAtLeastSelected(1)) {
				List<Long> identityKeys = new ArrayList<>(coachList.size());
				for(Member coach:coachList) {
					identityKeys.add(coach.getKey());
				}
				List<Identity> coaches = securityManager.loadIdentityByKeys(identityKeys);
				ContactList memberList = new ContactList(translate("contact.list.coaches"));
				memberList.addAllIdentites(coaches);
				contactList.add(memberList);
			}
			
			if(participantEl != null && participantEl.isAtLeastSelected(1)) {
				List<Long> identityKeys = new ArrayList<>(participantList.size());
				for(Member participant:participantList) {
					identityKeys.add(participant.getKey());
				}
				List<Identity> participants = securityManager.loadIdentityByKeys(identityKeys);
				ContactList memberList = new ContactList(translate("contact.list.participants"));
				memberList.addAllIdentites(participants);
				contactList.add(memberList);
			}
			
			if(waitingEl != null && waitingEl.isAtLeastSelected(1)) {
				List<Long> identityKeys = new ArrayList<>(waitingList.size());
				for(Member waiter:waitingList) {
					identityKeys.add(waiter.getKey());
				}
				List<Identity> waiters = securityManager.loadIdentityByKeys(identityKeys);
				ContactList memberList = new ContactList(translate("contact.list.waiting"));
				memberList.addAllIdentites(waiters);
				contactList.add(memberList);
			}
		} else {			
			if(ownerEl != null && ownerEl.isAtLeastSelected(1)) {
				RepositoryEntry courseRepositoryEntry = courseEnv.getCourseGroupManager().getCourseEntry();
				List<Identity> owners = repositoryService.getMembers(courseRepositoryEntry, RepositoryEntryRelationType.entryAndCurriculums, GroupRoles.owner.name());
				ContactList memberList = new ContactList(translate("contact.list.owners"));
				memberList.addAllIdentites(owners);
				contactList.add(memberList);
			}
			
			if(coachEl != null && coachEl.isAtLeastSelected(1)) {
				Set<Long> sendToWhatYouSee = new HashSet<>();
				for(Member coach:coachList) {
					sendToWhatYouSee.add(coach.getKey());
				}
				CourseGroupManager cgm = courseEnv.getCourseGroupManager();
				ContactList memberList = new ContactList(translate("contact.list.coaches"));
				avoidInvisibleMember(cgm.getCoachesFromBusinessGroups(), memberList, sendToWhatYouSee);
				avoidInvisibleMember(cgm.getCoaches(), memberList, sendToWhatYouSee);
				contactList.add(memberList);
			}
			
			if(participantEl != null && participantEl.isAtLeastSelected(1)) {
				Set<Long> sendToWhatYouSee = new HashSet<>();
				for(Member participant:participantList) {
					sendToWhatYouSee.add(participant.getKey());
				}
				CourseGroupManager cgm = courseEnv.getCourseGroupManager();
				ContactList memberList = new ContactList(translate("contact.list.participants"));
				avoidInvisibleMember(cgm.getParticipantsFromBusinessGroups(), memberList, sendToWhatYouSee);
				avoidInvisibleMember(cgm.getParticipants(), memberList, sendToWhatYouSee);
				contactList.add(memberList);
			}
		}
		
		if(individualEl != null && individualEl.isAtLeastSelected(1)
				&& selectedMembers != null && selectedMembers.size() > 0) {
			List<Long> identityKeys = new ArrayList<>(selectedMembers.size());
			for(Member member:selectedMembers) {
				identityKeys.add(member.getKey());
			}
			List<Identity> selectedIdentities = securityManager.loadIdentityByKeys(identityKeys);
			ContactList otherList = new ContactList(translate("contact.list.others"));
			otherList.addAllIdentites(selectedIdentities);
			contactList.add(otherList);
		}
		
		if(externalEl != null && externalEl.isAtLeastSelected(1)) {
			String value = externalAddressesEl.getValue();
			if(StringHelper.containsNonWhitespace(value)) {
				ContactList externalList = new ContactList(translate("contact.list.external"));
				for(StringTokenizer tokenizer= new StringTokenizer(value, ",\r\n", false); tokenizer.hasMoreTokens(); ) {
					String email = tokenizer.nextToken().trim();
					externalList.add(email);
				}
				contactList.add(externalList);
			}
		}

		doSendEmailToMember(ureq, contactList);
	}
	
	private void doSendEmailToMember(UserRequest ureq, List<ContactList> contactList) {
		boolean success = false;
		try {
			File[] attachmentArr = getAttachments();
			MailContext context = new MailContextImpl(getWindowControl().getBusinessControl().getAsString());
			MailBundle bundle = new MailBundle();
			bundle.setContext(context);
			bundle.setFromId(getIdentity());						
			bundle.setContactLists(contactList);
			bundle.setContent(subjectEl.getValue(), bodyEl.getValue(), attachmentArr);
			MailerResult result = mailService.sendMessage(bundle);
			if(copyFromEl.isAtLeastSelected(1)) {
				MailBundle ccBundle = new MailBundle();
				ccBundle.setContext(context);
				ccBundle.setFromId(getIdentity()); 
				ccBundle.setCc(getIdentity());							
				ccBundle.setContent(subjectEl.getValue(), bodyEl.getValue(), attachmentArr);
				MailerResult ccResult = mailService.sendMessage(ccBundle);
				result.append(ccResult);
			}
			success = result.isSuccessful();
		} catch (Exception e) {
			//error in recipient email address(es)
			handleAddressException(success);
		}
		if (success) {
			showInfo("msg.send.ok");
			// do logging
			ThreadLocalUserActivityLogger.log(MailLoggingAction.MAIL_SENT, getClass());
			fireEvent(ureq, Event.DONE_EVENT);
		} else {
			showInfo("error.msg.send.nok");
			fireEvent(ureq, Event.FAILED_EVENT);
		}
	}
	
	private void handleAddressException(boolean success) {
		StringBuilder error = new StringBuilder();
		if (success) {
			error.append(translate("error.msg.send.partially.nok"))
			     .append("<br />")
			     .append(translate("error.msg.send.invalid.rcps"));
		} else {
			error.append(translate("error.msg.send.nok"))
			     .append("<br />")
			     .append(translate("error.msg.send.553"));
		}
		getWindowControl().setError(error.toString());
	}
	
	private void avoidInvisibleMember(List<Identity> members, ContactList contactList, Set<Long> sendToWhatYouSee) {
		for(Identity member:members) {
			if(sendToWhatYouSee.contains(member.getKey())) {
				contactList.add(member);
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSend(ureq);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	public static class Attachment {
		
		private final File file;
		private final String filename;
		private final String cssClass;
		private final FormLink deleteLink;
		
		public Attachment(File file, String filename, String cssClass, FormLink deleteLink) {
			this.file = file;
			this.filename = filename;
			this.cssClass = cssClass;
			this.deleteLink = deleteLink;
		}
		
		public File getFile() {
			return file;
		}

		public String getCssClass() {
			return cssClass;
		}

		public String getFilename() {
			return filename;
		}

		public FormLink getDeleteLink() {
			return deleteLink;
		}
		
		public String getDeleteComponentName() {
			return deleteLink.getComponent().getComponentName();
		}
	}
}