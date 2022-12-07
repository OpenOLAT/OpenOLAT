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
package org.olat.modules.curriculum.model;

import java.util.ArrayList;
import java.util.List;

import org.olat.modules.curriculum.CurriculumElement;

/**
 * 
 * Initial date: 20 mai 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementNode {
	
	private final CurriculumElement element;
	private final List<CurriculumElementNode> children = new ArrayList<>();
	
	public CurriculumElementNode(CurriculumElement element) {
		this.element = element;
	}
	
	public CurriculumElement getElement() {
		return element;
	}
	
	/**
	 * @return A copy of the list of direct children.
	 */
	public List<CurriculumElementNode> getChildrenNode() {
		return new ArrayList<>(children);
	}
	
	public void addChild(CurriculumElementNode node) {
		children.add(node);
	}
	
	

}
