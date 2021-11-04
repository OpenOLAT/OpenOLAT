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

import org.olat.core.util.vfs.VFSItem;

/**
 * 
 * Initial date: 8 oct. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportCourseFile {

	private boolean renamed;
	private String renamedFilename;
	
	private VFSItem targetItem;
	private VFSItem originalItem;
	private String courseFolderSubPath;
	
	private ImportCourseFile parent;
	private final List<ImportCourseFile> children = new ArrayList<>();
	
	public ImportCourseFile(VFSItem originalItem) {
		this.originalItem = originalItem;
	}
	
	public VFSItem getOriginalItem() {
		return originalItem;
	}
	
	public VFSItem getTargetItem() {
		return targetItem;
	}

	public void setTargetItem(VFSItem targetItem) {
		this.targetItem = targetItem;
	}

	public boolean isRenamed() {
		return renamed;
	}

	public void setRenamed(boolean renamed) {
		this.renamed = renamed;
	}

	public String getRenamedFilename() {
		return renamedFilename;
	}

	public void setRenamedFilename(String renamedFilename) {
		this.renamedFilename = renamedFilename;
	}

	public String getCourseFolderSubPath() {
		return courseFolderSubPath;
	}

	public void setCourseFolderSubPath(String courseFolderSubPath) {
		this.courseFolderSubPath = courseFolderSubPath;
	}

	public ImportCourseFile getParent() {
		return parent;
	}

	public void setParent(ImportCourseFile parent) {
		this.parent = parent;
	}

	public List<ImportCourseFile> getChildren() {
		return children;
	}
}
