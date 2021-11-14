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
package org.olat.modules.qpool.ui.metadata;

import static org.olat.modules.qpool.ui.metadata.MetaUIFactory.validateElementLogic;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.qpool.MetadataSecurityCallback;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemAuditLog.Action;
import org.olat.modules.qpool.QuestionItemAuditLogBuilder;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.events.QItemEdited;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 05.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TechnicalMetadataEditController extends FormBasicController  {


	private StaticTextElement editorEl;
	private StaticTextElement formatEl;
	private StaticTextElement editorVersionEl;
	private StaticTextElement lastModifiedEl;
	private StaticTextElement statusLastModifiedEl;	
	private TextElement versionEl;
	private FormLayoutContainer buttonsCont;
	
	private QuestionItem item;
	
	@Autowired
	private QPoolService qpoolService;

	public TechnicalMetadataEditController(UserRequest ureq, WindowControl wControl, QuestionItem item,
			MetadataSecurityCallback securityCallback, boolean wideLayout) {
		super(ureq, wControl, wideLayout ? LAYOUT_DEFAULT : LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(QuestionsController.class, getLocale(), getTranslator()));
		
		this.item = item;
		
		initForm(ureq);
		setItem(item, securityCallback);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uifactory.addStaticTextElement("general.identifier", item.getIdentifier(), formLayout);
		
		uifactory.addStaticTextElement("general.master.identifier", item.getMasterIdentifier(), formLayout);

		editorEl = uifactory.addStaticTextElement("technical.editor", "", formLayout);

		editorVersionEl = uifactory.addStaticTextElement("technical.editorVersion", "", formLayout);

		formatEl = uifactory.addStaticTextElement("technical.format", "", formLayout);
		
		Formatter formatter = Formatter.getInstance(getLocale());
		String creationDate = formatter.formatDateAndTime(item.getCreationDate());
		if(StringHelper.containsNonWhitespace(creationDate)) {
			uifactory.addStaticTextElement("technical.creation", creationDate, formLayout);
		}
		
		lastModifiedEl = uifactory.addStaticTextElement("technical.lastModified", "", formLayout);

		versionEl = uifactory.addTextElement("lifecycle.version", "lifecycle.version", 50, "", formLayout);
		
		statusLastModifiedEl = uifactory.addStaticTextElement("technical.statusLastModified", "", formLayout);
		
		buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("ok", "ok", buttonsCont);
	}
	
	private void setReadOnly(MetadataSecurityCallback securityCallback) {
		boolean canChangeVersion = securityCallback.canChangeVersion();
		versionEl.setEnabled(canChangeVersion);
		buttonsCont.setVisible(canChangeVersion);
	}

	private void updateUI() {
		String editor = item.getEditor() == null ? "" : item.getEditor();
		editorEl.setValue(editor);
		
		String editorVersion = item.getEditorVersion() == null ? "" : item.getEditorVersion();
		editorVersionEl.setValue(editorVersion);
		
		String format = item.getFormat() == null ? "" : item.getFormat();
		formatEl.setValue(format);
		
		Formatter formatter = Formatter.getInstance(getLocale());
		
		String lastModified = formatter.formatDateAndTime(item.getLastModified());
		lastModifiedEl.setValue(lastModified);
		lastModifiedEl.setVisible(StringHelper.containsNonWhitespace(lastModified));
		
		versionEl.setValue(item.getItemVersion());
		
		String statusLastModified = formatter.formatDateAndTime(item.getQuestionStatusLastModified());
		statusLastModified = statusLastModified != null ? statusLastModified: "";
		statusLastModifiedEl.setValue(statusLastModified);
		statusLastModifiedEl.setVisible(StringHelper.containsNonWhitespace(statusLastModified));
	}

	public void setItem(QuestionItem item, MetadataSecurityCallback securityCallback) {
		this.item = item;
		updateUI();
		if (securityCallback != null) {
			setReadOnly(securityCallback);
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		allOk &= validateElementLogic(versionEl, versionEl.getMaxLength(), false, true);
		return allOk &= super.validateFormLogic(ureq);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		if(item instanceof QuestionItemImpl) {
			QuestionItemImpl itemImpl = (QuestionItemImpl)item;
			QuestionItemAuditLogBuilder builder = qpoolService.createAuditLogBuilder(getIdentity(),
					Action.UPDATE_QUESTION_ITEM_METADATA);
			builder.withBefore(itemImpl);
			
			itemImpl.setItemVersion(versionEl.getValue());
			
			item = qpoolService.updateItem(item);
			builder.withAfter(itemImpl);
			qpoolService.persist(builder.create());
			fireEvent(ureq, new QItemEdited(item));
		}
	}

}
