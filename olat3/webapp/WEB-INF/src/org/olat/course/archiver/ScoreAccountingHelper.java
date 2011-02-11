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
* <p>
*/ 

package org.olat.course.archiver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * @author schneider
 * Comment: Provides functionality to get a course results overview.
 */
public class ScoreAccountingHelper {
    private static final String PACKAGE = Util.getPackageName(ScoreAccountingArchiveController.class);
    
	/**
	 * The results from assessable nodes are written to one row per user into an excel-sheet. An
     * assessable node will only appear if it is producing at least one of the
     * following variables: score, passed, attempts, comments.
	 * 
	 * @param identities
	 * @param myNodes
	 * @param course
	 * @param locale
	 * @return String
	 */
	public static String createCourseResultsOverviewTable(List identities, List myNodes, ICourse course, Locale locale) {
	    Translator t = new PackageTranslator(PACKAGE, locale);
	    StringBuilder tableHeader1 = new StringBuilder();
		StringBuilder tableHeader2 = new StringBuilder();
		StringBuilder tableContent = new StringBuilder();
		StringBuilder table = new StringBuilder();

		String sequentialNumber = t.translate("column.header.seqnum");
		String login = t.translate("column.header.login");
		// user properties are dynamic
		String sc = t.translate("column.header.score");
		String pa = t.translate("column.header.passed");
		String co = t.translate("column.header.comment");
		String cco = t.translate("column.header.coachcomment");
		String at = t.translate("column.header.attempts");
		String na = t.translate("column.field.notavailable");
		String mi = t.translate("column.field.missing");
		String yes = t.translate("column.field.yes");
		String no = t.translate("column.field.no");

		
		tableHeader1.append(sequentialNumber);
		tableHeader1.append("\t");
		tableHeader2.append("\t");
		
		tableHeader1.append(login);
		tableHeader1.append("\t");
		tableHeader2.append("\t");
		// get user property handlers for this export, translate using the fallback
		// translator configured in the property handler
		
		List<UserPropertyHandler> userPropertyHandlers = UserManager.getInstance().getUserPropertyHandlersFor(
				ScoreAccountingHelper.class.getCanonicalName(), true);
		t = UserManager.getInstance().getPropertyHandlerTranslator(t);
		for (UserPropertyHandler propertyHandler : userPropertyHandlers) {
			tableHeader1.append(t.translate(propertyHandler.i18nColumnDescriptorLabelKey()));
			tableHeader1.append("\t");			
			tableHeader2.append("\t");
		}				

		// preload user properties cache
		course.getCourseEnvironment().getAssessmentManager().preloadCache();
		
		boolean firstIteration = true;
		int rowNumber = 1;
		Iterator iterIdentities = identities.iterator();
		while (iterIdentities.hasNext()) {
			Identity identity = (Identity) iterIdentities.next();
			String uname = identity.getName();

			tableContent.append(rowNumber);
			tableContent.append("\t");
			tableContent.append(uname);
			tableContent.append("\t");
			// add dynamic user properties
			for (UserPropertyHandler propertyHandler : userPropertyHandlers) {
				String value = propertyHandler.getUserProperty(identity.getUser(), t.getLocale());
				tableContent.append((StringHelper.containsNonWhitespace(value) ? value : na));
				tableContent.append("\t");			
			}

			// create a identenv with no roles, no attributes, no locale
			IdentityEnvironment ienv = new IdentityEnvironment();
			ienv.setIdentity(identity);
			UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
			uce.getScoreAccounting().evaluateAll();
			AssessmentManager am = course.getCourseEnvironment().getAssessmentManager();

			Iterator iterNodes = myNodes.iterator();
			while (iterNodes.hasNext()) {
				AssessableCourseNode acnode = (AssessableCourseNode) iterNodes.next();
				boolean scoreOk = acnode.hasScoreConfigured();
				boolean passedOk = acnode.hasPassedConfigured();
				boolean attemptsOk = acnode.hasAttemptsConfigured();
				boolean commentOk = acnode.hasCommentConfigured();

				if (scoreOk || passedOk || commentOk || attemptsOk) {
					ScoreEvaluation se = uce.getScoreAccounting().getScoreEvaluation(acnode);
					boolean nodeColumnOk = false;
					StringBuilder tabs = new StringBuilder();

					if (scoreOk) {
						Float score = se.getScore();
						nodeColumnOk = true;
						tabs.append("\t"); // tabulators for header1 after node title

						if (firstIteration) {
							tableHeader2.append(sc);
							tableHeader2.append("\t");
						}

						if (score != null) {
							tableContent.append(AssessmentHelper.getRoundedScore(score));
							tableContent.append("\t");
						} else { // score == null
							tableContent.append(mi);
							tableContent.append("\t");
						}
					}

					if (passedOk) {
						Boolean passed = se.getPassed();
						nodeColumnOk = true;
						tabs.append("\t"); // tabulators for header1 after node title

						if (firstIteration) {
							tableHeader2.append(pa);
							tableHeader2.append("\t");
						}

						if (passed != null) {
							String yesno;
							if (passed.booleanValue()) {
								yesno = yes;
							} else {
								yesno = no;
							}
							tableContent.append(yesno);
							tableContent.append("\t");
						} else { // passed == null
							tableContent.append(mi);
							tableContent.append("\t");
						}
					}

					if (attemptsOk) {
						Integer attempts = am.getNodeAttempts(acnode, identity);
						int a = attempts.intValue();
						nodeColumnOk = true;
						tabs.append("\t"); // tabulators for header1 after node title

						if (firstIteration) {
							tableHeader2.append(at);
							tableHeader2.append("\t");
						}

						tableContent.append(a);
						tableContent.append("\t");
					}

					if (commentOk) {
					    // Comments for user
						String comment = am.getNodeComment(acnode, identity);
						nodeColumnOk = true;
						tabs.append("\t"); // tabulators for header1 after node title

						if (firstIteration) {
							tableHeader2.append(co);
							tableHeader2.append("\t");
						}

						if (comment != null) {
							// put comment between double quote in order to prevent that
							// '\t','\r' or '\n' destroy the excel table
							tableContent.append("\"");
							tableContent.append(comment);
							tableContent.append("\"\t");
						} else {
							tableContent.append(mi);
							tableContent.append("\t");
						}
						
						// Comments for tutors
						String coachComment = am.getNodeCoachComment(acnode, identity);
						tabs.append("\t"); // tabulators for header1 after node title

						if (firstIteration) {
							tableHeader2.append(cco);
							tableHeader2.append("\t");
						}

						if (coachComment != null) {
							// put coachComment between double quote in order to prevent that
							// '\t','\r' or '\n' destroy the excel table
							tableContent.append("\"");
							tableContent.append(coachComment);
							tableContent.append("\"\t");
						} else {
							tableContent.append(mi);
							tableContent.append("\t");
						}
						
						
					}

					if (firstIteration && nodeColumnOk) {
						String shortTitle = acnode.getShortTitle();

						tableHeader1.append(shortTitle);
						tableHeader1.append(tabs.toString());
					}

				}
			}
			if (firstIteration) {
				tableHeader1.append("\t\n");
				tableHeader2.append("\t\n");
			}
			tableContent.append("\t\n");
			firstIteration = false;
			rowNumber++;
		}

		table.append(tableHeader1);
		table.append(tableHeader2);
		table.append(tableContent);
		String tab = table.toString();

		return tab;
	}
    
	
	/**
	 * Load all users from all known learning groups into a list
	 * 
	 * @param courseEnv
	 * @return The list of identities from this course
	 */
	public static List loadUsers(CourseEnvironment courseEnv) {
		List identites = new ArrayList();
		CourseGroupManager gm = courseEnv.getCourseGroupManager();
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		List groups = gm.getAllLearningGroupsFromAllContexts();

		Iterator iter = groups.iterator();
		while (iter.hasNext()) {
			BusinessGroup group = (BusinessGroup) iter.next();
			SecurityGroup participants = group.getPartipiciantGroup();
			List ids = securityManager.getIdentitiesOfSecurityGroup(participants);
			identites.addAll(ids);
		}
		return identites;
	}
	
	
	
	/**
	 * Load all nodes which are assessable
	 * 
	 * @param courseEnv
	 * @return The list of assessable nodes from this course
	 */
	public static List loadAssessableNodes(CourseEnvironment courseEnv) {
		CourseNode rootNode = courseEnv.getRunStructure().getRootNode();
		List nodeList = new ArrayList();
		collectAssessableCourseNodes(rootNode, nodeList);

		return nodeList;
	}

	/**
	 * Collects recursively all assessable course nodes
	 * 
	 * @param node
	 * @param nodeList
	 */
	private static void collectAssessableCourseNodes(CourseNode node, List nodeList) {
		if (node instanceof AssessableCourseNode) {
			nodeList.add(node);
		}
		int count = node.getChildCount();
		for (int i = 0; i < count; i++) {
			CourseNode cn = (CourseNode) node.getChildAt(i);
			collectAssessableCourseNodes(cn, nodeList);
		}
	}
	

}
