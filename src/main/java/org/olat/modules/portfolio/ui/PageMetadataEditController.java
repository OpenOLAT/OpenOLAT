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
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FileElementEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkPopupSettings;
import org.olat.core.gui.components.textboxlist.TextBoxItem;
import org.olat.core.gui.components.textboxlist.TextBoxItemImpl;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.translator.TranslatorHelper;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.io.SystemFilenameFilter;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.AssignmentType;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaHandler;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageImageAlign;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionRef;
import org.olat.modules.portfolio.manager.PortfolioFileStorage;
import org.olat.modules.portfolio.model.MediaPart;
import org.olat.modules.portfolio.model.SectionKeyRef;
import org.olat.modules.portfolio.ui.media.UploadMedia;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.CompetenceBrowserController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 08.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageMetadataEditController extends FormBasicController {
	
	private static final Set<String> imageMimeTypes = new HashSet<>();
	static {
		imageMimeTypes.add("image/gif");
		imageMimeTypes.add("image/jpg");
		imageMimeTypes.add("image/jpeg");
		imageMimeTypes.add("image/png");
	}
	
	private static final String[] alignKeys = new String[]{ PageImageAlign.background.name(), PageImageAlign.right.name(), PageImageAlign.right_large.name(), PageImageAlign.left.name(), PageImageAlign.left_large.name() };

	private static final String[] onKeys = new String[] { "on" };
	private static final String[] evaKeys = new String[] { "only-autoevaluation", "alien-evaluation"};
	
	private static final String[] editKeys = new String[] {"metadata.edit.local", "metadata.edit.global"};
	
	private SingleSelection editModeEl;
	private TextElement titleEl;
	private RichTextElement summaryEl;
	private SingleSelection bindersEl;
	private SingleSelection sectionsEl;
	private SingleSelection evaluationFormEl;
	private SingleSelection assignmentsTemplatesEl;
	private TextBoxListElement categoriesEl;
	private TextBoxListElement competencesEl;
	private DownloadLink downloadAssignmentDocEl;
	private FileElement assignmentDocUploadEl;
	private FormLayoutContainer assignmentDocsContainer;
	private MultipleSelectionElement reviewerSeeAutoEvaEl;
	private MultipleSelectionElement updateGlobalEntriesEl;
	private FormLink openCompetenceBrowserLink;
	
	private FileElement imageUpload;
	private SingleSelection imageAlignEl;
	private static final int picUploadlimitKB = 5120;
	
	private Page page;
	private Binder currentBinder;
	private Section currentSection;
	private final BinderSecurityCallback secCallback;
	private Page pageDelegate;

	private int counter;
	private File tempFolder;
	private final boolean chooseBinder;
	private final boolean chooseSection;
	private final boolean editTitleAndSummary;

	private List<FileInfos> documents = new ArrayList<>();
	private List<TextBoxItem> categories = new ArrayList<>();
	private Map<String,Category> categoriesMap = new HashMap<>();
	private Map<String,Assignment> assignmentTemplatesMap = new HashMap<>();
	List<TextBoxItem> existingCompetences;
	List<TextBoxItem> availableTaxonomyLevels;
	
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private PortfolioFileStorage portfolioFileStorage;
	@Autowired
	private PortfolioV2Module portfolioV2Module;
	@Autowired 
	private TaxonomyService taxonomyService;
	
	public PageMetadataEditController(UserRequest ureq, WindowControl wControl, BinderSecurityCallback secCallback,
			Binder currentBinder, boolean chooseBinder, Section currentSection, boolean chooseSection, Page pageDelegate) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		this.currentBinder = currentBinder;
		this.currentSection = currentSection;
		this.pageDelegate = pageDelegate;
		
		this.chooseBinder = chooseBinder;
		this.chooseSection = chooseSection;
		editTitleAndSummary = true;
		
		initTaxonomyCompetences();
		initForm(ureq);
		
		if(assignmentsTemplatesEl != null && assignmentsTemplatesEl.getKeys().length > 0) {
			Assignment assignmentTemplate = null;
			if(assignmentsTemplatesEl.isOneSelected()) {
				assignmentTemplate = assignmentTemplatesMap.get(assignmentsTemplatesEl.getSelectedKey());
			} else if(StringHelper.containsNonWhitespace(assignmentsTemplatesEl.getKeys()[0])) {
				assignmentsTemplatesEl.select(assignmentsTemplatesEl.getKeys()[0], true);
				assignmentTemplate = assignmentTemplatesMap.get(assignmentsTemplatesEl.getSelectedKey());
			} else if(assignmentsTemplatesEl.getKeys().length > 1) {
				assignmentsTemplatesEl.select(assignmentsTemplatesEl.getKeys()[1], true);
				assignmentTemplate = assignmentTemplatesMap.get(assignmentsTemplatesEl.getSelectedKey());
			}
			updateForAssignmentTemplate(assignmentTemplate);
		}
		
		if(pageDelegate != null) {
			initDelegate();
		}
	}
	
	public PageMetadataEditController(UserRequest ureq, WindowControl wControl, BinderSecurityCallback secCallback,
			Binder currentBinder, boolean chooseBinder, Assignment assignmentTemplate, boolean chooseSection) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		this.currentBinder = currentBinder;
		currentSection = null;
		
		this.chooseBinder = chooseBinder;
		this.chooseSection = chooseSection;
		editTitleAndSummary = true;
		
		initTaxonomyCompetences();
		initForm(ureq);
		
		assignmentsTemplatesEl.select(assignmentTemplate.getKey().toString(), true);
		updateForAssignmentTemplate(assignmentTemplate);
		assignmentsTemplatesEl.setEnabled(false);
	}
	
	public PageMetadataEditController(UserRequest ureq, WindowControl wControl, BinderSecurityCallback secCallback,
			Binder currentBinder, boolean chooseBinder, Section currentSection, boolean chooseSection,
			Page page, boolean editTitleAndSummary) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		this.page = page;
		this.editTitleAndSummary = editTitleAndSummary;
		
		this.currentBinder = currentBinder;
		this.currentSection = currentSection;
		
		this.chooseBinder = chooseBinder;
		this.chooseSection = chooseSection;
		
		if(page != null) {
			List<Category> tags = portfolioService.getCategories(page);
			for(Category tag:tags) {
				categories.add(new TextBoxItemImpl(tag.getName(), tag.getName()));
				categoriesMap.put(tag.getName(), tag);
			}
		}
		
		initTaxonomyCompetences();
		initForm(ureq);
	}
	
	public Page getPage() {
		return page;
	}

	@Override
	protected void doDispose() {
		if(tempFolder != null) {
			FileUtils.deleteDirsAndFiles(tempFolder, true, true);
		}
	}
	
	private void initDelegate() {
		titleEl.setValue(pageDelegate.getTitle());
		titleEl.setFocus(false);
		summaryEl.setValue(pageDelegate.getSummary());
		
		PageImageAlign alignment = pageDelegate.getImageAlignment();
		if(alignment != null) {
			for(int i=alignKeys.length; i-->0; ) {
				if(alignKeys[i].equals(alignment.name())) {
					imageAlignEl.select(alignKeys[i], true);
				}
			}
		}
		
		imageUpload.setMaxUploadSizeKB(picUploadlimitKB, null, null);
		File posterImg = portfolioService.getPosterImage(pageDelegate);
		if(posterImg != null) {
			imageUpload.setInitialFile(posterImg);
		}
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_pf_edit_entry_form");
		
		if(pageDelegate != null) {
			setFormInfo("page.sharing.infos");
		}
		
		if (page != null && page.getBody() != null && page.getBody().getUsage() > 1) {
			editModeEl = uifactory.addDropdownSingleselect("edit.mode", formLayout, editKeys, TranslatorHelper.translateAll(getTranslator(), editKeys));
			editModeEl.addActionListener(FormEvent.ONCHANGE);
			
			List<Page> sharedPages = portfolioService.getPagesSharingSameBody(page);
			String[] keys = new String[sharedPages.size()];
			String[] values = new String[sharedPages.size()];
			
			for(int i = 0; i < sharedPages.size(); i++) {
				Page page = sharedPages.get(i);
				keys[i] = page.getKey().toString();
				
				String title = page.getTitle();
				
				if (page.getSection() != null && page.getSection().getBinder() != null) {
					title = page.getSection().getBinder().getTitle() + " / " + page.getSection().getTitle() + " / " + page.getTitle();
				}
				
				
				values[i] = title;
			}
			
			updateGlobalEntriesEl = uifactory.addCheckboxesVertical("update.global.entries", formLayout, keys, values, 1);
			updateGlobalEntriesEl.setMandatory(true);
			updateGlobalEntriesEl.setVisible(false);
			
			setFormWarning("edit.mode.info");
		}
		
		String title = page == null ? null : page.getTitle();
		titleEl = uifactory.addTextElement("title", "page.title", 255, title, formLayout);
		titleEl.setElementCssClass("o_sel_pf_edit_entry_title");
		titleEl.setEnabled(editTitleAndSummary);
		titleEl.setMandatory(true);
		if(!StringHelper.containsNonWhitespace(title)) {
			titleEl.setFocus(true);
		}
		
		String summary = page == null ? null : page.getSummary();
		summaryEl = uifactory.addRichTextElementForStringDataMinimalistic("summary", "page.summary", summary, 8, 60, formLayout, getWindowControl());
		summaryEl.setPlaceholderKey("summary.placeholder", null);
		summaryEl.setEnabled(editTitleAndSummary);
		summaryEl.getEditorConfiguration().setPathInStatusBar(false);

		imageUpload = uifactory.addFileElement(getWindowControl(), getIdentity(), "file","fileupload", formLayout);			
		imageUpload.setPreview(ureq.getUserSession(), true);
		imageUpload.addActionListener(FormEvent.ONCHANGE);
		imageUpload.setDeleteEnabled(true);
		imageUpload.limitToMimeType(imageMimeTypes, "error.mimetype", new String[]{ imageMimeTypes.toString()} );
		imageUpload.setMaxUploadSizeKB(picUploadlimitKB, null, null);
		if(page != null) {
			File posterImg = portfolioService.getPosterImage(page);
			if(posterImg != null) {
				imageUpload.setInitialFile(posterImg);
			}
		}
		
		String[] alignValues = new String[]{ translate("image.align.background"), translate("image.align.right"), translate("image.align.right.large"), translate("image.align.left"), translate("image.align.left.large") };
		imageAlignEl = uifactory.addDropdownSingleselect("image.align", null, formLayout, alignKeys, alignValues, null);
		PageImageAlign alignment = page == null ? null : page.getImageAlignment();
		if(alignment == null) {
			imageAlignEl.select(alignKeys[0], true);
		} else {
			for(int i=alignKeys.length; i-->0; ) {
				if(alignKeys[i].equals(alignment.name())) {
					imageAlignEl.select(alignKeys[i], true);
				}
			}
		}
		
		categoriesEl = uifactory.addTextBoxListElement("categories", "categories", "categories.hint", categories, formLayout, getTranslator());
		categoriesEl.setHelpText(translate("categories.hint"));
		categoriesEl.setElementCssClass("o_sel_ep_tagsinput");
		categoriesEl.setAllowDuplicates(false);
		
		if (portfolioV2Module.isTaxonomyLinkingReady()) {
			competencesEl = uifactory.addTextBoxListElement("competences", "competences", "competences.hint", existingCompetences, formLayout, getTranslator());
			competencesEl.setHelpText(translate("competences.hint"));
			competencesEl.setElementCssClass("o_sel_ep_tagsinput");
			competencesEl.setAllowDuplicates(false);
			competencesEl.setAllowNewValues(false);
			competencesEl.setAutoCompleteContent(availableTaxonomyLevels);
			competencesEl.setCustomCSSForItems("o_competence");
		}
		
		openCompetenceBrowserLink = uifactory.addFormLink("open.browser.link", formLayout, Link.BUTTON_XSMALL);
		openCompetenceBrowserLink.setLabel("no.label", null);
		openCompetenceBrowserLink.setPopup(new LinkPopupSettings(800, 600, "Open"));
		
		bindersEl = uifactory.addDropdownSingleselect("binders", "page.binders", formLayout, new String[] { "" }, new String[] { "" }, null);
		
		sectionsEl = uifactory.addDropdownSingleselect("sections", "page.sections", formLayout, new String[] { "" }, new String[] { "" }, null);
		sectionsEl.setElementCssClass("o_sel_pf_edit_entry_section");
		sectionsEl.setVisible(false);
		
		assignmentsTemplatesEl = uifactory.addDropdownSingleselect("assignments", "assignments.templates", formLayout, new String[] { "" }, new String[] { "" }, null);
		assignmentsTemplatesEl.setElementCssClass("o_sel_pf_edit_entry_assignment_template");
		assignmentsTemplatesEl.addActionListener(FormEvent.ONCHANGE);
		assignmentsTemplatesEl.setVisible(false);
		
		String[] evaValues = new String[]{
				translate("assignment.evaluation.form.auto"),
				translate("assignment.evaluation.form.auto.extern")
			}; 
		evaluationFormEl = uifactory.addRadiosVertical("assignment.evaluation.form", formLayout, evaKeys, evaValues);
		evaluationFormEl.addActionListener(FormEvent.ONCHANGE);
		evaluationFormEl.setVisible(false);
		
		String[] reviewerValues = new String[] { translate("assignment.evaluation.form.reviewer.see.auto") };
		reviewerSeeAutoEvaEl = uifactory.addCheckboxesHorizontal("assignment.evaluation.form.reviewer.see.auto", null, formLayout, onKeys, reviewerValues);
		reviewerSeeAutoEvaEl.setVisible(false);
		
		downloadAssignmentDocEl = uifactory.addDownloadLink("assignments.doc", "", "assignment.template.doc", null, formLayout);
		downloadAssignmentDocEl.setVisible(false);
		
		assignmentDocUploadEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "assignment.template.documents", formLayout);
		assignmentDocUploadEl.addActionListener(FormEvent.ONCHANGE);
		assignmentDocUploadEl.setVisible(false);

		String documentsPage = velocity_root + "/assignment_documents.html";
		assignmentDocsContainer = FormLayoutContainer.createCustomFormLayout("documentsLayout", getTranslator(), documentsPage);
		formLayout.add(assignmentDocsContainer);
		assignmentDocsContainer.setRootForm(mainForm);
		assignmentDocsContainer.setLabel(null, null);
		assignmentDocsContainer.setVisible(false);
		assignmentDocsContainer.contextPut("documents", documents);
		
		initBinderSelection();
		updateSections();
		updateAssignmentTemplates();
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		if(page != null && page.getKey() != null) {
			uifactory.addFormSubmitButton("save", buttonsCont);
		} else {
			uifactory.addFormSubmitButton("create.page", buttonsCont);
		}
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	protected void initBinderSelection() {
		if (chooseBinder) {
			List<Binder> binders = portfolioService.getOwnedBinders(getIdentity());

			String[] theKeys = new String[binders.size()+1];
			String[] theValues = new String[binders.size()+1];
			theKeys[0] = "none";
			theValues[0] = translate("binder.none");
			for (int i = 0; i < binders.size(); ++i) {
				Binder binder = binders.get(i);
				theKeys[i+1] = binder.getKey().toString();
				theValues[i+1] = binder.getTitle();
			} 
			
			bindersEl.setKeysAndValues(theKeys, theValues, null);
			bindersEl.addActionListener(FormEvent.ONCHANGE);
			bindersEl.reset();
			

			String selectedBinder = theKeys[0];
			if (currentBinder != null) {
				selectedBinder = currentBinder.getKey().toString();					
			}
			
			for (String key : theKeys) {
				if (key.equals(selectedBinder)) {
					bindersEl.select(key, true);
				}
			}
		} else {
			String[] theKeys = new String[] { currentBinder.getKey().toString() };
			String[] theValues = new String[] { currentBinder.getTitle() };
			bindersEl.setKeysAndValues(theKeys, theValues, null);
			bindersEl.setEnabled(false);
			bindersEl.reset();
			bindersEl.select(theKeys[0], true);
		}
	}
	
	protected void updateSections() {
		if(chooseSection) {
			String selectedBinderKey =  bindersEl.isOneSelected() ? bindersEl.getSelectedKey() : null;
			if(selectedBinderKey == null || "none".equals(selectedBinderKey)) {
				sectionsEl.setKeysAndValues(new String[] { "" }, new String[] { "" }, null);
				sectionsEl.reset();
				sectionsEl.setVisible(false);
				
			} else {
				List<Section> sections = portfolioService.getSections(currentBinder);
				if(sections.isEmpty()) {
					sectionsEl.setKeysAndValues(new String[] { "" }, new String[] { "" }, null);
					sectionsEl.reset();
					sectionsEl.setVisible(false);
				} else {
					String selectedKey = null;
					int numOfSections = sections.size();
					String[] theKeys = new String[numOfSections];
					String[] theValues = new String[numOfSections];
					for (int i = 0; i < numOfSections; i++) {
						Long sectionKey = sections.get(i).getKey();
						theKeys[i] = sectionKey.toString();
						theValues[i] = (i + 1) + ". " + sections.get(i).getTitle();
						if (currentSection != null && currentSection.getKey().equals(sectionKey)) {
							selectedKey = theKeys[i];
						}
					}
					
					sectionsEl.setKeysAndValues(theKeys, theValues, null);
					sectionsEl.reset();
					sectionsEl.setEnabled(true);
					sectionsEl.setVisible(true);
					
					if (selectedKey != null) {
						sectionsEl.select(selectedKey, true);
					}
				}
			}
		} else {// currently never used
			String[] theKeys = new String[] { currentSection.getKey().toString() };
			String[] theValues = new String[]{ StringHelper.escapeHtml(currentSection.getTitle()) };
			sectionsEl.setKeysAndValues(theKeys, theValues, null);
			sectionsEl.select(theKeys[0], true);
			sectionsEl.setEnabled(false);
			sectionsEl.setVisible(true);
		}
	}
	
	private void updateAssignmentTemplates() {
		if(currentBinder == null || (page != null && page.getKey() != null) || pageDelegate != null) {
			return;
		}
		
		List<Assignment> assignments = portfolioService.getBindersAssignmentsTemplates(currentBinder);
		if(assignments.isEmpty()) return;
		
		List<String> assignmentKeys = new ArrayList<>(assignments.size() + 2);
		List<String> assignmentNames = new ArrayList<>(assignments.size() + 2);

		if(secCallback.canNewPageWithoutAssignment()) {
			assignmentKeys.add("");
			assignmentNames.add(translate("assignments.templates.without"));
		}
		for(int i=assignments.size(); i-->0; ) {
			Assignment assignment = assignments.get(i);
			String assignmentKey = assignment.getKey().toString();
			assignmentKeys.add(assignmentKey);
			assignmentNames.add(assignment.getTitle());
			assignmentTemplatesMap.put(assignmentKey, assignment);
		}
		
		assignmentsTemplatesEl.setKeysAndValues(assignmentKeys.toArray(new String[assignmentKeys.size()]), assignmentNames.toArray(new String[assignmentNames.size()]), null);
		assignmentsTemplatesEl.setMandatory(true);
		assignmentsTemplatesEl.setVisible(true);
	}
	
	private void updateForAssignmentTemplate(Assignment assignment) {
		if(assignment != null && assignment.getAssignmentType() == AssignmentType.form) {
			evaluationFormEl.setVisible(true);
			if(assignment.isOnlyAutoEvaluation()) {
				evaluationFormEl.select(evaKeys[0], true);
			} else {
				evaluationFormEl.select(evaKeys[1], true);
			}
			
			boolean otherOptions = evaluationFormEl.isOneSelected() && evaluationFormEl.isSelected(1);
			reviewerSeeAutoEvaEl.setVisible(otherOptions);
			if(assignment.isReviewerSeeAutoEvaluation()) {
				reviewerSeeAutoEvaEl.select(evaKeys[0], true);
			} else {
				reviewerSeeAutoEvaEl.uncheckAll();
			}
		} else {
			evaluationFormEl.setVisible(false);
			reviewerSeeAutoEvaEl.setVisible(false);
		}
		
		if(assignment == null || assignment.getAssignmentType() != AssignmentType.document) {
			downloadAssignmentDocEl.setVisible(false);
			assignmentDocUploadEl.setVisible(false);
			assignmentDocsContainer.setVisible(false);
		} else {
			File document = portfolioFileStorage.getAssignmentFirstFile(assignment);
			downloadAssignmentDocEl.setVisible(document != null);
			assignmentDocUploadEl.setVisible(true);
			if(document != null) {
				downloadAssignmentDocEl.setDownloadItem(document);
				downloadAssignmentDocEl.setLinkText(document.getName());
			}
			updateDocumentsLayout();
		}
	}
	
	// adds or updates the list of already existing attachments with a delete
	// button for each
	private void updateDocumentsLayout() {
		documents.clear();
		List<File> tempDocuments = getTempFolderFiles();
		for(File document:tempDocuments) {
			String docId = "delete_" + (counter++);

			FormLink deleteLink = uifactory.addFormLink(docId, "delete", "delete", null, assignmentDocsContainer, Link.BUTTON_XSMALL);
			deleteLink.setDomReplacementWrapperRequired(false);
			deleteLink.setIconLeftCSS("o_icon o_icon_delete_item");
			deleteLink.setUserObject(document);
			documents.add(new FileInfos(document, deleteLink));
		}
		assignmentDocsContainer.setVisible(!documents.isEmpty());
		assignmentDocsContainer.setDirty(true);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		titleEl.clearError();
		if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		if(sectionsEl != null && sectionsEl.isEnabled() && sectionsEl.isVisible()) {
			sectionsEl.clearError();
			if(!sectionsEl.isOneSelected() || !StringHelper.containsNonWhitespace(sectionsEl.getSelectedKey())) {
				sectionsEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		if(assignmentsTemplatesEl != null) {
			assignmentsTemplatesEl.clearError();
			if(assignmentsTemplatesEl.isVisible() && !assignmentsTemplatesEl.isOneSelected()) {
				assignmentsTemplatesEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		if (updateGlobalEntriesEl != null) {
			updateGlobalEntriesEl.clearError();
			if(updateGlobalEntriesEl.isVisible()) {
				if(updateGlobalEntriesEl.getSelectedKeys().size() < 1) {
					allOk &= false;
					updateGlobalEntriesEl.setErrorKey("form.legende.mandatory", null);
				}
				
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String imagePath = null;
		PageImageAlign imageAlign = null;
		if(imageAlignEl.isOneSelected()) {
			imageAlign = PageImageAlign.valueOf(imageAlignEl.getSelectedKey());
		}
		
		if (page == null) {
			String title = titleEl.getValue();
			String summary = summaryEl.getValue();
			SectionRef selectSection = getSelectedSection();
			if (imageUpload.getUploadFile() != null) {
				imagePath = portfolioService.addPosterImageForPage(imageUpload.getUploadFile(),
						imageUpload.getUploadFileName());
			} else if(pageDelegate != null && imageUpload.getInitialFile() != null) {
				imagePath = pageDelegate.getImagePath();// reuse
			}
			
			
			Boolean onlyAutoEvaluation = null;
			Boolean reviewerCanSeeAutoEvaluation = null;
			if(evaluationFormEl != null && evaluationFormEl.isVisible()) {
				onlyAutoEvaluation = evaluationFormEl.isOneSelected() && evaluationFormEl.isSelected(0);
				if(reviewerSeeAutoEvaEl.isVisible()) {
					reviewerCanSeeAutoEvaluation = reviewerSeeAutoEvaEl.isAtLeastSelected(1);
				}
			}
			
			if(pageDelegate != null) {
				page = portfolioService.appendNewPage(getIdentity(), title, summary, imagePath, imageAlign, selectSection, pageDelegate);
			} else if(assignmentsTemplatesEl != null && assignmentsTemplatesEl.isVisible() && assignmentsTemplatesEl.isOneSelected()
					&& StringHelper.containsNonWhitespace(assignmentsTemplatesEl.getSelectedKey())) {
				Assignment assignmentTemplate = assignmentTemplatesMap.get(assignmentsTemplatesEl.getSelectedKey());
				page = portfolioService.startAssignmentFromTemplate(assignmentTemplate.getKey(), getIdentity(), title, summary, imagePath, imageAlign,
						selectSection, onlyAutoEvaluation, reviewerCanSeeAutoEvaluation);
				saveDocuments(page, assignmentTemplate);
			} else {
				page = portfolioService.appendNewPage(getIdentity(), title, summary, imagePath, imageAlign, selectSection);
			}
		} else {
			page.setTitle(titleEl.getValue());
			page.setSummary(summaryEl.getValue());

			if (imageUpload.getUploadFile() != null) {
				imagePath = portfolioService.addPosterImageForPage(imageUpload.getUploadFile(),
						imageUpload.getUploadFileName());
				page.setImagePath(imagePath);
			} else if (imageUpload.getInitialFile() == null) {
				page.setImagePath(null);
				portfolioService.removePosterImage(page);
			}

			SectionRef selectSection = getSelectedSection();
			SectionRef newParent = null;
			if((page.getSection() == null && selectSection != null) ||
					(page.getSection() != null && selectSection != null && !page.getSection().getKey().equals(selectSection.getKey()))) {
				newParent = selectSection;
			}
			
			page.setImageAlignment(imageAlign);
			page = portfolioService.updatePage(page, newParent);
		}
		
		List<String> updatedCategories = categoriesEl.getValueList();
		portfolioService.updateCategories(page, updatedCategories);
		
		if (portfolioV2Module.isTaxonomyLinkingReady()) {
			portfolioService.linkCompetences(page, getIdentity(), competencesEl.getValueItems());
		}
		
		if (editModeEl != null && editModeEl.getSelectedKey().equals(editKeys[1])) {
		List<Page> sharingTheBody = portfolioService.getPagesSharingSameBody(page);
			for(Page sharing:sharingTheBody) {
				if(!sharing.equals(page) && updateGlobalEntriesEl.getSelectedKeys().contains(sharing.getKey().toString())) {
					sharing.setTitle(titleEl.getValue());
					sharing.setSummary(summaryEl.getValue());
					sharing.setImageAlignment(imageAlign);
					sharing.setImagePath(imagePath);
					portfolioService.updatePage(sharing, null);
					portfolioService.updateCategories(sharing, updatedCategories);
					if (portfolioV2Module.isTaxonomyLinkingReady()) {
						portfolioService.linkCompetences(sharing, getIdentity(), competencesEl.getValueItems());
					}
				}
			}
		}

		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void saveDocuments(Page thePage, Assignment assignmentTemplate) {
		if(assignmentTemplate.getAssignmentType() != AssignmentType.document || documents.isEmpty()) return;

		String businessPath = getWindowControl().getBusinessControl().getAsString();
		List<MediaHandler> availableHandlers = portfolioService.getMediaHandlers();

		for(FileInfos document:documents) {
			MediaHandler handler = null;
			String mimeType = WebappHelper.getMimeType(document.getName());
			for(MediaHandler availableHandler:availableHandlers) {
				if(availableHandler.acceptMimeType(mimeType)) {
					handler = availableHandler;
					break;
				}
			}
			
			if(handler != null) {
				UploadMedia mObject = new UploadMedia(document.getFile(), document.getName(), mimeType);
				Media media = handler.createMedia(document.getName(), "", mObject, businessPath, getIdentity());
				MediaPart part = new MediaPart();
				part.setMedia(media);
				portfolioService.appendNewPagePart(thePage, part);
			}
		}
	}
	
	private SectionRef getSelectedSection() {
		SectionRef selectSection = null;
		if (sectionsEl != null && sectionsEl.isOneSelected() && sectionsEl.isEnabled() && sectionsEl.isVisible()) {
			String selectedKey = sectionsEl.getSelectedKey();
			selectSection = new SectionKeyRef(Long.valueOf(selectedKey));
		}
		return selectSection;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		if (imageUpload == source) {
			if (event instanceof FileElementEvent) {
				String cmd = event.getCommand();
				if (FileElementEvent.DELETE.equals(cmd)) {
					if(imageUpload.getUploadFile() != null) {
						imageUpload.reset();
					} else if(imageUpload.getInitialFile() != null) {
						imageUpload.setInitialFile(null);
					}
				}
			}
		} else if (bindersEl == source) {
			if (bindersEl.getSelectedKey().equals("none")) {
				sectionsEl.setVisible(false);
				currentBinder = null;
			} else {
				try {
					String selectedKey = bindersEl.getSelectedKey();
					currentBinder = portfolioService.getBinderByKey(Long.valueOf(selectedKey));
					sectionsEl.setVisible(true);
					updateSections();
				} catch(NumberFormatException e) {
					logError("", e);
				}
			}
		} else if(assignmentsTemplatesEl == source) {
			Assignment assignment = null;
			if(assignmentsTemplatesEl.isEnabled() && assignmentsTemplatesEl.isOneSelected()
					&& StringHelper.containsNonWhitespace(assignmentsTemplatesEl.getSelectedKey())) {
				String templateKey = assignmentsTemplatesEl.getSelectedKey();
				assignment = assignmentTemplatesMap.get(templateKey);
			} else {
				assignment = null;
			}
			updateForAssignmentTemplate(assignment);
		} else if(assignmentDocUploadEl == source) {
			if(assignmentDocUploadEl.isUploadSuccess()) {
				processUpload();
				updateDocumentsLayout();
			}
		} else if(evaluationFormEl == source) {
			boolean otherOptions = evaluationFormEl.isOneSelected() && evaluationFormEl.isSelected(1);
			reviewerSeeAutoEvaEl.setVisible(otherOptions);
		} else if (editModeEl == source) {
			updateGlobalEntriesEl.setVisible(editModeEl.getSelectedKey().equals(editKeys[1]));
			sectionsEl.setVisible(editModeEl.getSelectedKey().equals(editKeys[0]));
			bindersEl.setVisible(editModeEl.getSelectedKey().equals(editKeys[0]));
		} else if (openCompetenceBrowserLink == source) {
			ControllerCreator competenceBrowserCreator = (lureq, lwControl) -> {
				return new CompetenceBrowserController(lureq, lwControl);				
			};
			ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, competenceBrowserCreator);
			openInNewBrowserWindow(ureq, layoutCtrlr);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("delete".equals(link.getCmd()) && link.getUserObject() instanceof File) {
				doDeleteTempFile((File)link.getUserObject());
			}
		}
	}
	
	private void initTaxonomyCompetences() {
		if (portfolioV2Module.isTaxonomyLinkingReady() && page != null) {
			List<TaxonomyCompetence> competences = portfolioService.getRelatedCompetences(page, true);
			existingCompetences = new ArrayList<>();
			for (TaxonomyCompetence competence : competences) {
				TextBoxItemImpl competenceTextBoxItem = new TextBoxItemImpl(competence.getTaxonomyLevel().getDisplayName(), competence.getTaxonomyLevel().getKey().toString());
				competenceTextBoxItem.setTooltip(competence.getTaxonomyLevel().getMaterializedPathIdentifiersWithoutSlash());
				existingCompetences.add(competenceTextBoxItem);
			}
		}
		
		if (portfolioV2Module.isTaxonomyLinkingReady()) {
			availableTaxonomyLevels = new ArrayList<>();
			for (Taxonomy taxonomy : portfolioV2Module.getLinkedTaxonomies()) {
				for (TaxonomyLevel taxonomyLevel : taxonomyService.getTaxonomyLevels(taxonomy)) {
					if (taxonomyLevel.getType() != null) {
						if(taxonomyLevel.getType().isAllowedAsCompetence() == false) {
							// Do not list items, which are not marked as available for competences
							continue;
						}
					}
					TextBoxItem item = new TextBoxItemImpl(taxonomyLevel.getDisplayName(), taxonomyLevel.getKey().toString());
					item.setDropDownInfo(taxonomyLevel.getMaterializedPathIdentifiersWithoutSlash());
					item.setTooltip(taxonomyLevel.getMaterializedPathIdentifiersWithoutSlash());
					availableTaxonomyLevels.add(item);
				}
			}

		}
	}
	
	private void processUpload() {
		File directory = getTempFolder();
		assignmentDocUploadEl.moveUploadFileTo(directory);
		assignmentDocUploadEl.showError(false);
		assignmentDocUploadEl.reset();
	}
	
	private File getTempFolder() {
		if(tempFolder == null) {
			tempFolder = new File(WebappHelper.getTmpDir(), "upload-" + CodeHelper.getGlobalForeverUniqueID());
			tempFolder.mkdirs();
		}
		return tempFolder;
	}
	
	private List<File> getTempFolderFiles() {
		File directory = getTempFolder();
		List<File> temporaryFiles = new ArrayList<>();
		File[] files = directory.listFiles(new SystemFilenameFilter(true, false));
		for(File file:files) {
			temporaryFiles.add(file);
		}
		return temporaryFiles;
	}
	
	private void doDeleteTempFile(File file) {
		if(file != null && file.exists()) {
			try {
				Files.delete(file.toPath());
			} catch (IOException e) {
				logError("", e);
			}
		}
		updateDocumentsLayout();
	}
	
	public class FileInfos {
		
		private final File file;
		private final FormLink deleteLink;
		
		public FileInfos(File file, FormLink deleteLink) {
			this.file = file;
			this.deleteLink = deleteLink;
		}
		
		public String getName() {
			return file.getName();
		}
		
		public long getSize() {
			return file.length();
		}

		public File getFile() {
			return file;
		}

		public FormLink getDeleteLink() {
			return deleteLink;
		}
	}
}