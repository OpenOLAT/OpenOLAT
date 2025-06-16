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
package org.olat.modules.coach.reports;

import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.core.util.vfs.LocalFileImpl;

/**
 * Initial date: 2025-01-27<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public interface ReportConfiguration {

	/**
	 * Returns true if the given 'secCallback' provides access to this report 
	 * configuration.
	 * 
	 * A person can have access to a report configuration because
	 * they are owner, coach or manager of courses or curricula, or they have
	 * an organizational role such as education manager that lets them execute this 
	 * report type, or some other reason. The set of possible access criteria can
	 * be queried in the 'secCallback'.
	 * 
	 * @param secCallback Callback object to query for access criteria.
	 * @return True if the person linked to the 'secCallback' has access to this report configuration.
	 */
	boolean hasAccess(ReportConfigurationAccessSecurityCallback secCallback);

	/**
	 * Returns the name of the report configuration. Implementations are encouraged to return a translated value.
	 * 
	 * @return The name of the report configuration.
	 */
	String getName(Locale locale);

	/**
	 * Returns a description that fits on a short line of text. Implementations are encouraged to return a translated value.
	 * 
	 * @return The description of the report configuration.
	 */
	String getDescription(Locale locale);

	/**
	 * Returns a category for the report configuration.
	 * 
	 * @return The category of the report configuration.
	 */
	String getCategory(Locale locale);

	/**
	 * A static report gets its data using a hard-coded query and always returns an export file of the same format
	 * with pre-defined columns.
	 * <p> 
	 * A dynamic report can be configured for both data retrieval and export.
	 *
	 * @return True if the report configuration is dynamic, false if it is static.
	 */
	boolean isDynamic();

	/**
	 * Generate the report. Implementations are free to decide where and in what format the reports should be.
     *
	 * @param coach Coach who triggered the report generation.
	 * @param locale The locale to use for localized/translated text.
	 * @param output The target file to store the output to.                 
	 */
	void generateReport(Identity identity, Locale locale, LocalFileImpl output);

	void generateReport(Identity coach, Locale locale);

	/**
	 * Provide a number to be used as a sorting order. This is used to display report configurations in an ordered
	 * list.

	 * @return A sorting order as integer. 
	 *         Sorting takes place in ascending order, 
	 *         so smaller numbers place the element higher up in the list.
	 */
	int getOrder();
}
