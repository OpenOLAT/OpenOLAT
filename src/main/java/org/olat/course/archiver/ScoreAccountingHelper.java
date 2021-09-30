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

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
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
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * @author schneider
 * Comment: Provides functionality to get a course results overview.
 */
public class ScoreAccountingHelper {
	
	private static final Logger log = Tracing.createLoggerFor(ScoreAccountingHelper.class);
	
	public static void createCourseResultsOverview(List<Identity> identities, List<CourseNode> nodes, ICourse course, Locale locale, ZipOutputStream zout) {
		try(OutputStream out = new ShieldOutputStream(zout)) {
			zout.putNextEntry(new ZipEntry("Course_results.xlsx"));
			createCourseResultsOverviewXMLTable(identities, nodes, course, locale, out);
			zout.closeEntry();
		} catch(IOException e) {
			log.error("", e);
		}

		for(CourseNode node:nodes) {
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
				DBFactory.getInstance().commitAndCloseSession();
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
	public static void createCourseResultsOverviewXMLTable(List<Identity> identities, List<CourseNode> myNodes, ICourse course, Locale locale, OutputStream bos) {
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(bos, 1)) {
			createCourseResultsOverviewXMLTable(identities, myNodes, course, locale, workbook);
		} catch(Exception e) {
			log.error("", e);
		}
	}
	
	private static void createCourseResultsOverviewXMLTable(List<Identity> identities, List<CourseNode> myNodes, ICourse course, Locale locale, OpenXMLWorkbook workbook) {
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		OpenXMLWorksheet sheet = workbook.nextWorksheet();
		sheet.setHeaderRows(2);
		
		int headerColCnt = 0;
		Translator t = Util.createPackageTranslator(ScoreAccountingArchiveController.class, locale);

		boolean obligationOk = NodeAccessType.of(course).getType().equals(LearningPathNodeAccessProvider.TYPE);
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
		String obli = t.translate("column.header.obligation");
		String na = t.translate("column.field.notavailable");
		String mi = t.translate("column.field.missing");
		String yes = t.translate("column.field.yes");
		String no = t.translate("column.field.no");
		String submitted = t.translate("column.field.submitted");
		String oblim = t.translate("column.field.mandatory");
		String oblio = t.translate("column.field.optional");
		String oblie = t.translate("column.field.excluded");

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
		for(CourseNode acNode:myNodes) {
			headerRow1.addCell(header1ColCnt++, acNode.getShortTitle());
			header1ColCnt += acNode.getType().equals("ita") ? 1 : 0;
			
			AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(acNode);
			boolean scoreOk = Mode.none != assessmentConfig.getScoreMode();
			boolean passedOk = Mode.none != assessmentConfig.getPassedMode();
			boolean attemptsOk = assessmentConfig.hasAttempts();
			boolean commentOk = assessmentConfig.hasComment();
			if (scoreOk || passedOk || commentOk || attemptsOk) {
				header1ColCnt += scoreOk ? 1 : 0;
				header1ColCnt += passedOk ? 1 : 0;
				header1ColCnt += attemptsOk ? 1 : 0;
				header1ColCnt += obligationOk ? 1 : 0;
				header1ColCnt++;//last modified
				header1ColCnt += commentOk ? 1 : 0;
				header1ColCnt++;//coach comment
			}
			header1ColCnt--;//column title
		}

		int header2ColCnt = headerColCnt;
		Row headerRow2 = sheet.newRow();
		for(CourseNode acNode:myNodes) {
			if (acNode.getType().equals("ita")) {
				headerRow2.addCell(header2ColCnt++, submitted);
			}
			
			AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(acNode);
			boolean scoreOk = Mode.none != assessmentConfig.getScoreMode();
			boolean passedOk = Mode.none != assessmentConfig.getPassedMode();
			boolean attemptsOk = assessmentConfig.hasAttempts();
			boolean commentOk = assessmentConfig.hasComment();
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
				if(obligationOk) {
					headerRow2.addCell(header2ColCnt++, obli);
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

			for (CourseNode acnode:myNodes) {
				AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(acnode);
				boolean scoreOk = Mode.none != assessmentConfig.getScoreMode();
				boolean passedOk = Mode.none != assessmentConfig.getPassedMode();
				boolean attemptsOk = assessmentConfig.hasAttempts();
				boolean commentOk = assessmentConfig.hasComment();

				if (acnode.getType().equals("ita")) {
					String log = courseAssessmentService.getAuditLog(acnode, uce);
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
					AssessmentEvaluation se = scoreAccount.evalCourseNode(acnode);

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
						int a = se.getAttempts() == null ? 0 : se.getAttempts().intValue();
						dataRow.addCell(dataColCnt++, a, null);
					}
					
					if (obligationOk) {
						if (se.getObligation() != null && se.getObligation().getCurrent() != null) {
							switch (se.getObligation().getCurrent()) {
							case mandatory:
								dataRow.addCell(dataColCnt++, oblim); break;
							case optional:
								dataRow.addCell(dataColCnt++, oblio); break;
							case excluded:
								dataRow.addCell(dataColCnt++, oblie); break;
							default:
								break;
							}
						} else {
							dataRow.addCell(dataColCnt++, mi);
						}
					}

					if(se.getLastModified() != null) {
						dataRow.addCell(dataColCnt++, se.getLastModified(), workbook.getStyles().getDateStyle());
					} else {
						dataRow.addCell(dataColCnt++, mi);
					}

					if (commentOk) {
						// Comments for user
						if (se.getComment() != null) {
							dataRow.addCell(dataColCnt++, se.getComment());
						} else {
							dataRow.addCell(dataColCnt++, mi);
						}
					}

					// Always export comments for tutors
					if (se.getCoachComment() != null) {
						dataRow.addCell(dataColCnt++, se.getCoachComment());
					} else {
						dataRow.addCell(dataColCnt++, mi);
					}
				}
			}
			DBFactory.getInstance().commitAndCloseSession();
		}

		//min. max. informations
		boolean first = true;
		for (CourseNode acnode:myNodes) {
			AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(acnode);
			if (Mode.none == assessmentConfig.getScoreMode()) {
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
			if(Mode.setByNode != assessmentConfig.getScoreMode()) {
				minVal = maxVal = cutVal = "-";
			} else {
				Float minScoreConfig = assessmentConfig.getMinScore();
				Float maxScoreConfig = assessmentConfig.getMaxScore();
				minVal = minScoreConfig == null ? "-" : AssessmentHelper.getRoundedScore(minScoreConfig);
				maxVal = maxScoreConfig == null ? "-" : AssessmentHelper.getRoundedScore(maxScoreConfig);
				if (Mode.none != assessmentConfig.getPassedMode()) {
					Float cutValueConfig = assessmentConfig.getCutValue();
					cutVal = cutValueConfig == null ? "-" : AssessmentHelper.getRoundedScore(cutValueConfig);
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
	}
	
	
	/**
	 * Load all participant from all known learning groups into a list
	 * 
	 * @param courseEnv
	 * @return The list of participants from this course
	 */
	public static List<Identity> loadParticipants(CourseEnvironment courseEnv) {
		CourseGroupManager gm = courseEnv.getCourseGroupManager();
		RepositoryEntry re = gm.getCourseEntry();
		Set<Identity> userSet = new HashSet<>();
		if(re != null) {
			RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
			userSet.addAll(repositoryService.getMembers(re, RepositoryEntryRelationType.all, GroupRoles.participant.name()));
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
	public static List<CourseNode> loadAssessableNodes(CourseEnvironment courseEnv) {
		CourseNode rootNode = courseEnv.getRunStructure().getRootNode();
		List<CourseNode> nodeList = new ArrayList<>();
		collectAssessableCourseNodes(rootNode, nodeList);
		return nodeList;
	}

	/**
	 * Collects recursively all assessable course nodes
	 * 
	 * @param node
	 * @param nodeList
	 */
	private static void collectAssessableCourseNodes(CourseNode node, List<CourseNode> nodeList) {
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		if (courseAssessmentService.getAssessmentConfig(node).isAssessable()) {
			nodeList.add(node);
		}
		int count = node.getChildCount();
		for (int i = 0; i < count; i++) {
			CourseNode cn = (CourseNode) node.getChildAt(i);
			collectAssessableCourseNodes(cn, nodeList);
		}
	}
	

}
