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
package org.olat.course.certificate.ui;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificatesManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificateChooserController extends FormBasicController {
	
	private FileElement fileEl;
	private FormLink selectLink, uploadLink;
	private SingleSelection publicTemplatesEl;

	@Autowired
	private CertificatesManager certificatesManager;
	
	private CertificateTemplate selectedTemplate;
	
	public CertificateChooserController(UserRequest ureq, WindowControl wControl, CertificateTemplate template) {
		super(ureq, wControl);
		
		this.selectedTemplate = template;
		
		initForm(ureq);
	}
	
	public CertificateTemplate getSelectedTemplate() {
		return selectedTemplate;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		List<CertificateTemplate> templates = certificatesManager.getTemplates();
		String[] templatesKeys = new String[templates.size() + 1];
		String[] templatesValues = new String[templates.size() + 1];
		templatesKeys[0] = "def";
		templatesValues[0] = "Default";
		
		int count = 1;
		for(CertificateTemplate template:templates) {
			templatesKeys[count] = template.getKey().toString();
			templatesValues[count++] = template.getName();
		}
		
		publicTemplatesEl = uifactory.addDropdownSingleselect("public.templates", formLayout, templatesKeys, templatesValues, null);
		if(selectedTemplate != null) {
			String selectedTemplateKey = selectedTemplate.getKey().toString();
			for(String templateKey:templatesKeys) {
				if(templateKey.equals(selectedTemplateKey)) {
					publicTemplatesEl.select(templateKey, true);
				}
			}
		}
		
		FormLayoutContainer selectButtonCont = FormLayoutContainer.createButtonLayout("selectButton", getTranslator());
		selectButtonCont.setRootForm(mainForm);
		formLayout.add(selectButtonCont);
		selectLink = uifactory.addFormLink("select", selectButtonCont, Link.BUTTON);
		
		uifactory.addSpacerElement("spaceman", formLayout, false);
		
		fileEl = uifactory.addFileElement("template.file", formLayout);
		fileEl.addActionListener(FormEvent.ONCHANGE);
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		uploadLink = uifactory.addFormLink("upload", buttonCont, Link.BUTTON);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
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
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(selectLink == source) {
			if(publicTemplatesEl.isOneSelected()) {
				
				String selectedkey = publicTemplatesEl.getSelectedKey();
				if("def".equals(selectedkey)) {
					
				} else if(StringHelper.isLong(selectedkey)) {
					try {
						Long key = new Long(selectedkey);
						selectedTemplate = certificatesManager.getTemplateById(key);
					} catch (NumberFormatException e) {
						logError("", e);
					}
				}
			}
			
			if(selectedTemplate != null) {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if(uploadLink == source) {
			if(validatePdf()) {
				doUpload(ureq);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doUpload(UserRequest ureq) {
		File template = fileEl.getUploadFile();
		if(template != null) {
			String name = fileEl.getUploadFileName();
			selectedTemplate = certificatesManager.addTemplate(name, template, false);
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
	
	private boolean validatePdf() {
		boolean allOk = true;
		
		File pdf = fileEl.getUploadFile();
		fileEl.clearError();
		if(pdf != null && pdf.exists()) {
			PDDocument document = null;
			try (InputStream in = Files.newInputStream(pdf.toPath())) {		
				document = PDDocument.load(in);
				if (document.isEncrypted()) {
					fileEl.setErrorKey("upload.error.encrypted", null);
					allOk &= false;
				}
			} catch(Exception ex) {
				logError("", ex);
				fileEl.setErrorKey("upload.unkown.error", null);
				allOk &= false;
			} finally {
				IOUtils.closeQuietly(document);
			}
		}
		
		return allOk;
	}
}