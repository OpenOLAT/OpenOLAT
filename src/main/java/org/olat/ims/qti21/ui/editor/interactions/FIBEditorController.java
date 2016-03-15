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
package org.olat.ims.qti21.ui.editor.interactions;

import java.io.File;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.RichTextConfiguration;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.AbstractEntry;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.NumericalEntry;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.TextEntry;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;

/**
 * 
 * Initial date: 24.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FIBEditorController extends FormBasicController {
	
	private TextElement titleEl;
	private RichTextElement textEl;

	private CloseableModalController cmc;
	private FIBTextEntrySettingsController textEntrySettingsCtrl;
	private FIBNumericalEntrySettingsController numericalEntrySettingsCtrl;

	private final File itemFile;
	private final File rootDirectory;
	private final VFSContainer rootContainer;
	private final QTI21QuestionType preferredType;
	private final FIBAssessmentItemBuilder itemBuilder;
	
	public FIBEditorController(UserRequest ureq, WindowControl wControl,
			QTI21QuestionType preferredType, FIBAssessmentItemBuilder itemBuilder,
			File rootDirectory, VFSContainer rootContainer, File itemFile) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.itemFile = itemFile;
		this.itemBuilder = itemBuilder;
		this.preferredType = preferredType;
		this.rootDirectory = rootDirectory;
		this.rootContainer = rootContainer;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		titleEl = uifactory.addTextElement("title", "form.imd.title", -1, itemBuilder.getTitle(), formLayout);
		titleEl.setMandatory(true);
		
		String relativePath = rootDirectory.toPath().relativize(itemFile.toPath().getParent()).toString();
		VFSContainer itemContainer = (VFSContainer)rootContainer.resolve(relativePath);
		
		String question = itemBuilder.getQuestion();
		textEl = uifactory.addRichTextElementForStringData("desc", "form.imd.descr", question, 16, -1, true, itemContainer, null,
				formLayout, ureq.getUserSession(), getWindowControl());
		textEl.addActionListener(FormEvent.ONCLICK);
		RichTextConfiguration richTextConfig = textEl.getEditorConfiguration();
		
		boolean hasNumericals = itemBuilder.hasNumericalInputs();
		boolean hasTexts = itemBuilder.hasTextEntry();
		if(!hasNumericals && !hasTexts) {
			if(preferredType == QTI21QuestionType.numerical) {
				hasNumericals = true;
				hasTexts = false;
			} else if(preferredType == QTI21QuestionType.fib) {
				hasNumericals = false;
				hasTexts = true;
			} else {
				hasNumericals = true;
				hasTexts = true;
			}
		}
		richTextConfig.enableQTITools(hasTexts, hasNumericals);
		
		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		formLayout.add(buttonsContainer);
		uifactory.addFormSubmitButton("submit", buttonsContainer);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(textEntrySettingsCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(numericalEntrySettingsCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(numericalEntrySettingsCtrl);
		removeAsListenerAndDispose(textEntrySettingsCtrl);
		removeAsListenerAndDispose(cmc);
		numericalEntrySettingsCtrl = null;
		textEntrySettingsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(textEl == source) {
			String cmd = event.getCommand();
			if("gapentry".equals(cmd)) {
				String responseIdentifier = ureq.getParameter("responseIdentifier");
				String type = ureq.getParameter("gapType");
				doGapEntry(ureq, responseIdentifier, type);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//title
		itemBuilder.setTitle(titleEl.getValue());
		//set the question with the text entries
		String questionText = textEl.getRawValue();
		itemBuilder.setQuestion(questionText);

		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED, itemBuilder.getAssessmentItem(), QTI21QuestionType.fib));
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		//
	}

	private void doGapEntry(UserRequest ureq, String responseIdentifier, String type) {
		if(textEntrySettingsCtrl != null || numericalEntrySettingsCtrl != null) return;
		
		AbstractEntry interaction = itemBuilder.getEntry(responseIdentifier);
		if(interaction == null) {
			if("string".equalsIgnoreCase(type)) {
				interaction = itemBuilder.createTextEntry(responseIdentifier);
			} else if("float".equalsIgnoreCase(type)) {
				interaction = itemBuilder.createNumericalEntry(responseIdentifier);
			}
		}
		
		if(interaction instanceof TextEntry) {
			textEntrySettingsCtrl = new FIBTextEntrySettingsController(ureq, getWindowControl(), (TextEntry)interaction);
			listenTo(textEntrySettingsCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), textEntrySettingsCtrl.getInitialComponent(), true, translate("title.add") );
			cmc.activate();
			listenTo(cmc);
		} else if(interaction instanceof NumericalEntry) {
			numericalEntrySettingsCtrl = new FIBNumericalEntrySettingsController(ureq, getWindowControl(), (NumericalEntry)interaction);
			listenTo(numericalEntrySettingsCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), numericalEntrySettingsCtrl.getInitialComponent(), true, translate("title.add") );
			cmc.activate();
			listenTo(cmc);
		}
	}
}
