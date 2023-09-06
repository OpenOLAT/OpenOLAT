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
 * Initial date: 11.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class Choices {

	private List<Choice> choices = new ArrayList<>();
	
	public List<Choice> asList() {
		return new ArrayList<>(choices);
	}
	
	public int size() {
		return choices == null ? 0 : choices.size();
	}

	public void addNotPresent(Choice choice) {
		if (!choices.contains(choice)) {
			choices.add(choice);
		}
	}
	
	public void remove(Choice choice) {
		choices.remove(choice);
	}
	
	public Integer getIndex(Choice choice) {
		for (int i = 0; i < choices.size(); i++) {
			if (choices.get(i).equals(choice)) {
				return i;
			}
		}
		return null;
	}
	
	public void swap(int i, int j) {
		Choice temp = choices.get(i);
		choices.set(i, choices.get(j));
		choices.set(j, temp);
	}

}
