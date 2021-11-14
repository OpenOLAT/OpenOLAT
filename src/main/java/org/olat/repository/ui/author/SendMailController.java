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
package org.olat.repository.ui.author;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.olat.core.util.mail.MailLoggingAction;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailModule;
import org.olat.core.util.mail.MailerResult;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SendMailController extends FormBasicController {
	
	private static final String[] onKeys = new String[]{ "on" };
	private static final String[] contactKeys = new String[]{ GroupRoles.owner.name(), GroupRoles.coach.name(), GroupRoles.participant.name() };
	
	private RichTextElement bodyEl;
	private FileElement attachmentEl;
	private TextElement subjectEl;
	private MultipleSelectionElement contactEl;
	private MultipleSelectionElement copyFromEl;
	private FormLayoutContainer uploadCont;
	
	private int counter = 0;
	private long attachmentSize = 0l;
	private File attachementTempDir;
	private final int contactAttachmentMaxSizeInMb;
	private List<Attachment> attachments = new ArrayList<>();
	private final List<? extends RepositoryEntryRef> repoEntries;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private MailManager mailService;
	@Autowired
	private MailModule mailModule;
	@Autowired
	private RepositoryService repositoryService;
	
	public SendMailController(UserRequest ureq, WindowControl wControl, List<? extends RepositoryEntryRef> repoEntries) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.repoEntries = repoEntries;
		this.contactAttachmentMaxSizeInMb = mailModule.getMaxSizeForAttachement();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String fullName = userManager.getUserDisplayName(getIdentity());
		if(StringHelper.containsNonWhitespace(fullName)) {
			fullName = "[" + fullName + "]";
		}
		TextElement fromEl = uifactory.addTextElement("from", "contact.from", 255, fullName, formLayout);
		fromEl.setEnabled(false);
		
		String[] contactValues = new String[] {
				translate("contact.to.owner"),
				translate("contact.to.coach"),
				translate("contact.to.participant"),
		};
		contactEl = uifactory.addCheckboxesVertical("to", "contact.to", formLayout, contactKeys, contactValues, 1);
		
		subjectEl = uifactory.addTextElement("subject", "contact.subject", 255, "", formLayout);
		subjectEl.setDisplaySize(255);
		subjectEl.setMandatory(true);
		bodyEl = uifactory.addRichTextElementForStringDataMinimalistic("body", "contact.body", "", 15, 8, formLayout, getWindowControl());
		bodyEl.setMandatory(true);
		
		attachmentEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "file_upload_1", "contact.attachment", formLayout);
		attachmentEl.addActionListener(FormEvent.ONCHANGE);
		attachmentEl.setExampleKey("contact.attachment.maxsize", new String[]{ Integer.toString(contactAttachmentMaxSizeInMb) });
		
		String attachmentPage = velocity_root + "/attachments.html";
		uploadCont = FormLayoutContainer.createCustomFormLayout("file_upload_inner", getTranslator(), attachmentPage);
		uploadCont.setRootForm(mainForm);
		uploadCont.setVisible(false);
		uploadCont.contextPut("attachments", attachments);
		formLayout.add(uploadCont);
		
		copyFromEl = uifactory.addCheckboxesHorizontal("copy.from", "contact.cp.from", formLayout, onKeys, new String[] { "" });
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("tools.send.mail", buttonGroupLayout);
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
		
		contactEl.clearError();
		if(contactEl.getSelectedKeys().isEmpty()) {
			contactEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
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
		if(source == attachmentEl) {
			doUploadAttachement();
		}
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("delete".equals(cmd)) {
				Attachment attachment = (Attachment)link.getUserObject();
				doDeleteAttachment(attachment);
			}
		}
		super.formInnerEvent(ureq, source, event);
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

	@Override
	protected void formOK(UserRequest ureq) {
		ContactList contactList = new ContactList("");
		Collection<String> roleList = contactEl.getSelectedKeys();
		String[] roles = roleList.toArray(new String[roleList.size()]);
		List<Identity> identities = repositoryService.getMembers(repoEntries, RepositoryEntryRelationType.all, roles);
		if(identities.isEmpty()) {
			showWarning("error.contact.to.empty");
		} else {
			Set<Identity> deduplicates = new HashSet<>(identities);
			contactList.addAllIdentites(deduplicates);
	
			boolean success = false;
			try {
				File[] attachmentArr = getAttachments();
				MailContext context = null;
				if(repoEntries.size() == 1) {
					context = new MailContextImpl("[RepositoryEntry:" + repoEntries.get(0).getKey() + "]");
				}
				MailBundle bundle = new MailBundle();
				bundle.setContext(context);
				bundle.setFromId(getIdentity());						
				bundle.setContactLists(Collections.singletonList(contactList));
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