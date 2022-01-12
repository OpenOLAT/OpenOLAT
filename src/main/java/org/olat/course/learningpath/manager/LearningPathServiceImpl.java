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

import org.olat.core.id.Identity;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathEditConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.SequenceConfig;
import org.olat.course.learningpath.model.SequenceConfigImpl;
import org.olat.course.nodes.CollectingVisitor;
import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
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
public class LearningPathServiceImpl implements LearningPathService {
	
	@Autowired
	private LearningPathRegistry registry;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService respositoryService;

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
		lpEntry = repositoryManager.setTechnicalType(lpEntry, LearningPathNodeAccessProvider.TYPE);
		
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
	
}
