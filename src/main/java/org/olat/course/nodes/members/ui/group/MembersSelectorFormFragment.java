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
package org.olat.course.nodes.members.ui.group;

import static org.olat.core.gui.components.util.KeyValues.entry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.condition.AreaSelectionController;
import org.olat.course.condition.CurriculumElementSelectionController;
import org.olat.course.condition.GroupSelectionController;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Reusable Form Fragment for dealing with course membership selection 
 * 
 * <p>Initial date: May 6, 2016<br>
 * @author lmihalkovic, http://www.frentix.com
 */
public abstract class MembersSelectorFormFragment extends FormBasicController {
	
	private static final String[] onKeys = new String[]{ "xx" };
	private static final String MEMEBER_KEY_ALL = "all";
	private static final String MEMEBER_KEY_COURSE = "course";
	private static final String MEMEBER_KEY_GROUP = "group";
	private static final String MEMEBER_KEY_CURRICULUM_ELEMENT = "curriculumElement";
	private static final String MEMEBER_KEY_ASSIGNED = "assigned";

	// Coaches
	private SelectionElement wantCoaches;
	private SingleSelection coachesChoice;

	private FormLink chooseGroupCoachesLink;
	private GroupSelectionController groupChooseCoaches;
	private StaticTextElement easyGroupCoachSelectionList;
	
	private FormLink chooseAreasCoachesLink;
	private AreaSelectionController areaChooseCoaches;
	private StaticTextElement easyAreaCoachSelectionList;
	
	private FormLink chooseCurriculumElementsCoachesLink;
	private CurriculumElementSelectionController curriculumElementsChooseCoaches;
	private StaticTextElement easyCurriculumElementCoachesSelectionList;
	
	// Participants
	private SelectionElement wantParticipants;
	private SingleSelection participantsChoice;
	
	private FormLink chooseGroupParticipantsLink;	
	private GroupSelectionController groupChooseParticipants;
	private StaticTextElement easyGroupParticipantsSelectionList;

	private FormLink chooseAreasParticipantsLink;
	private AreaSelectionController areaChooseParticipants;
	private StaticTextElement easyAreaParticipantsSelectionList;
	
	private FormLink chooseCurriculumElementsParticipantsLink;
	private CurriculumElementSelectionController curriculumElementsChooseParticipants;
	private StaticTextElement easyCurriculumElementParticipantsSelectionList;
	
	// Popup form
	private CloseableModalController cmc;

	private final boolean withCurriculum;
	private final boolean withAssignedCoaches;
	private final CourseEditorEnv cev;
	protected final ModuleConfiguration config;
	
	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	public MembersSelectorFormFragment(UserRequest ureq, WindowControl wControl,
			CourseEditorEnv cev, ModuleConfiguration config, boolean withCurriculum, boolean withAssignedCoaches) {
		super(ureq, wControl, Util.createPackageTranslator(MembersSelectorFormFragment.class, ureq.getLocale()));
		this.cev = cev;
		this.config = config;
		this.withAssignedCoaches = withAssignedCoaches;
		this.withCurriculum = withCurriculum && curriculumModule.isEnabled()
				&& !cev.getCourseGroupManager().getAllCurriculumElements().isEmpty();
		
		initForm(ureq);
		validateFormLogic(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// COACHES
		boolean coacheSelection = config.getBooleanSafe(getConfigKeyCoachesAll())
				|| config.getBooleanSafe(getConfigKeyCoachesCourse())
				|| config.getBooleanSafe(getConfigKeyCoachesAssigned())
				|| config.get(getConfigKeyCoachesGroup()) != null
				|| config.get(getConfigKeyCoachesArea()) != null
				|| config.get(getConfigKeyCoachesCurriculumElement()) != null;
		
		wantCoaches = uifactory.addCheckboxesHorizontal("coaches", "message.want.coaches", formLayout, onKeys, new String[]{ "" });
		wantCoaches.setElementCssClass("o_sel_config_want_coaches");
		if(coacheSelection) {
			wantCoaches.select("xx", true);
		}
		wantCoaches.addActionListener(FormEvent.ONCLICK);
		
		KeyValues coachesKV = new KeyValues();
		coachesKV.add(entry(MEMEBER_KEY_ALL, translate("form.message.coaches.all")));
		if (withAssignedCoaches) {
			coachesKV.add(entry(MEMEBER_KEY_ASSIGNED, translate("form.message.coaches.assigned")));
		}
		coachesKV.add(entry(MEMEBER_KEY_COURSE, translate("form.message.coaches.course")));
		coachesKV.add(entry(MEMEBER_KEY_GROUP, translate("form.message.coaches.group")));
		if (withCurriculum) {
			coachesKV.add(entry(MEMEBER_KEY_CURRICULUM_ELEMENT, translate("form.message.coaches.curriculum.element")));
		}
		coachesChoice = uifactory.addRadiosVertical("coachesChoice", null, formLayout, coachesKV.keys(), coachesKV.values());
		coachesChoice.setElementCssClass("o_sel_config_coaches");
		if(config.getBooleanSafe(getConfigKeyCoachesAll())) {
			coachesChoice.select(MEMEBER_KEY_ALL, true);
		}
		if(config.getBooleanSafe(getConfigKeyCoachesCourse())) {
			coachesChoice.select(MEMEBER_KEY_COURSE, true);
		}
		if(config.get(getConfigKeyCoachesGroup()) != null || config.get(getConfigKeyCoachesArea()) != null) {
			coachesChoice.select(MEMEBER_KEY_GROUP, true);
		}
		if(config.get(getConfigKeyCoachesCurriculumElement()) != null) {
			coachesChoice.select(MEMEBER_KEY_CURRICULUM_ELEMENT, true);
		}
		if(config.getBooleanSafe(getConfigKeyCoachesAssigned())) {
			coachesChoice.select(MEMEBER_KEY_ASSIGNED, true);
		}
		coachesChoice.addActionListener(FormEvent.ONCLICK);
		coachesChoice.setVisible(false);
		
		
		chooseGroupCoachesLink = uifactory.addFormLink("groupCoachesChoose", formLayout, "btn btn-default o_xsmall o_form_groupchooser");
		chooseGroupCoachesLink.setIconLeftCSS("o_icon o_icon-fw o_icon_group");
		chooseGroupCoachesLink.setVisible(false);
		chooseGroupCoachesLink.setLabel("form.message.group", null);
		if(!cev.getCourseGroupManager().hasBusinessGroups()){
			chooseGroupCoachesLink.setI18nKey("groupCreate");
		}
		
		chooseGroupCoachesLink.setElementCssClass("o_omit_margin");

		String groupCoachesInitVal;
		List<Long> groupCoachesKeys = config.getList(getConfigKeyCoachesGroupIds(), Long.class);
		if(groupCoachesKeys == null) {
			groupCoachesInitVal = config.getStringValue(getConfigKeyCoachesGroup());
			groupCoachesKeys = businessGroupService.toGroupKeys(groupCoachesInitVal, cev.getCourseGroupManager().getCourseEntry());
		}
		groupCoachesInitVal = getGroupNames(groupCoachesKeys);

		easyGroupCoachSelectionList = uifactory.addStaticTextElement("groupCoaches", null, groupCoachesInitVal, formLayout);
		easyGroupCoachSelectionList.setUserObject(groupCoachesKeys);		
		easyGroupCoachSelectionList.setVisible(false);
		easyGroupCoachSelectionList.setElementCssClass("text-muted");
		
				
		chooseAreasCoachesLink = uifactory.addFormLink("areaCoachesChoose", formLayout, "btn btn-default o_xsmall o_form_areachooser");
		chooseAreasCoachesLink.setIconLeftCSS("o_icon o_icon-fw o_icon_courseareas");
		chooseAreasCoachesLink.setLabel("form.message.area", null);
		chooseAreasCoachesLink.setElementCssClass("o_omit_margin");
		if(!cev.getCourseGroupManager().hasAreas()){
			chooseAreasCoachesLink.setI18nKey("areaCreate");
		}
		
		String areaCoachesInitVal;
		List<Long> areaCoachesKeys = config.getList(getConfigKeyCoachesAreaIds(), Long.class);
		if(areaCoachesKeys == null) {
			areaCoachesInitVal = (String)config.get(getConfigKeyCoachesArea());
			areaCoachesKeys = areaManager.toAreaKeys(areaCoachesInitVal, cev.getCourseGroupManager().getCourseResource());
		}
		areaCoachesInitVal = getAreaNames(areaCoachesKeys);

		easyAreaCoachSelectionList = uifactory.addStaticTextElement("areaCoaches", null, areaCoachesInitVal, formLayout);
		easyAreaCoachSelectionList.setUserObject(areaCoachesKeys);
		easyAreaCoachSelectionList.setVisible(false);
		easyAreaCoachSelectionList.setElementCssClass("text-muted");
		
		
		chooseCurriculumElementsCoachesLink = uifactory.addFormLink("curriculumElementsCoachesChoose", formLayout, "btn btn-default o_xsmall o_form_areachooser");
		chooseCurriculumElementsCoachesLink.setIconLeftCSS("o_icon o_icon-fw o_icon_curriculum_element");
		chooseCurriculumElementsCoachesLink.setVisible(false);
		chooseCurriculumElementsCoachesLink.setLabel("form.message.curriculum.element", null);
		chooseCurriculumElementsCoachesLink.setElementCssClass("o_omit_margin");
		
		List<Long> curriculumElementsCoachesKeys = config.getList(getConfigKeyCoachesCurriculumElementIds(), Long.class);
		String curriculumElementsCoachesInitVal = getCurriculumElementNames(curriculumElementsCoachesKeys);

		easyCurriculumElementCoachesSelectionList = uifactory.addStaticTextElement("curriculumElementsCoaches", null, curriculumElementsCoachesInitVal, formLayout);
		easyCurriculumElementCoachesSelectionList.setUserObject(curriculumElementsCoachesKeys);
		easyCurriculumElementCoachesSelectionList.setVisible(false);
		easyCurriculumElementCoachesSelectionList.setElementCssClass("text-muted");
		
		
		// PARTICIPANTS
		boolean particiapntSelection = config.getBooleanSafe(getConfigKeyParticipantsAll())
				|| config.getBooleanSafe(getConfigKeyParticipantsCourse())
				|| config.get(getConfigKeyParticipantsGroup()) != null
				|| config.get(getConfigKeyParticipantsArea()) != null
				|| config.get(getConfigKeyParticipantsCurriculumElement()) != null;
		
		wantParticipants = uifactory.addCheckboxesHorizontal("participants", "message.want.participants", formLayout, onKeys,new String[]{null});
		wantParticipants.setElementCssClass("o_sel_config_want_participants");
		if(particiapntSelection) wantParticipants.select("xx", true);
		wantParticipants.addActionListener(FormEvent.ONCLICK);
		
		KeyValues participantKV = new KeyValues();
		participantKV.add(entry(MEMEBER_KEY_ALL, translate("form.message.participants.all")));
		participantKV.add(entry(MEMEBER_KEY_COURSE, translate("form.message.participants.course")));
		participantKV.add(entry(MEMEBER_KEY_GROUP, translate("form.message.participants.group")));
		if (withCurriculum) {
			participantKV.add(entry(MEMEBER_KEY_CURRICULUM_ELEMENT, translate("form.message.participants.curriculum.element")));
		}
		participantsChoice = uifactory.addRadiosVertical("participantsChoice", null, formLayout, participantKV.keys(), participantKV.values());
		participantsChoice.setElementCssClass("o_sel_config_participants");
		if(config.getBooleanSafe(getConfigKeyParticipantsAll())) {
			participantsChoice.select(MEMEBER_KEY_ALL, true);
		}
		if(config.getBooleanSafe(getConfigKeyParticipantsCourse())) {
			participantsChoice.select(MEMEBER_KEY_COURSE, true);
		}
		if(config.get(getConfigKeyParticipantsGroup()) != null || config.get(getConfigKeyParticipantsArea()) != null) {
			participantsChoice.select(MEMEBER_KEY_GROUP, true);
		}
		if(config.get(getConfigKeyParticipantsCurriculumElement()) != null) {
			participantsChoice.select(MEMEBER_KEY_CURRICULUM_ELEMENT, true);
		}
		
		participantsChoice.addActionListener(FormEvent.ONCLICK);
		participantsChoice.setVisible(false); 
		
		chooseGroupParticipantsLink = uifactory.addFormLink("groupParticipantsChoose", formLayout, "btn btn-default o_xsmall o_form_groupchooser");
		chooseGroupParticipantsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_group");
		chooseGroupParticipantsLink.setVisible(false);
		chooseGroupParticipantsLink.setLabel("form.message.group", null);
		chooseGroupParticipantsLink.setElementCssClass("o_omit_margin");

		if(cev.getCourseGroupManager().getAllBusinessGroups().isEmpty()){
			chooseGroupParticipantsLink.setI18nKey("groupCreate");
		}
		
		String groupParticipantsInitVal;
		List<Long> groupParticipantsKeys = config.getList(getConfigKeyParticipantsGroupIds(), Long.class);
		if(groupParticipantsKeys == null) {// fallback for backwards compatibility
			groupParticipantsInitVal = (String)config.get(getConfigKeyParticipantsGroup());
			groupParticipantsKeys = businessGroupService.toGroupKeys(groupParticipantsInitVal, cev.getCourseGroupManager().getCourseEntry());
		}
		groupParticipantsInitVal = getGroupNames(groupParticipantsKeys);

		easyGroupParticipantsSelectionList = uifactory.addStaticTextElement("groupParticipants", null, groupParticipantsInitVal, formLayout);
		easyGroupParticipantsSelectionList.setUserObject(groupParticipantsKeys);
		easyGroupParticipantsSelectionList.setVisible(false);
		easyGroupParticipantsSelectionList.setElementCssClass("text-muted");
		
		
		chooseAreasParticipantsLink = uifactory.addFormLink("areaParticipantsChoose", formLayout, "btn btn-default o_xsmall o_form_areachooser");
		chooseAreasParticipantsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_courseareas");
		chooseAreasParticipantsLink.setVisible(false);
		chooseAreasParticipantsLink.setLabel("form.message.area", null);
		chooseAreasParticipantsLink.setElementCssClass("o_omit_margin");

		if(cev.getCourseGroupManager().getAllAreas().isEmpty()){
			chooseAreasParticipantsLink.setI18nKey("areaCreate");
		}
		
		String areaParticipantsInitVal;
		List<Long> areaParticipantsKeys = config.getList(getConfigKeyParticipantsAreaIds(), Long.class);
		if(areaParticipantsKeys == null) {// fallback for backwards compatibility
			areaParticipantsInitVal = (String)config.get(getConfigKeyParticipantsArea());
			areaParticipantsKeys = areaManager.toAreaKeys(areaParticipantsInitVal, cev.getCourseGroupManager().getCourseResource());
		}
		areaParticipantsInitVal = getAreaNames(areaParticipantsKeys);

		easyAreaParticipantsSelectionList = uifactory.addStaticTextElement("areaParticipants", null, areaParticipantsInitVal, formLayout);
		easyAreaParticipantsSelectionList.setUserObject(areaParticipantsKeys);
		easyAreaParticipantsSelectionList.setVisible(false);
		easyAreaParticipantsSelectionList.setElementCssClass("text-muted");
		
		chooseCurriculumElementsParticipantsLink = uifactory.addFormLink("curriculumElementsParticipantsChoose", formLayout, "btn btn-default o_xsmall o_form_areachooser");
		chooseCurriculumElementsParticipantsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_curriculum_element");
		chooseCurriculumElementsParticipantsLink.setVisible(false);
		chooseCurriculumElementsParticipantsLink.setLabel("form.message.curriculum.element", null);
		chooseCurriculumElementsParticipantsLink.setElementCssClass("o_omit_margin");
		
		List<Long> curriculumElementsParticipantsKeys = config.getList(getConfigKeyParticipantsCurriculumElementIds(), Long.class);
		String curriculumElementsParticipantsInitVal = getCurriculumElementNames(curriculumElementsParticipantsKeys);

		easyCurriculumElementParticipantsSelectionList = uifactory.addStaticTextElement("curriculumElementsParticipants", null, curriculumElementsParticipantsInitVal, formLayout);
		easyCurriculumElementParticipantsSelectionList.setUserObject(curriculumElementsParticipantsKeys);
		easyCurriculumElementParticipantsSelectionList.setVisible(false);
		easyCurriculumElementParticipantsSelectionList.setElementCssClass("text-muted");
		
	
		uifactory.addSpacerElement("s4", formLayout, false);		
	}
	
	protected void update() {
		// coaches
		boolean coachesSelected = wantCoaches.isSelected(0);
		boolean coachesGroup = coachesChoice.isOneSelected() && MEMEBER_KEY_GROUP.equals(coachesChoice.getSelectedKey());
		boolean coachesCurriculumElement = coachesChoice.isOneSelected() && MEMEBER_KEY_CURRICULUM_ELEMENT.equals(coachesChoice.getSelectedKey());
		coachesChoice.setVisible(coachesSelected);
		chooseGroupCoachesLink.setVisible(coachesSelected && coachesGroup);
		chooseAreasCoachesLink.setVisible(coachesSelected && coachesGroup);
		chooseCurriculumElementsCoachesLink.setVisible(coachesSelected && coachesCurriculumElement);
		easyGroupCoachSelectionList.setVisible(coachesSelected && coachesGroup);
		easyAreaCoachSelectionList.setVisible(coachesSelected && coachesGroup);
		easyCurriculumElementCoachesSelectionList.setVisible(coachesSelected && coachesCurriculumElement);

		// participants
		boolean participantsSelected = wantParticipants.isSelected(0);
		boolean participantsGroup = participantsChoice.isOneSelected() && MEMEBER_KEY_GROUP.equals(participantsChoice.getSelectedKey());
		boolean participantsCurriculumElement = participantsChoice.isOneSelected() && MEMEBER_KEY_CURRICULUM_ELEMENT.equals(participantsChoice.getSelectedKey());
		participantsChoice.setVisible(participantsSelected);
		chooseGroupParticipantsLink.setVisible(participantsSelected && participantsGroup);
		chooseAreasParticipantsLink.setVisible(participantsSelected && participantsGroup);
		chooseCurriculumElementsParticipantsLink.setVisible(participantsSelected && participantsCurriculumElement);
		easyGroupParticipantsSelectionList.setVisible(participantsSelected && participantsGroup);
		easyAreaParticipantsSelectionList.setVisible(participantsSelected && participantsGroup);
		easyCurriculumElementParticipantsSelectionList.setVisible(participantsSelected && participantsCurriculumElement);
		
		easyGroupParticipantsSelectionList.clearError();
		easyAreaParticipantsSelectionList.clearError();
		easyGroupCoachSelectionList.clearError();
		easyAreaCoachSelectionList.clearError();
		
		coachesChoice.clearError();
		participantsChoice.clearError();
		
		flc.setDirty(true);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean isOK = super.validateFormLogic(ureq);

		coachesChoice.clearError();
		easyAreaCoachSelectionList.clearError();
		easyGroupCoachSelectionList.clearError();
		easyCurriculumElementCoachesSelectionList.clearError();
		if(sendToCoaches()) {
			if(!coachesChoice.isOneSelected()){
				coachesChoice.setErrorKey("error.no.choice.specified", null);
				isOK &= false;
			} else if(MEMEBER_KEY_GROUP.equals(coachesChoice.getSelectedKey()) && isEmpty(easyAreaCoachSelectionList) && isEmpty(easyGroupCoachSelectionList)) {
				easyAreaCoachSelectionList.setErrorKey("error.no.group.specified", null);
				easyGroupCoachSelectionList.setErrorKey("error.no.group.specified", null);
				isOK &= false;
			} else if(MEMEBER_KEY_CURRICULUM_ELEMENT.equals(coachesChoice.getSelectedKey()) && isEmpty(easyCurriculumElementCoachesSelectionList)) {
				easyCurriculumElementCoachesSelectionList.setErrorKey("error.no.curriculum.element.specified", null);
				isOK &= false;
			}
		}

		participantsChoice.clearError();
		easyGroupParticipantsSelectionList.clearError();
		easyAreaParticipantsSelectionList.clearError();
		easyCurriculumElementParticipantsSelectionList.clearError();
		if(sendToPartips()) {
			if(!participantsChoice.isOneSelected()) {
				participantsChoice.setErrorKey("error.no.choice.specified", null);
				isOK &= false;
			} else if(MEMEBER_KEY_GROUP.equals(participantsChoice.getSelectedKey()) && isEmpty(easyAreaParticipantsSelectionList) &&  isEmpty(easyGroupParticipantsSelectionList)) {
				easyAreaParticipantsSelectionList.setErrorKey("error.no.group.specified", null);
				easyGroupParticipantsSelectionList.setErrorKey("error.no.group.specified", null);
				isOK &= false;
			} else if(MEMEBER_KEY_CURRICULUM_ELEMENT.equals(participantsChoice.getSelectedKey()) && isEmpty(easyCurriculumElementParticipantsSelectionList)) {
				easyCurriculumElementParticipantsSelectionList.setErrorKey("error.no.curriculum.element.specified", null);
				isOK &= false;
			}
		}

		return isOK;
	}

	@Override
	protected final void formOK(UserRequest ureq) {
		storeConfiguration(config);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	private String getGroupNames(List<Long> keys) {
		StringBuilder sb = new StringBuilder(128);
		List<BusinessGroupShort> groups = businessGroupService.loadShortBusinessGroups(keys);
		for(BusinessGroupShort group:groups) {
			if(sb.length() > 0) sb.append("&nbsp;&nbsp;");
			sb.append("<i class='o_icon o_icon-fw o_icon_group'>&nbsp;</i> ")
			  .append(StringHelper.escapeHtml(group.getName()));
		}
		return sb.toString();
	}
	
	private String getAreaNames(List<Long> keys) {
		StringBuilder sb = new StringBuilder(128);
		List<BGArea> areas = areaManager.loadAreas(keys);
		for(BGArea area:areas) {
			if(sb.length() > 0) sb.append("&nbsp;&nbsp;");
			sb.append("<i class='o_icon o_icon-fw o_icon_courseareas'>&nbsp;</i> ")
			  .append(StringHelper.escapeHtml(area.getName()));
		}
		return sb.toString();
	}
	
	private String getCurriculumElementNames(List<Long> keys) {
		StringBuilder sb = new StringBuilder(128);
		List<CurriculumElementRef> elementRefs = keys.stream()
				.map(CurriculumElementRefImpl::new).collect(Collectors.toList());
		List<CurriculumElement> curriculumElements = curriculumService.getCurriculumElements(elementRefs);
		for(CurriculumElement curriculumElement:curriculumElements) {
			if(sb.length() > 0) sb.append("&nbsp;&nbsp;");
			sb.append("<i class='o_icon o_icon-fw o_icon_curriculum_element'>&nbsp;</i> ")
			  .append(StringHelper.escapeHtml(curriculumElement.getDisplayName()));
		}
		return sb.toString();
	}

	@Override
	protected void doDispose() {
		// nothing at the moment
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == chooseGroupCoachesLink) {
			doChooseGroupCoaches(ureq);
			setFormCanSubmit(false);
		} else if(source == chooseGroupParticipantsLink){
			doChooseGroupParticipants(ureq);
			setFormCanSubmit(false);
		} else if (source == chooseAreasCoachesLink) {
			doChooseAreasCoaches(ureq);
			setFormCanSubmit(false);
		} else if (source == chooseAreasParticipantsLink){
			doChooseAreasParticipants(ureq);
			setFormCanSubmit(false);
		} else if(chooseCurriculumElementsCoachesLink == source) {
			doChooseCurriculumElementParticipants(ureq);
			setFormCanSubmit(false);
		} else if(chooseCurriculumElementsParticipantsLink == source) {
			doChooseCurriculumElementsParticipants(ureq);
			setFormCanSubmit(false);
		}
	}
	
	protected abstract void setFormCanSubmit(boolean enable);

	protected void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(areaChooseParticipants);
		removeAsListenerAndDispose(areaChooseCoaches);
		removeAsListenerAndDispose(groupChooseCoaches);			
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		setFormCanSubmit(true);

		if (source == groupChooseCoaches) {
			if (event == Event.DONE_EVENT) {
				easyGroupCoachSelectionList.setValue(getGroupNames(groupChooseCoaches.getSelectedKeys()));
				easyGroupCoachSelectionList.setUserObject(groupChooseCoaches.getSelectedKeys());
				chooseGroupCoachesLink.setI18nKey("groupCoachesChoose");
				flc.setDirty(true);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == areaChooseCoaches) {
			if (event == Event.DONE_EVENT) {
				easyAreaCoachSelectionList.setValue(getAreaNames(areaChooseCoaches.getSelectedKeys()));
				easyAreaCoachSelectionList.setUserObject(areaChooseCoaches.getSelectedKeys());
				chooseAreasCoachesLink.setI18nKey("areaCoachesChoose");
				flc.setDirty(true);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == groupChooseParticipants) {
			if (event == Event.DONE_EVENT) {
				easyGroupParticipantsSelectionList.setValue(getGroupNames(groupChooseParticipants.getSelectedKeys()));
				easyGroupParticipantsSelectionList.setUserObject(groupChooseParticipants.getSelectedKeys());
				chooseGroupParticipantsLink.setI18nKey("groupParticipantsChoose");
				flc.setDirty(true);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == areaChooseParticipants) {
			if (event == Event.DONE_EVENT) {
				easyAreaParticipantsSelectionList.setValue(getAreaNames(areaChooseParticipants.getSelectedKeys()));
				easyAreaParticipantsSelectionList.setUserObject(areaChooseParticipants.getSelectedKeys());
				chooseAreasParticipantsLink.setI18nKey("areaParticipantsChoose");
				flc.setDirty(true);
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == curriculumElementsChooseCoaches) {
			if (event == Event.DONE_EVENT) {
				easyCurriculumElementCoachesSelectionList.setValue(getCurriculumElementNames(curriculumElementsChooseCoaches.getSelectedKeys()));
				easyCurriculumElementCoachesSelectionList.setUserObject(curriculumElementsChooseCoaches.getSelectedKeys());
				chooseCurriculumElementsCoachesLink.setI18nKey("curriculumElementsCoachesChoose");
				flc.setDirty(true);
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == curriculumElementsChooseParticipants) {
			if (event == Event.DONE_EVENT) {
				easyCurriculumElementParticipantsSelectionList.setValue(getCurriculumElementNames(curriculumElementsChooseParticipants.getSelectedKeys()));
				easyCurriculumElementParticipantsSelectionList.setUserObject(curriculumElementsChooseParticipants.getSelectedKeys());
				chooseCurriculumElementsParticipantsLink.setI18nKey("curriculumElementsParticipantsChoose");
				flc.setDirty(true);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void doChooseGroupCoaches(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(groupChooseCoaches);

		groupChooseCoaches = new GroupSelectionController(ureq, getWindowControl(), true,
				cev.getCourseGroupManager(), getKeys(easyGroupCoachSelectionList));
		listenTo(groupChooseCoaches);
		
		String title = chooseGroupCoachesLink.getLinkTitleText();
		cmc = new CloseableModalController(getWindowControl(), "close", groupChooseCoaches.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doChooseGroupParticipants(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(groupChooseParticipants);
		
		groupChooseParticipants = new GroupSelectionController(ureq, getWindowControl(), true,
				cev.getCourseGroupManager(), getKeys(easyGroupParticipantsSelectionList));
		listenTo(groupChooseParticipants);
		
		String title = chooseGroupParticipantsLink.getLabelText();
		cmc = new CloseableModalController(getWindowControl(), "close", groupChooseParticipants.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doChooseAreasCoaches(UserRequest ureq) {
		// already areas -> choose areas
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(areaChooseCoaches);
		
		areaChooseCoaches = new AreaSelectionController (ureq, getWindowControl(), true,
				cev.getCourseGroupManager(), getKeys(easyAreaCoachSelectionList));
		listenTo(areaChooseCoaches);

		String title = chooseAreasCoachesLink.getLinkTitleText();
		cmc = new CloseableModalController(getWindowControl(), "close", areaChooseCoaches.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doChooseAreasParticipants(UserRequest ureq) {
		// already areas -> choose areas
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(areaChooseParticipants);
		
		areaChooseParticipants = new AreaSelectionController (ureq, getWindowControl(), true,
				cev.getCourseGroupManager(), getKeys(easyAreaParticipantsSelectionList));
		listenTo(areaChooseParticipants);

		String title = chooseAreasParticipantsLink.getLabelText();
		cmc = new CloseableModalController(getWindowControl(), "close", areaChooseParticipants.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doChooseCurriculumElementParticipants(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(curriculumElementsChooseCoaches);
		
		curriculumElementsChooseCoaches = new CurriculumElementSelectionController(ureq, getWindowControl(),
				cev.getCourseGroupManager(), getKeys(easyCurriculumElementCoachesSelectionList));
		listenTo(curriculumElementsChooseCoaches);
		
		String title = chooseCurriculumElementsCoachesLink.getLabelText();
		cmc = new CloseableModalController(getWindowControl(), "close", curriculumElementsChooseCoaches.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doChooseCurriculumElementsParticipants(UserRequest ureq) {
		// already areas -> choose areas
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(curriculumElementsChooseParticipants);
		
		curriculumElementsChooseParticipants = new CurriculumElementSelectionController(ureq, getWindowControl(),
				cev.getCourseGroupManager(), getKeys(easyCurriculumElementParticipantsSelectionList));
		listenTo(curriculumElementsChooseParticipants);

		String title = chooseCurriculumElementsParticipantsLink.getLabelText();
		cmc = new CloseableModalController(getWindowControl(), "close", curriculumElementsChooseParticipants.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private List<Long> getKeys(StaticTextElement element) {
		@SuppressWarnings("unchecked")
		List<Long> keys = (List<Long>)element.getUserObject();
		if(keys == null) {
			keys = new ArrayList<>();
			element.setUserObject(keys);
		}
		return keys;
	}

	// ----------------
	
	public boolean sendToCoaches() {
		return wantCoaches.isSelected(0);
	}
	
	protected String getGroupCoaches() {
		if (!isEmpty(easyGroupCoachSelectionList) && wantCoaches.isSelected(0) && coachesChoice.isOneSelected() && MEMEBER_KEY_GROUP.equals(coachesChoice.getSelectedKey())) {
			return easyGroupCoachSelectionList.getValue();
		}
		return null;
	}
	
	protected List<Long> getGroupCoachesIds() {
		if (!isEmpty(easyGroupCoachSelectionList) && wantCoaches.isSelected(0) && coachesChoice.isOneSelected()  && MEMEBER_KEY_GROUP.equals(coachesChoice.getSelectedKey())) {
			return getKeys(easyGroupCoachSelectionList);
		}
		return null;
	}
	
	protected String getGroupParticipants() {
		if (!isEmpty(easyGroupParticipantsSelectionList) && wantParticipants.isSelected(0) && participantsChoice.isOneSelected() && MEMEBER_KEY_GROUP.equals(participantsChoice.getSelectedKey())) {
			return easyGroupParticipantsSelectionList.getValue();
		}
		return null;
	}
	
	protected List<Long> getGroupParticipantsIds() {
		if (!isEmpty(easyGroupParticipantsSelectionList) && wantParticipants.isSelected(0) && participantsChoice.isOneSelected() && MEMEBER_KEY_GROUP.equals(participantsChoice.getSelectedKey())) {
			return getKeys(easyGroupParticipantsSelectionList);
		}
		return null;
	}

	/**
	 * returns the chosen learning areas, or null if no ares were chosen.
	 */
	protected String getCoachesAreas() {
		if(!isEmpty(easyAreaCoachSelectionList) && wantCoaches.isSelected(0) && coachesChoice.isOneSelected() && MEMEBER_KEY_GROUP.equals(coachesChoice.getSelectedKey())) {
			return easyAreaCoachSelectionList.getValue();
		}
		return null;
	}
	
	protected List<Long> getCoachesAreaIds() {
		if(!isEmpty(easyAreaCoachSelectionList) && wantCoaches.isSelected(0) && coachesChoice.isOneSelected() && MEMEBER_KEY_GROUP.equals(coachesChoice.getSelectedKey())) {
			return getKeys(easyAreaCoachSelectionList);
		}
		return null;
	}

	protected String getParticipantsAreas() {
		if(!isEmpty(easyAreaParticipantsSelectionList) && wantParticipants.isSelected(0) && participantsChoice.isOneSelected() && MEMEBER_KEY_GROUP.equals(participantsChoice.getSelectedKey())) {
			return easyAreaParticipantsSelectionList.getValue();
		}
		return null;
	}
	
	protected String getCoachesCurriculumElements() {
		if (!isEmpty(easyCurriculumElementCoachesSelectionList) && wantCoaches.isSelected(0) && participantsChoice.isOneSelected() && MEMEBER_KEY_CURRICULUM_ELEMENT.equals(coachesChoice.getSelectedKey())) {
			return easyCurriculumElementCoachesSelectionList.getValue();
		}
		return null;
	}
	
	protected List<Long> getCoachesCurriculumElementIds() {
		if (!isEmpty(easyCurriculumElementCoachesSelectionList) && wantCoaches.isSelected(0) && coachesChoice.isOneSelected() && MEMEBER_KEY_CURRICULUM_ELEMENT.equals(coachesChoice.getSelectedKey())) {
			return getKeys(easyCurriculumElementCoachesSelectionList);
		}
		return null;
	}
	
	protected String getParticipantsCurriculumElements() {
		if (!isEmpty(easyCurriculumElementParticipantsSelectionList) && wantParticipants.isSelected(0) && participantsChoice.isOneSelected() && MEMEBER_KEY_CURRICULUM_ELEMENT.equals(participantsChoice.getSelectedKey())) {
			return easyCurriculumElementParticipantsSelectionList.getValue();
		}
		return null;
	}
	
	protected List<Long> getCurriculumElementParticipantsIds() {
		if (!isEmpty(easyCurriculumElementParticipantsSelectionList) && wantParticipants.isSelected(0) && participantsChoice.isOneSelected() && MEMEBER_KEY_CURRICULUM_ELEMENT.equals(participantsChoice.getSelectedKey())) {
			return getKeys(easyCurriculumElementParticipantsSelectionList);
		}
		return null;
	}
	
	public boolean sendToPartips() {
		return wantParticipants.isSelected(0);
	}
	
	public boolean sendToCoachesCourse(){
		return wantCoaches.isSelected(0) && coachesChoice.isOneSelected() && coachesChoice.isOneSelected() && MEMEBER_KEY_COURSE.equals(coachesChoice.getSelectedKey());
	}
	
	public boolean sendToCoachesAssigned(){
		return wantCoaches.isSelected(0) && coachesChoice.isOneSelected() && coachesChoice.isOneSelected() && MEMEBER_KEY_ASSIGNED.equals(coachesChoice.getSelectedKey());
	}
	
	protected boolean sendToCoachesAll(){
		return wantCoaches.isSelected(0) && coachesChoice.isOneSelected() && coachesChoice.isOneSelected() && MEMEBER_KEY_ALL.equals(coachesChoice.getSelectedKey());
	}
	
	protected boolean sendToCoachesGroup(){
		return wantCoaches.isSelected(0) && coachesChoice.isOneSelected() && coachesChoice.isOneSelected() && MEMEBER_KEY_GROUP.equals(coachesChoice.getSelectedKey());
	}
	
	protected boolean sendToParticipantsCourse(){
		return wantParticipants.isSelected(0) && participantsChoice.isOneSelected() && participantsChoice.isOneSelected() && MEMEBER_KEY_COURSE.equals(participantsChoice.getSelectedKey());
	}
	
	protected boolean sendToParticipantsAll(){
		return wantParticipants.isSelected(0) && participantsChoice.isOneSelected() && participantsChoice.isOneSelected() && MEMEBER_KEY_ALL.equals(participantsChoice.getSelectedKey());
	}
	
	protected boolean sendToParticipantsGroup(){
		return wantParticipants.isSelected(0) && participantsChoice.isOneSelected() && participantsChoice.isOneSelected() && MEMEBER_KEY_GROUP.equals(participantsChoice.getSelectedKey());
	}
	
	protected List<Long> getParticipantsAreaIds() {
		if(!isEmpty(easyAreaParticipantsSelectionList) && wantParticipants.isSelected(0) && participantsChoice.isOneSelected() && MEMEBER_KEY_GROUP.equals(participantsChoice.getSelectedKey())) {
			return getKeys(easyAreaParticipantsSelectionList);
		}
		return null;
	}
	
	private boolean isEmpty(StaticTextElement element) {
		return getKeys(element).isEmpty();
	}
	
	protected void storeConfiguration(ModuleConfiguration configToStore) {
		configToStore.set(getConfigKeyCoachesGroup(), getGroupCoaches());
		configToStore.set(getConfigKeyCoachesGroupIds(), getGroupCoachesIds());
		configToStore.set(getConfigKeyCoachesArea(), getCoachesAreas());
		configToStore.set(getConfigKeyCoachesAreaIds(), getCoachesAreaIds());
		configToStore.set(getConfigKeyCoachesCurriculumElement(), getCoachesCurriculumElements());
		configToStore.set(getConfigKeyCoachesCurriculumElementIds(), getCoachesCurriculumElementIds());
		configToStore.setBooleanEntry(getConfigKeyCoachesAll(), sendToCoachesAll());
		configToStore.setBooleanEntry(getConfigKeyCoachesCourse(), sendToCoachesCourse());
		if (getConfigKeyCoachesAssigned() != null) {
			configToStore.setBooleanEntry(getConfigKeyCoachesAssigned(), sendToCoachesAssigned());
		}
		
		configToStore.set(getConfigKeyParticipantsGroup(), getGroupParticipants());
		configToStore.set(getConfigKeyParticipantsGroupIds(), getGroupParticipantsIds());
		configToStore.set(getConfigKeyParticipantsArea(), getParticipantsAreas());
		configToStore.set(getConfigKeyParticipantsAreaIds(), getParticipantsAreaIds());
		configToStore.set(getConfigKeyParticipantsCurriculumElement(), getParticipantsCurriculumElements());
		configToStore.set(getConfigKeyParticipantsCurriculumElementIds(), getCurriculumElementParticipantsIds());
		configToStore.setBooleanEntry(getConfigKeyParticipantsAll(), sendToParticipantsAll());
		configToStore.setBooleanEntry(getConfigKeyParticipantsCourse(), sendToParticipantsCourse());
	}
	
	protected abstract String getConfigKeyCoachesGroup();
	protected abstract String getConfigKeyCoachesGroupIds();
	
	protected abstract String getConfigKeyCoachesArea();
	protected abstract String getConfigKeyCoachesAreaIds();

	protected abstract String getConfigKeyCoachesCurriculumElement();
	protected abstract String getConfigKeyCoachesCurriculumElementIds();
	
	protected abstract String getConfigKeyCoachesCourse();
	protected abstract String getConfigKeyCoachesAssigned();
	protected abstract String getConfigKeyCoachesAll();

	protected abstract String getConfigKeyParticipantsGroup();
	protected abstract String getConfigKeyParticipantsArea();
	
	protected abstract String getConfigKeyParticipantsGroupIds();
	protected abstract String getConfigKeyParticipantsAreaIds();
	
	protected abstract String getConfigKeyParticipantsCurriculumElement();
	protected abstract String getConfigKeyParticipantsCurriculumElementIds();
	
	protected abstract String getConfigKeyParticipantsCourse();
	protected abstract String getConfigKeyParticipantsAll();
}
