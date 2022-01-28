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
package org.olat.course.editor;

import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.CourseEditorTreeNode;

public class CourseEditorHelper {
	
	/**
	 * Helper to create the proposed directory name for the page base on the
	 * course node short title. The title is normalized, converted to ASCII and
	 * all strange characters are removed. Users should recognize the proposed
	 * directory name in most cases. As a fallback, the course node ID is used
	 * in case everything is stripped. If a proposed name already exists, the
	 * directory will get a number appended.
	 * <br />
	 * The file name will always be "index.html"
	 * <br />
	 * The end result will look something like this:
	 * <br />
	 * <pre>/single_page3/index.html</pre>
	 * 
	 * @param courseNode 
	 * @param courseFolderBaseContainer 
	 * @return The proposed relative file path
	 */
	public static String createUniqueRelFilePathFromShortTitle(CourseNode courseNode, VFSContainer courseFolderBaseContainer) {
		// Normalize course node title to something URL and filesystem safe
		// containing only ASCII characters
		String safePath = FileUtils.normalizeFilename(courseNode.getShortTitle().toLowerCase()).trim()
				.replaceAll("[^\\p{ASCII}]", "")			// delete non-ASCII
	            .replaceAll("[\\p{Punct}]+", "_")			// replace punctuation characters
	            .replaceAll("\\s+", "_")					// replace whitespace
        		.replaceAll("_+", "_")						// multiple replacement strings
        		.replaceAll("^_", "")						// delete replacement string at beginning
        		.replaceAll("_$", "")						// delete replacement string at end
        		.toLowerCase(Locale.ENGLISH);		
		
		// If string is empty, use node ID instead
		if (!StringHelper.containsNonWhitespace(safePath)) {
			safePath = courseNode.getIdent();
		}

		// Check if directory already exists, if yes, append number at end of
		// directory
		VFSItem direxists = courseFolderBaseContainer.resolve(safePath);
		int i = 1;
		while (direxists != null && i<100) {
			i++;
			safePath = FileUtils.appendNumberAtTheEndOfFilename(safePath, i);
			direxists = courseFolderBaseContainer.resolve(safePath);
		}

		return "/" + safePath + "/" + safePath + ".html";
	}


	/**
	 * Helper method to create a new course node and attach it to the editor model
	 * 
	 * @param newNodeType The type of the new course node
	 * @param course      The current course
	 * @param currentNode The currently selected node in the editor. The newly
	 *                    created node will be a sibling of this node
	 * @param translator
	 * @return The new created course node
	 */
	protected static CourseNode createAndInsertNewNode(String newNodeType, ICourse course, CourseEditorTreeNode currentNode, Translator translator) {
		CourseEditorTreeModel editorTreeModel = course.getEditorTreeModel();
		CourseEditorTreeNode selectedNode = null;
		int pos = 0;
		if(editorTreeModel.getRootNode().equals(currentNode)) {
			//root, add as last child
			pos = currentNode.getChildCount();
			selectedNode = currentNode;
		} else {
			selectedNode = (CourseEditorTreeNode)currentNode.getParent();
			pos = currentNode.getPosition() + 1;
		}
		
		// user chose a position to insert a new node
		CourseNodeConfiguration newNodeConfig = CourseNodeFactory.getInstance().getCourseNodeConfiguration(newNodeType);
		CourseNode createdNode = newNodeConfig.getInstance();
		createdNode.updateModuleConfigDefaults(true, selectedNode, NodeAccessType.of(course));

		// Set some default values
		String title = newNodeConfig.getLinkText(translator.getLocale());
		createdNode.setLongTitle(title);
		createdNode.setNoAccessExplanation(translator.translate("form.noAccessExplanation.default"));
		
		// Add node
		editorTreeModel.insertCourseNodeAt(createdNode, selectedNode.getCourseNode(), pos);
		CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());
		return createdNode;
	}

	
}
