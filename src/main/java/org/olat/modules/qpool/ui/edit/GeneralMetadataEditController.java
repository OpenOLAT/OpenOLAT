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
package org.olat.modules.qpool.ui.edit;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.ui.MetadatasController;

/**
 * 
 * Initial date: 05.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GeneralMetadataEditController extends FormBasicController {
	
	private TextElement titleEl, keywordsEl, coverageEl, addInfosEl, languageEl;
	
	private String taxonomicPath;
	private QuestionItem item;
	private final QPoolService qpoolService;
	
	public GeneralMetadataEditController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(MetadatasController.class, ureq.getLocale(), getTranslator()));
		
		this.item = item;
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		taxonomicPath = qpoolService.getTaxonomicPath(item);
		
		initForm(ureq);
	}
	
	public GeneralMetadataEditController(UserRequest ureq, WindowControl wControl, QuestionItem item, Form rootForm) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, rootForm);
		
		this.item = item;
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		taxonomicPath = qpoolService.getTaxonomicPath(item);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("general");

		uifactory.addStaticTextElement("general.key", item.getKey().toString(), formLayout);
		uifactory.addStaticTextElement("general.identifier", item.getIdentifier(), formLayout);
		uifactory.addStaticTextElement("general.master.identifier", item.getMasterIdentifier(), formLayout);
		
		String title = item.getTitle();
		titleEl = uifactory.addTextElement("general.title", "general.title", 1000, title, formLayout);
		String keywords = item.getKeywords();
		keywordsEl = uifactory.addTextElement("general.keywords", "general.keywords", 1000, keywords, formLayout);
		String coverage = item.getCoverage();
		coverageEl = uifactory.addTextElement("general.coverage", "general.coverage", 1000, coverage, formLayout);
		String addInfos = item.getAdditionalInformations();
		addInfosEl = uifactory.addTextElement("general.additional.informations", "general.additional.informations",
				256, addInfos, formLayout);
		String language = item.getLanguage();
		languageEl = uifactory.addTextElement("general.language", "general.language", 10, language, formLayout);
		
		//classification
		uifactory.addStaticTextElement("classification.taxonomic.path", taxonomicPath == null ? "" : taxonomicPath, formLayout);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", "ok", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		allOk &= validateElementLogic(titleEl, titleEl.getMaxLength(), true);
		allOk &= validateElementLogic(keywordsEl, keywordsEl.getMaxLength(), false);
		allOk &= validateElementLogic(coverageEl, coverageEl.getMaxLength(), false);
		allOk &= validateElementLogic(addInfosEl, addInfosEl.getMaxLength(), false);
		allOk &= validateElementLogic(languageEl, languageEl.getMaxLength(), true);
		return allOk && super.validateFormLogic(ureq);
	}
	
	private boolean validateElementLogic(TextElement el, int maxLength, boolean mandatory) {
		boolean allOk = true;
		String value = el.getValue();
		el.clearError();
		if(mandatory && !StringHelper.containsNonWhitespace(value)) {
			el.setErrorKey("form.mandatory.hover", null);
			allOk = false;
		} else if (value != null && value.length() > maxLength) {
			String[] lengths = new String[]{ Integer.toString(maxLength), Integer.toString(value.length())};
			el.setErrorKey("error.input.toolong", lengths);
			allOk = false;
		}
		return allOk;
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(item instanceof QuestionItemImpl) {
			QuestionItemImpl itemImpl = (QuestionItemImpl)item;
			itemImpl.setTitle(titleEl.getValue());
			itemImpl.setKeywords(keywordsEl.getValue());
			itemImpl.setCoverage(coverageEl.getValue());
			itemImpl.setAdditionalInformations(addInfosEl.getValue());
			itemImpl.setLanguage(languageEl.getValue());
		}
		item = qpoolService.updateItem(item);
		fireEvent(ureq, new QItemEdited(item));
	}
}