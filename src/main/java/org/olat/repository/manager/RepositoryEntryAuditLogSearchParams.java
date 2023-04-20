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
package org.olat.repository.manager;

import java.util.Date;

import org.olat.core.id.Identity;

/**
 * Search parameters for repositoryEntryAuditLog filtering
 * <p>
 * Initial date: Apr 12, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RepositoryEntryAuditLogSearchParams {

	private Identity exlcudedAuthor;
	private Identity owner;
	private Date untilCreationDate;

	/**
	 * get author which should be excluded from the search - for action statusChange ignore own changes
	 *
	 * @return identity of excluded author
	 */
	public Identity getExlcudedAuthor() {
		return exlcudedAuthor;
	}

	/**
	 * set identity of excluded author
	 *
	 * @param exlcudedAuthor
	 */
	public void setExlcudedAuthor(Identity exlcudedAuthor) {
		this.exlcudedAuthor = exlcudedAuthor;
	}

	/**
	 * get Date for filtering with a start date until current date
	 *
	 * @return date by which the earliest entry should be retrieved
	 */
	public Date getUntilCreationDate() {
		return untilCreationDate;
	}

	/**
	 * set date by which the earliest entry should be retrieved
	 *
	 * @param untilCreationDate
	 */
	public void setUntilCreationDate(Date untilCreationDate) {
		this.untilCreationDate = untilCreationDate;
	}

	/**
	 * get Owner for learning resources
	 *
	 * @return identity of learning resource owner
	 */
	public Identity getOwner() {
		return owner;
	}

	/**
	 * set identity of learning resource owner
	 *
	 * @param owner
	 */
	public void setOwner(Identity owner) {
		this.owner = owner;
	}
}
