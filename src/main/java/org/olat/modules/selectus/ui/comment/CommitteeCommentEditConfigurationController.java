/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.comment;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormCancel;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.events.NewPositionSavedEvent;
import org.olat.modules.selectus.ui.position.PositionEditableController;

/**
 * 
 * Initial date: 3 juil. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CommitteeCommentEditConfigurationController extends FormBasicController implements PositionEditableController {

	private static String[] onKeys = new String[] { "on" };
	private static String[] yesNoKeys = new String[] { "yes", "no" };

	private MultipleSelectionElement enableEl;
	private SingleSelection headVisibilityEl;
	private SingleSelection secretaryVisibilityEl;
	private SingleSelection committeeVisibilityEl;
	private SingleSelection exofficioVisibilityEl;
	
	private Position position;
	private final boolean readOnly;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public CommitteeCommentEditConfigurationController(UserRequest ureq, WindowControl wControl, Position position, boolean readOnly) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.readOnly = readOnly;
		
		initForm(ureq);
		updateUI();
	}
	
	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public void updatePosition(Position updatedPosition) {
		this.position = updatedPosition;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("committee.comment.configuration");
		setFormDescription("committee.comment.configuration.explanation");

		String[] onValues = new String[] { translate("on") };
		enableEl = uifactory.addCheckboxesHorizontal("enable.committee.comment", formLayout, onKeys, onValues);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		enableEl.setEnabled(!readOnly);
		if(position != null && position.isCommitteeCommentEnabled()) {
			enableEl.select("on", true);
		}
		
		PositionRole[] visibility = position.getCommitteeCommentVisiblity();
		
		String[] yesNoValues = new String[] { translate("visibility.yes"), translate("visibility.no") };
		headVisibilityEl = uifactory.addRadiosVertical("hvisiblity", "visibility.comment.head", formLayout, yesNoKeys, yesNoValues);
		headVisibilityEl.setEnabled(!readOnly);
		setVisible(headVisibilityEl, visibility, PositionRole.head);
		secretaryVisibilityEl = uifactory.addRadiosVertical("secvisiblity", "visibility.comment.secretary", formLayout, yesNoKeys, yesNoValues);
		secretaryVisibilityEl.setEnabled(!readOnly);
		setVisible(secretaryVisibilityEl, visibility, PositionRole.secretary);
		committeeVisibilityEl = uifactory.addRadiosVertical("comvisiblity", "visibility.comment.committee", formLayout, yesNoKeys, yesNoValues);
		committeeVisibilityEl.setEnabled(!readOnly);
		setVisible(committeeVisibilityEl, visibility, PositionRole.member);
		exofficioVisibilityEl = uifactory.addRadiosVertical("exovisiblity", "visibility.comment.exofficio", formLayout, yesNoKeys, yesNoValues);
		exofficioVisibilityEl.setEnabled(!readOnly);
		setVisible(exofficioVisibilityEl, visibility, PositionRole.exofficio);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		FormSubmit submitButton = uifactory.addFormSubmitButton("save", buttonsCont);
		submitButton.setVisible(!readOnly);
		FormCancel cancelButton = uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		cancelButton.setVisible(!readOnly);
	}
	
	private void updateUI() {
		boolean featureEnabled = enableEl.isAtLeastSelected(1);
		headVisibilityEl.setVisible(featureEnabled);
		secretaryVisibilityEl.setVisible(featureEnabled);
		committeeVisibilityEl.setVisible(featureEnabled);
		exofficioVisibilityEl.setVisible(featureEnabled && recruitingModule.isRoleExOfficioEnabled());
	}
	
	private void setVisible(SingleSelection visibilityEl, PositionRole[] visibility, PositionRole forRole) {
		boolean yes = false;
		if(visibility != null && visibility.length > 0) {
			for(PositionRole role:visibility) {
				if(role != null && role == forRole) {
					yes = true;
				}
			}
		}
		
		if(yes) {
			visibilityEl.select(yesNoKeys[0], true);
		} else {
			visibilityEl.select(yesNoKeys[1], true);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Event doneEvent = Event.DONE_EVENT;
		if(position.getKey() != null) {
			position = recruitingService.getPosition(position.getKey());
		} else {
			doneEvent = new NewPositionSavedEvent();
		}
		
		String before = auditService.toAuditXml(position);
		
		position.setCommitteeCommentEnabled(enableEl.isAtLeastSelected(1));
		List<PositionRole> visibilityList = new ArrayList<>();
		if(headVisibilityEl.isVisible() && headVisibilityEl.isSelected(0)) {
			visibilityList.add(PositionRole.head);
		}
		if(secretaryVisibilityEl.isVisible() && secretaryVisibilityEl.isSelected(0)) {
			visibilityList.add(PositionRole.secretary);
		}
		if(committeeVisibilityEl.isVisible() && committeeVisibilityEl.isSelected(0)) {
			visibilityList.add(PositionRole.member);
		}
		if(exofficioVisibilityEl.isVisible() && exofficioVisibilityEl.isSelected(0)) {
			visibilityList.add(PositionRole.exofficio);
		}
		position.setCommitteeCommentVisiblity(visibilityList.toArray(new PositionRole[visibilityList.size()]));
		position = recruitingService.savePosition(position);
		dbInstance.commit();

		String after = auditService.toAuditXml(position);
		if(!before.equals(after)) {
			String messageI18n = "audit.log.position.change.configuration";
			String[] messageArgs = new String[] { position.getMLTitle(recruitingModule.getPositionDefaultLocale()) };
			auditService.auditPositionLog(Action.changeConfiguration, ActionTarget.position, before, after,
					messageI18n, messageArgs, getTranslator(), position, getIdentity());
		}
		
		fireEvent(ureq, doneEvent);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
