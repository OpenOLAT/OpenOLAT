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
import org.olat.modules.project.ProjMilestone;

/**
 * 
 * Initial date: 9 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjMilestoneEditController extends FormBasicController {

	private ProjMilestoneContentEditController contentCtrl;
	private ProjArtefactMetadataController metadataCtrl;

	private final ProjMilestone milestone;
	private final boolean firstEdit;
	private Boolean metadataOpen = Boolean.FALSE;

	public ProjMilestoneEditController(UserRequest ureq, WindowControl wControl, ProjMilestone milestone,
			boolean firstEdit) {
		super(ureq, wControl, "edit");
		this.milestone = milestone;
		this.firstEdit = firstEdit;
		
		initForm(ureq);
	}

	public ProjMilestone getMilestone() {
		return milestone;
	}

	public boolean isFirstEdit() {
		return firstEdit;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		contentCtrl = new ProjMilestoneContentEditController(ureq, getWindowControl(), mainForm, milestone);
		listenTo(contentCtrl);
		formLayout.add("content", contentCtrl.getInitialFormItem());
		
		if (!firstEdit) {
			metadataCtrl = new ProjArtefactMetadataController(ureq, getWindowControl(), mainForm, milestone.getArtefact());
			listenTo(metadataCtrl);
			formLayout.add("metadata", metadataCtrl.getInitialFormItem());
			flc.contextPut("metadataOpen", metadataOpen);
		}
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		if (firstEdit) {
			uifactory.addFormCancelButton("remove", buttonLayout, ureq, getWindowControl());
		} else {
			uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if ("ONCLICK".equals(event.getCommand())) {
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
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}

}
