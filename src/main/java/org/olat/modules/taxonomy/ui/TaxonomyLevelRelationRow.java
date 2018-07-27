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
package org.olat.modules.taxonomy.ui;

import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 16 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyLevelRelationRow {
	
	private final Long key;
	private final String displayName;
	private final String externalId;
	
	private final Object relation;
	
	public TaxonomyLevelRelationRow(QuestionItemShort item) {
		this.relation = item;
		key = item.getKey();
		displayName = item.getTitle();
		externalId = null;
	}
	
	public TaxonomyLevelRelationRow(RepositoryEntry entry) {
		this.relation = entry;
		key = entry.getKey();
		displayName = entry.getDisplayname();
		externalId = entry.getExternalId();
	}
	
	public TaxonomyLevelRelationRow(CurriculumElement element) {
		this.relation = element;
		key = element.getKey();
		displayName = element.getDisplayName();
		externalId = element.getExternalId();
	}
	
	public TaxonomyLevelRelationRow(LectureBlock lectureBlock) {
		this.relation = lectureBlock;
		key = lectureBlock.getKey();
		displayName = lectureBlock.getTitle();
		externalId = lectureBlock.getExternalId();
	}
	
	public TaxonomyLevelRelationRow(QualityDataCollection collection) {
		this.relation = collection;
		key = collection.getKey();
		displayName = collection.getTitle();
		externalId = null;
	}
	
	public Long getKey() {
		return key;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	public String getExternalId() {
		return externalId;
	}

	public Object getRelation() {
		return relation;
	}
}
