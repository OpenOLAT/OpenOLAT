/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.topicbroker.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Initial date: 15 Jul 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBProcessSelections {
	
	private List<TBProcessSelection> selections;

	public List<TBProcessSelection> getSelections() {
		return selections;
	}

	public void setSelections(List<TBProcessSelection> selections) {
		this.selections = selections;
	}
	
	public void addSelection(Long topicKey, String topicTitle, int sortOrder) {
		TBProcessSelection selection = new TBProcessSelection();
		selection.setTopicKey(topicKey);
		selection.setTopicTitle(topicTitle);
		selection.setSortOrder(sortOrder);
		
		if (selections == null) {
			selections = new ArrayList<>(3);
		}
		selections.add(selection);
	}

	@Override
	public String toString() {
		String string = "TBProcessSelections [selections=";
		if (selections != null && !selections.isEmpty()) {
			for (TBProcessSelection selection : selections) {
				string += selection.getSortOrder() + ":" + selection.getTopicKey() + ",";
			}
		}
		string += "]";
		
		return string;
	}

}
