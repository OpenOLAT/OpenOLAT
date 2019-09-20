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
package org.olat.course.nodeaccess.manager;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.logging.Tracing;
import org.olat.course.nodeaccess.NodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessProviderIdentifier;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.CourseTreeNodeBuilder;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 27 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class NodeAccessServiceImpl implements NodeAccessService {

	private static final Logger log = Tracing.createLoggerFor(NodeAccessServiceImpl.class);

	@Autowired
	private List<NodeAccessProvider> nodeAccessProviders;
	
	private NodeAccessProvider getNodeAccessProvider(NodeAccessType type) {
		for (NodeAccessProvider provider : nodeAccessProviders) {
			if (provider.getType().equals(type.getType())) {
				return provider;
			}
		}
		log.error("No node access provider found for type '{}'!", type.getType());
		return null;
	}
	
	@Override
	public List<? extends NodeAccessProviderIdentifier> getNodeAccessProviderIdentifer() {
		return nodeAccessProviders;
	}

	@Override
	public boolean isSupported(NodeAccessType type, String courseNodeType) {
		return getNodeAccessProvider(type).isSupported(courseNodeType);
	}

	@Override
	public boolean isSupported(NodeAccessType type, CourseNode courseNode) {
		return isSupported(type, courseNode.getType());
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, NodeAccessType type,
			CourseNode courseNode, UserCourseEnvironment userCourseEnvironment, CourseEditorTreeModel editorModel) {
		return getNodeAccessProvider(type).createEditController(ureq, wControl, courseNode, userCourseEnvironment, editorModel);
	}

	@Override
	public CourseTreeNodeBuilder getNodeEvaluationBuilder(UserCourseEnvironment userCourseEnvironment) {
		NodeAccessType type = NodeAccessType.of(userCourseEnvironment);
		return getNodeAccessProvider(type).getNodeEvaluationBuilder(userCourseEnvironment);
	}

	@Override
	public void onCompletionUpdate(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment,
			Double completion, AssessmentEntryStatus status, Role by) {
		NodeAccessType type = NodeAccessType.of(userCourseEnvironment);
		getNodeAccessProvider(type).onCompletionUpdate(courseNode, userCourseEnvironment, completion, status, by);
	}

}
