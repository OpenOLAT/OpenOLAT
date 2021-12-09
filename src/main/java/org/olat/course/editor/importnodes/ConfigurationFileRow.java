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

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSItem;

/**
 * 
 * Initial date: 6 oct. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfigurationFileRow extends AbstractConfigurationRow {
	
	private VFSItem item;
	private int numOfChildren;
	private List<ConfigurationFileRow> children = new ArrayList<>();
	
	private boolean parentLine;
	private String renamedFilename;
	private String courseFolderSubPath;
	private final List<ImportCourseNode> usedByList = new ArrayList<>();
	
	private FormLink toolLink;
	
	public ConfigurationFileRow(ConfigurationFileRow parent, VFSItem item) {
		super(parent);
		this.item = item;
	}

	public boolean isParentLine() {
		return parentLine;
	}

	public void setParentLine(boolean parentLine) {
		this.parentLine = parentLine;
	}

	public VFSItem getItem() {
		return item;
	}

	public int getNumOfChildren() {
		return numOfChildren;
	}

	public void setNumOfChildren(int numOfChildren) {
		this.numOfChildren = numOfChildren;
	}

	public String getCourseFolderSubPath() {
		return courseFolderSubPath;
	}

	public void setCourseFolderSubPath(String courseFolderSubPath) {
		this.courseFolderSubPath = courseFolderSubPath;
	}

	public List<ImportCourseNode> getUsedByList() {
		return usedByList;
	}
	
	public void addUsedByList(ImportCourseNode node) {
		if(!usedByList.contains(node)) {
			usedByList.add(node);
		}
	}

	public boolean isRename() {
		return StringHelper.containsNonWhitespace(renamedFilename);
	}

	public String getRenamedFilename() {
		return renamedFilename;
	}

	public void setRenamedFilename(String renamedFilename) {
		this.renamedFilename = renamedFilename;
	}

	@Override
	public ConfigurationFileRow getParent() {
		return (ConfigurationFileRow)super.getParent();
	}

	public List<ConfigurationFileRow> getChildren() {
		return children;
	}

	public FormLink getToolLink() {
		return toolLink;
	}

	public void setToolLink(FormLink toolLink) {
		this.toolLink = toolLink;
	}
}
