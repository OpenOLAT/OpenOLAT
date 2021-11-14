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
package org.olat.course.nodes;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.duedate.ui.DueDateConfigFormatter;
import org.olat.course.highscore.ui.HighScoreEditController;
import org.olat.course.noderight.NodeRight;
import org.olat.course.noderight.NodeRightGrant;
import org.olat.course.noderight.NodeRightType;
import org.olat.course.noderight.manager.NodeRightServiceImpl;
import org.olat.course.noderight.ui.NodeRightsController;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;

/**
 * Initial date: 23.07.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CourseNodeDatesListController extends FormBasicController {

	private final DueDateConfigFormatter dueDateConfigFormatter;
	
	private final CopyCourseContext context;
	private CourseNode courseNode;
	
	public CourseNodeDatesListController(UserRequest ureq, WindowControl wControl, CopyCourseContext context) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(CourseNode.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(NodeRightsController.class, getLocale(), getTranslator()));
		this.context = context;
		this.dueDateConfigFormatter = DueDateConfigFormatter.create(getLocale());
	}	
	
	public void updateCourseNode(CourseNode courseNode, UserRequest ureq) {
		this.courseNode = courseNode;
		this.flc.removeAll();
		
		initForm(ureq);
	}
	
	public void updateDates(UserRequest ureq) {
		flc.removeAll();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// If there are no dates, stop here
		if (courseNode == null || !courseNode.hasDates()) {
			return;
		}
		
		long dateDifference = context.getDateDifference(courseNode.getIdent());
		
		// Load course node dependant dates
		if (courseNode.getNodeSpecificDatesWithLabel().stream().map(Entry::getValue).anyMatch(DueDateConfig::isDueDate)) {
			FormLayoutContainer courseNodeDatesLayout = FormLayoutContainer.createDefaultFormLayout("courseNodeDatesLayout", getTranslator());
			courseNodeDatesLayout.setRootForm(mainForm);
			courseNodeDatesLayout.setFormTitle(translate("course.node.dates"));
			formLayout.add(courseNodeDatesLayout);
			
			for (Entry<String, DueDateConfig> innerDate : courseNode.getNodeSpecificDatesWithLabel()) {
				DueDateConfig dueDateConfig = innerDate.getValue();
				
				if (DueDateConfig.isRelative(dueDateConfig)) {
					uifactory.addStaticTextElement(innerDate.getKey(), dueDateConfigFormatter.formatRelativDateConfig(dueDateConfig), courseNodeDatesLayout);
				} else if(DueDateConfig.isAbsolute(dueDateConfig)) {
					Date movedDate = new Date(dueDateConfig.getAbsoluteDate().getTime() + dateDifference);
					DateChooser dateChooser = uifactory.addDateChooser(innerDate.getKey(), movedDate, courseNodeDatesLayout);
					dateChooser.setDateChooserTimeEnabled(true);
					dateChooser.setEnabled(false);
				}
			}
		}
		
		// Load course node config
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		
		// Load potential highscore data
		DueDateConfig startDateConfig = courseNode.getDueDateConfig(HighScoreEditController.CONFIG_KEY_DATESTART);
		if (DueDateConfig.isDueDate(startDateConfig)) {
			FormLayoutContainer highScoreDatesLayout = FormLayoutContainer.createDefaultFormLayout("highScoreDatesLayout", getTranslator());
			highScoreDatesLayout.setRootForm(mainForm);
			highScoreDatesLayout.setFormTitle(translate("course.node.highscore.dates"));
			formLayout.add(highScoreDatesLayout);
			if (DueDateConfig.isRelative(startDateConfig)) {
				uifactory.addStaticTextElement("highscore.date.start", dueDateConfigFormatter.formatRelativDateConfig(startDateConfig), highScoreDatesLayout);
			} else if (DueDateConfig.isAbsolute(startDateConfig)) {
				Date highScorePublicationDate = new Date(startDateConfig.getAbsoluteDate().getTime() + dateDifference);
				DateChooser highScoreChooser = uifactory.addDateChooser("highscore.date.start", highScorePublicationDate, highScoreDatesLayout);
				highScoreChooser.setDateChooserTimeEnabled(true);
				highScoreChooser.setEnabled(false);
			}
		}
				
		// Load potential user rights
		// Move potential user right dates
		List<NodeRightType> nodeRightTypes = courseNode.getNodeRightTypes();
		Map<String, Object> potentialNodeRights = config.getConfigEntries(NodeRightServiceImpl.KEY_PREFIX);
		
		// If there are no right types or rights, finish here
		if (nodeRightTypes.isEmpty() || potentialNodeRights.isEmpty()) {
			return;
		}
		
		// Create map for easier handling
		Map<String, NodeRightType> nodeRightTypesMap = nodeRightTypes.stream().collect(Collectors.toMap(NodeRightType::getIdentifier, Function.identity()));
		Map<NodeRightType, List<NodeRightGrant>> nodeRightGrants = new HashMap<>();
		
		for (Map.Entry<String, Object> entry : potentialNodeRights.entrySet()) {
			if (!(entry.getValue() instanceof NodeRight)) {
				continue;
			}
			
			NodeRight nodeRight = (NodeRight) entry.getValue();
			
			
			if (nodeRight.getGrants() != null) {
				for (NodeRightGrant grant : nodeRight.getGrants()) {
					// Remove any rights associated with an identity or group, they won't be copied
					if (grant.getBusinessGroupRef() != null || grant.getIdentityRef() != null) {
						continue;
					}
					
					// If the right does not include any date, don't list it
					boolean hasDate = grant.getStart() != null || grant.getEnd() != null;
					if (!hasDate) {
						continue;
					}
					
					// Put the grant into the map
					NodeRightType type = nodeRightTypesMap.get(nodeRight.getTypeIdentifier());
					
					if (type != null) {
						List<NodeRightGrant> grants = nodeRightGrants.get(type);
						
						if (grants == null) {
							grants = new ArrayList<>();
						}
						
						grants.add(grant);
						
 						nodeRightGrants.put(type, grants);
					}
				}
			}
		}
		
		// If no user right grants where found, stop here
		if (nodeRightGrants.isEmpty()) {
			return;
		}
		
		FormLayoutContainer rightDatesLayout = FormLayoutContainer.createDefaultFormLayout("rightDatesLayout", getTranslator());
		rightDatesLayout.setRootForm(mainForm);
		rightDatesLayout.setFormTitle(translate("course.node.user.right.dates"));
		formLayout.add(rightDatesLayout);
		
		int count = 0;
		
		for (NodeRightType type : nodeRightGrants.keySet()) {
			for (NodeRightGrant grant : nodeRightGrants.get(type)) {
				String rightRole = grant.getRole() != null ? translate("role." + grant.getRole().name().toLowerCase()) : "";
				String rightTitle = type.getTranslatorBaseClass() != null ? Util.createPackageTranslator(type.getTranslatorBaseClass(), getLocale()).translate(type.getI18nKey()) : type.getIdentifier();
				
				String label = rightTitle + (StringHelper.containsNonWhitespace(rightRole) ? " - " + rightRole : "");
				
				DateChooser userRightDate = uifactory.addDateChooser("user_right_" + count++, null, rightDatesLayout);
				userRightDate.setDateChooserTimeEnabled(true);
				userRightDate.setSecondDate(true);
				userRightDate.setEnabled(false);
				userRightDate.setLabel(label, null, false);
				userRightDate.setSeparator("date.separator");
				
				Date start = grant.getStart();
				if (start != null) {
					start = new Date(start.getTime() + dateDifference);
				}
				
				Date end = grant.getEnd();
				if (end != null) {
					end = new Date(end.getTime() + dateDifference);
				}
				
				userRightDate.setDate(start);
				userRightDate.setSecondDate(end);
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// Nothing to to here
	}
	
}
