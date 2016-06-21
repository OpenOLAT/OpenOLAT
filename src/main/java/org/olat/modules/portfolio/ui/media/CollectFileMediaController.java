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
package org.olat.modules.portfolio.ui.media;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.handler.FileHandler;
import org.olat.modules.portfolio.ui.PortfolioHomeController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CollectFileMediaController extends FormBasicController {
	
	private FileElement fileEl;
	private TextElement titleEl;
	private TextElement descriptionEl;
	private TextBoxListElement categoriesEl;

	private Media mediaReference;
	private Map<String,String> categories = new HashMap<>();
	
	private final String businessPath;
	
	@Autowired
	private FileHandler fileHandler;
	@Autowired
	private PortfolioService portfolioService;

	public CollectFileMediaController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(PortfolioHomeController.class, getLocale(), getTranslator()));
		businessPath = "[Binder:0]";
		
		initForm(ureq);
	}
	
	public Media getMediaReference() {
		return mediaReference;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		titleEl = uifactory.addTextElement("artefact.title", "artefact.title", 255, "", formLayout);
		titleEl.setMandatory(true);
		
		descriptionEl = uifactory.addRichTextElementForStringData("artefact.descr", "artefact.descr", "", 8, 6, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		
		fileEl = uifactory.addFileElement(getWindowControl(), "artefact.file", "artefact.file", formLayout);
		fileEl.addActionListener(FormEvent.ONCHANGE);
		
		
		categoriesEl = uifactory.addTextBoxListElement("categories", "categories", "categories.hint", categories, formLayout, getTranslator());
		categoriesEl.setElementCssClass("o_sel_ep_tagsinput");
		categoriesEl.setAllowDuplicates(false);
		
		//String source = "Forum";
		//uifactory.addStaticTextElement("artefact.source", "artefact.source", source, formLayout);
		
		String date = Formatter.getInstance(getLocale()).formatDate(new Date());
		uifactory.addStaticTextElement("artefact.collect.date", "artefact.collect.date", date, formLayout);

		if(StringHelper.containsNonWhitespace(businessPath)) {
			String link = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
			uifactory.addStaticTextElement("artefact.collect.link", "artefact.collect.link", link, formLayout);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", "save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;

		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(mediaReference == null) {
			String title = titleEl.getValue();
			String description = descriptionEl.getValue();
			File uploadedFile = fileEl.getUploadFile();
			String uploadedFilename = fileEl.getUploadFileName();
			mediaReference = fileHandler.createMedia(title, description, uploadedFile, uploadedFilename, businessPath, getIdentity());
		} else {
			//TODO can we update an artefact?
		}

		List<String> updatedCategories = categoriesEl.getValueList();
		portfolioService.updateCategories(mediaReference, updatedCategories);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
