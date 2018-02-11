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
package org.olat.modules.forms.ui;

import static org.olat.modules.forms.handler.EvaluationFormResource.FORM_XML_FILE;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SliderElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FileElementEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.FileUpload;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.FormXStream;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.model.xml.TextInput;
import org.olat.modules.forms.ui.model.EvaluationFormElementWrapper;
import org.olat.modules.forms.ui.model.FileUploadWrapper;
import org.olat.modules.forms.ui.model.SliderWrapper;
import org.olat.modules.forms.ui.model.TextInputWrapper;
import org.olat.modules.portfolio.PageBody;
import org.olat.modules.portfolio.ui.editor.ValidatingController;
import org.olat.modules.portfolio.ui.editor.ValidationMessage;
import org.olat.modules.portfolio.ui.editor.ValidationMessage.Level;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormController extends FormBasicController implements ValidatingController {
	
	private static final OLog log = Tracing.createLoggerFor(EvaluationFormController.class);

	private int count = 0;
	private final Form form;
	private PageBody anchor;
	private boolean readOnly;
	private final boolean doneButton;
	private final Identity evaluator;
	private final RepositoryEntry formEntry;
	
	private EvaluationFormSession session;
	private List<EvaluationFormElementWrapper> elementWrapperList = new ArrayList<>();
	private final Map<String, EvaluationFormResponse> identifierToResponses = new HashMap<>();
	
	private FormSubmit saveAsDoneButton;
	
	private DialogBoxController confirmDoneCtrl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	
	/**
	 * The responses are saved, it's aimed at the binder where the assignment was deleted.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param xmlForm
	 */
	public EvaluationFormController(UserRequest ureq, WindowControl wControl, Identity evaluator, PageBody pageBody, String xmlForm, boolean readOnly) {
		super(ureq, wControl, "run");
		
		form = (Form)XStreamHelper.readObject(FormXStream.getXStream(), xmlForm);
		this.evaluator = evaluator;
		this.readOnly = readOnly;
		this.anchor = pageBody;
		formEntry = null;
		doneButton = false;
		loadResponses();
		initForm(ureq);
	}
	
	/**
	 * The responses are not saved, it's only a preview.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param formFile
	 */
	public EvaluationFormController(UserRequest ureq, WindowControl wControl, File formFile) {
		super(ureq, wControl, "run");
		form = (Form)XStreamHelper.readObject(FormXStream.getXStream(), formFile);
		evaluator = null;
		readOnly = false;
		formEntry = null;
		doneButton = false;
		initForm(ureq);
	}
	
	/**
	 * The responses are saved and linked to the anchor.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param form
	 * @param anchor The database object which hold the evaluation.
	 */
	public EvaluationFormController(UserRequest ureq, WindowControl wControl,
			Identity evaluator, PageBody anchor, RepositoryEntry formEntry, boolean readOnly, boolean doneButton) {
		super(ureq, wControl, "run");
		this.anchor = anchor;
		this.readOnly = readOnly;
		this.evaluator = evaluator;
		this.formEntry = formEntry;
		this.doneButton = doneButton;
		
		File repositoryDir = new File(FileResourceManager.getInstance().getFileResourceRoot(formEntry.getOlatResource()), FileResourceManager.ZIPDIR);
		File formFile = new File(repositoryDir, FORM_XML_FILE);
		form = (Form)XStreamHelper.readObject(FormXStream.getXStream(), formFile);
		
		loadResponses();
		initForm(ureq);
	}
	
	public EvaluationFormSession getSession() {
		return session;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		updateElements(ureq);
		
		if(doneButton && !readOnly) {
			saveAsDoneButton = uifactory.addFormSubmitButton("save.as.done", formLayout);
		}
	}
	
	private void updateElements(UserRequest ureq) {
		List<EvaluationFormElementWrapper> elementWrappers = new ArrayList<>();
		for(AbstractElement element:form.getElements()) {
			EvaluationFormElementWrapper wrapper = forgeElement(ureq, element);
			if(wrapper != null) {
				elementWrappers.add(wrapper);
			}
		}
		elementWrapperList = elementWrappers;
		flc.contextPut("elements", elementWrappers);
	}
	
	private void loadResponses() {
		if(evaluator == null) return;

		flc.contextPut("messageNotDone", Boolean.FALSE);
		session = evaluationFormManager.getSessionForPortfolioEvaluation(evaluator, anchor);
		if(session == null) {
			session = evaluationFormManager.createSessionForPortfolioEvaluation(evaluator, anchor, formEntry);
		}
		if(session.getEvaluationFormSessionStatus() == EvaluationFormSessionStatus.done) {
			readOnly = true;
		} else if(!evaluator.equals(getIdentity())) {
			flc.contextPut("messageNotDone", Boolean.TRUE);
		}
		
		List<EvaluationFormResponse> responses = evaluationFormManager.getResponsesFromPortfolioEvaluation(evaluator, anchor);
		for(EvaluationFormResponse response:responses) {
			identifierToResponses.put(response.getResponseIdentifier(), response);
		}
	}
	
	private EvaluationFormElementWrapper forgeElement(UserRequest ureq, AbstractElement element) {
		EvaluationFormElementWrapper wrapper = null;
		
		String type = element.getType();
		switch(type) {
			case "formhtitle":
			case "formhr":
			case "formhtmlraw":
				wrapper = new EvaluationFormElementWrapper(element);
				break;
			case "formrubric":
				wrapper = forgeRubric((Rubric)element);
				break;
			case "formtextinput":
				wrapper = forgeTextInput((TextInput)element);
				break;
			case "formfileupload":
				wrapper = forgeFileUpload(ureq, (FileUpload)element);
				break;
		}
		return wrapper;
	}

	private EvaluationFormElementWrapper forgeFileUpload(UserRequest ureq, FileUpload element) {
		FileElement fileEl = uifactory.addFileElement(getWindowControl(), "file_upload_" + CodeHelper.getRAMUniqueID(), "", flc);
		fileEl.setPreview(ureq.getUserSession(), true);
		fileEl.addActionListener(FormEvent.ONCHANGE);
		fileEl.setDeleteEnabled(true);
		fileEl.setMaxUploadSizeKB(element.getMaxUploadSizeKB(), "file.upload.error.limit.exeeded", null);
		Set<String> mimeTypes = MimeTypeSetFactory.getMimeTypes(element.getMimeTypeSetKey());
		fileEl.limitToMimeType(mimeTypes, "file.upload.error.mime.type.wrong", null);
		EvaluationFormResponse response = identifierToResponses.get(element.getId());
		File responseFile = evaluationFormManager.loadResponseFile(response);
		if (responseFile != null) {
			fileEl.setInitialFile(responseFile);
		}
		fileEl.setEnabled(!readOnly);
		
		FileUploadWrapper fileUploadWrapper = new FileUploadWrapper(fileEl, element);
		fileEl.setUserObject(fileUploadWrapper);
		
		EvaluationFormElementWrapper wrapper = new EvaluationFormElementWrapper(element);
		wrapper.setFileUploadWrapper(fileUploadWrapper);
		return wrapper;
	}

	private EvaluationFormElementWrapper forgeTextInput(TextInput element) {
		String initialValue = "";
		EvaluationFormResponse response = identifierToResponses.get(element.getId());
		if(response != null && StringHelper.containsNonWhitespace(response.getStringuifiedResponse())) {
			initialValue = response.getStringuifiedResponse();
		}
		
		int rows = 12;
		if(element.getRows() > 0) {
			rows = element.getRows();
		}
		TextElement textEl = uifactory.addTextAreaElement("textinput_" + (count++), null, Integer.MAX_VALUE, rows, 72, false, initialValue, flc);
		textEl.setEnabled(!readOnly);
		FormLink saveButton = uifactory.addFormLink("save_" + (count++), "save", null, flc, Link.BUTTON);
		saveButton.setVisible(!readOnly);
		
		TextInputWrapper textInputWrapper = new TextInputWrapper(element, textEl, saveButton);
		saveButton.setUserObject(textInputWrapper);
		textEl.setUserObject(textInputWrapper);
		EvaluationFormElementWrapper wrapper = new EvaluationFormElementWrapper(element);
		wrapper.setTextInputWrapper(textInputWrapper);
		return wrapper;
	}
	
	private EvaluationFormElementWrapper forgeRubric(Rubric element) {
		EvaluationFormElementWrapper wrapper = new EvaluationFormElementWrapper(element);
		List<Slider> sliders = element.getSliders();
		List<SliderWrapper> sliderWrappers = new ArrayList<>(sliders.size());
		for(Slider slider:sliders) {
			String responseIdentifier = slider.getId();
			EvaluationFormResponse response = identifierToResponses.get(responseIdentifier);
			
			SliderType type = element.getSliderType();
			SliderWrapper sliderWrapper = null;
			if(type == SliderType.discrete) {
				sliderWrapper = forgeDiscreteRadioButtons(slider, element, response);
			} else if(type == SliderType.discrete_slider) {
				sliderWrapper = forgeDiscreteSlider(slider, element, response);
			} else if(type == SliderType.continuous) {
				sliderWrapper = forgeContinuousSlider(slider, element, response);
			}
			
			if(sliderWrapper != null) {
				sliderWrappers.add(sliderWrapper);
			}
		}
		wrapper.setSliders(sliderWrappers);
		return wrapper;
	}
	

	private SliderWrapper forgeContinuousSlider(Slider slider, Rubric element, EvaluationFormResponse response) {
		SliderElement sliderEl = uifactory.addSliderElement("slider_" + (count++), null, flc);
		sliderEl.setDomReplacementWrapperRequired(false);
		sliderEl.addActionListener(FormEvent.ONCHANGE);
		sliderEl.setEnabled(!readOnly);
		if(response != null && response.getNumericalResponse() != null) {
			double val = response.getNumericalResponse().doubleValue();
			sliderEl.setInitialValue(val);
		}
		sliderEl.setMinValue(element.getStart());
		sliderEl.setMaxValue(element.getEnd());
		SliderWrapper sliderWrapper = new SliderWrapper(slider, sliderEl);
		sliderEl.setUserObject(sliderWrapper);
		return sliderWrapper;
	}
	
	private SliderWrapper forgeDiscreteSlider(Slider slider, Rubric element, EvaluationFormResponse response) {
		SliderElement sliderEl = uifactory.addSliderElement("slider_" + (count++), null, flc);
		sliderEl.setDomReplacementWrapperRequired(false);
		sliderEl.addActionListener(FormEvent.ONCHANGE);
		sliderEl.setEnabled(!readOnly);
		if(response != null && response.getNumericalResponse() != null) {
			double val = response.getNumericalResponse().doubleValue();
			sliderEl.setInitialValue(val);
		}
		sliderEl.setMinValue(element.getStart());
		sliderEl.setMaxValue(element.getEnd());
		sliderEl.setStep(1);
		SliderWrapper sliderWrapper = new SliderWrapper(slider, sliderEl);
		sliderEl.setUserObject(sliderWrapper);
		return sliderWrapper;
	}

	private SliderWrapper forgeDiscreteRadioButtons(Slider slider, Rubric element, EvaluationFormResponse response) {
		int start = element.getStart();
		int end = element.getEnd();
		int steps = element.getSteps();
		
		double[] theSteps = new double[steps];
		String[] theKeys = new String[steps];
		String[] theValues = new String[steps];
		
		double step = (end - start + 1) / (double)steps;
		for(int i=0; i<steps; i++) {
			theSteps[i] = start + (i * step);
			theKeys[i] = Double.toString(theSteps[i]);
			theValues[i] = "";
		}

		SingleSelection radioEl = uifactory.addRadiosVertical("slider_" + (count++), null, flc, theKeys, theValues);
		radioEl.setDomReplacementWrapperRequired(false);
		radioEl.addActionListener(FormEvent.ONCHANGE);
		radioEl.setEnabled(!readOnly);
		radioEl.setAllowNoSelection(true);
		int widthInPercent = EvaluationFormElementWrapper.getWidthInPercent(element);
		radioEl.setWidthInPercent(widthInPercent, true);
		if(response != null && response.getNumericalResponse() != null) {
			double val = response.getNumericalResponse().doubleValue();
			double error = step / 10.0d;
			for(int i=0; i<theSteps.length; i++) {
				double theStep = theSteps[i];
				double margin = Math.abs(theStep - val);
				if(margin < error) {
					radioEl.select(theKeys[i], true);
				}
			}
		}
		SliderWrapper sliderWrapper = new SliderWrapper(slider, radioEl);
		radioEl.setUserObject(sliderWrapper);
		return sliderWrapper;
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//super.propagateDirtinessToContainer(fiSrc, fe);
	}

	@Override
	public boolean validate(UserRequest ureq, List<ValidationMessage> messages) {
		boolean allFiled = true;
		for(EvaluationFormElementWrapper elementWrapper:elementWrapperList) {
			if(elementWrapper.isTextInput()) {
				TextInputWrapper wrapper = elementWrapper.getTextInputWrapper();
				if(wrapper != null && !hasResponse(wrapper.getId())) {
					allFiled &= false;
				}
			} else if(elementWrapper.isFileUpload()) {
				FileUploadWrapper wrapper = elementWrapper.getFileUploadWrapper();
				if(wrapper != null && !hasResponse(wrapper.getId())) {
					allFiled &= false;
				}
			} else if(elementWrapper.getSliders() != null && elementWrapper.getSliders().size() > 0) {
				for(SliderWrapper slider:elementWrapper.getSliders()) {
					if(slider != null && !hasResponse(slider.getId())) {
						allFiled &= false;
					}
				}
			}
		}
		
		if(!allFiled) {
			String msg = translate("warning.form.not.completed");
			messages.add(new ValidationMessage(Level.warning, msg));
		}
		return validateFormLogic(ureq);
	}
	
	private boolean hasResponse(String id) {
		if(id == null) return true;//not a field
		
		if(!identifierToResponses.containsKey(id)) {
			return false;
		}
		EvaluationFormResponse response = identifierToResponses.get(id);
		if(response == null ||
				(response.getNumericalResponse() == null
					&& !StringHelper.containsNonWhitespace(response.getStringuifiedResponse())
					&& response.getFileResponse() == null)) {
			return false;
		}
		return true;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmDoneCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				saveAsDone(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doConfirmDone(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(saveAsDoneButton == source) {
			doConfirmDone(ureq);
		} else if(source instanceof SingleSelection) {
			SingleSelection radioEl = (SingleSelection)source;
			Object uobject = radioEl.getUserObject();
			if(uobject instanceof SliderWrapper) {
				String selectedKey = radioEl.getSelectedKey();
				SliderWrapper sliderWrapper = (SliderWrapper)uobject;
				saveNumericalResponse(new BigDecimal(selectedKey), selectedKey, sliderWrapper.getId());
			}
		} else if(source instanceof SliderElement) {
			SliderElement slider = (SliderElement)source;
			Object uobject = slider.getUserObject();
			if(uobject instanceof SliderWrapper) {
				double value = slider.getValue();
				SliderWrapper sliderWrapper = (SliderWrapper)uobject;
				saveNumericalResponse(BigDecimal.valueOf(value), Double.toString(value), sliderWrapper.getId());
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			Object uobject = link.getUserObject();
			if(uobject instanceof TextInputWrapper) {
				TextInputWrapper wrapper = (TextInputWrapper)uobject;
				String value = wrapper.getTextEl().getValue();
				saveNumericalResponse(null, value, wrapper.getId());
			}
		} else if (source instanceof FileElement) {
			FileElement fileElement = (FileElement)source;
			Object uobject = fileElement.getUserObject();
			if (uobject instanceof FileUploadWrapper) {
				FileUploadWrapper wrapper = (FileUploadWrapper)uobject;
				if(event instanceof FileElementEvent) {
					if(FileElementEvent.DELETE.equals(event.getCommand())) {
						saveFileResponse(null, null, wrapper.getId());
						fileElement.setInitialFile(null);
						if(fileElement.getUploadFile() != null) {
							fileElement.reset();
						}
						flc.setDirty(true);
					}
				} else if (fileElement.isUploadSuccess()) {
					File file = fileElement.getUploadFile();
					String filename = fileElement.getUploadFileName();
					saveFileResponse(file, filename, wrapper.getId());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void saveNumericalResponse(BigDecimal numericalValue, String stringuifiedReponse, String responseIdentifier) {
		if(evaluator == null || readOnly) return;
		
		EvaluationFormResponse response = identifierToResponses.get(responseIdentifier);
		if(response == null) {
			response = evaluationFormManager.createResponseForPortfolioEvaluation(responseIdentifier,
					numericalValue, stringuifiedReponse, session);
		} else {
			response = evaluationFormManager.updateResponseForPortfolioEvaluation(numericalValue, stringuifiedReponse, response);
		}
		updateCache(responseIdentifier, response);
	}
	
	private void saveFileResponse(File file, String filename, String responseIdentifier) {
		if(evaluator == null || readOnly) return;
		
		EvaluationFormResponse response = identifierToResponses.get(responseIdentifier);
		try {
			if (response == null) {
				response = evaluationFormManager.createResponseForPortfolioEvaluation(responseIdentifier, file, filename,
						session);
			} else {
				response = evaluationFormManager.updateResponseForPortfolioEvaluation(file, filename, response);
			}
		} catch (IOException e) {
			showError("error.cannot.save.file");
			log.warn("Cannot save file for an evaluation form response!", e);
		}

		updateCache(responseIdentifier, response);
	}
	
	private void updateCache(String responseIdentifier, EvaluationFormResponse response) {
		if(response != null) {
			identifierToResponses.put(responseIdentifier, response);
		}
	}
	
	private void doConfirmDone(UserRequest ureq) {
		for(EvaluationFormElementWrapper elementWrapper:elementWrapperList) {
			if(elementWrapper.isTextInput()) {
				TextInputWrapper wrapper = elementWrapper.getTextInputWrapper();
				String value = wrapper.getTextEl().getValue();
				saveNumericalResponse(null, value, wrapper.getId());
			}	
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("<p>").append(translate("confirm.done")).append("</p>");
		
		List<ValidationMessage> messages = new ArrayList<>();
		validate(ureq, messages);
		if(messages.size() > 0) {
			for(ValidationMessage message:messages) {
				sb.append("<p class='o_warning'>").append(message.getMessage()).append("</p>");
			}
		}
		confirmDoneCtrl = activateYesNoDialog(ureq, null, sb.toString(), confirmDoneCtrl);
	}
	
	private void saveAsDone(UserRequest ureq) {
		//save text inputs
		session = evaluationFormManager.changeSessionStatus(session, EvaluationFormSessionStatus.done);
		readOnly = true;
		dbInstance.commit();
		loadResponses();
		updateElements(ureq);
		if (saveAsDoneButton != null) {
			saveAsDoneButton.setVisible(false);			
		}
		dbInstance.commit();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void doDispose() {
		//
	}
}
