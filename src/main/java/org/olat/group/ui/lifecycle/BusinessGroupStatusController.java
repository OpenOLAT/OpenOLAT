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
package org.olat.group.ui.lifecycle;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupLifecycleManager;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupStatusEnum;
import org.olat.group.ui.main.BusinessGroupListController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupStatusController extends FormBasicController {
	
	private FormLink restoreButton;
	private FormLink softDeleteButton;
	private FormLink reactivateButton;
	private FormLink inactivateButton;
	private FormLink startSoftDeleteButton;
	private FormLink startInactivateButton;
	private FormLink definitivelyDeleteButton;
	
	private final boolean hasMembers;
	private BusinessGroup businessGroup;

	private CloseableModalController cmc;
	private ConfirmRestoreController confirmRestoreCtrl;
	private ConfirmBusinessGroupChangeStatusController confirmChangeStatusCtlr;
	private ConfirmBusinessGroupStartChangeStatusController confirmStartChangeStatusCtlr;
	private ConfirmBusinessGroupDefinitivelyDeleteController confirmDefinitivelyDeleteCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BusinessGroupModule businessGroupModule;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupLifecycleManager businessGroupLifecycleManager;
	
	public BusinessGroupStatusController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup) {
		super(ureq, wControl, Util.createPackageTranslator(BusinessGroupListController.class, ureq.getLocale()));
		this.businessGroup = businessGroup;
		hasMembers = businessGroupService.countMembers(businessGroup, GroupRoles.coach.name(), GroupRoles.participant.name()) > 0;
		
		initForm(ureq);
	}
	
	public BusinessGroup getBusinessGroup() {
		return businessGroup;
	}

	public void updateBusinessGroup(BusinessGroup bGroup) {
		this.businessGroup = bGroup;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("fieldset.legend.status");

		Formatter formatter = Formatter.getInstance(getLocale());
		initStatusForm(formLayout);
		
		Date creationDate = businessGroup.getCreationDate();
		String creation = formatter.formatDate(creationDate);
		uifactory.addStaticTextElement("status.creation", creation, formLayout);

		BusinessGroupStatusEnum status = businessGroup.getGroupStatus();
		if(status == BusinessGroupStatusEnum.active) {
			initActiveForm(formLayout, ureq, formatter);
		} else if(status == BusinessGroupStatusEnum.inactive) {
			initInactiveForm(formLayout, ureq, formatter);
		} else if(status == BusinessGroupStatusEnum.trash) {
			initSoftDeletedForm(formLayout, ureq, formatter);
		}
	}
	

	private void initStatusForm(FormItemContainer formLayout) {
		BusinessGroupStatusEnum status = businessGroup.getGroupStatus();
		String value = translate("status." + status.name());
		boolean mailSent = false;
		if(status == BusinessGroupStatusEnum.active) {
			mailSent = businessGroupLifecycleManager.getInactivationEmailDate(businessGroup) != null;	
		} else if(status == BusinessGroupStatusEnum.inactive) {
			mailSent = businessGroupLifecycleManager.getSoftDeleteEmailDate(businessGroup) != null;
		}
		
		if(mailSent) {
			value += " - " + translate("status.within.reactiontime");
		}
		
		uifactory.addStaticTextElement("status", value, formLayout);
	}
	
	private void initActiveForm(FormItemContainer formLayout, UserRequest ureq, Formatter formatter) {
		Date lastUsageDate = businessGroup.getLastUsage();
		String lastUsage = formatter.formatDate(lastUsageDate);
		uifactory.addStaticTextElement("status.last.usage", lastUsage, formLayout);

		boolean withMail = businessGroupModule.getNumberOfDayBeforeDeactivationMail() > 0;
		String mode = buildMode(businessGroupModule.isAutomaticGroupInactivationEnabled(), withMail);
		uifactory.addStaticTextElement("status.mode", mode, formLayout);

		int delay = businessGroupModule.getNumberOfDayBeforeDeactivationMail();
		if(delay > 0) {
			long used = businessGroupLifecycleManager.getInactivationResponseDelayUsed(businessGroup);
			if(used >= 0) {
				String[] responseArgs = new String[] { Integer.toString(delay), Long.toString(used) };
				String response;
				if(delay == 1l) {
					response = translate("status.inactivation.delay.days.of.singular", responseArgs);
				} else {
					response = translate("status.inactivation.delay.days.of", responseArgs);
				}
				uifactory.addStaticTextElement("status.inactivation.delay", response, formLayout);
			}
		}

		Date inactivationDate = businessGroupLifecycleManager.getInactivationDate(businessGroup);
		String inactivation = formatter.formatDate(inactivationDate);
		long days = DateUtils.countDays(ureq.getRequestTimestamp(), inactivationDate);
		String inactivationI18n;
		if(days == 0) {
			inactivationI18n = "status.inactivation.at.today";
		} else if(days == 1) {
			inactivationI18n = "status.inactivation.at.singular";
		} else if(days > 1) {
			inactivationI18n = "status.inactivation.at";
		} else if(days == -1) {
			inactivationI18n = "status.inactivation.overdue.singular";
		} else {
			inactivationI18n = "status.inactivation.overdue";
		}
		uifactory.addStaticTextElement("status.inactivation.planned", translate(inactivationI18n, inactivation, Long.toString(Math.abs(days))), formLayout);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		
		if(withMail && hasMembers && businessGroupLifecycleManager.getInactivationEmailDate(businessGroup) == null) {
			startInactivateButton = uifactory.addFormLink("inactivate.group.start", buttonsCont, Link.BUTTON);
			startInactivateButton.setCustomEnabledLinkCSS("btn btn-default btn-primary");
		} else {
			inactivateButton = uifactory.addFormLink("inactivate.group", buttonsCont, Link.BUTTON);
			inactivateButton.setCustomEnabledLinkCSS("btn btn-default btn-primary");
		}
		
		if(businessGroupLifecycleManager.getInactivationEmailDate(businessGroup) != null) {
			reactivateButton = uifactory.addFormLink("cancel.inactivate.group", buttonsCont, Link.BUTTON);
		}
	}
	
	private void initInactiveForm(FormItemContainer formLayout, UserRequest ureq, Formatter formatter) {
		Date inactivationDate = businessGroupLifecycleManager.getInactivationDate(businessGroup);
		String inactivation = formatter.formatDate(inactivationDate);
		uifactory.addStaticTextElement("status.inactivation", inactivation, formLayout);

		Identity inactivatedBy = businessGroupLifecycleManager.getInactivatedBy(businessGroup);
		String inactivatedByStr;
		if(inactivatedBy == null) {
			inactivatedByStr = translate("process.auto");
		} else {
			inactivatedByStr = userManager.getUserDisplayName(inactivatedBy);
		}
		uifactory.addStaticTextElement("status.inactivation.by", inactivatedByStr, formLayout);
		
		boolean withMail = businessGroupModule.getNumberOfDayBeforeSoftDeleteMail() > 0;
		String mode = buildMode(businessGroupModule.isAutomaticGroupInactivationEnabled(), withMail);
		uifactory.addStaticTextElement("status.mode", mode, formLayout);

		int delay = businessGroupModule.getNumberOfDayBeforeSoftDeleteMail();
		if(delay > 0) {
			long used = businessGroupLifecycleManager.getSoftDeleteResponseDelayUsed(businessGroup);
			if(used >= 0) {
				String[] responseArgs = new String[] { Integer.toString(delay), Long.toString(used) };
				String response;
				if(delay == 1l) {
					response = translate("status.soft.delete.delay.days.of.singular", responseArgs);
				} else {
					response = translate("status.soft.delete.delay.days.of", responseArgs);
				}
				uifactory.addStaticTextElement("status.soft.delete.delay", response, formLayout);
			}
		}
		
		Date planedDate = businessGroupLifecycleManager.getSoftDeleteDate(businessGroup);
		if(planedDate != null) {
			long numOfDaysBeforeDelete = DateUtils.countDays(ureq.getRequestTimestamp(), planedDate);
			String[] args = new String[] { formatter.formatDate(planedDate), Long.toString(Math.abs(numOfDaysBeforeDelete)) };
			String plan;
			if(numOfDaysBeforeDelete == 0) {
				plan = translate("status.soft.delete.planned.days.of.today", args);
			} else if(numOfDaysBeforeDelete == 1) {
				plan = translate("status.soft.delete.planned.days.of.singular", args);
			} else if(numOfDaysBeforeDelete > 1) {
				plan = translate("status.soft.delete.planned.days.of", args);
			} else if(numOfDaysBeforeDelete == -1) {
				plan = translate("status.soft.delete.overdue.days.singular", args);
			} else {
				plan = translate("status.soft.delete.overdue.days", args);
			}
			uifactory.addStaticTextElement("status.soft.delete.planned", plan, formLayout);
		}

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		
		if(withMail && hasMembers && businessGroupLifecycleManager.getSoftDeleteEmailDate(businessGroup) == null) {
			startSoftDeleteButton = uifactory.addFormLink("soft.delete.group.start", buttonsCont, Link.BUTTON);
			startSoftDeleteButton.setCustomEnabledLinkCSS("btn btn-default btn-primary");
		} else {
			softDeleteButton = uifactory.addFormLink("soft.delete.group.action", buttonsCont, Link.BUTTON);
			softDeleteButton.setCustomEnabledLinkCSS("btn btn-default btn-primary");
		}
		reactivateButton = uifactory.addFormLink("reactivate.group", buttonsCont, Link.BUTTON);
	}
	

	private void initSoftDeletedForm(FormItemContainer formLayout, UserRequest ureq, Formatter formatter) {
		
		Date softDeleteDate = businessGroupLifecycleManager.getSoftDeleteDate(businessGroup);
		String softDelete = formatter.formatDate(softDeleteDate);
		uifactory.addStaticTextElement("status.soft.delete.at", softDelete, formLayout);
		
		Identity softDeletedBy = businessGroupLifecycleManager.getSoftDeletedBy(businessGroup);
		String softDeletedByStr;
		if(softDeletedBy == null) {
			softDeletedByStr = translate("process.auto");
		} else {
			softDeletedByStr = userManager.getUserDisplayName(softDeletedBy);
		}
		uifactory.addStaticTextElement("status.soft.delete.by", softDeletedByStr, formLayout);
		
		String mode = buildMode(businessGroupModule.isAutomaticGroupDefinitivelyDeleteEnabled(), null);
		uifactory.addStaticTextElement("status.mode", mode, formLayout);
		
		Date planedDate = businessGroupLifecycleManager.getDefinitiveDeleteDate(businessGroup);
		if(planedDate != null) {
			long numOfDaysBeforeDelete = DateUtils.countDays(ureq.getRequestTimestamp(), planedDate);
			String[] args = new String[] { formatter.formatDate(planedDate), Long.toString(Math.abs(numOfDaysBeforeDelete)) };
			String plan;
			if(numOfDaysBeforeDelete == 0 || numOfDaysBeforeDelete == 1) {
				plan = translate("status.definitive.delete.planned.days.of.singular", args);
			} else if(numOfDaysBeforeDelete > 1) {
				plan = translate("status.definitive.delete.planned.days.of", args);
			} else if(numOfDaysBeforeDelete == -1) {
				plan = translate("status.definitive.delete.overdue.singular", args);
			} else {
				plan = translate("status.definitive.delete.overdue", args);
			}
			uifactory.addStaticTextElement("status.definitive.delete.planned", plan, formLayout);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		definitivelyDeleteButton = uifactory.addFormLink("delete.group", buttonsCont, Link.BUTTON);
		definitivelyDeleteButton.setCustomEnabledLinkCSS("btn btn-default btn-danger");
		restoreButton = uifactory.addFormLink("restore", buttonsCont, Link.BUTTON);
	}
	
	private String buildMode(boolean auto, Boolean days) {
		String i18nAuto = auto ? "process.auto" : "process.manual";
		StringBuilder sb = new StringBuilder();
		sb.append(translate(i18nAuto));
		if(days != null) {
			String dayI18n = days.booleanValue() ? "process.with.email.short" : "process.without.email";
			sb.append(" - ").append(translate(dayI18n));
		}
		return sb.toString();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmChangeStatusCtlr == source || confirmStartChangeStatusCtlr == source || confirmRestoreCtrl == source) {
			cmc.deactivate();
			if(event == Event.DONE_EVENT) {
				businessGroup = businessGroupService.loadBusinessGroup(businessGroup);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cleanUp();
		} else if(confirmDefinitivelyDeleteCtrl == source) {
			cmc.deactivate();
			if(event == Event.DONE_EVENT) {
				businessGroup = null;
				fireEvent(ureq, Event.CLOSE_EVENT);
			}
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDefinitivelyDeleteCtrl);
		removeAsListenerAndDispose(confirmStartChangeStatusCtlr);
		removeAsListenerAndDispose(confirmChangeStatusCtlr);
		removeAsListenerAndDispose(cmc);
		confirmDefinitivelyDeleteCtrl = null;
		confirmStartChangeStatusCtlr = null;
		confirmChangeStatusCtlr = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(inactivateButton == source) {
			doConfirmChangeStatus(ureq, BusinessGroupStatusEnum.inactive);
		} else if(reactivateButton == source) {
			doReactivate(ureq);
		} else if(restoreButton == source) {
			doConfirmRestore(ureq);
		} else if(softDeleteButton == source) {
			doConfirmChangeStatus(ureq, BusinessGroupStatusEnum.trash);
		} else if(definitivelyDeleteButton == source) {
			doConfirmDefinitivelyDelete(ureq);
		} else if(startInactivateButton == source) {
			doConfirmStartChangeStatus(ureq, BusinessGroupStatusEnum.inactive);
		} else if(this.startSoftDeleteButton == source) {
			doConfirmStartChangeStatus(ureq, BusinessGroupStatusEnum.trash);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doConfirmChangeStatus(UserRequest ureq, BusinessGroupStatusEnum newStatus) {
		confirmChangeStatusCtlr = new ConfirmBusinessGroupChangeStatusController(ureq, getWindowControl(),
				List.of(businessGroup), newStatus );
		listenTo(confirmChangeStatusCtlr);
		
		String key;
		if(newStatus == BusinessGroupStatusEnum.trash) {
			key = "dialog.modal.bg.soft.delete.title.singular";
		} else {
			key = "dialog.modal.bg.inactivate.title.singular";
		}
		String title = translate(key, businessGroup.getName(), "1");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmChangeStatusCtlr.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doConfirmStartChangeStatus(UserRequest ureq, BusinessGroupStatusEnum newStatus) {
		confirmStartChangeStatusCtlr = new ConfirmBusinessGroupStartChangeStatusController(ureq, getWindowControl(),
				List.of(businessGroup), newStatus );
		listenTo(confirmStartChangeStatusCtlr);
		
		String key;
		if(newStatus == BusinessGroupStatusEnum.trash) {
			key = "dialog.modal.bg.soft.delete.title.singular";
		} else {
			key = "dialog.modal.bg.inactivate.title.singular";
		}
		String title = translate(key, businessGroup.getName(), "1" );
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmStartChangeStatusCtlr.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doReactivate(UserRequest ureq) {
		boolean groupOwner = isCoach();
		businessGroup = businessGroupLifecycleManager.reactivateBusinessGroup(businessGroup, getIdentity(), groupOwner);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doConfirmDefinitivelyDelete(UserRequest ureq) {
		confirmDefinitivelyDeleteCtrl = new ConfirmBusinessGroupDefinitivelyDeleteController(ureq, getWindowControl(),
				List.of(businessGroup), true);
		listenTo(confirmDefinitivelyDeleteCtrl);

		String key = "dialog.modal.bg.delete.title";	
		String title = translate(key, businessGroup.getName(), "1");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDefinitivelyDeleteCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doConfirmRestore(UserRequest ureq) {
		confirmRestoreCtrl = new ConfirmRestoreController(ureq, getWindowControl(), List.of(businessGroup));
		listenTo(confirmRestoreCtrl);

		String key = "dialog.modal.bg.restore.title";	
		String title = translate(key, businessGroup.getName(), "1");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmRestoreCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private boolean isCoach() {
		return businessGroupService.hasRoles(getIdentity(), businessGroup, GroupRoles.coach.name());
	}
}
