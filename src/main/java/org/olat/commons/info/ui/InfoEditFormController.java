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

package org.olat.commons.info.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.commons.info.InfoMessage;
import org.olat.commons.info.InfoMessageFrontendManager;
import org.olat.core.commons.modules.bc.FileUploadController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.co.ContactForm;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  26 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoEditFormController extends FormBasicController {
	
	private static final int MESSAGE_MAX_LENGTH = 32000;
	private static final String NLS_CONTACT_ATTACHMENT = "contact.attachment";
	private static final String NLS_CONTACT_ATTACHMENT_EXPL = "contact.attachment.maxsize";

	private TextElement titleEl;
	private FileElement attachmentEl;
	private RichTextElement messageEl;
	
	private String attachmentPath;
	private Set<String> attachmentPathToDelete = new HashSet<>();
	
	private File attachementTempDir;
	private long attachmentSize = 0l;
	private int contactAttachmentMaxSizeInMb = 5;
	private List<FormLink> attachmentLinks = new ArrayList<>();	
	
	private FormLayoutContainer uploadCont;
	private Map<String,String> attachmentCss = new HashMap<>();
	private Map<String,String> attachmentNames = new HashMap<>();
	
	private final boolean showTitle;
	private final InfoMessage infoMessage;
	
	@Autowired
	private InfoMessageFrontendManager infoMessageManager;
	
	public InfoEditFormController(UserRequest ureq, WindowControl wControl, Form mainForm, boolean showTitle, InfoMessage infoMessage) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, mainForm);
		this.showTitle = showTitle;
		this.infoMessage = infoMessage;
		this.attachmentPath = infoMessage.getAttachmentPath();
		if (this.attachmentPath != null) {
			attachementTempDir = FileUtils.createTempDir("attachements", null, null);
			for (File file : infoMessageManager.getAttachmentFiles(infoMessage)) {
				FileUtils.copyFileToDir(file, attachementTempDir, "Copy attachments to temp folder");
			}
		}
		setTranslator(Util.createPackageTranslator(ContactForm.class, getLocale(), getTranslator()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_info_form");
		if(showTitle) {
			setFormTitle("edit.title");
		}
		
		String title = infoMessage.getTitle();
		titleEl = uifactory.addTextElement("info_title", "edit.info_title", 512, title, formLayout);
		titleEl.setElementCssClass("o_sel_info_title");
		titleEl.setMandatory(true);

		String message = infoMessage.getMessage();
		messageEl = uifactory.addRichTextElementForStringDataMinimalistic("edit.info_message", "edit.info_message", message, 18, 80,
				formLayout, getWindowControl());
		messageEl.getEditorConfiguration().setRelativeUrls(false);
		messageEl.getEditorConfiguration().setRemoveScriptHost(false);
		messageEl.getEditorConfiguration().enableCharCount();
		messageEl.getEditorConfiguration().setPathInStatusBar(true);
		messageEl.setMandatory(true);
		messageEl.setMaxLength(MESSAGE_MAX_LENGTH);
		
		String attachmentPage = Util.getPackageVelocityRoot(this.getClass()) + "/attachments.html";
		uploadCont = FormLayoutContainer.createCustomFormLayout("file_upload_inner", getTranslator(), attachmentPage);
		uploadCont.setRootForm(mainForm);
		formLayout.add(uploadCont);
		
		attachmentEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "attachment", formLayout);
		attachmentEl.setDeleteEnabled(true);
		attachmentEl.setMaxUploadSizeKB(5000, "attachment.max.size", new String[] { "5000" });
		
		attachmentEl.addActionListener(FormEvent.ONCHANGE);
		
		if(infoMessage.getAttachmentPath() != null) {			
			for (VFSLeaf file : infoMessageManager.getAttachments(infoMessage)) {
				attachmentSize += file.getSize();
				FormLink removeFile = uifactory.addFormLink(file.getName(), "delete", null, uploadCont, Link.BUTTON_XSMALL);
				removeFile.setIconLeftCSS("o_icon o_icon-fw o_icon_delete");
				removeFile.setUserObject(file);
				attachmentLinks.add(removeFile);
				//pretty labels
				uploadCont.setLabel(NLS_CONTACT_ATTACHMENT, null);
				attachmentNames.put(file.getName(), file.getName() + " <span class='text-muted'>(" + Formatter.formatBytes(file.getSize()) + ")</span>");
				attachmentCss.put(file.getName(), CSSHelper.createFiletypeIconCssClassFor(file.getName()));
			}
			
			uploadCont.contextPut("attachments", attachmentLinks);
			uploadCont.contextPut("attachmentNames", attachmentNames);
			uploadCont.contextPut("attachmentCss", attachmentCss);
			attachmentEl.setLabel(null, null);
		}	
	}
	
	@Override
	protected void doDispose() {
		cleanUpAttachments();
        super.doDispose();
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == attachmentEl) {
			String filename = attachmentEl.getUploadFileName();
			if(attachementTempDir == null) {
				attachementTempDir = FileUtils.createTempDir("attachements", null, null);
			}
			
			long size = attachmentEl.getUploadSize();
			if(size + attachmentSize > (contactAttachmentMaxSizeInMb  * 1024 * 1024)) {
				showWarning(NLS_CONTACT_ATTACHMENT_EXPL, Integer.toString(contactAttachmentMaxSizeInMb));
				attachmentEl.reset();
			} else {
				File attachment = attachmentEl.moveUploadFileTo(attachementTempDir);
				// OO-48  somehow file-move can fail, check for it, display error-dialog if it failed
				if(attachment == null){
					attachmentEl.reset();
					logError("Could not move contact-form attachment to " + attachementTempDir.getAbsolutePath(), null);
					setTranslator(Util.createPackageTranslator(FileUploadController.class, getLocale(),getTranslator()));
					showError("FileMoveCopyFailed","");
					return;
				}
				attachmentEl.reset();
				attachmentSize += size;
				FormLink removeFile = uifactory.addFormLink(attachment.getName(), "delete", null, uploadCont, Link.BUTTON_XSMALL);
				removeFile.setIconLeftCSS("o_icon o_icon-fw o_icon_delete");
				removeFile.setUserObject(attachment);
				attachmentLinks.add(removeFile);
				//pretty labels
				uploadCont.setLabel(NLS_CONTACT_ATTACHMENT, null);
				attachmentNames.put(attachment.getName(), filename + " <span class='text-muted'>(" + Formatter.formatBytes(size) + ")</span>");
				attachmentCss.put(attachment.getName(), CSSHelper.createFiletypeIconCssClassFor(filename));
				uploadCont.contextPut("attachments", attachmentLinks);
				uploadCont.contextPut("attachmentNames", attachmentNames);
				uploadCont.contextPut("attachmentCss", attachmentCss);
				attachmentEl.setLabel(null, null);
			}
		} else if (attachmentLinks.contains(source)) {
			Object uploadedFile = source.getUserObject();
		
			if (uploadedFile instanceof File) {
				File file = (File) uploadedFile;
				if(file.exists()) {
					attachmentSize -= file.length();
					attachmentPathToDelete.add(file.getName());
				}
			} else if (uploadedFile instanceof VFSLeaf) {
				VFSLeaf leaf = (VFSLeaf) uploadedFile;
				if(leaf.exists()) {
					attachmentSize -= leaf.getSize();
					attachmentPathToDelete.add(leaf.getName());
				}
			} else {
				return;
			}
			
			attachmentLinks.remove(source);
			uploadCont.remove(source);
			if(attachmentLinks.isEmpty()) {
				uploadCont.setLabel(null, null);
				attachmentEl.setLabel(NLS_CONTACT_ATTACHMENT, null);
			}
		} 
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		titleEl.clearError();
		messageEl.clearError();
		boolean allOk = super.validateFormLogic(ureq);
		
		String t = titleEl.getValue();
		if(!StringHelper.containsNonWhitespace(t)) {
			titleEl.setErrorKey("form.legende.mandatory", new String[] {});
			allOk &= false;
		} else if (t.length() > 500) {
			titleEl.setErrorKey("input.toolong", new String[] {"500", Integer.toString(t.length())});
			allOk &= false;
		}
		
		String m = messageEl.getValue();
		if(!StringHelper.containsNonWhitespace(m)) {
			messageEl.setErrorKey("form.legende.mandatory", new String[] {});
			allOk &= false;
		} else if (m.length() > MESSAGE_MAX_LENGTH) {
			messageEl.setErrorKey("input.toolong", new String[] { Integer.toString(MESSAGE_MAX_LENGTH), Integer.toString(m.length()) });
			allOk &= false;
		}
		
		List<ValidationStatus> validation = new ArrayList<>();
		attachmentEl.validate(validation);
		if(validation.size() > 0) {
			allOk &= false;
		}
		return allOk;
	}
	
	public InfoMessage getInfoMessage() {
		infoMessage.setTitle(titleEl.getValue());
		infoMessage.setMessage(messageEl.getValue());
		infoMessage.setAttachmentPath(attachmentPath);
		return infoMessage;
	}
	
	public File getAttachements() {
		return attachementTempDir;
	}
	
	public Collection<String> getAttachmentPathToDelete() {
		return attachmentPathToDelete;	
	}

	@Override
	public FormLayoutContainer getInitialFormItem() {
		return flc;
	}
	
	public void cleanUpAttachments() {
 		if(attachementTempDir != null && attachementTempDir.exists()) {
			FileUtils.deleteDirsAndFiles(attachementTempDir, true, true);
			attachementTempDir = null;
		}
 	}
}
