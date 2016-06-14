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
	private SectionRef currentSection;
	
	private final boolean chooseBinder;
	private final boolean chooseSection;
	
	@Autowired
	private PortfolioService portfolioService;
	
	public PageMetadataEditController(UserRequest ureq, WindowControl wControl,
			Binder currentBinder, boolean chooseBinder,
			SectionRef currentSection, boolean chooseSection) {
		super(ureq, wControl);
		
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
		//fileUpload.setExampleKey("advanced_form.file", null);
		fileUpload.limitToMimeType(imageMimeTypes, null, null);
		fileUpload.setMaxUploadSizeKB(picUploadlimitKB, null, null);

		
		//list of binder
		if(chooseBinder) {
			
		} else {
			String[] theKeys = new String[] { currentBinder.getKey().toString() };
			String[] theValues = new String[]{ StringHelper.escapeHtml(currentBinder.getTitle()) };
			bindersEl = uifactory.addDropdownSingleselect("binders", "page.binders", formLayout, theKeys, theValues, null);
		}
		
		//list of sections
		if(chooseSection) {
			if(chooseBinder) {
				
			} else {
				List<Section> sections = portfolioService.getSections(currentBinder);
				if(sections.isEmpty()) {
					//wrong
				} else {
					int numOfSections = sections.size();
					String selectedKey = null;
					String[] theKeys = new String[numOfSections];
					String[] theValues = new String[numOfSections];
					for(int i=0; i<numOfSections; i++) {
						Long sectionKey = sections.get(i).getKey();
						theKeys[i] = sectionKey.toString();
						theValues[i] = (i + 1) + ". " + StringHelper.escapeHtml(sections.get(i).getTitle());
						if(currentSection != null && currentSection.getKey().equals(sectionKey)) {
							selectedKey = theKeys[i];
						}
					}
					sectionsEl = uifactory.addDropdownSingleselect("sections", "page.sections", formLayout, theKeys, theValues, null);
					if(selectedKey != null) {
						sectionsEl.select(selectedKey, true);
					}
				}
			}	
		} else {
			
		}

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("create.page", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String title = titleEl.getValue();
		String summary = summaryEl.getValue();
		
		SectionRef selectSection = null;
		if(sectionsEl.isOneSelected() && sectionsEl.isEnabled()) {
			String selectedKey = sectionsEl.getSelectedKey();
			selectSection = new SectionKeyRef(new Long(selectedKey));
		}
		portfolioService.appendNewPage(title, summary, selectSection);
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
					fileUpload.reset();
				}
			}
		}
	}
}
