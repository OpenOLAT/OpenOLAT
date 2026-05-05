/**

 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.app_wizard;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.DeleteFileElementEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.DocumentOption;
import org.olat.modules.selectus.DocumentType;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.pdf.PDFUtility;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.RecruitingMainController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  11 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class DocumentsController extends FormBasicController {

	private List<DocumentElement> fileElements = new ArrayList<>();
	private FileElement combinedFileEl;
	
	private final boolean admin;
	private final boolean segmented;
	private final boolean editable;
	private Application application;
	private final boolean onlyPdfs;
	private final Position position;
	private final TabConfiguration tabConfiguration;
	private final Map<DocumentEnum,List<DocumentType>> docTypes;
	
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService erFrontendManager;

	public DocumentsController(UserRequest ureq, WindowControl wControl, Form rootForm, Position position,
			Application application, TabConfiguration tabConfiguration, boolean admin, boolean segmented, boolean editable) {
		super(ureq, wControl, null, Util.createPackageTranslator(RecruitingMainController.class, ureq.getLocale()));
		this.segmented = segmented;
		if(rootForm != null) {
			mainForm = rootForm;
			flc.setRootForm(rootForm);
			mainForm.addSubFormListener(this);
		}
		flc.setEnabled(editable);
		this.admin = admin;
		this.editable = editable;
		this.application = application;
		this.tabConfiguration = tabConfiguration == null
				? application.getPosition().getTabConfiguration(Tab.documents) : tabConfiguration;
		
		// allowed document types
		this.position = position;
		docTypes = position.getDocumentTypes();
		onlyPdfs = DocumentType.isOnlyPDFs(docTypes);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(!segmented) {
			setFormTitle("wizard.documents.legend", new String[] { StringHelper.escapeHtml(position.getMLTitle(getLocale())) });
		}
		
		String explanation = tabConfiguration.getHelp(getLocale());
		if(StringHelper.containsNonWhitespace(explanation)) {
			setFormTranslatedDescription(RecruitingHelper.escWithBR(explanation));
		} else if(StringHelper.containsNonWhitespace(translate("wizard.documents.explanation"))) {
			setFormDescription("wizard.documents.explanation");
		}

		Position position = application.getPosition();
		Set<String> available = position.getAvailableDocuments();
		Set<String> staffOnly = position.getStaffDocuments();
		Set<String> mandatory = position.getMandatoryDocuments();
		
		DocumentOption docCombinedOption = null;
		for(DocumentOption docOption:recruitingModule.getDocumentOptions(position)) {
			DocumentEnum doc = docOption.getDoc();
			if(doc == DocumentEnum.combined) {
				docCombinedOption = docOption;
				continue;
			}
			
			if(staffOnly.contains(doc.name()) && !admin) {
				continue;
			}
			if(!available.contains(doc.name()) && !mandatory.contains(doc.name())) {
				continue;
			}

			String i18nKey = doc.i18nKey();
			if(DocumentEnum.other.equals(doc)) {
				uifactory.addSpacerElement("doc-spacer", formLayout, false);
				i18nKey += ".long";
			}
			
			String page = velocity_root + "/upload.html";
			FormLayoutContainer uploadCont = FormLayoutContainer.createCustomFormLayout("upload-" + doc.name(), getTranslator(), page);
			uploadCont.setRootForm(mainForm);
			
			String documentName = position.getDocumentName(doc, getLocale());
			if(StringHelper.containsNonWhitespace(documentName)) {
				uploadCont.setLabel(documentName, null, false);
			} else {
				uploadCont.setLabel(i18nKey, null);
			}
			formLayout.add(uploadCont);

			
			FileElement fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "upload", null, uploadCont);
			initLimitToMimeType(doc, fileEl);
			fileEl.setDeleteEnabled(true);
			fileEl.setMaxUploadSizeKB(docOption.getMaxSize() * 1000l, "error.upload.maxsize",
					new String[] { Integer.toString(docOption.getMaxSize())} );
			fileEl.addActionListener(FormEvent.ONCHANGE);
			fileEl.setEnabled(editable);
			
			initUploadExplain(doc, fileEl);

			Attachment attachment = doc.path(application);
			if(attachment != null) {
				initAttachment(attachment, doc, fileEl);
			}
			if(!admin && mandatory.contains(doc.name())) {
				uploadCont.setMandatory(true);
				fileEl.setMandatory(true, "file.mandatory");
			}
			DocumentElement docEl = new DocumentElement(doc, fileEl);
			fileElements.add(docEl);
		}
		
		if(admin && docCombinedOption != null) {
			initFormCombined(docCombinedOption, formLayout);
		}
	}

	private void initFormCombined(DocumentOption docCombinedOption, FormItemContainer formLayout) {
		String i18nKey = "edit.application.document.combined";
		String page = velocity_root + "/upload.html";
		FormLayoutContainer uploadCont = FormLayoutContainer.createCustomFormLayout("upload-" + DocumentEnum.combined.name(), getTranslator(), page);
		uploadCont.setRootForm(mainForm);
		uploadCont.setLabel(i18nKey, null);
		formLayout.add(uploadCont);
		
		combinedFileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "upload", i18nKey, uploadCont);
		initLimitToMimeType(DocumentEnum.combined, combinedFileEl);
		combinedFileEl.setMaxUploadSizeKB(docCombinedOption.getMaxSize() * 1000l, "error.upload.maxsize",
				new String[] { Integer.toString(docCombinedOption.getMaxSize())} );
		combinedFileEl.addActionListener(FormEvent.ONCHANGE);
		combinedFileEl.setEnabled(editable);
		combinedFileEl.setDeleteEnabled(editable);

		DocumentElement docEl = new DocumentElement(DocumentEnum.combined, combinedFileEl);
		fileElements.add(docEl);

		initUploadExplain(DocumentEnum.combined, combinedFileEl);
		
		Attachment attachment = DocumentEnum.combined.path(application);
		if(attachment != null) {
			initAttachment(attachment, DocumentEnum.combined, combinedFileEl);
		}
	}
	
	private void initLimitToMimeType(DocumentEnum doc, FileElement fileEl) {
		List<DocumentType> types = docTypes.get(doc);
		Set<String> allowedMimeTypes = DocumentType.toMimeTypes(docTypes.get(doc));
		
		if(types == null || types.isEmpty()) {
			fileEl.limitToMimeType(allowedMimeTypes, "error.file.type", new String[] { DocumentType.toString(DocumentType.pdf) });
		} else {
			String[] typeArgs = DocumentType.toString(types);
			if(typeArgs.length == 3) {
				fileEl.limitToMimeType(allowedMimeTypes, "error.file.type.3", typeArgs);
			} else if(typeArgs.length == 2) {
				fileEl.limitToMimeType(allowedMimeTypes, "error.file.type.2", typeArgs);
			} else {
				fileEl.limitToMimeType(allowedMimeTypes, "error.file.type", typeArgs);
			}
		}
	}
	
	private void initAttachment(Attachment attachment, DocumentEnum doc, FileElement fileEl) {
		String filename = attachment.getName();
		if(!StringHelper.containsNonWhitespace(filename)) {
			String type = attachment.getType();
			if(!StringHelper.containsNonWhitespace(type)) {
				type = "pdf";
			}
			filename = doc.name() + "." + type;
		}
		File file = new File(filename);
		fileEl.setInitialFile(file);
	}
	
	private void initUploadExplain(DocumentEnum doc, FileElement fileEl) {
		List<DocumentType> types = docTypes.get(doc);
		String explain = position.getDocumentExplain(doc, getLocale());
		if(!StringHelper.containsNonWhitespace(explain)) {
			explain = translate(doc.i18nExplainKey());
		}
		String explainTypes;
		if(onlyPdfs) {
			explainTypes = null;
		} else if(types != null && !types.isEmpty()) {
			explainTypes = DocumentType.toFlatString(types);
		} else {
			explainTypes = DocumentType.toFlatString(Collections.singletonList(DocumentType.pdf));
		}

		if(explain != null && explain.length() > 2 && explain.length() < 1024) {
			if(StringHelper.containsNonWhitespace(explainTypes)) {
				fileEl.setExampleKey("edit.application.document.type.wrapper", new String[] { explain, explainTypes });
			} else {
				fileEl.setExampleKey("edit.application.document.type.explain", new String[] { explain, "" });
			}
		} else if(StringHelper.containsNonWhitespace(explainTypes)) {
			fileEl.setExampleKey("edit.application.document.type.wrapper", new String[] { "", explainTypes });
		}
	}
	
	@Override
	protected void doDispose() {
		mainForm.removeSubFormListener(this);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		for(DocumentElement fileEl:fileElements) {
			allOk &= validEncryption(ureq, fileEl.getFileElement());
		}
		if(combinedFileEl != null) {
			allOk &= validEncryption(ureq, combinedFileEl);
		}
		return allOk;
	}
	
	private boolean validEncryption(UserRequest ureq, FileElement fileEl) {
		File file = fileEl.getUploadFile();
		boolean ok = true;
		
		ok &= validateFormItem(ureq, fileEl);
		
		if(ok) {
			if(file != null && file.exists() && getFileType(fileEl) == DocumentType.pdf) {
				ok &= !PDFUtility.isEncrypted(file);
			}
			if(!ok) {
				fileEl.setErrorKey("error.encrypted_file");
			}
		}
		return ok;
	}

	public Application commitChanges(Application app) {
		for(DocumentElement docEl:fileElements) {
			DocumentEnum doc = docEl.getDoc();
			
			FileElement fileEl = docEl.getFileElement();
			File file = fileEl.getUploadFile();
			String filename = fileEl.getUploadFileName();
			if(file != null && file.exists()) {
				DocumentType docType = getFileType(fileEl);
				uploadDocument(app, file, filename, docType, doc);
			} else if(docEl.isDelete()) {
				deleteDocument(app, doc);
			}
		}
		application = erFrontendManager.saveTempApplication(app, false);
		return application;
	}
	
	private DocumentType getFileType(FileElement fileEl) {
		String mimeType = fileEl.getUploadMimeType();
		String filename = fileEl.getUploadFileName();
		return DocumentType.valueOf(filename, mimeType);
	}
	
	private void deleteDocument(Application app, DocumentEnum doc) {
		try {
			Attachment attachment = doc.path(app);
			if(attachment != null) {
				erFrontendManager.removeAttachmentDatas(app, attachment);
				doc.setPath(app, null);
			}
		} catch (Exception e) {
			logError("", e);
		}
	}
	
	private void uploadDocument(Application app, File file, String filename, DocumentType fileType, DocumentEnum doc) {
		try(FileInputStream fis = new FileInputStream(file)) {
			byte[] datas = new byte[(int)file.length()];
			fis.read(datas);
			
			Attachment attachment = doc.path(app);
			if(attachment == null) {
				attachment = erFrontendManager.setAttachmentDatas(app, attachment, datas, filename, fileType);
				doc.setPath(app, attachment);
			} else {
				erFrontendManager.setAttachmentDatas(app, attachment, datas, filename, fileType);
			}
		} catch (Exception e) {
			logError("", e);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FileElement) {
			FileElement fileEl = (FileElement)source;
			if(fileEl.getUserObject() instanceof DocumentElement) {
				DocumentElement docEl = (DocumentElement)fileEl.getUserObject();
				if(event instanceof DeleteFileElementEvent) {
					if(fileEl.getInitialFile() != null) {
						if(fileEl.getUploadFile() != null) {
							fileEl.reset();
						} else {
							docEl.setDelete(true);
							fileEl.setInitialFile(null);
						}	
					} else if (fileEl.getUploadFile() != null) {
						fileEl.reset();
					}
				} else {
					docEl.setDelete(false);
				}
			}
		}
	}

	public static class DocumentElement {
		
		private boolean delete;
		private final DocumentEnum doc;
		private final FileElement fileElement;
		
		public DocumentElement(DocumentEnum doc, FileElement fileElement) {
			this.doc = doc;
			this.fileElement = fileElement;
			fileElement.setUserObject(this);
		}

		public boolean isDelete() {
			return delete;
		}

		public void setDelete(boolean delete) {
			this.delete = delete;
		}

		public DocumentEnum getDoc() {
			return doc;
		}

		public FileElement getFileElement() {
			return fileElement;
		}
	}
}
