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
package org.olat.course.core.manager;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.control.Event;
import org.olat.core.logging.Tracing;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.core.CourseElement;
import org.olat.course.core.CourseElementSearchParams;
import org.olat.course.core.CourseNodeService;
import org.olat.course.editor.PublishEvent;
import org.olat.course.nodes.CollectingVisitor;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 24 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CourseNodeServiceImpl implements CourseNodeService, GenericEventListener {
	
	private static final Logger log = Tracing.createLoggerFor(CourseNodeServiceImpl.class);
	
	@Autowired
	private CourseElementDAO courseElementDao;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	@PostConstruct
	public void init() {
		CourseModule.registerForCourseType(this, null);
	}
	
	@Override
	public void syncCourseElements(ICourse course) {
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		
		// Get all course nodes
		CollectingVisitor collectingVisitor = CollectingVisitor.testing(load -> true);
		CourseNode rootNode = course.getRunStructure().getRootNode();
		TreeVisitor tv = new TreeVisitor(collectingVisitor, rootNode, true);
		tv.visitAll();
		List<CourseNode> courseNodes = collectingVisitor.getCourseNodes();
		
		// Get all course elements
		CourseElementSearchParams searchParams = new CourseElementSearchParams();
		searchParams.setRepositoryEntries(Collections.singletonList(courseEntry));
		List<CourseElement> courseElements = courseElementDao.load(searchParams);
		
		syncCourseElements(courseEntry, courseNodes, courseElements);
	}

	void syncCourseElements(RepositoryEntry courseEntry, List<CourseNode> courseNodes, List<CourseElement> courseElements) {
		Set<String> courseNodeIdents = courseNodes.stream().map(CourseNode::getIdent).collect(Collectors.toSet());
		Map<String, CourseElement> courseElementIdentsToElement = courseElements.stream()
				.collect(Collectors.toMap(CourseElement::getSubIdent, Function.identity()));
		Set<String> courseElementIdents = courseElementIdentsToElement.keySet();
		
		log.info("Sync course elements {} ({})", courseEntry.getKey(),
				courseEntry.getDisplayname());
		
		// Find differences
		Set<String> identsToCreate = new HashSet<>(courseNodeIdents);
		identsToCreate.removeAll(courseElementIdents);
		Set<String> identsToUpdate = new HashSet<>(courseNodeIdents);
		identsToUpdate.retainAll(courseElementIdents);
		Set<String> identsToDelete = new HashSet<>(courseElementIdents);
		identsToDelete.removeAll(courseNodeIdents);
		
		// Update in database
		for (CourseNode courseNode : courseNodes) {
			String nodeIdent = courseNode.getIdent();
			if (identsToCreate.contains(nodeIdent)) {
				createCourseElement(courseEntry, courseNode);
			} else if (identsToUpdate.contains(nodeIdent)) {
				updateCourseElement(courseEntry, courseElementIdentsToElement.get(nodeIdent), courseNode);
			}
		}
		courseElementDao.delete(courseEntry, identsToDelete);
		
		if (log.isDebugEnabled()) {
			log.debug("Course elements synched {} ({}): {} created, {} updated, {} deleted.", courseEntry.getKey(),
					courseEntry.getDisplayname(), identsToCreate.size(), identsToUpdate.size(), identsToDelete.size());
		}
	}
	
	private CourseElement createCourseElement(RepositoryEntry courseEntry, CourseNode courseNode) {
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, courseNode);
		return courseElementDao.create(courseEntry, courseNode, assessmentConfig);
	}

	private CourseElement updateCourseElement(RepositoryEntry courseEntry, CourseElement courseElement, CourseNode courseNode) {
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, courseNode);
		if (!isSame(courseElement, courseNode, assessmentConfig)) {
			return courseElementDao.update(courseElement, courseNode, assessmentConfig);
		}
		return courseElement;
	}
	
	boolean isSame(CourseElement courseElement, CourseNode courseNode, AssessmentConfig assessmentConfig) {
		return Objects.equals(courseElement.getShortTitle(), courseNode.getShortTitle())
				&& Objects.equals(courseElement.getLongTitle(), courseNode.getLongTitle())
				&& courseElement.isAssesseable() == assessmentConfig.isAssessable()
				&& Objects.equals(courseElement.getScoreMode(), assessmentConfig.getScoreMode())
				&& courseElement.hasGrade() == assessmentConfig.hasGrade()
				&& courseElement.isAutoGrade() == assessmentConfig.isAutoGrade()
				&& Objects.equals(courseElement.getPassedMode(), assessmentConfig.getPassedMode())
				&& compareCutVaue(courseElement.getCutValue(), assessmentConfig.getCutValue())
				;
	}
	
	private boolean compareCutVaue(BigDecimal cutValue, Float cutValue2) {
		if (cutValue == null &&  cutValue2 == null) return true;
		if (cutValue == null &&  cutValue2 != null) return false;
		if (cutValue != null &&  cutValue2 == null) return false;
		return Objects.equals(AssessmentHelper.getRoundedScore(cutValue), AssessmentHelper.getRoundedScore(cutValue2));
	}

	@Override
	public List<CourseElement> getCourseElements(CourseElementSearchParams searchParams) {
		return courseElementDao.load(searchParams);
	}

	@Override
	public void deleteCourseElements(RepositoryEntryRef repositoryEntry) {
		courseElementDao.delete(repositoryEntry);
	}

	@Override
	public void event(Event event) {
		if (event instanceof PublishEvent) {
			PublishEvent pe = (PublishEvent) event;
			if (pe.getState() == PublishEvent.PUBLISH && pe.isEventOnThisNode()) {
				ICourse course = CourseFactory.loadCourse(pe.getPublishedCourseResId());
				if (course != null) {
					syncCourseElements(course);
				}
			}
		}
	}
}
