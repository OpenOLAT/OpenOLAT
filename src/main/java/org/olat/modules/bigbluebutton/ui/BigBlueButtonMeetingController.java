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
package org.olat.modules.bigbluebutton.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSLeafButSystemFilter;
import org.olat.modules.bigbluebutton.BigBlueButtonAttendee;
import org.olat.modules.bigbluebutton.BigBlueButtonAttendeeRoles;
import org.olat.modules.bigbluebutton.BigBlueButtonDispatcher;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.BigBlueButtonRecording;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingReference;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingsHandler;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingsPublishedRoles;
import org.olat.modules.bigbluebutton.manager.AvatarMapper;
import org.olat.modules.bigbluebutton.manager.SlidesContainerMapper;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.modules.bigbluebutton.model.BigBlueButtonRecordingWithReference;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonRecordingTableModel.BRecordingsCols;
import org.olat.user.DisplayPortraitManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonMeetingController extends FormBasicController implements GenericEventListener {

	private static final int EXPIRATION_TIME = 5 * 60 * 60;// 10 minutes
	
	private String avatarUrl;
	private final boolean readOnly;
	private final boolean moderator;
	private final boolean administrator;
	private BigBlueButtonMeeting meeting;
	
	private int count = 0;
	private final boolean guest;
	private final boolean moderatorStartMeeting;
	private final OLATResourceable meetingOres;

	private boolean withTools = false;
	private boolean withPublish = false;

	private FormLink joinButton;
	private FormLink uploadButton;
	private FormLink guestJoinButton;
	private MultipleSelectionElement acknowledgeRecordingEl;
	private FlexiTableElement tableEl;
	private BigBlueButtonRecordingTableModel recordingTableModel;
	
	private SlidesContainerMapper slidesMapper; 

	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private SlideUploadController uploadSlideCtrl;
	private PublishRecordingController publishCtrl;
	private DialogBoxController confirmDeleteRecordingDialog;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private CloseableCalloutWindowController publishCalloutCtrl;

	@Autowired
	private TaskExecutorManager taskExecutorManager;
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	@Autowired
	private DisplayPortraitManager displayPortraitManager;
	
	public BigBlueButtonMeetingController(UserRequest ureq, WindowControl wControl,
			BigBlueButtonMeeting meeting, BigBlueButtonMeetingDefaultConfiguration configuration,
			boolean administrator, boolean moderator, boolean readOnly) {
		super(ureq, wControl, "meeting");
		this.meeting = meeting;
		this.readOnly = readOnly;
		this.moderator = moderator;
		this.administrator = administrator;
		UserSession usess = ureq.getUserSession();
		guest = usess.getRoles().isGuestOnly();
		meetingOres = OresHelper.createOLATResourceableInstance(BigBlueButtonMeeting.class.getSimpleName(), meeting.getKey());
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), meetingOres);
		moderatorStartMeeting = configuration.isModeratorStartMeeting();
		
		initAvatarUrl();
		initForm(ureq);
		updateButtonsAndStatus();
		loadRecordingsModel();
		
		if(guest) {
			usess.putEntryInNonClearedStore("meeting-" + meeting.getKey(), Boolean.TRUE);
		}
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initJoinForm(formLayout);
		initRecordings(formLayout);
	}
	
	private void initAvatarUrl() {
		if(!bigBlueButtonModule.isAvatarEnabled()) return;
		
		File portraitFile = displayPortraitManager.getBigPortrait(getIdentity());
		if(portraitFile != null) {
			String rnd = "r" + getIdentity().getKey() + CodeHelper.getRAMUniqueID();
			avatarUrl = Settings.createServerURI()
					+ registerCacheableMapper(null, rnd, new AvatarMapper(portraitFile), EXPIRATION_TIME)
					+ "/" + portraitFile.getName();
		}
	}
	
	private void initJoinForm(FormItemContainer formLayout) {
		boolean ended = isEnded();
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("title", meeting.getName());
			String descr = meeting.getDescription();
			if(StringHelper.containsNonWhitespace(descr)) {
				if(!StringHelper.isHtml(descr)) {
					descr = Formatter.escWithBR(descr).toString();
				}
				layoutCont.contextPut("description", descr);
			}
			if(meeting.getStartDate() != null) {
				String start = Formatter.getInstance(getLocale()).formatDateAndTime(meeting.getStartDate());
				layoutCont.contextPut("start", start);
			}
			if(meeting.getEndDate() != null) {
				String end = Formatter.getInstance(getLocale()).formatDateAndTime(meeting.getEndDate());
				layoutCont.contextPut("end", end);
			}
			
			if((administrator || moderator) && StringHelper.containsNonWhitespace(meeting.getReadableIdentifier())) {
				String url = BigBlueButtonDispatcher.getMeetingUrl(meeting.getReadableIdentifier());
				layoutCont.contextPut("externalUrl", url);
				String password = meeting.getPassword();
				layoutCont.contextPut("externalPassword", password);
			}
			
			if(administrator || moderator) {
				loadSlides(layoutCont);
				uploadButton = uifactory.addFormLink("meeting.slides.upload", formLayout, Link.BUTTON_SMALL);
			}
			
			if(StringHelper.containsNonWhitespace(meeting.getMainPresenter())) {
				layoutCont.contextPut("mainPresenter", meeting.getMainPresenter());
			}
		}
		
		joinButton = uifactory.addFormLink("meeting.join.button", translate("meeting.join.button"), null,
				formLayout, Link.BUTTON | Link.NONTRANSLATED);
		joinButton.setElementCssClass("o_sel_bbb_join");
		joinButton.setNewWindow(true, true, true);
		joinButton.setVisible(!ended && !guest);
		joinButton.setTextReasonForDisabling(translate("warning.no.access"));
		
		guestJoinButton = uifactory.addFormLink("meeting.guest.join.button", formLayout, Link.BUTTON);
		guestJoinButton.setElementCssClass("btn btn-lg btn-default o_sel_bbb_guest_join");
		guestJoinButton.setNewWindow(true, true, true);
		guestJoinButton.setVisible(!ended && guest);
		
		SelectionValues acknowledgeKeyValue = new SelectionValues();
		acknowledgeKeyValue.add(SelectionValues.entry("agree", translate("meeting.acknowledge.recording.agree")));
		acknowledgeRecordingEl = uifactory.addCheckboxesHorizontal("meeting.acknowledge.recording", null, formLayout,
				acknowledgeKeyValue.keys(), acknowledgeKeyValue.values());
	}
	
	private void loadSlides(FormLayoutContainer layoutCont) {
		List<SlideWrapper> documentWrappers = new ArrayList<>();
		if(StringHelper.containsNonWhitespace(meeting.getDirectory())) {
			VFSContainer slidesContainer = bigBlueButtonManager.getSlidesContainer(meeting);
			if(slidesMapper == null) {
				slidesMapper = new SlidesContainerMapper(slidesContainer);
				String mapperUri = registerCacheableMapper(null, "BigBlueButtonSlides::" + meeting.getKey(), slidesMapper);
				layoutCont.contextPut("mapperUri", mapperUri);
			}
			
			if(slidesContainer != null && slidesContainer.exists()) {
				boolean slidesEditable = isSlidesEditable();
				List<VFSItem> items = slidesContainer.getItems(new VFSLeafButSystemFilter());
				for(VFSItem item:items) {
					if(item instanceof VFSLeaf) {
						VFSLeaf slide = (VFSLeaf)item;
						SlideWrapper wrapper = new SlideWrapper(slide, false);
						if(slidesEditable) {
							FormLink deleteButton = uifactory
									.addFormLink("delete_" + (++count), "delete", "delete", null, layoutCont, Link.BUTTON_XSMALL);
							deleteButton.setUserObject(wrapper);
							wrapper.setDeleteButton(deleteButton);
						}
						documentWrappers.add(wrapper);
					}
				}
			}
		}
		Collections.sort(documentWrappers);
		layoutCont.contextPut("documents", documentWrappers);
		
		boolean showWarning = meeting.isPermanent() && !documentWrappers.isEmpty();
		layoutCont.contextPut("uploadWarning", Boolean.valueOf(showWarning));
	}
	
	private void initRecordings(FormItemContainer formLayout) {
		BigBlueButtonRecordingsHandler recordingsHandler = bigBlueButtonManager.getRecordingsHandler();
			
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BRecordingsCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BRecordingsCols.type, new RecordingTypeCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BRecordingsCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BRecordingsCols.end));
		if(administrator && recordingsHandler.canDeleteRecordings() && recordingsHandler.allowPermanentRecordings()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BRecordingsCols.permanent));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.recording.open", BRecordingsCols.open.ordinal(), "open-recording",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("table.header.recording.open"), "open-recording", true, true), null)));
		
		if(administrator) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BRecordingsCols.publish));
			withPublish = true;
			if(recordingsHandler.canDeleteRecordings() && recordingsHandler.allowPermanentRecordings()) {
				StickyActionColumnModel toolsCol = new StickyActionColumnModel(BRecordingsCols.tools);
				toolsCol.setIconHeader("o_icon o_icon_actions o_icon-fws o_icon-lg");
				columnsModel.addFlexiColumnModel(toolsCol);
				withTools = true;
			} else if(recordingsHandler.canDeleteRecordings()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", BRecordingsCols.presentation.ordinal(), "delete",
						new BooleanCellRenderer(
								new StaticFlexiCellRenderer(translate("delete"), "delete"), null)));
			}
		}
		
		recordingTableModel = new BigBlueButtonRecordingTableModel(columnsModel, bigBlueButtonModule.isRecordingsPermanent(), getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "recordings", recordingTableModel, 24, false, getTranslator(), formLayout);
		tableEl.setEmptyTableMessageKey("no.recordings");
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCustomizeColumns(false);
	}
	
	private void loadRecordingsModel() {
		BigBlueButtonAttendee attendee = bigBlueButtonManager.getAttendee(getIdentity(), meeting);
		
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		List<BigBlueButtonRecordingWithReference> recordings = bigBlueButtonManager.getRecordingAndReferences(meeting, errors);
		List<BigBlueButtonRecordingRow> rows = new ArrayList<>(recordings.size());
		for(BigBlueButtonRecordingWithReference recording:recordings) {
			rows.add(forgeRow(recording, attendee));
		}
		recordingTableModel.setObjects(rows);
		tableEl.reset(true, true, true);
		flc.contextPut("hasRecordings", Boolean.valueOf(!recordings.isEmpty()));
		String recordingInfo = bigBlueButtonManager.getRecordingsHandler().getRecordingInfo(getLocale());
		flc.contextPut("recordingInfo", recordingInfo);
	}
	
	private BigBlueButtonRecordingRow forgeRow(BigBlueButtonRecordingWithReference recording, BigBlueButtonAttendee attendee) {
		boolean pusblished = isPublishedForMe(recording.getReference(), attendee);
		BigBlueButtonRecordingRow row = new BigBlueButtonRecordingRow(recording, pusblished);
		if(administrator || moderator) {
			String recId = recording.getRecording().getRecordId();
			String recordingType = recording.getRecording().getType();
			if(withPublish && (BigBlueButtonRecording.PRESENTATION.equals(recordingType)
					|| BigBlueButtonRecording.OPENCAST.equals(recordingType))) {
				FormLink publishLink = uifactory.addFormLink("publish-".concat(recId),
						"publish", "publish.recording", tableEl);
				row.setPublishLink(publishLink);
				publishLink.setUserObject(row);
			}
			
			if(withTools) {
				FormLink toolsLink = uifactory.addFormLink("tools-".concat(recId),
						"tools", "", tableEl, Link.LINK | Link.NONTRANSLATED);
				toolsLink.setAriaLabel(translate("table.header.actions"));
				toolsLink.setIconRightCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
				toolsLink.setUserObject(row);
				row.setToolsLink(toolsLink);
			}
		}
		return row;
	}
	
	private boolean isPublishedForMe(BigBlueButtonRecordingReference reference, BigBlueButtonAttendee attendee) {
		if(reference == null) return false;
		
		BigBlueButtonRecordingsPublishedRoles[] publishTo = reference.getPublishToEnum();
		if(BigBlueButtonRecordingsPublishedRoles.has(publishTo, BigBlueButtonRecordingsPublishedRoles.none)) {
			return false;
		}
		if(guest) {
			return BigBlueButtonRecordingsPublishedRoles.has(publishTo, BigBlueButtonRecordingsPublishedRoles.guest);
		}
		if(attendee != null && BigBlueButtonRecordingsPublishedRoles.has(publishTo, BigBlueButtonRecordingsPublishedRoles.all)) {
			return true;
		}
		if(administrator || moderator) {
			return BigBlueButtonRecordingsPublishedRoles.has(publishTo, BigBlueButtonRecordingsPublishedRoles.coach);
		}
		return BigBlueButtonRecordingsPublishedRoles.has(publishTo, BigBlueButtonRecordingsPublishedRoles.participant);
	}
	
	private boolean isEnded() {
		return meeting != null && meeting.getEndDate() != null && new Date().after(meeting.getEndDate());
	}
	
	private boolean isAccessible() {
		if(meeting == null) return false;
		if(meeting.isPermanent()) {
			return bigBlueButtonModule.isPermanentMeetingEnabled();
		}

		Date now = new Date();
		Date start = (administrator || moderator) ? meeting.getStartWithLeadTime() : meeting.getStartDate();
		Date end = meeting.getEndWithFollowupTime();
		return !((start != null && start.compareTo(now) >= 0) || (end != null && end.compareTo(now) <= 0));
	}
	
	private boolean isSlidesEditable() {
		if(meeting == null) return false;
		if(meeting.isPermanent()) {
			return true;
		}

		Date now = new Date();
		Date start = meeting.getStartDate();
		return start != null && start.compareTo(now) > 0;
	}
	
	private void reloadButtonsAndStatus() {
		meeting = bigBlueButtonManager.getMeeting(meeting);
		updateButtonsAndStatus();
		flc.setDirty(true);
	}
	
	private boolean isDisabled() {
		return meeting != null && meeting.getServer() != null && !meeting.getServer().isEnabled();
	}
	
	private void updateButtonsAndStatus() {
		boolean isEnded = isEnded();
		boolean accessible = isAccessible();
		boolean disabled = isDisabled();
		flc.contextPut("disabled", Boolean.valueOf(disabled));
		flc.contextPut("ended", Boolean.valueOf(isEnded));
		flc.contextPut("notStarted", Boolean.TRUE);
		// only change from invisible to visible
		if(!joinButton.isVisible()) {
			joinButton.setVisible(accessible && !disabled && !guest);
		}
		joinButton.setEnabled(!readOnly && accessible && !disabled && !guest);
		
		if(!guestJoinButton.isVisible()) {
			guestJoinButton.setVisible(accessible && !disabled && guest);
		}
		guestJoinButton.setEnabled(!readOnly && accessible && !disabled && guest);
		
		if(uploadButton != null) {
			boolean slidesEditable = isSlidesEditable();
			uploadButton.setVisible(slidesEditable);
		}
			
		if(accessible && !disabled) {
			boolean running = bigBlueButtonManager.isMeetingRunning(meeting);
			if(moderator || administrator) {
				flc.contextPut("notStarted", Boolean.FALSE);
				if(!running && moderatorStartMeeting) {
					joinButton.setI18nKey(translate("meeting.start.button"));
				} else {
					joinButton.setI18nKey(translate("meeting.join.button"));
				}
			} else if(!running && moderatorStartMeeting) {
				flc.contextPut("notStarted", Boolean.TRUE);
				joinButton.setEnabled(false);
				guestJoinButton.setEnabled(false);
			} else {
				flc.contextPut("notStarted", Boolean.FALSE);
				joinButton.setEnabled(!readOnly && !guest);
				guestJoinButton.setEnabled(!readOnly && guest);
			}
		} else if(isEnded) {
			flc.contextPut("notStarted", Boolean.FALSE);
		}

		// update button style to indicate that the user must now press to start
		if (guestJoinButton.isEnabled()) {
			guestJoinButton.setElementCssClass("btn btn-lg btn-primary o_sel_bbb_guest_join");			
		} else {			
			guestJoinButton.setElementCssClass("btn btn-lg btn-default o_sel_bbb_guest_join");			
		}
		joinButton.setPrimary(joinButton.isEnabled());
		acknowledgeRecordingEl.setVisible(BigBlueButtonUIHelper.isRecord(meeting)
				&& ((joinButton.isVisible() && joinButton.isEnabled())
						|| (guestJoinButton.isEnabled() && guestJoinButton.isVisible())));
	}

	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, meetingOres);
        super.doDispose();
	}

	@Override
	public void event(Event event) {
		if(event instanceof BigBlueButtonEvent) {
			BigBlueButtonEvent ace = (BigBlueButtonEvent)event;
			if(ace.getMeetingKey() != null && ace.getMeetingKey().equals(meeting.getKey())) {
				reloadButtonsAndStatus();
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmDeleteRecordingDialog == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				BigBlueButtonRecording recording = (BigBlueButtonRecording)confirmDeleteRecordingDialog.getUserObject();
				doDeleteRecording(recording);
			}
			cleanUp();
		} else if(publishCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadRecordingsModel();
			}
			publishCalloutCtrl.deactivate();
			cleanUp();
		} else if(uploadSlideCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				meeting = bigBlueButtonManager.getMeeting(meeting);
				loadSlides(flc);
			}
			cmc.deactivate();
			cleanUp();
		} else if(toolsCtrl == source) {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		} else if(publishCalloutCtrl == source || toolsCalloutCtrl == source || cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteRecordingDialog);
		removeAsListenerAndDispose(publishCalloutCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(uploadSlideCtrl);
		removeAsListenerAndDispose(publishCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeleteRecordingDialog = null;
		publishCalloutCtrl = null;
		toolsCalloutCtrl = null;
		uploadSlideCtrl = null;
		publishCtrl = null;
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(acknowledgeRecordingEl != null) {
			acknowledgeRecordingEl.clearError();
			if(acknowledgeRecordingEl.isVisible()
					&& acknowledgeRecordingEl.isEnabled() && !acknowledgeRecordingEl.isAtLeastSelected(1)) {
				acknowledgeRecordingEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(joinButton == source) {
			doJoin(ureq);
		} else if(this.guestJoinButton == source) {
			doGuestJoin(ureq);
		} else if(uploadButton == source) {
			doUploadSlides(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("delete".equals(se.getCommand())) {
					doConfirmDeleteRecording(ureq, recordingTableModel.getObject(se.getIndex()).getRecording());
				} else if("open-recording".equals(se.getCommand())) {
					doOpenRecording(ureq, recordingTableModel.getObject(se.getIndex()).getRecording());
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("publish".equals(link.getCmd()) && link.getUserObject() instanceof BigBlueButtonRecordingRow) {
				doPublish(ureq, link, (BigBlueButtonRecordingRow)link.getUserObject());
			} else if("tools".equals(link.getCmd()) && link.getUserObject() instanceof BigBlueButtonRecordingRow) {
				doOpenTools(ureq, link, (BigBlueButtonRecordingRow)link.getUserObject());
			} else if("delete".equals(link.getCmd()) && link.getUserObject() instanceof SlideWrapper) {
				doDeleteSlide((SlideWrapper)link.getUserObject());
				loadSlides(flc);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doUploadSlides(UserRequest ureq) {
		uploadSlideCtrl = new SlideUploadController(ureq, getWindowControl(), meeting);
		listenTo(uploadSlideCtrl);
		
		String title = translate("meeting.slides.upload");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), uploadSlideCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDeleteSlide(SlideWrapper slide) {
		VFSLeaf document = slide.getDocument();
		VFSContainer slidesContainer = bigBlueButtonManager.getSlidesContainer(meeting);
		VFSItem reloadedDocument = slidesContainer.resolve(document.getName());
		if(reloadedDocument != null && reloadedDocument.exists()) {
			reloadedDocument.delete();
		}
	}
	
	private void doPublish(UserRequest ureq, FormLink link, BigBlueButtonRecordingRow row) {
		publishCtrl = new PublishRecordingController(ureq, getWindowControl(), row);
		listenTo(publishCtrl); 

		publishCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				publishCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(publishCalloutCtrl);
		publishCalloutCtrl.activate();
	}
	
	private void doOpenTools(UserRequest ureq, FormLink link, BigBlueButtonRecordingRow row) {
		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl); 

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doTogglePermanent(BigBlueButtonRecordingRow row) {
		BigBlueButtonRecordingReference ref = bigBlueButtonManager.getRecordingReference(row.getReference());
		if(ref != null) {
			boolean flag = ref.getPermanent() == null || !ref.getPermanent().booleanValue();
			ref.setPermanent(Boolean.valueOf(flag));
			bigBlueButtonManager.updateRecordingReference(ref);
		}
		loadRecordingsModel();
	}
	
	private void doGuestJoin(UserRequest ureq) {
		if(!validateFormLogic(ureq)) {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			return;
		}
		
		String meetingUrl = Settings.getServerContextPathURI() + "/bigbluebutton/" + meeting.getIdentifier();
		getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(meetingUrl));
	}

	private void doJoin(UserRequest ureq) {
		if(!validateFormLogic(ureq)) {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			return;
		}

		meeting = bigBlueButtonManager.getMeeting(meeting);
		if(meeting == null) {
			showWarning("warning.no.meeting");
			fireEvent(ureq, Event.BACK_EVENT);
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			return;
		}
		
		String meetingUrl = null;
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		if(moderator || administrator) {
			meetingUrl = bigBlueButtonManager.join(meeting, getIdentity(), null, avatarUrl, BigBlueButtonAttendeeRoles.moderator, null, errors);
			delayEvent(new BigBlueButtonEvent(meeting.getKey(), getIdentity().getKey()));
		} else if(!moderatorStartMeeting) {
			BigBlueButtonAttendeeRoles role = guest ? BigBlueButtonAttendeeRoles.guest : BigBlueButtonAttendeeRoles.viewer;
			meetingUrl = bigBlueButtonManager.join(meeting, getIdentity(), null, avatarUrl, role, null, errors);
		} else if(bigBlueButtonManager.isMeetingRunning(meeting)) {
			BigBlueButtonAttendeeRoles role = guest ? BigBlueButtonAttendeeRoles.guest : BigBlueButtonAttendeeRoles.viewer;
			meetingUrl = bigBlueButtonManager.join(meeting, getIdentity(), null, avatarUrl, role, Boolean.TRUE, errors);
		}
		redirectTo(meetingUrl, errors);
	}
	
	private void redirectTo(String meetingUrl, BigBlueButtonErrors errors) {
		if(errors.hasErrors()) {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
		} else if(StringHelper.containsNonWhitespace(meetingUrl)) {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(meetingUrl));
		} else {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			showWarning("warning.no.access");
		}
	}
	
	private void doOpenRecording(UserRequest ureq, BigBlueButtonRecording recording) {
		String url = bigBlueButtonManager.getRecordingUrl(ureq.getUserSession(), recording);
		if(StringHelper.containsNonWhitespace(url)) {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
		} else {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			showWarning("warning.recording.not.found");
		}
	}
	
	private void doConfirmDeleteRecording(UserRequest ureq, BigBlueButtonRecording recording) {
		String confirmDeleteTitle = translate("confirm.delete.recording.title", recording.getName());
		String confirmDeleteText = translate("confirm.delete.recording", recording.getName());
		confirmDeleteRecordingDialog = activateYesNoDialog(ureq, confirmDeleteTitle, confirmDeleteText, confirmDeleteRecordingDialog);
		confirmDeleteRecordingDialog.setUserObject(recording);
	}
	
	private void doDeleteRecording(BigBlueButtonRecording recording) {
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		bigBlueButtonManager.deleteRecording(recording, meeting, errors);
		if(errors.hasErrors()) {
			getWindowControl().setError(BigBlueButtonErrorHelper.formatErrors(getTranslator(), errors));
		}
		loadRecordingsModel();
	}
	
	private void delayEvent(BigBlueButtonEvent openEvent) {
		final EventTask task = new EventTask(openEvent, meetingOres);
		taskExecutorManager.schedule(task , 10000);
	}
	
	private static class EventTask extends TimerTask {
		
		private final BigBlueButtonEvent event;
		private final OLATResourceable ores;
		
		public EventTask(BigBlueButtonEvent event, OLATResourceable ores) {
			this.event = event;
			this.ores = OresHelper.clone(ores);
		}

		@Override
		public void run() {
        	CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(event, ores);
		}
	}
	
	private class ToolsController extends BasicController {
		
		private final Link deleteLink;
		private final Link permanentLink;
		
		private final BigBlueButtonRecordingRow recordingRow;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, BigBlueButtonRecordingRow recordingRow) {
			super(ureq, wControl);
			this.recordingRow = recordingRow;
			
			VelocityContainer mainVC = createVelocityContainer("recording_tools");
			
			Boolean permanent = recordingRow.getReference().getPermanent();
			boolean flagged = permanent == null || !permanent.booleanValue();
			String permanentI18nKey = flagged ? "mark.as.permanent" : "mark.as.not.permanent";
			permanentLink = LinkFactory.createLink(permanentI18nKey, "permanent", getTranslator(), mainVC, this, Link.LINK);
			permanentLink.setIconLeftCSS("o_icon o_icon-fw o_icon_copy");
			
			deleteLink = LinkFactory.createLink("delete", "delete", getTranslator(), mainVC, this, Link.LINK);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");

			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(permanentLink == source) {
				doTogglePermanent(recordingRow);
			} else if(deleteLink == source) {
				doConfirmDeleteRecording(ureq, recordingRow.getRecording());
			}
		}
	}
}
