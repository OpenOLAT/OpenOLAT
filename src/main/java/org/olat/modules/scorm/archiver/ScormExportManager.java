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
package org.olat.modules.scorm.archiver;

import java.util.List;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.course.archiver.ScoreAccountingHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ScormCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.scorm.ScormDirectoryHelper;
import org.olat.modules.scorm.server.servermodels.ScoDocument;


public class ScormExportManager {
	private static final String CMI_OBJECTIVES = "cmi.objectives.";
	private static final String CMI_INTERACTIONS = "cmi.interactions.";
	private static final String CMI_RAW_SCORE = "cmi.core.score.raw";
	private static final String CMI_LESSON_STATUS = "cmi.core.lesson_status";
	private static final String CMI_COMMENTS = "cmi.comments";
	private static final String CMI_TOTAL_TIME = "cmi.core.total_time";
	
	private static final String CMI_ID = "id";
	private static final String CMI_SCORE_RAW = "score.raw";
	private static final String CMI_SCORE_MIN = "score.min";
	private static final String CMI_SCORE_MAX = "score.max";
	private static final String CMI_RESULT = "result";
	private static final String CMI_STUDENT_RESPONSE = "student_response";
	private static final String CMI_CORRECT_RESPONSE = "correct_responses.";
	private static final String OBJECTIVES = "objectives.";
	private static final String CMI_COUNT = "_count";
	
	private static final Logger logger = Tracing.createLoggerFor(ScormExportManager.class);
	
	private static final ScormExportManager instance = new ScormExportManager();
	
	private ScormExportManager(){
		//
	}
	
	public static ScormExportManager getInstance() {
		return instance;
	}
	
	/**
	 * Export the results of a SCORM course 
	 * @param courseEnv
	 * @param node
	 * @param translator
	 * @param exportDirectory
	 * @param charset
	 * @return the name of the file, if any created or empty string otherwise
	 */
	public String getResults(CourseEnvironment courseEnv, ScormCourseNode node, Translator translator) {
		ScormExportFormatter visitor = new ScormExportFormatter(translator);
		visitScoDatas(courseEnv, node, visitor);
		return visitor.export();
	}
	
	/**
	 * Finds out if any results available.
	 * @param courseEnv
	 * @param node
	 * @param translator
	 * @return
	 */
	public boolean hasResults(CourseEnvironment courseEnv, CourseNode node, Translator translator) {
		ScormExportVisitor visitor = new ScormExportFormatter(translator);		
		boolean dataFound = visitScoDatas(courseEnv, (ScormCourseNode)node, visitor);
		return dataFound;
	}
	
	/**
	 * Visit the scos user's datamodel of a SCORM course. The users must be in a group.
	 * @param courseEnv
	 * @param node
	 * @param visitor
	 */
	public boolean visitScoDatas(CourseEnvironment courseEnv, ScormCourseNode node, ScormExportVisitor visitor) {
		boolean dataFound = false;
		Long courseId = courseEnv.getCourseResourceableId();
		String scoDirectoryName = courseId.toString() + "-" + node.getIdent();
		
		VFSContainer scormRoot = ScormDirectoryHelper.getScormRootFolder();
		List<Identity> users = ScoreAccountingHelper.loadUsers(courseEnv);
		//fxdiff: FXOLAT-249 prevent connection timeout if collecting data take a long time
		DBFactory.getInstance().commitAndCloseSession();
		
		for (Identity identity : users) {
			String username = identity.getName();
			VFSItem userFolder = scormRoot.resolve(username);
			if(userFolder instanceof VFSContainer) {
				VFSItem scosFolder = ((VFSContainer)userFolder).resolve(scoDirectoryName);
				if(scosFolder instanceof VFSContainer) {
					collectData(username, (VFSContainer)scosFolder, visitor);
					dataFound = true;
				}
			}
		}
		return dataFound;
	}
	
	private void collectData(String username, VFSContainer scoFolder, ScormExportVisitor visitor) {
		List<VFSItem> contents = scoFolder.getItems(new XMLFilter());
		for(VFSItem file:contents) {
			ScoDocument document = new ScoDocument(null);
			try {
				if(file instanceof LocalFileImpl) {
					document.loadDocument(((LocalFileImpl)file).getBasefile());
				}
				else {
					logger.warn("Cannot use this type of VSFItem to load a SCO Datamodel: {}", file.getClass().getName());
					continue;
				}

				String[][] scoModel = document.getScoModel();
				ScoDatas parsedDatas = parseScoModel(file.getName(), username, scoModel);
				visitor.visit(parsedDatas);
			} catch (Exception e) {
				logger.error("Cannot load a SCO Datamodel", e);
			}
		}
	}
	
	/**
	 * Parse the raw cmi datas in a java friendly object.
	 * @param scoId
	 * @param username
	 * @param scoModel
	 * @return
	 */
	private ScoDatas parseScoModel(String scoId, String username, String[][] scoModel) {
		ScoDatas datas = new ScoDatas(scoId, username);
		
		String curInteractionID = null;
		
		for(String[] line:scoModel) {
			String key = null;
			try {
				key = line[0];
				if(key == null) continue;
				
				String value = line[1];
				if(key.equals(CMI_RAW_SCORE)) {
					datas.setRawScore(value);
				}
				else if(key.equals(CMI_LESSON_STATUS)) {
					datas.setLessonStatus(value);
				}
				else if(key.equals(CMI_COMMENTS)) {
					datas.setComments(value);
				}
				else if(key.equals(CMI_TOTAL_TIME)) {
					datas.setTotalTime(value);
				}
				else if(key.startsWith(CMI_OBJECTIVES)) {
					String endStr = key.substring(CMI_OBJECTIVES.length());
					int nextPoint = endStr.indexOf('.');
					if(nextPoint < 0) {
						//cmi.objectives._count
						continue;
					}
					String interactionNr = endStr.substring(0, nextPoint);
					int nr = Integer.valueOf(interactionNr).intValue();
					ScoObjective objective = datas.getObjective(nr);
					
					String endKey = endStr.substring(nextPoint + 1);
					if(CMI_ID.equals(endKey)) {
						objective.setId(value);
					}
					if(CMI_SCORE_RAW.equals(endKey)) {
						objective.setScoreRaw(value);
					}
					else if(CMI_SCORE_MIN.equals(endKey)) {
						objective.setScoreMin(value);
					}
					else if(CMI_SCORE_MAX.equals(endKey)) {
						objective.setScoreMax(value);
					}
				}
				else if(key.startsWith(CMI_INTERACTIONS)) {
					String endStr = key.substring(CMI_INTERACTIONS.length());
					int nextPoint = endStr.indexOf('.');
					if(nextPoint < 0) {
						continue;
					}
					
					String interactionNr = endStr.substring(0, nextPoint);
					int nr = Integer.valueOf(interactionNr).intValue();
					
					ScoInteraction interaction = datas.getInteraction(nr);
					if (curInteractionID != null) {
						interaction = datas.getInteractionByID (curInteractionID);
					}
					
					String endKey = endStr.substring(nextPoint + 1);
					if(CMI_ID.equals(endKey)) {
						interactionNr = endStr.substring(0, nextPoint);
						nr = Integer.valueOf(interactionNr).intValue();
						interaction = datas.getInteraction(nr);
						
						curInteractionID = value;
						interaction.setInteractionId(value);
					}
					else if(CMI_RESULT.equals(endKey)) {
						interaction.setResult(value);
					}
					else if(CMI_STUDENT_RESPONSE.equals(endKey)) {
						interaction.setStudentResponse(value);
					}
					else if(endKey.startsWith(CMI_CORRECT_RESPONSE)) {
						interaction.setCorrectResponse(value);
					}
					else if(endKey.indexOf(OBJECTIVES) >= 0 && endKey.indexOf(CMI_COUNT) < 0) {
						interaction.getObjectiveIds().add(value);
					}
				}
			}
			catch(Exception ex) {
				logger.debug("Error parse this cmi data: " + key);
			}
		}
		
		return datas;
	}
	
	public class XMLFilter implements VFSItemFilter {
		public boolean accept(VFSItem file) {
			String name = file.getName();
			if(name.endsWith(".xml") && !(name.equals("reload-settings.xml")))
			{
				return true;
			}
			return false;
		}
	}
}