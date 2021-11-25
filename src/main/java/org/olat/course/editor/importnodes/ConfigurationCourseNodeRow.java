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

import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.course.assessment.IndentedNodeRenderer.IndentedCourseNode;
import org.olat.course.tree.CourseEditorTreeNode;

/**
 * 
 * Initial date: 4 oct. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfigurationCourseNodeRow extends AbstractConfigurationRow implements IndentedCourseNode {
	
	private final ImportCourseNode node;
	
	private int numOfReminders;
	private SingleSelection configurationItem;
	
	public ConfigurationCourseNodeRow(ImportCourseNode node, ConfigurationCourseNodeRow parent) {
		super(parent);
		this.node = node;
	}
	
	@Override
	public String getShortTitle() {
		return node.getCourseNode().getShortTitle();
	}

	@Override
	public String getLongTitle() {
		return node.getCourseNode().getLongTitle();
	}

	@Override
	public String getType() {
		return node.getCourseNode().getType();
	}

	@Override
	public int getRecursionLevel() {
		int recursionLevel = 0;
		for(FlexiTreeTableNode current=getParent(); current != null; current=current.getParent()) {
			recursionLevel++;
		}
		return recursionLevel;
	}

	@Override
	public ConfigurationCourseNodeRow getParent() {
		return (ConfigurationCourseNodeRow)super.getParent();
	}

	@Override
	public String getCrump() {
		return null;
	}

	public SingleSelection getConfigurationItem() {
		return configurationItem;
	}

	public void setConfigurationItem(SingleSelection configurationItem) {
		this.configurationItem = configurationItem;
	}

	public int getNumOfReminders() {
		return numOfReminders;
	}

	public void setNumOfReminders(int numOfReminders) {
		this.numOfReminders = numOfReminders;
	}
	
	public ImportCourseNode getImportCourseNode() {
		return node;
	}

	public CourseEditorTreeNode getEditorTreeNode() {
		return node.getEditorTreeNode();
	}
}
