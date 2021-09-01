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
package org.olat.modules.forms.model.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Initial date: 7 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Form {
	
	private List<AbstractElement> elements = new ArrayList<>();
	private List<Rule> rules;

	public List<AbstractElement> getElements() {
		return elements;
	}

	public void setElements(List<AbstractElement> elements) {
		this.elements = elements;
	}
	
	public void addElement(AbstractElement element) {
		if(elements == null) {
			elements = new ArrayList<>();
		}
		elements.add(element);
	}
	
	public void addElement(AbstractElement element, int index) {
		if(elements == null) {
			elements = new ArrayList<>();
		}
		if(index >= 0 && index < elements.size()) {
			elements.add(index, element);
		} else {
			elements.add(element);
		}
	}
	
	public void removeElement(AbstractElement element) {
		elements.remove(element);
	}
	
	public void moveUpElement(AbstractElement element) {
		int previousSibling = previousSiblingIndex(element);
		if(previousSibling > 0 && previousSibling < elements.size()) {
			elements.remove(element);
			elements.add(previousSibling, element);
		} else {
			elements.remove(element);
			elements.add(0, element);
		}
	}
	
	private int previousSiblingIndex(AbstractElement element) {
		int index = elements.indexOf(element);
		if(index <= 0) {
			return -1;
		}
		
		if(isContained(element)) {
			// this case is not possible, the editor makes the change in the container 
		} else {
			for(int i=index; i-->0; ) {
				AbstractElement currentElement = elements.get(i);
				if(!isContained(currentElement)) {
					return i;
				}	
			}
		}
		return index - 1;
	}
	
	private boolean isContained(AbstractElement element) {
		final String elementId = element.getId();
		for(AbstractElement el:elements) {
			if(el instanceof Container && ((Container)el).getContainerSettings().hasElement(elementId)) {
				return true;
			}
		}
		return false;
	}
	
	public void moveDownElement(AbstractElement element) {
		int nextSibling = nextSiblingIndex(element);
		if(nextSibling >= 0 && nextSibling + 1 < elements.size()) {
			elements.remove(element);
			elements.add(nextSibling, element);
		} else {
			elements.remove(element);
			elements.add(element);
		}
	}
	
	private int nextSiblingIndex(AbstractElement element) {
		final int index = elements.indexOf(element);
		if(index < 0) {
			return -1;
		}
		
		if(isContained(element)) {
			// this case is not possible, the editor makes the change in the container 
		} else {
			int numOfElements = elements.size();
			for(int i=index + 1; i<numOfElements; i++) {
				AbstractElement currentElement = elements.get(i);
				if(!isContained(currentElement)) {
					return i;
				}	
			}
		}
		return index + 1;
	}
	
	public void moveElement(AbstractElement elementToMove, AbstractElement sibling, boolean after) {
		if(elements.remove(elementToMove)) {
			int index;
			if(sibling == null) {
				index = elements.size();
			} else {
				index = elements.indexOf(sibling);
			}
			
			if(after) {
				index++;
			}
			
			if(index >= 0 && index <= elements.size()) {
				elements.add(index, elementToMove);
			} else {
				elements.add(elementToMove);
			}
		}
	}
	
	public List<Rule> getRules() {
		if (rules == null) {
			rules = new ArrayList<>(1);
		}
		return rules;
	}

	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}
}
