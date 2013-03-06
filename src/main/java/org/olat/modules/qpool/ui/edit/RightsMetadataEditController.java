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

import java.util.Arrays;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionPoolService;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.ui.QuestionItemMetadatasController;

/**
 * 
 * Initial date: 05.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RightsMetadataEditController extends FormBasicController {
	
	private SingleSelection copyrightEl;
	private TextElement descriptionEl;
	
	private QuestionItem item;
	private final QuestionPoolService qpoolService;
	private static final String[] keys = {
		"-",
		"all rights reserved",
		"CC by",
		"CC by-sa",
		"CC by-nd",
		"CC by-nc",
		"CC by-nc-sa",
		"CC by-nc-nd",
		"freetext"
	};

	public RightsMetadataEditController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(QuestionItemMetadatasController.class, ureq.getLocale(), getTranslator()));
		
		this.item = item;
		qpoolService = CoreSpringFactory.getImpl(QuestionPoolService.class);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("rights");

		String[] values = Arrays.copyOf(keys, keys.length);
		values[0] = "None";
		values[values.length - 1] = "Freetext";
		copyrightEl = uifactory.addDropdownSingleselect("rights.copyright", "rights.copyright", formLayout, keys, values, null);
		copyrightEl.addActionListener(this, FormEvent.ONCHANGE);
		String copyright = item.getCopyright();
		if(!StringHelper.containsNonWhitespace(copyright)) {
			copyrightEl.select(keys[0], true);
		} else if(isKey(copyright)) {
			copyrightEl.select(copyright, true);
		} else {
			copyrightEl.select(keys[keys.length - 1], true);
		}
		
		String description = copyright == null ? "" : copyright;
		descriptionEl = uifactory.addTextAreaElement("rights.description", "rights.description", 1000, 6, 40, true, description, formLayout);
		descriptionEl.setVisible(copyrightEl.getSelectedKey().equals(keys[keys.length - 1]));

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", "ok", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private boolean isKey(String value) {
		for(String key:keys) {
			if(key.equals(value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == copyrightEl) {
			String selectedKey = copyrightEl.getSelectedKey();
			descriptionEl.setVisible(selectedKey.equals(keys[keys.length - 1]));
			flc.setDirty(true);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		copyrightEl.clearError();
		descriptionEl.clearError();
		if(!copyrightEl.isOneSelected()) {
			copyrightEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		} else if(copyrightEl.getSelectedKey().equals(keys[keys.length - 1])) {
			String licence = descriptionEl.getValue();
			if(!StringHelper.containsNonWhitespace(licence)) {
				descriptionEl.setErrorKey("form.mandatory.hover", null);
				allOk &= false;
			}
		}
		return allOk &= super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(item instanceof QuestionItemImpl) {
			QuestionItemImpl itemImpl = (QuestionItemImpl)item;
			String selectedKey = copyrightEl.getSelectedKey();
			if(selectedKey.equals(keys[0])) {
				itemImpl.setCopyright(null);
			} else if(selectedKey.equals(keys[keys.length - 1])) {
				itemImpl.setCopyright(descriptionEl.getValue());
			} else {
				itemImpl.setCopyright(selectedKey);
			}
		}
		item = qpoolService.updateItem(item);
		fireEvent(ureq, new QItemEdited(item));
	}
}