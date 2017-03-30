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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FileElementEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageImageAlign;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionRef;
import org.olat.modules.portfolio.model.SectionKeyRef;
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
	
	private TextElement titleEl;
	private RichTextElement summaryEl;
	private SingleSelection bindersEl, sectionsEl;
	private TextBoxListElement categoriesEl;
	
	private FileElement imageUpload;
	private SingleSelection imageAlignEl;
	private static final int picUploadlimitKB = 5120;
	
	private Page page;
	private Binder currentBinder;
	private Section currentSection;
	
	private final boolean chooseBinder;
	private final boolean chooseSection;
	private final boolean editTitleAndSummary;

	private Map<String,String> categories = new HashMap<>();
	private Map<String,Category> categoriesMap = new HashMap<>();
	
	@Autowired
	private PortfolioService portfolioService;
	
	public PageMetadataEditController(UserRequest ureq, WindowControl wControl,
			Binder currentBinder, boolean chooseBinder,
			Section currentSection, boolean chooseSection) {
		super(ureq, wControl);
		
		this.currentBinder = currentBinder;
		this.currentSection = currentSection;
		
		this.chooseBinder = chooseBinder;
		this.chooseSection = chooseSection;
		editTitleAndSummary = true;
		initForm(ureq);
	}
	
	public PageMetadataEditController(UserRequest ureq, WindowControl wControl,
			Binder currentBinder, boolean chooseBinder,
			Section currentSection, boolean chooseSection,
			Page page, boolean editTitleAndSummary) {
		super(ureq, wControl);

		this.page = page;
		this.editTitleAndSummary = editTitleAndSummary;
		
		this.currentBinder = currentBinder;
		this.currentSection = currentSection;
		
		this.chooseBinder = chooseBinder;
		this.chooseSection = chooseSection;
		
		if(page != null) {
			List<Category> tags = portfolioService.getCategories(page);
			for(Category tag:tags) {
				categories.put(tag.getName(), tag.getName());
				categoriesMap.put(tag.getName(), tag);
			}
		}
		
		initForm(ureq);
	}
	
	public Page getPage() {
		return page;
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_pf_edit_entry_form");
		
		String title = page == null ? null : page.getTitle();
		titleEl = uifactory.addTextElement("title", "page.title", 255, title, formLayout);
		titleEl.setElementCssClass("o_sel_pf_edit_entry_title");
		titleEl.setEnabled(editTitleAndSummary);
		titleEl.setMandatory(true);
		
		String summary = page == null ? null : page.getSummary();
		summaryEl = uifactory.addRichTextElementForStringDataMinimalistic("summary", "page.summary", summary, 8, 60, formLayout, getWindowControl());
		summaryEl.setPlaceholderKey("summary.placeholder", null);
		summaryEl.setEnabled(editTitleAndSummary);
		summaryEl.getEditorConfiguration().setPathInStatusBar(false);

		imageUpload = uifactory.addFileElement(getWindowControl(), "file", "fileupload",formLayout);			
		imageUpload.setPreview(ureq.getUserSession(), true);
		imageUpload.addActionListener(FormEvent.ONCHANGE);
		imageUpload.setDeleteEnabled(true);
		imageUpload.limitToMimeType(imageMimeTypes, null, null);
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
		
		bindersEl = uifactory.addDropdownSingleselect("binders", "page.binders", formLayout, new String[] { "" }, new String[] { "" }, null);
		
		sectionsEl = uifactory.addDropdownSingleselect("sections", "page.sections", formLayout, new String[] { "" }, new String[] { "" }, null);
		sectionsEl.setElementCssClass("o_sel_pf_edit_entry_section");
		sectionsEl.setVisible(false);
		
		initBinderSelection();
		updateSections();
		
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

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
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
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (page == null) {
			String title = titleEl.getValue();
			String summary = summaryEl.getValue();
			SectionRef selectSection = getSelectedSection();
			String imagePath = null;
			if (imageUpload.getUploadFile() != null) {
				imagePath = portfolioService.addPosterImageForPage(imageUpload.getUploadFile(),
						imageUpload.getUploadFileName());
			}
			PageImageAlign align = null;
			if(imageAlignEl.isOneSelected()) {
				align = PageImageAlign.valueOf(imageAlignEl.getSelectedKey());
			}
			page = portfolioService.appendNewPage(getIdentity(), title, summary, imagePath, align, selectSection);
			
		} else {
			page.setTitle(titleEl.getValue());
			page.setSummary(summaryEl.getValue());

			if (imageUpload.getUploadFile() != null) {
				String imagePath = portfolioService.addPosterImageForPage(imageUpload.getUploadFile(),
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
			if(imageAlignEl.isOneSelected()) {
				page.setImageAlignment(PageImageAlign.valueOf(imageAlignEl.getSelectedKey()));
			}
			page = portfolioService.updatePage(page, newParent);
		}
		
		List<String> updatedCategories = categoriesEl.getValueList();
		portfolioService.updateCategories(page, updatedCategories);

		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private SectionRef getSelectedSection() {
		SectionRef selectSection = null;
		if (sectionsEl != null && sectionsEl.isOneSelected() && sectionsEl.isEnabled() && sectionsEl.isVisible()) {
			String selectedKey = sectionsEl.getSelectedKey();
			selectSection = new SectionKeyRef(new Long(selectedKey));
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
					currentBinder = portfolioService.getBinderByKey(new Long(selectedKey));
					sectionsEl.setVisible(true);
					updateSections();
				} catch(NumberFormatException e) {
					logError("", e);
				}
			}
		}
	}
}