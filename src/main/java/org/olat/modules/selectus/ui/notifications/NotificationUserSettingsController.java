/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.notifications;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.AuditService.NotificationIntervals;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.RecruitingAuditLogUserSettings;

/**
 * 
 * Initial date: 23 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NotificationUserSettingsController extends FormBasicController {

	private static final String[] enableKeys = new String[] { "enable", "disable" };
	
	private SingleSelection enableEl;
	private SingleSelection scheduleEl;
	
	private RecruitingAuditLogUserSettings settings;
	
	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingModule recruitingModule;
	
	public NotificationUserSettingsController(UserRequest ureq, WindowControl wControl, Identity settingsIdentity) {
		super(ureq, wControl);
		settings = auditService.getOrCreateRecruitingAuditLogUserSettings(settingsIdentity);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("notification.settings.descr");
		
		String[] enableValues = new String[] { translate("notification.enabled"), translate("notification.disabled") };
		enableEl = uifactory.addRadiosVertical("not.email", "notifications.email.settings", formLayout, enableKeys, enableValues);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		if(settings.isEnabled()) {
			enableEl.select(enableKeys[0], true);
		} else {
			enableEl.select(enableKeys[1], true);
		}
		
		NotificationIntervals[] intervals = recruitingModule.getNotificationIntervals();
		List<String> intervalKeys = new ArrayList<>(intervals.length);
		List<String> intervalValues = new ArrayList<>(intervals.length);
		for(NotificationIntervals interval:intervals) {
			intervalKeys.add(interval.name());
			intervalValues.add(translate(interval.name()));
		}
		scheduleEl = uifactory.addDropdownSingleselect("not.schedule", "notifications.schedule.settings", formLayout,
				intervalKeys.toArray(new String[intervalKeys.size()]), intervalValues.toArray(new String[intervalValues.size()]), null);
		for(String intervalKey:intervalKeys) {
			if(intervalKey.equals(settings.getInterval())) {
				scheduleEl.select(intervalKey, true);
			}
		}
		scheduleEl.setVisible(enableEl.isSelected(0));

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		enableEl.clearError();
		if(!enableEl.isOneSelected()) {
			enableEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		scheduleEl.clearError();
		if(!scheduleEl.isOneSelected()) {
			scheduleEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source) {
			scheduleEl.setVisible(enableEl.isSelected(0));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		settings.setEnabled(enableEl.isSelected(0));
		if(settings.isEnabled()) {
			settings.setInterval(scheduleEl.getSelectedKey());
		} else {
			settings.setInterval(NotificationIntervals.never.name());
		}
		settings = auditService.updateRecruitingAuditLogUserSettings(settings);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
