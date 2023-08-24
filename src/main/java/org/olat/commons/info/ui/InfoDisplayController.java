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

package org.olat.commons.info.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.commons.info.InfoMessage;
import org.olat.commons.info.InfoMessageFrontendManager;
import org.olat.commons.info.InfoMessageToCurriculumElement;
import org.olat.commons.info.InfoMessageToGroup;
import org.olat.commons.info.manager.MailFormatter;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.date.DateComponentFactory;
import org.olat.core.gui.components.date.DateElement;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.run.GoToEvent;
import org.olat.group.BusinessGroup;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Controller which display the info messages from an OLATResourceable
 * 
 * <P>
 * Initial Date:  26 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class InfoDisplayController extends FormBasicController {

	private Step start;
	private FormLink newInfoLink;
	private FormLink oldMsgsLink;
	private FormLink newMsgsLink;
	private final List<FormLink> editLinks = new ArrayList<>();
	private final List<FormLink> deleteLinks = new ArrayList<>();
	private StepsMainRunController newInfoWizard;
	private StepsMainRunController editInfoWizard;
	private DialogBoxController confirmDelete;
	
	private final List<Long> previousDisplayKeys = new ArrayList<>();
	private final InfoSecurityCallback secCallback;
	private final OLATResourceable ores;
	private final String resSubPath;
	private final String businessPath;
	private String attachmentMapper;
	private Map<String, VFSLeaf> infoKeysToAttachment;
	
	private int maxResults = 0;
	private int maxResultsConfig = 0;
	private Date after = null;
	private Date afterConfig = null;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private InfoMessageFrontendManager infoMessageManager;
	
	private LockResult lockEntry;
	private MailFormatter sendMailFormatter;

	private SendMailOption sendSubscriberOption;
	private final List<SendMailOption> sendMailOptions = new ArrayList<>();
	private final List<SendMailOption> groupsMailOptions = new ArrayList<>();
	private final List<SendMailOption> curriculaMailOptions = new ArrayList<>();
	
	public InfoDisplayController(UserRequest ureq, WindowControl wControl, InfoSecurityCallback secCallback,
			BusinessGroup businessGroup, String resSubPath, String businessPath) {
		super(ureq, wControl, "display");
		this.secCallback = secCallback;
		this.ores = businessGroup.getResource();
		this.resSubPath = resSubPath;
		this.businessPath = businessPath;
		// default show 10 messages for groups
		maxResults = maxResultsConfig = 10;
		attachmentMapper = registerCacheableMapper(ureq, "InfoMessages", new AttachmentMapper());
		
		initForm(ureq);	
		
		// now load with configuration
		loadMessages();
	}
	
	public InfoDisplayController(UserRequest ureq, WindowControl wControl, int maxResults, int duration,
			InfoSecurityCallback secCallback, OLATResourceable ores, String resSubPath, String businessPath) {
		super(ureq, wControl, "display");
		this.secCallback = secCallback;
		this.ores = ores;
		this.resSubPath = resSubPath;
		this.businessPath = businessPath;
		this.maxResults = maxResults;
		this.maxResultsConfig = maxResults;
		
		attachmentMapper = registerCacheableMapper(ureq, "InfoMessages", new AttachmentMapper());
		
		if(duration > 0) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.DATE, -duration);
			after = afterConfig = cal.getTime();
		}
		
		
		
		initForm(ureq);
		
		// OLAT-6302 when a specific message is shown display the page that
		// contains the message. Jump in e.g. from portlet
		ContextEntry ce = wControl.getBusinessControl().popLauncherContextEntry();
		if (ce != null) { // a context path is left for me
			OLATResourceable businessPathResource = ce.getOLATResourceable();
			String typeName = businessPathResource.getResourceableTypeName();
			if ("InfoMessage".equals(typeName)) {
				Long messageId = businessPathResource.getResourceableId();
				if (messageId != null && messageId.longValue() > 0) {
					// currently no pageing is implemented, just page with all entries
					maxResults = -1;
					after = null;
				}
			}
		}
		
		// now load with configuration
		loadMessages();
	}

	public void setSendSubscriberOption(SendMailOption sendSubscriberOption) {
		this.sendSubscriberOption = sendSubscriberOption;
	}
	
	public void addSendMailOptions(SendMailOption sendMailOption) {
		sendMailOptions.add(sendMailOption);
	}
	
	public void addGroupMailOption(SendMailOption sendMailOption) {
		groupsMailOptions.add(sendMailOption);
	}
	
	public void addCurriuclaMailOptions(SendMailOption sendMailOption) {
		curriculaMailOptions.add(sendMailOption);
	}

	public void setSendMailFormatter(MailFormatter sendMailFormatter) {
		this.sendMailFormatter = sendMailFormatter;
	}

	/**
	 * This is the main method which push the messages in the layout container,
	 * and clean-up old links.
	 */
	protected void loadMessages() {
		//first clear the current message if any
		for(Long key:previousDisplayKeys) {
			flc.contextRemove("info.date." + key);
			if(flc.getComponent("info.delete." + key) != null) {
				flc.remove("info.delete." + key);
			}
			if(flc.getComponent("info.edit." + key) != null) {
				flc.remove("info.edit." + key);
			}
		}
		previousDisplayKeys.clear();
		deleteLinks.clear();

		List<InfoMessage> msgs = infoMessageManager.loadInfoMessageByResource(ores, resSubPath, businessPath, after, null, 0, maxResults);
		List<InfoMessageForDisplay> infoDisplays = new ArrayList<>(msgs.size());
		Map<String, VFSLeaf> keysToDisplay = new HashMap<>();
		// sort infoMessages by publishdate
		msgs.sort(Comparator.comparing(InfoMessage::getPublishDate).reversed());
		for(InfoMessage info:msgs) {
			// skip infoMessage, if it is unpublished and the current user is not allowed to manage it
			if (!info.isPublished() && !secCallback.canEdit(info)) {
				continue;
			}
			previousDisplayKeys.add(info.getKey());
			InfoMessageForDisplay infoDisplay = createInfoMessageForDisplay(info);
			infoDisplays.add(infoDisplay);
			if(infoDisplay.getAttachments() != null) {
				for (VFSLeaf attachment : infoDisplay.getAttachments()) {
					keysToDisplay.put(info.getKey().toString() + "/" + attachment.getName(), attachment); 
				}
			}
			
			String dateCmpName = "info.date." + info.getKey();
			DateElement dateEl = DateComponentFactory.createDateElementWithYear(dateCmpName, info.getPublishDate());
			flc.add(dateCmpName, dateEl);
			
			if(secCallback.canEdit(info)) {
				String editName = "info.edit." + info.getKey();
				FormLink link = uifactory.addFormLink(editName, "edit", "edit", flc, Link.BUTTON_SMALL);
				link.setElementCssClass("o_sel_info_edit_msg");
				link.setUserObject(info);
				editLinks.add(link);
				flc.add(link);
			}
			if(secCallback.canDelete()) {
				String delName = "info.delete." + info.getKey();
				FormLink link = uifactory.addFormLink(delName, "delete", "delete", flc, Link.BUTTON_SMALL);
				link.setElementCssClass("o_sel_info_delete_msg");
				link.setUserObject(info);
				deleteLinks.add(link);
				flc.add(link);
			}
		}
		flc.contextPut("infos", infoDisplays);
		infoKeysToAttachment = keysToDisplay;

		int numOfInfos = infoMessageManager.countInfoMessageByResource(ores, resSubPath, businessPath, null, null);
		oldMsgsLink.setVisible((msgs.size() < numOfInfos));
		newMsgsLink.setVisible((msgs.size() == numOfInfos) && (numOfInfos > maxResultsConfig) && (maxResultsConfig > 0));
	}
	
	private InfoMessageForDisplay createInfoMessageForDisplay(InfoMessage info) {
		String message = info.getMessage();
		boolean html = StringHelper.isHtml(message);
		if(html) {
			message = message.toString();
		} else if(StringHelper.containsNonWhitespace(message)) {
			message = Formatter.escWithBR(info.getMessage()).toString();
			message =	Formatter.formatURLsAsLinks(message, true);
		}
		
		Formatter formatter = Formatter.getInstance(getLocale());
		
		String modifier = null;
		if(info.getModifier() != null) {
			String formattedName = userManager.getUserDisplayName(info.getModifier());
			String creationDate = formatter.formatDateAndTime(info.getModificationDate());
			modifier = translate("display.modifier", StringHelper.escapeHtml(formattedName), creationDate);
		}

		String authorName = userManager.getUserDisplayName(info.getAuthor());
		String creationDate = formatter.formatDate(info.getCreationDate());
		String publishDate = formatter.formatDateAndTime(info.getPublishDate());
		String infos;
		if (authorName.isEmpty()) {
			infos = translate("display.info.noauthor", publishDate);
		} else if (new Date().after(info.getPublishDate())) {
			infos = translate("display.info", StringHelper.escapeHtml(authorName), publishDate);
		} else {
			infos = translate("display.info.scheduled", publishDate, StringHelper.escapeHtml(authorName), creationDate);
		}
		List<VFSLeaf> attachments = infoMessageManager.getAttachments(info);
		return new InfoMessageForDisplay(info.getKey(), info.getTitle(), message, attachments, infos, modifier, info.isPublished());
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(secCallback.canAdd()) {
			newInfoLink = uifactory.addFormLink("new_message", "new_message", "new_message", formLayout, Link.BUTTON);
			newInfoLink.setElementCssClass("o_sel_course_info_create_msg");
			newInfoLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		}
		
		oldMsgsLink = uifactory.addFormLink("display.old_messages", "display.old_messages", "display.old_messages", formLayout, Link.BUTTON);
		oldMsgsLink.setElementCssClass("o_sel_course_info_old_msgs");
		newMsgsLink = uifactory.addFormLink("display.new_messages", "display.new_messages", "display.new_messages", formLayout, Link.BUTTON);
		newMsgsLink.setElementCssClass("o_sel_course_info_new_msgs");
		
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("attachmentMapper", attachmentMapper);
		}
	}
	
	@Override
	protected void doDispose() {
		if(lockEntry != null) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
			lockEntry = null;
		}
        super.doDispose();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == newInfoWizard) {
			if (event == Event.CANCELLED_EVENT) {
				getWindowControl().pop();
			} else if (event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				loadMessages();
			}	else if (event == Event.DONE_EVENT){
				showError("failed");
			}
		} else if(source == editInfoWizard) {
			if (event == Event.CANCELLED_EVENT) {
				getWindowControl().pop();
			} else if (event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				loadMessages();
			}	else if (event == Event.DONE_EVENT){
				showError("failed");
			}
			
			//release lock
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
			lockEntry = null;
		} else if(source == confirmDelete) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				InfoMessage msgToDelete = (InfoMessage)confirmDelete.getUserObject();

				ThreadLocalUserActivityLogger.log(CourseLoggingAction.INFO_MESSAGE_DELETED, getClass(),
						LoggingResourceable.wrap(msgToDelete.getOLATResourceable(), OlatResourceableType.infoMessage));
				
				infoMessageManager.deleteInfoMessage(msgToDelete);
				loadMessages();
			}
			confirmDelete.setUserObject(null);
			
			//release lock
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
			lockEntry = null;
		} else {
			super.event(ureq, source, event);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == newInfoLink) {
			InfoMessage msg = infoMessageManager.createInfoMessage(ores, resSubPath, businessPath, getIdentity());
			
			start = new CreateInfoStep(ureq, ores, resSubPath, sendSubscriberOption, sendMailOptions, groupsMailOptions, curriculaMailOptions, msg);
			newInfoWizard = new StepsMainRunController(ureq, getWindowControl(), start, new FinishedCallback(),
					new CancelCallback(), translate("create_message"), "o_sel_info_messages_create_wizard");
			listenTo(newInfoWizard);
			getWindowControl().pushAsModalDialog(newInfoWizard.getInitialComponent());
		} else if(deleteLinks.contains(source)) {
			InfoMessage msg = (InfoMessage)source.getUserObject();
			popupDelete(ureq, msg);
		} else if(editLinks.contains(source)) {
			InfoMessage msg = (InfoMessage)source.getUserObject();
			launchEditWizard(ureq, msg);
		} else if(source == oldMsgsLink) {
			maxResults = -1;
			after = null;
			loadMessages();
		} else if(source == newMsgsLink) {
			maxResults = maxResultsConfig;
			after = afterConfig;
			loadMessages();
		} else if(source == flc) {
			doGoTo(ureq);
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//nothing to do
	}
	
	private void doGoTo(UserRequest ureq) {
		String nodeId = ureq.getParameter("activateCourseNode");
		String tool = ureq.getParameter("activateCourseTool");
		if(StringHelper.containsNonWhitespace(nodeId)) {
			fireEvent(ureq, new GoToEvent(GoToEvent.GOTO_NODE, nodeId));
		} else if(StringHelper.containsNonWhitespace(tool)) {
			fireEvent(ureq, new GoToEvent(GoToEvent.GOTO_TOOL, tool));
		}
	}
	
	protected void popupDelete(UserRequest ureq, InfoMessage msg) {
		OLATResourceable mres = OresHelper.createOLATResourceableInstance(InfoMessage.class, msg.getKey());
		lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(mres, ureq.getIdentity(), "", getWindow());
		if(lockEntry.isSuccess()) {
			//locked -> reload the message
			msg = infoMessageManager.loadInfoMessage(msg.getKey());
			if(msg == null) {
				showWarning("already.deleted");
				CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
				lockEntry = null;
				loadMessages();
			} else {
				String title = StringHelper.escapeHtml(msg.getTitle());
				String confirmDeleteText = translate("edit.confirm_delete", new String[]{ title });
				confirmDelete = activateYesNoDialog(ureq, null, confirmDeleteText, confirmDelete);
				confirmDelete.setUserObject(msg);
			}
		} else {
			showLockError();
		}
	}
	
	protected void launchEditWizard(UserRequest ureq, InfoMessage msg) {
		OLATResourceable mres = OresHelper.createOLATResourceableInstance(InfoMessage.class, msg.getKey());
		lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(mres, ureq.getIdentity(), "", getWindow());
		if(lockEntry.isSuccess()) {
			msg = infoMessageManager.loadInfoMessage(msg.getKey());
			if(msg == null) {
				showWarning("already.deleted");
				CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
				lockEntry = null;
				loadMessages();
			} else {
				removeAsListenerAndDispose(editInfoWizard);

				start = new CreateInfoStep(ureq, ores, resSubPath, sendSubscriberOption, sendMailOptions, groupsMailOptions, curriculaMailOptions, msg);
				newInfoWizard = new StepsMainRunController(ureq, getWindowControl(), start, new FinishedCallback(),
						new CancelCallback(), translate("create_message"), "o_sel_info_messages_create_wizard");
				listenTo(newInfoWizard);
				getWindowControl().pushAsModalDialog(newInfoWizard.getInitialComponent());
			}
		} else {
			showLockError();
		}
	}
	
	private void showLockError() {
		String name = userManager.getUserDisplayName(lockEntry.getOwner());
		if(lockEntry.isDifferentWindows()) {
			showWarning("already.edited.same.user", name);
		} else {
			showWarning("already.edited", name);
		}
	}
	
	protected class FinishedCallback implements StepRunnerCallback {
		@Override
		public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
			
			InfoMessage msg = (InfoMessage)runContext.get(WizardConstants.MSG);
			File attachementsFolder = (File) runContext.get(WizardConstants.ATTACHEMENTS);
			msg.setAttachmentPath(infoMessageManager.storeAttachment(attachementsFolder, msg.getAttachmentPath(), msg.getOLATResourceable(), getIdentity()));

			// reload object infoMessage, if it is in an editing process
			if (infoMessageManager.loadInfoMessage(msg.getKey()) != null) {
				String msgTitle = msg.getTitle();
				String msgMessage = msg.getMessage();
				String msgAttachmentPath = msg.getAttachmentPath();
				Date msgPublishDate = msg.getPublishDate();
				msg = infoMessageManager.loadInfoMessage(msg.getKey());
				// update necessary fields, which could've been altered
				msg.setTitle(msgTitle);
				msg.setMessage(msgMessage);
				msg.setAttachmentPath(msgAttachmentPath);
				msg.setPublishDate(msgPublishDate);
			}

			String notificationType = (String) runContext.get(WizardConstants.PUBLICATION_NOTIFICATION_TYPE);
			boolean isSubscribersSelected = runContext.get(WizardConstants.SEND_MAIL_SUBSCRIBERS) != null && (boolean) runContext.get(WizardConstants.SEND_MAIL_SUBSCRIBERS);
			@SuppressWarnings("unchecked")
			Set<String> selectedOptions = runContext.get(WizardConstants.SEND_MAIL) != null ? (Set<String>) runContext.get(WizardConstants.SEND_MAIL) : new HashSet<>();
			@SuppressWarnings("unchecked")
			Set<String> selectedGroupOptions = runContext.get(WizardConstants.SEND_GROUPS) != null ? (Set<String>) runContext.get(WizardConstants.SEND_GROUPS) : new HashSet<>();
			@SuppressWarnings("unchecked")
			Set<String> selectedCurriculumOptions = runContext.get(WizardConstants.SEND_CURRICULA) != null ? (Set<String>) runContext.get(WizardConstants.SEND_CURRICULA) : new HashSet<>();
			@SuppressWarnings("unchecked")
			Collection<String> fileNamesToDelete = (Set<String>)runContext.get(WizardConstants.PATH_TO_DELETE);
			
			// Delete files, which should be deleted
			if (fileNamesToDelete != null && !fileNamesToDelete.isEmpty()) {
				Collection<String> pathsToDelete = new HashSet<>();
	
				for (String fileName : fileNamesToDelete) {
					pathsToDelete.add(msg.getAttachmentPath() + "/" + fileName);
				}
				
				infoMessageManager.deleteAttachments(pathsToDelete);
			}

			// if individual date was set, save it into the infoMessage object
			if (runContext.get(WizardConstants.PUBLICATION_DATE_TYPE) == WizardConstants.PUBLICATION_DATE_SELECT_INDIVIDUAL) {
				DateChooser publishDate = (DateChooser) runContext.get(WizardConstants.PUBLICATION_DATE);
				if (publishDate.getDate().after(new Date())) {
					msg.setPublishDate(publishDate.getDate());
					msg.setPublished(false);
				}
			} else {
				// if not, then "immediately" was selected, so directly publish it with current date
				msg.setPublishDate(new Date());
				msg.setPublished(true);
			}

			// notificationType can only be null, when wizard gets finished in first step
			// And altering this area is only necessary for sending out mails, if that was selected
			if (notificationType != null && notificationType.equals(SendMailStepController.SEND_TO_SUBS_AND_MAILS)) {
				StringBuilder sendMailTo = null;
				// Set, so identities which are included e.g. in a group and a curriculum should not be added twice or more
				Set<Identity> identities = new HashSet<>();

				// Subscribers
				if (isSubscribersSelected) {
					identities.addAll(sendSubscriberOption.getSelectedIdentities());
					sendMailTo = new StringBuilder(sendSubscriberOption.getOptionKey());
				}

				// Course members
				for (SendMailOption option : sendMailOptions) {
					if (selectedOptions.contains(option.getOptionKey())) {
						identities.addAll(option.getSelectedIdentities());
						if (sendMailTo != null && !sendMailTo.toString().contains(option.getOptionKey())) {
							sendMailTo.append(",").append(option.getOptionKey());
						} else if (sendMailTo == null) {
							sendMailTo = new StringBuilder(option.getOptionKey());
						}
					}
				}
				msg.setSendMailTo(sendMailTo != null ? sendMailTo.toString() : null);

				// group members
				for (SendMailOption option : groupsMailOptions) {
					if (selectedGroupOptions != null && selectedGroupOptions.contains(option.getOptionKey())) {
						identities.addAll(option.getSelectedIdentities());
					}
				}

				// curriculum members
				for (SendMailOption option : curriculaMailOptions) {
					if (selectedCurriculumOptions != null && selectedCurriculumOptions.contains(option.getOptionKey())) {
						identities.addAll(option.getSelectedIdentities());
					}
				}

				if (msg.isPublished()) {
					infoMessageManager.sendInfoMessage(msg, sendMailFormatter, ureq.getLocale(), ureq.getIdentity(), identities);
				} else {
					infoMessageManager.saveInfoMessage(msg);
				}

				// create link entries between infoMessage and groups
				Set<InfoMessageToGroup> infoMessageToGroups = msg.getGroups() != null ? msg.getGroups() : new HashSet<>();
				// check if group already is saved for given message, if not then create an entry
				for (SendMailOption option : groupsMailOptions) {
					if (selectedGroupOptions.contains(option.getOptionKey())
							&& (option instanceof SendMailGroupOption groupOption)
							&& (infoMessageToGroups.stream().noneMatch(ig -> ig.getBusinessGroup().equals(groupOption.getBusinessGroup())))) {
						infoMessageManager.createInfoMessageToGroup(msg, groupOption.getBusinessGroup());
					}
				}
				// if group gets deselected, delete connection to infoMessage
				if (!infoMessageToGroups.isEmpty()) {
					for (InfoMessageToGroup infoGroup : infoMessageToGroups) {
						if (!selectedGroupOptions.contains("send-mail-group-" + infoGroup.getBusinessGroup().getKey().toString())) {
							infoMessageManager.deleteInfoMessageToGroup(infoGroup);
						}
					}
				}

				// create link entries between infoMessage and curricula
				Set<InfoMessageToCurriculumElement> infoMessageToCurriculumElements = msg.getCurriculumElements() != null ? msg.getCurriculumElements() : new HashSet<>();
				// check if curriculumElement already is saved for given message, if not then create an entry
				for (SendMailOption option : curriculaMailOptions) {
					if (selectedCurriculumOptions.contains(option.getOptionKey())
							&& (option instanceof SendMailCurriculumOption curriculumOption
							&& (infoMessageToCurriculumElements.stream().noneMatch(g -> g.getCurriculumElement().equals(curriculumOption.getCurriculumElement()))))) {
						infoMessageManager.createInfoMessageToCurriculumElement(msg, curriculumOption.getCurriculumElement());
					}
				}
				// if curriculumElement gets deselected, delete connection to infoMessage
				if (!infoMessageToCurriculumElements.isEmpty()) {
					for (InfoMessageToCurriculumElement infoCurEl : infoMessageToCurriculumElements) {
						if (!selectedCurriculumOptions.contains("send-mail-curriculum-" + infoCurEl.getCurriculumElement().getKey().toString())) {
							infoMessageManager.deleteInfoMessageToCurriculumElement(infoCurEl);
						}
					}
				}
			} else {
				infoMessageManager.saveInfoMessage(msg);
			}

			ThreadLocalUserActivityLogger.log(CourseLoggingAction.INFO_MESSAGE_CREATED, getClass(),
					LoggingResourceable.wrap(msg.getOLATResourceable(), OlatResourceableType.infoMessage));

			return StepsMainRunController.DONE_MODIFIED;
		}
	}
	
	protected class CancelCallback implements StepRunnerCallback {
		@Override
		public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
			return Step.NOSTEP;
		}
	}
	
	private class AttachmentMapper implements Mapper {
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			if(infoKeysToAttachment == null) {
				return new NotFoundMediaResource();
			}
			
			String path; 
			
			if (relPath.startsWith("/")) {
				path = relPath.substring(1);
			} else {
				path = relPath;
			}
			
			if(StringHelper.containsNonWhitespace(path)) {
				try {
					VFSLeaf attachment = infoKeysToAttachment.get(path);
					return new VFSMediaResource(attachment);	
				} catch (NumberFormatException e) {
					//ignore them
				}
			}
			return new NotFoundMediaResource();
		}
	}
}
