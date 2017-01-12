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

package org.olat.course.archiver;

import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * @author schneider
 * Comment: Provides functionality to get a course results overview.
 */
public class ScoreAccountingHelper {
 
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
	public static String createCourseResultsOverviewTable(List<Identity> identities, List<AssessableCourseNode> myNodes, ICourse course, Locale locale) {
	  Translator t = Util.createPackageTranslator(ScoreAccountingArchiveController.class, locale);
	  StringBuilder tableHeader1 = new StringBuilder();
		StringBuilder tableHeader2 = new StringBuilder();
		StringBuilder tableContent = new StringBuilder();
		StringBuilder table = new StringBuilder();

		String sequentialNumber = t.translate("column.header.seqnum");
		String login = t.translate("column.header.businesspath");
		// user properties are dynamic
		String sc = t.translate("column.header.score");
		String pa = t.translate("column.header.passed");
		String co = t.translate("column.header.comment");
		String cco = t.translate("column.header.coachcomment");
		String at = t.translate("column.header.attempts");
		String il = t.translate("column.header.initialLaunchDate");
		String slm = t.translate("column.header.scoreLastModified");
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
		
			//Initial launch date
		tableHeader1.append(il).append("\t");
		tableHeader2.append("\t");
		
		List<UserPropertyHandler> userPropertyHandlers = UserManager.getInstance().getUserPropertyHandlersFor(
				ScoreAccountingHelper.class.getCanonicalName(), true);
		t = UserManager.getInstance().getPropertyHandlerTranslator(t);
		for (UserPropertyHandler propertyHandler : userPropertyHandlers) {
			tableHeader1.append(t.translate(propertyHandler.i18nColumnDescriptorLabelKey()));
			tableHeader1.append("\t");			
			tableHeader2.append("\t");
		}

		// preload user properties cache
		CourseEnvironment courseEnvironment = course.getCourseEnvironment();
		
		boolean firstIteration = true;
		int rowNumber = 1;

		UserCourseInformationsManager mgr = CoreSpringFactory.getImpl(UserCourseInformationsManager.class);
		OLATResource courseResource = courseEnvironment.getCourseGroupManager().getCourseResource();
		Map<Long,Date> firstTimes = mgr.getInitialLaunchDates(courseResource, identities);
		Formatter formatter = Formatter.getInstance(locale);

		int count = 0;
		for (Identity identity:identities) {
			ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(identity);
			String uname = BusinessControlFactory.getInstance().getAsURIString(Collections.singletonList(ce), false);

			tableContent.append(rowNumber);
			tableContent.append("\t");
			tableContent.append(uname);
			tableContent.append("\t");

			String initialLaunchDate = "";
			if(firstTimes.containsKey(identity.getKey())) {
				initialLaunchDate = formatter.formatDateAndTime(firstTimes.get(identity.getKey()));
			}
			tableContent.append(initialLaunchDate).append("\t");

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
			ScoreAccounting scoreAccount = uce.getScoreAccounting();
			scoreAccount.evaluateAll();
			AssessmentManager am = course.getCourseEnvironment().getAssessmentManager();

			for (AssessableCourseNode acnode:myNodes) {
				boolean scoreOk = acnode.hasScoreConfigured();
				boolean passedOk = acnode.hasPassedConfigured();
				boolean attemptsOk = acnode.hasAttemptsConfigured();
				boolean commentOk = acnode.hasCommentConfigured();

				if (scoreOk || passedOk || commentOk || attemptsOk) {
					ScoreEvaluation se = scoreAccount.evalCourseNode(acnode);
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
						int a = attempts == null ? 0 : attempts.intValue();
						nodeColumnOk = true;
						tabs.append("\t"); // tabulators for header1 after node title

						if (firstIteration) {
							tableHeader2.append(at);
							tableHeader2.append("\t");
						}

						tableContent.append(a);
						tableContent.append("\t");
					}

					if (firstIteration) {
						//last Modified
						tableHeader2.append(slm);
						tableHeader2.append("\t");
					}

					String scoreLastModified = "";
					Date lastModified = am.getScoreLastModifiedDate(acnode, identity);
					if(lastModified != null) {
						scoreLastModified = formatter.formatDateAndTime(lastModified);
					}
					tableContent.append(scoreLastModified);
					tableContent.append("\t");

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
							// A (double) quote must be represented by two (double) quotes.
							tableContent.append("\"");
							tableContent.append(comment.replace("\"", "\"\""));
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
							// A (double) quote must be represented by two (double) quotes.
							tableContent.append("\"");
							tableContent.append(coachComment.replace("\"", "\"\""));
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
			
			if(count++ % 20 == 0) {
				DBFactory.getInstance().commitAndCloseSession();
			}
		}
		//fxdiff VCRP-4: assessment overview with max score
		StringBuilder tableFooter = new StringBuilder();
		tableFooter.append("\t\n").append("\t\n").append(t.translate("legend")).append("\t\n").append("\t\n");
		for (AssessableCourseNode acnode:myNodes) {
			if (!acnode.hasScoreConfigured()) {
				// only show min/max/cut legend when score configured
				continue;
			}
			String minVal;
			String maxVal;
			String cutVal;
			if(acnode instanceof STCourseNode || !acnode.hasScoreConfigured()) {
				minVal = maxVal = cutVal = "-";
			} else {
				minVal = acnode.getMinScoreConfiguration() == null ? "-" : AssessmentHelper.getRoundedScore(acnode.getMinScoreConfiguration());
				maxVal = acnode.getMaxScoreConfiguration() == null ? "-" : AssessmentHelper.getRoundedScore(acnode.getMaxScoreConfiguration());
				if (acnode.hasPassedConfigured()) {
					cutVal = acnode.getCutValueConfiguration() == null ? "-" : AssessmentHelper.getRoundedScore(acnode.getCutValueConfiguration());
				} else {
					cutVal = "-";
				}
			}
			
			tableFooter.append('"');
			tableFooter.append(acnode.getShortTitle());
			tableFooter.append('"');
			tableFooter.append('\n');

			tableFooter.append("\t\t");
			tableFooter.append("minValue");
			tableFooter.append('\t');
			tableFooter.append(minVal);
			tableFooter.append('\n');

			tableFooter.append("\t\t");
			tableFooter.append("maxValue");
			tableFooter.append('\t');
			tableFooter.append(maxVal);
			tableFooter.append('\n');

			tableFooter.append("\t\t");
			tableFooter.append("cutValue");
			tableFooter.append('\t');
			tableFooter.append(cutVal);
			tableFooter.append('\n');
		}

		table.append(tableHeader1);
		table.append(tableHeader2);
		table.append(tableContent);
		table.append(tableFooter);
		String tab = table.toString();

		return tab;
	}
	
	public static void createCourseResultsOverviewXMLTable(List<Identity> identities, List<AssessableCourseNode> myNodes, ICourse course, Locale locale, OutputStream bos) {
		OpenXMLWorkbook workbook = new OpenXMLWorkbook(bos, 1);
		OpenXMLWorksheet sheet = workbook.nextWorksheet();
		int headerColCnt = 0;
		Translator t = Util.createPackageTranslator(ScoreAccountingArchiveController.class, locale);

		String sequentialNumber = t.translate("column.header.seqnum");
		String login = t.translate("column.header.businesspath");
		// user properties are dynamic
		String sc = t.translate("column.header.score");
		String pa = t.translate("column.header.passed");
		String co = t.translate("column.header.comment");
		String cco = t.translate("column.header.coachcomment");
		String at = t.translate("column.header.attempts");
		String il = t.translate("column.header.initialLaunchDate");
		String slm = t.translate("column.header.scoreLastModified");
		String na = t.translate("column.field.notavailable");
		String mi = t.translate("column.field.missing");
		String yes = t.translate("column.field.yes");
		String no = t.translate("column.field.no");
		String submitted = t.translate("column.field.submitted");

		AssessableCourseNode firstAcnode = myNodes.get(0);
		boolean scoreOk = firstAcnode.hasScoreConfigured();
		boolean passedOk = firstAcnode.hasPassedConfigured();
		boolean attemptsOk = firstAcnode.hasAttemptsConfigured();
		boolean commentOk = firstAcnode.hasCommentConfigured();
		
		Row headerRow1 = sheet.newRow();
		headerRow1.addCell(headerColCnt++, sequentialNumber);
		headerRow1.addCell(headerColCnt++, login);
		//Initial launch date
		headerRow1.addCell(headerColCnt++, il);
		// get user property handlers for this export, translate using the fallback
		// translator configured in the property handler
		List<UserPropertyHandler> userPropertyHandlers = UserManager.getInstance().getUserPropertyHandlersFor(
				ScoreAccountingHelper.class.getCanonicalName(), true);
		t = UserManager.getInstance().getPropertyHandlerTranslator(t);
		for (UserPropertyHandler propertyHandler : userPropertyHandlers) {
			headerRow1.addCell(headerColCnt++, t.translate(propertyHandler.i18nColumnDescriptorLabelKey()));
		}
		if (scoreOk || passedOk || commentOk || attemptsOk)
			headerRow1.addCell(headerColCnt, firstAcnode.getShortTitle());

		Row headerRow2 = sheet.newRow();
		if (firstAcnode.getType().equals("ita")) {
			headerRow2.addCell(headerColCnt++, submitted);
		}

		if (scoreOk)
			headerRow2.addCell(headerColCnt++, sc);
		if (passedOk)
			headerRow2.addCell(headerColCnt++, pa);
		if (attemptsOk)
			headerRow2.addCell(headerColCnt++, at);
		headerRow2.addCell(headerColCnt++, slm);
		if (commentOk) {
			headerRow2.addCell(headerColCnt++, co);
			headerRow2.addCell(headerColCnt++, cco);
		}
		sheet.setHeaderRows(2);

		// preload user properties cache
		CourseEnvironment courseEnvironment = course.getCourseEnvironment();

		int rowNumber = 0;

		UserCourseInformationsManager mgr = CoreSpringFactory.getImpl(UserCourseInformationsManager.class);
		OLATResource courseResource = courseEnvironment.getCourseGroupManager().getCourseResource();
		Map<Long,Date> firstTimes = mgr.getInitialLaunchDates(courseResource, identities);

		for (Identity identity:identities) {
			Row dataRow = sheet.newRow();
			int dataColCnt = 0;
			ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(identity);
			String uname = BusinessControlFactory.getInstance().getAsURIString(Collections.singletonList(ce), false);

			dataRow.addCell(dataColCnt++, ++rowNumber, null);
			dataRow.addCell(dataColCnt++, uname, null);

			if(firstTimes.containsKey(identity.getKey())) {
				dataRow.addCell(dataColCnt++, firstTimes.get(identity.getKey()), workbook.getStyles().getDateStyle());
			} else {
				dataRow.addCell(dataColCnt++, mi);
			}

			// add dynamic user properties
			for (UserPropertyHandler propertyHandler : userPropertyHandlers) {
				String value = propertyHandler.getUserProperty(identity.getUser(), t.getLocale());
				dataRow.addCell(dataColCnt++, (StringHelper.containsNonWhitespace(value) ? value : na));
			}

			// create a identenv with no roles, no attributes, no locale
			IdentityEnvironment ienv = new IdentityEnvironment();
			ienv.setIdentity(identity);
			UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
			ScoreAccounting scoreAccount = uce.getScoreAccounting();
			scoreAccount.evaluateAll();
			AssessmentManager am = course.getCourseEnvironment().getAssessmentManager();

			for (AssessableCourseNode acnode:myNodes) {
				scoreOk = acnode.hasScoreConfigured();
				passedOk = acnode.hasPassedConfigured();
				attemptsOk = acnode.hasAttemptsConfigured();
				commentOk = acnode.hasCommentConfigured();

				if (acnode.getType().equals("ita")) {
					String log = acnode.getUserLog(uce);
					String date = null;
					Date lastUploaded = null;
					try {
						log = log.toLowerCase();
						log = log.substring(0, log.lastIndexOf("submit"));
						log = log.substring(log.lastIndexOf("date:"));
						date = log.split("\n")[0].substring(6);
						DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
						lastUploaded = df.parse(date);
					} catch (Exception e) {
					}
					if (lastUploaded != null) {
						dataRow.addCell(dataColCnt++, lastUploaded, workbook.getStyles().getDateStyle());
					} else { // date == null
						dataRow.addCell(dataColCnt++, mi);
					}
				}

				if (scoreOk || passedOk || commentOk || attemptsOk) {
					ScoreEvaluation se = scoreAccount.evalCourseNode(acnode);

					if (scoreOk) {
						Float score = se.getScore();
						if (score != null) {
							dataRow.addCell(dataColCnt++, AssessmentHelper.getRoundedScore(score), null);
						} else { // score == null
							dataRow.addCell(dataColCnt++, mi);
						}
					}

					if (passedOk) {
						Boolean passed = se.getPassed();
						if (passed != null) {
							String yesno;
							if (passed.booleanValue()) {
								yesno = yes;
							} else {
								yesno = no;
							}
							dataRow.addCell(dataColCnt++, yesno);
						} else { // passed == null
							dataRow.addCell(dataColCnt++, mi);
						}
					}

					if (attemptsOk) {
						Integer attempts = am.getNodeAttempts(acnode, identity);
						int a = attempts == null ? 0 : attempts.intValue();
						dataRow.addCell(dataColCnt++, a, null);
					}

					Date lastModified = am.getScoreLastModifiedDate(acnode, identity);
					if(lastModified != null) {
						dataRow.addCell(dataColCnt++, lastModified, workbook.getStyles().getDateStyle());
					} else {
						dataRow.addCell(dataColCnt++, mi);
					}

					if (commentOk) {
						// Comments for user
						String comment = am.getNodeComment(acnode, identity);
						if (comment != null) {
							dataRow.addCell(dataColCnt++, comment);
						} else {
							dataRow.addCell(dataColCnt++, mi);
						}

						// Comments for tutors
						String coachComment = am.getNodeCoachComment(acnode, identity);
						if (coachComment != null) {
							dataRow.addCell(dataColCnt++, coachComment);
						} else {
							dataRow.addCell(dataColCnt++, mi);
						}
					}
				}
			}
		}
		
		IOUtils.closeQuietly(workbook);
	}
    
	
	/**
	 * Load all users from all known learning groups into a list
	 * 
	 * @param courseEnv
	 * @return The list of identities from this course
	 */
	public static List<Identity> loadUsers(CourseEnvironment courseEnv) {
		CourseGroupManager gm = courseEnv.getCourseGroupManager();
		List<BusinessGroup> groups = gm.getAllBusinessGroups();
		
		BusinessGroupService businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		Set<Identity> userSet = new HashSet<>(businessGroupService.getMembers(groups, GroupRoles.participant.name()));
		RepositoryEntry re = gm.getCourseEntry();
		if(re != null) {
			RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
			userSet.addAll(repositoryService.getMembers(re, GroupRoles.participant.name()));
		}

		List<Identity> assessedList = courseEnv.getCoursePropertyManager().getAllIdentitiesWithCourseAssessmentData(userSet);
		if(assessedList.size() > 0) {
			userSet.addAll(assessedList);
		}
		return new ArrayList<Identity>(userSet);
	}
	
	public static List<Identity> loadUsers(CourseEnvironment courseEnv, ArchiveOptions options) {
		List<Identity> users;
		if(options == null) {
			users = loadUsers(courseEnv);
		} else if(options.getGroup() != null) {
			BusinessGroupService businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
			users = businessGroupService.getMembers(options.getGroup(), GroupRoles.participant.name());
		} else if(options.getIdentities() != null) {
			users = options.getIdentities();
		} else {
			users = loadUsers(courseEnv);
		}
		return users;
	}
	
	/**
	 * Load all nodes which are assessable
	 * 
	 * @param courseEnv
	 * @return The list of assessable nodes from this course
	 */
	public static List<AssessableCourseNode> loadAssessableNodes(CourseEnvironment courseEnv) {
		CourseNode rootNode = courseEnv.getRunStructure().getRootNode();
		List<AssessableCourseNode> nodeList = new ArrayList<AssessableCourseNode>();
		collectAssessableCourseNodes(rootNode, nodeList);
		return nodeList;
	}

	/**
	 * Collects recursively all assessable course nodes
	 * 
	 * @param node
	 * @param nodeList
	 */
	private static void collectAssessableCourseNodes(CourseNode node, List<AssessableCourseNode> nodeList) {
		if (node instanceof AssessableCourseNode) {
			nodeList.add((AssessableCourseNode)node);
		}
		int count = node.getChildCount();
		for (int i = 0; i < count; i++) {
			CourseNode cn = (CourseNode) node.getChildAt(i);
			collectAssessableCourseNodes(cn, nodeList);
		}
	}
	

}
