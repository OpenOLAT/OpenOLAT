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

package org.olat.course.nodes.co;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;
import org.olat.course.condition.AreaSelectionController;
import org.olat.course.condition.GroupSelectionController;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.ui.NewAreaController;
import org.olat.group.ui.NewBGController;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;

/**
 * Description:<BR/> Configuration form for the contact form building block
 * 
 * Initial Date: Oct 13, 2004
 * @author Felix Jost
 */
public class COConfigForm extends FormBasicController {
	
	private SelectionElement wantEmail;
	private TextElement teArElEmailToAdresses;
	
	private TextElement teElSubject;
	private TextElement teArElBody;
	
	private SelectionElement wantGroup;
	private SelectionElement coaches;
	private SelectionElement partips;
	private FormLayoutContainer coachesAndPartips;
	
	private SpacerElement s1, s2, s3;
	
	
	private StaticTextElement easyGroupList;
	private FormLink chooseGroupsLink;
	private FormLink createGroupsLink;
	
	private StaticTextElement easyAreaList;
	private FormLink chooseAreasLink;
	private FormLink createAreasLink;
	
	private NewAreaController areaCreateCntrllr;
	private NewBGController groupCreateCntrllr;
	private AreaSelectionController areaChooseC;
	private GroupSelectionController groupChooseC;
	
	
	private FormLayoutContainer areaChooseSubContainer, groupChooseSubContainer ;
	private FormItemContainer groupsAndAreasSubContainer;
	private FormItemContainer recipentsContainer;
	
	private FormLink fixGroupError;
	private FormLink fixAreaError;
	
	private FormSubmit subm;
	
	private boolean hasAreas;
	private boolean hasGroups;
	
	private CloseableModalController cmc;
	
	private List<String> eList;
	private ModuleConfiguration config;
	private CourseEditorEnv cev;
	
	private final BGAreaManager areaManager;
	private final BusinessGroupService businessGroupService;
	
	private final boolean managedGroups;

	/**
	 * Form constructor
	 * 
	 * @param name The form name
	 * @param config The module configuration
	 * @param withCancel true: cancel button is rendered, false: no cancel button
	 */
	protected COConfigForm(UserRequest ureq, WindowControl wControl, ModuleConfiguration config, UserCourseEnvironment uce) {
		super(ureq, wControl);
		this.config = config;
		this.cev = uce.getCourseEditorEnv();
		areaManager = CoreSpringFactory.getImpl(BGAreaManager.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);

		OLATResource courseResource = cev.getCourseGroupManager().getCourseResource();
		RepositoryEntry courseRe = RepositoryManager.getInstance().lookupRepositoryEntry(courseResource, false);
		managedGroups = RepositoryEntryManagedFlag.isManaged(courseRe, RepositoryEntryManagedFlag.groups);
		
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.components.Form#validate(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		if (!wantGroup.isSelected(0) && !wantEmail.isSelected(0)) {
			s3.setVisible(true);
			recipentsContainer.setErrorKey("no.recipents.specified", null);
			return false;
		}
		s3.setVisible(false);
		recipentsContainer.clearError();
		
		
		coachesAndPartips.clearError();
		if (wantGroup.isSelected(0)) {
			if (!coaches.isSelected(0) && !partips.isSelected(0)) {
				coachesAndPartips.setErrorKey("form.choose.coachesandpartips", null);
			}
			if (!validateGroupFields()) return false;
		}
		
		/*
		 * somehow e-mail recipients must be specified, checking each of the
		 * possibility, at least one must be configured resulting in some e-mails.
		 * The case that the specified groups can contain zero members must be
		 * handled by the e-mail controller!
		 */
		String emailToAdresses = teArElEmailToAdresses.getValue();
		String[] emailAdress = emailToAdresses.split("\\s*\\r?\\n\\s*");
		
		teArElEmailToAdresses.clearError();
		if (wantEmail.isSelected(0) &&
				(emailAdress == null || emailAdress.length == 0|| "".equals(emailAdress[0]))) {
			// otherwise the entry field shows that no e-mails are specified
			teArElEmailToAdresses.setErrorKey("email.not.specified", null);
			return false;
		}
		
		//check validity of manually provided e-mails
		if ((emailAdress != null) && (emailAdress.length > 0) && (!"".equals(emailAdress[0]))) {
			this.eList = new ArrayList<String>();
			for (int i = 0; i < emailAdress.length; i++) {
				String eAd = emailAdress[i].trim();
				boolean emailok = MailHelper.isValidEmailAddress(eAd);
				if (emailok == false) {
					teArElEmailToAdresses.setErrorKey("email.not.valid", null);
					return false;
				}
				eList.add(eAd);
			}
		}
		return true;
	}
	
	private boolean validateGroupFields () {
		boolean retVal = true;
		List<Long> activeGroupSelection = null;
		List<Long> activeAreaSelection = null;

		if (!isEmpty(easyGroupList)) {
			// check whether groups exist
			activeGroupSelection = getKeys(easyGroupList);
			
			Set<Long> missingGroups = new HashSet<Long>();
			List<BusinessGroupShort> existingGroups =  businessGroupService.loadShortBusinessGroups(activeGroupSelection);
			a_a:
			for(Long activeGroupKey:activeGroupSelection) {
				for(BusinessGroupShort group:existingGroups) {
					if(group.getKey().equals(activeGroupKey)) {
						continue a_a;
					}
				}
				missingGroups.add(activeGroupKey);
			}
			
			if (!missingGroups.isEmpty()) {
				retVal = false;
				String labelKey = missingGroups.size() == 1 ? "error.notfound.name" : "error.notfound.names";
				String csvMissGrps = toString(missingGroups);
				String[] params = new String[] { "-", csvMissGrps };

				// create error with link to fix it
				String vc_errorPage = velocity_root + "/erroritem.html";
				FormLayoutContainer errorGroupItemLayout = FormLayoutContainer.createCustomFormLayout(
						"errorgroupitem", getTranslator(), vc_errorPage);

				groupChooseSubContainer.setErrorComponent(errorGroupItemLayout, this.flc);
				// FIXING LINK ONLY IF A DEFAULTCONTEXT EXISTS
				fixGroupError = new FormLinkImpl("error.fix", "create");
				// link
				fixGroupError.setCustomEnabledLinkCSS("b_button");
				errorGroupItemLayout.add(fixGroupError);

				fixGroupError.setErrorKey(labelKey, params);
				fixGroupError.showError(true);
				fixGroupError.showLabel(false);
				// hinty to pass the information if one group is
				// missing or if 2 or more groups are missing
				// (see fixGroupErrer.getUserObject to understand)
				// e.g. if userobject String[].lenght == 1 -> one group only
				// String[].lenght > 1 -> show bulkmode creation group
				if (missingGroups.size() > 1) {
					fixGroupError.setUserObject(new String[] { csvMissGrps, "dummy" });
				} else {
					fixGroupError.setUserObject(new String[] { csvMissGrps });
				}

				groupChooseSubContainer.showError(true);
			} else {
				// no more errors
				groupChooseSubContainer.clearError();
			}
		}
		if (!isEmpty(easyAreaList)) {
			// check whether areas exist
			activeAreaSelection = getKeys(easyAreaList);

			List<Long> missingAreas = new ArrayList<Long>();
			List<BGArea> cnt = areaManager.loadAreas(activeAreaSelection);
			a_a:
			for(Long activeAreaKey:activeAreaSelection) {
				for (BGArea element : cnt) {
					if (element.getKey().equals(activeAreaKey)) { 
						continue a_a;
					}
				}
				missingAreas.add(activeAreaKey);
			}

			if (!missingAreas.isEmpty()) {
				retVal = false;
				String labelKey = missingAreas.size() == 1 ? "error.notfound.name" : "error.notfound.names";
				String csvMissAreas = toString(missingAreas);
				String[] params = new String[] { "-", csvMissAreas };

				// create error with link to fix it
				String vc_errorPage = velocity_root + "/erroritem.html";
				FormLayoutContainer errorAreaItemLayout = FormLayoutContainer.createCustomFormLayout(
						"errorareaitem", getTranslator(), vc_errorPage
				);
				
				areaChooseSubContainer.setErrorComponent(errorAreaItemLayout, this.flc);
				// FXINGIN LINK ONLY IF DEFAULT CONTEXT EXISTS
				fixAreaError = new FormLinkImpl("error.fix", "create");// erstellen
				// link
				fixAreaError.setCustomEnabledLinkCSS("b_button");
				errorAreaItemLayout.add(fixAreaError);

				fixAreaError.setErrorKey(labelKey, params);
				fixAreaError.showError(true);
				fixAreaError.showLabel(false);
				
				// hint to pass the information if one area is
				// missing or if 2 or more areas are missing
				// (see fixGroupErrer.getUserObject to understand)
				// e.g. if userobject String[].lenght == 1 -> one group only
				// String[].lenght > 1 -> show bulkmode creation group
				if (missingAreas.size() > 1) {
					fixAreaError.setUserObject(new String[] { csvMissAreas, "dummy" });
				} else {
					fixAreaError.setUserObject(new String[] { csvMissAreas });
				}
			
				areaChooseSubContainer.showError(true);
			} else {
				areaChooseSubContainer.clearError();
			}	
		}

		boolean easyGroupOK = activeGroupSelection != null && !activeGroupSelection.isEmpty();
		boolean easyAreaOK = activeAreaSelection != null && !activeAreaSelection.isEmpty();
		if (easyGroupOK || easyAreaOK) {
			// clear general error
			flc.clearError();
		} else {
			// error concerns both fields -> set it as switch error
			groupsAndAreasSubContainer.setErrorKey("form.noGroupsOrAreas", null);
			retVal = false;
		}
		
		//
		boolean needsGroupsOrAreas = coaches.isSelected(0) || partips.isSelected(0);
		if (needsGroupsOrAreas && !easyGroupOK && !easyAreaOK) {
			groupsAndAreasSubContainer.setErrorKey("form.noGroupsOrAreas", null);
		} else {
			groupsAndAreasSubContainer.clearError();
		}
			
		if (retVal) {
			areaChooseSubContainer.clearError();
			groupChooseSubContainer.clearError();
			groupsAndAreasSubContainer.clearError();
		}
		
		return retVal;
	}
	
	
	/**
	 * @return the message subject
	 */
	protected String getMSubject() {
		return teElSubject.getValue();
	}

	/**
	 * @return the meesage body
	 */
	protected String getMBody() {
		return teArElBody.getValue();
	}

	/**
	 * @return the email list
	 */
	protected List<String> getEmailList() {
		return eList;
	}

	/**
	 * returns the choosen groups, or null if no groups were choosen.
	 * 
	 * @return
	 */
	protected String getEmailGroups() {
		if (!isEmpty(easyGroupList)) {
			return easyGroupList.getValue();
		}
		return null;
	}
	
	protected List<Long> getEmailGroupIds() {
		if (!isEmpty(easyGroupList)) {
			return getKeys(easyGroupList);
		}
		return null;
	}

	/**
	 * returns the choosen learning areas, or null if no ares were choosen.
	 */
	protected String getEmailAreas() {
		if(!isEmpty(easyAreaList)) {
			return easyAreaList.getValue();
		}
		return null;
	}
	
	protected List<Long> getEmailAreaIds() {
		if(!isEmpty(easyAreaList)) {
			return getKeys(easyAreaList);
		}
		return null;
	}

	protected boolean sendToCoaches() {
		return coaches.isSelected(0);
	}

	protected boolean sendToPartips() {
		return partips.isSelected(0);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("header", null);
		setFormContextHelp("org.olat.course.nodes.co","ced-co.html","help.hover.co");
		
		wantGroup = uifactory.addCheckboxesVertical("wantGroup", "message.want.group", formLayout, new String[]{"xx"},new String[]{null} , null, 1);
		wantGroup.addActionListener(this, FormEvent.ONCLICK);
		
		coaches = uifactory.addCheckboxesVertical("coaches", "form.message.chckbx.coaches", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		coaches.select("xx", config.getBooleanEntry(COEditController.CONFIG_KEY_EMAILTOCOACHES));
		partips = uifactory.addCheckboxesVertical("partips", "form.message.chckbx.partips", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		partips.select("xx", config.getBooleanEntry(COEditController.CONFIG_KEY_EMAILTOPARTICIPANTS));
	
		wantGroup.select("xx", coaches.isSelected(0) || partips.isSelected(0));
		
		coachesAndPartips = FormLayoutContainer.createHorizontalFormLayout(
				"coachesAndPartips", getTranslator()
		);
		formLayout.add(coachesAndPartips);
		s1 = uifactory.addSpacerElement("s1", formLayout, true);
		
		// groups
		groupChooseSubContainer = FormLayoutContainer.createHorizontalFormLayout(
				"groupChooseSubContainer", getTranslator()
		);
		groupChooseSubContainer.setLabel("form.message.group", null);
		
		formLayout.add(groupChooseSubContainer);
		
		String groupInitVal;
		@SuppressWarnings("unchecked")
		List<Long> groupKeys = (List<Long>)config.get(COEditController.CONFIG_KEY_EMAILTOGROUP_IDS);
		if(groupKeys == null) {
			groupInitVal = (String)config.get(COEditController.CONFIG_KEY_EMAILTOGROUPS);
			groupKeys = businessGroupService.toGroupKeys(groupInitVal, cev.getCourseGroupManager().getCourseResource());
		}
		groupInitVal = getGroupNames(groupKeys);

		easyGroupList = uifactory.addStaticTextElement("group", null, groupInitVal, groupChooseSubContainer);
		easyGroupList.setUserObject(groupKeys);
		
		chooseGroupsLink = uifactory.addFormLink("choose", groupChooseSubContainer,"b_form_groupchooser");
		createGroupsLink = uifactory.addFormLink("create", groupChooseSubContainer,"b_form_groupchooser");	

		hasGroups = businessGroupService.countBusinessGroups(null, cev.getCourseGroupManager().getCourseResource()) > 0;
		
		// areas
		areaChooseSubContainer = FormLayoutContainer.createHorizontalFormLayout(
				"areaChooseSubContainer", getTranslator()
		);
		areaChooseSubContainer.setLabel("form.message.area", null);
		formLayout.add(areaChooseSubContainer);		
		
		groupsAndAreasSubContainer = FormLayoutContainer.createHorizontalFormLayout("groupSubContainer", getTranslator());
		formLayout.add(groupsAndAreasSubContainer);
		
		String areaInitVal;
		@SuppressWarnings("unchecked")
		List<Long> areaKeys = (List<Long>)config.get(COEditController.CONFIG_KEY_EMAILTOAREA_IDS);
		if(areaKeys == null) {
			areaInitVal = (String)config.get(COEditController.CONFIG_KEY_EMAILTOAREAS);
			areaKeys = areaManager.toAreaKeys(areaInitVal, cev.getCourseGroupManager().getCourseResource());
		}
		areaInitVal = getAreaNames(areaKeys);

		easyAreaList = uifactory.addStaticTextElement("area", null, areaInitVal, areaChooseSubContainer);
		easyAreaList.setUserObject(areaKeys);
		
		chooseAreasLink = uifactory.addFormLink("choose", areaChooseSubContainer,"b_form_groupchooser");
		createAreasLink = uifactory.addFormLink("create", areaChooseSubContainer,"b_form_groupchooser");
		
		hasAreas = areaManager.countBGAreasInContext(cev.getCourseGroupManager().getCourseResource()) > 0;
		
		s2 = uifactory.addSpacerElement("s2", formLayout, false);
		
		wantEmail = uifactory.addCheckboxesVertical("wantEmail", "message.want.email", formLayout, new String[]{"xx"},new String[]{null} , null, 1);
		wantEmail.addActionListener(this, FormEvent.ONCLICK);
		
		//recipients
		eList = (List<String>) config.get(COEditController.CONFIG_KEY_EMAILTOADRESSES);
		String emailToAdresses = "";
		if (eList != null) {
			emailToAdresses = StringHelper.formatIdentitesAsEmailToString(eList, "\n");
			wantEmail.select("xx", eList.size()>0);
		}
		teArElEmailToAdresses = uifactory.addTextAreaElement("email", "message.emailtoadresses", -1, 3, 60, true, emailToAdresses, formLayout);
		teArElEmailToAdresses.setMandatory(true);
		
		s3 =uifactory.addSpacerElement("s3", formLayout, true);
		
		//for displaying error message in case neither group stuff nor email is selected
		recipentsContainer = FormLayoutContainer.createHorizontalFormLayout(
				"recipents", getTranslator()
		);
		formLayout.add(recipentsContainer);
		
		uifactory.addSpacerElement("s4", formLayout, false);
				
		//subject
		String mS = (String) config.get(COEditController.CONFIG_KEY_MSUBJECT_DEFAULT);
		String mSubject = (mS != null) ? mS : "";
		teElSubject = uifactory.addTextElement("mSubject", "message.subject", 255, mSubject, formLayout);
		
		//messagebody
		String mB = (String) config.get(COEditController.CONFIG_KEY_MBODY_DEFAULT);
		String mBody = (mB != null) ? mB : "";
		teArElBody = uifactory.addTextAreaElement("mBody", "message.body", 10000, 8, 60, true, mBody, formLayout);
		
		subm = uifactory.addFormSubmitButton("save", formLayout);
		
		update();
	}

	private void update () {
		
		boolean wg = wantGroup.isSelected(0);
		coaches.setVisible(wg);
		partips.setVisible(wg);
		coachesAndPartips.setVisible(wg);
		
		groupChooseSubContainer.setVisible(wg);
		areaChooseSubContainer.setVisible(wg);
		
		s1.setVisible(wg);
		s2.setVisible(wg);
		
		if (!wg) {
			coaches.select("xx", false);
			partips.select("xx", false);
			easyAreaList.setValue("");
			easyAreaList.setUserObject(null);
			easyGroupList.setValue("");
			easyGroupList.setUserObject(null);
		}
		
		hasGroups = businessGroupService.countBusinessGroups(null, cev.getCourseGroupManager().getCourseResource()) > 0;
		chooseGroupsLink.setVisible(wg &&  hasGroups);
		createGroupsLink.setVisible(wg && !hasGroups && !managedGroups);
		
		hasAreas = areaManager.countBGAreasInContext(cev.getCourseGroupManager().getCourseResource()) > 0;
		chooseAreasLink.setVisible(wg &&  hasAreas);
		createAreasLink.setVisible(wg && !hasAreas);	
		
		teArElEmailToAdresses.setVisible(wantEmail.isSelected(0));
		teArElEmailToAdresses.clearError();
		if (!wantEmail.isSelected(0)) {
			teArElEmailToAdresses.setValue("");
			eList = null;
		}
		
		recipentsContainer.clearError();
		flc.setDirty(true);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == chooseGroupsLink) {
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(groupChooseC);
			
			groupChooseC = new GroupSelectionController(ureq, getWindowControl(), "group",
					cev.getCourseGroupManager(), getKeys(easyGroupList));
			listenTo(groupChooseC);

			cmc = new CloseableModalController(getWindowControl(), "close", groupChooseC.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
			subm.setEnabled(false);
		} else if (source == createGroupsLink) {
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(groupCreateCntrllr);
			
			// no groups in group management -> directly show group create dialog
			OLATResource courseResource = cev.getCourseGroupManager().getCourseResource();
			RepositoryEntry courseRe = RepositoryManager.getInstance().lookupRepositoryEntry(courseResource, false);
			groupCreateCntrllr = new NewBGController(ureq, getWindowControl(), courseRe, true, null);
			listenTo(groupCreateCntrllr);
			cmc = new CloseableModalController(getWindowControl(), "close", groupCreateCntrllr.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
			subm.setEnabled(false);
		} else if (source == chooseAreasLink) {
			// already areas -> choose areas
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(areaChooseC);
			
			areaChooseC = new AreaSelectionController (ureq, getWindowControl(), "area",
					cev.getCourseGroupManager(), getKeys(easyAreaList));
			listenTo(areaChooseC);

			cmc = new CloseableModalController(getWindowControl(), "close", areaChooseC.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
			subm.setEnabled(false);
		} else if (source == createAreasLink) {
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(areaCreateCntrllr);
			
			// no areas -> directly show creation dialog
			OLATResource courseResource = cev.getCourseGroupManager().getCourseResource();
			areaCreateCntrllr = new NewAreaController(ureq, getWindowControl(), courseResource, true, null);
			listenTo(areaCreateCntrllr);
			cmc = new CloseableModalController(getWindowControl(), "close", areaCreateCntrllr.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
			subm.setEnabled(false);
		} else if (source == fixGroupError) {
			// user wants to fix problem with fixing group error link e.g. create one or more group at once.
			String[] csvGroupName = (String[]) fixGroupError.getUserObject();
			OLATResource courseResource = cev.getCourseGroupManager().getCourseResource();
			RepositoryEntry courseRe = RepositoryManager.getInstance().lookupRepositoryEntry(courseResource, false);
			easyGroupList.setEnabled(false);
			removeAsListenerAndDispose(groupCreateCntrllr);
			groupCreateCntrllr = new NewBGController(ureq, getWindowControl(), courseRe, true, csvGroupName[0]);
			listenTo(groupCreateCntrllr);

			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(
					getWindowControl(), "close",
					groupCreateCntrllr.getInitialComponent()
			);
			listenTo(cmc);
			
			cmc.activate();
			subm.setEnabled(false);
			
		} else if (source == fixAreaError) {
			// user wants to fix problem with fixing area error link e.g. create one or more areas at once.
			String[] csvAreaName = (String[]) fixAreaError.getUserObject();
			OLATResource courseResource = cev.getCourseGroupManager().getCourseResource();
			easyAreaList.setEnabled(false);
			removeAsListenerAndDispose(areaCreateCntrllr);
			areaCreateCntrllr = new NewAreaController(ureq, getWindowControl(), courseResource, true, csvAreaName[0]);
			listenTo(areaCreateCntrllr);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), "close", areaCreateCntrllr.getInitialComponent());
			listenTo(cmc);
			
			cmc.activate();
			subm.setEnabled(false);
		}
		update();
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		subm.setEnabled(true);
		if (source == groupChooseC) {
			if (event == Event.DONE_EVENT) {
				cmc.deactivate();
				easyGroupList.setValue(StringHelper.formatAsSortUniqCSVString(groupChooseC.getSelectedNames()));
				easyGroupList.setUserObject(groupChooseC.getSelectedKeys());
				easyGroupList.getRootForm().submit(ureq);
			} else if (Event.CANCELLED_EVENT == event) {
				cmc.deactivate();
			}
		} else if (source == areaChooseC) {
			if (event == Event.DONE_EVENT) {
				cmc.deactivate();
				easyAreaList.setValue(StringHelper.formatAsSortUniqCSVString(areaChooseC.getSelectedNames()));
				easyAreaList.setUserObject(areaChooseC.getSelectedKeys());
				easyAreaList.getRootForm().submit(ureq);
			} else if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			}
		} else if (source == groupCreateCntrllr) {
			easyGroupList.setEnabled(true);
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				List<Long> c = new ArrayList<Long>();
				c.addAll(getKeys(easyGroupList));
				if (fixGroupError != null && fixGroupError.getUserObject() != null) {
					c.removeAll(Arrays.asList((String[])fixGroupError.getUserObject()));
				}
				c.addAll(groupCreateCntrllr.getCreatedGroupKeys());
				easyGroupList.setValue(getGroupNames(c));
				easyGroupList.setUserObject(c);
				
				if (groupCreateCntrllr.getCreatedGroupNames().size() > 0 && !hasGroups) {
					chooseGroupsLink.setVisible(true);
					createGroupsLink.setVisible(false);
				}
				easyGroupList.getRootForm().submit(ureq);
			}
		} else if (source == areaCreateCntrllr) {
			easyAreaList.setEnabled(true);
			cmc.deactivate();
			
			if (event == Event.DONE_EVENT) {
				List<Long> c = new ArrayList<Long>();
				c.addAll(getKeys(easyAreaList));
				if (fixAreaError != null && fixAreaError.getUserObject() != null) {
					c.removeAll(Arrays.asList((String[])fixAreaError.getUserObject()));
				}
				c.addAll(areaCreateCntrllr.getCreatedAreaKeys());
				
				easyAreaList.setValue(getAreaNames(c));
				easyAreaList.setUserObject(c);
				
				if (areaCreateCntrllr.getCreatedAreaNames().size() > 0 && !hasAreas) {
					chooseAreasLink.setVisible(true);
					createAreasLink.setVisible(false);
				}
				easyAreaList.getRootForm().submit(ureq);
			} 
		}
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private boolean isEmpty(StaticTextElement element) {
		List<Long> keys = getKeys(element);
		if(keys == null || keys.isEmpty()) {
			return true;
		}
		return false;
	}
	
	private List<Long> getKeys(StaticTextElement element) {
		@SuppressWarnings("unchecked")
		List<Long> keys = (List<Long>)element.getUserObject();
		if(keys == null) {
			keys = new ArrayList<Long>();
			element.setUserObject(keys);
		}
		return keys;
	}
	
	private String toString(Collection<Long> keys) {
		StringBuilder sb = new StringBuilder();
		for(Long key:keys) {
			if(sb.length() > 0) sb.append(',');
			sb.append(key);
		}
		return sb.toString();
	}
	
	private String getGroupNames(List<Long> keys) {
		StringBuilder sb = new StringBuilder();
		List<BusinessGroupShort> groups = businessGroupService.loadShortBusinessGroups(keys);
		for(BusinessGroupShort group:groups) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(group.getName());
		}
		return sb.toString();
	}
	
	private String getAreaNames(List<Long> keys) {
		StringBuilder sb = new StringBuilder();
		List<BGArea> areas = areaManager.loadAreas(keys);
		for(BGArea area:areas) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(area.getName());
		}
		return sb.toString();
	}
}
