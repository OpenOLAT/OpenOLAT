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
package org.olat.course.editor.formfragments;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.BasicFormFragment;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.course.condition.AreaSelectionController;
import org.olat.course.condition.GroupSelectionController;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.modules.IModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Reusable Form Fragment for dealing with course membership selection 
 * 
 * <p>Initial date: May 6, 2016<br>
 * @author lmihalkovic, http://www.frentix.com
 * @see IFormFragment
 */
public class MembersSelectorFormFragment extends BasicFormFragment {
		
	public static final String CONFIG_KEY_COACHES_GROUP 		= "GroupCoaches";
	public static final String CONFIG_KEY_COACHES_AREA 			= "AreaCoaches";
	public static final String CONFIG_KEY_COACHES_GROUP_ID 		= "GroupCoachesIds";
	public static final String CONFIG_KEY_COACHES_AREA_IDS 		= "AreaCoachesIds";
	public static final String CONFIG_KEY_COACHES_COURSE 		= "CourseCoaches";
	public static final String CONFIG_KEY_COACHES_ALL 			= "CoachesAll";
	
	public static final String CONFIG_KEY_PARTICIPANTS_GROUP 	= "GroupParticipants";
	public static final String CONFIG_KEY_PARTICIPANTS_AREA 	= "AreaParticipants";
	public static final String CONFIG_KEY_PARTICIPANTS_GROUP_ID = "GroupParticipantsIds";
	public static final String CONFIG_KEY_PARTICIPANTS_AREA_ID 	= "AreaParticipantsIds";
	public static final String CONFIG_KEY_PARTICIPANTS_COURSE 	= "CourseParticipants";
	public static final String CONFIG_KEY_PARTICIPANTS_ALL 		= "ParticipantsAll";
	
	
	private CourseEditorEnv cev;

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
	
	public MembersSelectorFormFragment(UserRequest ureq, WindowControl wControl, UserCourseEnvironment uce) {
		super(wControl);
		this.cev = uce.getCourseEditorEnv();
	}
	
	@Override
	protected void initFormFragment(FormItemContainer formLayout, Controller listener, UserRequest ureq, IModuleConfiguration config) {
		FormUIFactory uifactory = uifactory();
		
		// ----------------------------------------------------------------------
//		Boolean ownerSelection = config.getBooleanSafe(CONFIG_KEY_OWNERS);
		Boolean coacheSelection = config.getBooleanSafe(CONFIG_KEY_COACHES_ALL) || config.getBooleanSafe(CONFIG_KEY_COACHES_COURSE) || config.get(CONFIG_KEY_COACHES_GROUP) != null || config.get(CONFIG_KEY_COACHES_AREA) != null;

		
		// COACHES: from course or groups
		wantCoaches = uifactory.addCheckboxesHorizontal("coaches", "message.want.coaches", formLayout, new String[]{"xx"},new String[]{null});
		wantCoaches.setTranslator(host.getFragmentTranslator());
		if(coacheSelection != null && coacheSelection) wantCoaches.select("xx", true);

		wantCoaches.addActionListener(FormEvent.ONCLICK);
		
		
		coachesChoice = uifactory.addRadiosVertical(
				"coachesChoice", null, formLayout, 
				new String[]{"all", "course", "group"},
				new String[]{host.getFragmentTranslator().translate("form.message.coaches.all"), host.getFragmentTranslator().translate("form.message.coaches.course"), host.getFragmentTranslator().translate("form.message.coaches.group")}
		);
		if(config.getBooleanSafe(CONFIG_KEY_COACHES_ALL)) coachesChoice.select("all", true);
		if(config.getBooleanSafe(CONFIG_KEY_COACHES_COURSE)) coachesChoice.select("course", true);
		if(config.get(CONFIG_KEY_COACHES_GROUP) != null || config.get(CONFIG_KEY_COACHES_AREA) != null) coachesChoice.select("group", true);
		coachesChoice.addActionListener(FormEvent.ONCLICK);
		coachesChoice.setVisible(false);
		
		
		chooseGroupCoachesLink = uifactory.addFormLink("groupCoachesChoose", formLayout, "btn btn-default o_xsmall o_form_groupchooser");
		chooseGroupCoachesLink.setTranslator(host.getFragmentTranslator());		
		chooseGroupCoachesLink.setIconLeftCSS("o_icon o_icon-fw o_icon_group");
		chooseGroupCoachesLink.setVisible(false);
		chooseGroupCoachesLink.setLabel("form.message.group", null);
		if(!cev.getCourseGroupManager().hasBusinessGroups()){
			chooseGroupCoachesLink.setI18nKey("groupCreate");
		}
		
		chooseGroupCoachesLink.setElementCssClass("o_omit_margin");

		String groupCoachesInitVal;
		@SuppressWarnings("unchecked")
		List<Long> groupCoachesKeys = (List<Long>)config.get(CONFIG_KEY_COACHES_GROUP_ID);
		if(groupCoachesKeys == null) {
			groupCoachesInitVal = config.getAs(CONFIG_KEY_COACHES_GROUP);
			groupCoachesKeys = businessGroupService.toGroupKeys(groupCoachesInitVal, cev.getCourseGroupManager().getCourseEntry());
		}
		groupCoachesInitVal = getGroupNames(groupCoachesKeys);

		easyGroupCoachSelectionList = uifactory.addStaticTextElement("groupCoaches", null, groupCoachesInitVal, formLayout);
		easyGroupCoachSelectionList.setTranslator(host.getFragmentTranslator());		
		easyGroupCoachSelectionList.setUserObject(groupCoachesKeys);		
		easyGroupCoachSelectionList.setVisible(false);
		easyGroupCoachSelectionList.setElementCssClass("text-muted");
		
				
		chooseAreasCoachesLink = uifactory.addFormLink("areaCoachesChoose", formLayout, "btn btn-default o_xsmall o_form_areachooser");
		chooseAreasCoachesLink.setTranslator(host.getFragmentTranslator());
		chooseAreasCoachesLink.setIconLeftCSS("o_icon o_icon-fw o_icon_courseareas");
		chooseAreasCoachesLink.setLabel("form.message.area", null);
		chooseAreasCoachesLink.setElementCssClass("o_omit_margin");
		if(!cev.getCourseGroupManager().hasAreas()){
			chooseAreasCoachesLink.setI18nKey("areaCreate");
		}
		
		String areaCoachesInitVal;
		@SuppressWarnings("unchecked")
		List<Long> areaCoachesKeys = (List<Long>)config.get(CONFIG_KEY_COACHES_AREA_IDS);
		if(areaCoachesKeys == null) {
			areaCoachesInitVal = (String)config.get(CONFIG_KEY_COACHES_AREA);
			areaCoachesKeys = areaManager.toAreaKeys(areaCoachesInitVal, cev.getCourseGroupManager().getCourseResource());
		}
		areaCoachesInitVal = getAreaNames(areaCoachesKeys);

		easyAreaCoachSelectionList = uifactory.addStaticTextElement("areaCoaches", null, areaCoachesInitVal, formLayout);
		easyAreaCoachSelectionList.setTranslator(host.getFragmentTranslator());		
		easyAreaCoachSelectionList.setUserObject(areaCoachesKeys);
		easyAreaCoachSelectionList.setVisible(false);
		easyAreaCoachSelectionList.setElementCssClass("text-muted");
				
		
		// PARTICIPANTS: from course or groups
		Boolean particiapntSelection = config.getBooleanSafe(CONFIG_KEY_PARTICIPANTS_ALL) || config.getBooleanSafe(CONFIG_KEY_PARTICIPANTS_COURSE) || config.get(CONFIG_KEY_PARTICIPANTS_GROUP) != null || config.get(CONFIG_KEY_PARTICIPANTS_AREA) != null;
		
		wantParticipants = uifactory.addCheckboxesHorizontal("participants", "message.want.participants", formLayout, new String[]{"xx"},new String[]{null});
		wantParticipants.setTranslator(host.getFragmentTranslator());
		if(particiapntSelection != null && particiapntSelection) wantParticipants.select("xx", true);
		wantParticipants.addActionListener(FormEvent.ONCLICK);
		
		participantsChoice = uifactory.addRadiosVertical(
				"participantsChoice", null, formLayout, 
				new String[]{"all", "course", "group"},
				new String[]{host.getFragmentTranslator().translate("form.message.participants.all"), host.getFragmentTranslator().translate("form.message.participants.course"), host.getFragmentTranslator().translate("form.message.participants.group")}
		);
		if(config.getBooleanSafe(CONFIG_KEY_PARTICIPANTS_ALL)) participantsChoice.select("all", true);
		if(config.getBooleanSafe(CONFIG_KEY_PARTICIPANTS_COURSE)) participantsChoice.select("course", true);
		if(config.get(CONFIG_KEY_PARTICIPANTS_GROUP) != null || config.get(CONFIG_KEY_PARTICIPANTS_AREA) != null) participantsChoice.select("group", true);
		participantsChoice.addActionListener(FormEvent.ONCLICK);
		participantsChoice.setVisible(false); 
		
		chooseGroupParticipantsLink = uifactory.addFormLink("groupParticipantsChoose", formLayout, "btn btn-default o_xsmall o_form_groupchooser");
		chooseGroupParticipantsLink.setTranslator(host.getFragmentTranslator());		
		chooseGroupParticipantsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_group");
		chooseGroupParticipantsLink.setVisible(false);
		chooseGroupParticipantsLink.setLabel("form.message.group", null);
		chooseGroupParticipantsLink.setElementCssClass("o_omit_margin");

		if(cev.getCourseGroupManager().getAllBusinessGroups().isEmpty()){
			chooseGroupParticipantsLink.setI18nKey("groupCreate");
		}
		
		String groupParticipantsInitVal;
		@SuppressWarnings("unchecked")
		List<Long> groupParticipantsKeys = (List<Long>)config.get(CONFIG_KEY_PARTICIPANTS_GROUP_ID);
		if(groupParticipantsKeys == null) {
			groupParticipantsInitVal = (String)config.get(CONFIG_KEY_PARTICIPANTS_GROUP);
			groupParticipantsKeys = businessGroupService.toGroupKeys(groupParticipantsInitVal, cev.getCourseGroupManager().getCourseEntry());
		}
		groupParticipantsInitVal = getGroupNames(groupParticipantsKeys);

		easyGroupParticipantsSelectionList = uifactory.addStaticTextElement("groupParticipants", null, groupParticipantsInitVal, formLayout);
		easyGroupParticipantsSelectionList.setTranslator(host.getFragmentTranslator());		
		easyGroupParticipantsSelectionList.setUserObject(groupParticipantsKeys);
		easyGroupParticipantsSelectionList.setVisible(false);
		easyGroupParticipantsSelectionList.setElementCssClass("text-muted");
		
		
		chooseAreasParticipantsLink = uifactory.addFormLink("areaParticipantsChoose", formLayout, "btn btn-default o_xsmall o_form_areachooser");
		chooseAreasParticipantsLink.setTranslator(host.getFragmentTranslator());
		chooseAreasParticipantsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_courseareas");
		chooseAreasParticipantsLink.setVisible(false);
		chooseAreasParticipantsLink.setLabel("form.message.area", null);
		chooseAreasParticipantsLink.setElementCssClass("o_omit_margin");

		if(cev.getCourseGroupManager().getAllAreas().isEmpty()){
			chooseAreasParticipantsLink.setI18nKey("areaCreate");
		}
		
		String areaParticipantsInitVal;
		@SuppressWarnings("unchecked")
		List<Long> areaParticipantsKeys = (List<Long>)config.get(CONFIG_KEY_PARTICIPANTS_AREA_ID);
		if(areaParticipantsKeys == null) {
			areaParticipantsInitVal = (String)config.get(CONFIG_KEY_PARTICIPANTS_AREA);
			areaParticipantsKeys = areaManager.toAreaKeys(areaParticipantsInitVal, cev.getCourseGroupManager().getCourseResource());
		}
		areaParticipantsInitVal = getAreaNames(areaParticipantsKeys);

		easyAreaParticipantsSelectionList = uifactory.addStaticTextElement("areaParticipants", null, areaParticipantsInitVal, formLayout);
		easyAreaParticipantsSelectionList.setTranslator(host.getFragmentTranslator());		
		easyAreaParticipantsSelectionList.setUserObject(areaParticipantsKeys);
		easyAreaParticipantsSelectionList.setVisible(false);
		easyAreaParticipantsSelectionList.setElementCssClass("text-muted");
	
		uifactory.addSpacerElement("s4", formLayout, false);
				
//		update();			
	}

	@Override
	public void refreshContents() {
		update();
	}
	
	private void update () {
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
		
		container.setNeedsLayout();
	}
	
	@Override
	public boolean validateFormLogic(UserRequest ureq) {
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
	public void dispose() {
		// nothing at the moment
	}

	@Override
	public boolean processFormEvent(UserRequest ureq, FormItem source, FormEvent event) {
		boolean processed = false;
		
		if (source == chooseGroupCoachesLink) {
			host.getFragmentController().removeAsListenerAndDispose(cmc);
			host.getFragmentController().removeAsListenerAndDispose(groupChooseCoaches);

			groupChooseCoaches = new GroupSelectionController(ureq, host.getFragmentController().getWindowControl(), true,
					cev.getCourseGroupManager(), getKeys(easyGroupCoachSelectionList));
			host.getFragmentController().listenTo(groupChooseCoaches);
			
			String title = chooseGroupCoachesLink.getLinkTitleText();
			cmc = new CloseableModalController(host.getFragmentController().getWindowControl(), "close", groupChooseCoaches.getInitialComponent(), true, title);
			host.getFragmentController().listenTo(cmc);
			cmc.activate();
			host.getFragmentController().setFormCanSubmit(false);
			processed = true;
		} else if(source == chooseGroupParticipantsLink){
			host.getFragmentController().removeAsListenerAndDispose(cmc);
			host.getFragmentController().removeAsListenerAndDispose(groupChooseParticipants);
			
			groupChooseParticipants = new GroupSelectionController(ureq, host.getFragmentController().getWindowControl(), true,
					cev.getCourseGroupManager(), getKeys(easyGroupParticipantsSelectionList));
			host.getFragmentController().listenTo(groupChooseParticipants);
			
			String title = chooseGroupParticipantsLink.getLabelText();
			cmc = new CloseableModalController(host.getFragmentController().getWindowControl(), "close", groupChooseParticipants.getInitialComponent(), true, title);
			host.getFragmentController().listenTo(cmc);
			cmc.activate();
			host.getFragmentController().setFormCanSubmit(false);
			processed = true;
		} else if (source == chooseAreasCoachesLink) {
			// already areas -> choose areas
			host.getFragmentController().removeAsListenerAndDispose(cmc);
			host.getFragmentController().removeAsListenerAndDispose(areaChooseCoaches);
			
			areaChooseCoaches = new AreaSelectionController (ureq, host.getFragmentController().getWindowControl(), true,
					cev.getCourseGroupManager(), getKeys(easyAreaCoachSelectionList));
			host.getFragmentController().listenTo(areaChooseCoaches);

			String title = chooseAreasCoachesLink.getLinkTitleText();
			cmc = new CloseableModalController(host.getFragmentController().getWindowControl(), "close", areaChooseCoaches.getInitialComponent(), true, title);
			host.getFragmentController().listenTo(cmc);
			cmc.activate();
			host.getFragmentController().setFormCanSubmit(false);
			processed = true;
		} else if (source == chooseAreasParticipantsLink){
			// already areas -> choose areas
			host.getFragmentController().removeAsListenerAndDispose(cmc);
			host.getFragmentController().removeAsListenerAndDispose(areaChooseParticipants);
			
			areaChooseParticipants = new AreaSelectionController (ureq, host.getFragmentController().getWindowControl(), true,
					cev.getCourseGroupManager(), getKeys(easyAreaParticipantsSelectionList));
			host.getFragmentController().listenTo(areaChooseParticipants);

			String title = chooseAreasParticipantsLink.getLabelText();
			cmc = new CloseableModalController(host.getFragmentController().getWindowControl(), "close", areaChooseParticipants.getInitialComponent(), true, title);
			host.getFragmentController().listenTo(cmc);
			cmc.activate();
			host.getFragmentController().setFormCanSubmit(false);
			processed = true;
		} 

		return processed;
	}

	protected void cleanUp() {
		host.getFragmentController().removeAsListenerAndDispose(cmc);
		host.getFragmentController().removeAsListenerAndDispose(areaChooseParticipants);
		host.getFragmentController().removeAsListenerAndDispose(areaChooseCoaches);
		host.getFragmentController().removeAsListenerAndDispose(groupChooseCoaches);			
	}
	
	@Override
	public boolean processEvent(UserRequest ureq, Controller source, Event event) {

		host.getFragmentController().setFormCanSubmit(true);
//		subm.setEnabled(true);
		if (source == groupChooseCoaches) {
			if (event == Event.DONE_EVENT) {
				cmc.deactivate();
				easyGroupCoachSelectionList.setValue(getGroupNames(groupChooseCoaches.getSelectedKeys()));
				easyGroupCoachSelectionList.setUserObject(groupChooseCoaches.getSelectedKeys());
				chooseGroupCoachesLink.setI18nKey("groupCoachesChoose");
				container.setNeedsLayout();
			} else if (Event.CANCELLED_EVENT == event) {
				cmc.deactivate();
			}
		} else if (source == areaChooseCoaches) {
			if (event == Event.DONE_EVENT) {
				cmc.deactivate();
				easyAreaCoachSelectionList.setValue(getAreaNames(areaChooseCoaches.getSelectedKeys()));
				easyAreaCoachSelectionList.setUserObject(areaChooseCoaches.getSelectedKeys());
				chooseAreasCoachesLink.setI18nKey("areaCoachesChoose");
				container.setNeedsLayout();
			} else if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			}
		} else if (source == groupChooseParticipants) {
			if (event == Event.DONE_EVENT) {
				cmc.deactivate();
				easyGroupParticipantsSelectionList.setValue(getGroupNames(groupChooseParticipants.getSelectedKeys()));
				easyGroupParticipantsSelectionList.setUserObject(groupChooseParticipants.getSelectedKeys());
				chooseGroupParticipantsLink.setI18nKey("groupParticipantsChoose");
				container.setNeedsLayout();
			} else if (Event.CANCELLED_EVENT == event) {
				cmc.deactivate();
			}
		} else if (source == areaChooseParticipants) {
			if (event == Event.DONE_EVENT) {
				cmc.deactivate();
				easyAreaParticipantsSelectionList.setValue(getAreaNames(areaChooseParticipants.getSelectedKeys()));
				easyAreaParticipantsSelectionList.setUserObject(areaChooseParticipants.getSelectedKeys());
				chooseAreasParticipantsLink.setI18nKey("areaParticipantsChoose");
				container.setNeedsLayout();
			} else if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			}
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
		return coachesChoice.isSelected(0)&& wantCoaches.isSelected(0);
	}
	
	protected boolean sendToCoachesGroup(){
		return coachesChoice.isSelected(2) && wantCoaches.isSelected(0);
	}
	
	protected boolean sendToParticipantsCourse(){
		return participantsChoice.isSelected(1)&& wantParticipants.isSelected(0);
	}
	
	protected boolean sendToParticipantsAll(){
		return participantsChoice.isSelected(0)&& wantParticipants.isSelected(0);
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
	
	@Override
	public void storeConfiguration(UserRequest ureq, IModuleConfiguration moduleConfiguration) {
		MembersSelectorFormFragment configForm = this;
		moduleConfiguration.set(CONFIG_KEY_COACHES_GROUP, configForm.getGroupCoaches());
		moduleConfiguration.set(CONFIG_KEY_COACHES_GROUP_ID, configForm.getGroupCoachesIds());
		moduleConfiguration.set(CONFIG_KEY_COACHES_AREA, configForm.getCoachesAreas());
		moduleConfiguration.set(CONFIG_KEY_COACHES_AREA_IDS, configForm.getCoachesAreaIds());
		moduleConfiguration.setBooleanEntry(CONFIG_KEY_COACHES_ALL, configForm.sendToCoachesAll());
		moduleConfiguration.setBooleanEntry(CONFIG_KEY_COACHES_COURSE, configForm.sendToCoachesCourse());
		
		moduleConfiguration.set(CONFIG_KEY_PARTICIPANTS_GROUP, configForm.getGroupParticipants());
		moduleConfiguration.set(CONFIG_KEY_PARTICIPANTS_GROUP_ID, configForm.getGroupParticipantsIds());
		moduleConfiguration.set(CONFIG_KEY_PARTICIPANTS_AREA, configForm.getParticipantsAreas());
		moduleConfiguration.set(CONFIG_KEY_PARTICIPANTS_AREA_ID, configForm.getParticipantsAreaIds());
		moduleConfiguration.setBooleanEntry(CONFIG_KEY_PARTICIPANTS_ALL, configForm.sendToParticipantsAll());
		moduleConfiguration.setBooleanEntry(CONFIG_KEY_PARTICIPANTS_COURSE, configForm.sendToParticipantsCourse());
	}

}
