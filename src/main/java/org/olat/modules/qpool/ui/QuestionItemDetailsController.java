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
package org.olat.modules.qpool.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionStatus;

/**
 * 
 * Initial date: 24.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionItemDetailsController extends FormBasicController {

	private final QuestionItem item;
	
	public QuestionItemDetailsController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		super(ureq, wControl, "item_details");
		this.item = item;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		//general
		FormLayoutContainer standardCont = FormLayoutContainer.createDefaultFormLayout("details_general", getTranslator());
		formLayout.add(standardCont);
		formLayout.add("details_general", standardCont);
		standardCont.setFormTitle(translate("general"));

		uifactory.addStaticTextElement("item.key", item.getKey().toString(), standardCont);
		uifactory.addStaticTextElement("item.subject", item.getSubject(), standardCont);
		String studyField = "";
		uifactory.addStaticTextElement("item.studyField", studyField, standardCont);
		uifactory.addStaticTextElement("item.keywords", "", standardCont);
		
		String type = item.getType();
		if(StringHelper.containsNonWhitespace(type)) {
			type = translate("item.type." + type.toLowerCase());
		}
		uifactory.addStaticTextElement("item.type", type, standardCont);
		uifactory.addStaticTextElement("item.language", "", standardCont);
		
		String s = "";
		QuestionStatus status = item.getQuestionStatus();
		if(status != null) {
			s = translate(status.name());
		}
		uifactory.addStaticTextElement("item.status", s, standardCont);
		
		//description
		FormLayoutContainer descriptionCont = FormLayoutContainer.createDefaultFormLayout("details_description", getTranslator());
		formLayout.add(descriptionCont);
		formLayout.add("details_description", descriptionCont);
		descriptionCont.setFormTitle(translate("item.description"));
		
		//rights
		FormLayoutContainer rightsCont = FormLayoutContainer.createDefaultFormLayout("details_rights", getTranslator());
		formLayout.add(rightsCont);
		formLayout.add("details_rights", rightsCont);
		rightsCont.setFormTitle(translate("rights"));
		
		//applications
		FormLayoutContainer applicationsCont = FormLayoutContainer.createDefaultFormLayout("details_applications", getTranslator());
		formLayout.add(applicationsCont);
		formLayout.add("details_applications", applicationsCont);
		applicationsCont.setFormTitle(translate("applications"));

		uifactory.addStaticTextElement("item.selectivity", "", applicationsCont);
		uifactory.addStaticTextElement("item.usage", "", applicationsCont);
		
		//technics
		FormLayoutContainer technicsCont = FormLayoutContainer.createDefaultFormLayout("details_technics", getTranslator());
		formLayout.add(technicsCont);
		formLayout.add("details_technics", technicsCont);
		technicsCont.setFormTitle(translate("technics"));
		
		Formatter format = Formatter.getInstance(getLocale());
		uifactory.addStaticTextElement("item.creation", format.formatDate(item.getCreationDate()), technicsCont);
		uifactory.addStaticTextElement("item.lastModified", format.formatDate(item.getLastModified()), technicsCont);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
