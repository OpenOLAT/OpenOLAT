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
package org.olat.course.learningpath.manager;

import java.util.List;

import javax.annotation.PostConstruct;

import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.ScoreAccountingTrigger;
import org.olat.course.assessment.ScoreAccountingTriggerData;
import org.olat.course.editor.PublishEvent;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathEditConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.SequenceConfig;
import org.olat.course.learningpath.model.SequenceConfigImpl;
import org.olat.course.learningpath.obligation.ExceptionalObligation;
import org.olat.course.learningpath.obligation.ExceptionalObligationHandler;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CollectingVisitor;
import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 1 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LearningPathServiceImpl implements LearningPathService, GenericEventListener {
	
	@Autowired
	private LearningPathRegistry registry;
	@Autowired
	private RepositoryService respositoryService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	@PostConstruct
	public void init() {
		CourseModule.registerForCourseType(this, null);
	}
	
	@Override
	public LearningPathConfigs getConfigs(CourseNode courseNode) {
		return registry.getLearningPathNodeHandler(courseNode).getConfigs(courseNode);
	}

	@Override
	public LearningPathEditConfigs getEditConfigs(CourseNode courseNode) {
		return registry.getLearningPathNodeHandler(courseNode).getEditConfigs();
	}

	@Override
	public SequenceConfig getSequenceConfig(CourseNode courseNode) {
		LearningPathConfigs lpConfig = registry.getLearningPathNodeHandler(courseNode).getConfigs(courseNode);
		Boolean hasSequentialChildren = lpConfig.hasSequentialChildren();
		Boolean inheritedSequencialChildren = getInheritedSequencialChildren(courseNode);
		
		boolean sequentialChildren = hasSequentialChildren != null
				? hasSequentialChildren.booleanValue()                // Node defines config itself
				: Boolean.TRUE.equals(inheritedSequencialChildren);   // use the inherited config
		boolean inSequence = inheritedSequencialChildren != null
				? inheritedSequencialChildren.booleanValue()
				: sequentialChildren;                                 // root has no inherited config, take the config from the root itself
		
		return new SequenceConfigImpl( inSequence, sequentialChildren);
	}
	
	private Boolean getInheritedSequencialChildren(CourseNode courseNode) {
		INode parentNode = courseNode.getParent();
		if (parentNode instanceof CourseNode) {
			CourseNode parent = (CourseNode)parentNode;
			LearningPathConfigs parentConfig = registry.getLearningPathNodeHandler(parent).getConfigs(parent);
			
			Boolean hasSequentialChildren = parentConfig.hasSequentialChildren();
			if (hasSequentialChildren != null) {
				return hasSequentialChildren;
			}
			
			return getInheritedSequencialChildren(parent);
		}
		return null;
	}

	@Override
	public List<ExceptionalObligationHandler> getExceptionalObligationHandlers() {
		return registry.getExceptionalObligationHandler();
	}

	@Override
	public ExceptionalObligationHandler getExceptionalObligationHandler(String type) {
		return registry.getExceptionalObligationHandler(type, false);
	}

	@Override
	public void syncExceptionalObligations(Long courseResId) {
		ICourse course = CourseFactory.loadCourse(courseResId);
		if (course == null) return;
		
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<ScoreAccountingTrigger> scoreAccountingTrigger = courseAssessmentService.getScoreAccountingTriggers(courseEntry);
		
		// Create ScoreAccountingTriggers of created ExceptionalObligations
		CourseNode rootNode = course.getRunStructure().getRootNode();
		TreeVisitor tv = new TreeVisitor(createExceptionalObligations(courseEntry, scoreAccountingTrigger), rootNode, true);
		tv.visitAll();
		
		// Delete ScoreAccountingTrigger of deleted ExceptionalObligations
		courseAssessmentService.deleteScoreAccountingTriggers(scoreAccountingTrigger);
	}
	
	/**
	 * Creates a ScoreAccountingTriggers for every ExceptionalObligation of the CourseNode if not found but needed.
	 * Removes ScoreAccountingTriggers from the list if an according ExceptionalObligation exists.
	 * 
	 * @param courseEntry 
	 * @param scoreAccountingTriggers
	 * @return
	 */
	private Visitor createExceptionalObligations(RepositoryEntry courseEntry, List<ScoreAccountingTrigger> scoreAccountingTriggers) {
		return iNode -> {
			if (iNode instanceof CourseNode) {
				CourseNode courseNode = (CourseNode)iNode;
				List<ExceptionalObligation> nodeExceptionalObligations = getConfigs(courseNode).getExceptionalObligations();
				for (ExceptionalObligation exceptionalObligation : nodeExceptionalObligations) {
					ExceptionalObligationHandler exceptionalObligationHandler = registry.getExceptionalObligationHandler(exceptionalObligation.getType(), true);
					if (exceptionalObligationHandler != null && exceptionalObligationHandler.hasScoreAccountingTrigger()) {
						boolean found = scoreAccountingTriggers.removeIf(ref -> ref.getIdentifier().equals(exceptionalObligation.getIdentifier()));
						if (!found) {
							ScoreAccountingTriggerData data = exceptionalObligationHandler.getScoreAccountingTriggerData(exceptionalObligation);
							if (data != null) {
								data.setIdentifier(exceptionalObligation.getIdentifier());
								courseAssessmentService.createScoreAccountingTrigger(courseEntry, courseNode.getIdent(), data);
							}
						}
					}
				}
			}
		};
	}

	@Override
	public List<CourseNode> getUnsupportedCourseNodes(ICourse course) {
		CourseEditorTreeModel editorTreeModel = course.getEditorTreeModel();
		CollectingVisitor visitor = CollectingVisitor.applying(new UnsupportedFunction(editorTreeModel));
		TreeVisitor tv = new TreeVisitor(visitor, editorTreeModel.getRootNode(), true);
		tv.visitAll();
		return visitor.getCourseNodes();
	}

	@Override
	public RepositoryEntry migrate(RepositoryEntry courseEntry, Identity identity) {
		String displayname = courseEntry.getDisplayname() + " (copy)";
		RepositoryEntry lpEntry = respositoryService.copy(courseEntry, identity, displayname);
		
		ICourse course = CourseFactory.loadCourse(lpEntry);
		course = CourseFactory.openCourseEditSession(course.getResourceableId());
		
		course.getCourseConfig().setNodeAccessType(LearningPathNodeAccessProvider.TYPE);
		CourseFactory.setCourseConfig(course.getResourceableId(), course.getCourseConfig());
		
		TreeVisitor tv = new TreeVisitor(new PostMigrationVisitor(registry), course.getEditorTreeModel().getRootNode(), false);
		tv.visitAll();
		tv = new TreeVisitor(new PostMigrationVisitor(registry), course.getRunStructure().getRootNode(), false);
		tv.visitAll();
		
		CourseFactory.saveCourse(course.getResourceableId());
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
		return lpEntry;
	}

	@Override
	public void event(Event event) {
		if (event instanceof PublishEvent) {
			PublishEvent pe = (PublishEvent) event;
			if (pe.getState() == PublishEvent.PUBLISH && pe.isEventOnThisNode()) {
				ICourse course = CourseFactory.loadCourse(pe.getPublishedCourseResId());
				if (LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(course).getType())) {
					syncExceptionalObligations(pe.getPublishedCourseResId());
					courseAssessmentService.evaluateAllAsync(pe.getPublishedCourseResId());
				}
			}
		}
	}
	
}
