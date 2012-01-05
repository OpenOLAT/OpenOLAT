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
package org.olat.course;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.logging.LogDelegator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.TACourseNode;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Description:<br>
 * Upgrade-Code for course
 * will check for up-to-date editorTreeModel or runStructure first.
 * 
 * <P>
 * Initial Date:  17.07.2009 <br>
 * @author Roman Haag, www.frentix.com, roman.haag@frentix.com,
 */
public class CourseUpgrade extends LogDelegator {
	private static final String MS_TYPE = "ms";

	public CourseUpgrade(){
		//
	}
	
	public void migrateCourse(ICourse course){
		PersistingCourseImpl ccourse = (PersistingCourseImpl) course;
		// only upgrade from version 1 => 2 
		// this will migrate wiki-syntax to html
		int migrateTargetVersion = 2;
		CourseEditorTreeModel editorTreeModel = course.getEditorTreeModel();
		if (!editorTreeModel.isVersionUpToDate() && editorTreeModel.getVersion() != migrateTargetVersion){
			logError("as of OpenOLAT 8, old courses with verison 1 are no longer supported. No migration done! Upgrade to 7.0 first!", null);
		}		
		Structure runStructure = course.getRunStructure();
		if (!runStructure.isVersionUpToDate() && runStructure.getVersion() != migrateTargetVersion){
			logError("as of OpenOLAT 8, old courses with verison 1 are no longer supported. No migration done! Upgrade to 7.0 first!", null);
		}
	}

	
	
	
	
	
}
