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

package org.olat.commons.info.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.olat.commons.info.InfoMessage;
import org.olat.commons.info.InfoMessageFrontendManager;
import org.olat.commons.info.manager.MailFormatter;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.date.DateComponentFactory;
import org.olat.core.gui.components.date.DateElement;
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
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
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
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoDisplayController extends FormBasicController {

	private Step start;
	private FormLink newInfoLink;
	private FormLink oldMsgsLink;
	private FormLink newMsgsLink;
	private final List<FormLink> editLinks = new ArrayList<>();
	private final List<FormLink> deleteLinks = new ArrayList<>();
	private StepsMainRunController newInfoWizard;
	private DialogBoxController confirmDelete;
	private InfoEditController editController;
	private CloseableModalController editDialogBox;
	
	private final List<Long> previousDisplayKeys = new ArrayList<>();
	private final InfoSecurityCallback secCallback;
	private final OLATResourceable ores;
	private final String resSubPath;
	private final String businessPath;
	private final String thumbnailMapper;
	private final String attachmentMapper;
	private Map<Long,VFSLeaf> infoKeyToAttachment;
	
	private int maxResults = 0;
	private int maxResultsConfig = 0;
	private Date after = null;
	private Date afterConfig = null;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private InfoMessageFrontendManager infoMessageManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	private LockResult lockEntry;
	private MailFormatter sendMailFormatter;
	private List<SendMailOption> sendMailOptions = new ArrayList<>();
	private List<SendMailOption> groupsMailOptions = new ArrayList<>();
	private List<SendMailOption> curriculaMailOptions = new ArrayList<>();
	
	public InfoDisplayController(UserRequest ureq, WindowControl wControl, InfoSecurityCallback secCallback,
			BusinessGroup businessGroup, String resSubPath, String businessPath) {
		super(ureq, wControl, "display");
		this.secCallback = secCallback;
		this.ores = businessGroup.getResource();
		this.resSubPath = resSubPath;
		this.businessPath = businessPath;
		// default show 10 messages for groups
		maxResults = maxResultsConfig = 10;
		thumbnailMapper = registerCacheableMapper(ureq, "InfoMessagesThumbnail", new ThumbnailMapper());
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
		
		thumbnailMapper = registerCacheableMapper(ureq, "InfoMessagesThumbnail", new ThumbnailMapper());
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

	public List<SendMailOption> getSendMailOptions() {
		return sendMailOptions;
	}
	
	public void addSendMailOptions(SendMailOption sendMailOption) {
		sendMailOptions.add(sendMailOption);
	}
	
	public List<SendMailOption> getGroupMailOptions() {
		return groupsMailOptions;
	}
	
	public void addGroupMailOption(SendMailOption sendMailOption) {
		groupsMailOptions.add(sendMailOption);
	}
	
	public List<SendMailOption> getCurriculaSendOptions() {
		return curriculaMailOptions;
	}
	
	public void addCurriuclaMailOptions(SendMailOption sendMailOption) {
		curriculaMailOptions.add(sendMailOption);
	}
	
	public MailFormatter getSendMailFormatter() {
		return sendMailFormatter;
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
		Map<Long,VFSLeaf> keyToDisplay = new HashMap<>();
		for(InfoMessage info:msgs) {
			previousDisplayKeys.add(info.getKey());
			InfoMessageForDisplay infoDisplay = createInfoMessageForDisplay(info);
			infoDisplays.add(infoDisplay);
			if(infoDisplay.getAttachment() != null) {
				keyToDisplay.put(info.getKey(), infoDisplay.getAttachment());
			}
			
			String dateCmpName = "info.date." + info.getKey();
			DateElement dateEl = DateComponentFactory.createDateElementWithYear(dateCmpName, info.getCreationDate());
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
		infoKeyToAttachment = keyToDisplay;

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
			message =	Formatter.formatURLsAsLinks(message);
		}
		
		Formatter formatter = Formatter.getInstance(getLocale());
		
		String modifier = null;
		if(info.getModifier() != null) {
			String formattedName = userManager.getUserDisplayName(info.getModifier());
			String creationDate = formatter.formatDateAndTime(info.getModificationDate());
			modifier = translate("display.modifier", new String[]{StringHelper.escapeHtml(formattedName), creationDate});
		}

		String authorName = userManager.getUserDisplayName(info.getAuthor());
		String creationDate = formatter.formatDateAndTime(info.getCreationDate());
		String infos;
		if (authorName.isEmpty()) {
			infos = translate("display.info.noauthor", new String[]{creationDate});
		} else {
			infos = translate("display.info", new String[]{StringHelper.escapeHtml(authorName), creationDate});
		}
		VFSLeaf attachment = infoMessageManager.getAttachment(info);
		return new InfoMessageForDisplay(info.getKey(), info.getTitle(), message, attachment, infos, modifier);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(secCallback.canAdd()) {
			newInfoLink = uifactory.addFormLink("new_message", "new_message", "new_message", formLayout, Link.BUTTON);
			newInfoLink.setElementCssClass("o_sel_course_info_create_msg");
		}
		
		oldMsgsLink = uifactory.addFormLink("display.old_messages", "display.old_messages", "display.old_messages", formLayout, Link.BUTTON);
		oldMsgsLink.setElementCssClass("o_sel_course_info_old_msgs");
		newMsgsLink = uifactory.addFormLink("display.new_messages", "display.new_messages", "display.new_messages", formLayout, Link.BUTTON);
		newMsgsLink.setElementCssClass("o_sel_course_info_new_msgs");
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("thumbnailMapper", thumbnailMapper);
			layoutCont.contextPut("attachmentMapper", attachmentMapper);
		}
	}
	
	@Override
	protected void doDispose() {
		if(lockEntry != null) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
			lockEntry = null;
		}
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
		} else if (source == editController) {
			if(event == Event.DONE_EVENT) {
				loadMessages();
			}
			editDialogBox.deactivate();
			removeAsListenerAndDispose(editController);
			editDialogBox = null;
			editController = null;
			
			//release lock
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
			lockEntry = null;
		} else if (source == editDialogBox) {
			//release lock if the dialog is closed
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
			
			start = new CreateInfoStep(ureq, sendMailOptions, groupsMailOptions, curriculaMailOptions, msg);
			newInfoWizard = new StepsMainRunController(ureq, getWindowControl(), start, new FinishedCallback(),
					new CancelCallback(), translate("create_message"), "o_sel_info_messages_create_wizard");
			listenTo(newInfoWizard);
			getWindowControl().pushAsModalDialog(newInfoWizard.getInitialComponent());
		} else if(deleteLinks.contains(source)) {
			InfoMessage msg = (InfoMessage)source.getUserObject();
			popupDelete(ureq, msg);
		} else if(editLinks.contains(source)) {
			InfoMessage msg = (InfoMessage)source.getUserObject();
			popupEdit(ureq, msg);
		} else if(source == oldMsgsLink) {
			maxResults = -1;
			after = null;
			loadMessages();
		}  else if(source == newMsgsLink) {
			maxResults = maxResultsConfig;
			after = afterConfig;
			loadMessages();
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//nothing to do
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
	
	protected void popupEdit(UserRequest ureq, InfoMessage msg) {
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
				removeAsListenerAndDispose(editController);
				removeAsListenerAndDispose(editDialogBox);
				editController = new InfoEditController(ureq, getWindowControl(), msg);
				listenTo(editController);
				editDialogBox = new CloseableModalController(getWindowControl(), translate("edit"),
						editController.getInitialComponent(), true, translate("edit.title"), true);
				editDialogBox.activate();
				listenTo(editDialogBox);
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
			@SuppressWarnings("unchecked")
			Set<String> selectedOptions = (Set<String>)runContext.get(WizardConstants.SEND_MAIL);
			@SuppressWarnings("unchecked")
			Set<String> selectedGroupOptions = (Set<String>)runContext.get(WizardConstants.SEND_GROUPS);
			@SuppressWarnings("unchecked")
			Set<String> selectedCurriculumOptions = (Set<String>)runContext.get(WizardConstants.SEND_CURRICULA);
			@SuppressWarnings("unchecked")
			Collection<String> pathToDelete = (Set<String>)runContext.get(WizardConstants.PATH_TO_DELETE);

			List<Identity> identities = new ArrayList<>();
			for (SendMailOption option : sendMailOptions) {
				if (selectedOptions != null && selectedOptions.contains(option.getOptionKey())) {
					identities.addAll(option.getSelectedIdentities());
				}
			}
			
			for (SendMailOption option : groupsMailOptions) {
				if (selectedGroupOptions != null && selectedGroupOptions.contains(option.getOptionKey())) {
					identities.addAll(option.getSelectedIdentities());
				}
			}
			
			for (SendMailOption option : curriculaMailOptions) {
				if (selectedCurriculumOptions != null && selectedCurriculumOptions.contains(option.getOptionKey())) {
					identities.addAll(option.getSelectedIdentities());
				}
			}
			
			infoMessageManager.sendInfoMessage(msg, sendMailFormatter, ureq.getLocale(), ureq.getIdentity(), identities);
			infoMessageManager.deleteAttachments(pathToDelete);
			
			ThreadLocalUserActivityLogger.log(CourseLoggingAction.INFO_MESSAGE_CREATED, getClass(),
					LoggingResourceable.wrap(msg.getOLATResourceable(), OlatResourceableType.infoMessage));

			return StepsMainRunController.DONE_MODIFIED;
		}
	}
	
	protected class CancelCallback implements StepRunnerCallback {
		@Override
		public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
			@SuppressWarnings("unchecked")
			Collection<String> pathToDelete = (Set<String>)runContext.get(WizardConstants.PATH_TO_DELETE);
			infoMessageManager.deleteAttachments(pathToDelete);
			return Step.NOSTEP;
		}
	}
	
	private class AttachmentMapper implements Mapper {
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			if(infoKeyToAttachment == null) {
				return new NotFoundMediaResource();
			}
			
			String[] query = relPath.split("/");
			if(query.length > 1) {
				try {
					Long infoKey = Long.valueOf(Long.parseLong(query[1]));
					VFSLeaf attachment = infoKeyToAttachment.get(infoKey);
					return new VFSMediaResource(attachment);	
				} catch (NumberFormatException e) {
					//ignore them
				}
			}
			return new NotFoundMediaResource();
		}
	}
	
	private class ThumbnailMapper implements Mapper {
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			if(infoKeyToAttachment == null) {
				return new NotFoundMediaResource();
			}
			
			String[] query = relPath.split("/");
			if(query.length > 2) {
				try {
					Long infoKey = Long.valueOf(Long.parseLong(query[1]));
					VFSLeaf attachment = infoKeyToAttachment.get(infoKey);
					if(attachment != null && attachment.canMeta() == VFSConstants.YES) {
						VFSMetadata meta = attachment.getMetaInfo();
						if (meta.getUuid().equals(query[2])) {
							VFSLeaf thumb = vfsRepositoryService.getThumbnail(attachment, meta, 200, 200, false);
							if(thumb != null) {
								// Positive lookup, send as response
								return new VFSMediaResource(thumb);
							}
						}
					}	
				} catch (NumberFormatException e) {
					//ignore them
				}
			}
			return new NotFoundMediaResource();
		}
	}
}
