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
import org.olat.core.util.StringHelper;
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
	private final boolean restrictedEdit;
	private final FIBAssessmentItemBuilder itemBuilder;
	
	public FIBEditorController(UserRequest ureq, WindowControl wControl,
			QTI21QuestionType preferredType, FIBAssessmentItemBuilder itemBuilder,
			File rootDirectory, VFSContainer rootContainer, File itemFile, boolean restrictedEdit) {
		super(ureq, wControl, LAYOUT_DEFAULT_2_10);
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.itemFile = itemFile;
		this.itemBuilder = itemBuilder;
		this.preferredType = preferredType;
		this.rootDirectory = rootDirectory;
		this.rootContainer = rootContainer;
		this.restrictedEdit = restrictedEdit;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		titleEl = uifactory.addTextElement("title", "form.imd.title", -1, itemBuilder.getTitle(), formLayout);
		titleEl.setMandatory(true);
		
		String relativePath = rootDirectory.toPath().relativize(itemFile.toPath().getParent()).toString();
		VFSContainer itemContainer = (VFSContainer)rootContainer.resolve(relativePath);
		
		String question = itemBuilder.getQuestion();
		textEl = uifactory.addRichTextElementForQTI21("desc", "form.imd.descr", question, 16, -1, itemContainer,
				formLayout, ureq.getUserSession(),  getWindowControl());
		textEl.addActionListener(FormEvent.ONCLICK);
		textEl.setEnabled(!restrictedEdit);
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
		if(hasNumericals) {
			setFormContextHelp("Test editor QTI 2.1 in detail#details_testeditor_fragetypen_ni");
		} else {
			setFormContextHelp("Test editor QTI 2.1 in detail#details_testeditor_fragetypen_fib");
		}
		richTextConfig.enableQTITools(hasTexts, hasNumericals, false);
		
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
				String selectedText = ureq.getParameter("selectedText");
				String type = ureq.getParameter("gapType");
				doGapEntry(ureq, responseIdentifier, selectedText, type);
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

	private void doGapEntry(UserRequest ureq, String responseIdentifier, String selectedText, String type) {
		if(textEntrySettingsCtrl != null || numericalEntrySettingsCtrl != null) return;
		
		AbstractEntry interaction = itemBuilder.getEntry(responseIdentifier);
		if(interaction == null) {
			if("string".equalsIgnoreCase(type)) {
				TextEntry textInteraction = itemBuilder.createTextEntry(responseIdentifier);
				if(StringHelper.containsNonWhitespace(selectedText)) {
					String[] alternatives = selectedText.split(",");
					for(String alternative:alternatives) {
						if(StringHelper.containsNonWhitespace(alternative)) {
							alternative = alternative.trim();
							if(textInteraction.getSolution() == null) {
								textInteraction.setSolution(alternative);
							} else {
								textInteraction.addAlternative(alternative, textInteraction.getScore());
							}
						}
					}
					if(alternatives.length > 0) {
						textInteraction.setSolution(alternatives[0]);
					}
				}
				interaction = textInteraction;
			} else if("float".equalsIgnoreCase(type)) {
				NumericalEntry numericalInteraction = itemBuilder.createNumericalEntry(responseIdentifier);
				if(StringHelper.containsNonWhitespace(selectedText)) {
					try {
						Double val = Double.parseDouble(selectedText.trim());
						numericalInteraction.setSolution(val);
					} catch (NumberFormatException e) {
						//
					}
				}
				interaction = numericalInteraction;
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
