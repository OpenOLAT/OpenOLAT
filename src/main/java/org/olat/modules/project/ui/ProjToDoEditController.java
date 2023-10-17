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
package org.olat.modules.project.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjToDo;

/**
 * 
 * Initial date: 26. Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjToDoEditController extends FormBasicController {

	private ProjToDoContentEditController contentCtrl;
	private ProjArtefactReferencesController referenceCtrl;
	private ProjArtefactMetadataController metadataCtrl;

	private final ProjProject project;
	private final ProjToDo toDo;
	private final boolean withOpenInSameWindow;
	private final boolean showContext;
	private Boolean referenceOpen = Boolean.FALSE;
	private Boolean metadataOpen = Boolean.FALSE;
	
	public ProjToDoEditController(UserRequest ureq, WindowControl wControl, ProjProject project, boolean withOpenInSameWindow) {
		super(ureq, wControl, "edit");
		this.project = project;
		this.toDo = null;
		this.withOpenInSameWindow = withOpenInSameWindow;
		this.showContext = false;
		
		initForm(ureq);
	}

	public ProjToDoEditController(UserRequest ureq, WindowControl wControl, ProjToDo toDo, boolean withOpenInSameWindow, boolean showContext) {
		super(ureq, wControl, "edit");
		this.project = toDo.getArtefact().getProject();
		this.toDo = toDo;
		this.withOpenInSameWindow = withOpenInSameWindow;
		this.showContext = showContext;
		
		initForm(ureq);
	}

	public ProjToDo getToDo() {
		return contentCtrl.getToDo();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		contentCtrl = new ProjToDoContentEditController(ureq, getWindowControl(), mainForm, project, toDo, showContext);
		listenTo(contentCtrl);
		formLayout.add("content", contentCtrl.getInitialFormItem());
		
		ProjArtefact artefact = toDo != null? toDo.getArtefact(): null;
		referenceCtrl = new ProjArtefactReferencesController(ureq, getWindowControl(), mainForm, project, artefact,
				false, false, withOpenInSameWindow);
		listenTo(referenceCtrl);
		formLayout.add("reference", referenceCtrl.getInitialFormItem());
		flc.contextPut("referenceOpen", referenceOpen);
		
		if (artefact != null) {
			metadataCtrl = new ProjArtefactMetadataController(ureq, getWindowControl(), mainForm, artefact);
			listenTo(metadataCtrl);
			formLayout.add("metadata", metadataCtrl.getInitialFormItem());
			flc.contextPut("metadataOpen", metadataOpen);
		}
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		if (artefact != null) {
			uifactory.addFormSubmitButton("save", buttonLayout);
		} else {
			uifactory.addFormSubmitButton("create", buttonLayout);
		}
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == referenceCtrl) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if ("ONCLICK".equals(event.getCommand())) {
			String referenceOpenVal = ureq.getParameter("referenceOpen");
			if (StringHelper.containsNonWhitespace(referenceOpenVal)) {
				referenceOpen = Boolean.valueOf(referenceOpenVal);
				flc.contextPut("referenceOpen", referenceOpen);
			}
			String metadataOpenVal = ureq.getParameter("metadataOpen");
			if (StringHelper.containsNonWhitespace(metadataOpenVal)) {
				metadataOpen = Boolean.valueOf(metadataOpenVal);
				flc.contextPut("metadataOpen", metadataOpen);
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, FormEvent.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		contentCtrl.formOK(ureq);
		ProjToDo toDo = contentCtrl.getToDo();
		referenceCtrl.save(toDo.getArtefact());
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}

}
