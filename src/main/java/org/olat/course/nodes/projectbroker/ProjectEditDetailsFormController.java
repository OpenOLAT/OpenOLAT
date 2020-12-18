/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.projectbroker;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.projectbroker.datamodel.CustomField;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.olat.course.nodes.projectbroker.datamodel.ProjectEvent;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerMailer;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerManager;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerModuleConfiguration;
import org.olat.course.nodes.projectbroker.service.ProjectGroupManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author guretzki
 */

public class ProjectEditDetailsFormController extends FormBasicController {

	private static final String CUSTOM_DATE_FORMAT = "dd.MM.yyyy HH:mm";
	
	private final String DROPDOWN_NO_SELECETION = "dropdown.nothing.selected";
	
	private Project project;

	private TextElement projectTitle;	
	private RichTextElement projectDescription;
	private IntegerElement maxMembers;
	private StaticTextElement projectState;
	private FormLayoutContainer stateLayout;
	private FileElement attachmentFileName;

	private CourseEnvironment courseEnv;
	private CourseNode courseNode;
	private ProjectBrokerModuleConfiguration projectBrokerModuleConfiguration;

	private final List<FormItem> customfieldElementList;
	private final Map<Project.EventType, DateChooser> eventStartElementList;
	private final Map<Project.EventType, DateChooser> eventEndElementList;

	private MultipleSelectionElement selectionMaxMembers;
	private MultipleSelectionElement allowDeselection;

	private boolean enableCancel;

	private MultipleSelectionElement mailNotification;

	private FormLink removeAttachmentLink;

	private final static String[] keys = new String[] { "form.modules.enabled.yes" };
	private final static String[] values = new String[] { "" };
	private static final int MAX_MEMBERS_DEFAULT = 1;

	@Autowired
	private ProjectBrokerMailer projectBrokerMailer;
	@Autowired
	private ProjectGroupManager projectGroupManager;
	@Autowired
	private ProjectBrokerManager projectBrokerManager;

	/**
	 * Modules selection form.
	 * @param name
	 * @param config
	 */
	public ProjectEditDetailsFormController(UserRequest ureq, WindowControl wControl, Project project, CourseEnvironment courseEnv, CourseNode courseNode, ProjectBrokerModuleConfiguration projectBrokerModuleConfiguration, boolean enableCancel) {
		super(ureq, wControl);
		this.project = project;
		this.courseEnv = courseEnv;
		this.courseNode = courseNode;
		this.projectBrokerModuleConfiguration = projectBrokerModuleConfiguration;
		this.enableCancel = enableCancel;
		customfieldElementList = new ArrayList<>();
		eventStartElementList = new HashMap<>();
		eventEndElementList = new HashMap<>();
		initForm(ureq);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		// validate event dates
		for (Project.EventType eventType : eventStartElementList.keySet()) {
			Date startDate = eventStartElementList.get(eventType).getDate();
			Date endDate   = eventEndElementList.get(eventType).getDate();
			getLogger().debug("validate startDate=" + startDate + " enddate=" + endDate);
			if ( (startDate != null) && (endDate != null) && startDate.after(endDate) ) {
				eventStartElementList.get(eventType).setErrorKey("from.error.date.start.after.end", null);
				return false;
			}
		}
		if (  !project.getTitle().equals(projectTitle.getValue()) 
				&& projectBrokerManager.existProjectName(project.getProjectBroker().getKey(), projectTitle.getValue()) ) {		
			projectTitle.setErrorKey("form.error.project.title.already.exist", null);
			return false;
		}
		if (projectTitle.getValue().trim().isEmpty()) {
			projectTitle.setErrorKey("form.error.project.title.is.empty", null);
			return false;
		}
		
		// http://jira.openolat.org/browse/OO-131  check for too long filename
		if (attachmentFileName.getUploadFileName() != null && attachmentFileName.getUploadFileName().length() > 99) {
			attachmentFileName.setErrorKey("form.error.project.filenametoolong", null);
			return false;
		}
		return true;
	}

	/**
	 * Initialize form.
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		//create form elements
		projectTitle = uifactory.addTextElement("title", "detailsform.title.label", 100, project.getTitle(), formLayout);
		
		// account-Managers
		StringBuilder projectLeaderString = new StringBuilder();
		for (Iterator<Identity> iterator = project.getProjectLeaders().iterator(); iterator.hasNext();) {
			Identity identity = iterator.next();
			String last = identity.getUser().getProperty(UserConstants.LASTNAME, getLocale());
			String first= identity.getUser().getProperty(UserConstants.FIRSTNAME, getLocale());
			if (projectLeaderString.length() > 0) {
				projectLeaderString.append(",");
			}
			projectLeaderString.append(first);
			projectLeaderString.append(" ");
			projectLeaderString.append(last);
		}
		TextElement projectLeaders = uifactory.addTextElement("projectleaders", "detailsform.projectleaders.label", 100, projectLeaderString.toString(), formLayout);
		projectLeaders.setEnabled(false);
		
		// add the learning objectives rich text input element
		projectDescription = uifactory.addRichTextElementForStringData("description", "detailsform.description.label", project.getDescription(), 10, -1, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		projectDescription.setMaxLength(2500);		

		stateLayout = FormLayoutContainer.createHorizontalFormLayout("stateLayout", getTranslator());
		stateLayout.setLabel("detailsform.state.label", null);
		formLayout.add(stateLayout);
		String stateValue = getTranslator().translate(projectBrokerManager.getStateFor(project,ureq.getIdentity(),projectBrokerModuleConfiguration));
		projectState = uifactory.addStaticTextElement("detailsform.state", stateValue + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", stateLayout);
		projectState.setLabel(null, null);

		String keyDetailsformMax = null;
		if (projectBrokerModuleConfiguration.isAcceptSelectionManually()) {
			keyDetailsformMax = "detailsform.max.candidates.label";
		} else {
			keyDetailsformMax = "detailsform.max.members.label";
		}
		selectionMaxMembers = uifactory.addCheckboxesHorizontal(keyDetailsformMax, formLayout, keys, values);
		maxMembers = uifactory.addIntegerElement("form.options.number.of.participants.per.topic_nbr", project.getMaxMembers(), formLayout);
		maxMembers.setMinValueCheck(0, null);
		maxMembers.setDisplaySize(3);
		if (project.getMaxMembers() == Project.MAX_MEMBERS_UNLIMITED) {
			maxMembers.setVisible(false);
			selectionMaxMembers.select(keys[0], false);
		} else {
			selectionMaxMembers.select(keys[0], true);
		}
		selectionMaxMembers.addActionListener(FormEvent.ONCLICK);
		
		String[] deselectValues = new String[] {translate("detailsform.allow.deselection.hint")};
		allowDeselection = uifactory.addCheckboxesHorizontal("detailsform.allow.deselection", formLayout, keys, deselectValues);
		allowDeselection.select(keys[0], projectGroupManager.isDeselectionAllowed(project));
		allowDeselection.addActionListener(FormEvent.ONCLICK);
		uifactory.addSpacerElement("spacer_1", formLayout, false);

		// customfields
		List<CustomField> customFields = projectBrokerModuleConfiguration.getCustomFields();
		int customFieldIndex = 0;
		for (CustomField customField:customFields) {
			getLogger().debug("customField: {}={}", customField.getName(), customField.getValue());
			StringTokenizer tok = new StringTokenizer(customField.getValue(),ProjectBrokerManager.CUSTOMFIELD_LIST_DELIMITER);
			if (customField.getValue() == null || customField.getValue().equals("") || !tok.hasMoreTokens()) {
				// no value define => Text-input
			  // Add StaticTextElement as workaroung for non translated label
				uifactory.addStaticTextElement("customField_label" + customFieldIndex, null, customField.getName(), formLayout);//null > no label
				TextElement textElement = uifactory.addTextElement("customField_" + customFieldIndex, "", 150, project.getCustomFieldValue(customFieldIndex), formLayout);
				textElement.setDisplaySize(60);
				textElement.showLabel(false);
				customfieldElementList.add(textElement);
			} else {
				// values define => dropdown selection
				List<String> valueList = new ArrayList<>();
				while (tok.hasMoreTokens()) {
					String value = tok.nextToken();
					valueList.add(value);
					getLogger().debug("valueList add: {}", value);
				}
				String[] theValues = new String[valueList.size() + 1];
				String[] theKeys   = new String[valueList.size() + 1];
				int arrayIndex = 0;
				theValues[arrayIndex]=translate(DROPDOWN_NO_SELECETION);
				theKeys[arrayIndex]=DROPDOWN_NO_SELECETION;
				arrayIndex++;
				for (Iterator<String> iterator2 = valueList.iterator(); iterator2.hasNext();) {
					String value = iterator2.next();
					theValues[arrayIndex]=value;
					theKeys[arrayIndex]=Integer.toString(arrayIndex);
					arrayIndex++;
				}
				// Add StaticTextElement as workaround for non translated label
				uifactory.addStaticTextElement("customField_label" + customFieldIndex, null, customField.getName(), formLayout);//null > no label
				SingleSelection selectionElement = uifactory.addDropdownSingleselect("customField_" + customFieldIndex, null, formLayout, theKeys, theValues, null);
				if (project.getCustomFieldValue(customFieldIndex) != null && !project.getCustomFieldValue(customFieldIndex).equals("")) {
					if (valueList.contains(project.getCustomFieldValue(customFieldIndex))) {			
						String key = Integer.toString(valueList.indexOf(project.getCustomFieldValue(customFieldIndex)) + 1);// '+1' because no-selection at the beginning
						selectionElement.select(key, true);
					} else {
						this.showInfo("warn.customfield.key.does.not.exist",project.getCustomFieldValue(customFieldIndex));
					}
				}
				customfieldElementList.add(selectionElement);
			}
			uifactory.addSpacerElement("customField_spacer" + customFieldIndex, formLayout, false);
			customFieldIndex++;
		}
		
		// Events
		for (Project.EventType eventType : Project.EventType.values()) {
			if ( projectBrokerModuleConfiguration.isProjectEventEnabled(eventType) ){
				ProjectEvent projectEvent = project.getProjectEvent(eventType);
				DateChooser dateChooserStart = uifactory.addDateChooser(eventType + "start", eventType.getI18nKey() + ".start.label", null, formLayout);
				dateChooserStart.setDateChooserTimeEnabled(true);
				dateChooserStart.setDisplaySize(CUSTOM_DATE_FORMAT.length());
				getLogger().info("Event=" + eventType + ", startDate=" + projectEvent.getStartDate());
				dateChooserStart.setDate(projectEvent.getStartDate());
				eventStartElementList.put(eventType, dateChooserStart);
				DateChooser dateChooserEnd   = uifactory.addDateChooser(eventType + "end", eventType.getI18nKey() + ".end.label", null, formLayout);
				dateChooserEnd.setDateChooserTimeEnabled(true);
				dateChooserEnd.setDisplaySize(CUSTOM_DATE_FORMAT.length());
				getLogger().debug("Event=" + eventType + ", endDate=" + projectEvent.getEndDate());
				dateChooserEnd.setDate(projectEvent.getEndDate());
				eventEndElementList.put(eventType, dateChooserEnd);
				uifactory.addSpacerElement(eventType + "spacer", formLayout, false);
			}
		}

		attachmentFileName = uifactory.addFileElement(getWindowControl(), "detailsform.attachmentfilename.label", formLayout);
		attachmentFileName.setLabel("detailsform.attachmentfilename.label", null);
		if (project.getAttachmentFileName() != null && !project.getAttachmentFileName().equals("")) {
			attachmentFileName.setInitialFile(new File(project.getAttachmentFileName()));
			removeAttachmentLink = uifactory.addFormLink("detailsform.remove.attachment", formLayout, Link.BUTTON_XSMALL);
		}
		attachmentFileName.addActionListener(FormEvent.ONCHANGE);

		mailNotification = uifactory.addCheckboxesHorizontal("detailsform.mail.notification.label", formLayout, keys, values);
		mailNotification.select(keys[0], project.isMailNotificationEnabled());
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		uifactory.addFormSubmitButton("save", buttonGroupLayout);
		if (this.enableCancel) {
			uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean projectChanged = false;
		if (!project.getTitle().equals(projectTitle.getValue()) ) {
			// title has been changed => change project-group name too
			String newProjectGroupName = translate("project.member.groupname", projectTitle.getValue());
			String newProjectGroupDescription = translate("project.member.groupdescription", projectTitle.getValue());
			OLATResource courseResource = courseEnv.getCourseGroupManager().getCourseResource();
			projectGroupManager.changeProjectGroupName(getIdentity(), project.getProjectGroup(), newProjectGroupName, newProjectGroupDescription, courseResource);
			projectGroupManager.sendGroupChangeEvent(project, courseEnv.getCourseResourceableId(), ureq.getIdentity());
			projectChanged = true;
		}
		if (!project.getTitle().equals(projectTitle.getValue())) {
			project.setTitle(projectTitle.getValue());
			projectChanged = true;
		}
		if (!project.getDescription().equals(projectDescription.getValue())) {		
			project.setDescription(projectDescription.getValue());
			projectChanged = true;
		}
		if (project.getMaxMembers() != maxMembers.getIntValue()) {
			project.setMaxMembers(maxMembers.getIntValue());
			projectGroupManager.setProjectGroupMaxMembers(getIdentity(), project.getProjectGroup(), maxMembers.getIntValue());
			projectChanged = true;
		}			
		if (StringHelper.containsNonWhitespace(attachmentFileName.getUploadFileName())) {
			// First call uploadFiles than setAttachedFileName because uploadFiles needs old attachment name 
			uploadFiles(attachmentFileName);
			project.setAttachedFileName(attachmentFileName.getUploadFileName());
			projectChanged = true;
		} else if (StringHelper.containsNonWhitespace(project.getAttachmentFileName())
				&& attachmentFileName.getInitialFile() == null) {
			// Attachment file has been removed
			project.setAttachedFileName("");
			projectChanged = true;
		}
		// store customfields
		int index = 0;
		for (FormItem element : customfieldElementList) {
			String value = "";
			if (element instanceof TextElement) {
				TextElement textElement = (TextElement)element;
				value = textElement.getValue(); 
			} else if (element instanceof SingleSelection) {
				SingleSelection selectionElement = (SingleSelection)element;
				if (!selectionElement.getSelectedKey().equals(DROPDOWN_NO_SELECETION)) {
					value = selectionElement.getValue(selectionElement.getSelected());
				} else {
					value = "";
				}
			}
			String currentValue = project.getCustomFieldValue(index);
			getLogger().debug("customfield index={} value={} project.getCustomFieldValue(index)={}", index, value, currentValue);
			if (!currentValue.equals(value)) {
				project.setCustomFieldValue(index, value);
				projectChanged = true;
			}			
			index++;
		}
		// store events
		for (Project.EventType eventType : eventStartElementList.keySet()) {
			Date startDate = eventStartElementList.get(eventType).getDate();
			Date endDate   = eventEndElementList.get(eventType).getDate();
			// First handle startdate
			if (   hasBeenChanged(project.getProjectEvent(eventType).getStartDate(), startDate)
					|| hasBeenChanged(project.getProjectEvent(eventType).getEndDate(), endDate) ) {
				project.setProjectEvent(new ProjectEvent(eventType, startDate, endDate));
				projectChanged = true;				
			}
		}
		if (mailNotification.isSelected(0) != project.isMailNotificationEnabled()) {
			project.setMailNotificationEnabled(mailNotification.isSelected(0));
			projectChanged = true;	
		}
		if (projectChanged) {
			if (projectBrokerManager.existsProject( project.getKey())) {		
				projectBrokerManager.updateProject(project);
				projectBrokerMailer.sendProjectChangedEmailToParticipants(ureq.getIdentity(), project, this.getTranslator());
			} else {
				showInfo("info.project.nolonger.exist", project.getTitle());
			}
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	private boolean hasBeenChanged(Date projectDate, Date formDate) {
		if ( projectDate == null) {
			if (formDate == null) {
				return false;
			} else {
				return true;
			}
		} else {
			// projectDate is NOT null
			if (formDate == null) {
				return true;
			} else {
				return (projectDate.compareTo(formDate) != 0);
			}
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == selectionMaxMembers) {
			if (selectionMaxMembers.isSelected(0)) {
				maxMembers.setVisible(true);
				maxMembers.setIntValue(MAX_MEMBERS_DEFAULT);
			} else {
				maxMembers.setVisible(false);
				maxMembers.setIntValue(Project.MAX_MEMBERS_UNLIMITED);
			}
		} else if(source == allowDeselection){
			if(allowDeselection.isSelected(0)){
				projectGroupManager.setDeselectionAllowed(project, true);
			}else{
				projectGroupManager.setDeselectionAllowed(project, false);
			}
		}else if (source == removeAttachmentLink) {
			attachmentFileName.setInitialFile(null);
		}
		this.flc.setDirty(true);
	}

	@Override
	protected void doDispose() {
		//nothing
	}

	private void uploadFiles(FileElement attachmentFileElement) {
		VFSLeaf uploadedItem = new LocalFileImpl(attachmentFileElement.getUploadFile());
		projectBrokerManager.saveAttachedFile(project, attachmentFileElement.getUploadFileName(), uploadedItem, courseEnv, courseNode );
	}
}
