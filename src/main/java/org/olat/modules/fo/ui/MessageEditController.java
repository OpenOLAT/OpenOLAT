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
package org.olat.modules.fo.ui;

import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.PersistenceException;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityShort;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.DBRuntimeException;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSItemMetaFilter;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumCallback;
import org.olat.modules.fo.ForumChangedEvent;
import org.olat.modules.fo.ForumLoggingAction;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.Pseudonym;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.modules.fo.ui.events.ErrorEditMessage;
import org.olat.user.DisplayPortraitController;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Can be used for creating / editing or replying to a thread/post. editmode is
 * set to do internal mode-switch, where needed.
 * 
 * <P>
 * Initial Date: 18.06.2009 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, frentix GmbH
 */
public class MessageEditController extends FormBasicController {

	private static final String CMD_DELETE_ATTACHMENT = "delete.attachment.";
	protected static final Integer MAX_BODY_LENGTH = 32000;

	// see OLAT-4182/OLAT-4219 and OLAT-4259
	// the filtering of .nfs is sort of temporary until we make sure that we no longer reference
	// attached files anywhere at the time of deleting it
	// likely to be resolved after user logs out, caches get cleared - and if not the server
	// restart overnight definitely removes those .nfs files.
	private static final String[] enableKeys = new String[]{ "on" };
	
	private RichTextElement bodyEl;
	private TextElement titleEl, pseudonymEl, passwordEl;
	private MultipleSelectionElement usePseudonymEl;
	private FileElement fileUpload;

	
	private DisplayPortraitController portraitCtr;
	private DialogBoxController confirmDeleteAttachmentCtrl;
	
	private VFSContainer tempUploadFolder;
	private boolean userIsMsgCreator;
	private boolean msgHasChildren;
	private VFSItemMetaFilter exclFilter;

	private final Forum forum;
	private final EditMode editMode;
	private final boolean guestOnly;
	private String proposedPseudonym;
	private final ForumCallback foCallback;
	private Message message, parentMessage;

	@Autowired
	private ForumManager fm;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private NotificationsManager notificationsManager;
	
	public enum EditMode {
		newThread,
		edit,
		reply
	}

	/**
	 * 
	 * @param ureq
	 * @param control
	 * @param forumCallback
	 * @param message may be a new message created by ForumManager.createMessage() which is not yet saved in db
	 * @param quoteMessage may be null if Editor isn't used to reply to a message
	 */
	public MessageEditController(UserRequest ureq, WindowControl control, Forum forum, ForumCallback foCallback,
			Message message, Message parentMessage, EditMode mode) {
		super(ureq, control, FormBasicController.LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(Forum.class, getLocale(), getTranslator()));
		
		this.forum = forum;
		this.editMode = mode;
		this.message = message;
		this.foCallback = foCallback;
		this.parentMessage = parentMessage;
		this.guestOnly = ureq.getUserSession().getRoles().isGuestOnly();

		tempUploadFolder = new LocalFolderImpl(new File(WebappHelper.getTmpDir(), CodeHelper.getUniqueID()));
		exclFilter = new VFSItemMetaFilter();
		
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_forum_message_form");
		
		titleEl = uifactory.addTextElement("msgTitle", "msg.title", 100, message.getTitle(), formLayout);
		titleEl.setElementCssClass("o_sel_forum_message_title");
		titleEl.setMandatory(true);
		titleEl.setNotEmptyCheck("error.field.not.empty");
		bodyEl = uifactory.addRichTextElementForStringData("msgBody", "msg.body", message.getBody(), 15, -1, true, null, null,
				formLayout, ureq.getUserSession(), getWindowControl());
		bodyEl.setElementCssClass("o_sel_forum_message_body");
		bodyEl.setMandatory(true);
		bodyEl.setNotEmptyCheck("error.field.not.empty");
		bodyEl.setMaxLength(MAX_BODY_LENGTH);
		bodyEl.setNotLongerThanCheck(MAX_BODY_LENGTH, "input.toolong");
		bodyEl.getEditorConfiguration().enableCharCount();
		
		setEditPermissions(message);
		// list existing attachments. init attachment layout now, to place it in
		// right position
		createOrUpdateAttachmentListLayout(formLayout);

		// provide upload field
		if (foCallback.mayEditMessageAsModerator() || ((userIsMsgCreator) && (msgHasChildren == false))) {
			fileUpload = uifactory.addFileElement(getWindowControl(), "msg.upload", formLayout);
			fileUpload.addActionListener(FormEvent.ONCHANGE);
			fileUpload.setMaxUploadSizeKB((int) FolderConfig.getLimitULKB(), "attachments.too.big", new String[] { ((Long) (FolderConfig
					.getLimitULKB() / 1024)).toString() });
		}
		
		if(foCallback.mayUsePseudonym() || guestOnly) {
			String[] enablePseudonymValues = new String[]{ translate("use.pseudonym.label") };
			usePseudonymEl = uifactory.addCheckboxesHorizontal("use.pseudonym", formLayout, enableKeys, enablePseudonymValues);
			if(StringHelper.containsNonWhitespace(message.getPseudonym())
					|| guestOnly || foCallback.pseudonymAsDefault()) {
				usePseudonymEl.select(enableKeys[0], true);
			}
			pseudonymEl = uifactory.addTextElement("pseudonym", "pseudonym", 128, message.getPseudonym(), formLayout);
			pseudonymEl.setElementCssClass("o_sel_forum_message_alias");
			
			passwordEl = uifactory.addPasswordElement("password", "password", 128, "", formLayout);
			passwordEl.setElementCssClass("o_sel_forum_message_alias_pass");
			passwordEl.setPlaceholderKey("password.placeholder", null);
			passwordEl.setAutocomplete("new-password");

			if(guestOnly) {
				usePseudonymEl.setVisible(false);
				pseudonymEl.setLabel("use.pseudonym", null);
				pseudonymEl.setMandatory(true);
				proposedPseudonym = (String)ureq.getUserSession().getEntry("FOPseudo" + forum.getKey());
				if(StringHelper.containsNonWhitespace(proposedPseudonym)) {
					pseudonymEl.setValue(proposedPseudonym);
					String proposedPassword = (String)ureq.getUserSession().getEntry("FOPseudo-" + proposedPseudonym);
					if(StringHelper.containsNonWhitespace(proposedPassword)) {
						passwordEl.setValue(proposedPassword);
					}
				}
			} else if(userIsMsgCreator) {
				pseudonymEl.setLabel(null, null);
				usePseudonymEl.addActionListener(FormEvent.ONCHANGE);
				proposedPseudonym = fm.getPseudonym(forum, getIdentity());
				if(StringHelper.containsNonWhitespace(proposedPseudonym)) {
					pseudonymEl.setValue(proposedPseudonym);
					usePseudonymEl.select(enableKeys[0], true);
					String proposedPassword = (String)ureq.getUserSession().getEntry("FOPseudo-" + proposedPseudonym);
					if(StringHelper.containsNonWhitespace(proposedPassword)) {
						passwordEl.setValue(proposedPassword);
					}
				}
				usePseudonymEl.setMandatory(usePseudonymEl.isAtLeastSelected(1));
				pseudonymEl.setVisible(usePseudonymEl.isAtLeastSelected(1));
				passwordEl.setVisible(usePseudonymEl.isAtLeastSelected(1));
			} else {
				usePseudonymEl.setVisible(false);
				pseudonymEl.setLabel("use.pseudonym", null);
				pseudonymEl.setEnabled(false);
				passwordEl.setVisible(false);
			}
		}

		// save and cancel buttons
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("msg.save", buttonLayout);
		uifactory.addFormCancelButton("msg.cancel", buttonLayout, ureq, getWindowControl());

		// show message replying to, if in reply modus
		if (editMode == EditMode.reply) {
			String previewPage = Util.getPackageVelocityRoot(this.getClass()) + "/msg-preview.html";
			FormLayoutContainer replyMsgLayout = FormLayoutContainer.createCustomFormLayout("replyMsg", getTranslator(), previewPage);
			uifactory.addSpacerElement("spacer1", formLayout, false);
			formLayout.add(replyMsgLayout);
			
			replyMsgLayout.setLabel("label.replytomsg", new String[] { StringHelper.escapeHtml(parentMessage.getTitle()) });
			replyMsgLayout.contextPut("messageBody", parentMessage.getBody());
			replyMsgLayout.contextPut("message", parentMessage);
			replyMsgLayout.contextPut("guestOnly", new Boolean(guestOnly));

			Identity creator = parentMessage.getCreator();
			if(creator != null) {
				replyMsgLayout.contextPut("identity", creator);
				portraitCtr = new DisplayPortraitController(ureq, getWindowControl(), creator, true, true);
				replyMsgLayout.put("portrait", portraitCtr.getInitialComponent());
			}
		}
	}

	private void setEditPermissions(Message msg){
		// defaults for a new message
		userIsMsgCreator = true;
		msgHasChildren = false;
		// set according to message
		if (msg.getKey() != null) {
			if(msg.getCreator() != null) {
				
			} else {
				userIsMsgCreator = getIdentity().equals(msg.getCreator());
				msgHasChildren = fm.hasChildren(msg);
			}
		}
	}
	
	// adds or updates the list of already existing attachments with a delete
	// button for each
	private void createOrUpdateAttachmentListLayout(FormItemContainer formLayout) {
		FormItem attachLayout = formLayout.getFormComponent("attachLayout");

		List<VFSItem> attachments = new ArrayList<VFSItem>();
		// add already existing attachments:
		if (message.getKey() != null) {
			VFSContainer msgContainer = fm.getMessageContainer(message.getForum().getKey(), message.getKey());
			attachments.addAll(msgContainer.getItems(exclFilter));
		}
		// add files from TempFolder
		attachments.addAll(getTempFolderFileList());
		
		Collections.sort(attachments, new Comparator<VFSItem>(){
			final Collator c = Collator.getInstance(getLocale());
			public int compare(final VFSItem o1, final VFSItem o2) {
				return c.compare((o1).getName(), (o2).getName());
			}});		
		
		FormLayoutContainer tmpLayout;
		if (attachLayout == null) {
			String editPage = Util.getPackageVelocityRoot(this.getClass()) + "/attachments-editview.html";
			tmpLayout = FormLayoutContainer.createCustomFormLayout("attachLayout", getTranslator(), editPage);
			formLayout.add(tmpLayout);
		} else {
			tmpLayout = (FormLayoutContainer) attachLayout;
		}
		tmpLayout.contextPut("attachments", attachments);

		// add delete links for each attachment if user is allowed to see them
		int attNr = 1;
		for (VFSItem tmpFile : attachments) {
			FormLink tmpLink = uifactory.addFormLink(CMD_DELETE_ATTACHMENT + attNr, tmpLayout, Link.BUTTON_XSMALL);
			if (!(foCallback.mayEditMessageAsModerator() || ((userIsMsgCreator) && (msgHasChildren == false)))) {
				tmpLink.setEnabled(false);  
				tmpLink.setVisible(false);
			}
			tmpLink.setUserObject(tmpFile);
			tmpLink.setI18nKey("attachments.remove.string");
			attNr++;
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#doDispose()
	 */
	@Override
	protected void doDispose() {
		removeTempUploadedFiles();
		if (portraitCtr != null) {
			portraitCtr.dispose();
			portraitCtr = null;
		}
		if (confirmDeleteAttachmentCtrl != null) {
			confirmDeleteAttachmentCtrl.dispose();
			confirmDeleteAttachmentCtrl = null;
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		if(usePseudonymEl != null) {
			pseudonymEl.clearError();
			passwordEl.clearError();
			if(guestOnly || usePseudonymEl.isAtLeastSelected(1)) {
				String pseudonym = pseudonymEl.getValue();
				String password = passwordEl.getValue();
				
				if(!StringHelper.containsNonWhitespace(pseudonym)) {
					pseudonymEl.setErrorKey("form.legende.mandatory", null);
					allOk &= false;
				} else if(!validatePseudonym(pseudonym)) {
					pseudonymEl.setErrorKey("error.pseudonym", null);
					allOk &= false;
				} else if(!validatePseudonymProtected(pseudonym, password)) {
					allOk &= false;
				}
			}
		}
		return allOk & super.validateFormLogic(ureq);
	}
	
	private boolean validatePseudonym(String value) {
		boolean allOk = true;
		
		if(proposedPseudonym == null || !proposedPseudonym.equalsIgnoreCase(value)) {
			List<IdentityShort> sameValues = securityManager.searchIdentityShort(value, 250);
			if(sameValues.size() == 1) {
				allOk &= !sameValues.get(0).getKey().equals(getIdentity().getKey());
			} else if(sameValues.size() > 1) {
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	/**
	 * No password:
	 * <ul>
	 *  <li>exists pseudonym with password: error</li>
	 *  <li>doesn't exist pseudonym with passwort -> can use the pseudonym</li>
	 * </ul>
	 * With password:
	 * <ul>
	 *  <li>exists pseudonym with password + password wrong: error</li>
	 *  <li>exists pseudonym with password + password ok: ok</li>
	 *  <li>exists pseudonym with password + password wrong: error</li>
	 * </ul>
	 * 
	 * @param value
	 * @param password
	 * @return
	 */
	private boolean validatePseudonymProtected(String value, String password) {
		boolean allOk = true;
		
		if(StringHelper.containsNonWhitespace(password)) {
			List<Pseudonym> pseudonyms = fm.getPseudonyms(value);
			if(pseudonyms.size() > 0) {
				boolean authenticated = false;
				for(Pseudonym pseudonym:pseudonyms) {
					if(fm.authenticatePseudonym(pseudonym, password)) {
						authenticated = true;
						break;
					}
				}
				
				if(!authenticated) {
					passwordEl.setErrorKey("error.pseudonym.authentication", null);
					allOk &= false;
				}
			} else if(fm.isPseudonymInUseInForums(value)) {
				pseudonymEl.setErrorKey("error.pseudonym", null);
				allOk &= false;
			}
		} else if(fm.isPseudonymProtected(value)) {
			pseudonymEl.setErrorKey("error.pseudonym.protected", null);
			allOk &= false;
		}

		return allOk;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		try {
			commitMessage(ureq);
			fireEvent(ureq, Event.DONE_EVENT);
		} catch(DBRuntimeException | PersistenceException e) {
			DBFactory.getInstance().rollback();
			logError("", e);
			fireEvent(ureq, new ErrorEditMessage());
		}
	}
	
	private void commitMessage(UserRequest ureq) {
		// if msg exist -> persist uploads directly to final dest
		if (message.getKey() != null) {
			message = fm.loadMessage(message.getKey());
		}

		// set values from form to message
		message.setTitle(titleEl.getValue());
		String body = bodyEl.getValue();
		body = body.replace("<p>&nbsp;", "<p>");

		message.setBody(body.trim());
		if(usePseudonymEl != null && (usePseudonymEl.isAtLeastSelected(1) || guestOnly)) {
			String password = passwordEl.getValue();
			String pseudonym = pseudonymEl.getValue();
			if(StringHelper.containsNonWhitespace(password)) {
				List<Pseudonym> protectedPseudonyms = fm.getPseudonyms(pseudonym);
				if(protectedPseudonyms.isEmpty()) {
					fm.createProtectedPseudonym(pseudonym, password);
					ureq.getUserSession().putEntry("FOPseudo-" + pseudonym, password);
				} else {
					//we double check the password
					boolean authenticated = false;
					for(Pseudonym protectedPseudonym:protectedPseudonyms) {
						if(fm.authenticatePseudonym(protectedPseudonym, password)) {
							ureq.getUserSession().putEntry("FOPseudo-" + protectedPseudonym.getPseudonym(), password);
							authenticated = true;
							break;
						}
					}
					
					if(!authenticated) {
						validateFormLogic(ureq);
						return;
					}
				}
			}
			message.setPseudonym(pseudonym);
			if(guestOnly) {
				ureq.getUserSession().putEntry("FOPseudo" + forum.getKey(), message.getPseudonym());
			}
		} else if(message.getCreator() != null && message.getCreator().equals(getIdentity())) {
			message.setPseudonym(null);
		}

		if(editMode == EditMode.newThread) {
			if(foCallback.mayOpenNewThread()) {
				// save a new thread
				message = fm.addTopMessage(message);
				fm.markNewMessageAsRead(getIdentity(), forum, message);
				persistTempUploadedFiles(message);
				// if notification is enabled -> notify the publisher about news
				notifiySubscription();
				addLoggingResourceable(LoggingResourceable.wrap(message));
				//commit before sending events
				DBFactory.getInstance().commit();
				ForumChangedEvent event = new ForumChangedEvent(ForumChangedEvent.NEW_MESSAGE, message.getKey(), message.getKey(), getIdentity());
				CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(event, forum);	
				ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_MESSAGE_CREATE, getClass());
			} else {
				showWarning("may.not.save.msg.as.author");
			}

		} else if(editMode == EditMode.edit) { 
			boolean children = fm.countMessageChildren(message.getKey()) > 0;
			if (foCallback.mayEditMessageAsModerator() || (userIsMsgCreator && !children)) {
				message.setModifier(getIdentity());	
				message = fm.updateMessage(message, true);
				persistTempUploadedFiles(message);
				notifiySubscription();
				//commit before sending events
				DBFactory.getInstance().commit();
				Long threadTopKey = message.getThreadtop() == null ? null : message.getThreadtop().getKey();
				ForumChangedEvent event = new ForumChangedEvent(ForumChangedEvent.CHANGED_MESSAGE, threadTopKey, message.getKey(), getIdentity());
				CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(event, forum);
				ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_MESSAGE_EDIT, getClass(),
						LoggingResourceable.wrap(message));
			} else {
				showWarning("may.not.save.msg.as.author");
			}
		} else if(editMode == EditMode.reply) { 
			message = fm.replyToMessage(message, parentMessage);
			fm.markNewMessageAsRead(getIdentity(), forum, message);
			persistTempUploadedFiles(message);
			notifiySubscription();
			Long threadTopKey = message.getThreadtop() == null ? null : message.getThreadtop().getKey();

			//commit before sending events
			DBFactory.getInstance().commit();
			ForumChangedEvent event = new ForumChangedEvent(ForumChangedEvent.NEW_MESSAGE, threadTopKey, message.getKey(), getIdentity());
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(event, forum);	
			ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_REPLY_MESSAGE_CREATE, getClass(),
					LoggingResourceable.wrap(message));
		}
	}
	
	private void notifiySubscription() {
		if (foCallback.getSubscriptionContext() != null) {
			notificationsManager.markPublisherNews(foCallback.getSubscriptionContext(), getIdentity(), true);
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		// remove uploaded files if editing is canceled
		removeTempUploadedFiles();
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(usePseudonymEl == source) {
			usePseudonymEl.setMandatory(usePseudonymEl.isAtLeastSelected(1));
			pseudonymEl.setVisible(usePseudonymEl.isAtLeastSelected(1));
			passwordEl.setVisible(usePseudonymEl.isAtLeastSelected(1));
		} else if (source == fileUpload) {
			if (fileUpload.isUploadSuccess()) {
				String fileName = fileUpload.getUploadFileName();
				if (fileUpload.getUploadSize() / 1024 < fileUpload.getMaxUploadSizeKB()) {

					// checking tmp-folder and msg-container for filename
					boolean fileExists = false;
					if (getTempFolderFileList().contains(fileName)) {
						fileExists = true;
					}
					if (message.getKey() != null) {
						VFSContainer msgContainer = fm.getMessageContainer(message.getForum().getKey(), message.getKey());
						if (msgContainer.resolve(fileName) != null) {
							fileExists = true;
						}
					}

					if (fileExists) {
						fileUpload.setErrorKey("attachments.error.file.exists", null);
						fileUpload.getUploadFile().delete();
						fileUpload.showError(true);
					} else {
						// files got stored in an extra tempFolder, to use the same
						// fileUploader multiple times
						fileUpload.moveUploadFileTo(tempUploadFolder);
						fileUpload.showError(false);
						fileUpload.reset();

						createOrUpdateAttachmentListLayout(this.flc);
						showInfo("attachments.upload.successful", fileName);
					}
				} else {
					fileUpload.setErrorKey("attachments.too.big", new String[] { Long.toString((fileUpload.getMaxUploadSizeKB() / 1024)) });
					fileUpload.getUploadFile().delete();
					fileUpload.showError(true);
				}
			}
		} else if (source instanceof FormLink) {
			FormLink activeLink = (FormLink) source;
			// attachment delete button may have been pressed
			Object userObj = activeLink.getUserObject();
			if (userObj != null) {
				setEditPermissions(message);
				if (userObj instanceof VFSLeaf) {
					VFSLeaf file = (VFSLeaf) userObj;
					if (foCallback.mayEditMessageAsModerator() || (userIsMsgCreator && !msgHasChildren)) {
						confirmDeleteAttachmentCtrl = activateYesNoDialog(ureq, null, translate("reallydeleteAtt"), confirmDeleteAttachmentCtrl);
						confirmDeleteAttachmentCtrl.setUserObject(file);
					} else {
						if ((userIsMsgCreator) && (msgHasChildren == true)) {
							// user is author of the current message but it has already at
							// least one child
							showWarning("may.not.delete.att.as.author");
						} else {
							// user isn't author of the current message
							showInfo("may.not.delete.att");
						}
					}
				}
			}
		}
	}

	private List<VFSItem> getTempFolderFileList() {
		if (tempUploadFolder == null) {
			tempUploadFolder = new OlatRootFolderImpl(File.separator + "tmp/" + CodeHelper.getGlobalForeverUniqueID() + "/", null);
		}		
		return tempUploadFolder.getItems(exclFilter);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == confirmDeleteAttachmentCtrl) {
			if (DialogBoxUIFactory.isYesEvent(event)) { // ok to really delete this																					// attachment
				Object userObj = confirmDeleteAttachmentCtrl.getUserObject();
				if (userObj instanceof VFSLeaf) {
					((VFSLeaf)userObj).delete();
					showInfo("delete.att.ok");
					createOrUpdateAttachmentListLayout(flc);
				}
			}
		}
	}

	/**
	 * Used to get the message edited right before. the new values got saved to it
	 * locally by formOK()
	 * 
	 * @return the edited message
	 */
	public Message getMessage() {
		return message;
	}

	/**
	 * gives back the mode in which the editor was (create/edit/reply)
	 * 
	 * @return editMode which can be matched against static Strings from this
	 *         class
	 */
	public EditMode getLastEditModus() {
		return editMode;
	}

	/**
	 * - used locally if in edit mode where the msg-key is known 
	 * - called from ForumController after creating a thread or a reply to copy temp files to
	 * msg-folder
	 * 
	 * @param tmpMessage
	 */
	public void persistTempUploadedFiles(Message tmpMessage) {
		if (tmpMessage == null) throw new AssertException("Message may not be null to persist temp files");
		VFSContainer msgContainer = fm.getMessageContainer(forum.getKey(), message.getKey());
		if (msgContainer != null) {
			List<VFSItem> tmpFList = getTempFolderFileList();
			for (VFSItem file : tmpFList) {
				VFSLeaf leaf = (VFSLeaf) file;
				try {
					FileUtils.bcopy(
							leaf.getInputStream(),
							msgContainer.createChildLeaf(leaf.getName()).getOutputStream(false),
							"forumSaveUploadedFile"
					);
				} catch (IOException e) {
					removeTempUploadedFiles();
					throw new RuntimeException ("I/O error saving uploaded file:" + msgContainer + "/" + leaf.getName());
				}
			}
		}
		removeTempUploadedFiles();
	}

	private void removeTempUploadedFiles() {
		if (tempUploadFolder != null) {
			tempUploadFolder.delete();
			tempUploadFolder = null;
		}
	}
}
