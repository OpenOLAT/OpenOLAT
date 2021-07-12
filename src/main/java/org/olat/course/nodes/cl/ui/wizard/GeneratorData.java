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
package org.olat.course.nodes.cl.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.course.nodes.cl.model.Checkbox;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 13.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GeneratorData {
	
	private String structureTitle;
	private String structureShortTitle;
	private String structureDescription;
	
	private boolean points;
	private boolean passed;
	private Float cutValue;
	
	private int numOfNodes = 1;
	private String nodePrefix;
	private List<CheckListNode> nodes;
	
	private final List<Checkbox> checkboxList = new ArrayList<>();
	private final ModuleConfiguration config = new ModuleConfiguration();

	public int getNumOfCheckbox() {
		return checkboxList == null ? 0 : checkboxList.size();
	}
	
	public List<Checkbox> getCheckboxList() {
		return checkboxList;
	}

	public int getNumOfNodes() {
		return numOfNodes;
	}

	public void setNumOfNodes(int numOfNodes) {
		this.numOfNodes = numOfNodes;
	}

	public String getNodePrefix() {
		return nodePrefix;
	}

	public void setNodePrefix(String nodePrefix) {
		this.nodePrefix = nodePrefix;
	}

	public List<CheckListNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<CheckListNode> nodes) {
		this.nodes = nodes;
	}

	public ModuleConfiguration getModuleConfiguration() {
		return config;
	}

	public String getStructureTitle() {
		return structureTitle;
	}

	public void setStructureTitle(String structureTitle) {
		this.structureTitle = structureTitle;
	}

	public String getStructureShortTitle() {
		return structureShortTitle;
	}

	public void setStructureShortTitle(String structureShortTitle) {
		this.structureShortTitle = structureShortTitle;
	}

	public String getStructureDescription() {
		return structureDescription;
	}

	public void setStructureDescription(String structureDescription) {
		this.structureDescription = structureDescription;
	}

	public boolean isPoints() {
		return points;
	}

	public void setPoints(boolean points) {
		this.points = points;
	}

	public boolean isPassed() {
		return passed;
	}

	public void setPassed(boolean passed) {
		this.passed = passed;
	}

	public Float getCutValue() {
		return cutValue;
	}

	public void setCutValue(Float cutValue) {
		this.cutValue = cutValue;
	}
}
