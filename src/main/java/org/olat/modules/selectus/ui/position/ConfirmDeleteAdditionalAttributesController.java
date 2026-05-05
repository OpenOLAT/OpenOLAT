/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.position;

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
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionAttributeDefinition;

/**
 * 
 * Initial date: 12 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmDeleteAdditionalAttributesController extends FormBasicController {
	
	private FormLink deleteButton;
	
	private Position position;
	private final PositionAttributeDefinition attributeDefinition;
	
	@Autowired
	private RecruitingService recruitingService;
	
	public ConfirmDeleteAdditionalAttributesController(UserRequest ureq, WindowControl wControl,
			Position position, PositionAttributeDefinition attributeDefinition) {
		super(ureq, wControl, "confirm_delete_attribute");
		this.position = position;
		this.attributeDefinition = attributeDefinition;
		initForm(ureq);
	}
	
	public PositionAttributeDefinition getDefinition() {
		return attributeDefinition;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		long usage = attributeDefinition.getKey() == null ? 0 : recruitingService.getAttributeUsage(position, attributeDefinition);
		if(usage == 0) {
			String msg = translate("confirm.delete.attr.text", new String[] { attributeDefinition.getLabel(getLocale(), true) });
			formLayout.contextPut("msg", msg);
		} else {
			String warningMsg = translate("confirm.delete.attr.used.text", new String[] {
					attributeDefinition.getLabel(getLocale(), true), Long.toString(usage) });
			formLayout.contextPut("warningMsg", warningMsg);
		}
		
		deleteButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(deleteButton == source) {
			doDelete(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doDelete(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
