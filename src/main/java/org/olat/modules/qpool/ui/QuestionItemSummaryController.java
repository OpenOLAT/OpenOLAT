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

import java.math.BigDecimal;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.QuestionItem;

/**
 * 
 * Initial date: 12.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionItemSummaryController extends FormBasicController {
	
	private StaticTextElement keyEl;
	private StaticTextElement subjectEl;
	private StaticTextElement studyFieldEl;
	private StaticTextElement keywordsEl;
	private StaticTextElement selectivityEl;
	private StaticTextElement usageEl;
	private StaticTextElement descriptionEl;
	

	private QuestionItem item;
	
	public QuestionItemSummaryController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("metadatas");
		
		keyEl = uifactory.addStaticTextElement("item.key", "", formLayout);
		subjectEl = uifactory.addStaticTextElement("item.subject", "", formLayout);
		studyFieldEl = uifactory.addStaticTextElement("item.studyField", "", formLayout);
		keywordsEl = uifactory.addStaticTextElement("item.keywords", "", formLayout);
		selectivityEl = uifactory.addStaticTextElement("item.selectivity", "", formLayout);
		usageEl = uifactory.addStaticTextElement("item.usage", "", formLayout);
		descriptionEl = uifactory.addStaticTextElement("item.description", "", formLayout);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	public QuestionItem getItem() {
		return item;
	}
	
	public void updateItem(QuestionItem item) {
		this.item = item;

		keyEl.setValue(item.getKey().toString());
		subjectEl.setValue(item.getSubject());
		studyFieldEl.setValue("");
		
		String keywords = item.getKeywords();
		if(StringHelper.containsNonWhitespace(keywords)) {
			keywordsEl.setValue(keywords);
		} else {
			keywordsEl.setValue("");
		}
		
		BigDecimal selectivity = item.getSelectivity();
		String selectivityStr = "";
		if(selectivity != null) {
			selectivityStr = selectivity.toPlainString();
		}
		selectivityEl.setValue(selectivityStr);
		
		int usage = item.getUsage();
		String usageStr = "";
		if(usage >= 0) {
			usageStr = Integer.toString(usage);
		}
		usageEl.setValue(usageStr);
		
		String description = item.getDescription();
		if(StringHelper.containsNonWhitespace(description)) {
			descriptionEl.setValue(description);
		} else {
			descriptionEl.setValue("");
		}
	}
	
	public void reset() {
		keyEl.setValue("");
		subjectEl.setValue("");
		studyFieldEl.setValue("");
		keywordsEl.setValue("");
		selectivityEl.setValue("");
		usageEl.setValue("");
		descriptionEl.setValue("");
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}