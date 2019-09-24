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

package org.olat.course.assessment;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.course.ICourse;
import org.olat.course.assessment.model.AssessmentNodeData;
import org.olat.course.assessment.model.AssessmentNodesLastModified;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.ProjectBrokerCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.ScormCourseNode;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.ModuleConfiguration;

/**
 * Description:<br>
 * Helper methods for the course assessment system
 * <P>
 * Initial Date: Oct 28, 2004<br>
 * @author gnaegi
 */
public class AssessmentHelper {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentHelper.class);
	
	public static final String KEY_TYPE = "type";
	public static final String KEY_IDENTIFYER = "identifyer";
	public static final String KEY_INDENT = "indent";

	public static final String KEY_TITLE_SHORT = "short.title";
	public static final String KEY_TITLE_LONG = "long.title";
	public static final String KEY_PASSED = "passed";
	public static final String KEY_SCORE = "score";
	public static final String KEY_SCORE_F = "fscore";
	public static final String KEY_ATTEMPTS = "attempts";
	public static final String KEY_DETAILS = "details";
	public static final String KEY_SELECTABLE = "selectable";
	public static final String KEY_MIN = "minScore";
	public static final String KEY_MAX = "maxScore";
	public static final String KEY_TOTAL_NODES = "totalNodes";
	public static final String KEY_ATTEMPTED_NODES = "attemptedNodes";
	public static final String KEY_PASSED_NODES = "attemptedNodes";
	public static final String KEY_LAST_USER_MODIFIED = "lastUserModified";
	public static final String KEY_LAST_COACH_MODIFIED = "lastCoachModified";

	/**
	 * String to symbolize 'not available' or 'not assigned' in assessments
	 * details *
	 */
	public static final String DETAILS_NA_VALUE = "n/a";

	/** Highes score value supported by OLAT * */
	public static final float MAX_SCORE_SUPPORTED = 10000f;
	/** Lowest score value supported by OLAT * */
	public static final float MIN_SCORE_SUPPORTED = -10000f;

	private static final DecimalFormat scoreFormat = new DecimalFormat("#0.###", new DecimalFormatSymbols(Locale.ENGLISH));

	/**
	 * Wraps an identity and it's score evaluation / attempts in a wrapper object
	 * for a given course node
	 * 
	 * @param identity
	 * @param localUserCourseEnvironmentCache
	 * @param course the course
	 * @param courseNode an assessable course node or null if no details and
	 *          attempts must be fetched
	 * @return a wrapped identity
	 */
	public static AssessedIdentityWrapper wrapIdentity(Identity identity, Map<Long,UserCourseEnvironment> localUserCourseEnvironmentCache,
			Map<Long, Date> initialLaunchDates, ICourse course, AssessableCourseNode courseNode) {
		// Try to get user course environment from local hash map cache. If not
		// successful
		// create the environment and add it to the map for later performance
		// optimization
			UserCourseEnvironment uce = localUserCourseEnvironmentCache.get(identity.getKey());
			if (uce == null) {
				uce = createAndInitUserCourseEnvironment(identity, course);
				// add to cache for later usage
				localUserCourseEnvironmentCache.put(identity.getKey(), uce);
				if (log.isDebugEnabled()){
					log.debug("localUserCourseEnvironmentCache hit failed, adding course environment for user::" + identity.getKey());
				}
			}
			
			Date initialLaunchDate = initialLaunchDates.get(identity.getKey());
			return wrapIdentity(uce, initialLaunchDate, courseNode);
	}

	/**
	 * Wraps an identity and it's score evaluation / attempts in a wrapper object
	 * for a given course node
	 * 
	 * @param uce The users course environment. Must be initialized
	 *          (uce.getScoreAccounting().evaluateAll() must be called previously)
	 * @param courseNode an assessable course node or null if no details and
	 *          attempts must be fetched
	 * @return a wrapped identity
	 */
	public static AssessedIdentityWrapper wrapIdentity(UserCourseEnvironment uce, Date initialLaunchDate, AssessableCourseNode courseNode) {
		// Fetch attempts and details for this node if available
		Integer attempts = null;
		String details = null;
		if (courseNode != null) {
			if (courseNode.hasAttemptsConfigured()) {
				attempts = courseNode.getUserAttempts(uce);
			}
			if (courseNode.hasDetails()) {
				details = courseNode.getDetailsListView(uce);
				if (details == null) {
					details = DETAILS_NA_VALUE;
				}
			}
		}

		Identity identity = uce.getIdentityEnvironment().getIdentity();
		Date lastModified = uce.getCourseEnvironment().getAssessmentManager().getScoreLastModifiedDate(courseNode, identity);
		return new AssessedIdentityWrapper(uce, attempts, details, initialLaunchDate, lastModified);
	}
	
	public static UserCourseEnvironment createInitAndUpdateUserCourseEnvironment(Identity identity, ICourse course) {
		// create an identenv with no roles, no attributes, no locale
		IdentityEnvironment ienv = new IdentityEnvironment(); 
		ienv.setIdentity(identity);
		UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
		// Fetch all score and passed and calculate score accounting for the entire
		// course
		uce.getScoreAccounting().evaluateAll(true);
		return uce;
	}

	/**
	 * Create a user course environment for the given user and course. After
	 * creation, the users score accounting will be initialized.
	 * 
	 * @param identity
	 * @param course
	 * @return Initialized user course environment
	 */
	public static UserCourseEnvironment createAndInitUserCourseEnvironment(Identity identity, ICourse course) {
		// create an identenv with no roles, no attributes, no locale
		IdentityEnvironment ienv = new IdentityEnvironment(); 
		ienv.setIdentity(identity);
		UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
		// Fetch all score and passed and calculate score accounting for the entire
		// course
		uce.getScoreAccounting().evaluateAll();
		return uce;
	}
	
	public static UserCourseEnvironment createAndInitUserCourseEnvironment(Identity identity, CourseEnvironment courseEnv) {
		// create an identenv with no roles, no attributes, no locale
		IdentityEnvironment ienv = new IdentityEnvironment(); 
		ienv.setIdentity(identity);
		UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, courseEnv);
		// Fetch all score and passed and calculate score accounting for the entire
		// course
		uce.getScoreAccounting().evaluateAll();
		return uce;
	}

	/**
	 * check the given node for assessability.
	 * @param node
	 * @return
	 */
	public static boolean checkIfNodeIsAssessable(CourseNode node) {
		if (node instanceof AssessableCourseNode) {
			if (node instanceof STCourseNode) {
				STCourseNode scn = (STCourseNode) node;
				if (scn.hasPassedConfigured() || scn.hasScoreConfigured()) {
					return true;
				}
			} else if (node instanceof ScormCourseNode) {
				ScormCourseNode scormn = (ScormCourseNode) node;
				if (scormn.hasPassedConfigured() || scormn.hasScoreConfigured()) {
					return true;
				}
			} else if (node instanceof ProjectBrokerCourseNode) {
				return false;//no assessment-tool in V1.0 return always false
			} else {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks recursivley a course structure or a part of it for assessable nodes
	 * or for structure course nodes (subtype of assessable node), which
	 * 'hasPassedConfigured' or 'hasScoreConfigured' is true. If founds the first
	 * node that meets the criterias, it returns true.
	 * 
	 * @param node
	 * @return boolean
	 */
	public static boolean checkForAssessableNodes(CourseNode node) {
		if(checkIfNodeIsAssessable(node)) {
			return true;
		}
		// check children now
		int count = node.getChildCount();
		for (int i = 0; i < count; i++) {
			CourseNode cn = (CourseNode) node.getChildAt(i);
			if (checkForAssessableNodes(cn)) {
				return true;
			}
		}
		return false;
	}
	
	public static int countAssessableNodes(CourseNode node) {
		int count = 0;
		if(checkIfNodeIsAssessable(node)) {
			count++;
		}
		// check children now
		int numOfChildren = node.getChildCount();
		for (int i = 0; i<numOfChildren; i++) {
			CourseNode cn = (CourseNode) node.getChildAt(i);
			count += countAssessableNodes(cn);
		}
		return count;
	}

	/**
	 * Get all assessable nodes including the root node (if assessable)
	 * 
	 * @param editorModel
	 * @param excludeNode Node that should be excluded in the list, e.g. the
	 *          current node or null if all assessable nodes should be used
	 * @return List of assessable course nodes
	 */
	public static List<CourseNode> getAssessableNodes(final CourseEditorTreeModel editorModel, final CourseNode excludeNode) {
		CourseEditorTreeNode rootNode = (CourseEditorTreeNode) editorModel.getRootNode();
		final List<CourseNode> nodes = new ArrayList<>();
		// visitor class: takes all assessable nodes if not the exclude node and
		// puts
		// them into the nodes list
		Visitor visitor = new Visitor() {
			@Override
			public void visit(INode node) {
				CourseEditorTreeNode editorNode = (CourseEditorTreeNode) node;
				CourseNode courseNode = editorModel.getCourseNode(node.getIdent());
				if (!editorNode.isDeleted() && (courseNode != excludeNode)) {
					if(checkIfNodeIsAssessable(courseNode)) {
						nodes.add(courseNode);
					}
				}
			}
		};
		// not visit beginning at the root node
		TreeVisitor tv = new TreeVisitor(visitor, rootNode, false);
		tv.visitAll();
		return nodes;
	}
	
	public static String getRoundedScore(BigDecimal score) {
		if (score == null) return null;
		
		Double fscore = score.doubleValue();
		return getRoundedScore(fscore); 
	}

	/**
	 * @param score The score to be rounded
	 * @return The rounded score for GUI presentation
	 */
	public static String getRoundedScore(Float score) {
		if (score == null) return null;

		//cluster_OK the formatter is not multi-thread and costly to create
		synchronized(scoreFormat) {
			return scoreFormat.format(score);
		}
	}
	
	public static String getRoundedScore(Double score) {
		if (score == null) return null;

		//cluster_OK the formatter is not multi-thread and costly to create
		synchronized(scoreFormat) {
			return scoreFormat.format(score);
		}
	}
	
	public static Float getRoundedScore(String score) {
		if (!StringHelper.containsNonWhitespace(score)) return null;

		//cluster_OK the formatter is not multi-thread and costly to create
		synchronized(scoreFormat) {
			try {
				return new Float(scoreFormat.parse(score).floatValue());
			} catch (ParseException e) {
				log.error("", e);
				return null;
			}
		}
	}
	
	/**
	 * 
	 * @param course
	 * @return
	 */
	public static TreeModel assessmentTreeModel(ICourse course) {
		CourseNode rootNode = course.getRunStructure().getRootNode();
		GenericTreeModel gtm = new GenericTreeModel();
		GenericTreeNode node = new GenericTreeNode();
		node.setTitle(rootNode.getShortTitle());
		node.setUserObject(rootNode);
		node.setIconCssClass(CourseNodeFactory.getInstance().getCourseNodeConfiguration(rootNode.getType()).getIconCSSClass());
		gtm.setRootNode(node);
		
		List<GenericTreeNode> children = addAssessableNodesToList(rootNode);
		children.forEach(child -> node.addChild(child));
		return gtm;
	}
	
	private static List<GenericTreeNode> addAssessableNodesToList(CourseNode parentCourseNode) {
		List<GenericTreeNode> result = new ArrayList<>();
		for(int i=0; i<parentCourseNode.getChildCount(); i++) {
			CourseNode courseNode = (CourseNode)parentCourseNode.getChildAt(i);
			List<GenericTreeNode> assessableChildren = addAssessableNodesToList(courseNode);
			
			if (assessableChildren.size() > 0 || isAssessable(courseNode)) {
				GenericTreeNode node = new GenericTreeNode();
				node.setTitle(courseNode.getShortTitle());
				node.setUserObject(courseNode);
				CourseNodeConfiguration nodeconfig = CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(courseNode.getType());
				node.setIconCssClass(nodeconfig.getIconCSSClass());
				result.add(node);
				assessableChildren.forEach(child -> node.addChild(child));
			}
		}
		return result;
	}
	
	private static boolean isAssessable(CourseNode courseNode) {
		boolean assessable = false;
		if (courseNode instanceof AssessableCourseNode && !(courseNode instanceof ProjectBrokerCourseNode)) {
			AssessableCourseNode assessableCourseNode = (AssessableCourseNode) courseNode;
			if (assessableCourseNode.hasDetails()
				|| assessableCourseNode.hasAttemptsConfigured()
				|| assessableCourseNode.hasScoreConfigured()
				|| assessableCourseNode.hasPassedConfigured()
				|| assessableCourseNode.hasCommentConfigured()) {

				assessable = true;
			}
		}
		return assessable;
	}
	

	public static List<Map<String,Object>> assessmentNodeDataListToMap(List<AssessmentNodeData> assessmentNodeData) {
		List<Map<String,Object>> maps = new ArrayList<>(assessmentNodeData.size());
		for(AssessmentNodeData data:assessmentNodeData) {
			maps.add(data.toMap());
		}
		return maps;
	}
	
	public static  List<AssessmentNodeData> assessmentNodeDataMapToList(List<Map<String,Object>> assessmentNodeData) {
		List<AssessmentNodeData> list = new ArrayList<>(assessmentNodeData.size());
		for(Map<String,Object> data:assessmentNodeData) {
			list.add(new AssessmentNodeData(data));
		}
		return list;
	}
	
	/**
	 * Add all assessable nodes and the scoring data to a list. Each item in the list is an object array
	 * that has the following data:
	 * @param recursionLevel
	 * @param courseNode
	 * @param userCourseEnv
	 * @param discardEmptyNodes
	 * @param discardComments
	 * @return list of object arrays or null if empty
	 */
	public static List<AssessmentNodeData> getAssessmentNodeDataList(UserCourseEnvironment userCourseEnv,
			AssessmentNodesLastModified lastModifications,
			boolean followUserVisibility, boolean discardEmptyNodes, boolean discardComments) {
		List<AssessmentNodeData> data = new ArrayList<>(50);
		ScoreAccounting scoreAccounting = userCourseEnv.getScoreAccounting();
		scoreAccounting.evaluateAll();
		getAssessmentNodeDataList(0, userCourseEnv.getCourseEnvironment().getRunStructure().getRootNode(),
				scoreAccounting, userCourseEnv, followUserVisibility, discardEmptyNodes, discardComments, data, lastModifications);
		return data;
	}
	
	/**
	 * Calculated 
	 * 
	 * 
	 * @param evaluatedScoreAccounting
	 * @param userCourseEnv
	 * @param discardEmptyNodes
	 * @param discardComments
	 * @return
	 */
	public static List<AssessmentNodeData> getAssessmentNodeDataList(ScoreAccounting evaluatedScoreAccounting, UserCourseEnvironment userCourseEnv,
			boolean followUserVisibility, boolean discardEmptyNodes, boolean discardComments) {
		List<AssessmentNodeData> data = new ArrayList<>(50);
		getAssessmentNodeDataList(0, userCourseEnv.getCourseEnvironment().getRunStructure().getRootNode(),
				evaluatedScoreAccounting, userCourseEnv, followUserVisibility, discardEmptyNodes, discardComments, data, null);
		return data;
	}
	
	
	public static int getAssessmentNodeDataList(int recursionLevel, CourseNode courseNode, ScoreAccounting scoreAccounting,
			UserCourseEnvironment userCourseEnv, boolean followUserVisibility, boolean discardEmptyNodes, boolean discardComments,
			List<AssessmentNodeData> data, AssessmentNodesLastModified lastModifications) {
		// 1) Get list of children data using recursion of this method
		AssessmentNodeData assessmentNodeData = new AssessmentNodeData(recursionLevel, courseNode);
		data.add(assessmentNodeData);
		
		int numOfChildren = 0;
		for (int i = 0; i < courseNode.getChildCount(); i++) {
			CourseNode child = (CourseNode) courseNode.getChildAt(i);
			numOfChildren += getAssessmentNodeDataList(recursionLevel + 1,  child,  scoreAccounting,
					userCourseEnv, followUserVisibility, discardEmptyNodes, discardComments, data, lastModifications);
		}
		
		// 2) Get data of this node only if
		// - it has any wrapped children  or
		// - it is of an assessable course node type
		boolean hasDisplayableValuesConfigured = false;
		boolean hasDisplayableUserValues = false;
		if (numOfChildren > 0 || courseNode instanceof AssessableCourseNode) {
			if(courseNode instanceof ProjectBrokerCourseNode) {
				//ProjectBroker : no assessment-tool in V1.0 , remove project broker completely form assessment-tool gui
				assessmentNodeData.setSelectable(false);
			} else if (courseNode instanceof AssessableCourseNode) {
				AssessableCourseNode assessableCourseNode = (AssessableCourseNode) courseNode;
				AssessmentEvaluation scoreEvaluation = scoreAccounting.evalCourseNode(assessableCourseNode);
				if(scoreEvaluation != null) {
					assessmentNodeData.setAssessmentStatus(scoreEvaluation.getAssessmentStatus());
					assessmentNodeData.setNumOfAssessmentDocs(scoreEvaluation.getNumOfAssessmentDocs());
				}
				assessmentNodeData.setUserVisibility(scoreEvaluation.getUserVisible());
				assessmentNodeData.setLastModified(scoreEvaluation.getLastModified());
				assessmentNodeData.setLastUserModified(scoreEvaluation.getLastUserModified());
				assessmentNodeData.setLastCoachModified(scoreEvaluation.getLastCoachModified());
				if(lastModifications != null) {
					lastModifications.addLastModified(scoreEvaluation.getLastModified());
					lastModifications.addLastUserModified(scoreEvaluation.getLastUserModified());
					lastModifications.addLastCoachModified(scoreEvaluation.getLastCoachModified());
				}
				
				if(!followUserVisibility || scoreEvaluation.getUserVisible() == null || scoreEvaluation.getUserVisible().booleanValue()) {
					// details 
					if (assessableCourseNode.hasDetails()) {
						hasDisplayableValuesConfigured = true;
						String detailValue = assessableCourseNode.getDetailsListView(userCourseEnv);
						if (detailValue == null) {
							// ignore unset details in discardEmptyNodes mode
							assessmentNodeData.setDetails(AssessmentHelper.DETAILS_NA_VALUE);
						} else {
							assessmentNodeData.setDetails(detailValue);
							hasDisplayableUserValues = true;
						}
					}
					// attempts
					if (assessableCourseNode.hasAttemptsConfigured()) {
						hasDisplayableValuesConfigured = true;
						Integer attemptsValue = scoreEvaluation.getAttempts(); 
						if (attemptsValue != null) {
							assessmentNodeData.setAttempts(attemptsValue);
							if (attemptsValue.intValue() > 0) {
								// ignore attempts = 0  in discardEmptyNodes mode
								hasDisplayableUserValues = true;
							}
						}
					}
					// score
					if (assessableCourseNode.hasScoreConfigured()) {
						hasDisplayableValuesConfigured = true;
						Float score = scoreEvaluation.getScore();
						if (score != null) {
							assessmentNodeData.setRoundedScore(AssessmentHelper.getRoundedScore(score));
							assessmentNodeData.setScore(score);
							hasDisplayableUserValues = true;
						}
						if(!(assessableCourseNode instanceof STCourseNode)) {
							assessmentNodeData.setMaxScore(assessableCourseNode.getMaxScoreConfiguration());
							assessmentNodeData.setMinScore(assessableCourseNode.getMinScoreConfiguration());
						}
					}
					// passed
					if (assessableCourseNode.hasPassedConfigured()) {
						hasDisplayableValuesConfigured = true;
						Boolean passed = scoreEvaluation.getPassed();
						if (passed != null) {
							assessmentNodeData.setPassed(passed);
							hasDisplayableUserValues = true;
						}
					}
				}
				// selection command available
				AssessableCourseNode acn = (AssessableCourseNode) courseNode;
				if (acn.isEditableConfigured()) {
					// Assessable course nodes are selectable
					assessmentNodeData.setSelectable(true);
				} else {
					// assessable nodes that do not have score or passed are not selectable
					// (e.g. a st node with no defined rule
					assessmentNodeData.setSelectable(false);
				}
				
				if (!hasDisplayableUserValues && assessableCourseNode.hasCommentConfigured() && !discardComments) {
				  // comments are invisible in the table but if configured the node must be in the list
					// for the efficiency statement this can be ignored, this is the case when discardComments is true
					hasDisplayableValuesConfigured = true;
					if (assessableCourseNode.getUserUserComment(userCourseEnv) != null) {
						hasDisplayableUserValues = true;
					}
				}
			} else {
				// Not assessable nodes are not selectable. (e.g. a node that 
				// has an assessable child node but is itself not assessable)
				assessmentNodeData.setSelectable(false);
			}
		}
		
		// 3) Add data of this node to mast list if node assessable or children list has any data.
		// Do only add nodes when they have any assessable element, otherwhise discard (e.g. empty course, 
		// structure nodes without scoring rules)! When the discardEmptyNodes flag is set then only
		// add this node when there is user data found for this node.
		
		boolean addNode = (numOfChildren > 0 
				|| (discardEmptyNodes && hasDisplayableValuesConfigured && hasDisplayableUserValues)
				|| (!discardEmptyNodes && hasDisplayableValuesConfigured));
		if(!addNode) {
			data.remove(assessmentNodeData);
			return 0;
		}
		return numOfChildren + 1;//add itself
	}
	
	/**
	 * Evaluates if the results are visble or not in respect of the configured CONFIG_KEY_DATE_DEPENDENT_RESULTS parameter. <br>
	 * The results are always visible if no date dependent, 
	 * or if date dependent only in the period: startDate-endDate. 
	 * EndDate could be null, that is there is no restriction for the end date.
	 * 
	 * @return true if is visible.
	 */
	public static boolean isResultVisible(ModuleConfiguration modConfig) {
		boolean isVisible = false;
		Boolean showResultsActive = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS);
		if(showResultsActive != null && showResultsActive.booleanValue()) {
			Date startDate = (Date)modConfig.get(IQEditController.CONFIG_KEY_RESULTS_START_DATE);
			Date endDate = (Date)modConfig.get(IQEditController.CONFIG_KEY_RESULTS_END_DATE);
			Date currentDate = new Date();
			if(startDate != null && currentDate.after(startDate) && (endDate == null || currentDate.before(endDate))) {
				isVisible = true;
			}
		} else {
			isVisible = true;
		}
		return isVisible;
	}
}