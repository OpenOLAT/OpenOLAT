/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.certificationprogram.ui;

import java.io.File;
import java.io.InputStream;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.commons.persistence.DB;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.StreamedMediaResource;
import org.olat.core.gui.media.ZippedDirectoryMediaResource;
import org.olat.core.util.vfs.JavaIOItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.model.PreviewCertificate;
import org.olat.course.certificate.ui.CertificateChooserController;
import org.olat.course.certificate.ui.PreviewMediaResource;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class EditCertificationProgramCertificateController extends FormBasicController {
	
	private TextElement certificationCustom1El;
	private TextElement certificationCustom2El;
	private TextElement certificationCustom3El;
	private FormLayoutContainer templateCont;
	
	private FormLink selectTemplateLink;
	private Link previewTemplateLink;

	private final String mapperUrl;
	private CertificateTemplate selectedTemplate;
	private CertificationProgram certificationProgram;
	
	private CloseableModalController cmc;
	private CertificateChooserController certificateChooserCtrl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	public EditCertificationProgramCertificateController(UserRequest ureq, WindowControl wControl, CertificationProgram certificationProgram) {
		super(ureq, wControl);
		this.certificationProgram = certificationProgram;
		mapperUrl = registerMapper(ureq, new TemplateMapper());
		
		initForm(ureq);
		updateUI();
	}
	
	public CertificationProgram getCertificationProgram() {
		return certificationProgram;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		certificationCustom1El = uifactory.addTextElement("certificate.custom1", 1000, certificationProgram.getCertificateCustom1(), formLayout);
		certificationCustom2El = uifactory.addTextElement("certificate.custom2", 2000, certificationProgram.getCertificateCustom2(), formLayout);
		certificationCustom3El = uifactory.addTextElement("certificate.custom3", 3000, certificationProgram.getCertificateCustom3(), formLayout);
		
		String templatePage = velocity_root + "/select_certificate.html";
		templateCont = uifactory.addCustomFormLayout("template.cont", null, templatePage, formLayout);
		templateCont.contextPut("mapperUrl", mapperUrl);
		templateCont.setLabel("certificate.pdf.template", null);
		
		selectTemplateLink = uifactory.addFormLink("select", templateCont, Link.BUTTON);
		selectedTemplate = certificationProgram.getTemplate();
		if(selectedTemplate != null) {
			templateCont.contextPut("templateName", selectedTemplate.getName());
		} else {
			templateCont.contextPut("templateName", translate("default.template"));
		}
		
		previewTemplateLink = LinkFactory.createButton("preview", templateCont.getFormItemComponent(), this);
		previewTemplateLink.setTarget("preview");
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private void updateUI() {
		if(selectedTemplate == null) {
			templateCont.contextPut("templateName", translate("default.template"));
			previewTemplateLink.setEnabled(false);
		} else {
			templateCont.contextPut("templateName", selectedTemplate.getName());
			previewTemplateLink.setEnabled(true);
		}
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == previewTemplateLink) {
			doPreviewTemplate(ureq);
		} 
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == certificateChooserCtrl) {
			if(event == Event.DONE_EVENT) {
				doSetTemplate(certificateChooserCtrl.getSelectedTemplate());
				updateUI();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(certificateChooserCtrl);
		removeAsListenerAndDispose(cmc);
		certificateChooserCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(selectTemplateLink == source) {
			doSelectTemplate(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		certificationProgram = certificationProgramService.getCertificationProgram(certificationProgram);
		
		certificationProgram.setCertificateCustom1(certificationCustom1El.getValue());
		certificationProgram.setCertificateCustom2(certificationCustom2El.getValue());
		certificationProgram.setCertificateCustom3(certificationCustom3El.getValue());
		certificationProgram.setTemplate(selectedTemplate);
		
		certificationProgram = certificationProgramService.updateCertificationProgram(certificationProgram);
		dbInstance.commitAndCloseSession();
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doSelectTemplate(UserRequest ureq) {
		certificateChooserCtrl = new CertificateChooserController(ureq, getWindowControl(), selectedTemplate);
		listenTo(certificateChooserCtrl);
		
		String title = translate("choose.certificate.template.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), certificateChooserCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();	
	}
	
	private void doSetTemplate(CertificateTemplate template) {
		this.selectedTemplate = template;
		if(selectedTemplate == null) {
			templateCont.contextPut("templateName", translate("default.template"));
			previewTemplateLink.setEnabled(false);
		} else {
			templateCont.contextPut("templateName", template.getName());
			previewTemplateLink.setEnabled(true);
		}
	}
	
	private void doPreviewTemplate(UserRequest ureq) {
		selectedTemplate = certificatesManager.getTemplateById(selectedTemplate.getKey());
		String custom1 = certificationCustom1El.getValue();
		String custom2 = certificationCustom2El.getValue();
		String custom3 = certificationCustom3El.getValue();
		PreviewCertificate preview = certificatesManager.previewCertificate(selectedTemplate, certificationProgram, getLocale(), custom1, custom2, custom3);
		MediaResource resource = new PreviewMediaResource(preview);
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	public class TemplateMapper implements Mapper {
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			MediaResource resource;
			if(selectedTemplate != null) {
				VFSLeaf templateLeaf = certificatesManager.getTemplateLeaf(selectedTemplate);
				if(templateLeaf.getName().equals("index.html") && templateLeaf instanceof JavaIOItem indexFile) {
					File templateDir = indexFile.getBasefile().getParentFile();
					resource = new ZippedDirectoryMediaResource(selectedTemplate.getName(), templateDir);
				} else {
					resource = new VFSMediaResource(templateLeaf); 
				}
			} else {
				InputStream stream = certificatesManager.getDefaultTemplate();
				resource = new StreamedMediaResource(stream, "Certificate_template.pdf", "application/pdf");
			}
			return resource;
		}
	}
	
}
