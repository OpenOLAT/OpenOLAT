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
package org.olat.course.nodes.practice.ui;

import org.olat.group.BusinessGroup;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QuestionItemCollection;

/**
 * 
 * Initial date: 6 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharedResourceRow {
	
	private final String name;
	private final Long key;
	
	private Pool pool;
	private BusinessGroup businessGroup;
	private QuestionItemCollection collection;
	
	public SharedResourceRow(Pool pool) {
		name = pool.getName();
		key = pool.getKey();
		this.pool = pool;
	}
	
	public SharedResourceRow(BusinessGroup businessGroup) {
		name = businessGroup.getName();
		key = businessGroup.getKey();
		this.businessGroup = businessGroup;
	}
	
	public SharedResourceRow(QuestionItemCollection collection) {
		name = collection.getName();
		key = collection.getKey();
		this.collection = collection;
	}
	
	public Long getKey() {
		return key;
	}

	public String getName() {
		return name;
	}

	public Pool getPool() {
		return pool;
	}

	public BusinessGroup getBusinessGroup() {
		return businessGroup;
	}

	public QuestionItemCollection getCollection() {
		return collection;
	}
	
	
	
}
