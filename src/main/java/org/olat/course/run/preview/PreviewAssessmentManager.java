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

package org.olat.course.run.preview;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.util.event.GenericEventListener;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentRunStatus;

/**
 * Initial Date:  08.02.2005
 *
 * @author Mike Stock
 */
final class PreviewAssessmentManager implements AssessmentManager {
	private Map<String,Float> nodeScores = new HashMap<>();
	private Map<String,Boolean> nodePassed = new HashMap<>();
	private Map<String,Integer> nodeAttempts = new HashMap<>();
	private Map<String,Long> nodeAssessmentID = new HashMap<>();

	private void saveNodeScore(CourseNode courseNode, Float score) {
		nodeScores.put(courseNode.getIdent(), score);
	}

	@Override
	public List<AssessmentEntry> getAssessmentEntries(CourseNode courseNode) {
		return Collections.emptyList();
	}
	
	@Override
	public List<AssessmentEntry> getAssessmentEntriesWithStatus(CourseNode courseNode, AssessmentEntryStatus status, boolean excludeZeroScore) {
		return Collections.emptyList();
	}

	@Override
	public AssessmentEntry getAssessmentEntry(CourseNode courseNode, Identity assessedIdentity) {
		return null;
	}

	@Override
	public List<AssessmentEntry> getAssessmentEntries(Identity assessedIdentity) {
		return Collections.emptyList();
	}

	@Override
	public List<AssessmentEntry> getAssessmentEntries(BusinessGroup group, CourseNode courseNode) {
		return Collections.emptyList();
	}

	@Override
	public AssessmentEntry getOrCreateAssessmentEntry(CourseNode courseNode, Identity assessedIdentity, Boolean entryRoot) {
		return null;
	}

	@Override
	public AssessmentEntry updateAssessmentEntry(AssessmentEntry assessmentEntry) {
		return assessmentEntry;
	}

	@Override
	public void saveNodeAttempts(CourseNode courseNode, Identity identity, Identity assessedIdentity, Integer attempts, Date lastAttempt, Role by) {
		nodeAttempts.put(courseNode.getIdent(), attempts);
	}

	@Override
	public void saveNodeComment(CourseNode courseNode, Identity identity, Identity assessedIdentity, String comment) {
		throw new AssertException("Not implemented for preview.");
	}

	@Override
	public void addIndividualAssessmentDocument(CourseNode courseNode, Identity identity, Identity assessedIdentity,
			File document, String filename) {
		// do nothing
	}

	@Override
	public void removeIndividualAssessmentDocument(CourseNode courseNode, Identity identity, Identity assessedIdentity, File document) {
		// do nothing
	}

	@Override
	public void deleteIndividualAssessmentDocuments(CourseNode courseNode) {
		//
	}

	@Override
	public void saveNodeCoachComment(CourseNode courseNode, Identity assessedIdentity, String comment) {
		throw new AssertException("Not implemented for preview.");
	}

	private void saveNodePassed(CourseNode courseNode, Boolean passed) {
		nodePassed.put(courseNode.getIdent(), passed);
	}

	@Override
	public void incrementNodeAttempts(CourseNode courseNode, Identity identity, UserCourseEnvironment userCourseEnvironment, Role by) {
		Integer attempts = nodeAttempts.get(courseNode.getIdent());
		if (attempts == null) attempts = Integer.valueOf(0);
		int iAttempts = attempts.intValue();
		iAttempts++;
		nodeAttempts.put(courseNode.getIdent(), Integer.valueOf(iAttempts));
	}

	@Override
	public void updateCurrentCompletion(CourseNode courseNode, Identity assessedIdentity, UserCourseEnvironment userCourseEnvironment,
			Date start, Double currentCompletion, AssessmentRunStatus runStatus, Role by) {
		//
	}

	@Override
	public void updateCompletion(CourseNode courseNode, Identity assessedIdentity,
			UserCourseEnvironment userCourseEnvironment, Double currentCompletion, AssessmentEntryStatus status,
			Role by) {
		//
	}

	@Override
	public void updateFullyAssessed(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment, Boolean fullyAssessed,
			AssessmentEntryStatus status) {
		//
	}

	@Override
	public void updateLastModifications(CourseNode courseNode, Identity assessedIdentity, UserCourseEnvironment userCourseEnvironment, Role by) {
		//
	}

	@Override
	public void updateLastVisited(CourseNode courseNode, Identity assessedIdentity, Date lastVisit) {
		//
	}

	@Override
	public Float getNodeScore(CourseNode courseNode, Identity identity) {
		return nodeScores.get(courseNode.getIdent());
	}

	@Override
	public String getNodeComment(CourseNode courseNode, Identity identity) {
		return "This is a preview"; //default comment for preview
	}
	
	@Override
	public List<File> getIndividualAssessmentDocuments(CourseNode courseNode, Identity identity) {
		return Collections.emptyList();
	}

	@Override
	public String getNodeCoachComment(CourseNode courseNode, Identity identity) {
		return "This is a preview"; //default comment for preview
	}

	@Override
	public Boolean getNodePassed(CourseNode courseNode, Identity identity) {
		return nodePassed.get(courseNode.getIdent());
	}

	@Override
	public Boolean getNodeFullyAssessed(CourseNode courseNode, Identity identity) {
		return nodePassed.get(courseNode.getIdent());
	}

	@Override
	public Integer getNodeAttempts(CourseNode courseNode, Identity identity) {
		Integer attempts = nodeAttempts.get(courseNode.getIdent());
		return (attempts == null ? Integer.valueOf(0) : attempts);
	}

	@Override
	public Double getNodeCompletion(CourseNode courseNode, Identity identity) {
		return null;
	}

	@Override
	public Double getNodeCurrentRunCompletion(CourseNode courseNode, Identity identity) {
		return null;
	}

	@Override
	public void registerForAssessmentChangeEvents(GenericEventListener gel, Identity identity) {
		throw new AssertException("Not implemented for preview.");
	}

	@Override
	public void deregisterFromAssessmentChangeEvents(GenericEventListener gel) {
		throw new AssertException("Not implemented for preview.");
	}

	private void saveAssessmentID(CourseNode courseNode, Long assessmentID) {
		nodeAssessmentID.put(courseNode.getIdent(), assessmentID);
	}
	
	@Override
	public Long getAssessmentID(CourseNode courseNode, Identity identity) {
		return nodeAssessmentID.get(courseNode.getIdent());
	}

	@Override
	public Date getScoreLastModifiedDate(CourseNode courseNode, Identity identity) {
		return null;
	}

	@Override
	public void saveScoreEvaluation(CourseNode courseNode, Identity identity, Identity assessedIdentity, ScoreEvaluation scoreEvaluation, 
			UserCourseEnvironment userCourseEnvironment, boolean incrementUserAttempts, Role by) {
		
		saveNodeScore(courseNode, scoreEvaluation.getScore());
		saveNodePassed(courseNode, scoreEvaluation.getPassed());
		saveAssessmentID(courseNode, scoreEvaluation.getAssessmentID());
		if(incrementUserAttempts) {
			incrementNodeAttempts(courseNode, identity, userCourseEnvironment, by);
		}
	}
	@Override
	public Overridable<Boolean> getRootPassed(UserCourseEnvironment userCourseEnvironment) {
		return Overridable.empty();
	}

	@Override
	public Overridable<Boolean> overrideRootPassed(Identity coach, UserCourseEnvironment userCourseEnvironment, Boolean passed) {
		return Overridable.empty();
	}

	@Override
	public Overridable<Boolean> resetRootPassed(Identity coach, UserCourseEnvironment userCourseEnvironment) {
		return Overridable.empty();
	}

	@Override
	public OLATResourceable createOLATResourceableForLocking(Identity assessedIdentity) {
		throw new AssertException("Not implemented for preview.");
	}
}