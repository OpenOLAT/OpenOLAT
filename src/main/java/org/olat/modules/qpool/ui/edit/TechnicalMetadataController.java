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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.ui.QPoolEvent;
import org.olat.modules.qpool.ui.QuestionItemMetadatasController;

/**
 * 
 * Initial date: 05.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TechnicalMetadataController extends FormBasicController  {
	
	private FormLink editLink;
	private StaticTextElement editorEl, editorVersionEl, formatEl, creationDateEl, lastModifiedEl;

	public TechnicalMetadataController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		super(ureq, wControl, "view");
		setTranslator(Util.createPackageTranslator(QuestionItemMetadatasController.class, ureq.getLocale(), getTranslator()));
		
		initForm(ureq);
		setItem(item);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("technical");

		editLink = uifactory.addFormLink("edit", "edit", null, formLayout, Link.BUTTON_XSMALL);
		editLink.setCustomEnabledLinkCSS("b_link_left_icon b_link_edit");
		
		FormLayoutContainer metaCont = FormLayoutContainer.createDefaultFormLayout("metadatas", getTranslator());
		formLayout.add("metadatas", metaCont);

		editorEl = uifactory.addStaticTextElement("technical.editor", "", metaCont);
		editorVersionEl = uifactory.addStaticTextElement("technical.editorVersion", "", metaCont);
		formatEl = uifactory.addStaticTextElement("technical.format", "", metaCont);
		creationDateEl = uifactory.addStaticTextElement("technical.creation", "", metaCont);
		lastModifiedEl = uifactory.addStaticTextElement("technical.lastModified", "", metaCont);
	}
	
	public void setItem(QuestionItem item) {
		String editor = item.getEditor() == null ? "" : item.getEditor();
		editorEl.setValue(editor);
		String editorVersion = item.getEditorVersion() == null ? "" : item.getEditorVersion();
		editorVersionEl.setValue(editorVersion);
		String format = item.getFormat() == null ? "" : item.getFormat();
		formatEl.setValue(format);
		Formatter formatter = Formatter.getInstance(getLocale());
		String creationDate = formatter.formatDateAndTime(item.getCreationDate());
		creationDateEl.setValue(creationDate);
		String lastModified = formatter.formatDateAndTime(item.getLastModified());
		lastModifiedEl.setValue(lastModified);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(editLink == source) {
			fireEvent(ureq, new QPoolEvent(QPoolEvent.EDIT));
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
}
