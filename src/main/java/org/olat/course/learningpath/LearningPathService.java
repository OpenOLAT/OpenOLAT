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
package org.olat.course.learningpath;

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.util.nodes.INode;
import org.olat.course.ICourse;
import org.olat.course.learningpath.obligation.ExceptionalObligationHandler;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 1 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface LearningPathService {

	public LearningPathConfigs getConfigs(CourseNode courseNode);
	
	/**
	 * Get the LearningPathConfigs. This method has to be used, if the courseNode is
	 * not part of the tree. This is e.g. the case if the courseNode is extracted
	 * from the CourseEditorTreeNode. The parent may be used to update the configs.
	 *
	 * @param courseNode
	 * @param parent
	 * @return
	 */
	public LearningPathConfigs getConfigs(CourseNode courseNode, INode parent);
	
	public LearningPathEditConfigs getEditConfigs(CourseNode courseNode);
	
	public SequenceConfig getSequenceConfig(CourseNode courseNode);
	
	
	/**
	 * Get all enabled ExceptionalObligationHandler ordered by sort value.
	 * 
	 * @return 
	 */
	public List<ExceptionalObligationHandler> getExceptionalObligationHandlers();
	
	/**
	 * 
	 * @param type
	 * @return the ExceptionalObligationHandler or null if not found or not enabled.
	 */
	public ExceptionalObligationHandler getExceptionalObligationHandler(String type);
	
	/**
	 * Synchronize the exceptional obligations from the course run structure to the database.
	 *
	 * @param courseResId
	 */
	public void syncExceptionalObligations(Long courseResId);
	
	public List<CourseNode> getUnsupportedCourseNodes(ICourse course);
	
	public RepositoryEntry migrate(RepositoryEntry courseEntry, Identity identity);

}
