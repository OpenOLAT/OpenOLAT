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

import static org.olat.modules.bigbluebutton.ui.BigBlueButtonUIHelper.getSelectedTemplate;
import static org.olat.modules.bigbluebutton.ui.BigBlueButtonUIHelper.isWebcamLayoutAvailable;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.group.BusinessGroup;
import org.olat.modules.bigbluebutton.BigBlueButtonDispatcher;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingLayoutEnum;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingsPublishingEnum;
import org.olat.modules.bigbluebutton.BigBlueButtonServer;
import org.olat.modules.bigbluebutton.BigBlueButtonTemplatePermissions;
import org.olat.modules.bigbluebutton.JoinPolicyEnum;
import org.olat.modules.bigbluebutton.manager.SlidesContainerMapper;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditBigBlueButtonMeetingController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	private static final String[] yesNoKeys = new String[] { "yes", "no" };

	private FormLink openCalLink;
	private TextElement nameEl;
	private TextElement descriptionEl;
	private TextElement mainPresenterEl;
	private TextElement welcomeEl;
	private TextElement leadTimeEl;
	private TextElement followupTimeEl;
	private DateChooser startDateEl;
	private DateChooser endDateEl;
	private SingleSelection templateEl;
	private SingleSelection layoutEl;
	private SingleSelection serverEl;
	private SingleSelection recordEl;
	private SingleSelection publishingEl;
	private SingleSelection joinPolicyEl;
	private MultipleSelectionElement guestEl;
	private TextElement externalLinkEl;
	private MultipleSelectionElement passwordEnableEl;
	private TextElement passwordEl;
	private FileElement uploadSlidesEl;
	private FormLayoutContainer slidesCont;

	private final Mode mode;
	private final String subIdent;
	private final RepositoryEntry entry;
	private final boolean withSaveButtons;
	private final BusinessGroup businessGroup;
	private BigBlueButtonMeeting meeting;
	private final List<BigBlueButtonTemplatePermissions> permissions;
	private List<BigBlueButtonMeetingTemplate> templates;
	private SlidesContainerMapper slidesMapper;
	private final String mapperUri;
	private VFSContainer slidesContainer;
	private VFSContainer temporaryContainer;
	private List<SlideWrapper> documentWrappers = new ArrayList<>();
	private int count = 0;
	
	private final boolean running;
	private final boolean editable;
	private final boolean editableInternal;
	private final boolean administrator;
	
	private BigBlueButtonMeetingsCalendarController calCtr;
	private CloseableModalController cmc;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	public EditBigBlueButtonMeetingController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, String subIdent, BusinessGroup businessGroup,
			List<BigBlueButtonTemplatePermissions> permissions, Mode mode) {
		super(ureq, wControl);
		withSaveButtons = true;
		editable = true;
		running = false;
		editableInternal = true;
		administrator = ureq.getUserSession().getRoles().isAdministrator();
		
		this.mode = mode;
		this.entry = entry;
		this.subIdent = subIdent;
		this.businessGroup = businessGroup;
		this.permissions = permissions;
		templates = bigBlueButtonManager.getTemplates();

		temporaryContainer = new LocalFolderImpl(new File(WebappHelper.getTmpDir(), CodeHelper.getUniqueID()));
		slidesMapper = new SlidesContainerMapper(temporaryContainer);
		mapperUri = registerCacheableMapper(null, null, slidesMapper);
		
		initForm(ureq);
		reloadSlides();
	}
	
	public EditBigBlueButtonMeetingController(UserRequest ureq, WindowControl wControl,
			BigBlueButtonMeeting meeting, List<BigBlueButtonTemplatePermissions> permissions) {
		super(ureq, wControl);
		withSaveButtons = true;
		mode = (meeting.isPermanent() && bigBlueButtonModule.isPermanentMeetingEnabled()) ? Mode.permanent : Mode.dates;
		entry = meeting.getEntry();
		subIdent = meeting.getSubIdent();
		businessGroup = meeting.getBusinessGroup();
		this.meeting = meeting;
		this.permissions = permissions;
		templates = bigBlueButtonManager.getTemplates();
		
		running = isRunning(meeting, ureq);
		editable = isEditable(meeting, ureq);
		editableInternal = isEditableInternal(meeting, ureq);
		administrator = ureq.getUserSession().getRoles().isAdministrator();

		slidesContainer = bigBlueButtonManager.getSlidesContainer(meeting);
		temporaryContainer = new LocalFolderImpl(new File(WebappHelper.getTmpDir(), CodeHelper.getUniqueID()));
		slidesMapper = new SlidesContainerMapper(temporaryContainer, slidesContainer);
		mapperUri = registerCacheableMapper(null, null, slidesMapper);
		
		initForm(ureq);
		reloadSlides();
	}
	
	private boolean isEditable(BigBlueButtonMeeting m, UserRequest ureq) {
		Date now = ureq.getRequestTimestamp();
		return m == null || m.isPermanent()
				|| (m.getStartWithLeadTime() != null && m.getStartWithLeadTime().compareTo(now) > 0);
	}
	
	private boolean isEditableInternal(BigBlueButtonMeeting m, UserRequest ureq) {
		Date now = ureq.getRequestTimestamp();
		return m == null || m.isPermanent()
				|| (m.getEndWithFollowupTime() != null && now.compareTo(m.getEndWithFollowupTime()) < 0);
	}
	
	private boolean isRunning(BigBlueButtonMeeting m, UserRequest ureq) {
		Date now = ureq.getRequestTimestamp();
		return m != null && !m.isPermanent()
				&& (m.getStartWithLeadTime() != null && m.getEndWithFollowupTime() != null)
				&& (m.getStartWithLeadTime().compareTo(now) <= 0 && m.getEndWithFollowupTime().compareTo(now) >= 0)
				&& (m.getServer() != null && bigBlueButtonManager.isMeetingRunning(m));
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_bbb_edit_meeting");
		
		if(running || !editable) {
			setFormWarning("warning.meeting.started");
		}
		
		String name = meeting == null ? "" : meeting.getName();
		nameEl = uifactory.addTextElement("meeting.name", "meeting.name", 128, name, formLayout);
		nameEl.setElementCssClass("o_sel_bbb_edit_meeting_name");
		nameEl.setMandatory(true);
		nameEl.setEnabled(editable || editableInternal);
		if(editable && !StringHelper.containsNonWhitespace(name)) {
			nameEl.setFocus(true);
		}
		
		Identity creator = meeting == null ? getIdentity() : meeting.getCreator();
		if(creator != null) {
			String creatorFullName = userManager.getUserDisplayName(creator);
			uifactory.addStaticTextElement("meeting.creator", creatorFullName, formLayout);
		}
		
		String description = meeting == null ? "" : meeting.getDescription();
		descriptionEl = uifactory.addTextAreaElement("meeting.description", "meeting.description", 2000, 4, 72, false, false, description, formLayout);
		descriptionEl.setEnabled(editable || editableInternal);
		
		String welcome = meeting == null ? "" : meeting.getWelcome();
		welcomeEl = uifactory.addRichTextElementForStringDataMinimalistic("meeting.welcome", "meeting.welcome", welcome, 8, 60, formLayout, getWindowControl());
		welcomeEl.setEnabled(editable);
		
		String presenter = meeting == null ? userManager.getUserDisplayName(getIdentity()) : meeting.getMainPresenter();
		mainPresenterEl = uifactory.addTextElement("meeting.main.presenter", "meeting.main.presenter", 128, presenter, formLayout);
		mainPresenterEl.setElementCssClass("o_sel_bbb_edit_meeting_presenter");
		mainPresenterEl.setEnabled(editable || editableInternal);
		
		// slides
		String page = velocity_root + "/meeting_slides.html"; 
		slidesCont = FormLayoutContainer.createCustomFormLayout("meeting.slides.container", getTranslator(), page);
		slidesCont.setLabel("meeting.slides", null);
		slidesCont.contextPut("mapperUri", mapperUri);
		formLayout.add(slidesCont);
		
		uploadSlidesEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "meeting.slides.upload", "meeting.slides", formLayout);
		uploadSlidesEl.addActionListener(FormEvent.ONCHANGE);
		uploadSlidesEl.setVisible(editable || editableInternal);
		uploadSlidesEl.limitToMimeType(BigBlueButtonModule.SLIDES_MIME_TYPES, "error.slides.type", null);
		
		Long selectedTemplateKey = meeting == null || meeting.getTemplate() == null
				? null : meeting.getTemplate().getKey();
		SelectionValues templatesKeyValues = new SelectionValues();
		for(BigBlueButtonMeetingTemplate template:templates) {
			if((template.isEnabled() && template.availableTo(permissions))
					|| template.getKey().equals(selectedTemplateKey)) {
				templatesKeyValues.add(SelectionValues.entry(template.getKey().toString(), template.getName()));
			}
		}
		String[] templatesKeys = templatesKeyValues.keys();
		templateEl = uifactory.addDropdownSingleselect("meeting.template", "meeting.template", formLayout,
				templatesKeys, templatesKeyValues.values());
		templateEl.addActionListener(FormEvent.ONCHANGE);
		templateEl.setMandatory(true);
		templateEl.setElementCssClass("o_omit_margin");
		templateEl.setEnabled(editable);
		boolean templateSelected = false;
		if(selectedTemplateKey != null) {
			String currentTemplateId = selectedTemplateKey.toString();
			for(String key:templatesKeys) {
				if(currentTemplateId.equals(key)) {
					templateEl.select(currentTemplateId, true);
					templateSelected = true;
				}
			}
		}
		if(!templateSelected && templatesKeys.length > 0) {
			templateEl.select(templatesKeys[0], true);
		}
		
		SelectionValues serverKeyValues = new SelectionValues();
		serverKeyValues.add(SelectionValues.entry("auto", translate("meeting.server.auto")));
		appendServerList(serverKeyValues);
		serverEl = uifactory.addDropdownSingleselect("meeting.server", formLayout, serverKeyValues.keys(), serverKeyValues.values());
		serverEl.setEnabled((editable || editableInternal || running) && administrator);
		serverEl.addActionListener(FormEvent.ONCHANGE);
		if(meeting != null && meeting.getServer() != null && serverKeyValues.containsKey(meeting.getServer().getKey().toString())) {
			serverEl.select(meeting.getServer().getKey().toString(), true);
		} else {
			serverEl.select(serverKeyValues.keys()[0], true);
		}
		
		String[] yesNoValues = new String[] { translate("yes"), translate("no")  };
		recordEl = uifactory.addRadiosVertical("meeting.record", formLayout, yesNoKeys, yesNoValues);
		recordEl.addActionListener(FormEvent.ONCHANGE);
		recordEl.setEnabled(editable);
		if(meeting == null || BigBlueButtonUIHelper.isRecord(meeting)) {
			recordEl.select(yesNoKeys[0], true);
		} else {
			recordEl.select(yesNoKeys[1], true);
		}
		
		SelectionValues publishKeyValues = new SelectionValues();
		publishKeyValues.add(SelectionValues.entry(BigBlueButtonRecordingsPublishingEnum.auto.name(), translate("meeting.publishing.auto")));
		publishKeyValues.add(SelectionValues.entry(BigBlueButtonRecordingsPublishingEnum.manual.name(), translate("meeting.publishing.manual")));
		publishingEl = uifactory.addRadiosVertical("meeting.publishing", formLayout, publishKeyValues.keys(), publishKeyValues.values());
		BigBlueButtonRecordingsPublishingEnum publish = meeting == null ? BigBlueButtonRecordingsPublishingEnum.auto :  meeting.getRecordingsPublishingEnum();
		publishingEl.select(publish.name(), true);
		publishingEl.setEnabled(editable);

		SelectionValues layoutKeyValues = new SelectionValues();
		layoutKeyValues.add(SelectionValues.entry(BigBlueButtonMeetingLayoutEnum.standard.name(), translate("layout.standard")));
		if(isWebcamLayoutAvailable(getSelectedTemplate(templateEl, templates))) {
			layoutKeyValues.add(SelectionValues.entry(BigBlueButtonMeetingLayoutEnum.webcam.name(), translate("layout.webcam")));
		}
		layoutEl = uifactory.addDropdownSingleselect("meeting.layout", "meeting.layout", formLayout,
				layoutKeyValues.keys(), layoutKeyValues.values());
		layoutEl.setEnabled(editable);
		boolean layoutSelected = false;
		String selectedLayout = meeting == null ? BigBlueButtonMeetingLayoutEnum.standard.name() : meeting.getMeetingLayout().name();
		for(String layoutKey:layoutKeyValues.keys()) {
			if(layoutKey.equals(selectedLayout)) {
				layoutEl.select(layoutKey, true);
				layoutSelected = true;
			}
		}
		if(!layoutSelected) {
			layoutEl.select(BigBlueButtonMeetingLayoutEnum.standard.name(), true);
		}
		layoutEl.setVisible(layoutEl.getKeys().length > 1);
		
		SelectionValues joinKeyValues = new SelectionValues();
		joinKeyValues.add(SelectionValues.entry(JoinPolicyEnum.disabled.name(), translate("join.users.control.disabled")));
		joinKeyValues.add(SelectionValues.entry(JoinPolicyEnum.guestsApproval.name(), translate("join.users.control.guests")));
		joinKeyValues.add(SelectionValues.entry(JoinPolicyEnum.allUsersApproval.name(), translate("join.users.control.users")));
		joinPolicyEl = uifactory.addDropdownSingleselect("template.join.policy", "template.join.policy", formLayout,
				joinKeyValues.keys(), joinKeyValues.values());
		JoinPolicyEnum joinPolicy = meeting == null ? JoinPolicyEnum.disabled : meeting.getJoinPolicyEnum();
		joinPolicyEl.select(joinPolicy.name(), true);
		
		String[] guestValues = new String[] { translate("meeting.guest.on") };
		guestEl = uifactory.addCheckboxesHorizontal("meeting.guest", formLayout, onKeys, guestValues);
		guestEl.setElementCssClass("o_sel_bbb_edit_meeting_guest");
		guestEl.setVisible(entry != null && entry.isGuests());
		guestEl.select(onKeys[0], meeting != null && meeting.isGuest());
		guestEl.setEnabled(editable);
		
		String externalLink = meeting == null ? CodeHelper.getForeverUniqueID() + "" : meeting.getReadableIdentifier();
		externalLinkEl = uifactory.addTextElement("meeting.external.users", 64, externalLink, formLayout);
		externalLinkEl.setElementCssClass("o_sel_bbb_edit_meeting_guest");
		externalLinkEl.setPlaceholderKey("meeting.external.users.empty", null);
		externalLinkEl.setHelpTextKey("meeting.external.users.help", null);
		externalLinkEl.addActionListener(FormEvent.ONCHANGE);
		if (externalLink != null) {
			externalLinkEl.setExampleKey("noTransOnlyParam", new String[] {BigBlueButtonDispatcher.getMeetingUrl(externalLink)});
		}

		String password = meeting == null ? null : meeting.getPassword();
		String[] enableValues = new String[] { translate("meeting.password.enable.on") };
		passwordEnableEl = uifactory.addCheckboxesHorizontal("meeting.password.enable", "meeting.password.enable", formLayout, onKeys, enableValues);
		passwordEnableEl.select(onKeys[0], StringHelper.containsNonWhitespace(password));
		passwordEnableEl.addActionListener(FormEvent.ONCHANGE);
		
		passwordEl = uifactory.addTextElement("meeting.password", 64, password, formLayout);
		
		openCalLink = uifactory.addFormLink("calendar.open", formLayout);
		openCalLink.setIconLeftCSS("o_icon o_icon-fw o_icon_calendar");
		boolean meetingExists = meeting != null && meeting.getKey() != null;
		BigBlueButtonUIHelper.updateTemplateInformations(templateEl, externalLinkEl, passwordEnableEl, passwordEl, publishingEl, recordEl,
				templates, meetingExists);
		BigBlueButtonUIHelper.updateJoinPolicy(templateEl, joinPolicyEl, templates, meetingExists);
		
		if(mode == Mode.dates) {
			Date startDate = meeting == null ? new Date() : meeting.getStartDate();
			startDateEl = uifactory.addDateChooser("meeting.start", "meeting.start", startDate, formLayout);
			startDateEl.setMandatory(true);
			startDateEl.setDateChooserTimeEnabled(true);
			startDateEl.setEnabled(editable);
			
			String leadtime = meeting == null ? null : Long.toString(meeting.getLeadTime());
			leadTimeEl = uifactory.addTextElement("meeting.leadTime", 8, leadtime, formLayout);
			leadTimeEl.setEnabled(editable);
			leadTimeEl.setExampleKey("meeting.leadTime.explain", null);
			
			Date endDate = meeting == null ? null : meeting.getEndDate();
			if (endDate == null && startDate != null) {
				// set meeting time default to 1 hour
				Calendar calendar = Calendar.getInstance();
			    calendar.setTime(startDate);
			    calendar.add(Calendar.HOUR_OF_DAY, 1);
			    endDate = calendar.getTime();
			}
			endDateEl = uifactory.addDateChooser("meeting.end", "meeting.end", endDate, formLayout);
			endDateEl.setMandatory(true);
			endDateEl.setDefaultValue(startDateEl);
			endDateEl.setDateChooserTimeEnabled(true);
			endDateEl.setEnabled(editable);
			
			String followup = meeting == null ? null : Long.toString(meeting.getFollowupTime());
			followupTimeEl = uifactory.addTextElement("meeting.followupTime", 8, followup, formLayout);
			followupTimeEl.setEnabled(editable);
		}
		
		if(withSaveButtons) {
			FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			formLayout.add("buttons", buttonLayout);
			uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
			if(editable || editableInternal) {
				uifactory.addFormSubmitButton("save", buttonLayout);
			}
		}
	}
	
	private void appendServerList(SelectionValues serverKeyValues) {
		List<BigBlueButtonServer> servers = bigBlueButtonManager.getServers();
		for(BigBlueButtonServer server:servers) {
			if(!server.isEnabled()) continue;
			
			String name = server.getName();
			if(!StringHelper.containsNonWhitespace(name)) {
				name = BigBlueButtonUIHelper.getServerNameFromUrl(server.getUrl());
			}
			serverKeyValues.add(SelectionValues.entry(server.getKey().toString(), name));
		}
	}
	
	private void doOpenCalendar(UserRequest ureq) {
		removeAsListenerAndDispose(calCtr);
		removeAsListenerAndDispose(cmc);

		// open calendar controller in modal. Not very nice to have stacked modal, but
		// still better than having no overview at all
		calCtr = new BigBlueButtonMeetingsCalendarController(ureq, getWindowControl());
		listenTo(calCtr);
		cmc = new CloseableModalController(getWindowControl(), "close", calCtr.getInitialComponent(), true,
				translate("calendar.open"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void reloadSlides() {
		Map<String,SlideWrapper> docsMap = documentWrappers.stream()
				.collect(Collectors.toMap(SlideWrapper::getFilename, doc -> doc));
		
		List<SlideWrapper> wrappers = new ArrayList<>();
		if(temporaryContainer != null && temporaryContainer.exists()) {
			reloadSlides(temporaryContainer, true, wrappers, docsMap);
		}
		if(slidesContainer != null && slidesContainer.exists()) {
			reloadSlides(slidesContainer, false, wrappers, docsMap);
		}
		
		Collections.sort(wrappers);
		slidesCont.contextPut("documents", wrappers);
		slidesCont.setVisible(!wrappers.isEmpty());

		if(uploadSlidesEl != null && uploadSlidesEl.isVisible()) {
			if(wrappers.isEmpty()) {
				uploadSlidesEl.setLabel("meeting.slides.upload", null);
				slidesCont.setDirty(true);
			} else {
				uploadSlidesEl.setLabel(null, null);
			}
		}
	}
	
	private void reloadSlides(VFSContainer container, boolean temporary, List<SlideWrapper> wrappers, Map<String,SlideWrapper> docsMap) {
		List<VFSItem>  documents = container.getItems(new VFSSystemItemFilter());
		for (VFSItem document : documents) {
			if(document instanceof VFSLeaf) {
				SlideWrapper wrapper = docsMap.get(document.getName());
				if(wrapper == null) {
					wrapper = new SlideWrapper((VFSLeaf)document, temporary);
					documentWrappers.add(wrapper);
					if(editable || editableInternal) {
						FormLink deleteButton = uifactory
								.addFormLink("delete_" + (++count), "delete", "delete", null, slidesCont, Link.BUTTON_XSMALL);
						deleteButton.setUserObject(wrapper);
						wrapper.setDeleteButton(deleteButton);
					}
				}
				if(!wrapper.isDeleted()) {
					wrappers.add(wrapper);
				}
			}
		}
	}
	
	@Override
	protected void doDispose() {
		if(temporaryContainer != null) {
			temporaryContainer.deleteSilently();
		}
		if(meeting != null && meeting.getKey() == null) {
			bigBlueButtonManager.deleteSlides(meeting);
		}
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= BigBlueButtonUIHelper.validateReadableIdentifier(externalLinkEl, meeting);

		if(mode == Mode.dates) {
			startDateEl.clearError();
			endDateEl.clearError();
			if(startDateEl.getDate() == null) {
				startDateEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
			if(endDateEl.getDate() == null) {
				endDateEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
			
			if(startDateEl.getDate() != null && endDateEl.getDate() != null) {
				Date start = startDateEl.getDate();
				Date end = endDateEl.getDate();
				if(end.before(start)) {
					endDateEl.setErrorKey("error.start.after.end", null);
					allOk &= false;
				}
				
				Date now = new Date();
				if(end.before(now)) {
					endDateEl.setErrorKey("error.end.past", null);
					allOk &= false;
				}
			}
			
			allOk &= BigBlueButtonUIHelper.validateTime(leadTimeEl, 15l);
			allOk &= BigBlueButtonUIHelper.validateTime(followupTimeEl, 15l);
		}
		
		allOk &= validateSingleSelection(templateEl);
		allOk &= validateSingleSelection(serverEl);
		
		// dates ok
		if(allOk) {
			BigBlueButtonMeetingTemplate template = BigBlueButtonUIHelper.getSelectedTemplate(templateEl, templates);
			if(mode == Mode.permanent) {
				allOk &= BigBlueButtonUIHelper.validatePermanentSlot(templateEl, meeting, template);
			} else if(mode == Mode.dates) {
				allOk &= BigBlueButtonUIHelper.validateDuration(startDateEl, leadTimeEl, endDateEl, followupTimeEl, template);
				allOk &= BigBlueButtonUIHelper.validateSlot(startDateEl, leadTimeEl, endDateEl, followupTimeEl, meeting, template);
			}
		}
		
		nameEl.clearError();
		if(!StringHelper.containsNonWhitespace(nameEl.getValue())) {
			nameEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if (nameEl.getValue().contains("&")) {
			nameEl.setErrorKey("form.invalidchar.noamp", null);
			allOk &= false;
		}
		
		allOk &= validateSingleSelection(publishingEl);
		allOk &= validateSingleSelection(joinPolicyEl);
		
		allOk &= validateSlidesSize();

		return allOk;
	}
	
	private boolean validateSingleSelection(SingleSelection el) {
		boolean allOk = true;
		
		el.clearError();
		if(!el.isOneSelected()) {
			el.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validateSlidesSize() {
		boolean allOk = true;
		
		Integer maxSizeInMb = bigBlueButtonModule.getMaxUploadSize();
		slidesCont.clearError();
		if(maxSizeInMb != null && maxSizeInMb.intValue() > 0) {
			long total = 0l;
			for(SlideWrapper doc:documentWrappers) {
				if(!doc.isDeleted()) {
					total += doc.getDocument().getSize();
				}
			}
			if(total > (maxSizeInMb.intValue() * 1000 * 1000)) {
				slidesCont.setErrorKey("error.slides.size", new String[] { maxSizeInMb.toString() });
				allOk &= false;
			}
		}
		slidesCont.setDirty(true);
		
		return allOk;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(calCtr == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(calCtr);
		removeAsListenerAndDispose(cmc);
		calCtr = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(templateEl == source) {
			boolean meetingExists = meeting != null && meeting.getKey() != null;
			BigBlueButtonUIHelper.updateTemplateInformations(templateEl, externalLinkEl, passwordEnableEl, passwordEl, publishingEl, recordEl,
					templates, meetingExists);
			boolean webcamAvailable = isWebcamLayoutAvailable(getSelectedTemplate(templateEl, templates));
			BigBlueButtonUIHelper.updateLayoutSelection(layoutEl, getTranslator(), webcamAvailable);
			BigBlueButtonUIHelper.updateJoinPolicy(templateEl, joinPolicyEl, templates, meetingExists);
		} else if(recordEl == source || passwordEnableEl == source) {
			BigBlueButtonUIHelper.updateTemplateInformations(templateEl, externalLinkEl, passwordEnableEl, passwordEl, publishingEl, recordEl,
					templates, meeting != null && meeting.getKey() != null);
		} else if (openCalLink == source) {
			doOpenCalendar(ureq);
		} else if (externalLinkEl == source) {
			BigBlueButtonUIHelper.validateReadableIdentifier(externalLinkEl, meeting);
		} else if (serverEl == source) {
			serverChangeWarning();
		} else if(uploadSlidesEl == source) {
			if(uploadSlidesEl.getUploadFile() != null && StringHelper.containsNonWhitespace(uploadSlidesEl.getUploadFileName())) {
				doUploadSlide(uploadSlidesEl.getUploadFile(), uploadSlidesEl.getUploadFileName());
				reloadSlides();
				validateSlidesSize();
				uploadSlidesEl.reset();
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("delete".equals(link.getCmd()) && link.getUserObject() instanceof SlideWrapper) {
				doDeleteSlide((SlideWrapper)link.getUserObject());
				reloadSlides();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void serverChangeWarning() {
		if(meeting != null && meeting.getServer() != null && (running || meeting.isPermanent())
				&& !serverEl.getSelectedKey().equals(meeting.getServer().getKey().toString())) {
			showWarning("warning.change.server");
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(meeting == null) {
			meeting = bigBlueButtonManager
					.createAndPersistMeeting(nameEl.getValue(), entry, subIdent, businessGroup, getIdentity());
		} else {
			if(meeting.getKey() != null) {
				meeting = bigBlueButtonManager.getMeeting(meeting);
			}
			meeting.setName(nameEl.getValue());
		}
		
		meeting.setDescription(descriptionEl.getValue());
		meeting.setWelcome(welcomeEl.getValue());
		meeting.setMainPresenter(mainPresenterEl.getValue());
		BigBlueButtonMeetingTemplate template = getSelectedTemplate(templateEl, templates);
		meeting.setTemplate(template);
		
		if(template != null && template.isExternalUsersAllowed()
				&& externalLinkEl.isVisible() && StringHelper.containsNonWhitespace(externalLinkEl.getValue())) {
			meeting.setReadableIdentifier(externalLinkEl.getValue());
			if(passwordEnableEl.isAtLeastSelected(1)) {
				meeting.setPassword(passwordEl.getValue());
			} else {
				meeting.setPassword(null);
			}
		} else {
			meeting.setPassword(null);
			meeting.setReadableIdentifier(null);
		}
		
		meeting.setPermanent(mode == Mode.permanent);
		if(mode == Mode.permanent) {
			meeting.setStartDate(null);
			meeting.setEndDate(null);
			meeting.setLeadTime(0l);
			meeting.setFollowupTime(0l);
		} else {
			Date startDate = startDateEl.getDate();
			meeting.setStartDate(startDate);
			Date endDate = endDateEl.getDate();
			meeting.setEndDate(endDate);
			long leadTime = BigBlueButtonUIHelper.getLongOrZero(leadTimeEl);
			meeting.setLeadTime(leadTime);
			long followupTime = BigBlueButtonUIHelper.getLongOrZero(followupTimeEl);
			meeting.setFollowupTime(followupTime);
		}
		
		boolean guests = guestEl.isVisible() && guestEl.isAtLeastSelected(1);
		meeting.setGuest(guests);
		
		JoinPolicyEnum joinPolicy = JoinPolicyEnum.secureValueOf(joinPolicyEl.getSelectedKey());
		meeting.setJoinPolicyEnum(joinPolicy);
		
		if(layoutEl.isVisible() && layoutEl.isOneSelected()) {
			BigBlueButtonMeetingLayoutEnum layout = BigBlueButtonMeetingLayoutEnum.secureValueOf(layoutEl.getSelectedKey());
			meeting.setMeetingLayout(layout);
		} else {
			meeting.setMeetingLayout(BigBlueButtonMeetingLayoutEnum.standard);
		}
		
		if(publishingEl.isVisible() && publishingEl.isOneSelected()) {
			meeting.setRecordingsPublishingEnum(BigBlueButtonRecordingsPublishingEnum.valueOf(publishingEl.getSelectedKey()));
		} else {
			meeting.setRecordingsPublishingEnum(BigBlueButtonRecordingsPublishingEnum.manual);
		}
		if(recordEl.isVisible() && recordEl.isOneSelected()) {
			meeting.setRecord(Boolean.valueOf(yesNoKeys[0].equals(recordEl.getSelectedKey())));
		} else {
			meeting.setRecord(null);
		}
		
		String selectedServerKey = serverEl.getSelectedKey();
		if("auto".equals(selectedServerKey)) {
			meeting.setServer(null);
		} else if(StringHelper.isLong(selectedServerKey)) {
			BigBlueButtonServer server = bigBlueButtonManager.getServer(Long.valueOf(selectedServerKey));
			meeting.setServer(server);
		}
		
		// copy the slides, eventually update the directory field
		doCopySlides(ureq.getIdentity());

		meeting = bigBlueButtonManager.updateMeeting(meeting);

		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doCopySlides(Identity savedBy) {
		if(documentWrappers.isEmpty()) return;

		VFSContainer storage = bigBlueButtonManager.getSlidesContainer(meeting);
		for(SlideWrapper doc:documentWrappers) {
			if(doc.isDeleted()) {
				doc.getDocument().deleteSilently();
			} else if(doc.isTemporary()) {
				VFSLeaf target = storage.createChildLeaf(doc.getFilename());
				VFSManager.copyContent(doc.getDocument(), target, true, savedBy);
			}
		}
	}
	
	private void doUploadSlide(File file, String filename) {
		List<ValidationStatus> validationResults = new ArrayList<>();
		uploadSlidesEl.validate(validationResults);
		if(validationResults.isEmpty()) {
			VFSLeaf newSlide = VFSManager.resolveOrCreateLeafFromPath(temporaryContainer, filename);
			VFSManager.copyContent(file, newSlide, getIdentity());
			
			for(SlideWrapper doc:documentWrappers) {
				if(filename.equals(doc.getFilename())) {
					doc.setDeleted(false);
				}
			}
		}
	}
	
	private void doDeleteSlide(SlideWrapper slide) {
		slide.setDeleted(true);
		slidesCont.setDirty(true);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		if(meeting != null && meeting.getKey() == null) {
			bigBlueButtonManager.deleteSlides(meeting);
		}
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	public enum Mode {
		permanent,
		dates
	}
}
