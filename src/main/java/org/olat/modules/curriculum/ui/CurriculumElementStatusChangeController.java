/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.curriculum.ui;

import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.member.CustomizeNotificationController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Dec 13, 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementStatusChangeController extends FormBasicController {
	
	private final static String CHANGE_FOR_ALL = "all";
	private final static String CHANGE_FOR_CURRENT = "current";

	private SingleSelection changeForEl;
	private FormLink applyCustomNotificationButton;
	private FormLink applyWithoutNotificationButton;
	
	private CloseableModalController cmc;
	private CustomizeNotificationController customizeNotificationsCtrl;
	
	private CurriculumElement curriculumElement;
	private final CurriculumElementStatus newStatus;
	private final CurriculumSecurityCallback secCallback;
	
	@Autowired
	private CurriculumService curriculumService;
	
	public CurriculumElementStatusChangeController(UserRequest ureq, WindowControl wControl,
			CurriculumElement curriculumElement, CurriculumElementStatus newStatus, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.curriculumElement = curriculumElement;
		this.secCallback = secCallback;
		this.newStatus = newStatus;
		initForm(ureq);
	}

	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		updateWarning();
		formLayout.setElementCssClass("o_curriculum_element_change_status_form");
		
		FormLayoutContainer statusCont = FormLayoutContainer.createCustomFormLayout("status", getTranslator(), velocity_root + "/status_change_status.html");
		statusCont.setRootForm(mainForm);
		formLayout.add(statusCont);
		
		statusCont.contextPut("statusOld", curriculumElement.getElementStatus());
		statusCont.contextPut("statusNew", newStatus);
		
		if (!curriculumElement.getElementStatus().isCancelledOrClosed()) {
			long numChildren = numOfEditableChildren();
			if (numChildren > 0) {
				SelectionValues changeForSV = new SelectionValues();
				String allI18nKey;
				if (CurriculumElementStatus.active == newStatus &&
						(CurriculumElementStatus.provisional == curriculumElement.getElementStatus()
							|| CurriculumElementStatus.confirmed == curriculumElement.getElementStatus())
						) {
					allI18nKey = "change.status.for.all.active";
				} else {
					allI18nKey = "change.status.for.all";
				}
				changeForSV.add(SelectionValues.entry(CHANGE_FOR_ALL, translate(allI18nKey,
						StringHelper.escapeHtml(curriculumElement.getDisplayName()), String.valueOf(numChildren))));
				changeForSV.add(SelectionValues.entry(CHANGE_FOR_CURRENT, translate("change.status.for.current")));
				
				changeForEl = uifactory.addRadiosVertical("change.status.for", formLayout, changeForSV.keys(), changeForSV.values());
				changeForEl.select(CHANGE_FOR_ALL, true);
			}
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createCustomFormLayout("buttons", getTranslator(), velocity_root + "/status_change_buttons.html");
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		
		uifactory.addFormSubmitButton("apply", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		
		if (CurriculumElementStatus.confirmed == newStatus || CurriculumElementStatus.cancelled == newStatus) {
			applyCustomNotificationButton = uifactory.addFormLink("apply.custom.notifications", buttonsCont, Link.LINK);
			DropdownItem moreMenu = uifactory.addDropdownMenu("action.more", null, null, buttonsCont, getTranslator());
			moreMenu.setCarretIconCSS("o_icon o_icon_caret");
			moreMenu.setOrientation(DropdownOrientation.normal);
			moreMenu.addElement(applyCustomNotificationButton);
			
			applyWithoutNotificationButton = uifactory.addFormLink("apply.without.notifications", buttonsCont, Link.BUTTON);
		}
	}
	
	/**
	 * If all descendants are not editable, there is no editable children.
	 * 
	 * @return Number of children, or 0 if one of them is not editable
	 */
	private long numOfEditableChildren() {
		List<CurriculumElement> elements = curriculumService.getCurriculumElementsDescendants(curriculumElement).stream()
				.filter(element -> !element.getElementStatus().isCancelledOrClosed())
				.toList();
		
		long numChildren = elements.size();
		long numOfEditableChildren = elements.stream()
				.filter(element -> secCallback.canEditCurriculumElement(element))
				.count();
		
		return numOfEditableChildren == numChildren ? numOfEditableChildren : 0;
	}
	
	private void updateWarning() {
		Date begin = curriculumElement.getBeginDate();
		Date end = curriculumElement.getEndDate();
		Date todayStart = DateUtils.getStartOfDay(new Date());
		Date todayEnd = DateUtils.getEndOfDay(todayStart);
		if (!newStatus.isCancelledOrClosed()) {
			if(end != null && todayEnd.after(end)) {
				setFormWarning("warning.execution.period.already.ended");
			} else if(begin != null && todayStart.after(begin)) {
				setFormWarning("warning.execution.period.already.started");
			}
		} else if(newStatus == CurriculumElementStatus.cancelled) {
			if(end != null && todayEnd.before(end) && begin != null && todayStart.after(begin)) {
				setFormWarning("warning.execution.period.already.started");
			}
		} else if(newStatus == CurriculumElementStatus.finished) {
			if(end != null && todayEnd.before(end)) {
				setFormWarning("warning.execution.period.not.ended");
			} else if(begin != null &&  todayStart.before(begin)) {
				setFormWarning("warning.execution.period.not.started");
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (customizeNotificationsCtrl == source) {
			MailTemplate customTemplate = customizeNotificationsCtrl.getMailTemplate();
			cmc.deactivate();
			cleanUp();
			
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				doApplyWithCustomNotifications(ureq, customTemplate);
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(customizeNotificationsCtrl);
		removeAsListenerAndDispose(cmc);
		customizeNotificationsCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (applyWithoutNotificationButton == source) {
			doApply(ureq, new MailPackage(false));
		} else if (applyCustomNotificationButton == source) {
			doCustomizeNotifications(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doApplyWithNotifications(ureq);
	}
	
	private void doCustomizeNotifications(UserRequest ureq) {
		customizeNotificationsCtrl = new CustomizeNotificationController(ureq, getWindowControl(), getMailTemplate());
		listenTo(customizeNotificationsCtrl);
		
		String title = translate("customize.notifications.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), customizeNotificationsCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doApplyWithCustomNotifications(UserRequest ureq, MailTemplate template) {
		MailPackage mailing = new MailPackage(template, new MailerResult(), (MailContext)null, template != null);
		doApply(ureq, mailing);
	}
	
	private void doApplyWithNotifications(UserRequest ureq) {
		MailTemplate template = getMailTemplate();
		MailPackage mailing = template != null? new MailPackage(template, new MailerResult(), (MailContext)null, true): null;
		doApply(ureq, mailing);
	}

	private void doApply(UserRequest ureq, MailPackage mailPackage) {
		boolean updateChildren = changeForEl != null && changeForEl.isOneSelected() && changeForEl.isKeySelected(CHANGE_FOR_ALL);
		curriculumElement = curriculumService.updateCurriculumElementStatus(getIdentity(), curriculumElement, newStatus, updateChildren, mailPackage);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private MailTemplate getMailTemplate() {
		if (CurriculumElementStatus.confirmed == newStatus) {
			return CurriculumMailing.getStatusConfirmedMailTemplate(curriculumElement.getCurriculum(), curriculumElement, getIdentity());
		} else if (CurriculumElementStatus.cancelled == newStatus) {
			return CurriculumMailing.getStatusCancelledMailTemplate(curriculumElement.getCurriculum(), curriculumElement, getIdentity());
		}
		return null;
	}

}
