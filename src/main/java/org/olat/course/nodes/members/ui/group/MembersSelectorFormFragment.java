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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.condition.AreaSelectionController;
import org.olat.course.condition.GroupSelectionController;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.modules.ModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Reusable Form Fragment for dealing with course membership selection 
 * 
 * <p>Initial date: May 6, 2016<br>
 * @author lmihalkovic, http://www.frentix.com
 */
public abstract class MembersSelectorFormFragment extends FormBasicController {

	// Coaches
	private SelectionElement wantCoaches;
	private SingleSelection coachesChoice;

	private FormLink chooseGroupCoachesLink;
	private GroupSelectionController groupChooseCoaches;
	private StaticTextElement easyGroupCoachSelectionList;
	
	private FormLink chooseAreasCoachesLink;
	private AreaSelectionController areaChooseCoaches;
	private StaticTextElement easyAreaCoachSelectionList;
	
	// Participants
	private SelectionElement wantParticipants;
	private SingleSelection participantsChoice;
	
	private FormLink chooseGroupParticipantsLink;	
	private GroupSelectionController groupChooseParticipants;
	private StaticTextElement easyGroupParticipantsSelectionList;

	private FormLink chooseAreasParticipantsLink;
	private AreaSelectionController areaChooseParticipants;
	private StaticTextElement easyAreaParticipantsSelectionList;

	// Popup form
	private CloseableModalController cmc;
	
	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private BusinessGroupService businessGroupService;

	private final CourseEditorEnv cev;
	protected final ModuleConfiguration config;
	
	public MembersSelectorFormFragment(UserRequest ureq, WindowControl wControl,
			CourseEditorEnv cev, ModuleConfiguration config) {
		super(ureq, wControl, Util.createPackageTranslator(MembersSelectorFormFragment.class, ureq.getLocale()));
		this.cev = cev;
		this.config = config;
		initForm(ureq);
		validateFormLogic(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		Boolean coacheSelection = config.getBooleanSafe(getConfigKeyCoachesAll())
				|| config.getBooleanSafe(getConfigKeyCoachesCourse())
				|| config.get(getConfigKeyCoachesGroup()) != null
				|| config.get(getConfigKeyCoachesArea()) != null;

		// COACHES: from course or groups
		wantCoaches = uifactory.addCheckboxesHorizontal("coaches", "message.want.coaches", formLayout, new String[]{"xx"},new String[]{null});
		if(coacheSelection != null && coacheSelection) {
			wantCoaches.select("xx", true);
		}
		wantCoaches.addActionListener(FormEvent.ONCLICK);
		
		
		coachesChoice = uifactory.addRadiosVertical("coachesChoice", null, formLayout, 
				new String[]{"all", "course", "group"},
				new String[]{ translate("form.message.coaches.all"), translate("form.message.coaches.course"), translate("form.message.coaches.group")}
		);
		if(config.getBooleanSafe(getConfigKeyCoachesAll())) {
			coachesChoice.select("all", true);
		}
		if(config.getBooleanSafe(getConfigKeyCoachesCourse())) {
			coachesChoice.select("course", true);
		}
		if(config.get(getConfigKeyCoachesGroup()) != null || config.get(getConfigKeyCoachesArea()) != null) {
			coachesChoice.select("group", true);
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
				
		
		// PARTICIPANTS: from course or groups
		Boolean particiapntSelection = config.getBooleanSafe(getConfigKeyParticipantsAll())
				|| config.getBooleanSafe(getConfigKeyParticipantsCourse())
				|| config.get(getConfigKeyParticipantsGroup()) != null
				|| config.get(getConfigKeyParticipantsArea()) != null;
		
		wantParticipants = uifactory.addCheckboxesHorizontal("participants", "message.want.participants", formLayout, new String[]{"xx"},new String[]{null});
		if(particiapntSelection != null && particiapntSelection) wantParticipants.select("xx", true);
		wantParticipants.addActionListener(FormEvent.ONCLICK);
		
		participantsChoice = uifactory.addRadiosVertical(
				"participantsChoice", null, formLayout, 
				new String[]{ "all", "course", "group" },
				new String[]{ translate("form.message.participants.all"), translate("form.message.participants.course"), translate("form.message.participants.group")}
		);
		if(config.getBooleanSafe(getConfigKeyParticipantsAll())) {
			participantsChoice.select("all", true);
		}
		if(config.getBooleanSafe(getConfigKeyParticipantsCourse())) {
			participantsChoice.select("course", true);
		}
		if(config.get(getConfigKeyParticipantsGroup()) != null || config.get(getConfigKeyParticipantsArea()) != null) {
			participantsChoice.select("group", true);
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
		if(groupParticipantsKeys == null) {
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
		if(areaParticipantsKeys == null) {
			areaParticipantsInitVal = (String)config.get(getConfigKeyParticipantsArea());
			areaParticipantsKeys = areaManager.toAreaKeys(areaParticipantsInitVal, cev.getCourseGroupManager().getCourseResource());
		}
		areaParticipantsInitVal = getAreaNames(areaParticipantsKeys);

		easyAreaParticipantsSelectionList = uifactory.addStaticTextElement("areaParticipants", null, areaParticipantsInitVal, formLayout);
		easyAreaParticipantsSelectionList.setUserObject(areaParticipantsKeys);
		easyAreaParticipantsSelectionList.setVisible(false);
		easyAreaParticipantsSelectionList.setElementCssClass("text-muted");
	
		uifactory.addSpacerElement("s4", formLayout, false);		
	}
	
	protected void update() {
		coachesChoice.setVisible(wantCoaches.isSelected(0));
		chooseGroupCoachesLink.setVisible(coachesChoice.isSelected(2) && wantCoaches.isSelected(0));
		chooseAreasCoachesLink.setVisible(coachesChoice.isSelected(2) && wantCoaches.isSelected(0));
		easyGroupCoachSelectionList.setVisible(coachesChoice.isSelected(2) && wantCoaches.isSelected(0));
		easyAreaCoachSelectionList.setVisible(coachesChoice.isSelected(2) && wantCoaches.isSelected(0));
		
		participantsChoice.setVisible(wantParticipants.isSelected(0));
		chooseGroupParticipantsLink.setVisible(participantsChoice.isSelected(2) && wantParticipants.isSelected(0));
		chooseAreasParticipantsLink.setVisible(participantsChoice.isSelected(2) && wantParticipants.isSelected(0));
		easyGroupParticipantsSelectionList.setVisible(participantsChoice.isSelected(2) && wantParticipants.isSelected(0));
		easyAreaParticipantsSelectionList.setVisible(participantsChoice.isSelected(2) && wantParticipants.isSelected(0));
		
		easyGroupParticipantsSelectionList.clearError();
		easyAreaParticipantsSelectionList.clearError();
		easyGroupCoachSelectionList.clearError();
		easyAreaCoachSelectionList.clearError();
		
		coachesChoice.clearError();
		participantsChoice.clearError();
		
		setNeedsLayout();
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean isOK = true;

		if(sendToCoaches()){
			if(!coachesChoice.isOneSelected()){
				coachesChoice.setErrorKey("error.no.choice.specified", null);
				isOK = false;
			}else{
				coachesChoice.clearError();
			}
			if(coachesChoice.isSelected(2) &&(isEmpty(easyAreaCoachSelectionList)|| easyAreaCoachSelectionList == null)){
				if(easyGroupCoachSelectionList.getValue() == null && isEmpty(easyGroupCoachSelectionList) || easyGroupCoachSelectionList.getValue().equals("")){
					easyAreaCoachSelectionList.setErrorKey("error.no.group.specified", null);
					easyGroupCoachSelectionList.setErrorKey("error.no.group.specified", null);
					isOK = false;
				}
			}
		}

		if(sendToPartips()){
			if(!participantsChoice.isOneSelected()){
				participantsChoice.setErrorKey("error.no.choice.specified", null);
				isOK = false;
			}else{
				participantsChoice.clearError();
			}
			if(participantsChoice.isSelected(2) &&(isEmpty(easyAreaParticipantsSelectionList)|| easyAreaParticipantsSelectionList == null)){
				if(easyGroupParticipantsSelectionList.getValue() == null && isEmpty(easyGroupParticipantsSelectionList)|| easyGroupParticipantsSelectionList.getValue().equals("")){
					easyAreaParticipantsSelectionList.setErrorKey("error.no.group.specified", null);
					easyGroupParticipantsSelectionList.setErrorKey("error.no.group.specified", null);
					isOK = false;
				}
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
		StringBuilder sb = new StringBuilder();
		List<BusinessGroupShort> groups = businessGroupService.loadShortBusinessGroups(keys);
		for(BusinessGroupShort group:groups) {
			if(sb.length() > 0) sb.append("&nbsp;&nbsp;");
			sb.append("<i class='o_icon o_icon-fw o_icon_group'>&nbsp;</i> ");
			sb.append(StringHelper.escapeHtml(group.getName()));
		}
		return sb.toString();
	}
	
	private String getAreaNames(List<Long> keys) {
		StringBuilder sb = new StringBuilder();
		List<BGArea> areas = areaManager.loadAreas(keys);
		for(BGArea area:areas) {
			if(sb.length() > 0) sb.append("&nbsp;&nbsp;");
			sb.append("<i class='o_icon o_icon-fw o_icon_courseareas'>&nbsp;</i> ");
			sb.append(StringHelper.escapeHtml(area.getName()));
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
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(groupChooseCoaches);

			groupChooseCoaches = new GroupSelectionController(ureq, getWindowControl(), true,
					cev.getCourseGroupManager(), getKeys(easyGroupCoachSelectionList));
			listenTo(groupChooseCoaches);
			
			String title = chooseGroupCoachesLink.getLinkTitleText();
			cmc = new CloseableModalController(getWindowControl(), "close", groupChooseCoaches.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
			setFormCanSubmit(false);
		} else if(source == chooseGroupParticipantsLink){
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(groupChooseParticipants);
			
			groupChooseParticipants = new GroupSelectionController(ureq, getWindowControl(), true,
					cev.getCourseGroupManager(), getKeys(easyGroupParticipantsSelectionList));
			listenTo(groupChooseParticipants);
			
			String title = chooseGroupParticipantsLink.getLabelText();
			cmc = new CloseableModalController(getWindowControl(), "close", groupChooseParticipants.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
			setFormCanSubmit(false);
		} else if (source == chooseAreasCoachesLink) {
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
			setFormCanSubmit(false);
		} else if (source == chooseAreasParticipantsLink){
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
				cmc.deactivate();
				easyGroupCoachSelectionList.setValue(getGroupNames(groupChooseCoaches.getSelectedKeys()));
				easyGroupCoachSelectionList.setUserObject(groupChooseCoaches.getSelectedKeys());
				chooseGroupCoachesLink.setI18nKey("groupCoachesChoose");
				setNeedsLayout();
			} else if (Event.CANCELLED_EVENT == event) {
				cmc.deactivate();
			}
		} else if (source == areaChooseCoaches) {
			if (event == Event.DONE_EVENT) {
				cmc.deactivate();
				easyAreaCoachSelectionList.setValue(getAreaNames(areaChooseCoaches.getSelectedKeys()));
				easyAreaCoachSelectionList.setUserObject(areaChooseCoaches.getSelectedKeys());
				chooseAreasCoachesLink.setI18nKey("areaCoachesChoose");
				setNeedsLayout();
			} else if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			}
		} else if (source == groupChooseParticipants) {
			if (event == Event.DONE_EVENT) {
				cmc.deactivate();
				easyGroupParticipantsSelectionList.setValue(getGroupNames(groupChooseParticipants.getSelectedKeys()));
				easyGroupParticipantsSelectionList.setUserObject(groupChooseParticipants.getSelectedKeys());
				chooseGroupParticipantsLink.setI18nKey("groupParticipantsChoose");
				setNeedsLayout();
			} else if (Event.CANCELLED_EVENT == event) {
				cmc.deactivate();
			}
		} else if (source == areaChooseParticipants) {
			if (event == Event.DONE_EVENT) {
				cmc.deactivate();
				easyAreaParticipantsSelectionList.setValue(getAreaNames(areaChooseParticipants.getSelectedKeys()));
				easyAreaParticipantsSelectionList.setUserObject(areaChooseParticipants.getSelectedKeys());
				chooseAreasParticipantsLink.setI18nKey("areaParticipantsChoose");
				setNeedsLayout();
			} else if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			}
		}
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

	// ----------------
	
	public boolean sendToCoaches() {
		return wantCoaches.isSelected(0);
	}
	
	protected String getGroupCoaches() {
		if (!isEmpty(easyGroupCoachSelectionList) && wantCoaches.isSelected(0) && coachesChoice.isSelected(2)) {
			return easyGroupCoachSelectionList.getValue();
		}
		return null;
	}
	
	protected List<Long> getGroupCoachesIds() {
		if (!isEmpty(easyGroupCoachSelectionList) && wantCoaches.isSelected(0) && coachesChoice.isSelected(2)) {
			return getKeys(easyGroupCoachSelectionList);
		}
		return null;
	}
	
	protected String getGroupParticipants() {
		if (!isEmpty(easyGroupParticipantsSelectionList) && wantParticipants.isSelected(0)&& participantsChoice.isSelected(2)) {
			return easyGroupParticipantsSelectionList.getValue();
		}
		return null;
	}
	
	protected List<Long> getGroupParticipantsIds() {
		if (!isEmpty(easyGroupParticipantsSelectionList) && wantParticipants.isSelected(0)&& participantsChoice.isSelected(2)) {
			return getKeys(easyGroupParticipantsSelectionList);
		}
		return null;
	}

	/**
	 * returns the chosen learning areas, or null if no ares were chosen.
	 */
	protected String getCoachesAreas() {
		if(!isEmpty(easyAreaCoachSelectionList)&&wantCoaches.isSelected(0)&& coachesChoice.isSelected(2)) {
			return easyAreaCoachSelectionList.getValue();
		}
		return null;
	}
	
	protected List<Long> getCoachesAreaIds() {
		if(!isEmpty(easyAreaCoachSelectionList)&&wantCoaches.isSelected(0)&& coachesChoice.isSelected(2)) {
			return getKeys(easyAreaCoachSelectionList);
		}
		return null;
	}

	protected String getParticipantsAreas() {
		if(!isEmpty(easyAreaParticipantsSelectionList)&& wantParticipants.isSelected(0)&& participantsChoice.isSelected(2)) {
			return easyAreaParticipantsSelectionList.getValue();
		}
		return null;
	}
	
	public boolean sendToPartips() {
		return wantParticipants.isSelected(0);
	}
	
	public boolean sendToCoachesCourse(){
		return coachesChoice.isSelected(1)&& wantCoaches.isSelected(0);
	}
	
	protected boolean sendToCoachesAll(){
		return coachesChoice.isSelected(0) && wantCoaches.isSelected(0);
	}
	
	protected boolean sendToCoachesGroup(){
		return coachesChoice.isSelected(2) && wantCoaches.isSelected(0);
	}
	
	protected boolean sendToParticipantsCourse(){
		return participantsChoice.isSelected(1) && wantParticipants.isSelected(0);
	}
	
	protected boolean sendToParticipantsAll(){
		return participantsChoice.isSelected(0) && wantParticipants.isSelected(0);
	}
	
	protected boolean sendToParticipantsGroup(){
		return participantsChoice.isSelected(2) && wantParticipants.isSelected(0);
	}
	
	protected List<Long> getParticipantsAreaIds() {
		if(!isEmpty(easyAreaParticipantsSelectionList)&& wantParticipants.isSelected(0)&& participantsChoice.isSelected(2)) {
			return getKeys(easyAreaParticipantsSelectionList);
		}
		return null;
	}
	
	private boolean isEmpty(StaticTextElement element) {
		List<Long> keys = getKeys(element);
		if(keys == null || keys.isEmpty()) {
			return true;
		}
		return false;
	}
	
	protected void storeConfiguration(ModuleConfiguration config) {
		config.set(getConfigKeyCoachesGroup(), getGroupCoaches());
		config.set(getConfigKeyCoachesGroupIds(), getGroupCoachesIds());
		config.set(getConfigKeyCoachesArea(), getCoachesAreas());
		config.set(getConfigKeyCoachesAreaIds(), getCoachesAreaIds());
		config.setBooleanEntry(getConfigKeyCoachesAll(), sendToCoachesAll());
		config.setBooleanEntry(getConfigKeyCoachesCourse(), sendToCoachesCourse());
		
		config.set(getConfigKeyParticipantsGroup(), getGroupParticipants());
		config.set(getConfigKeyParticipantsGroupIds(), getGroupParticipantsIds());
		config.set(getConfigKeyParticipantsArea(), getParticipantsAreas());
		config.set(getConfigKeyParticipantsAreaIds(), getParticipantsAreaIds());
		config.setBooleanEntry(getConfigKeyParticipantsAll(), sendToParticipantsAll());
		config.setBooleanEntry(getConfigKeyParticipantsCourse(), sendToParticipantsCourse());
	}
	
	protected abstract String getConfigKeyCoachesGroup();
	protected abstract String getConfigKeyCoachesGroupIds();
	
	protected abstract String getConfigKeyCoachesArea();
	protected abstract String getConfigKeyCoachesAreaIds();
	
	protected abstract String getConfigKeyCoachesCourse();
	protected abstract String getConfigKeyCoachesAll();

	protected abstract String getConfigKeyParticipantsGroup();
	protected abstract String getConfigKeyParticipantsArea();
	
	protected abstract String getConfigKeyParticipantsGroupIds();
	protected abstract String getConfigKeyParticipantsAreaIds();
	
	protected abstract String getConfigKeyParticipantsCourse();
	protected abstract String getConfigKeyParticipantsAll();
}
