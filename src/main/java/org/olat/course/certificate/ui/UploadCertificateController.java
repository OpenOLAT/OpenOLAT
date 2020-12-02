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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.PathUtils;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.fileresource.types.FileResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UploadCertificateController extends FormBasicController {
	
	protected static final String[] formatKeys = new String[]{ "A4", "Letter" };
	protected static final String[] orientationKeys = new String[]{ "portrait", "landscape" };
	
	protected FileElement fileEl;
	protected SingleSelection orientationEl, formatEl;
	private CertificateTemplate templateToUpdate;
	
	@Autowired
	private CertificatesManager certificatesManager;
	
	public UploadCertificateController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}
	
	public UploadCertificateController(UserRequest ureq, WindowControl wControl, CertificateTemplate template) {
		super(ureq, wControl);
		this.templateToUpdate = template;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "template.file", formLayout);
		fileEl.addActionListener(FormEvent.ONCHANGE);
		
		String[] orientationValues = new String[]{
			translate(orientationKeys[0]), translate(orientationKeys[1])
		};
		orientationEl = uifactory.addRadiosVertical("orientation", formLayout, orientationKeys, orientationValues);
		orientationEl.select(orientationKeys[0], true);
		orientationEl.setVisible(false);
		
		formatEl = uifactory.addRadiosVertical("format", formLayout, formatKeys, formatKeys);
		formatEl.select(formatKeys[0], true);
		formatEl.setVisible(false);
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		uifactory.addFormSubmitButton("save", buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}
	
	public String getFormat() {
		String format = null;
		if(formatEl.isVisible() && formatEl.isOneSelected()) {
			format = formatEl.getSelectedKey();
		}
		return format;
	}
	
	public String getOrientation() {
		String format = null;
		if(orientationEl.isVisible() && orientationEl.isOneSelected()) {
			format = orientationEl.getSelectedKey();
		}
		return format;
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		File template = fileEl.getUploadFile();
		if(template != null) {
			String name = fileEl.getUploadFileName();
			if(templateToUpdate == null) {
				certificatesManager.addTemplate(name, template, getFormat(), getOrientation(), true, getIdentity());
			} else {
				certificatesManager.updateTemplate(templateToUpdate, name, template, getFormat(), getOrientation(), getIdentity());
			}
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == fileEl) {
			validateTemplate();
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validateTemplate();
		return allOk;
	}

	protected boolean validateTemplate() {
		boolean allOk = true;
		
		File template = fileEl.getUploadFile();
		fileEl.clearError();
		formatEl.setVisible(false);
		orientationEl.setVisible(false);
		if(template != null && template.exists()) {
			String filename = fileEl.getUploadFileName().toLowerCase();
			if(filename.endsWith(".pdf")) {
				allOk = validatePdf(template);
			} else if(filename.endsWith(".zip")) {
				allOk = validateHtml(filename, template);
			} else {
				fileEl.setErrorKey("upload.wrong.mimetype", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	private boolean validateHtml(String filename, File template) {
		boolean allOk = true;
		try {
			if(certificatesManager.isHTMLTemplateAllowed()) {
				Path path = FileResource.getResource(template, filename);
				IndexVisitor visitor = new IndexVisitor(path);
				Files.walkFileTree(path, visitor);
				if(!visitor.hasFound() || path.getNameCount() > 0) {
					fileEl.setErrorKey("upload.error.noindex", null);
					allOk &= false;
				}
				PathUtils.closeSubsequentFS(path);
				formatEl.setVisible(allOk);
				orientationEl.setVisible(allOk);
			} else {
				fileEl.setErrorKey("upload.error.no.html.templates", null);
				allOk &= false;
			}
		} catch (Exception e) {
			logError("", e);
			fileEl.setErrorKey("upload.unkown.error", null);
			allOk = false;
		}
		return allOk;
	}
	
	private boolean validatePdf(File template) {
		boolean allOk = true;
		
		try (InputStream in = Files.newInputStream(template.toPath());
				PDDocument document = PDDocument.load(in)) {		
			
			if (document.isEncrypted()) {
				fileEl.setErrorKey("upload.error.encrypted", null);
				allOk &= false;
			} else {
				//check if we can write the form
				PDDocumentCatalog docCatalog = document.getDocumentCatalog();
				PDAcroForm acroForm = docCatalog.getAcroForm();
				if (acroForm != null) {
					List<PDField> fields = acroForm.getFields();
					for(PDField field:fields) {
						field.setValue("test");
					}
				}
				document.save(new DevNullOutputStream());
			}
		} catch(IOException ex) {
			logError("", ex);
			if(ex.getMessage() != null && ex.getMessage().contains("Don't know how to calculate the position for non-simple fonts")) {
				fileEl.setErrorKey("upload.error.simplefonts", null);
			} else {
				fileEl.setErrorKey("upload.unkown.error", null);
			}
			allOk &= false;
		} catch(Exception ex) {
			logError("", ex);
			fileEl.setErrorKey("upload.unkown.error", null);
			allOk &= false;
		}
		
		return allOk;
	}
	
	private static class DevNullOutputStream extends OutputStream {
		@Override
        public void write(int b){
			//
		}
    }
	
	private static class IndexVisitor extends SimpleFileVisitor<Path> {
		
		private final Path root;
		private boolean found;
		
		public IndexVisitor(Path root) {
			this.root = root;
		}

		public boolean hasFound() {
			return found;
		}
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
		throws IOException {
			if(file.getParent().equals(root) && file.getFileName().toString().equals("index.html")) {
				found = true;
			}
			
			return FileVisitResult.CONTINUE;
		}
	}
}
