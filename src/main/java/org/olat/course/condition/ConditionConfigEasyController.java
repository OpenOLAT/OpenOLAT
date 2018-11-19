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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.DependencyRuleApplayable;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormItemDependencyRule;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.JSDateChooser;
import org.olat.core.gui.components.form.flexible.impl.rules.RulesFactory;
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
	//private FormReset reset;
	private MultipleSelectionElement coachExclusive, assessmentMode;
	private JSDateChooser fromDate;
	private JSDateChooser toDate;
	private FormItemContainer dateSubContainer, groupSubContainer, assessSubContainer;
	private FormLayoutContainer areaChooseSubContainer,  groupChooseSubContainer;
	private MultipleSelectionElement dateSwitch;
	private StaticTextElement easyGroupList;
	private FormLink chooseGroupsLink;
	private StaticTextElement easyAreaList;
	private FormLink chooseAreasLink;
	private MultipleSelectionElement groupSwitch;
	private GroupSelectionController groupChooseC;
	private AreaSelectionController areaChooseC;
	private FormLink fixGroupError;
	private FormLink fixAreaError;
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
	 * @param ureq
	 * @param wControl
	 * @param groupMgr The course group manager
	 * @param cond condition for initialisation
	 * @param title The title that should be displayed for this condition
	 * @param formName Name of the condition form - must be unique within a HTML
	 *          page
	 * @param nodeIdentList
	 * @param euce
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
		this.validatedCondition = cloneCondition(cond);
		if (this.validatedCondition == null) throw new OLATRuntimeException(
				"CondititionEditController called with a NULL condition",
				new IllegalArgumentException()
		);
		this.courseEditorEnv = env;	
		this.nodeIdentList = nodeIdentList;

		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		singleUserEventCenter.deregisterFor(this, groupConfigChangeEventOres);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {

		// 1) rule applies also for coach switch - one checkbox
		// has opposite meaning -> selected is saved as false, and vice versa
		if (applyRulesForCoach.getSelectedKeys().size() == 0) {
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
				if(nodeIdentList.size()==0) {
					assessmentSwitch.setEnabled(false);
				}
			}

			// 6) attribute switch
			if (shibbolethModule.isEnableShibbolethCourseEasyConfig()) {
				if (attributeSwitch.getSelectedKeys().size() == 1) {
					List<ExtendedCondition> le = attribteRowAdderSubform.getAttributeConditions();
		
					if (le.size() < 1) {
						// some error(s) occured, don't save anything
						validatedCondition.setAttributeConditions(null); 
					} else {
						// add the conditions
						validatedCondition.setAttributeConditions(le); 
					}
					validatedCondition.setAttributeConditionsConnectorIsAND(new Boolean(attributeBconnector.getSelectedKey().equals(BCON_VAL_AND)));
		
				} else {
					validatedCondition.setAttributeConditions(null);
					validatedCondition.setAttributeConditionsConnectorIsAND(null);
				}
			}
			
			// assessment mode
			validatedCondition.setAssessmentMode(assessmentMode.isAtLeastSelected(1));
		}

		// calculate expression from easy mode form
		String condString = validatedCondition.getConditionFromEasyModeConfiguration();
		validatedCondition.setConditionExpression(condString);
		validatedCondition.setExpertMode(false);

		/*
		 * condition is updated
		 */

		// Inform all listeners about the changed condition
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
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == chooseGroupsLink) {
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(groupChooseC);
			
			List<Long> groupKeys = getKeys(easyGroupList);
			groupChooseC = new GroupSelectionController(ureq, getWindowControl(), true,
					courseEditorEnv.getCourseGroupManager(), groupKeys);
			listenTo(groupChooseC);

			cmc = new CloseableModalController(
					getWindowControl(), "close", groupChooseC.getInitialComponent(),
					true, getTranslator().translate("popupchoosegroups"));
			listenTo(cmc);
			cmc.activate();
		} else if (source == createGroupsLink) {
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(groupCreateCtlr);
			
			OLATResource courseResource = courseEditorEnv.getCourseGroupManager().getCourseResource();
			RepositoryEntry courseRe = RepositoryManager.getInstance().lookupRepositoryEntry(courseResource, false);
			groupCreateCtlr = new NewBGController(ureq, getWindowControl(), courseRe, true, null);
			listenTo(groupCreateCtlr);
			cmc = new CloseableModalController(getWindowControl(), "close", groupCreateCtlr.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
		} else if (source == chooseAreasLink) {
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(areaChooseC);
			
			areaChooseC = new AreaSelectionController(ureq, getWindowControl(), true,
					courseEditorEnv.getCourseGroupManager(), getKeys(easyAreaList));
			listenTo(areaChooseC);
			
			cmc = new CloseableModalController(
					getWindowControl(), "close", areaChooseC.getInitialComponent(),
					true, getTranslator().translate("popupchooseareas"));
			listenTo(cmc);
			cmc.activate();
		} else if (source == createAreasLink) {
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(areaCreateCtlr);

			OLATResource courseResource = courseEditorEnv.getCourseGroupManager().getCourseResource();
			areaCreateCtlr = new NewAreaController(ureq, getWindowControl(), courseResource, true, null);
			listenTo(areaCreateCtlr);

			cmc = new CloseableModalController(getWindowControl(), "close",areaCreateCtlr.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
		} else if (source == fixGroupError) {
			// user wants to fix problem with fixing group error link e.g. create one
			// or more group at once.
			String[] csvGroupName = (String[]) fixGroupError.getUserObject();
			OLATResource courseResource = courseEditorEnv.getCourseGroupManager().getCourseResource();
			RepositoryEntry courseRe = RepositoryManager.getInstance().lookupRepositoryEntry(courseResource, false);
			removeAsListenerAndDispose(groupCreateCtlr);
			groupCreateCtlr = new NewBGController(ureq, getWindowControl(), courseRe, true, csvGroupName[0]);
			listenTo(groupCreateCtlr);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), "close", groupCreateCtlr.getInitialComponent());
			listenTo(cmc);
			cmc.activate();	
		} else if (source == fixAreaError) {
			/*
			 * user wants to fix problem with fixing area error link e.g. create one
			 * or more areas at once.
			 */
			String[] csvAreaName = (String[]) fixAreaError.getUserObject();
			OLATResource courseResource = courseEditorEnv.getCourseGroupManager().getCourseResource();
			removeAsListenerAndDispose(areaCreateCtlr);
			areaCreateCtlr = new NewAreaController(ureq, getWindowControl(), courseResource, true, csvAreaName[0]);
			listenTo(areaCreateCtlr);
			
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), "close", areaCreateCtlr.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, org.olat.core.gui.control.Event event) {
		
		if (source == groupChooseC) {
			
			if (Event.DONE_EVENT == event) {
				cmc.deactivate();
				List<Long> groupKeys = groupChooseC.getSelectedKeys();
				easyGroupList.setUserObject(groupKeys);
				easyGroupList.setValue(getGroupNames(groupKeys));
				validateGroupFields();
				easyGroupList.getRootForm().submit(ureq);
			} else if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			} else if (event == Event.CHANGED_EVENT) {
				//a group was created within from within the chooser
			}
			
		} else if (source == areaChooseC) {
				
			if (event == Event.DONE_EVENT) {
		
				cmc.deactivate();
				List<Long> areaKeys = areaChooseC.getSelectedKeys();
				easyAreaList.setUserObject(areaKeys);
				easyAreaList.setValue(this.getAreaNames(areaKeys));
				validateGroupFields();
				easyAreaList.getRootForm().submit(ureq);
				
			} else if (event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			} else if (event == Event.CHANGED_EVENT) {
				//an area was created within from within the chooser
			}
			
		} else if (source == groupCreateCtlr) {
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				List<Long> c = new ArrayList<Long>();
				c.addAll(getKeys(easyGroupList));
				if (fixGroupError != null && fixGroupError.getUserObject() != null) {
					c.removeAll(Arrays.asList((String[])fixGroupError.getUserObject()));
				}
				c.addAll(groupCreateCtlr.getCreatedGroupKeys());
				easyGroupList.setValue(getGroupNames(c));
				easyGroupList.setUserObject(c);
				easyGroupList.getRootForm().submit(ureq);
				validateGroupFields();
	
				if (!groupCreateCtlr.getCreatedGroupKeys().isEmpty()) {
					singleUserEventCenter.fireEventToListenersOf(new MultiUserEvent("changed"), groupConfigChangeEventOres);
				}
			} 
		} else if (source == areaCreateCtlr) {
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				List<Long> c = new ArrayList<Long>();
				c.addAll(getKeys(easyAreaList));
				if (fixAreaError!= null && fixAreaError.getUserObject() != null) {
					c.removeAll(Arrays.asList((String[])fixAreaError.getUserObject()));
				}
				c.addAll(areaCreateCtlr.getCreatedAreaKeys());
				
				easyAreaList.setValue(getAreaNames(c));
				easyAreaList.setUserObject(c);
				easyAreaList.getRootForm().submit(ureq);
				validateGroupFields();
				
				if (!areaCreateCtlr.getCreatedAreaKeys().isEmpty())  {
					singleUserEventCenter.fireEventToListenersOf(new MultiUserEvent("changed"), groupConfigChangeEventOres);
				}
			}
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean retVal = true;
		// (1)
		// dateswitch is enabled and the datefields are valid
		// check complex rules involving checks over multiple elements
		Date fromDateVal = fromDate.getDate();
		Date toDateVal = toDate.getDate();
		if (dateSwitch.getSelectedKeys().size() == 1 && !fromDate.hasError() && !toDate.hasError()) {
			// one must be set
			if (fromDate.isEmpty() && toDate.isEmpty()) {
				// error concern both input fields -> set it as switch error				
				dateSubContainer.setErrorKey("form.easy.error.date", null);
				retVal = false;
			} else {
				// remove general error				
				dateSubContainer.clearError();
			}
			// check valid dates

			// if both are set, check from < to
			if (fromDateVal != null && toDateVal != null) {
				/*
				 * bugfix http://bugs.olat.org/jira/browse/OLAT-813 valid dates and not
				 * empty, in easy mode we assume that Start and End date should
				 * implement the meaning of
				 * ----false---|S|-----|now|-TRUE---------|E|---false--->t .............
				 * Thus we check for Startdate < Enddate, error otherwise
				 */
				if (fromDateVal.after(toDateVal)) {					
					dateSubContainer.setErrorKey("form.easy.error.bdateafteredate", null);
					retVal = false;
				} else {
					// remove general error
					dateSwitch.clearError();
				}
			} else {
				if (fromDateVal == null && !fromDate.isEmpty()) {
					//not a correct begin date
					fromDate.setErrorKey("form.easy.error.bdate", null);
					retVal = false;
				}
				if (toDateVal == null && !toDate.isEmpty()) {
					toDate.setErrorKey("form.easy.error.edate", null);
					retVal = false;
				}
			}
		}

		// (2)
		// groups switch is enabled
		// check if either group or area is defined
		retVal = validateGroupFields() && retVal;

		if (assessmentSwitch.getSelectedKeys().size() == 1) {
			// foop d fbg,dfbg,f ,gfbirst check two error cases of a selection
			// no node selected or a deleted node selected
			if (nodePassed.getSelectedKey().equals(NO_NODE_SELECTED_IDENTIFYER)) {				
				nodePassed.setErrorKey("form.easy.error.nodePassed", null);
				retVal = false;
			} else if (nodePassed.getSelectedKey().equals(DELETED_NODE_IDENTIFYER)) {
				nodePassed.setErrorKey("form.easy.error.nodeDeleted", null);
				retVal = false;
			} else {
				//clear nodepassed error
				nodePassed.clearError();
				//retVal stays
			}
		}
		
		if (shibbolethModule.isEnableShibbolethCourseEasyConfig()) {
			retVal=validateAttibuteFields()&&retVal;	
		}
		//
		return retVal;
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

				if (missingGroups.size() > 0) {
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
					fixGroupError.setCustomEnabledLinkCSS("btn btn-default");
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
			areaChooseSubContainer.clearError();
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
				
				if (missingAreas.size() > 0) {
					retVal = false;
					String labelKey = missingAreas.size() == 1 ? "error.notfound.name" : "error.notfound.names";
					String csvMissAreas = toString(missingAreas);
					String[] params = new String[] { "-", csvMissAreas };

					// create error with link to fix it
					String vc_errorPage = velocity_root + "/erroritem.html";
					FormLayoutContainer errorAreaItemLayout = FormLayoutContainer.createCustomFormLayout(
							"errorareaitem", getTranslator(), vc_errorPage);

					areaChooseSubContainer.setErrorComponent(errorAreaItemLayout, this.flc);
					// FXINGIN LINK ONLY IF DEFAULT CONTEXT EXISTS
					fixAreaError = new FormLinkImpl("error.fix", "create");// erstellen
					// link
					fixAreaError.setCustomEnabledLinkCSS("btn btn-default");
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

			boolean easyGroupOK = (!isEmpty(easyGroupList) && activeGroupSelection != null && !activeGroupSelection.isEmpty());
			boolean easyAreaOK = (!isEmpty(easyAreaList) && activeAreaSelection != null && !activeAreaSelection.isEmpty());
			if (easyGroupOK || easyAreaOK) {
				// clear general error
				groupSubContainer.clearError();
			} else {
				// error concerns both fields -> set it as switch error
				groupSubContainer.setErrorKey("form.easy.error.group", null);
				retVal = false;
			}

		}
		return retVal;
	}
	/**
	 * @param retVal
	 * @return
	 */
	private boolean validateAttibuteFields() {
		boolean retVal = true;
		if (attributeSwitch.getSelectedKeys().size() == 1&&attribteRowAdderSubform.hasError()) {
			attributeSwitch.setErrorKey("form.easy.error.attribute", null);
			retVal = false;
			return retVal;
		}
		attributeSwitch.clearError();
		return retVal;
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
	
		addCoachExclusive(formLayout);
		addFromToDateChooser(formLayout);
		addEasyGroupAreaChoosers(formLayout);
		addAssessmentSwitch(formLayout);
		//
		if(shibbolethModule.isEnableShibbolethCourseEasyConfig()) {
			addAttributeSwitch(formLayout, ureq);
		}
		flc.contextPut("shibbolethEnabled", new Boolean(shibbolethModule.isEnableShibbolethCourseEasyConfig()));
		addAssessmentMode(formLayout);
		addApplyRulesForTutorsToo(formLayout);
		
		
		// add rules
		addRules(formLayout);
		
		// the submit button
		uifactory.addFormSubmitButton("subm", "submit", formLayout);
		validateGroupFields();
	}

	private void addAttributeSwitch(FormItemContainer formLayout, UserRequest ureq) {
		/*
		 * yes / no chooser defines if learner do not see the building block at all
		 */
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
		if (validatedCondition.getAttributeConditions() != null && validatedCondition.getAttributeConditions().size() > 0) {
			attribteRowAdderSubform.setAttributeConditions(validatedCondition.getAttributeConditions());
		}
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
	
	/**
	 * Show applyRulesForCoach depending on isDateGroupAssessmentOAttributeSwitchOn state.
	 */
	private void showOrHideApplyRulesForCoach() {
		if(isDateGroupAssessmentOrAttributeSwitchOnOrAssessmentModeOn()) {
			applyRulesForCoach.setVisible(true);
		} else {
			applyRulesForCoach.setVisible(false);
		}
	}

	/**
	 * you may find here now the complexest rule set ever use in OLAT<br>
	 * This form has a 5 switches<br>
	 * <ul>
	 * <li>[] 1 Learners only</li>
	 * <li>[] 2 Date dependent</li>
	 * <li>[] 3 Group dependent</li>
	 * <li>[] 4 Assess dependent</li>
	 * <li>[] 5 Apply rules also to coaches</li>
	 * </ul>
	 * enable [1] this greys out all others<br>
	 * if one of [2] [3] or [4] is selected -> [5] becomes selectable<br>
	 * selecting [2] [3] or [4] opens their respective subconfiguration<br>
	 * "[2] date dependent" shows an end and startdate where at least one must be  
	 * selected and the start date must be before the enddate.<br>
	 * "[3] group dependent" shows a group or area input field. which takes group
	 * or area names comma separated. the form evaluates if the areas or groups 
	 * exists, and if not, a quick fix is provided to create the missing groups/areas.
	 * furthermore there is a "choose" button to choose groups/areas. This choose
	 * button is named "create" if there are no groups or areas to choose from. If
	 * create is clicked a create group/area workflow is started directly. If some
	 * comma separated values are in the input field, it allows to create them at
	 * once. At least an area or groupname must be specified, and all the specified
	 * names must exist.<br>
	 * "[4] assessment " allows to choose a node and to define a cut value or if 
	 * it should be checked for passed.<br>
	 * To accomplish all the hiding, disabling, enabling, resetting to initial values
	 * the following rules are added. this may look confusing, and it is confusing.
	 * It took quite some days and testing until to get it right.
	 * @param formLayout
	 */
	private void addRules(FormItemContainer formLayout) {
		
		// disable date choosers if date switch is set to no
		// enable it otherwise.
		final Set<FormItem> dependenciesDateSwitch = new HashSet<FormItem>();
		dependenciesDateSwitch.add(toDate);
		dependenciesDateSwitch.add(fromDate);
		dependenciesDateSwitch.add(dateSubContainer);
		
		final Set<FormItem> dependenciesAttributeSwitch = new HashSet<FormItem>();
		// only add when initialized. is null when shibboleth module is not enabled
		if (shibbolethModule.isEnableShibbolethCourseEasyConfig()) {
			dependenciesAttributeSwitch.add(attributeBconnector);
		}
		
		// show elements dependent on other values set.
		FormItemDependencyRule hideClearDateSwitchDeps = RulesFactory.createCustomRule(dateSwitch, null, dependenciesDateSwitch, formLayout);
		hideClearDateSwitchDeps.setDependencyRuleApplayable(new DependencyRuleApplayable() {
			public void apply(FormItem triggerElement, Object triggerVal, Set<FormItem> targets) {
				toDate.setDate(null);
				toDate.setVisible(false);
				fromDate.setDate(null);
				fromDate.setVisible(false);
				toDate.clearError();
				fromDate.clearError();
				dateSwitch.clearError();
				dateSubContainer.setVisible(false);				
				
				fromDate.setFocus(false);
				/*
				 * special rules for apply rules for coach and for assessment dependent
				 */
				//assessment switch only enabled if nodes to be selected
				boolean coachExclIsOn = coachExclusive.getSelectedKeys().size() == 1;
				assessmentSwitch.setEnabled(!coachExclIsOn && (nodeIdentList.size() > 0  || isSelectedNodeDeleted()));
					
				showOrHideApplyRulesForCoach();				
			}
		});
		RulesFactory.createShowRule(dateSwitch, "ison", dependenciesDateSwitch, formLayout);
		FormItemDependencyRule toggleApplyRule = RulesFactory.createCustomRule(dateSwitch, "ison", dependenciesDateSwitch, formLayout);
		toggleApplyRule.setDependencyRuleApplayable(new DependencyRuleApplayable() {
			public void apply(FormItem triggerElement, Object triggerVal, Set<FormItem> targets) {

				fromDate.setFocus(true);
				
				//assessment switch only enabled if nodes to be selected
				assessmentSwitch.setEnabled((nodeIdentList.size() > 0  || isSelectedNodeDeleted()));
				
				showOrHideApplyRulesForCoach();				
			}
		});
		
		if (shibbolethModule.isEnableShibbolethCourseEasyConfig()) {
			FormItemDependencyRule hideClearAttibuteSwitchDeps = RulesFactory.createCustomRule(attributeSwitch, null, dependenciesAttributeSwitch, formLayout);
			
			hideClearAttibuteSwitchDeps.setDependencyRuleApplayable(new DependencyRuleApplayable() {
				public void apply(FormItem triggerElement, Object triggerVal, Set<FormItem> targets) {
					attributeSwitch.clearError();
					attributeBconnector.select(BCON_VAL_AND, true);
					attributeBconnector.setVisible(false);
					if(attribteRowAdderSubform!=null){
						attribteRowAdderSubform.cleanUp();
					}
					showOrHideApplyRulesForCoach();					
				}
			});
			
			RulesFactory.createShowRule(attributeSwitch, "ison", dependenciesAttributeSwitch, formLayout);
			
			FormItemDependencyRule attributeSwitchtoggleApplyRule = RulesFactory.createCustomRule(attributeSwitch, "ison", dependenciesAttributeSwitch, formLayout);
			attributeSwitchtoggleApplyRule.setDependencyRuleApplayable(new DependencyRuleApplayable() {
				public void apply(FormItem triggerElement, Object triggerVal, Set<FormItem> targets) {
					attributeBconnector.setVisible(true);
					if(attribteRowAdderSubform!=null){
						attribteRowAdderSubform.init();
					}
					showOrHideApplyRulesForCoach();					
				}
			});
		}
	
		//
		// enable textfields and subworkflow-start-links if groups is yes
		// disable it otherwise
		final Set<FormItem> dependenciesGroupSwitch = new HashSet<FormItem>();
		dependenciesGroupSwitch.add(groupSubContainer);
		FormItemDependencyRule hideClearGroupSwitchDeps = RulesFactory.createCustomRule(groupSwitch, null, dependenciesGroupSwitch, formLayout);
		hideClearGroupSwitchDeps.setDependencyRuleApplayable(new DependencyRuleApplayable() {
			public void apply(FormItem triggerElement, Object triggerVal, Set<FormItem> targets) {
				
				easyAreaList.clearError();
				easyGroupList.clearError();
				groupSwitch.clearError();
				groupSubContainer.setVisible(false);			
				
				if (shibbolethModule.isEnableShibbolethCourseEasyConfig()) {
					attributeSwitch.clearError();
				}
				easyGroupList.setFocus(false);
				
				//assessment switch only enabled if nodes to be selected
				boolean coachExclIsOn = coachExclusive.getSelectedKeys().size() == 1;
				assessmentSwitch.setEnabled(!coachExclIsOn && (nodeIdentList.size() > 0  || isSelectedNodeDeleted()));
					
				showOrHideApplyRulesForCoach();				
			}
		});
		RulesFactory.createShowRule(groupSwitch, "ison", dependenciesGroupSwitch, formLayout);
		toggleApplyRule = RulesFactory.createCustomRule(groupSwitch, "ison", dependenciesGroupSwitch, formLayout);
		toggleApplyRule.setDependencyRuleApplayable(new DependencyRuleApplayable() {
			public void apply(FormItem triggerElement, Object triggerVal, Set<FormItem> targets) {

				easyGroupList.setFocus(true);
				
				//assessment switch only enabled if nodes to be selected
				assessmentSwitch.setEnabled((nodeIdentList.size() > 0  || isSelectedNodeDeleted()));
						
				showOrHideApplyRulesForCoach();				
			}
		});
		
		//	
		// dependencies of assessment switch
		final Set<FormItem> assessDeps = new HashSet<FormItem>();
		assessDeps.add(assessmentTypeSwitch);
		assessDeps.add(nodePassed);
		assessDeps.add(cutValue);
		assessDeps.add(assessSubContainer);

		// show elements dependent on other values set.
		FormItemDependencyRule showAssessmentSwitchDeps = RulesFactory.createCustomRule(assessmentSwitch, "ison", assessDeps, formLayout);
		showAssessmentSwitchDeps.setDependencyRuleApplayable(new DependencyRuleApplayable() {
			public void apply(FormItem triggerElement, Object triggerVal, Set<FormItem> targets) {
				boolean cutValueVisibility = assessmentTypeSwitch.getSelectedKey().equals(NODEPASSED_VAL_SCORE);
				assessSubContainer.setVisible(true);				
				assessmentTypeSwitch.setVisible(true);
				nodePassed.setVisible(true);
				cutValue.setVisible(cutValueVisibility);
				assessmentSwitch.clearError();
				cutValue.clearError();
				nodePassed.clearError();

				showOrHideApplyRulesForCoach();				
			}
		});

		// hide elements and reset values.
		FormItemDependencyRule hideResetAssessmentSwitchDeps = RulesFactory.createCustomRule(assessmentSwitch, null, assessDeps, formLayout);
		hideResetAssessmentSwitchDeps.setDependencyRuleApplayable(new DependencyRuleApplayable() {
			public void apply(FormItem triggerElement, Object triggerVal, Set<FormItem> targets) {
				assessSubContainer.setVisible(false);				
				assessmentTypeSwitch.select(NODEPASSED_VAL_PASSED, true);
				assessmentTypeSwitch.setVisible(false);
				nodePassed.select(NO_NODE_SELECTED_IDENTIFYER, true);
				nodePassed.setVisible(false);
				cutValue.setIntValue(0);
				cutValue.setVisible(false);

				showOrHideApplyRulesForCoach();				
			}
		});

		final Set<FormItem> assessTypeDeps = new HashSet<FormItem>();
		assessTypeDeps.add(cutValue);
		RulesFactory.createHideRule(assessmentTypeSwitch, NODEPASSED_VAL_PASSED, assessTypeDeps, assessSubContainer);
		RulesFactory.createShowRule(assessmentTypeSwitch, NODEPASSED_VAL_SCORE, assessTypeDeps, assessSubContainer);

		// 
		//
		final Set<FormItem> dependenciesCoachExclusiveReadonly = new HashSet<FormItem>();
		dependenciesCoachExclusiveReadonly.addAll(dependenciesDateSwitch);
		dependenciesCoachExclusiveReadonly.addAll(dependenciesGroupSwitch);
		dependenciesCoachExclusiveReadonly.addAll(assessDeps);
		dependenciesCoachExclusiveReadonly.addAll(dependenciesAttributeSwitch);

		// coach exclusive switch rules
		// -> custom rule implementation because it is not a simple hide / show rule
		// while disabling reset the elements
		FormItemDependencyRule disableAndResetOthers = RulesFactory.createCustomRule(coachExclusive, "ison",
				dependenciesCoachExclusiveReadonly, formLayout);
		disableAndResetOthers.setDependencyRuleApplayable(new DependencyRuleApplayable() {
			public void apply(FormItem triggerElement, Object triggerVal, Set<FormItem> targets) {
				// disable and remove checkbox selection
				// uncheck and disable checkboxes
				dateSwitch.select("ison", false);
				groupSwitch.select("ison", false);
				assessmentSwitch.select("ison", false);
				dateSwitch.setEnabled(false);
				toDate.setDate(null);
				fromDate.setDate(null);
				groupSwitch.setEnabled(false);
				easyAreaList.setValue("");
				easyAreaList.setUserObject(new ArrayList<Long>());
				easyGroupList.setValue("");
				easyGroupList.setUserObject(new ArrayList<Long>());
				assessmentSwitch.setEnabled(false);
				assessmentMode.select("ison", false);
				assessmentMode.setEnabled(false);
				
				// disable the shibboleth attributes switch and reset the row subform
				if (attributeSwitch != null) {
					attributeSwitch.select("ison", false);
					attributeSwitch.setEnabled(false);
					attribteRowAdderSubform.cleanUp();
					attributeSwitch.clearError();
				}
				showOrHideApplyRulesForCoach();
				
				// hide (e.g. remove) general erros
				dateSwitch.clearError();
				groupSwitch.clearError();
				assessmentSwitch.clearError();

				
				// all dependent elements become invisible
				for (Iterator<FormItem> iter = dependenciesCoachExclusiveReadonly.iterator(); iter.hasNext();) {
					FormItem element = iter.next();
					element.setVisible(false);
				}
			}
		});

		// two rules to bring them back visible and also checkable
		// dependencies of assessment switch
		final Set<FormItem> switchesOnly = new HashSet<FormItem>();
		switchesOnly.add(dateSwitch);
		switchesOnly.add(groupSwitch);
		switchesOnly.add(assessmentSwitch);
		switchesOnly.add(applyRulesForCoach);
		if (shibbolethModule.isEnableShibbolethCourseEasyConfig()) {
			switchesOnly.add(attributeSwitch);
		}

		FormItemDependencyRule enableOthers = RulesFactory.createCustomRule(coachExclusive, null, switchesOnly, formLayout);
		enableOthers.setDependencyRuleApplayable(new DependencyRuleApplayable() {
			private boolean firedDuringInit = true;
			public void apply(FormItem triggerElement, Object triggerVal, Set<FormItem> targets) {
				dateSwitch.setEnabled(true);
				groupSwitch.setEnabled(true);
				//assessment switch only enabled if nodes to be selected
				assessmentSwitch.setEnabled((nodeIdentList.size() > 0  || isSelectedNodeDeleted()));
				assessmentMode.setEnabled(true);
								
				//default is a checked disabled apply rules for coach
				if (shibbolethModule.isEnableShibbolethCourseEasyConfig()) {
					attributeSwitch.setEnabled(true);
				}
				if(!firedDuringInit){
					showOrHideApplyRulesForCoach();					
				}
				firedDuringInit = false;
			}
		});
		
		//
		// dependencies of assessment mode
		final Set<FormItem> assessModeDeps = new HashSet<FormItem>();

		// show elements dependent on other values set.
		FormItemDependencyRule showAssessmentModeDeps = RulesFactory.createCustomRule(assessmentMode, "ison", assessModeDeps, formLayout);
		showAssessmentModeDeps.setDependencyRuleApplayable(new DependencyRuleApplayable() {
			public void apply(FormItem triggerElement, Object triggerVal, Set<FormItem> targets) {
				showOrHideApplyRulesForCoach();				
			}
		});

		// hide elements and reset values.
		FormItemDependencyRule hideResetAssessmentModeDeps = RulesFactory.createCustomRule(assessmentMode, null, assessModeDeps, formLayout);
		hideResetAssessmentModeDeps.setDependencyRuleApplayable(new DependencyRuleApplayable() {
			public void apply(FormItem triggerElement, Object triggerVal, Set<FormItem> targets) {
				showOrHideApplyRulesForCoach();				
			}
		});
	}

	/**
	 * Methods to add elements
	 * @param formLayout
	 * @param listener
	 */
	private void addCoachExclusive(FormItemContainer formLayout) {
		/*
		 * yes / no chooser defines if learner do not see the building block at all
		 */
		coachExclusive = uifactory.addCheckboxesHorizontal("coachExclusive", null, formLayout, new String[] { "ison" }, new String[] { translate("form.easy.coachExclusive") });
		coachExclusive.setElementCssClass("o_sel_condition_coach_exclusive");
		boolean coachesAndAdminsInitValue = validatedCondition.isEasyModeCoachesAndAdmins();
		coachExclusive.select("ison", coachesAndAdminsInitValue);
		
		// register for on click event to hide/disable other elements
		coachExclusive.addActionListener(FormEvent.ONCLICK);
		// rules are later added
	}

	private void addAssessmentMode(FormItemContainer formLayout) {
		assessmentMode = uifactory.addCheckboxesHorizontal("assessmentMode", null, formLayout, new String[] { "ison" }, new String[] { translate("form.easy.assessmentMode") });
		assessmentMode.select("ison", validatedCondition.isAssessmentMode());
		assessmentMode.addActionListener(FormEvent.ONCLICK);
		assessmentMode.setVisible(assessmentModule.isAssessmentModeEnabled());
	}

	/**
	 * @param formLayout
	 * @param listener
	 */
	private void addApplyRulesForTutorsToo(FormItemContainer formLayout) {
		
		/*
		 * yes / no chooser defines if learner do not see the building block at all
		 */
		applyRulesForCoach = uifactory.addCheckboxesHorizontal("applyRulesForCoach", null, formLayout, new String[] { "ison" }, new String[] { translate("form.easy.applyRulesForCoach") });
		if(isDateGroupAssessmentOrAttributeSwitchOnOrAssessmentModeOn()) {
			applyRulesForCoach.setVisible(true);
		} else {
			applyRulesForCoach.setVisible(false);
		}
		//note that in the condition this rule is saved with the opposite meaning:
		// true when
		// coach and admins always have access, false when rule should apply also to
		// them
		boolean alwaysAllowCoachesAndAdminsInitValue = validatedCondition.isEasyModeAlwaysAllowCoachesAndAdmins();
		if (alwaysAllowCoachesAndAdminsInitValue) {
			applyRulesForCoach.select("ison", false);
		} else {
			applyRulesForCoach.select("ison", true);
		}
	}

	/**
	 * @param formLayout
	 * @param listener
	 */
	private void addFromToDateChooser(FormItemContainer formLayout) {
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
		dateSubContainer.add(fromDate);
		
		toDate = new JSDateChooser("toDate", getLocale());
		toDate.setLabel("form.easy.edate", null);
		toDate.setDate(ConditionDateFormatter.parse(validatedCondition.getEasyModeEndDate()));
		toDate.setExampleKey("form.easy.example.edate", null);
		toDate.setDateChooserTimeEnabled(true);
		toDate.setDisplaySize(toDate.getExampleDateString().length());
		toDate.setDefaultTimeAtEndOfDay(true);
		dateSubContainer.add(toDate);

		dateSwitch = uifactory.addCheckboxesHorizontal("dateSwitch", null, formLayout, new String[] { "ison" }, new String[] { translate("form.easy.dateSwitch") });
		if (fromDate.getDate() != null || toDate.getDate() != null) {
			dateSwitch.select("ison", true);
		} else {
			dateSwitch.select("ison", false);
		}
		dateSwitch.addActionListener(FormEvent.ONCLICK);
	}

	/**
	 * @param formLayout
	 * @param listener
	 */
	private void addEasyGroupAreaChoosers(FormItemContainer formLayout) {

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
		} else {
			groupSwitch.select("ison", false);
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

	private void addAssessmentSwitch(FormItemContainer formLayout) {

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
		List<String> keysList = new ArrayList<String>();
		List<String> valuesList = new ArrayList<String>();
		if (nodeIdentList.size() > 0) {
			// there are nodes to be selected
			keysList.add(NO_NODE_SELECTED_IDENTIFYER);
			valuesList.add("- " + translator.translate("form.easy.nodePassed.select") + " -");
			for (int i = 0; i < nodeIdentList.size(); i++) {
				CourseNode courseNode = nodeIdentList.get(i);
				keysList.add(courseNode.getIdent());
				valuesList.add(courseNode.getShortName() + " (Id:" + courseNode.getIdent() + ")");
				if (courseNode.getIdent().equals(nodePassedInitVal)) selectedNodeIsInList = true;
			}
		} else {
			// no nodes to be selected
			keysList.add(NO_NODE_SELECTED_IDENTIFYER);
			valuesList.add("- " + translator.translate("form.easy.nodePassed.noNodes"));
			//disable switch
			assessmentSwitch.setEnabled(false);
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
			assessmentSwitch.select("ison", false);
			assessmentTypeSwitch.setVisible(false);
			assessmentTypeSwitch.select(NODEPASSED_VAL_PASSED, true);
		}
		
		cutValue = uifactory.addIntegerElement("cutval", "form.easy.cutValue", cutInitValue, assessSubContainer);
		cutValue.setDisplaySize(3);
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
			
	/**
	 * It checks if the selected EasyModeNodePassedId is not null, while the assessable node list is empty, 
	 * and it enables the assessmentSwitch.
	 * 
	 * @return
	 */
	private boolean isSelectedNodeDeleted() {
		String nodePassedInitVal = validatedCondition.getEasyModeNodePassedId();
		if(nodePassedInitVal!=null && nodeIdentList.size()==0) {			
			assessmentSwitch.setEnabled(true);
						
			return true;
		}
		return false;
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
