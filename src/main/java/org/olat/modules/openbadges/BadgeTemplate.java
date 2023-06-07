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
package org.olat.modules.openbadges;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Initial date: 2023-05-16<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public interface BadgeTemplate {

	enum Scope {
		global, courseOnly
	}

	Date getCreationDate();

	Date getLastModified();

	void setLastModified(Date lastModified);

	String getImage();

	String getName();

	void setName(String name);

	String getDescription();

	void setDescription(String description);

	String getTags();

	void setTags(String tags);

	String getCategory();

	void setCategory(String category);

	String getScopes();

	Collection<String> getScopesAsCollection();

	void setScopes(String scopes);

	void setScopesAsCollection(Collection<String> scopes);

	String getPlaceholders();

	void setPlaceholders(String placeholders);
}
