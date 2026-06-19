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
package org.olat.modules.taxonomy.matching.model;

import java.util.Date;

import org.olat.core.id.Persistable;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * Durable per-level indexing queue state for the taxonomy embedding indexing worker.
 *
 * Initial date: 2026-06-19<br>
 * @author uhensler, https://www.frentix.com
 */
public interface TaxonomyLevelIndexState extends Persistable {

	TaxonomyLevel getLevel();

	IndexStatus getStatus();

	void setStatus(IndexStatus status);

	int getAttemptCount();

	void setAttemptCount(int attemptCount);

	String getLastError();

	void setLastError(String lastError);

	String getIndexedModelId();

	void setIndexedModelId(String indexedModelId);

	String getIndexedModelVersion();

	void setIndexedModelVersion(String indexedModelVersion);

	Date getLastIndexDate();

	void setLastIndexDate(Date lastIndexDate);

	Date getCreationDate();

	Date getLastModified();

	void setLastModified(Date lastModified);

	enum IndexStatus {
		scheduled,
		indexing,
		indexed,
		failed
	}
}
