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
import static org.olat.modules.qpool.ui.metadata.MetaUIFactory.validateSelection;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.ims.qti.QTIConstants;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.events.QItemEdited;

/**
 * 
 * Initial date: 05.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TechnicalMetadataEditController extends FormBasicController {
	
	private TextElement editorEl, editorVersionEl;
	private SingleSelection formatEl;
	
	private QuestionItem item;
	private final QPoolService qpoolService;

	public TechnicalMetadataEditController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(QuestionsController.class, getLocale(), getTranslator()));
		
		this.item = item;
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("technical");
		
		uifactory.addStaticTextElement("general.identifier", item.getIdentifier(), formLayout);
		uifactory.addStaticTextElement("general.master.identifier", item.getMasterIdentifier(), formLayout);

		editorEl = uifactory.addTextElement("technical.editor", "technical.editor", 50, item.getEditor(), formLayout);
		editorVersionEl = uifactory.addTextElement("technical.editorVersion", "technical.editorVersion", 50, item.getEditorVersion(), formLayout);
		
		String[] formatKeys = new String[]{ QTIConstants.QTI_12_FORMAT };
		formatEl = uifactory.addDropdownSingleselect("technical.format", "technical.format", formLayout,
				formatKeys, formatKeys, null);
		
		Formatter formatter = Formatter.getInstance(getLocale());
		String creationDate = formatter.formatDateAndTime(item.getCreationDate());
		uifactory.addStaticTextElement("technical.creation", creationDate, formLayout);
		String lastModified = formatter.formatDateAndTime(item.getLastModified());
		uifactory.addStaticTextElement("technical.lastModified", lastModified, formLayout);

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
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		allOk &= validateElementLogic(editorEl, editorEl.getMaxLength(), true, true);
		allOk &= validateElementLogic(editorVersionEl, editorVersionEl.getMaxLength(), true, true);
		allOk &= validateSelection(formatEl, true);
		return allOk &= super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(item instanceof QuestionItemImpl) {
			QuestionItemImpl itemImpl = (QuestionItemImpl)item;
			itemImpl.setEditor(editorEl.getValue());
			itemImpl.setEditorVersion(editorVersionEl.getValue());
			if(formatEl.isOneSelected()) {
				itemImpl.setFormat(formatEl.getSelectedKey());
			}
		}
		item = qpoolService.updateItem(item);
		fireEvent(ureq, new QItemEdited(item));
	}
}