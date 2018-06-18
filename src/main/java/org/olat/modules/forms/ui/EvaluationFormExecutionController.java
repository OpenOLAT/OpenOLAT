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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
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
import org.olat.core.util.xml.XStreamHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.handler.AllHandlerPageProvider;
import org.olat.modules.forms.handler.EvaluationFormElementHandler;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.FormXStream;
import org.olat.modules.forms.ui.model.EvaluationFormExecutionElement;
import org.olat.modules.portfolio.ui.editor.ValidatingController;
import org.olat.modules.portfolio.ui.editor.ValidationMessage;
import org.olat.modules.portfolio.ui.editor.ValidationMessage.Level;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormExecutionController extends FormBasicController implements ValidatingController {

	private static final OLog log = Tracing.createLoggerFor(EvaluationFormExecutionController.class);
	
	private final Map<String, EvaluationFormElementHandler> handlerMap = new HashMap<>();
	private final List<ExecutionFragment> fragments = new ArrayList<>();
	private FormLink saveLink;
	private FormLink doneLink;
	private DialogBoxController confirmDoneCtrl;
	
	private final Form form;
	private boolean readOnly;
	private boolean showDoneButton;
	
	private boolean immediateSave = false;
	
	private EvaluationFormSession session;
	private final EvaluationFormResponses responses;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	
	public EvaluationFormExecutionController(UserRequest ureq, WindowControl wControl, EvaluationFormSession session) {
		this(ureq, wControl, null, session, null, false, true);
	}
	
	/**
	 * Optimized to use already loaded responses and form.
	 * 
	 */
	public EvaluationFormExecutionController(UserRequest ureq, WindowControl wControl, EvaluationFormSession session,
			EvaluationFormResponses responses, Form form) {
		this(ureq, wControl, form, session, responses, false, true);
	}
	
	public EvaluationFormExecutionController(UserRequest ureq, WindowControl wControl, EvaluationFormSession session,
			boolean readOnly, boolean showDoneButton) {
		this(ureq, wControl, null, session, null, readOnly, showDoneButton);
	}
	
	private EvaluationFormExecutionController(UserRequest ureq, WindowControl wControl, Form form,
			EvaluationFormSession session, EvaluationFormResponses responses, boolean readOnly,
			boolean showDoneButton) {
		super(ureq, wControl, "execute");
		
		this.session = session;
		this.readOnly = readOnly;
		this.showDoneButton = showDoneButton;
		
		if (form != null) {
			this.form = form;
		} else {
			RepositoryEntry formEntry = session.getSurvey().getFormEntry();
			File repositoryDir = new File(
					FileResourceManager.getInstance().getFileResourceRoot(formEntry.getOlatResource()),
					FileResourceManager.ZIPDIR);
			File formFile = new File(repositoryDir, FORM_XML_FILE);
			this.form = (Form)XStreamHelper.readObject(FormXStream.getXStream(), formFile);
		}
		
		if (responses != null) {
			this.responses = responses;
		} else {
			this.responses = evaluationFormManager.loadResponsesBySessions(Collections.singletonList(session));
		}
		
		initForm(ureq);
	}
	
	public EvaluationFormExecutionController(UserRequest ureq, WindowControl wControl, File formFile) {
		super(ureq, wControl, "execute");

		this.form = (Form)XStreamHelper.readObject(FormXStream.getXStream(), formFile);
		
		this.session = null;
		this.responses = null;
		this.readOnly = false;
		this.showDoneButton = false;
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		AllHandlerPageProvider provider = new AllHandlerPageProvider(form);
		for(EvaluationFormElementHandler handler: provider.getAvailableHandlers()) {
			handlerMap.put(handler.getType(), handler);
		}
		
		ajustFromSession();
		loadElements(ureq);
		loadResponses();
		propagateReadOnly();
		
		boolean notAnonymous = session != null
				&& session.getParticipation() != null
				&& !session.getParticipation().isAnonymous();
		boolean anonymous = !notAnonymous;
		flc.contextPut("anonymous", Boolean.valueOf(showDoneButton && anonymous));

		saveLink = uifactory.addFormLink("save.intermediate", "save.intermediate", null, flc, Link.BUTTON);
		doneLink = uifactory.addFormLink("save.as.done", "save.as.done", null, flc, Link.BUTTON);
		showHideButtons();
	}
	
	private void ajustFromSession() {
		if (session == null) return;
		
		if(session.getEvaluationFormSessionStatus() == EvaluationFormSessionStatus.done) {
			readOnly = true;
			showDoneButton = false;
		} else {
			Identity executor = null;
			if (session.getParticipation() != null) {
				executor = session.getParticipation().getExecutor();
			}
			if (executor != null && !executor.equals(getIdentity())) {
				flc.contextPut("messageNotDone", Boolean.TRUE);
			}
		}
		
	}

	private void loadElements(UserRequest ureq) {
		fragments.clear();
		List<AbstractElement> elements = form.getElements();
		if (elements.isEmpty()) {
			flc.contextPut("messageWithoutElements", Boolean.TRUE);
		}
		for(AbstractElement element: elements) {
			EvaluationFormElementHandler handler = handlerMap.get(element.getType());
			if(handler != null) {
				EvaluationFormExecutionElement executionElement = handler.getExecutionElement(ureq, getWindowControl(), this.mainForm, element);
				String cmpId = "cpt-" + CodeHelper.getRAMUniqueID();
				fragments.add(new ExecutionFragment(handler.getType(), cmpId, executionElement));
				if (executionElement.hasFormItem()) {
					flc.add(cmpId, executionElement.getFormItem());
				} else {
					flc.put(cmpId, executionElement.getComponent());
				}
			}
		}
		flc.contextPut("fragments", fragments);
	}
	
	private void loadResponses() {
		if (session == null) return;
		
		for (ExecutionFragment fragment: fragments) {
			fragment.initResponse(session, responses);
		}
	}
	
	private void propagateReadOnly() {
		for (ExecutionFragment fragment: fragments) {
			fragment.setReadOnly(readOnly);
		}
	}

	private void showHideButtons() {
		saveLink.setVisible(!readOnly && session != null);
		doneLink.setVisible(showDoneButton);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (saveLink == source) {
			immediateSave = true;
			mainForm.submit(ureq);
		} else if (doneLink == source) {
			immediateSave = false;
			mainForm.submit(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (confirmDoneCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				saveAsDone(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean responsesSaved = doSaveResponses();
		if (!immediateSave && responsesSaved) {
			doConfirmDone(ureq);
		}
	}

	@Override
	public boolean validate(UserRequest ureq, List<ValidationMessage> messages) {
		areAllResponded(messages);
		for (ExecutionFragment fragment: fragments) {
			fragment.validate(ureq, messages);
		}
		return messages.size() == 0;
	}

	private void areAllResponded(List<ValidationMessage> messages) {
		boolean allResponded = true;
		for (ExecutionFragment fragment: fragments) {
			if (!fragment.hasResponse()) {
				allResponded &= false;
			}
		}
		if (!allResponded) {
			String msg = translate("warning.form.not.completed");
			messages.add(new ValidationMessage(Level.warning, msg));
		}
	}

	private boolean doSaveResponses() {
		boolean allSaved = true;
		for (ExecutionFragment fragment: fragments) {
			try {
				fragment.save(session);
			} catch (Exception e) {
				log.error("Saving evaluation form response failed!", e);
				allSaved = false;
			}
		}
		dbInstance.commit();
		if (!allSaved) {
			showError("error.cannot.save");	
		}
		return allSaved;
	}

	private void doConfirmDone(UserRequest ureq) {
		StringBuilder sb = new StringBuilder();
		sb.append("<p>").append(translate("confirm.done")).append("</p>");

		List<ValidationMessage> messages = new ArrayList<>();
		validate(ureq, messages);
		if (messages.size() > 0) {
			for (ValidationMessage message : messages) {
				sb.append("<p class='o_warning'>").append(message.getMessage()).append("</p>");
			}
		}
		confirmDoneCtrl = activateYesNoDialog(ureq, null, sb.toString(), confirmDoneCtrl);
	}

	private void saveAsDone(UserRequest ureq) {
		session = evaluationFormManager.finishSession(session);
		dbInstance.commit();
		readOnly = true;
		propagateReadOnly();
		showDoneButton = false;
		showHideButtons();
		flc.setDirty(true);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	public static final class ExecutionFragment {

		private final String type;
		private final String componentName;
		private final EvaluationFormExecutionElement executionElement;
		
		public ExecutionFragment(String type, String componentName, EvaluationFormExecutionElement executionElement) {
			this.type = type;
			this.componentName = componentName;
			this.executionElement = executionElement;
		}
		
		public String getCssClass() {
			return "o_ed_".concat(type);
		}
		
		public String getComponentName() {
			return componentName;
		}
		
		public boolean validate(UserRequest ureq, List<ValidationMessage> messages) {
			return executionElement.validate(ureq, messages);
		}
		
		public void setReadOnly(boolean readOnly) {
			executionElement.setReadOnly(readOnly);
		}
		
		public boolean hasResponse() {
			return executionElement.hasResponse();
		}
		
		public void initResponse(EvaluationFormSession session, EvaluationFormResponses responses){
			executionElement.initResponse(session, responses);;
		}
		
		public void save(EvaluationFormSession session) {
			executionElement.saveResponse(session);
		}
	}
}
