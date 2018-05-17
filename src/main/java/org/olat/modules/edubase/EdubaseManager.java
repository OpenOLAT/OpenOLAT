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
package org.olat.modules.edubase;

import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;

/**
 *
 * Initial date: 26.06.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface EdubaseManager {

	/**
	 * Validates if the URL can be parsed and if a valid Edubase Book ID can be
	 * extracted.
	 *
	 * @param url
	 * @return
	 */
	public boolean validateBookId(String url);

	/**
	 * Parses the URL and tries to extract the Edubase Book ID. If the Book ID
	 * can not be extracted from the URL, the original String is returned.
	 *
	 * @param url
	 * @return the Edubase Book ID or the original String
	 */
	public String parseBookId(String url);

	/**
	 * Get the ID of the user for the the authentication in Edubase.
	 *
	 * @param identityEnvironment
	 * @return
	 */
	public String getUserId(IdentityEnvironment identityEnvironment);

	/**
	 * Generates the effective LTI Launch URI.
	 *
	 * @param bookSection
	 * @return
	 */
	public String getLtiLaunchUrl(BookSection bookSection);

	/**
	 * The application url is the target url of the edubase reader. The url is
	 * depending on the module configuration unique per identity.
	 * 
	 * @param identity
	 * @return
	 */
	public String getApplicationUrl(Identity identity);

	/**
	 * Request details of a book version from the Edubase InfoDocVers Service.
	 *
	 * @param bookId
	 * @return the information to a book. If the request failed, BookDetails
	 *         with empty fields is returned
	 */
	public BookDetails fetchBookDetails(String bookId);

}
