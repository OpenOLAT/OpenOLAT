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
package org.olat.course.condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.JSDateChooser;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.StringHelper;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.condition.interpreter.ConditionDateFormatter;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.nodes.CourseNode;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.ui.NewAreaController;
import org.olat.group.ui.NewBGController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.shibboleth.ShibbolethModule;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * Description:<br>
 * The ConditionConfigEasyController implements the easy condition editing
 * workflow in the course editor. It has several complex dependency rules to
 * show and hide form input fields, and also the possibility to start
 * subworkflows for creation of groups or areas.
 * <P>
 * Initial Date: 13.06.2007 <br>
 * 
 * @author patrickb
 */
public class ConditionConfigEasyController extends FormBasicController implements GenericEventListener {
	private Condition validatedCondition;
	private CourseEditorEnv courseEditorEnv;
	private List<CourseNode> nodeIdentList;
	private MultipleSelectionElement coachExclusive;
	private MultipleSelectionElement assessmentMode;
	private MultipleSelectionElement assessmentModeResultVisible;
	private JSDateChooser fromDate;
	private JSDateChooser toDate;
	private FormItemContainer dateSubContainer;
	private FormItemContainer groupSubContainer;
	private FormItemContainer assessSubContainer;
	private FormLayoutContainer areaChooseSubContainer;
	private FormLayoutContainer groupChooseSubContainer;
	private MultipleSelectionElement dateSwitch;
	private StaticTextElement easyGroupList;
	private FormLink chooseGroupsLink;
	private StaticTextElement easyAreaList;
	private FormLink chooseAreasLink;
	private MultipleSelectionElement groupSwitch;
	private GroupSelectionController groupChooseC;
	private AreaSelectionController areaChooseC;
	private MultipleSelectionElement assessmentSwitch;
	private SingleSelection assessmentTypeSwitch;
	private SingleSelection nodePassed;
	//
	private MultipleSelectionElement attributeSwitch;
	private SingleSelection attributeBconnector;
	private AttributeEasyRowAdderController attribteRowAdderSubform;
	//
	private IntegerElement cutValue;
	private MultipleSelectionElement applyRulesForCoach;
	
	private NewBGController groupCreateCtlr;
	private CloseableModalController cmc;
	private NewAreaController areaCreateCtlr;
	private FormLink createGroupsLink;
	private FormLink createAreasLink;

	private static final String NODEPASSED_VAL_PASSED = "passed";
	private static final String NODEPASSED_VAL_SCORE = "score";

	private static final String DELETED_NODE_IDENTIFYER = "deletedNode";
	private static final String NO_NODE_SELECTED_IDENTIFYER = "";
	protected static final String BCON_VAL_OR = "bcon_or";
	protected static final String BCON_VAL_AND = "bcon_and";
	
	
	private EventBus singleUserEventCenter;
	private OLATResourceable groupConfigChangeEventOres;
	
	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private ShibbolethModule shibbolethModule;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private AssessmentModule assessmentModule;
	
	private boolean managedGroup;
	
	/**
	 * with default layout <tt>_content/easycondedit.html</tt>
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param cond The condition to edit
	 * @param nodeIdentList The list of assessable course elements
	 * @param env The course editor environment
	 */
	public ConditionConfigEasyController(UserRequest ureq, WindowControl wControl, Condition cond,
			List<CourseNode> nodeIdentList, CourseEditorEnv env) {
		super(ureq, wControl, "easycondedit");

		singleUserEventCenter = ureq.getUserSession().getSingleUserEventCenter();
		groupConfigChangeEventOres = OresHelper.createOLATResourceableType(MultiUserEvent.class);
		singleUserEventCenter.registerFor(this, ureq.getIdentity(), groupConfigChangeEventOres);
		
		OLATResource courseResource = env.getCourseGroupManager().getCourseResource();
		RepositoryEntry courseRe = RepositoryManager.getInstance().lookupRepositoryEntry(courseResource, false);
		managedGroup = RepositoryEntryManagedFlag.isManaged(courseRe, RepositoryEntryManagedFlag.groups);

		/*
		 * my instance variables, these data is used by form items to initialise
		 */
		validatedCondition = cloneCondition(cond);
		if (validatedCondition == null) {
			throw new OLATRuntimeException("CondititionEditController called with a NULL condition", new IllegalArgumentException());
		}
		this.courseEditorEnv = env;	
		this.nodeIdentList = nodeIdentList;

		initForm(ureq);
		updateUI();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// or blocked for learner
		coachExclusive = uifactory.addCheckboxesHorizontal("coachExclusive", null, formLayout,
				new String[] { "ison" }, new String[] { translate("form.easy.coachExclusive") });
		coachExclusive.setElementCssClass("o_sel_condition_coach_exclusive");
		coachExclusive.addActionListener(FormEvent.ONCLICK);
		if(validatedCondition.isEasyModeCoachesAndAdmins()) {
			coachExclusive.select("ison", true);
		}
	
		initFromToDateChooser(formLayout);
		initEasyGroupAreaChoosers(formLayout);
		initAssessmentSwitch(formLayout);
		// shibboleth attributes
		boolean enableShibbolethEasyConfig = shibbolethModule.isEnableShibbolethCourseEasyConfig();
		if(enableShibbolethEasyConfig) {
			initAttributeSwitch(formLayout, ureq);
		}
		flc.contextPut("shibbolethEnabled", Boolean.valueOf(enableShibbolethEasyConfig));
		
		assessmentMode = uifactory.addCheckboxesHorizontal("assessmentMode", null, formLayout,
				new String[] { "ison" }, new String[] { translate("form.easy.assessmentMode") });
		if(validatedCondition.isAssessmentMode()) {
			assessmentMode.select("ison", true);
		}
		assessmentMode.addActionListener(FormEvent.ONCLICK);
		assessmentMode.setVisible(assessmentModule.isAssessmentModeEnabled());
		
		assessmentModeResultVisible = uifactory.addCheckboxesHorizontal("assessmentModeResultVisible", null, formLayout,
				new String[] { "ison" }, new String[] { translate("form.easy.assessmentMode.visible") });
		if(validatedCondition.isAssessmentModeViewResults()) {
			assessmentModeResultVisible.select("ison", true);
		}
		assessmentModeResultVisible.setVisible(assessmentModule.isAssessmentModeEnabled() && assessmentMode.isAtLeastSelected(1));
		
		applyRulesForCoach = uifactory.addCheckboxesHorizontal("applyRulesForCoach", null, formLayout,
				new String[] { "ison" }, new String[] { translate("form.easy.applyRulesForCoach") });
		applyRulesForCoach.setVisible(isDateGroupAssessmentOrAttributeSwitchOnOrAssessmentModeOn());
		// note that in the condition this rule is saved with the opposite meaning:
		// true when coach and admins always have access, false when rule should apply also to them
		if (!validatedCondition.isEasyModeAlwaysAllowCoachesAndAdmins()) {
			applyRulesForCoach.select("ison", true);
		}
		
		// the submit button
		uifactory.addFormSubmitButton("subm", "submit", formLayout);
		validateGroupFields();
	}
	
	private void initFromToDateChooser(FormItemContainer formLayout) {
		/*
		 * yes / no switch if a date.time constraint is defined
		 */
		dateSubContainer = FormLayoutContainer.createDefaultFormLayout("dateSubContainer", getTranslator());
		formLayout.add(dateSubContainer);

		fromDate = new JSDateChooser("fromDate", getLocale());
		fromDate.setLabel("form.easy.bdate", null);
		fromDate.setDate(ConditionDateFormatter.parse(validatedCondition.getEasyModeBeginDate()));
		fromDate.setExampleKey("form.easy.example.bdate", null);
		fromDate.setDateChooserTimeEnabled(true);
		fromDate.setDisplaySize(fromDate.getExampleDateString().length());
		fromDate.setValidDateCheck("form.error.date");
		dateSubContainer.add(fromDate);
		
		toDate = new JSDateChooser("toDate", getLocale());
		toDate.setLabel("form.easy.edate", null);
		toDate.setDate(ConditionDateFormatter.parse(validatedCondition.getEasyModeEndDate()));
		toDate.setExampleKey("form.easy.example.edate", null);
		toDate.setDateChooserTimeEnabled(true);
		toDate.setDisplaySize(toDate.getExampleDateString().length());
		toDate.setDefaultTimeAtEndOfDay(true);
		toDate.setValidDateCheck("form.error.date");
		dateSubContainer.add(toDate);

		dateSwitch = uifactory.addCheckboxesHorizontal("dateSwitch", null, formLayout, new String[] { "ison" }, new String[] { translate("form.easy.dateSwitch") });
		if (fromDate.getDate() != null || toDate.getDate() != null) {
			dateSwitch.select("ison", true);
		}
		dateSwitch.addActionListener(FormEvent.ONCLICK);
	}
	
	/**
	 * @param formLayout The form container
	 */
	private void initEasyGroupAreaChoosers(FormItemContainer formLayout) {

		groupSubContainer = FormLayoutContainer.createBareBoneFormLayout("groupSubContainer", getTranslator());
		formLayout.add(groupSubContainer);

		List<Long> groupKeyList = validatedCondition.getEasyModeGroupAccessIdList();
		String groupInitVal = getGroupNames(groupKeyList);
		List<Long> areaKeyList  = validatedCondition.getEasyModeGroupAreaAccessIdList();
		String areaInitVal = getAreaNames(areaKeyList);

		groupSwitch = uifactory.addCheckboxesHorizontal("groupSwitch", null, formLayout, new String[] { "ison" }, new String[] { translate("form.easy.groupSwitch") });
		groupSwitch.setElementCssClass("o_sel_condition_groups");
		// initialize selection
		if (!groupKeyList.isEmpty() || !areaKeyList.isEmpty()) {
			groupSwitch.select("ison", true);
		}
		groupSwitch.addActionListener(FormEvent.ONCLICK);
		
		//groups
		groupChooseSubContainer = FormLayoutContainer.createDefaultFormLayout("groupChooseSubContainer", getTranslator());
		groupSubContainer.add(groupChooseSubContainer);		

		easyGroupList = uifactory.addStaticTextElement("groupList", "form.easy.group", groupInitVal, groupChooseSubContainer);
		easyGroupList.setUserObject(groupKeyList);

		chooseGroupsLink = uifactory.addFormLink("choose", groupChooseSubContainer, "o_form_groupchooser");
		chooseGroupsLink.setElementCssClass("o_sel_condition_choose_groups");
		createGroupsLink = uifactory.addFormLink("create", groupChooseSubContainer, "o_form_groupchooser");	
		createGroupsLink.setElementCssClass("o_sel_condition_create_groups");
		
		//areas
		areaChooseSubContainer = FormLayoutContainer.createDefaultFormLayout("areaChooseSubContainer", getTranslator());
		groupSubContainer.add(areaChooseSubContainer);		

		easyAreaList = uifactory.addStaticTextElement("groupList", "form.easy.area", areaInitVal, areaChooseSubContainer);
		easyAreaList.setUserObject(areaKeyList);
		
		chooseAreasLink = uifactory.addFormLink("choose", areaChooseSubContainer, "o_form_groupchooser");
		createAreasLink = uifactory.addFormLink("create", areaChooseSubContainer, "o_form_groupchooser");
		
		updateGroupsAndAreasCheck();
	}
	
	private void initAttributeSwitch(FormItemContainer formLayout, UserRequest ureq) {

		String[] values = new String[]{ translate("form.easy.attributeSwitch") };
		attributeSwitch = uifactory.addCheckboxesHorizontal("attributeSwitch", null, formLayout, new String[] { "ison" }, values);
		attributeSwitch.select("ison", validatedCondition.getAttributeConditions() != null);
		// register for on click event to hide/disable other elements
		attributeSwitch.addActionListener(FormEvent.ONCLICK);
		// rules are later added

		// add attribute connector: AND or OR
		final String[] attributebconnectorValues = new String[] { translate("form.easy.attributebconnector.and"), translate("form.easy.attributebconnector.or") };
		attributeBconnector = uifactory.addRadiosHorizontal("attributeBconnector", "form.easy.attributebconnector", formLayout, new String[] { BCON_VAL_AND, BCON_VAL_OR }, attributebconnectorValues);
		if(validatedCondition.isConditionsConnectorIsAND()){
			attributeBconnector.select(BCON_VAL_AND, true);
		} else {
			attributeBconnector.select(BCON_VAL_OR, true);
		}
		
		// add attribute condition rows
		attribteRowAdderSubform = new AttributeEasyRowAdderController(ureq, getWindowControl(), mainForm);
		flc.add(attribteRowAdderSubform.getFormItem());
		
		listenTo(attribteRowAdderSubform);
		flc.put("attribteRowAdderSubform", attribteRowAdderSubform.getInitialComponent());
		if (validatedCondition.getAttributeConditions() != null && !validatedCondition.getAttributeConditions().isEmpty()) {
			attribteRowAdderSubform.setAttributeConditions(validatedCondition.getAttributeConditions());
		}
	}
	
	private void initAssessmentSwitch(FormItemContainer formLayout) {
		assessSubContainer = FormLayoutContainer.createDefaultFormLayout("assessSubContainer", getTranslator());
		formLayout.add(assessSubContainer);

		Translator translator = getTranslator();

		final String[] assessmentSwitchKeys = new String[] { NODEPASSED_VAL_PASSED, NODEPASSED_VAL_SCORE };
		final String[] assessmentSwitchValues = new String[] { translator.translate("form.easy.assessmentSwitch.passed"),
				translator.translate("form.easy.assessmentSwitch.score") };

		String nodePassedInitVal = validatedCondition.getEasyModeNodePassedId();
		String cutInitStrValue = validatedCondition.getEasyModeCutValue();
		int cutInitValue = 0;
		
		assessmentSwitch = uifactory.addCheckboxesHorizontal("assessmentSwitch", null, formLayout, new String[] { "ison" }, new String[] { translate("form.easy.assessmentSwitch") });
		assessmentSwitch.addActionListener(FormEvent.ONCLICK);
		

		boolean selectedNodeIsInList = false;
		List<String> keysList = new ArrayList<>();
		List<String> valuesList = new ArrayList<>();
		if (nodeIdentList.isEmpty()) {
			// no nodes to be selected
			keysList.add(NO_NODE_SELECTED_IDENTIFYER);
			valuesList.add("- " + translator.translate("form.easy.nodePassed.noNodes"));
			//disable switch
			assessmentSwitch.setEnabled(false);
		} else {
			// there are nodes to be selected
			keysList.add(NO_NODE_SELECTED_IDENTIFYER);
			valuesList.add("- " + translator.translate("form.easy.nodePassed.select") + " -");
			for (int i = 0; i < nodeIdentList.size(); i++) {
				CourseNode courseNode = nodeIdentList.get(i);
				keysList.add(courseNode.getIdent());
				valuesList.add(courseNode.getShortName() + " (Id:" + courseNode.getIdent() + ")");
				if (courseNode.getIdent().equals(nodePassedInitVal)) selectedNodeIsInList = true;
			}
		}
		// add dummy value if needed
		if (nodePassedInitVal != null && !selectedNodeIsInList) {
			keysList.add(DELETED_NODE_IDENTIFYER);
			valuesList.add("- " + translator.translate("form.easy.nodePassed.deletedNode") + " -");
		}
		final String[] nodePassedKeys = new String[keysList.size()];
		keysList.toArray(nodePassedKeys);
		final String[] nodePassedValues = new String[valuesList.size()];
		valuesList.toArray(nodePassedValues);

		nodePassed = uifactory.addDropdownSingleselect("nodePassed", "form.easy.nodePassed", assessSubContainer, nodePassedKeys, nodePassedValues, null);
		if (nodePassedInitVal != null) {
			if (selectedNodeIsInList) {
				nodePassed.select(nodePassedInitVal, true);
			} else {
				nodePassed.select(DELETED_NODE_IDENTIFYER, true);
			}
		} else {
			nodePassed.select(NO_NODE_SELECTED_IDENTIFYER, true);
		}
		
		assessmentTypeSwitch = uifactory.addRadiosVertical("assessmentTypeSwitch", null, assessSubContainer,
				assessmentSwitchKeys, assessmentSwitchValues);
		assessmentTypeSwitch.setLabel("form.easy.assessmentSwitch.type", null);
		assessmentTypeSwitch.addActionListener(FormEvent.ONCLICK);

		if (nodePassedInitVal != null) {
			// has a node configured
			if (cutInitStrValue == null) {
				// with cut value
				assessmentTypeSwitch.select(NODEPASSED_VAL_PASSED, true);
			} else {
				cutInitValue = Integer.valueOf(cutInitStrValue);
				assessmentTypeSwitch.select(NODEPASSED_VAL_SCORE, true);
			}
			assessmentSwitch.select("ison", true);
			assessmentTypeSwitch.setVisible(true);
		} else {
			assessmentSwitch.uncheckAll();
			assessmentTypeSwitch.setVisible(false);
			assessmentTypeSwitch.select(NODEPASSED_VAL_PASSED, true);
		}
		
		cutValue = uifactory.addIntegerElement("cutval", "form.easy.cutValue", cutInitValue, assessSubContainer);
		cutValue.setDisplaySize(3);
	}

	@Override
	protected void doDispose() {
		singleUserEventCenter.deregisterFor(this, groupConfigChangeEventOres);
        super.doDispose();
	}

	@Override
	protected void formOK(UserRequest ureq) {

		// 1) rule applies also for coach switch - one checkbox
		// has opposite meaning -> selected is saved as false, and vice versa
		if (applyRulesForCoach.getSelectedKeys().isEmpty()) {
			validatedCondition.setEasyModeAlwaysAllowCoachesAndAdmins(true);
		} else {
			validatedCondition.setEasyModeAlwaysAllowCoachesAndAdmins(false);
		}
		// 2) admin and coach switch - one checkbox
		if (coachExclusive.getSelectedKeys().size() == 1) {
			validatedCondition.setEasyModeCoachesAndAdmins(true);
			// when true discard all other rules
			validatedCondition.setEasyModeBeginDate(null);
			validatedCondition.setEasyModeEndDate(null);
			validatedCondition.setEasyModeGroupAccess(null);
			validatedCondition.setEasyModeGroupAreaAccess(null);
			validatedCondition.setEasyModeCutValue(null);
			validatedCondition.setEasyModeNodePassedId(null);
			validatedCondition.setAttributeConditions(null);
			validatedCondition.setAttributeConditionsConnectorIsAND(null);
			validatedCondition.setAssessmentMode(false);
			validatedCondition.setAssessmentModeViewResults(false);
			validatedCondition.setEasyModeAssessmentModeNodeId(null);
			validatedCondition.setEasyModeGroupAccessIdList(null);
			validatedCondition.setEasyModeGroupAreaAccessIdList(null);
		} else {
			validatedCondition.setEasyModeCoachesAndAdmins(false);
			// 3) date switch
			if (dateSwitch.getSelectedKeys().size() == 1) {
				if (StringHelper.containsNonWhitespace(fromDate.getValue())) {
					validatedCondition.setEasyModeBeginDate(ConditionDateFormatter.format(fromDate.getDate()));
				} else {
					validatedCondition.setEasyModeBeginDate(null);
				}

				if (StringHelper.containsNonWhitespace(toDate.getValue())) {
					validatedCondition.setEasyModeEndDate(ConditionDateFormatter.format(toDate.getDate()));
				} else {
					validatedCondition.setEasyModeEndDate(null);
				}
			} else {
				validatedCondition.setEasyModeBeginDate(null);
				validatedCondition.setEasyModeEndDate(null);
			}

			// 4) group switch
			if (groupSwitch.getSelectedKeys().size() == 1) {
				// groups
				if (!isEmpty(easyGroupList)) {
					validatedCondition.setEasyModeGroupAccessIdList(getKeys(easyGroupList));
				} else {
					validatedCondition.setEasyModeGroupAccess(null);
				}
				// areas
				if (!isEmpty(easyAreaList)) {
					validatedCondition.setEasyModeGroupAreaAccessIdList(getKeys(easyAreaList));
				} else {
					validatedCondition.setEasyModeGroupAreaAccess(null);
				}
			} else {
				validatedCondition.setEasyModeGroupAccess(null);
				validatedCondition.setEasyModeGroupAccessIdList(null);
				validatedCondition.setEasyModeGroupAreaAccess(null);
				validatedCondition.setEasyModeGroupAreaAccessIdList(null);
			}

			// 5) assessment switch
			if (assessmentSwitch.getSelectedKeys().size() == 1) {
				// now evaluate the selection
				// which node, if one is selected
				if (!nodePassed.getSelectedKey().equals("")) {
					validatedCondition.setEasyModeNodePassedId(nodePassed.getSelectedKey());
				} else {
					validatedCondition.setEasyModeNodePassedId(null);
				}
				if (assessmentTypeSwitch.getSelectedKey().equals(NODEPASSED_VAL_SCORE)) {
					// this is formOK -> value of integer elment is ensured to be an
					// int!
					validatedCondition.setEasyModeCutValue(cutValue.getValue());
				} else {
					validatedCondition.setEasyModeCutValue(null);
				}
			} else {
				validatedCondition.setEasyModeNodePassedId(null);
				if(nodeIdentList.isEmpty()) {
					assessmentSwitch.setEnabled(false);
				}
			}

			// 6) attribute switch
			if (shibbolethModule.isEnableShibbolethCourseEasyConfig()) {
				if (attributeSwitch.getSelectedKeys().size() == 1) {
					List<ExtendedCondition> le = attribteRowAdderSubform.getAttributeConditions();
		
					if (le.isEmpty()) {
						// some error(s) occured, don't save anything
						validatedCondition.setAttributeConditions(null); 
					} else {
						// add the conditions
						validatedCondition.setAttributeConditions(le); 
					}
					validatedCondition.setAttributeConditionsConnectorIsAND(Boolean.valueOf(attributeBconnector.getSelectedKey().equals(BCON_VAL_AND)));
		
				} else {
					validatedCondition.setAttributeConditions(null);
					validatedCondition.setAttributeConditionsConnectorIsAND(null);
				}
			}
			
			// assessment mode
			validatedCondition.setAssessmentMode(assessmentMode.isAtLeastSelected(1));
			
			if(assessmentMode.isAtLeastSelected(1) && assessmentModeResultVisible.isAtLeastSelected(1)) {
				validatedCondition.setAssessmentModeViewResults(true);
				String currentNodeId = courseEditorEnv.getCurrentCourseNodeId();
				validatedCondition.setEasyModeAssessmentModeNodeId(currentNodeId);
			} else {
				validatedCondition.setAssessmentModeViewResults(false);
				validatedCondition.setEasyModeAssessmentModeNodeId(null);
			}
		}

		// calculate expression from easy mode form
		String condString = validatedCondition.getConditionFromEasyModeConfiguration();
		validatedCondition.setConditionExpression(condString);
		validatedCondition.setExpertMode(false);

		/*
		 * condition is updated
		 */
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	@Override
	public void event(Event event) {
		if (event.getCommand().equals("changed")) {
			updateGroupsAndAreasCheck();
			validateGroupFields();
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, org.olat.core.gui.control.Event event) {
		if (source == groupChooseC) {
			if (Event.DONE_EVENT == event) {
				doGroupChoosed(ureq, groupChooseC.getSelectedKeys());
				cmc.deactivate();
			} else if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			}
		} else if (source == areaChooseC) {	
			if (event == Event.DONE_EVENT) {
				doAreaChoosed(ureq, areaChooseC.getSelectedKeys());
				cmc.deactivate();
			} else if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			}
		} else if (source == groupCreateCtlr) {
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				doGroupCreated(ureq, groupCreateCtlr.getCreatedGroupKeys());
			} 
		} else if (source == areaCreateCtlr) {
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				doAreaCreated(ureq, areaCreateCtlr.getCreatedAreaKeys());
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == chooseGroupsLink) {
			doChooseGroup(ureq);
		} else if (source == createGroupsLink) {
			doCreateGroup(ureq);
		} else if (source == chooseAreasLink) {
			doChooseArea(ureq);
		} else if (source == createAreasLink) {
			doCreateArea(ureq);
		} else {
			if(coachExclusive == source && coachExclusive.isAtLeastSelected(1)) {
				setBlockedForLearner();
			} else if(dateSwitch == source && !dateSwitch.isAtLeastSelected(1)) {
				disableDates();
			} else if(assessmentSwitch == source && !assessmentSwitch.isAtLeastSelected(1)) {
				disableAssessment();
			} else if(groupSwitch == source && !groupSwitch.isAtLeastSelected(1)) {
				disableGroup();
			} else if(attributeSwitch == source && !attributeSwitch.isAtLeastSelected(1)) {
				disableAttribute();
			}
			updateUI();
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean retVal = true;
		// (1)
		// dateswitch is enabled and the datefields are valid
		// check complex rules involving checks over multiple elements
		
		dateSubContainer.clearError();
		if (dateSwitch.getSelectedKeys().size() == 1) {
			retVal &= validateFromToDates(ureq);
		}

		// (2)
		// groups switch is enabled
		// check if either group or area is defined
		retVal = validateGroupFields() && retVal;

		if (assessmentSwitch.getSelectedKeys().size() == 1) {
			// foop d fbg,dfbg,f ,gfbirst check two error cases of a selection
			// no node selected or a deleted node selected
			if (nodePassed.getSelectedKey().equals(NO_NODE_SELECTED_IDENTIFYER)) {				
				nodePassed.setErrorKey("form.easy.error.nodePassed");
				retVal = false;
			} else if (nodePassed.getSelectedKey().equals(DELETED_NODE_IDENTIFYER)) {
				nodePassed.setErrorKey("form.easy.error.nodeDeleted");
				retVal = false;
			} else {
				//clear nodepassed error
				nodePassed.clearError();
				//retVal stays
			}
		}
		
		if (shibbolethModule.isEnableShibbolethCourseEasyConfig()) {
			retVal &= validateAttibuteFields();	
		}
		//
		return retVal;
	}
	
	private boolean validateFromToDates(UserRequest ureq) {
		boolean allOk = true;
		
		Date fromDateVal = fromDate.getDate();
		Date toDateVal = toDate.getDate();
		// clear all errors
		toDate.clearError();
		fromDate.clearError();
		dateSubContainer.clearError();

		// validate again the 2 fields
		// one must be set
		if (fromDate.isEmpty() && toDate.isEmpty()) {
			// error concern both input fields -> set it as switch error				
			dateSubContainer.setErrorKey("form.easy.error.date");
			allOk &= false;
		} else if(!validateFormItem(ureq, fromDate) || !validateFormItem(ureq, toDate)) {
			allOk &= false;
		} else if (fromDateVal != null && toDateVal != null) {

			// if both are set, check from < to
			/*
			 * bugfix http://bugs.olat.org/jira/browse/OLAT-813 valid dates and not
			 * empty, in easy mode we assume that Start and End date should
			 * implement the meaning of
			 * ----false---|S|-----|now|-TRUE---------|E|---false--->t .............
			 * Thus we check for Startdate < Enddate, error otherwise
			 */
			if (fromDateVal.after(toDateVal)) {					
				dateSubContainer.setErrorKey("form.easy.error.bdateafteredate");
				allOk &= false;
			}
		} else if (fromDateVal == null && !fromDate.isEmpty()) {
			//not a correct begin date
			fromDate.setErrorKey("form.easy.error.bdate");
			allOk &= false;
		} else if (toDateVal == null && !toDate.isEmpty()) {
			toDate.setErrorKey("form.easy.error.edate");
			allOk &= false;
		}

		
		return allOk;
	}

	/**
	 * @param retVal
	 * @return
	 */
	private boolean validateGroupFields() {
		boolean retVal = true;
		if (groupSwitch.getSelectedKeys().size() == 1) {
			
			List<Long> activeGroupSelection = null;
			List<Long> activeAreaSelection = null;
			groupChooseSubContainer.clearError();
			if (!isEmpty(easyGroupList)) {
				// check whether groups exist
				activeGroupSelection = getKeys(easyGroupList);

				Set<Long> missingGroups = new HashSet<>();
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

				if (missingGroups.isEmpty()) {
					easyGroupList.clearError();
				} else {
					// Only show a message, don't block save
					String labelKey = missingGroups.size() == 1 ? "error.notfound.name" : "error.notfound.names";
					String csvMissGrps = toString(missingGroups);
					String[] params = new String[] { "-", csvMissGrps };
					easyGroupList.setWarningKey(labelKey, params);
				}
			}
			areaChooseSubContainer.clearError();
			if (!isEmpty(easyAreaList)) {
				// check whether areas exist
				activeAreaSelection = getKeys(easyAreaList);
				
				List<Long> missingAreas = new ArrayList<>();
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
				
				if (missingAreas.isEmpty()) {
					easyAreaList.clearError();
				} else {
					retVal = false;
					String labelKey = missingAreas.size() == 1 ? "error.notfound.name" : "error.notfound.names";
					String csvMissAreas = toString(missingAreas);
					String[] params = new String[] { "-", csvMissAreas };
					easyAreaList.setWarningKey(labelKey, params);
				}
			}

			boolean easyGroupOK = (!isEmpty(easyGroupList) && activeGroupSelection != null && !activeGroupSelection.isEmpty());
			boolean easyAreaOK = (!isEmpty(easyAreaList) && activeAreaSelection != null && !activeAreaSelection.isEmpty());
			if (easyGroupOK || easyAreaOK) {
				// clear general error
				groupSubContainer.clearError();
			} else {
				// error concerns both fields -> set it as switch error
				groupSubContainer.setErrorKey("form.easy.error.group");
				retVal = false;
			}
		}
		return retVal;
	}
	
	/**
	 * @return
	 */
	private boolean validateAttibuteFields() {
		boolean retVal = true;
		if (attributeSwitch.getSelectedKeys().size() == 1 && attribteRowAdderSubform.hasError()) {
			attributeSwitch.setErrorKey("form.easy.error.attribute");
			retVal = false;
			return retVal;
		}
		attributeSwitch.clearError();
		return retVal;
	}
	
	/**
	 * Check if any of the date/group/assessment/attributes switch is ON.
	 * @return
	 */
	private boolean isDateGroupAssessmentOrAttributeSwitchOnOrAssessmentModeOn() {
		return dateSwitch.getSelectedKeys().size() == 1 
				|| groupSwitch.getSelectedKeys().size() == 1 
				|| assessmentSwitch.getSelectedKeys().size() == 1
				|| (attributeSwitch!=null && attributeSwitch.getSelectedKeys().size()==1)
				|| (assessmentMode.isEnabled() && assessmentMode.isAtLeastSelected(1));
	}
	
	private void updateUI() {
		boolean blockedForLearner = coachExclusive.isAtLeastSelected(1);
		dateSwitch.setEnabled(!blockedForLearner);
		groupSwitch.setEnabled(!blockedForLearner);
		
		boolean dateEnabled = dateSwitch.isAtLeastSelected(1) && dateSwitch.isEnabled();
		toDate.setVisible(dateEnabled);
		fromDate.setVisible(dateEnabled);
		dateSubContainer.setVisible(dateEnabled);
		
		boolean assessmentEnabled = assessmentSwitch.isAtLeastSelected(1);
		assessSubContainer.setVisible(assessmentEnabled);
		assessmentTypeSwitch.setVisible(assessmentEnabled);
		nodePassed.setVisible(assessmentEnabled);
		
		boolean groupEnabled = groupSwitch.isAtLeastSelected(1) && groupSwitch.isEnabled();
		groupSubContainer.setVisible(groupEnabled);			
		easyGroupList.setFocus(groupEnabled);
		
		boolean cutValueVisible = assessmentTypeSwitch.isVisible()
				&& NODEPASSED_VAL_SCORE.equals(assessmentTypeSwitch.getSelectedKey());
		cutValue.setVisible(cutValueVisible);
		
		//assessment switch only enabled if nodes to be selected
		assessmentSwitch.setEnabled(!blockedForLearner && (!nodeIdentList.isEmpty()  || isSelectedNodeDeleted()));
		assessmentMode.setEnabled(!blockedForLearner);
		
		assessmentModeResultVisible.setVisible(assessmentMode.isAtLeastSelected(1));
		assessmentModeResultVisible.setEnabled(assessmentMode.isEnabled());
						
		//default is a checked disabled apply rules for coach
		if (attributeSwitch != null) {
			attributeSwitch.setEnabled(!blockedForLearner);
			
			boolean attributeEnabled = attributeSwitch.isAtLeastSelected(1) && attributeSwitch.isEnabled();
			attributeBconnector.setVisible(attributeEnabled);
			if(attribteRowAdderSubform != null) {
				if(attributeEnabled) {
					attribteRowAdderSubform.init();
				} else {
					attribteRowAdderSubform.cleanUp();
				}
			}
		}
		
		// base on dates and so on
		boolean applyRulesForCoachVisible = isDateGroupAssessmentOrAttributeSwitchOnOrAssessmentModeOn();
		applyRulesForCoach.setVisible(applyRulesForCoachVisible);
	}
	
	private void disableDates() {
		toDate.setDate(null);
		fromDate.setDate(null);
		toDate.clearError();
		fromDate.clearError();
		dateSwitch.clearError();
	}
	
	private void disableAssessment() {
		assessmentTypeSwitch.select(NODEPASSED_VAL_PASSED, true);
		nodePassed.select(NO_NODE_SELECTED_IDENTIFYER, true);
		cutValue.setIntValue(0);
	}
	
	private void disableAttribute() {
		attributeSwitch.clearError();
		attributeBconnector.select(BCON_VAL_AND, true);
		if(attribteRowAdderSubform != null) {
			attribteRowAdderSubform.cleanUp();
		}
	}
	
	private void disableGroup() {
		easyAreaList.clearError();
		easyGroupList.clearError();
		groupSwitch.clearError();		
		if (shibbolethModule.isEnableShibbolethCourseEasyConfig()) {
			attributeSwitch.clearError();
		}
	}
	
	private void setBlockedForLearner() {
		dateSwitch.uncheckAll();
		groupSwitch.uncheckAll();
		assessmentSwitch.uncheckAll();
		toDate.setDate(null);
		fromDate.setDate(null);
		easyAreaList.setValue("");
		easyAreaList.setUserObject(new ArrayList<Long>());
		easyGroupList.setValue("");
		easyGroupList.setUserObject(new ArrayList<Long>());
		assessmentMode.uncheckAll();
		assessmentModeResultVisible.uncheckAll();
		
		// disable the shibboleth attributes switch and reset the row subform
		if (attributeSwitch != null) {
			attributeSwitch.uncheckAll();
			attributeSwitch.clearError();
			attribteRowAdderSubform.cleanUp();
		}
	}


	/*
	 * HELPER METHODS AFTER HERE
	 */
	private Condition cloneCondition(Condition orig) {
		return orig.clone();
	}

	public Condition getCondition() {
		return validatedCondition;
	}

	/**
	 * gets called if other condition edit controller on the same screen has
	 * created groups or areas and informs other condition edit controller to 
	 * update for example the "create link" to "choose link
	 */
	private void updateGroupsAndAreasCheck() {
		OLATResource courseResource = courseEditorEnv.getCourseGroupManager().getCourseResource();
		RepositoryEntry courseEntry = courseEditorEnv.getCourseGroupManager().getCourseEntry();
		boolean hasAreas = areaManager.countBGAreasInContext(courseResource) > 0;
		boolean hasGroups = businessGroupService.countBusinessGroups(null, courseEntry) > 0;
		
		createGroupsLink.setVisible(!hasGroups && !managedGroup);
		chooseGroupsLink.setVisible(!createGroupsLink.isVisible());
		createAreasLink.setVisible(!hasAreas);
		chooseAreasLink.setVisible(!createAreasLink.isVisible());
		
	}
	
	private void doChooseGroup(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(groupChooseC);
		
		List<Long> groupKeys = getKeys(easyGroupList);
		groupChooseC = new GroupSelectionController(ureq, getWindowControl(), true,
				courseEditorEnv.getCourseGroupManager(), groupKeys);
		listenTo(groupChooseC);

		cmc = new CloseableModalController(getWindowControl(), "close", groupChooseC.getInitialComponent(),
				true, getTranslator().translate("popupchoosegroups"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doGroupChoosed(UserRequest ureq, List<Long> groupKeys) {
		easyGroupList.setUserObject(groupKeys);
		easyGroupList.setValue(getGroupNames(groupKeys));
		validateGroupFields();
		easyGroupList.getRootForm().submit(ureq);
	}
	
	private void doCreateGroup(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(groupCreateCtlr);
		
		OLATResource courseResource = courseEditorEnv.getCourseGroupManager().getCourseResource();
		RepositoryEntry courseRe = RepositoryManager.getInstance().lookupRepositoryEntry(courseResource, false);
		groupCreateCtlr = new NewBGController(ureq, getWindowControl(), courseRe, true, null);
		listenTo(groupCreateCtlr);
		cmc = new CloseableModalController(getWindowControl(), "close", groupCreateCtlr.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doGroupCreated(UserRequest ureq, Collection<Long> groupKeys) {
		List<Long> c = new ArrayList<>();
		c.addAll(getKeys(easyGroupList));
		c.addAll(groupKeys);
		easyGroupList.setValue(getGroupNames(c));
		easyGroupList.setUserObject(c);
		easyGroupList.getRootForm().submit(ureq);
		validateGroupFields();

		if (!groupKeys.isEmpty()) {
			singleUserEventCenter.fireEventToListenersOf(new MultiUserEvent("changed"), groupConfigChangeEventOres);
		}
	}
	
	private void doChooseArea(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(areaChooseC);
		
		areaChooseC = new AreaSelectionController(ureq, getWindowControl(), true,
				courseEditorEnv.getCourseGroupManager(), getKeys(easyAreaList));
		listenTo(areaChooseC);
		
		cmc = new CloseableModalController(getWindowControl(), "close", areaChooseC.getInitialComponent(),
				true, translate("popupchooseareas"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doAreaChoosed(UserRequest ureq, List<Long> areaKeys) {
		easyAreaList.setUserObject(areaKeys);
		easyAreaList.setValue(this.getAreaNames(areaKeys));
		validateGroupFields();
		easyAreaList.getRootForm().submit(ureq);
	}
	
	private void doCreateArea(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(areaCreateCtlr);

		OLATResource courseResource = courseEditorEnv.getCourseGroupManager().getCourseResource();
		areaCreateCtlr = new NewAreaController(ureq, getWindowControl(), courseResource, true, null);
		listenTo(areaCreateCtlr);

		cmc = new CloseableModalController(getWindowControl(), "close", areaCreateCtlr.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAreaCreated(UserRequest ureq, Collection<Long> createdAreaKeys) {
		List<Long> c = new ArrayList<>();
		c.addAll(getKeys(easyAreaList));
		c.addAll(createdAreaKeys);
		
		easyAreaList.setValue(getAreaNames(c));
		easyAreaList.setUserObject(c);
		easyAreaList.getRootForm().submit(ureq);
		validateGroupFields();
		
		if (!createdAreaKeys.isEmpty())  {
			singleUserEventCenter.fireEventToListenersOf(new MultiUserEvent("changed"), groupConfigChangeEventOres);
		}
	}
			
	/**
	 * It checks if the selected EasyModeNodePassedId is not null, while the assessable node list is empty, 
	 * and it enables the assessmentSwitch.
	 * 
	 * @return
	 */
	private boolean isSelectedNodeDeleted() {
		String nodePassedInitVal = validatedCondition.getEasyModeNodePassedId();
		if(nodePassedInitVal!=null && nodeIdentList.isEmpty()) {			
			assessmentSwitch.setEnabled(true);
						
			return true;
		}
		return false;
	}
	
	private boolean isEmpty(StaticTextElement element) {
		return getKeys(element).isEmpty();
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
			sb.append(StringHelper.escapeHtml(group.getName()));
		}
		return sb.toString();
	}
	
	private String getAreaNames(List<Long> keys) {
		StringBuilder sb = new StringBuilder();
		for(Long key:keys) {
			BGArea area = areaManager.loadArea(key);
			if(area != null) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(StringHelper.escapeHtml(area.getName()));
			}
		}
		return sb.toString();
	}
}
