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
package org.olat.modules.qpool.model;

import java.util.List;

import org.olat.modules.qpool.QuestionItemShort;

/**
 * 
 * Initial date: 18.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QItemList {

	private final boolean groupByTaxonomyLevel;
	private final List<QuestionItemShort> items;
	
	public QItemList(List<QuestionItemShort> items, boolean groupByTaxonomyLevel) {
		this.items = items;
		this.groupByTaxonomyLevel = groupByTaxonomyLevel;
	}

	public boolean isGroupByTaxonomyLevel() {
		return groupByTaxonomyLevel;
	}

	public List<QuestionItemShort> getItems() {
		return items;
	}

}
