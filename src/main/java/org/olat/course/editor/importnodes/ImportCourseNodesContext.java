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
package org.olat.course.editor.importnodes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 4 oct. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportCourseNodesContext {
	
	private final CourseEditorTreeNode copyTo;
	private CourseEditorTreeNode firstNode;
	
	private RepositoryEntry entry;
	private final RepositoryEntry targetEntry;
	private List<ImportCourseNode> nodes = new ArrayList<>();
	private List<ImportCourseFile> courseFolderFiles = new ArrayList<>();
	
	public ImportCourseNodesContext(CourseEditorTreeNode copyTo, RepositoryEntry targetEntry) {
		this.copyTo = copyTo;
		this.targetEntry = targetEntry;
	}
	
	public CourseEditorTreeNode getCopyTo() {
		return copyTo;
	}

	public CourseEditorTreeNode getFirstNode() {
		return firstNode;
	}

	public void setFirstNode(CourseEditorTreeNode firstNode) {
		this.firstNode = firstNode;
	}

	public RepositoryEntry getEntry() {
		return entry;
	}

	public void setEntry(RepositoryEntry entry) {
		this.entry = entry;
	}
	
	public RepositoryEntry getTargetEntry() {
		return targetEntry;
	}
	
	public boolean hasNodes() {
		return nodes != null && !nodes.isEmpty();
	}

	/**
	 * @return All the nodes selected by the user
	 */
	public List<ImportCourseNode> getSelectedNodes() {
		if(nodes == null) {
			nodes = new ArrayList<>();
		}
		return nodes;
	}
	
	/**
	 * @return Only the nodes selected by the user which aren't exclude (by errors).
	 */
	public List<ImportCourseNode> getNodesToImport() {
		return getSelectedNodes().stream()
				.filter(node -> !node.isExcludeFromImport())
				.collect(Collectors.toList());
	}

	public void setNodes(List<ImportCourseNode> nodes) {
		this.nodes = nodes;
	}

	public List<ImportCourseFile> getCourseFolderFiles() {
		return courseFolderFiles;
	}

	public void setCourseFolderFiles(List<ImportCourseFile> courseFolderFiles) {
		this.courseFolderFiles = courseFolderFiles;
	}
}
