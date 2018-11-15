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
package org.olat.modules.portfolio.ui;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.core.util.vfs.filters.SystemItemFilter;
import org.olat.modules.forms.handler.EvaluationFormResource;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.AssignmentType;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.manager.PortfolioFileStorage;
import org.olat.modules.portfolio.model.SectionKeyRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssignmentEditController extends FormBasicController {

	public static final String[] typeKeys = new String[] {
			AssignmentType.essay.name(),
			//AssignmentType.document.name(),
			AssignmentType.form.name()
	};

	public static final String[] onKeys = new String[] { "on" };
	public static final String[] evaKeys = new String[] { "only-autoevaluation", "alien-evaluation"};
	
	private TextElement titleEl;
	private RichTextElement summaryEl;
	private SingleSelection typeEl;
	private SingleSelection sectionsEl;
	private FileElement documentUploadEl;
	private FormLayoutContainer filesLayout, selectFormLayout;
	private RichTextElement contentEl;
	private FormLink selectFormButton;
	private SingleSelection evaluationFormEl;
	private MultipleSelectionElement reviewerSeeAutoEvaEl;
	private MultipleSelectionElement anonymousExternEvaEl;
	
	private CloseableModalController cmc;
	private ReferencableEntriesSearchController searchFormCtrl;
	
	private Binder binder;
	private Section section;
	private Assignment assignment;
	private RepositoryEntry formEntry;
	private VFSContainer tempUploadFolder;
	private VFSContainer documentContainer;

	private String mapperUri;
	
	private boolean assignmentInUse;
	
	@Autowired
	private PortfolioFileStorage fileStorage;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private PortfolioFileStorage portfolioFileStorage;
	
	public AssignmentEditController(UserRequest ureq, WindowControl wControl, Binder binder) {
		super(ureq, wControl);
		this.binder = binder;
		assignmentInUse = false;
		initForm(ureq);
	}
	
	public AssignmentEditController(UserRequest ureq, WindowControl wControl, Section section) {
		super(ureq, wControl);
		this.section = section;
		assignmentInUse = false;
		initForm(ureq);
	}
	
	public AssignmentEditController(UserRequest ureq, WindowControl wControl, Assignment assignment) {
		super(ureq, wControl);
		this.assignment = assignment;
		assignmentInUse = portfolioService.isAssignmentInUse(assignment);
		formEntry = assignment.getFormEntry();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_pf_edit_assignment_form");
		
		if(assignmentInUse) {
			setFormWarning("assignment.in.use");
		}
		
		String title = assignment == null ? null : assignment.getTitle();
		titleEl = uifactory.addTextElement("title", "assignment.title", 255, title, formLayout);
		titleEl.setElementCssClass("o_sel_pf_edit_assignment_title");
		titleEl.setMandatory(true);
		
		String summary = assignment == null ? null : assignment.getSummary();
		summaryEl = uifactory.addRichTextElementForStringDataMinimalistic("summary", "summary", summary, 8, 60, formLayout, getWindowControl());
		summaryEl.setElementCssClass("o_sel_pf_edit_assignment_summary");
		summaryEl.setPlaceholderKey("summary.placeholder", null);
		summaryEl.getEditorConfiguration().setPathInStatusBar(false);
		
		String content = assignment == null ? null : assignment.getContent();
		contentEl = uifactory.addRichTextElementForStringDataCompact("content", "assignment.content", content, 12, 60, null, formLayout,
				ureq.getUserSession(), getWindowControl());
		contentEl.setElementCssClass("o_sel_pf_edit_assignment_content");
		contentEl.getEditorConfiguration().disableMedia();
		contentEl.getEditorConfiguration().disableImageAndMovie();
		
		if(binder != null) {
			List<Section> sections = portfolioService.getSections(binder);

			String selectedKey = null;
			int numOfSections = sections.size();
			String[] theKeys = new String[numOfSections];
			String[] theValues = new String[numOfSections];
			for (int i = 0; i < numOfSections; i++) {
				Long sectionKey = sections.get(i).getKey();
				theKeys[i] = sectionKey.toString();
				theValues[i] = (i + 1) + ". " + sections.get(i).getTitle();
				if (section != null && section.getKey().equals(sectionKey)) {
					selectedKey = theKeys[i];
				}
			}
			
			sectionsEl = uifactory.addDropdownSingleselect("sections", "page.sections", formLayout, theKeys, theValues, null);
			if (selectedKey != null) {
				sectionsEl.select(selectedKey, true);
			} else if(theKeys.length > 0) {
				sectionsEl.select(theKeys[0], true);
			}
		}
		
		String[] typeValues = new String[ typeKeys.length ];
		for(int i=typeValues.length; i-->0; ) {
			typeValues[i] = translate("assignment.type.".concat(typeKeys[i]));
		}
		typeEl = uifactory.addDropdownSingleselect("type", "assignment.type", formLayout, typeKeys, typeValues, null);
		typeEl.setElementCssClass("o_sel_pf_edit_assignment_type");
		typeEl.addActionListener(FormEvent.ONCHANGE);
		String selectedType = assignment == null ? typeKeys[0] : assignment.getAssignmentType().name();
		typeEl.select(selectedType, true);
		typeEl.setEnabled(!assignmentInUse);
		
		createAssignmentEvaluationFormForm(formLayout);
		createAssignmentDocumentForm(formLayout);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);

		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);

		updateAssignmentDocumentForm(ureq);
		updateAssignmentForms();
	}
	
	protected void createAssignmentEvaluationFormForm(FormItemContainer formLayout) {
		String editPage = Util.getPackageVelocityRoot(getClass()) + "/assignment_select_form.html";
		selectFormLayout = FormLayoutContainer.createCustomFormLayout("selectFormLayout", getTranslator(), editPage);
		selectFormLayout.setLabel("assignment.evaluation.form.entry", null);
		selectFormLayout.setMandatory(true);
		formLayout.add(selectFormLayout);
		if(formEntry != null) {
			String displayName = StringHelper.escapeHtml(formEntry.getDisplayname());
			selectFormLayout.contextPut("formEntryName", displayName);
		}
		selectFormButton = uifactory.addFormLink("select.form", selectFormLayout, Link.BUTTON);
		selectFormButton.getComponent().setSuppressDirtyFormWarning(true);
		selectFormButton.setVisible(!assignmentInUse);
		
		String[] evaValues = new String[]{
				translate("assignment.evaluation.form.auto"),
				translate("assignment.evaluation.form.auto.extern")
			}; 
		evaluationFormEl = uifactory.addRadiosVertical("assignment.evaluation.form", formLayout, evaKeys, evaValues);
		evaluationFormEl.addActionListener(FormEvent.ONCHANGE);
		if(assignment == null || assignment.isOnlyAutoEvaluation()) {
			evaluationFormEl.select(evaKeys[0], true);
		} else {
			evaluationFormEl.select(evaKeys[1], true);
		}

		String[] reviewerValues = new String[] { translate("assignment.evaluation.form.reviewer.see.auto") };
		reviewerSeeAutoEvaEl = uifactory.addCheckboxesHorizontal("assignment.evaluation.form.reviewer.see.auto", null, formLayout, onKeys, reviewerValues);
		if(assignment != null && assignment.isReviewerSeeAutoEvaluation()) {
			reviewerSeeAutoEvaEl.select(onKeys[0], true);
		}
		
		String[] anonValues = new String[] { translate("assignment.evaluation.form.anonymous.extern") };
		anonymousExternEvaEl = uifactory.addCheckboxesHorizontal("assignment.evaluation.form.anonymous.extern", null, formLayout, onKeys, anonValues);
		if(assignment != null && assignment.isAnonymousExternalEvaluation()) {
			anonymousExternEvaEl.select(onKeys[0], true);
		}
	}
	
	protected void createAssignmentDocumentForm(FormItemContainer formLayout) {
		String editPage = Util.getPackageVelocityRoot(getClass()) + "/assignment_editfiles.html";
		filesLayout = FormLayoutContainer.createCustomFormLayout("filesLayout", getTranslator(), editPage);
		filesLayout.setLabel("assignment.document.upload", null);
		formLayout.add(filesLayout);

		documentUploadEl = uifactory.addFileElement(getWindowControl(), "assignment.document.upload", formLayout);
		documentUploadEl.addActionListener(FormEvent.ONCHANGE);
	}
	
	protected void updateAssignmentForms() {
		if(AssignmentType.essay.name().equals(typeEl.getSelectedKey())) {
			selectFormLayout.setVisible(false);
			evaluationFormEl.setVisible(false);
			reviewerSeeAutoEvaEl.setVisible(false);
			anonymousExternEvaEl.setVisible(false);
		} else if(AssignmentType.form.name().equals(typeEl.getSelectedKey())) {
			selectFormLayout.setVisible(true);
			evaluationFormEl.setVisible(true);
			boolean otherOptions = evaluationFormEl.isOneSelected() && evaluationFormEl.isSelected(1);
			reviewerSeeAutoEvaEl.setVisible(otherOptions);
			anonymousExternEvaEl.setVisible(otherOptions);
		}
	}
	
	protected void updateAssignmentDocumentForm(UserRequest ureq) {
		if(mapperUri == null) {
			mapperUri = registerCacheableMapper(ureq, "assigment-" + CodeHelper.getRAMUniqueID(), new DocumentMapper());
			filesLayout.contextPut("mapperUri", mapperUri);
		}
		
		List<VFSItem> files = new ArrayList<>();
		if(assignment != null && StringHelper.containsNonWhitespace(assignment.getStorage())) {
			documentContainer = fileStorage.getAssignmentContainer(assignment);
			files.addAll(documentContainer.getItems(new SystemItemFilter()));
		}

		// add files from TempFolder
		if(tempUploadFolder != null) {
			files.addAll(tempUploadFolder.getItems(new SystemItemFilter()));
		}
		
		Collections.sort(files, new Comparator<VFSItem>(){
			final Collator c = Collator.getInstance(getLocale());
			@Override
			public int compare(final VFSItem o1, final VFSItem o2) {
				return c.compare((o1).getName(), (o2).getName());
			}
		});		

		filesLayout.contextPut("files", files);

		// add delete links for each attachment if user is allowed to see them
		int count = 0;
		for (VFSItem file : files) {
			FormLink deleteLink = uifactory.addFormLink("delete_" + (++count), filesLayout, Link.BUTTON_XSMALL);
			deleteLink.setUserObject(file);
			deleteLink.setI18nKey("delete");
		}

		boolean hasFile = !files.isEmpty();
		filesLayout.setVisible(hasFile);
		filesLayout.showLabel(hasFile);
		documentUploadEl.showLabel(!hasFile);
	}

	@Override
	protected void doDispose() {
		deleteTempStorage();
	}

	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == searchFormCtrl) {
			if(event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				doSelectForm(searchFormCtrl.getSelectedEntry());
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(searchFormCtrl);
		removeAsListenerAndDispose(cmc);
		searchFormCtrl = null;
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		titleEl.clearError();
		if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		if(sectionsEl != null) {
			sectionsEl.clearError();
			if(!sectionsEl.isOneSelected()) {
				sectionsEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		typeEl.clearError();
		selectFormLayout.clearError();
		if(typeEl.isOneSelected()) {
			AssignmentType type = AssignmentType.valueOf(typeEl.getSelectedKey());
			if(type == AssignmentType.form) {
				if(formEntry == null) {
					selectFormLayout.setErrorKey("form.legende.mandatory", null);
					allOk &= false;
				}	
			}
		} else {
			typeEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String title = titleEl.getValue();
		String summary = summaryEl.getValue();
		String content = contentEl.getValue();
		AssignmentType type = AssignmentType.valueOf(typeEl.getSelectedKey());
		
		boolean onlyAutoEvaluation = evaluationFormEl.isOneSelected() && evaluationFormEl.isSelected(0);
		boolean reviewerCanSeeAutoEvaluation = reviewerSeeAutoEvaEl.isAtLeastSelected(1);
		boolean anonymousExternEvaluation = anonymousExternEvaEl.isAtLeastSelected(1);
		
		Section selectedSection = section;
		if(sectionsEl != null && sectionsEl.isOneSelected()) {
			String selectedKey = sectionsEl.getSelectedKey();
			Long selectedSectionKey = Long.valueOf(selectedKey);
			selectedSection = portfolioService.getSection(new SectionKeyRef(selectedSectionKey));
		}

		if(assignment == null) {
			assignment = portfolioService.addAssignment(title, summary, content, type, false, selectedSection, null,
					onlyAutoEvaluation, reviewerCanSeeAutoEvaluation, anonymousExternEvaluation, formEntry);
		} else {
			assignment = portfolioService.updateAssignment(assignment, title, summary, content, type,
					onlyAutoEvaluation, reviewerCanSeeAutoEvaluation, anonymousExternEvaluation, formEntry);
		}
		persistUploadedFiles();
		deleteTempStorage();
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void persistUploadedFiles() {
		if(tempUploadFolder == null) return;
		
		VFSContainer container = portfolioFileStorage.getAssignmentContainer(assignment);
		if (container != null) {
			List<VFSItem> tmpFList = tempUploadFolder.getItems(new SystemItemFilter());
			for (VFSItem file : tmpFList) {
				try {
					VFSLeaf leaf = (VFSLeaf) file;
					VFSLeaf storedFile = container.createChildLeaf(leaf.getName());
					FileUtils.bcopy(leaf.getInputStream(), storedFile.getOutputStream(false), "");
				} catch (Exception e) {
					logError("", e);
				}
			}
		}
	}
	
	private void deleteTempStorage() {
		if(tempUploadFolder != null) {
			tempUploadFolder.delete();
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == typeEl || source == evaluationFormEl) {
			updateAssignmentForms();
		} else if (source == documentUploadEl) {
			if (documentUploadEl.isUploadSuccess()) {
				doUploadDocument(ureq);
			}
		} else if(source == selectFormButton) {
			doSelectForm(ureq);
		} else if (source instanceof FormLink) {
			FormLink activeLink = (FormLink) source;
			Object uobject = activeLink.getUserObject();
			if (uobject instanceof VFSLeaf) {
				VFSLeaf file = (VFSLeaf)uobject;
				file.delete();
			}
			updateAssignmentDocumentForm(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doSelectForm(UserRequest ureq) {
		if(searchFormCtrl != null) return;

		searchFormCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq, 
					EvaluationFormResource.TYPE_NAME, translate("select.form"));
		listenTo(searchFormCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), searchFormCtrl.getInitialComponent(),
				true, translate("select.form"));
		cmc.suppressDirtyFormWarningOnClose();
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSelectForm(RepositoryEntry entry) {
		formEntry = entry;
		String displayName = StringHelper.escapeHtml(entry.getDisplayname());
		selectFormLayout.contextPut("formEntryName", displayName);
	}
	
	private void doUploadDocument(UserRequest ureq) {
		String fileName = documentUploadEl.getUploadFileName();
		// checking tmp-folder and msg-container for filename
		boolean fileExists = false;
		if (tempUploadFolder != null && tempUploadFolder.resolve(fileName) != null) {
			fileExists = true;
		} else if (documentContainer != null && documentContainer.resolve(fileName) != null) {
			fileExists = true;
		}

		if (fileExists) {
			documentUploadEl.setErrorKey("attachments.error.file.exists", null);
			documentUploadEl.getUploadFile().delete();
			documentUploadEl.showError(true);
		} else {
			// files got stored in an extra tempFolder, to use the same
			// fileUploader multiple times
			if(tempUploadFolder == null) {
				tempUploadFolder = new OlatRootFolderImpl(File.separator + "tmp/" + CodeHelper.getGlobalForeverUniqueID() + "/", null);
			}
			documentUploadEl.moveUploadFileTo(tempUploadFolder);
			documentUploadEl.showError(false);
			documentUploadEl.reset();
			updateAssignmentDocumentForm(ureq);
			showInfo("attachments.upload.successful", fileName);
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	public class DocumentMapper implements Mapper {
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			if(relPath.startsWith("/")) {
				relPath = relPath.substring(1, relPath.length());
			}
			
			@SuppressWarnings("unchecked")
			List<VFSItem> files = (List<VFSItem>)filesLayout.contextGet("files");
			if(files != null) {
				for(VFSItem file:files) {
					if(relPath.equalsIgnoreCase(file.getName()) && file instanceof VFSLeaf) {
						return new VFSMediaResource((VFSLeaf)file);
					}
				}
			}
			return null;
		}
	}
}
