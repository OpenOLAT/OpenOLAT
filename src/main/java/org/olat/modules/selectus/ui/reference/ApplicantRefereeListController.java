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
package org.olat.modules.selectus.ui.reference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.RecruitingTableOption;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceRequestStatus;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.model.references.ReferenceSearchParameters;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import org.olat.modules.selectus.ui.components.ReferenceStatusCellRenderer;
import org.olat.modules.selectus.ui.reference.ApplicantRefereeTableModel.RefCols;

/**
 * 
 * Initial date: 20 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicantRefereeListController extends FormBasicController {

	private int counter = 0;
	private final Position position;
	private final Application application;
	
	private FormLink addRefereeButton;
	private FlexiTableElement tableEl;
	private ApplicantRefereeTableModel tableModel;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private CloseableCalloutWindowController toolsCallout;
	private ConfirmSendReminderController sendReminderCtrl;
	private ApplicantRefereeCreateController addRecommendationCtrl;
	private ConfirmDeactivateReferenceController confirmDeactivateCtrl;
	private ConfirmReactivateReferenceController confirmReactivateCtrl;
	
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public ApplicantRefereeListController(UserRequest ureq, WindowControl wControl,  Position position, Application application) {
		super(ureq, wControl, "applicant_reference_list", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.application = application;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			initMessages(layoutCont);
			initDeadline(layoutCont);
		}
		
		addRefereeButton = uifactory.addFormLink("add.recommendation", formLayout, Link.BUTTON);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RefCols.fullName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RefCols.mail));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RefCols.referenceStatus, new ReferenceStatusCellRenderer()));
		
		RecruitingTableOption dueDateOption = recruitingModule.getTableApplicantDashboardDueDateOption();
		if(!dueDateOption.isDisabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(dueDateOption.isVisible(), RefCols.submissionDeadline, new DateCellRenderer()));
		}
	
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RefCols.dateInvitation, new DateCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RefCols.dateLastReminder, new DateCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RefCols.numOfReminders));
		
		StaticFlexiCellRenderer viewCol = new StaticFlexiCellRenderer(translate("send.reminder"), "reminder");
		viewCol.setIconLeftCSS("o_icon o_icon_mail");
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RefCols.sendReminder.i18nHeaderKey(), RefCols.sendReminder.ordinal(), "reminder",
				new BooleanCellRenderer(viewCol, null)));

		DefaultFlexiColumnModel toolsCol = new DefaultFlexiColumnModel(RefCols.tools);
		toolsCol.setIconHeader("o_icon o_icon-lg o_icon_actions_v2");
		columnsModel.addFlexiColumnModel(toolsCol);

		tableModel = new ApplicantRefereeTableModel(columnsModel, position, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 24, false, getTranslator(), formLayout);
	}

	private void initMessages(FormLayoutContainer formLayout) {
		String subTitle = translate("reference.management.subtitle", position.getMLTitle(getLocale()));
		formLayout.contextPut("subTitle", subTitle);
		
		// Message
		String refereeInstructionI18nKey = "";
		long min = position.getMinRefereesAsLong();
		long max = position.getMaxRefereesAsLong();
		if(min == 0 && max > 0) {
			refereeInstructionI18nKey = "reference.management.min.max.optional";	
		} else if(min >= 1 && min == max) {
			refereeInstructionI18nKey = "reference.management.min.max.equals";
		} else { // min >= 1 && max > 1
			refereeInstructionI18nKey = "reference.management.min.max";
		}
		String[] args = new String[] {
			RecruitingHelper.formatFullName(application, getTranslator()),	// 0 Full name
			Long.toString(min),												// 1 Min. references
			Long.toString(max),												// 2 Max. references
			
		};
		String refereesMinMax = translate(refereeInstructionI18nKey, args);
		formLayout.contextPut("message", refereesMinMax);
	}
	
	private void initDeadline(FormLayoutContainer formLayout) {
		String countdown;
		Date deadline = position.getApplicantRefereeManagementDeadline();
		if(deadline == null) {
			countdown = "";
		} else {
			Calendar cal = Calendar.getInstance();
			// Until end of day
			deadline = RecruitingHelper.endOfDay(deadline);
			cal.setTime(deadline);
			
			Date now = new Date();
			String formattedDeadline = Formatter.getInstance(getLocale()).formatDateLong(deadline);
			if(now.compareTo(deadline) > 0) {
				countdown = translate("reference.management.closed");
			} else {
				double daysConst = 24 * 60 * 60 * 1000d;
				long ratingDeadlineTime = deadline.getTime();
				long countDays = Math.round((ratingDeadlineTime - now.getTime()) / daysConst);
				String countdownI18n = (countDays > 1)  ? "reference.management.access.days": "reference.management.access.day";
				countdown = translate(countdownI18n, new String[] { Long.toString(countDays), formattedDeadline });
			}
		}
		formLayout.contextPut("access", countdown);
	}
	
	protected void loadModel() {
		int activeReferees = 0;
		int submittedReferees = 0;
		List<ApplicantRefereeRow> rows = new ArrayList<>();
		if(application != null) {
			ReferenceSearchParameters params = new ReferenceSearchParameters();
			List<Application> apps = new ArrayList<>();
			apps.add(application);
			params.setApplications(apps);
			params.setTypes(Collections.singletonList(ReferenceType.recommendation));
		
			List<Reference> references = recruitingService.getReferences(params);
			for(Reference reference:references) {
				ReferenceStatus status = reference.getReferenceStatus();
				ReferenceRequestStatus requestStatus = reference.getRequestStatus();
				if(ReferenceStatus.isActive(status, requestStatus)) {
					activeReferees++;
				}
				if(status == ReferenceStatus.submitted && requestStatus != ReferenceRequestStatus.declined) {
					submittedReferees++;
				}
			}

			for(Reference reference:references) {
				rows.add(forgeRow(reference, activeReferees));
			}
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
		
		loadStatusActive(activeReferees);
		loadStatusSubmitted(submittedReferees);
	}
	
	private void loadStatusActive(int activeReferees) {
		long min = position.getMinRefereesAsLong();
		long max = position.getMaxRefereesAsLong();
		String[] args = new String[] {
				Integer.toString(activeReferees),	// Active referees
				Long.toString(min),					// Min. references
				Long.toString(max),					// Max. references
				Long.toString(min - activeReferees)	// Missing references
		};

		// Active references
		String statusActive = null;
		String statusActiveCssClass = "o_success";
		String statusActiveIconCssClass = "o_icon_ok";
		if(min == max && activeReferees >= max) {
			statusActive = translate("reference.management.status.active.max", args);
		} else if(min != max && activeReferees >= min) {
			statusActive = translate("reference.management.status.active.min.max", args);
		} else if(activeReferees < min) {
			statusActive = translate("reference.management.status.active.min.error", args);
			statusActiveCssClass = "o_error";
			statusActiveIconCssClass = "o_icon_error";
		}

		flc.contextPut("statusActive", statusActive);
		flc.contextPut("statusActiveCssClass", statusActiveCssClass);
		flc.contextPut("statusActiveIconCssClass", statusActiveIconCssClass);
		
		addRefereeButton.setEnabled(max > activeReferees);
		
	}

	private void loadStatusSubmitted(int submittedReferees) {
		long min = position.getMinRefereesAsLong();
		long max = position.getMaxRefereesAsLong();
		String[] args = new String[] {
				Integer.toString(submittedReferees),	// Submitted referees
				Long.toString(min),						// Min. references
				Long.toString(max),						// Max. references
				Long.toString(min - submittedReferees)	// Missing references
		};
		
		// Submitted references
		String statusSubmitted = null;
		String statusSubmittedCssClass = "o_success";
		String statusSubmittedIconCssClass = "o_icon_ok";
		if(min == max && submittedReferees >= max) {
			statusSubmitted = translate("reference.management.status.submitted.max", args);
		} else if(min != max && submittedReferees >= min) {
			statusSubmitted = translate("reference.management.status.submitted.min.max", args);
		} else if(submittedReferees < min) {
			statusSubmitted = translate("reference.management.status.submitted.min.error", args);
			statusSubmittedCssClass = "o_warning";
			statusSubmittedIconCssClass = "o_icon_warn";
		}

		flc.contextPut("statusSubmitted", statusSubmitted);
		flc.contextPut("statusSubmittedCssClass", statusSubmittedCssClass);
		flc.contextPut("statusSubmittedIconCssClass", statusSubmittedIconCssClass);
	}
	
	private ApplicantRefereeRow forgeRow(Reference reference, int activeReferees) {
		ApplicantRefereeRow row = new ApplicantRefereeRow(reference);
		
		if(hasTools(reference, activeReferees)) {
			FormLink toolsLink = uifactory.addFormLink("tools_" + (++counter), "tools", "", null, null, Link.LINK | Link.NONTRANSLATED);
			toolsLink.setDomReplacementWrapperRequired(false);
			toolsLink.setIconLeftCSS("o_icon o_icon-lg o_icon_actions_v2");
			toolsLink.setUserObject(row);
			row.setToolsLink(toolsLink);
		}
		return row;
	}
	
	private boolean hasTools(Reference reference, int activeReferees) {
		if(reference.getReferenceStatus() == ReferenceStatus.submitted
				|| reference.getRequestStatus() == ReferenceRequestStatus.declined) {
			return false;
		}
		
		if(reference.getReferenceStatus() == ReferenceStatus.deactivated) {
			long max = position.getMaxRefereesAsLong();
			return activeReferees < max;
		}
		return true;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addRecommendationCtrl == source || sendReminderCtrl == source
				|| confirmReactivateCtrl == source || confirmDeactivateCtrl == source) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				if(toolsCallout != null) {
					toolsCallout.deactivate();
				}
				cleanUp();
			}
		} else if(cmc == source || toolsCallout == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeactivateCtrl);
		removeAsListenerAndDispose(confirmReactivateCtrl);
		removeAsListenerAndDispose(addRecommendationCtrl);
		removeAsListenerAndDispose(sendReminderCtrl);
		removeAsListenerAndDispose(toolsCallout);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeactivateCtrl = null;
		confirmReactivateCtrl = null;
		addRecommendationCtrl = null;
		sendReminderCtrl = null;
		toolsCallout = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addRefereeButton == source) {
			doAddReferee(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("reminder".equals(se.getCommand())) {
					doConfirmSendReminder(ureq, tableModel.getObject(se.getIndex()));
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("tools".equals(link.getCmd()) && link.getUserObject() instanceof ApplicantRefereeRow) {
				doOpenTools(ureq, link, (ApplicantRefereeRow)link.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doConfirmSendReminder(UserRequest ureq, ApplicantRefereeRow row) {
		if(guardModalController(sendReminderCtrl)) return;
		
		sendReminderCtrl = new ConfirmSendReminderController(ureq, getWindowControl(), position, application, row.getReference());
		listenTo(sendReminderCtrl);
		
		String title = translate("send.reminder");
		cmc = new CloseableModalController(getWindowControl(), "c", sendReminderCtrl.getInitialComponent(), title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doAddReferee(UserRequest ureq) {
		if(guardModalController(addRecommendationCtrl)) return;
		
		addRecommendationCtrl = new ApplicantRefereeCreateController(ureq, getWindowControl(), position, application);
		listenTo(addRecommendationCtrl);
		
		String title = translate("add.recommendation");
		cmc = new CloseableModalController(getWindowControl(), "c", addRecommendationCtrl.getInitialComponent(), title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doOpenTools(UserRequest ureq, FormLink link, ApplicantRefereeRow row) {
		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		toolsCallout = new CloseableCalloutWindowController(ureq, getWindowControl(), toolsCtrl.getInitialComponent(),
				link.getFormDispatchId(), translate("edit"), true, "");
		listenTo(toolsCallout);
		toolsCallout.activate();
	}
	
	private void doReactivate(UserRequest ureq, ApplicantRefereeRow row) {
		if(guardModalController(confirmReactivateCtrl)) return;
		
		confirmReactivateCtrl = new ConfirmReactivateReferenceController(ureq, getWindowControl(), row.getReference());
		listenTo(confirmReactivateCtrl);
		
		String title = translate("reference.management.reactivate");
		cmc = new CloseableModalController(getWindowControl(), "c", confirmReactivateCtrl.getInitialComponent(), title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doDeactivate(UserRequest ureq, ApplicantRefereeRow row) {
		if(guardModalController(confirmDeactivateCtrl)) return;
		
		confirmDeactivateCtrl = new ConfirmDeactivateReferenceController(ureq, getWindowControl(), row.getReference());
		listenTo(confirmDeactivateCtrl);
		
		String title = translate("reference.management.deactivate");
		cmc = new CloseableModalController(getWindowControl(), "c", confirmDeactivateCtrl.getInitialComponent(), title);
		cmc.activate();
		listenTo(cmc);
	}

	private class ToolsController extends BasicController {
		
		private Link reactivateLink;
		private Link deactivateLink;
		
		private final ApplicantRefereeRow refereeRow;
		
		private ToolsController(UserRequest ureq, WindowControl wControl, ApplicantRefereeRow refereeRow) {
			super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
			this.refereeRow = refereeRow;
			final Reference reference = refereeRow.getReference();

			VelocityContainer mainVC = createVelocityContainer("applicant_reference_tools");
			if(reference.getReferenceStatus() == ReferenceStatus.deactivated) {
				reactivateLink = LinkFactory.createLink("reference.management.reactivate", "reactivate", getTranslator(), mainVC, this, Link.LINK);
				mainVC.put("reactivate", reactivateLink);
			} else {
				deactivateLink = LinkFactory.createLink("reference.management.deactivate", "deactivate", getTranslator(), mainVC, this, Link.LINK);
				mainVC.put("deactivate", deactivateLink);
			}
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(reactivateLink == source) {
				doReactivate(ureq, refereeRow);
			} else if(deactivateLink == source) {
				doDeactivate(ureq, refereeRow);
			}
		}
	}
}
