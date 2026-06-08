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
package org.olat.modules.selectus.ui.position;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.PositionController;

/**
 * 
 * Initial date: 2 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionConfirmDeletePermanentlyController extends FormBasicController {
	
	private FormLink deleteButton;
	private MultipleSelectionElement confirmEl;
	
	private Position position;
	
	@Autowired
	private RecruitingService recruitingService;
	
	public PositionConfirmDeletePermanentlyController(UserRequest ureq, WindowControl wControl, Position position) {
		super(ureq, wControl, "confirm_delete_permanently", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		initForm(ureq);
	}
	
	public Position getPosition() {
		return position;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_confirm_delete_position_permanently");
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String shortTitle = position.getMLShortTitle(getLocale());
			String text = translate("confirm.delete.permanently", new String[]{ shortTitle });
			layoutCont.contextPut("message", text);
		}
		
		SelectionValues confirmPK = new SelectionValues();
		confirmPK.add(SelectionValues.entry("on", translate("confirm.delete.check.option")));
		confirmEl = uifactory.addCheckboxesVertical("confirm.delete", "confirm.delete.check", formLayout, confirmPK.keys(), confirmPK.values(), 1);
		confirmEl.setElementCssClass("o_sel_confirm");
		
		deleteButton = uifactory.addFormLink("delete", "delete.position.permanently.short", null, formLayout, Link.BUTTON);
		deleteButton.setElementCssClass("o_sel_delete_permanently");
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		confirmEl.clearError();
		if(!confirmEl.isAtLeastSelected(1)) {
			confirmEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == deleteButton) {
			if(validateFormLogic(ureq)) {
				doDeletePermanently();
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doDeletePermanently() {
		position = recruitingService.getPosition(position.getKey());
		recruitingService.deletePosition(position, getIdentity());
		logAudit("Position deleted: " + position.toStringFull(), null);
	}

}
