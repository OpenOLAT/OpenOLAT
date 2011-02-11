/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2009 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
 */
package de.bps.course.nodes.den;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

public class DENManageParticipantsForm extends FormBasicController {
	
	public static final Event ADD_PARTICIPANTS = new Event("addParticipants");
	
	private FormLinkImpl addParticipant;

	public DENManageParticipantsForm(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(this.flc, this, ureq);
	}

	@Override
	protected void doDispose() {
		//nothing
	}

	@Override
	protected void formOK(UserRequest arg0) {
		//nothing to do
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		addParticipant = new FormLinkImpl("addParticipantsButton", ADD_PARTICIPANTS.getCommand(), "participants.add", Link.BUTTON);
		formLayout.add(addParticipant);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == addParticipant) {
			fireEvent(ureq, ADD_PARTICIPANTS);
		}
	}

}
