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
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.project.ProjNoteInfo;

/**
 * 
 * Initial date: 12 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjNoteViewController extends FormBasicController {
	
	private ProjNoteContentViewController contentCtrl;
	private ProjArtefactReferencesController referenceCtrl;
	private ProjMembersAvatarController memberCtrl;
	private ProjNoteMetadataController metadataCtrl;

	private final ProjNoteInfo noteInfo;
	private final boolean withOpenInSameWindow;
	private Boolean referenceOpen = Boolean.FALSE;
	private Boolean memberOpen = Boolean.FALSE;
	private Boolean metadataOpen = Boolean.FALSE;

	public ProjNoteViewController(UserRequest ureq, WindowControl wControl, ProjNoteInfo noteInfo, boolean withOpenInSameWindow) {
		super(ureq, wControl, "edit");
		this.noteInfo = noteInfo;
		this.withOpenInSameWindow = withOpenInSameWindow;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		contentCtrl = new ProjNoteContentViewController(ureq, getWindowControl(), noteInfo.getNote());
		listenTo(contentCtrl);
		formLayout.add("content", new ComponentWrapperElement("contentView", contentCtrl.getInitialComponent()));
		
		referenceCtrl = new ProjArtefactReferencesController(ureq, getWindowControl(), mainForm,
				noteInfo.getNote().getArtefact(), withOpenInSameWindow);
		listenTo(referenceCtrl);
		formLayout.add("reference", referenceCtrl.getInitialFormItem());
		flc.contextPut("referenceOpen", referenceOpen);
		
		memberCtrl = new ProjMembersAvatarController(ureq, getWindowControl(), mainForm, noteInfo.getMembers());
		listenTo(memberCtrl);
		formLayout.add("member", memberCtrl.getInitialFormItem());
		flc.contextPut("memberOpen", memberOpen);
		
		metadataCtrl = new ProjNoteMetadataController(ureq, getWindowControl(), mainForm, noteInfo.getNote());
		listenTo(metadataCtrl);
		formLayout.add("metadata", metadataCtrl.getInitialFormItem());
		flc.contextPut("metadataOpen", metadataOpen);
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
			String memberOpenVal = ureq.getParameter("memberOpen");
			if (StringHelper.containsNonWhitespace(memberOpenVal)) {
				memberOpen = Boolean.valueOf(memberOpenVal);
				flc.contextPut("memberOpen", memberOpen);
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
	protected void formOK(UserRequest ureq) {
		//
	}

}
