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
package org.olat.course;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.nodes.INode;
import org.olat.course.condition.Condition;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.nodes.AbstractFeedCourseNode;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.COCourseNode;
import org.olat.course.nodes.CalCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.DialogCourseNode;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.nodes.FOCourseNode;
import org.olat.course.nodes.GenericCourseNode;
import org.olat.course.nodes.InfoCourseNode;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.course.nodes.ProjectBrokerCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.TACourseNode;
import org.olat.course.nodes.WikiCourseNode;
import org.olat.course.nodes.co.COEditController;
import org.olat.course.run.scoring.ScoreCalculator;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.resource.OLATResource;

/**
 * 
 * Description:<br>
 * Upgrade-Code for course
 * will check for up-to-date editorTreeModel or runStructure first.
 * 
 * <P>
 * Initial Date:  17.07.2009 <br>
 * @author Roman Haag, www.frentix.com, roman.haag@frentix.com,
 */
public class CourseUpgrade {
	
	private static final OLog log = Tracing.createLoggerFor(CourseUpgrade.class);
	
	private BGAreaManager areaManager;
	private BusinessGroupService businessGroupService;

	public CourseUpgrade(){
		//
	}
	
	/**
	 * [used by Spring]
	 * @param areaManager
	 */
	public void setAreaManager(BGAreaManager areaManager) {
		this.areaManager = areaManager;
	}

	/**
	 * [used by Spring]
	 * @param businessGroupService
	 */
	public void setBusinessGroupService(BusinessGroupService businessGroupService) {
		this.businessGroupService = businessGroupService;
	}
	
	/**
	 * Upgrade the version of the course to 3
	 * @param courseResource
	 * @param delegateOpeningEditSession
	 */
	public void processCourse(OLATResource courseResource, boolean delegateOpeningEditSession) {
		ICourse opendedCourse = null;
		try {
			List<BusinessGroup> groups = businessGroupService.findBusinessGroups(null, courseResource, 0, -1);
			List<BGArea> areas = areaManager.findBGAreasInContext(courseResource);

			ICourse course = CourseFactory.loadCourse(courseResource);
			ProcessingOccured processingFlag = new ProcessingOccured();
			log.info("Start: " + course.getCourseTitle());
			Structure structure = course.getRunStructure();
			if(structure.getVersion() < 3) {
				processCourseRec(processingFlag, structure.getRootNode(), groups, areas);
				structure.setVersion(3);
				processingFlag.processed();
			}
			CourseEditorTreeModel editorTree = course.getEditorTreeModel();
			if(editorTree.getVersion() < 3) {
				processEditorCourseRec(processingFlag, editorTree.getRootNode(), groups, areas);
				editorTree.setVersion(3);
				processingFlag.processed();
			}

			if(processingFlag.isProcessed()) {
				if(delegateOpeningEditSession) {
					opendedCourse = CourseFactory.openCourseEditSession(course.getResourceableId());
				}

				log.info("Save of:" + course.getCourseTitle() + " (" + course.getResourceableId() + ")");
				CourseFactory.saveCourse(course.getResourceableId());
			} else {
				log.info("No change for: " + course.getCourseTitle() + " (" + course.getResourceableId() + ")");
			}
		} catch (Exception e) {
			log.error("", e);
		} finally {
			if(opendedCourse != null && delegateOpeningEditSession) {
				CourseFactory.closeCourseEditSession(opendedCourse.getResourceableId(), true);
			}	
		}
	}

	private void processCourseRec(ProcessingOccured processingFlag, INode node,
			List<BusinessGroup> groups, List<BGArea> areas) {
		if(node instanceof CourseNode) {
			processCourseNode(processingFlag, (CourseNode)node, groups, areas);
		}
		for(int i=node.getChildCount(); i-->0; ) {
			INode subNode = node.getChildAt(i);
			processCourseRec(processingFlag, subNode, groups, areas);
		}
	}
	
	private void processEditorCourseRec(ProcessingOccured processingFlag, INode node,
			List<BusinessGroup> groups, List<BGArea> areas) {
		if(node instanceof CourseEditorTreeNode) {
			processCourseNode(processingFlag, ((CourseEditorTreeNode)node).getCourseNode(), groups, areas);
		}
		for(int i=node.getChildCount(); i-->0; ) {
			INode subNode = node.getChildAt(i);
			processEditorCourseRec(processingFlag, subNode, groups, areas);
		}
	}
	
	/**
	 * Update all the conditions in every type of course node
	 * @param processingFlag
	 * @param node
	 * @param groups
	 * @param areas
	 */
	private void processCourseNode(ProcessingOccured processingFlag, CourseNode node,
			List<BusinessGroup> groups, List<BGArea> areas) {
		if(node instanceof GenericCourseNode) {
			GenericCourseNode genericNode = (GenericCourseNode)node;
			processCondition(processingFlag, genericNode.getPreConditionAccess(), groups, areas);
			processCondition(processingFlag, genericNode.getPreConditionVisibility(), groups, areas);
			if(node instanceof AbstractAccessableCourseNode) {
				//nothing self but
				
				if(node instanceof WikiCourseNode) {
					WikiCourseNode wikiNode = (WikiCourseNode)node;
					processCondition(processingFlag, wikiNode.getPreConditionEdit(), groups, areas);
				} else if(node instanceof STCourseNode) {
					STCourseNode structureNode = (STCourseNode)node;
					processStructureNode(processingFlag, structureNode, groups, areas);
				} else if (node instanceof PortfolioCourseNode) {
					PortfolioCourseNode portfolioNode = (PortfolioCourseNode)node;
					processCondition(processingFlag, portfolioNode.getPreConditionEdit(), groups, areas);
				} else if (node instanceof InfoCourseNode) {
					InfoCourseNode infoNode = (InfoCourseNode)node;
					processCondition(processingFlag, infoNode.getPreConditionEdit(), groups, areas);
					processCondition(processingFlag, infoNode.getPreConditionAdmin(), groups, areas);
				} else if (node instanceof DialogCourseNode) {
					DialogCourseNode dialogNode = (DialogCourseNode)node;
					processCondition(processingFlag, dialogNode.getPreConditionModerator(), groups, areas);
					processCondition(processingFlag, dialogNode.getPreConditionPoster(), groups, areas);
					processCondition(processingFlag, dialogNode.getPreConditionReader(), groups, areas);
				} else if (node instanceof CalCourseNode) {
					CalCourseNode calNode = (CalCourseNode)node;
					processCondition(processingFlag, calNode.getPreConditionEdit(), groups, areas);
				}	else if (node instanceof COCourseNode) {
					COCourseNode coNode = (COCourseNode)node;
					processCONode(processingFlag, coNode, groups, areas);
				}	else if (node instanceof ENCourseNode) {
					ENCourseNode enrollmentNode = (ENCourseNode)node;
					processEnrollmentNode(processingFlag, enrollmentNode, groups, areas);
				}
			} 
			if(node instanceof TACourseNode) {
				TACourseNode taskNode = (TACourseNode)node;
				processCondition(processingFlag, taskNode.getConditionDrop(), groups, areas);
				processCondition(processingFlag, taskNode.getConditionReturnbox(), groups, areas);
				processCondition(processingFlag, taskNode.getConditionScoring(), groups, areas);
				processCondition(processingFlag, taskNode.getConditionSolution(), groups, areas);
				processCondition(processingFlag, taskNode.getConditionTask(), groups, areas);
			} else if(node instanceof ProjectBrokerCourseNode) {
				ProjectBrokerCourseNode brokerNode = (ProjectBrokerCourseNode)node;
				processCondition(processingFlag, brokerNode.getConditionDrop(), groups, areas);
				processCondition(processingFlag, brokerNode.getConditionReturnbox(), groups, areas);
				processCondition(processingFlag, brokerNode.getConditionScoring(), groups, areas);
				processCondition(processingFlag, brokerNode.getConditionProjectBroker(), groups, areas);
			} else if (node instanceof FOCourseNode) {
				FOCourseNode forumNode = (FOCourseNode)node;
				processCondition(processingFlag, forumNode.getPreConditionModerator(), groups, areas);
				processCondition(processingFlag, forumNode.getPreConditionPoster(), groups, areas);
				processCondition(processingFlag, forumNode.getPreConditionReader(), groups, areas);
			} else if (node instanceof BCCourseNode) {
				BCCourseNode bcNode = (BCCourseNode)node;
				processCondition(processingFlag, bcNode.getPreConditionDownloaders(), groups, areas);
				processCondition(processingFlag, bcNode.getPreConditionUploaders(), groups, areas);
			} else if (node instanceof AbstractFeedCourseNode) {
				AbstractFeedCourseNode feedNode = (AbstractFeedCourseNode)node;
				processCondition(processingFlag, feedNode.getPreConditionModerator(), groups, areas);
				processCondition(processingFlag, feedNode.getPreConditionPoster(), groups, areas);
				processCondition(processingFlag, feedNode.getPreConditionReader(), groups, areas);
			}
		}
	}
	
	private void processEnrollmentNode(ProcessingOccured processingFlag, ENCourseNode node,
			List<BusinessGroup> groups, List<BGArea> areas) {

		ModuleConfiguration mc = node.getModuleConfiguration();
		String groupNames = (String)mc.get(ENCourseNode.CONFIG_GROUPNAME);
		@SuppressWarnings("unchecked")
		List<Long> groupKeys = (List<Long>) mc.get(ENCourseNode.CONFIG_GROUP_IDS);
		if(groupKeys == null && StringHelper.containsNonWhitespace(groupNames)) {
			List<Long> groupKeyList = toGroupKeyList(groupNames, groups);
			mc.set(ENCourseNode.CONFIG_GROUP_IDS, groupKeyList);
			processingFlag.processed();
		}

		String areaNames = (String)mc.get(ENCourseNode.CONFIG_AREANAME);
		@SuppressWarnings("unchecked")
		List<Long> areaKeys = (List<Long>) mc.get(ENCourseNode.CONFIG_AREA_IDS);
		if(areaKeys == null && StringHelper.containsNonWhitespace(areaNames)) {
			List<Long> areaKeyList = toAreaKeyList(areaNames, areas);
			mc.set(ENCourseNode.CONFIG_AREA_IDS, areaKeyList);
			processingFlag.processed();
		}
	}
	
	private void processCONode(ProcessingOccured processingFlag, COCourseNode coNode,
			List<BusinessGroup> groups, List<BGArea> areas) {
		
		ModuleConfiguration mc = coNode.getModuleConfiguration();
		String groupNames = (String)mc.get(COEditController.CONFIG_KEY_EMAILTOGROUPS);
		@SuppressWarnings("unchecked")
		List<Long> groupKeys = (List<Long>) mc.get(COEditController.CONFIG_KEY_EMAILTOGROUP_IDS);
		if(groupKeys == null && StringHelper.containsNonWhitespace(groupNames)) {
			List<Long> groupKeyList = toGroupKeyList(groupNames, groups);
			mc.set(COEditController.CONFIG_KEY_EMAILTOGROUP_IDS, groupKeyList);
			processingFlag.processed();
		}

		String areaNames = (String)mc.get(COEditController.CONFIG_KEY_EMAILTOAREAS);
		@SuppressWarnings("unchecked")
		List<Long> areaKeys = (List<Long>) mc.get(COEditController.CONFIG_KEY_EMAILTOAREA_IDS);
		if(areaKeys == null && StringHelper.containsNonWhitespace(areaNames)) {
			List<Long> areaKeyList = toAreaKeyList(areaNames, areas);
			mc.set(COEditController.CONFIG_KEY_EMAILTOAREA_IDS, areaKeyList);
			processingFlag.processed();
		}
	}
	
	private void processStructureNode(ProcessingOccured processingFlag, STCourseNode structureNode,
			List<BusinessGroup> groups, List<BGArea> areas) {
		
		ScoreCalculator calculator = structureNode.getScoreCalculator();
		String score = calculator.getScoreExpression();
		String passed = calculator.getPassedExpression();
		
		boolean stProcessed = false;
		if(StringHelper.containsNonWhitespace(score)) {
			String processedExpression = processExpression(processingFlag, score, groups, areas);
			if(!processedExpression.equals(score)) {
				calculator.setScoreExpression(processedExpression);
				stProcessed = true;
			}	
		}
		
		if(StringHelper.containsNonWhitespace(passed)) {
			String processedExpression = processExpression(processingFlag, passed, groups, areas);
			if(!processedExpression.equals(score)) {
				calculator.setScoreExpression(processedExpression);
				stProcessed = true;
			}	
		}
		
		if(stProcessed) {
			structureNode.setScoreCalculator(calculator);
		}
	}
	
	private void processCondition(ProcessingOccured processingFlag, Condition condition,
			List<BusinessGroup> groups, List<BGArea> areas) {
		boolean easy = StringHelper.containsNonWhitespace(condition.getConditionFromEasyModeConfiguration());
		if(easy) {
			//already processed?
			if(StringHelper.containsNonWhitespace(condition.getEasyModeGroupAccessIds())
					|| StringHelper.containsNonWhitespace(condition.getEasyModeGroupAreaAccessIds())) {
				return;
			}
			if(!StringHelper.containsNonWhitespace(condition.getEasyModeGroupAccess())
					&& !StringHelper.containsNonWhitespace(condition.getEasyModeGroupAreaAccess())) {
				return;
			}
			
			String groupKeys = toGroupKeys(condition.getEasyModeGroupAccess(), groups);
			condition.setEasyModeGroupAccessIds(groupKeys);
			String areaKeys = toAreaKeys(condition.getEasyModeGroupAreaAccess(), areas);
			condition.setEasyModeGroupAreaAccessIds(areaKeys);
			processingFlag.processed();
		} else if(condition.isExpertMode()) {
			String expression = condition.getConditionExpression();
			if(StringHelper.containsNonWhitespace(expression)) {
				String reference = condition.getConditionExpression();
				String processExpression = processExpression(processingFlag, expression, groups, areas);
				if(!reference.equals(processExpression)) {
					condition.setConditionUpgraded(processExpression);
					processingFlag.processed();
				}
			}
		}
	}
	
	private String processExpression(ProcessingOccured processingFlag, String expression,
			List<BusinessGroup> groups, List<BGArea> areas) {

		for(BusinessGroup group:groups) {
			String strToMatch = "\"" + group.getName() + "\"";
			String replacement = "\"" + group.getKey() + "\"";
			expression = replaceAllCaseInsensitive(expression, strToMatch, replacement);
		}
		for(BGArea area:areas) {
			String strToMatch = "\"" + area.getName() + "\"";
			String replacement = "\"" + area.getKey() + "\"";
			expression = replaceAllCaseInsensitive(expression, strToMatch, replacement);
		}
		
		return expression;
	}
	
	private String replaceAllCaseInsensitive(String expression, String name, String replacement) {
		String lcExpresion = expression.toLowerCase();
		String lcName = name.toLowerCase();

		int index = 0;
		while((index = lcExpresion.indexOf(lcName, index)) > 0) {
			int startIndex = index;
			int stopIndex = index + lcName.length();
			
			String newExpression = expression.substring(0, startIndex);
			newExpression += replacement;
			newExpression += expression.substring(stopIndex);
			
			expression = newExpression;
			lcExpresion = expression.toLowerCase();
			index = startIndex + replacement.length();	
		}
		return expression;
	}
	
	private String toGroupKeys(String groupNames, List<BusinessGroup> groups) {
		if(!StringHelper.containsNonWhitespace(groupNames)) return null;
		String[] groupNameArr = groupNames.split(",");
		StringBuilder sb = new StringBuilder();
		for(String groupName:groupNameArr) {
			groupName = groupName.trim();
			for(BusinessGroup group:groups) {
				if(groupName.equalsIgnoreCase(group.getName())) {
					if(sb.length() > 0) {
						sb.append(',');
					}
					sb.append(group.getKey());
					break;
				}
			}
		}
		return sb.toString().trim();
	}
	
	private List<Long> toGroupKeyList(String groupNames, List<BusinessGroup> groups) {
		if(!StringHelper.containsNonWhitespace(groupNames)) return null;
		String[] groupNameArr = groupNames.split(",");
		List<Long> groupKeyList = new ArrayList<Long>();
		for(String groupName:groupNameArr) {
			groupName = groupName.trim();
			for(BusinessGroup group:groups) {
				if(groupName.equalsIgnoreCase(group.getName())) {
					groupKeyList.add(group.getKey());
					break;
				}
			}
		}
		return groupKeyList;
	}
	
	private String toAreaKeys(String areaNames, List<BGArea> areas) {
		if(!StringHelper.containsNonWhitespace(areaNames)) return null;
		String[] areaNameArr = areaNames.split(",");
		StringBuilder sb = new StringBuilder();
		for(String areaName:areaNameArr) {
			areaName = areaName.trim();
			for(BGArea area:areas) {
				if(areaName.equalsIgnoreCase(area.getName())) {
					if(sb.length() > 0) {
						sb.append(',');
					}
					sb.append(area.getKey());
					break;
				}
			}
		}
		return sb.toString().trim();
	}
	
	private List<Long> toAreaKeyList(String areaNames, List<BGArea> areas) {
		if(!StringHelper.containsNonWhitespace(areaNames)) return null;
		String[] areaNameArr = areaNames.split(",");
		List<Long> areaKeyList = new ArrayList<Long>();
		for(String areaName:areaNameArr) {
			areaName = areaName.trim();
			for(BGArea area:areas) {
				if(areaName.equalsIgnoreCase(area.getName())) {
					areaKeyList.add(area.getKey());
					break;
				}
			}
		}
		return areaKeyList;
	}
	
	private class ProcessingOccured {
		private boolean processed = false;

		public boolean isProcessed() {
			return processed;
		}

		public void processed() {
			processed = true;
		}
	}
	
}
