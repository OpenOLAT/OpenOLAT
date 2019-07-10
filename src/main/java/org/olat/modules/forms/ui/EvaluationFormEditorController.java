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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.ceditor.PageEditorProvider;
import org.olat.modules.ceditor.PageEditorSecurityCallback;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementHandler;
import org.olat.modules.ceditor.ui.FullEditorSecurityCallback;
import org.olat.modules.ceditor.ui.PageEditorController;
import org.olat.modules.forms.handler.ContainerHandler;
import org.olat.modules.forms.handler.DisclaimerHandler;
import org.olat.modules.forms.handler.FileUploadHandler;
import org.olat.modules.forms.handler.HTMLParagraphHandler;
import org.olat.modules.forms.handler.HTMLRawHandler;
import org.olat.modules.forms.handler.ImageHandler;
import org.olat.modules.forms.handler.MultipleChoiceHandler;
import org.olat.modules.forms.handler.RubricHandler;
import org.olat.modules.forms.handler.SessionInformationsHandler;
import org.olat.modules.forms.handler.SingleChoiceHandler;
import org.olat.modules.forms.handler.SpacerHandler;
import org.olat.modules.forms.handler.TableHandler;
import org.olat.modules.forms.handler.TextInputHandler;
import org.olat.modules.forms.handler.TitleHandler;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.FormXStream;

/**
 * 
 * Initial date: 6 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormEditorController extends BasicController {
	
	private VelocityContainer mainVC;
	
	private final Form form;
	private final File formFile;
	private final DataStorage storage;
	private boolean changes = false;
	private final boolean restrictedEdit;
	private final boolean restrictedEditWeight;
	
	private PageEditorController pageEditCtrl;
	
	public EvaluationFormEditorController(UserRequest ureq, WindowControl wControl, File formFile, DataStorage storage,
			boolean restrictedEdit, boolean restrictedEditWeight) {
		super(ureq, wControl);
		this.formFile = formFile;
		this.storage = storage;
		this.restrictedEdit = restrictedEdit;
		this.restrictedEditWeight = restrictedEditWeight;
		if(formFile.exists()) {
			form = (Form)XStreamHelper.readObject(FormXStream.getXStream(), formFile);
		} else {
			form = new Form();
			persistForm();
		}
		
		mainVC = createVelocityContainer("editor");
		
		PageEditorSecurityCallback secCallback = restrictedEdit ? new RestrictedEditorSecurityCallback() : new FullEditorSecurityCallback();
		pageEditCtrl = new PageEditorController(ureq, getWindowControl(), new FormPageEditorProvider(), secCallback, getTranslator());
		listenTo(pageEditCtrl);
		mainVC.put("page", pageEditCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	public boolean hasChanges() {
		return changes;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event == Event.CHANGED_EVENT) {
			persistForm();
		}
	}
	
	private void persistForm() {
		XStreamHelper.writeObject(FormXStream.getXStream(), formFile, form);
		changes = true;
	}

	private class FormPageEditorProvider implements PageEditorProvider {
		
		private final List<PageElementHandler> handlers = new ArrayList<>();
		private final List<PageElementHandler> creationHandlers = new ArrayList<>();
		
		public FormPageEditorProvider() {
			// handler for title
			TitleHandler titleRawHandler = new TitleHandler();
			handlers.add(titleRawHandler);
			// handler for HR code
			SpacerHandler hrHandler = new SpacerHandler();
			handlers.add(hrHandler);
			// handler for HTML code
			HTMLParagraphHandler htmlParagraphHandler = new HTMLParagraphHandler();
			handlers.add(htmlParagraphHandler);
			// handler for HTML code
			HTMLRawHandler htmlHandler = new HTMLRawHandler();
			handlers.add(htmlHandler);
			TableHandler tableHandler = new TableHandler();
			handlers.add(tableHandler);
			// handler media
			ImageHandler imageHandler = new ImageHandler(storage);
			handlers.add(imageHandler);
			// handler for rubric
			RubricHandler rubricHandler = new RubricHandler(restrictedEdit, restrictedEditWeight);
			handlers.add(rubricHandler);
			// handler for text input
			TextInputHandler textInputHandler = new TextInputHandler(restrictedEdit);
			handlers.add(textInputHandler);
			// handler for file upload
			FileUploadHandler fileUploadhandler = new FileUploadHandler(restrictedEdit);
			handlers.add(fileUploadhandler);
			// handler for single choice
			SingleChoiceHandler singleChoiceHandler = new SingleChoiceHandler(restrictedEdit);
			handlers.add(singleChoiceHandler);
			// handler for multiple choice
			MultipleChoiceHandler multipleChoiceHandler = new MultipleChoiceHandler(restrictedEdit);
			handlers.add(multipleChoiceHandler);
			DisclaimerHandler disclaimerHandler = new DisclaimerHandler(restrictedEdit);
			handlers.add(disclaimerHandler);
			SessionInformationsHandler sessionInformationsHandler = new SessionInformationsHandler(restrictedEdit);
			handlers.add(sessionInformationsHandler);
			ContainerHandler containerHandler = new ContainerHandler();
			handlers.add(containerHandler);

			if(!restrictedEdit) {
				creationHandlers.add(titleRawHandler);
				creationHandlers.add(htmlParagraphHandler);
				creationHandlers.add(tableHandler);
				creationHandlers.add(imageHandler);
				creationHandlers.add(rubricHandler);
				creationHandlers.add(singleChoiceHandler);
				creationHandlers.add(multipleChoiceHandler);
				creationHandlers.add(textInputHandler);
				creationHandlers.add(fileUploadhandler);
				creationHandlers.add(sessionInformationsHandler);
				creationHandlers.add(disclaimerHandler);
				creationHandlers.add(containerHandler);
				creationHandlers.add(hrHandler);
				creationHandlers.add(htmlHandler); // legacy
			}
		}

		@Override
		public List<? extends PageElement> getElements() {
			return form.getElements();
		}

		@Override
		public List<PageElementHandler> getCreateHandlers() {
			return creationHandlers;
		}

		@Override
		public List<PageElementHandler> getAvailableHandlers() {
			return handlers;
		}

		@Override
		public PageElement appendPageElement(PageElement element) {
			if(element instanceof AbstractElement) {
				form.addElement((AbstractElement)element);
				persistForm();
			}
			return element;
		}

		@Override
		public PageElement appendPageElementAt(PageElement element, int index) {
			if(element instanceof AbstractElement) {
				form.addElement((AbstractElement)element, index);
				persistForm();
			}
			return element;
		}

		@Override
		public void removePageElement(PageElement element) {
			if(element instanceof AbstractElement) {
				form.removeElement((AbstractElement)element);
				persistForm();
			}
		}

		@Override
		public void moveUpPageElement(PageElement element) {
			if(element instanceof AbstractElement) {
				form.moveUpElement((AbstractElement)element);
				persistForm();
			}
		}

		@Override
		public void moveDownPageElement(PageElement element) {
			if(element instanceof AbstractElement) {
				form.moveDownElement((AbstractElement)element);
				persistForm();
			}
		}

		@Override
		public void movePageElement(PageElement elementToMove, PageElement sibling, boolean after) {
			if(elementToMove instanceof AbstractElement && (sibling == null || sibling instanceof AbstractElement)) {
				form.moveElement((AbstractElement)elementToMove, (AbstractElement)sibling, after);
				persistForm();
			}
		}
	}
}
