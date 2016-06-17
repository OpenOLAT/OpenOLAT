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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
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
import org.olat.modules.portfolio.Page;
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
	
	private static final Set<String> imageMimeTypes = new HashSet<String>();
	static {
		imageMimeTypes.add("image/gif");
		imageMimeTypes.add("image/jpg");
		imageMimeTypes.add("image/jpeg");
		imageMimeTypes.add("image/png");
	}
	
	private TextElement titleEl, summaryEl;
	private SingleSelection bindersEl, sectionsEl;
	
	private FileElement fileUpload;
	private static final int picUploadlimitKB = 5120;
	
	private Page page;
	private Binder currentBinder;
	private Section currentSection;
	
	private final boolean chooseBinder;
	private final boolean chooseSection;
	
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
		initForm(ureq);
	}
	
	public PageMetadataEditController(UserRequest ureq, WindowControl wControl,
			Binder currentBinder, boolean chooseBinder,
			Section currentSection, boolean chooseSection, Page page) {
		super(ureq, wControl);

		this.page = page;
		
		this.currentBinder = currentBinder;
		this.currentSection = currentSection;
		
		this.chooseBinder = chooseBinder;
		this.chooseSection = chooseSection;
		
		initForm(ureq);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String title = page == null ? null : page.getTitle();
		titleEl = uifactory.addTextElement("title", "page.title", 255, title, formLayout);
		titleEl.setMandatory(true);
		
		String summary = page == null ? null : page.getSummary();
		summaryEl = uifactory.addTextAreaElement("summary", "page.summary", 4096, 4, 60, false, summary, formLayout);
		summaryEl.setPlaceholderKey("summary.placeholder", null);
		
		fileUpload = uifactory.addFileElement(getWindowControl(), "file", "fileupload",formLayout);			
		
		fileUpload.setPreview(ureq.getUserSession(), true);
		fileUpload.addActionListener(FormEvent.ONCHANGE);
		fileUpload.setDeleteEnabled(true);
		fileUpload.setHelpText("background img of binder");
		fileUpload.limitToMimeType(imageMimeTypes, null, null);
		fileUpload.setMaxUploadSizeKB(picUploadlimitKB, null, null);
		if(page != null) {
			File posterImg = portfolioService.getPosterImage(page);
			if(posterImg != null) {
				fileUpload.setInitialFile(posterImg);
			}
		}
		
		// list of binder
		if (chooseBinder) {
			List<Binder> binders = portfolioService.searchOwnedBinders(getIdentity());

			String[] theKeys = new String[binders.size()+1];
			String[] theValues = new String[binders.size()+1];

			for (int i = 0; i < binders.size(); ++i) {
				theKeys[i] = binders.get(i).getKey().toString();
				theValues[i] = StringHelper.escapeHtml(binders.get(i).getTitle());
			} 
			
			theKeys[binders.size()] = "none";
			theValues[binders.size()] = "none";

			bindersEl = uifactory.addDropdownSingleselect("binders", "page.binders", formLayout, theKeys, theValues, null);
			bindersEl.addActionListener(FormEvent.ONCHANGE);
			
			if (currentBinder == null) {
				currentBinder = binders.get(0);
			} else {
				for (String key : theKeys) {
					if (key.equals(currentBinder.getKey().toString()))
						bindersEl.select(key, true);
				}
			}
		} else {
			
			String[] theKeys = new String[] { currentBinder.getKey().toString() };
			String[] theValues = new String[] { StringHelper.escapeHtml(currentBinder.getTitle()) };

			bindersEl = uifactory.addDropdownSingleselect("binders", "page.binders", formLayout, theKeys, theValues, null);
			bindersEl.setEnabled(false);
		}

		//list of sections
		if(chooseSection) {
			retrieveSections(formLayout, true);
		} else {// currently never used
			String[] theKeys = new String[] { currentSection.getKey().toString() };
			String[] theValues = new String[]{ StringHelper.escapeHtml(currentSection.getTitle()) };
			sectionsEl = uifactory.addDropdownSingleselect("sections", "page.sections", formLayout, theKeys, theValues, null);
			sectionsEl.setEnabled(false);
		}

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
	
	protected void retrieveSections(FormItemContainer formLayout, boolean updateBox) {
		
		List<Section> sections = portfolioService.getSections(currentBinder);
		if (sections.isEmpty()) {
			// wrong
		} else {
			int numOfSections = sections.size();
			String selectedKey = null;
			String[] theKeys = new String[numOfSections];
			String[] theValues = new String[numOfSections];
			for (int i = 0; i < numOfSections; i++) {
				Long sectionKey = sections.get(i).getKey();
				theKeys[i] = sectionKey.toString();
				theValues[i] = (i + 1) + ". " + StringHelper.escapeHtml(sections.get(i).getTitle());
				if (currentSection != null && currentSection.getKey().equals(sectionKey)) {
					selectedKey = theKeys[i];
				}
			}
			
			if (updateBox) {
				sectionsEl = uifactory.addDropdownSingleselect("sections", "page.sections", formLayout, theKeys,
						theValues, null);
			} else {
				sectionsEl.setKeysAndValues(theKeys, theValues, null);
			}
			
			if (selectedKey != null) {
				sectionsEl.select(selectedKey, true);
			}
		}
	}
	
	protected void updateSections (){
		
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {

		if (page == null) {
			String title = titleEl.getValue();
			String summary = summaryEl.getValue();
			SectionRef selectSection = null;
			if (sectionsEl.isOneSelected() && sectionsEl.isEnabled() && sectionsEl.isVisible()) {
				String selectedKey = sectionsEl.getSelectedKey();
				selectSection = new SectionKeyRef(new Long(selectedKey));
			}
			String imagePath = null;
			if (fileUpload.getUploadFile() != null) {
				imagePath = portfolioService.addPosterImageForPage(fileUpload.getUploadFile(),
						fileUpload.getUploadFileName());
			}
			portfolioService.appendNewPage(getIdentity(), title, summary, imagePath, selectSection);
		} else {
			page.setTitle(titleEl.getValue());
			page.setSummary(summaryEl.getValue());

			if (fileUpload.getUploadFile() != null) {
				String imagePath = portfolioService.addPosterImageForPage(fileUpload.getUploadFile(),
						fileUpload.getUploadFileName());
				page.setImagePath(imagePath);
			} else if (fileUpload.getInitialFile() == null) {
				page.setImagePath(null);
				portfolioService.removePosterImage(page);
			}

			page = portfolioService.updatePage(page);
		}

		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		if (fileUpload == source) {
			if (event instanceof FileElementEvent) {
				String cmd = event.getCommand();
				if (FileElementEvent.DELETE.equals(cmd)) {
					if(fileUpload.getUploadFile() != null) {
						fileUpload.reset();
					} else if(fileUpload.getInitialFile() != null) {
						fileUpload.setInitialFile(null);
					}
				}
			}
		} else if (bindersEl == source) {
			if (bindersEl.getSelectedKey().equals("none")) {
				sectionsEl.setVisible(false);
				currentBinder = null;
			} else {
				currentBinder = portfolioService.searchOwnedBinders(getIdentity()).get(bindersEl.getSelected());
				sectionsEl.setVisible(true);
				retrieveSections(flc, false);
			}
		}
	}
}