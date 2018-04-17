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

import java.io.File;
import java.io.IOException;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.io.ShieldOutputStream;
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
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.MSCourseNode;
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
	
	private static final OLog log = Tracing.createLoggerFor(ScoreAccountingHelper.class);
	
	public static void createCourseResultsOverview(List<Identity> identities, List<AssessableCourseNode> nodes, ICourse course, Locale locale, ZipOutputStream zout) {
		try(OutputStream out = new ShieldOutputStream(zout)) {
			zout.putNextEntry(new ZipEntry("Course_results.xlsx"));
			createCourseResultsOverviewXMLTable(identities, nodes, course, locale, out);
			zout.closeEntry();
		} catch(IOException e) {
			log.error("", e);
		}

		for(AssessableCourseNode node:nodes) {
			String dir = "Assessment_documents/" + StringHelper.transformDisplayNameToFileSystemName(node.getShortName());
			if(node instanceof IQTESTCourseNode
					|| node.getModuleConfiguration().getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_INDIVIDUAL_ASSESSMENT_DOCS, false)) {
				for(Identity assessedIdentity:identities) {
					List<File> assessmentDocuments = course.getCourseEnvironment()
							.getAssessmentManager().getIndividualAssessmentDocuments(node, assessedIdentity);
					if(assessmentDocuments != null && !assessmentDocuments.isEmpty()) {
						String name = assessedIdentity.getUser().getLastName()
								+ "_" + assessedIdentity.getUser().getFirstName()
								+ "_" + assessedIdentity.getName();
						String userDirName = dir + "/" + StringHelper.transformDisplayNameToFileSystemName(name);
						for(File document:assessmentDocuments) {
							String path = userDirName + "/" + document.getName(); 
							ZipUtil.addFileToZip(path, document, zout);
						}
					}
				}
			}
		}
	}
	
	/**
	 * The results from assessable nodes are written to one row per user into an excel-sheet. An
     * assessable node will only appear if it is producing at least one of the
     * following variables: score, passed, attempts, comments.
     * 
	 * @param identities The list of identities which results need to be archived.
	 * @param myNodes The assessable nodes to archive.
	 * @param course The course.
	 * @param locale The locale.
	 * @param bos The output stream (which will be closed at the end, if you use a zip stream don't forget to shield it).
	 */
	public static void createCourseResultsOverviewXMLTable(List<Identity> identities, List<AssessableCourseNode> myNodes, ICourse course, Locale locale, OutputStream bos) {
		OpenXMLWorkbook workbook = new OpenXMLWorkbook(bos, 1);
		OpenXMLWorksheet sheet = workbook.nextWorksheet();
		sheet.setHeaderRows(2);
		
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
		
		int header1ColCnt = headerColCnt;
		for(AssessableCourseNode acNode:myNodes) {
			headerRow1.addCell(header1ColCnt++, acNode.getShortTitle());
			header1ColCnt += acNode.getType().equals("ita") ? 1 : 0;
			
			boolean scoreOk = acNode.hasScoreConfigured();
			boolean passedOk = acNode.hasPassedConfigured();
			boolean attemptsOk = acNode.hasAttemptsConfigured();
			boolean commentOk = acNode.hasCommentConfigured();
			if (scoreOk || passedOk || commentOk || attemptsOk) {
				header1ColCnt += scoreOk ? 1 : 0;
				header1ColCnt += passedOk ? 1 : 0;
				header1ColCnt += attemptsOk ? 1 : 0;
				header1ColCnt++;//last modified
				header1ColCnt += commentOk ? 1 : 0;
				header1ColCnt++;//coach comment
			}
			header1ColCnt--;//column title
		}

		int header2ColCnt = headerColCnt;
		Row headerRow2 = sheet.newRow();
		for(AssessableCourseNode acNode:myNodes) {
			if (acNode.getType().equals("ita")) {
				headerRow2.addCell(header2ColCnt++, submitted);
			}
			
			boolean scoreOk = acNode.hasScoreConfigured();
			boolean passedOk = acNode.hasPassedConfigured();
			boolean attemptsOk = acNode.hasAttemptsConfigured();
			boolean commentOk = acNode.hasCommentConfigured();
			if (scoreOk || passedOk || commentOk || attemptsOk) {
				if(scoreOk) {
					headerRow2.addCell(header2ColCnt++, sc);
				}
				if(passedOk) {
					headerRow2.addCell(header2ColCnt++, pa);
				}
				if(attemptsOk) {
					headerRow2.addCell(header2ColCnt++, at);
				}
				headerRow2.addCell(header2ColCnt++, slm);//last modified
				if (commentOk) {
					headerRow2.addCell(header2ColCnt++, co);
				}
				headerRow2.addCell(header2ColCnt++, cco);//coach comment
			}
		}
		

		// preload user properties cache
		CourseEnvironment courseEnvironment = course.getCourseEnvironment();

		int rowNumber = 0;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
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
				boolean scoreOk = acnode.hasScoreConfigured();
				boolean passedOk = acnode.hasPassedConfigured();
				boolean attemptsOk = acnode.hasAttemptsConfigured();
				boolean commentOk = acnode.hasCommentConfigured();

				if (acnode.getType().equals("ita")) {
					String log = acnode.getUserLog(uce);
					String date = null;
					Date lastUploaded = null;
					try {
						log = log.toLowerCase();
						log = log.substring(0, log.lastIndexOf("submit"));
						log = log.substring(log.lastIndexOf("date:"));
						date = log.split("\n")[0].substring(6);
						lastUploaded = df.parse(date);
					} catch (Exception e) {
						//
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
					}

					// Always export comments for tutors
					String coachComment = am.getNodeCoachComment(acnode, identity);
					if (coachComment != null) {
						dataRow.addCell(dataColCnt++, coachComment);
					} else {
						dataRow.addCell(dataColCnt++, mi);
					}
				}
			}
		}

		//min. max. informations
		boolean first = true;
		for (AssessableCourseNode acnode:myNodes) {
			if (!acnode.hasScoreConfigured()) {
				// only show min/max/cut legend when score configured
				continue;
			}
			
			if(first) {
				sheet.newRow().addCell(0, "");
				sheet.newRow().addCell(0, "");
				sheet.newRow().addCell(0, t.translate("legend"));
				sheet.newRow().addCell(0, "");
				first = false;
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
			
			sheet.newRow().addCell(0, acnode.getShortTitle());

			Row minRow = sheet.newRow();
			minRow.addCell(2, "minValue");
			minRow.addCell(3, minVal);
			Row maxRow = sheet.newRow();
			maxRow.addCell(2, "maxValue");
			maxRow.addCell(3, maxVal);
			Row cutRow = sheet.newRow();
			cutRow.addCell(2, "cutValue");
			cutRow.addCell(3, cutVal);
		}
		
		IOUtils.closeQuietly(workbook);
	}
	
	
	/**
	 * Load all participant from all known learning groups into a list
	 * 
	 * @param courseEnv
	 * @return The list of participants from this course
	 */
	public static List<Identity> loadParticipants(CourseEnvironment courseEnv) {
		CourseGroupManager gm = courseEnv.getCourseGroupManager();
		List<BusinessGroup> groups = gm.getAllBusinessGroups();
		
		BusinessGroupService businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		Set<Identity> userSet = new HashSet<>(businessGroupService.getMembers(groups, GroupRoles.participant.name()));
		RepositoryEntry re = gm.getCourseEntry();
		if(re != null) {
			RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
			userSet.addAll(repositoryService.getMembers(re, GroupRoles.participant.name()));
		}
		return new ArrayList<>(userSet);
	}
    
	
	/**
	 * Load all users from all known learning groups into a list
	 * 
	 * @param courseEnv
	 * @return The list of identities from this course
	 */
	public static List<Identity> loadUsers(CourseEnvironment courseEnv) {
		List<Identity> participants = loadParticipants(courseEnv);
		Set<Identity> userSet = new HashSet<>(participants);
		List<Identity> assessedList = courseEnv.getCoursePropertyManager().getAllIdentitiesWithCourseAssessmentData(userSet);
		if(!assessedList.isEmpty()) {
			userSet.addAll(assessedList);
		}
		return new ArrayList<>(userSet);
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
