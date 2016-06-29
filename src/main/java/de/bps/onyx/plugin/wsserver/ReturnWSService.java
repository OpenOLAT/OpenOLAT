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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.onyx.plugin.wsserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentNotificationsHandler;
import org.olat.course.assessment.NewCachePersistingAssessmentManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.QTIResultSet;

import de.bps.onyx.plugin.OnyxResultManager;

/**
 * OLATCE-1322 Switched from AXIS2 based WebService to JAXWS / Annotations-based
 * implementation for the ReturnWSService
 * 
 * This WebService could be used by the web-onyx-player to save the results for
 * qti2.1 tests in olat.
 * 
 * 
 */
@WebService(name = "RenderServices", serviceName = "ReturnWSService", targetNamespace = "http://test.plugin.bps.de/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class ReturnWSService {

	private static final String NONEXISTING_RESULT = "The uniqueId does not reference a valid qti-result-set: ";

	private static final String MSG_MISSING_RESULT = "Missing or empty result file-data";

	private final static OLog log = Tracing.createLoggerFor(ReturnWSService.class);

	private final static String FULLY_ASSESSED = "fullyassessed";

	private final static String SUSPENDED = "attemptSuspended";

	private static File errorFolder;

	private static void init() {
		try {
			File tmp = new File(WebappHelper.getUserDataRoot());
			tmp = new File(tmp, "resreportingErrorFiles");
			if (!tmp.exists()) {
				tmp.mkdir();
			}
			errorFolder = tmp;
		} catch (NullPointerException npe) {
			log.error("Unable to set errorFolder yet, try again later");
		}
	}

	@WebMethod(operationName = "saveResult2")
	public void saveResult(@WebParam(name = "uniqueId") String uniqueId, @WebParam(name = "resultFile") byte[] resultFile, @WebParam(name = "params") MapWrapper params)
			throws Exception {
		
		File temp = null;
		try {

			if (resultFile == null || resultFile.length < 1) {
				log.error(MSG_MISSING_RESULT);
				throw new IllegalArgumentException(MSG_MISSING_RESULT);
			}

			// file.createtempfile() is not correctly interpreted as an archive
			// even if the isArchive() method says so
			temp = new File(System.getProperty("java.io.tmpdir"), java.io.File.separatorChar + this.hashCode() + "_" + new Date().getTime() + ".zip");

			FileOutputStream out = new FileOutputStream(temp);
			out.write(resultFile);
			out.flush();
			out.close();

			QTIResultSet qtiResultSet = OnyxResultManager.getResultSet(Long.parseLong(uniqueId));
			if (qtiResultSet == null) {
				log.error(NONEXISTING_RESULT + uniqueId);
				throw new IllegalArgumentException(NONEXISTING_RESULT + uniqueId);
			}

			if (qtiResultSet.getOlatResource() == OnyxResultManager.IGNORE_PREVIEW_CASE) {
				// delete the temp-result previews or calls from the
				// learning-resource-tab
				log.info("Delete resultset for preview-call: " + uniqueId);
				QTIResultManager.getInstance().deleteResults(qtiResultSet);
				DBFactory.getInstance().commitAndCloseSession();
			} else {
				boolean changed = false;
				if (params != null) {
					Map<String, String> parameterMap = params.getMap();
					if (parameterMap != null) {
						if (log.isDebug()) {
							log.debug("Update properties for assessmentId : " + qtiResultSet.getAssessmentID());
						}
						boolean suspensionBlock = false;
						for (String key : params.getMap().keySet()) {
							// extract information if this result had been fully
							// assessed
							if (FULLY_ASSESSED.equalsIgnoreCase(key)) {
								changed = true;
								String accessedString = parameterMap.get(key);
								if (accessedString != null && accessedString.length() > 0) {
									Boolean accessed = Boolean.parseBoolean(accessedString);
									qtiResultSet.setFullyAssessed(accessed);
								} else {
									log.error("Got accessed-parameter but it was empty : " + accessedString);
								}
							} else if (SUSPENDED.equals(key)) {
								changed = true;
								String suspensionString = parameterMap.get(key);
								if (suspensionString != null && suspensionString.length() > 0) {
									suspensionBlock = Boolean.parseBoolean(suspensionString);
									qtiResultSet.setSuspended(suspensionBlock);
									if (suspensionBlock) {
										if (log.isDebug()) {
											log.debug("Testrun had been suspended, will not update UserScoreEvaluation");
										}
									}
								} else {
									log.error("Got suspension-parameter but it was empty : " + suspensionString);
								}
							} else if (OnyxResultManager.SCORE.equalsIgnoreCase(key)) {
								changed = true;
								String scoreString = parameterMap.get(key);
								if (scoreString != null && scoreString.length() > 0) {
									Float score = Float.parseFloat(scoreString);
									qtiResultSet.setScore(score);
								} else {
									log.error("Got score-parameter but it was empty : " + scoreString);
								}
							} else if (OnyxResultManager.PASS.equalsIgnoreCase(key)) {
								changed = true;
								String passedString = parameterMap.get(key);
								if (passedString != null && passedString.length() > 0) {
									Boolean passed = Boolean.parseBoolean(passedString);
									qtiResultSet.setIsPassed(passed);
								} else {
									log.error("Got passed-parameter but it was empty : " + passedString);
								}
							} else {
								if (log.isDebug()) {
									log.debug("Got unhandled parameter: " + key + " with value " + parameterMap.get(key));
								}
							}
						}

						if (changed) {
							DBFactory.getInstance().updateObject(qtiResultSet);
							DBFactory.getInstance().commitAndCloseSession();
							qtiResultSet = (QTIResultSet) DBFactory.getInstance().loadObject(qtiResultSet);
							if (!suspensionBlock && OnyxResultManager.isLastTestTry(qtiResultSet)) {
								Identity assessedIdentity = qtiResultSet.getIdentity();
								Long resourceId = qtiResultSet.getOlatResource(); // this
																					// is
																					// the
																					// courseId
																					// and
																					// not
																					// the
																					// resourceId
																					// of
																					// the
																					// testResource

								ICourse course = CourseFactory.loadCourse(resourceId);

								CourseNode courseNode = course.getRunStructure().getNode(qtiResultSet.getOlatResourceDetail());
								NewCachePersistingAssessmentManager am = (NewCachePersistingAssessmentManager) NewCachePersistingAssessmentManager.getInstance(course);

								CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
								// create an identenv with no roles, no
								// attributes, no locale
								IdentityEnvironment ienv = new IdentityEnvironment();
								ienv.setIdentity(assessedIdentity);
								UserCourseEnvironment userCourseEnvironment = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());

								ScoreEvaluation scoreEvaluation = new ScoreEvaluation(qtiResultSet.getScore(), qtiResultSet.getIsPassed(), qtiResultSet.getFullyAssessed(),
										qtiResultSet.getAssessmentID());
								am.syncAndsaveScoreEvaluation(courseNode, assessedIdentity, assessedIdentity, scoreEvaluation, false, userCourseEnvironment, cpm);

								AssessmentNotificationsHandler.getInstance().markPublisherNews(assessedIdentity, resourceId);
							}
						} else {
							if (log.isDebug()) {
								log.debug("nothing new " + (changed) + " or suspended " + suspensionBlock);
							}
						}
					}
				} else {
					if (log.isDebug()) {
						log.debug("Found no parameters");
					}
				}

				if (temp == null || temp.getAbsolutePath() == null) {
					log.error("unauthorized request: saveResultLocal.getUniqueId()=" + uniqueId + " saveResultLocal.getResultLocalFile()=" + temp);
					throw new java.lang.UnsupportedOperationException("unauthorized request, this event will be logged");
				} else {
					OnyxResultManager.persistOnyxResults(qtiResultSet, temp.getAbsolutePath());
				}
			}
			// delete temp-file as last step, if an error occurred than this
			// step is not reached and the file will be copied to the
			// error-handling directory in the exception-handling
			temp.delete();
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
			throw e;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			log.error("Error while saving results.", e);
			if (temp != null && temp.exists()) {
				if (errorFolder == null) {
					init();
				}
				try {
					File outFile = new File(errorFolder, uniqueId + "_" + System.currentTimeMillis() + ".zip");
					FileUtils.copyFileToFile(temp, outFile, false);
					StringBuilder builder = new StringBuilder();
					builder.append("Secured file to ").append(outFile.getAbsolutePath()).append(" uniqueId/assessmentId ").append(uniqueId).append(" parameters : ");
					HashMap<String, String> parameterMap = params.getMap();
					if (parameterMap != null) {
						builder.append(parameterMap.toString());
					}
					log.info(builder.toString());
				} catch (Exception subE) {
					log.error("Unable to safe error-fallback for resultfile !", subE);
				}
				temp.delete();
			} else {
				log.warn("unable to save inexistend file");
			}
			throw e;
		}
		if (log.isDebug()) {
			log.debug("leave ReturnWSService");
		}
	}

	@WebMethod
	public void saveResult(@WebParam(name = "uniqueId") String uniqueId, @WebParam(name = "resultFile") byte[] resultFile) throws Exception {
		saveResult(uniqueId, resultFile, null);
	}

	@WebMethod
	public void saveResultLocal(@WebParam(name = "uniqueId") String uniqueId, @WebParam(name = "resultLocalFile") String resultLocalFile) {
		QTIResultSet qtiResultSet = OnyxResultManager.getResultSet(Long.parseLong(uniqueId));

		if (resultLocalFile == null) {
			log.error("unauthorized request: saveResultLocal.getUniqueId()=" + uniqueId + " saveResultLocal.getResultLocalFile()=" + resultLocalFile);
			throw new java.lang.UnsupportedOperationException("unauthorized request, this event will be logged");
		} else {
			OnyxResultManager.persistOnyxResults(qtiResultSet, resultLocalFile);
		}
		return;
	}
}
