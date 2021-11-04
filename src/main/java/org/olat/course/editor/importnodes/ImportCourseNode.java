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

import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;

/**
 * 
 * Initial date: 5 oct. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportCourseNode {

	private final List<String> courseFolderSubPathList = new ArrayList<>();
	private boolean courseFolderSubPathWithParent;
	
	private CopyType importSetting;
	private final CourseEditorTreeNode editorTreeNode;
	
	private boolean excludeFromImport = false;
	private ImportCourseNode parent;
	private final List<ImportCourseNode> children = new ArrayList<>();
	private final List<ImportCourseFile> files = new ArrayList<>();
	
	private String targetCourseNodeIdent;
	
	public ImportCourseNode(CourseEditorTreeNode editorTreeNode) {
		this.editorTreeNode = editorTreeNode;
	}
	
	public String getIdent() {
		return editorTreeNode.getIdent();
	}
	
	public CourseEditorTreeNode getEditorTreeNode() {
		return editorTreeNode;
	}
	
	public CourseNode getCourseNode() {
		return editorTreeNode.getCourseNode();
	}

	public boolean isExcludeFromImport() {
		return excludeFromImport;
	}

	public void setExcludeFromImport(boolean excludeFromImport) {
		this.excludeFromImport = excludeFromImport;
	}

	public ImportCourseNode getParent() {
		return parent;
	}

	public void setParent(ImportCourseNode parent) {
		this.parent = parent;
	}

	public List<ImportCourseNode> getChildren() {
		return children;
	}

	public CopyType getImportSetting() {
		return importSetting;
	}

	public void setImportSetting(CopyType importSetting) {
		this.importSetting = importSetting;
	}

	public boolean isCourseFolderSubPathWithParent() {
		return courseFolderSubPathWithParent;
	}

	public void setCourseFolderSubPathWithParent(boolean courseFolderSubPathWithParent) {
		this.courseFolderSubPathWithParent = courseFolderSubPathWithParent;
	}

	public List<String> getCourseFolderSubPathList() {
		return courseFolderSubPathList;
	}

	public void addCourseFolderSubPath(String courseFolderSubPath) {
		courseFolderSubPathList.add(courseFolderSubPath);
	}

	public String getTargetCourseNodeIdent() {
		return targetCourseNodeIdent;
	}

	public void setTargetCourseNodeIdent(String targetCourseNodeIdent) {
		this.targetCourseNodeIdent = targetCourseNodeIdent;
	}

	public List<ImportCourseFile> getFiles() {
		return files;
	}

	@Override
	public int hashCode() {
		return editorTreeNode.getIdent().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof ImportCourseNode) {
			ImportCourseNode node = (ImportCourseNode)obj;
			return editorTreeNode.getIdent().equals(node.getEditorTreeNode().getIdent());
		}
		return false;
	}
}
