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
import java.io.StringReader;

import org.json.JSONException;
import org.json.JSONObject;
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
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.control.winmgr.JSCommand;
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
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;
import uk.ac.ed.ph.jqtiplus.internal.util.StringUtilities;

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
	private final boolean readOnly;
	private final boolean restrictedEdit;
	private final FIBAssessmentItemBuilder itemBuilder;
	
	public FIBEditorController(UserRequest ureq, WindowControl wControl, FIBAssessmentItemBuilder itemBuilder,
			File rootDirectory, VFSContainer rootContainer, File itemFile,
			boolean restrictedEdit, boolean readOnly) {
		super(ureq, wControl, LAYOUT_DEFAULT_2_10);
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.itemFile = itemFile;
		this.itemBuilder = itemBuilder;
		this.rootDirectory = rootDirectory;
		this.rootContainer = rootContainer;
		this.readOnly = readOnly;
		this.restrictedEdit = restrictedEdit;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("Configure test questions");
		
		titleEl = uifactory.addTextElement("title", "form.imd.title", -1, itemBuilder.getTitle(), formLayout);
		titleEl.setElementCssClass("o_sel_assessment_item_title");
		titleEl.setMandatory(true);
		titleEl.setEnabled(!readOnly);
		
		String relativePath = rootDirectory.toPath().relativize(itemFile.toPath().getParent()).toString();
		VFSContainer itemContainer = (VFSContainer)rootContainer.resolve(relativePath);
		
		String question = itemBuilder.getQuestion();
		textEl = uifactory.addRichTextElementForQTI21("desc", "form.imd.descr", question, 16, -1, itemContainer,
				formLayout, ureq.getUserSession(),  getWindowControl());
		textEl.addActionListener(FormEvent.ONCLICK);
		textEl.setElementCssClass("o_sel_assessment_item_fib_text");
		RichTextConfiguration richTextConfig = textEl.getEditorConfiguration();
		richTextConfig.setReadOnly(restrictedEdit || readOnly);
		richTextConfig.enableQTITools(true, true, false);

		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setElementCssClass("o_sel_fib_save");
		buttonsContainer.setRootForm(mainForm);
		buttonsContainer.setVisible(!readOnly);
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
			if(event == Event.DONE_EVENT) {
				String solution = textEntrySettingsCtrl.getSolution();
				String responseIdentifier = textEntrySettingsCtrl.getResponseIdentifier().toString();
				feedbackToTextElement(responseIdentifier, solution);
			} else if(event == Event.CANCELLED_EVENT) {
				cancelFeedbackToTextElement();
			}
			cmc.deactivate();
			cleanUp();
		} else if(numericalEntrySettingsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				Double val = numericalEntrySettingsCtrl.getSolution();
				String solution = val == null ? "" : Double.toString(val);
				String responseIdentifier = numericalEntrySettingsCtrl.getResponseIdentifier().toString();
				feedbackToTextElement(responseIdentifier, solution);
			} else if(event == Event.CANCELLED_EVENT) {
				cancelFeedbackToTextElement();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void feedbackToTextElement(String responseIdentifier, String solution) {
		try {
			JSONObject jo = new JSONObject();
			jo.put("responseIdentifier", responseIdentifier);
			jo.put("data-qti-solution", solution);
			Command jsc = new JSCommand("try { tinymce.activeEditor.execCommand('qtiUpdateTextEntry', false, " + jo.toString() + "); } catch(e){if(window.console) console.log(e) }");
			getWindowControl().getWindowBackOffice().sendCommandTo(jsc);
		} catch (JSONException e) {
			logError("", e);
		}
	}
	
	/**
	 * This helps TinyMCE to deselect the current tool.
	 */
	private void cancelFeedbackToTextElement() {
		try {
			Command jsc = new JSCommand("try { tinymce.activeEditor.execCommand('qtiCancelTextEntry'); } catch(e){if(window.console) console.log(e) }");
			getWindowControl().getWindowBackOffice().sendCommandTo(jsc);
		} catch (JSONException e) {
			logError("", e);
		}
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
				String newEntry = ureq.getParameter("newEntry");
				String emptySolution = ureq.getParameter("emptySolution");
				doGapEntry(ureq, responseIdentifier, selectedText, emptySolution, type, "true".equals(newEntry));
			} else if("copy-gapentry".equals(cmd)) {
				String responseIdentifier = ureq.getParameter("responseIdentifier");
				String selectedText = ureq.getParameter("selectedText");
				String type = ureq.getParameter("gapType");
				doCopyGapEntry(responseIdentifier, selectedText, type);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		String questionText = textEl.getRawValue();
		if(!StringHelper.containsNonWhitespace(questionText)) {
			textEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(!questionText.contains("<textentryinteraction")) {
			textEl.setErrorKey("error.missing.fib", null);
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(readOnly) return;
		//title
		itemBuilder.setTitle(titleEl.getValue());
		//set the question with the text entries
		String questionText = textEl.getRawValue();
		extractSolution(questionText);
		itemBuilder.setQuestion(questionText);

		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED, itemBuilder.getAssessmentItem(), QTI21QuestionType.fib));

		itemBuilder.extractQuestions();
		itemBuilder.extractEntriesSettingsFromResponseDeclaration();
		String question = itemBuilder.getQuestion();
		textEl.setValue(question);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		//
	}
	
	private void doCopyGapEntry(String responseIdentifier, String selectedText, String type) {
		AbstractEntry interaction = itemBuilder.getEntry(responseIdentifier);
		if(interaction == null) {
			createEntry(responseIdentifier, selectedText, type, true);
		}
	}

	private void doGapEntry(UserRequest ureq, String responseIdentifier, String selectedText, String emptySolution, String type, boolean newEntry) {
		if(textEntrySettingsCtrl != null || numericalEntrySettingsCtrl != null) return;
		
		AbstractEntry interaction = itemBuilder.getEntry(responseIdentifier);
		if(interaction == null) {
			interaction = createEntry(responseIdentifier, selectedText, type, newEntry);
		} else if(StringHelper.containsNonWhitespace(selectedText)) {
			updateSolution(interaction, selectedText, emptySolution);
		}
		
		if(interaction instanceof TextEntry) {
			textEntrySettingsCtrl = new FIBTextEntrySettingsController(ureq, getWindowControl(), (TextEntry)interaction,
					restrictedEdit, readOnly);
			listenTo(textEntrySettingsCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), textEntrySettingsCtrl.getInitialComponent(), true, translate("title.add") );
			cmc.activate();
			listenTo(cmc);
		} else if(interaction instanceof NumericalEntry) {
			numericalEntrySettingsCtrl = new FIBNumericalEntrySettingsController(ureq, getWindowControl(), (NumericalEntry)interaction,
					restrictedEdit, readOnly);
			listenTo(numericalEntrySettingsCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), numericalEntrySettingsCtrl.getInitialComponent(), true, translate("title.add") );
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private AbstractEntry createEntry(String responseIdentifier, String selectedText, String type, boolean newEntry) {
		AbstractEntry interaction = null;
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
					String solution = alternatives[0];
					if(newEntry && "gap".equals(solution)) {
						solution = "";
					}
					textInteraction.setSolution(solution);
				}
			}
			interaction = textInteraction;
		} else if("float".equalsIgnoreCase(type)) {
			NumericalEntry numericalInteraction = itemBuilder.createNumericalEntry(responseIdentifier);
			if(newEntry && "gap".equals(selectedText)) {
				//skip it, it's a placeholder
			} else if(StringHelper.containsNonWhitespace(selectedText)) {
				try {
					Double val = Double.parseDouble(selectedText.trim());
					numericalInteraction.setSolution(val);
				} catch (NumberFormatException e) {
					//
				}
			}
			interaction = numericalInteraction;
		}
		return interaction;
	}
	
	private void extractSolution(String content) {
		try {
			HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
			parser.setContentHandler(new SolutionExtractorHandler());
			parser.parse(new InputSource(new StringReader(content)));
		} catch (Exception e) {
			logError("", e);
		}
	}
	
	private void updateSolution(AbstractEntry entry, String solution, String solutionEmpty) {
		if(entry == null) {
			//problem
		} else if(entry instanceof TextEntry) {
			if("true".equals(solutionEmpty)) {
				((TextEntry)entry).setSolution("");
			} else {
				solution = itemBuilder.unescapeDataQtiSolution(solution);
				((TextEntry)entry).setSolution(solution);
			}
		} else if(entry instanceof NumericalEntry) {
			try {
				double val = Double.parseDouble(solution);
				((NumericalEntry)entry).setSolution(val);
			} catch (NumberFormatException e) {
				logError("", e);
			}
		}
	}
	
	private class SolutionExtractorHandler extends DefaultHandler {
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if("textentryinteraction".equals(localName)) {
				localName = qName = "textEntryInteraction";
				
				String solution = null;
				String solutionEmpty = null;
				String responseIdentifier = null;
				for(int i=0; i<attributes.getLength(); i++) {
					String name = attributes.getLocalName(i);
					if("data-qti-solution".equals(name)) {
						solution = attributes.getValue(i);
						if(solution != null) {
							solution = itemBuilder.unescapeDataQtiSolution(solution);
							solution = StringUtilities.trim(solution);
						}
					} else if("data-qti-solution-empty".equals(name)) {
						solutionEmpty = attributes.getValue(i);
					} else if("responseIdentifier".equalsIgnoreCase(name)) {
						responseIdentifier = attributes.getValue(i);
					}
				}
				
				if(StringHelper.containsNonWhitespace(responseIdentifier)
						&& (StringHelper.containsNonWhitespace(solution) || StringHelper.containsNonWhitespace(solutionEmpty))) {
					AbstractEntry entry = itemBuilder.getTextEntry(responseIdentifier);
					updateSolution(entry, solution, solutionEmpty);
				}
			}
		}
	}
}
