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

import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Initial date: 2025-01-27<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public interface ReportConfiguration {

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

	void generateReport(Identity coach, Locale locale, List<UserPropertyHandler> userPropertyHandlers);
}
