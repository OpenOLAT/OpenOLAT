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
package org.olat.modules.edusharing;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;

/**
 * 
 * Initial date: 28 Nov 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface EdusharingService {

	void test(Identity identity);
	
	/**
	 * Get the configuration values used to register OpenOLAT as an edu-sharing application.
	 *
	 * @return the configurations
	 */
	public Properties getConfigForRegistration();
	
	/**
	 * Load the configurations from the edu-sharing server.
	 *
	 * @return the loaded properties
	 */
	public EdusharingProperties getEdusharingRepoConfig() throws EdusharingException;
	
	public Ticket createTicket(Identity identity) throws EdusharingException;
	
	/**
	 * Validated the ticket token and updates the time of the ticket if the validation was successful.
	 * 
	 * @param ticket
	 * @return the valid ticket or empty optional if validation failed
	 */
	public Optional<Ticket> validateTicket(Ticket ticket);
	
	public EdusharingResponse getPreview(Ticket ticket, String objectUrl) throws EdusharingException;

	public EdusharingResponse getRendered(Identity viewer, String identifier, String width, String height,
			String language) throws EdusharingException;
	
	public String getRenderAsWindowUrl(Ticket ticket, Identity viewer, String identifier, String language);

	public EdusharingUsage createUsage(Identity identity, EdusharingHtmlElement element, EdusharingProvider provider)
			throws EdusharingException;
	
	public EdusharingUsage loadUsageByIdentifier(String identifier);

	public List<EdusharingUsage> loadUsages(OLATResourceable ores);

	public void deleteUsage(Identity identity, String identifier) throws EdusharingException;

}
