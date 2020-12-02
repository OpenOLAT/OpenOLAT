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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.commons.info.InfoMessage;
import org.olat.commons.info.InfoMessageFrontendManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FileElementEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ValidationStatus;
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

	private TextElement titleEl;
	private FileElement attachmentEl;
	private RichTextElement messageEl;
	
	private String attachmentPath;
	private Set<String> attachmentPathToDelete = new HashSet<>();
	
	private final boolean showTitle;
	private final InfoMessage infoMessage;
	
	@Autowired
	private InfoMessageFrontendManager infoMessageFrontendManager;
	
	public InfoEditFormController(UserRequest ureq, WindowControl wControl, Form mainForm, boolean showTitle, InfoMessage infoMessage) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, mainForm);
		this.showTitle = showTitle;
		this.infoMessage = infoMessage;
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
		
		attachmentEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "attachment", formLayout);
		attachmentEl.setDeleteEnabled(true);
		attachmentEl.setMaxUploadSizeKB(5000, "attachment.max.size", new String[] { "5000" });
		
		attachmentEl.addActionListener(FormEvent.ONCHANGE);
		if(infoMessage.getAttachmentPath() != null) {
			attachmentPath = infoMessage.getAttachmentPath();
			String filename = attachmentPath;
			int lastIndex = filename.lastIndexOf('/');
			if(lastIndex > 0) {
				filename = filename.substring(lastIndex + 1);
			}
			attachmentEl.setInitialFile(new File(filename));
		}
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
		if(attachmentEl == source) {
			if(FileElementEvent.DELETE.equals(event.getCommand())) {
				attachmentPathToDelete.add(attachmentPath);
				attachmentEl.reset();
				attachmentEl.setInitialFile(null);
				attachmentEl.clearError();
				attachmentPath = null;
			} else if(attachmentEl.isUploadSuccess()) {
				File uploadedFile = attachmentEl.getUploadFile();
				String uploadedFilename = attachmentEl.getUploadFileName();
				if(attachmentPath != null) {
					attachmentPathToDelete.add(attachmentPath);
				}
				attachmentPath = infoMessageFrontendManager.storeAttachment(uploadedFile, uploadedFilename, infoMessage.getOLATResourceable(), infoMessage.getResSubPath());
			}
			fireEvent(ureq, Event.CHANGED_EVENT);
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
	
	public Collection<String> getAttachmentPathToDelete() {
		if(attachmentPath != null) {
			attachmentPathToDelete.remove(attachmentPath);
		}
		return attachmentPathToDelete;	
	}

	@Override
	public FormLayoutContainer getInitialFormItem() {
		return flc;
	}
}
