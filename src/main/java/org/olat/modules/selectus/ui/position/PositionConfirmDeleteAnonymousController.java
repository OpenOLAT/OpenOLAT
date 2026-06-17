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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.DB;
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
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.app_wizard.ApplicationAttributesDelegate;

/**
 * 
 * Initial date: 2 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionConfirmDeleteAnonymousController extends FormBasicController {
	
	private FormLink deleteButton;
	private MultipleSelectionElement confirmEl;
	
	private Position position;
	private List<FormItem> additionalAttributesEl = new ArrayList<>();
	private final boolean globalAttributes;
	private final SelectionValues confirmPK = new SelectionValues();
	private final ApplicationAttributesDelegate attributesDelegate
		= new ApplicationAttributesDelegate(PositionApplicationAttributeTabEnum.global);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public PositionConfirmDeleteAnonymousController(UserRequest ureq, WindowControl wControl, Position position) {
		super(ureq, wControl, "confirm_delete_anonymous", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		globalAttributes = recruitingModule.isReportingEnabled() && attributesDelegate.hasSomeGlobalAttributes();
		initForm(ureq);
	}
	
	public Position getPosition() {
		return position;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_confirm_delete_position_anonymous");
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String shortTitle = position.getMLShortTitle(getLocale());
			
			String text;
			if(globalAttributes) {
				text = translate("confirm.delete.anonymous", new String[]{ shortTitle });
				layoutCont.contextPut("messageAdd", translate("confirm.delete.anonymous.2"));
			} else {
				text = translate("confirm.delete.anonymous.wo.attributes", new String[]{ shortTitle });
			}
			layoutCont.contextPut("message", text);
		}
		
		
		FormLayoutContainer layoutCont = FormLayoutContainer.createDefaultFormLayout("custom.attributes", getTranslator());
		layoutCont.setRootForm(mainForm);
		formLayout.add("custom.attributes", layoutCont);
		
		if(globalAttributes) {
			attributesDelegate.initGlobalAdditionalAttributes(layoutCont, additionalAttributesEl, position, true, true, getLocale());
			uifactory.addSpacerElement("global-spacer", layoutCont, false);
		}
		
		if(globalAttributes) {
			confirmPK.add(SelectionValues.entry("on1", translate("confirm.delete.anonymous.option.1")));
			confirmPK.add(SelectionValues.entry("on2", translate("confirm.delete.anonymous.option.2")));
		} else {
			confirmPK.add(SelectionValues.entry("on1", translate("confirm.delete.anonymous.wo.attributes.option")));
		}
		confirmEl = uifactory.addCheckboxesVertical("confirm.delete", "confirm.delete.check", formLayout, confirmPK.keys(), confirmPK.values(), 1);
		
		deleteButton = uifactory.addFormLink("delete", "delete.position.anonymous.short", null, formLayout, Link.BUTTON);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		confirmEl.clearError();
		if(!confirmEl.isAtLeastSelected(Math.max(1, confirmPK.size()))) {
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
				doAnonymousPosition(ureq);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doAnonymousPosition(UserRequest ureq) {
		position = recruitingService.getPosition(position.getKey());
		if(globalAttributes) {
			attributesDelegate.commitChanges(additionalAttributesEl, position);
		}
		position = recruitingService.savePosition(position);
		recruitingService.toReportPositionOnly(position, getIdentity());
		dbInstance.commit();
		getLogger().info(Tracing.M_AUDIT, "To report only position: {}", position.getKey());
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
