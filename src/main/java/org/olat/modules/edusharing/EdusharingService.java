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
	
	public Ticket getTicket(Identity identity);
	
	public EdusharingResponse getPreview(Ticket ticket, String objectUrl) throws EdusharingException;

	public EdusharingResponse getRendered(Identity viewer, String identifier, String width, String height,
			String language, String language2) throws EdusharingException;
	
	public String getRenderAsWindowUrl(Ticket ticket, Identity viewer, String identifier, String language);

	public EdusharingUsage createUsage(Identity identity, EdusharingHtmlElement element, EdusharingProvider provider)
			throws EdusharingException;
	
	public EdusharingUsage loadUsageByIdentifier(String identifier);

	public List<EdusharingUsage> loadUsages(OLATResourceable ores, String subPath);
	
	public void deleteUsage(EdusharingUsage usage) throws EdusharingException;

	public void deleteUsage(Identity identity, String identifier) throws EdusharingException;
	
	public void deleteUsages(EdusharingProvider edusharingProvider);
	
	public void deleteUsages(OLATResourceable ores, String subPath) throws EdusharingException;

}
