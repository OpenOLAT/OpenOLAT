/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.editor.overview;

import java.util.List;

import org.olat.core.gui.control.Event;
import org.olat.course.tree.CourseEditorTreeNode;

/**
 * 
 * Initial date: 27 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OverviewNodesChangedEvent extends Event {
	
	private static final long serialVersionUID = 5392290975127161751L;

	public static final String NODESCONFIG_CHANGED_EVENT = "nodesconfigchanged";
	
	private List<CourseEditorTreeNode> nodes;
	
	public OverviewNodesChangedEvent(List<CourseEditorTreeNode> nodes) {
		super(NODESCONFIG_CHANGED_EVENT);
		this.nodes = nodes;
	}
	
	public List<CourseEditorTreeNode> getNodes() {
		return nodes;
	}

}
