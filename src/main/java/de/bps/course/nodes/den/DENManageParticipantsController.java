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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.den;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;

import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableMultiSelectEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailNotificationEditController;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.co.ContactFormController;
import org.springframework.beans.factory.annotation.Autowired;

import de.bps.course.nodes.DENCourseNode;

public class DENManageParticipantsController extends BasicController {

	public static final String ADD_ACTION = "denAddParticipants";
	public static final String REMOVE_ACTION = "denRemoveParticipants";

	private DENCourseNode courseNode;
	private OLATResourceable ores;
	private DENStatus status;
	
	//objects for list of participants view
	private List<KalendarEvent> dateList;
	private DENListTableDataModel listTableData;
	private TableController tableListParticipants;
	
	//objects for manage participants view
	private VelocityContainer participantsVC;
	private DENParticipantsTableDataModel participantsTableData;
	private TableController tableManageParticipants;
	private Link addParticipantButton;
	private UserSearchController userSearchCntrl;
	private CloseableModalController userSearchCMC;
	private KalendarEvent selectedEvent;
	
	//mail notification
	private MailNotificationEditController notificationCtr;
	private ContactFormController contactCtr;
	private CloseableModalController notificationCmc;
	private List<Identity> selectedIds = new ArrayList<>();
	
	private CloseableModalController manageParticipantsModalCntrl;
	
	private DENManager denManager;
	@Autowired
	private MailManager mailManager;
	
	public DENManageParticipantsController(UserRequest ureq, WindowControl wControl,
			OLATResourceable ores, DENCourseNode courseNode, boolean readOnly) {
		super(ureq, wControl);
		
		this.ores = ores;
		this.courseNode = courseNode;
		denManager = DENManager.getInstance();
		
		//prepare list of enrolled participants
		dateList = denManager.getDENEvents(ores.getResourceableId(), courseNode.getIdent());
		listTableData = new DENListTableDataModel(dateList, getTranslator(), readOnly);
		tableListParticipants = denManager.createListParticipantsTable(ureq, wControl, getTranslator(), listTableData);
		listenTo(tableListParticipants);
		
		putInitialPanel(tableListParticipants.getInitialComponent());
	}
	
	@Override
	protected void doDispose() {
		if(tableListParticipants != null) {
			removeAsListenerAndDispose(tableListParticipants);
			tableListParticipants = null;
		}
		if(tableManageParticipants != null) {
			removeAsListenerAndDispose(tableManageParticipants);
			tableManageParticipants = null;
		}
        super.doDispose();
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(source == tableListParticipants) {
			if(event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent tableEvent = (TableEvent)event;
				//open window for choosen date to manage the enrolled users or manually add
				if(tableEvent.getActionId().equals(DENListTableDataModel.CHANGE_ACTION)) {
					selectedEvent = listTableData.getObject(tableEvent.getRowId());
					List<Identity> participants = denManager.getEventParticipants(selectedEvent);
					participantsTableData = new DENParticipantsTableDataModel(participants);
					
					removeAsListenerAndDispose(tableManageParticipants);
					tableManageParticipants = denManager.createParticipantsTable(ureq, getWindowControl(), getTranslator(), participantsTableData);
					listenTo(tableManageParticipants);
					
					participantsVC = createVelocityContainer("participants");
					DateFormat df = new SimpleDateFormat();
					participantsVC.contextPut("dateTitle", selectedEvent.getSubject());
					participantsVC.contextPut("dateTimeframe", df.format(selectedEvent.getBegin()) + " - " + df.format(selectedEvent.getEnd()));
					participantsVC.put("participantsTable", tableManageParticipants.getInitialComponent());
					
					addParticipantButton = LinkFactory.createButton("participants.add", participantsVC, this);
					
					removeAsListenerAndDispose(manageParticipantsModalCntrl);
					manageParticipantsModalCntrl = new CloseableModalController(getWindowControl(), "close", participantsVC, true, translate("dates.table.participant.manage"));
					listenTo(manageParticipantsModalCntrl);
					
					manageParticipantsModalCntrl.activate();
				}
			} else {
				TableMultiSelectEvent tmse = (TableMultiSelectEvent)event;
				BitSet selection = tmse.getSelection();
				//delete all users from the selected dates
				if(tmse.getAction().equals(DENListTableDataModel.DELETE_ACTION) && selection.cardinality() > 0) {
					selectedIds = denManager.getSelectedEventParticipants(dateList, selection);
					dateList = denManager.deleteParticipants(ores, courseNode, denManager.getSelectedEventIDs(dateList, selection));
					listTableData.setObjects(dateList);
					//send notification mail
					createNotificationMail(ureq, courseNode.getShortTitle(), REMOVE_ACTION);
				} else if(tmse.getAction().equals(DENListTableDataModel.MAIL_ACTION) && selection.cardinality() > 0) {
					//send email to all users from the selected dates
					List<Identity> participants = denManager.getSelectedEventParticipants(dateList, selection);
					createParticipantsMail(ureq, participants);
				} else if(selection.cardinality() == 0) {
					showWarning("participants.message.empty");
				}
			}
		} else if(source == userSearchCntrl) {
			if(event == Event.CANCELLED_EVENT) {
				userSearchCMC.deactivate();
			} else {
				List<Identity> toAdd = null;
				selectedIds = new ArrayList<>();
				if (event instanceof SingleIdentityChosenEvent) {
					SingleIdentityChosenEvent singleEvent = (SingleIdentityChosenEvent) event;
					Identity choosenIdentity = singleEvent.getChosenIdentity();
					toAdd = new ArrayList<>();
					toAdd.add(choosenIdentity);
				} else if (event instanceof MultiIdentityChosenEvent) {
					MultiIdentityChosenEvent multiEvent = (MultiIdentityChosenEvent) event;
					toAdd = multiEvent.getChosenIdentities();
				}
				boolean showMessage = false;
				if(toAdd != null && toAdd.size() > 0) {
					for( Identity identity : toAdd ) {
						status = denManager.doEnroll(identity, selectedEvent, ores, courseNode, true);
						if(!status.isEnrolled() && status.getErrorMessage().equals(DENStatus.ERROR_ALREADY_ENROLLED))
							showMessage = true;
						else
							selectedIds.add(identity);
					}
					if(showMessage)
						showWarning("enrollment.warning.manual");
					refreshTables();
				}
				userSearchCMC.deactivate();
				if(selectedIds.size() > 0) {
					//write notification mail
					createNotificationMail(ureq, selectedEvent.getSubject(), ADD_ACTION);
				}
			}
			
		} else if(source == tableManageParticipants) {
			if(event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent tableEvent = (TableEvent)event;
				//delete single user from event
				if(tableEvent.getActionId().equals(DENParticipantsTableDataModel.REMOVE_ACTION)) {
					Identity identity = participantsTableData.getEntryAt(tableEvent.getRowId());
					status = denManager.cancelEnroll(identity, selectedEvent, ores);
					if(!status.isCancelled()) showError();
					//send notification mail
					else {
						selectedIds.clear();
						selectedIds.add(identity);
						createNotificationMail(ureq, selectedEvent.getSubject(), REMOVE_ACTION);
					}
					refreshTables();
				//write email to single user
				} else if(tableEvent.getActionId().equals(DENParticipantsTableDataModel.MAIL_ACTION)) {
					List<Identity> participants = new ArrayList<>();
					participants.add(participantsTableData.getEntryAt(tableEvent.getRowId()));
					createParticipantsMail(ureq, participants);
				}
			}
		} else if(source == notificationCtr && event == Event.DONE_EVENT) {
			if(notificationCtr.getMailTemplate() != null) {
				Identity sender = ureq.getIdentity();
				MailerResult result = new MailerResult();
				String metaId = UUID.randomUUID().toString().replace("-", "");
				MailContext context = new MailContextImpl(getWindowControl().getBusinessControl().getAsString());
				MailBundle[] bundles = mailManager.makeMailBundles(context, selectedIds, notificationCtr.getMailTemplate(), sender, metaId, result);
				result.append(mailManager.sendMessage(bundles));
				if(notificationCtr.getMailTemplate().getCpfrom()) {
					MailBundle ccBundles = mailManager.makeMailBundle(context, sender, notificationCtr.getMailTemplate(), sender, metaId, result);
					result.append(mailManager.sendMessage(ccBundles));
				}
				Roles roles = ureq.getUserSession().getRoles();
				boolean detailedErrorOutput = roles.isAdministrator() || roles.isSystemAdmin();
				MailHelper.printErrorsAndWarnings(result, getWindowControl(), detailedErrorOutput, ureq.getLocale());
			}
			notificationCmc.deactivate();
			selectedIds.clear();
		} else if(source == contactCtr) {
			notificationCmc.deactivate();
		}
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == addParticipantButton) {
			//open user search controller to manually add users in date
			removeAsListenerAndDispose(userSearchCntrl);
			userSearchCntrl = new UserSearchController(ureq, getWindowControl(), true, true, false);
			listenTo(userSearchCntrl);
			
			removeAsListenerAndDispose(userSearchCMC);
			userSearchCMC = new CloseableModalController(getWindowControl(), "close", userSearchCntrl.getInitialComponent());
			listenTo(userSearchCMC);
			
			userSearchCMC.activate();
		}
	}
	
	private void showError() {
		String message = status.getErrorMessage();
		if(DENStatus.ERROR_ALREADY_ENROLLED.equals(message)) {
			getWindowControl().setError("");
		} else if(DENStatus.ERROR_NOT_ENROLLED.equals(message)) {
			getWindowControl().setError("");
		} else if(DENStatus.ERROR_PERSISTING.equals(message)) {
			getWindowControl().setError("");
		} else if(DENStatus.ERROR_GENERAL.equals(message)) {
			getWindowControl().setError("");
		}
	}

	private void refreshTables() {
		//set all table datas new to refresh view
		List<Identity> participants = denManager.getEventParticipants(selectedEvent);
		participantsTableData.setEntries((participants));
		tableManageParticipants.setTableDataModel(participantsTableData);
		listTableData.setObjects(dateList);
		tableListParticipants.setTableDataModel(listTableData);
	}

	private void createNotificationMail(UserRequest ureq, String subjectStr, String action) {
		MailTemplate mailTempl;
		if (action.equals(REMOVE_ACTION)) {
			mailTempl = denManager.getRemovedMailTemplate(ureq, subjectStr, getTranslator());
		} else { 
			mailTempl = denManager.getAddedMailTemplate(ureq, subjectStr, getTranslator());
		}
		
		removeAsListenerAndDispose(notificationCtr);

		notificationCtr = new MailNotificationEditController(getWindowControl(), ureq, mailTempl, false, false, true);
		listenTo(notificationCtr);
		
		removeAsListenerAndDispose(notificationCmc);
		notificationCmc = new CloseableModalController(getWindowControl(), "close", notificationCtr.getInitialComponent());
		listenTo(notificationCmc);
		
		notificationCmc.activate();
	}

	private void createParticipantsMail(UserRequest ureq, List<Identity> participants) {
		VelocityContainer sendMessageVC = createVelocityContainer("sendmessage");
		ContactMessage cmsg = new ContactMessage(ureq.getIdentity());
		ContactList contactList = new ContactList(translate("participants.message.to"));
		contactList.addAllIdentites(participants);
		cmsg.addEmailTo(contactList);
		
		removeAsListenerAndDispose(contactCtr);
		contactCtr = new ContactFormController(ureq, getWindowControl(), false, false, false, cmsg);
		listenTo(contactCtr);
		
		sendMessageVC.contextPut("title", translate("participants.message"));
		sendMessageVC.put("contactForm", contactCtr.getInitialComponent());
		
		removeAsListenerAndDispose(notificationCmc);
		notificationCmc = new CloseableModalController(getWindowControl(), "close", sendMessageVC);
		listenTo(notificationCmc);
		
		notificationCmc.activate();
	}
}
