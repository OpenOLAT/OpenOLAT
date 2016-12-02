//<OLATCE-103>
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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.vc.provider;

import java.net.URL;
import java.util.Date;
import java.util.Map;

import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;

import de.bps.course.nodes.vc.VCConfiguration;

/**
 * 
 * Description:<br>
 * Interface defines the API a virtual classroom provider has to provide. It is designed
 * to have an own instance for every single user.
 * 
 * <P>
 * Initial Date:  09.12.2010 <br>
 * @author skoeber
 */
public interface VCProvider extends ConfigOnOff {
	
	/**
	 * @return new independent instance of the provider
	 */
	public VCProvider newInstance();
	
	/**
	 * @return provider id as defined in the configuration
	 */
	public String getProviderId();
	
	/**
	 * @return display name of the provider
	 */
	public String getDisplayName();
	
	/**
	 * @return mapping of key and displayname for available templates or empty map, but never <code>null</code>
	 */
	public Map<String, String> getTemplates();
	
	/**
	 * @return <code>true</code> if virtual classroom is available at present, <code>false</code> otherwise
	 */
	public boolean isProviderAvailable();
	
	/**
	 * Create a new virtual classroom to be used e.g. in a course node
	 * @param roomId (maybe prefixed automatically) 
	 * @param name (optional name for meeting)
	 * @param description (optional description for meeting)
	 * @param begin (usage dependent on target platform, can be NULL)
	 * @param end (usage dependent on target platform, can be NULL)
	 * @param templateId
	 * @return success
	 */
	public boolean createClassroom(String roomId, String name, String description, Date begin, Date end, VCConfiguration config);
	
	/**
	 * Update an existing virtual classroom
	 * @param roomId of the existing classroom
	 * @param name the new name
	 * @param description the new description
	 * @param begin the new begin
	 * @param end the new end
	 * @param config the new configuration
	 * @return success
	 */
	public boolean updateClassroom(String roomId, String name, String description, Date begin, Date end, VCConfiguration config);
	
	/**
	 * Delete an existing virtual classroom
	 * @param roomId
	 * @param config
	 * @return success
	 */
	public boolean removeClassroom(String roomId, VCConfiguration config);
	
	/**
	 * Create user specific access url for the virtual classroom. Maybe the user
	 * must authenticated before.
	 * @param roomId
	 * @param identity
	 * @param config
	 * @return url
	 */
	public URL createClassroomUrl(String roomId, Identity identity, VCConfiguration config);
	
	/**
	 * Create guest access url for the virtual classroom. Dependent on the
	 * implementation the url can be user specific (e.g. to pre-set the username)
	 * @param roomId
	 * @param identity
	 * @param config
	 * @return url
	 */
	public URL createClassroomGuestUrl(String roomId, Identity identity, VCConfiguration config);
	
	/**
	 * Check whether the virtual classroom exists or not.
	 * @param roomId
	 * @param config
	 * @return <code>true</code> if the classroom exists, <code>false</code> otherwise
	 */
	public boolean existsClassroom(String roomId, VCConfiguration config);
	
	/**
	 * Login the user. Dependent on the implemenation the password can be <code>null</code>.
	 * If this is the case, the implementation can try to login automatically generated
	 * users with a default password or a password that's build up by a rule.
	 * @param identity
	 * @param password
	 * @return success
	 */
	public boolean login(Identity identity, String password);
	
	/**
	 * Create a new user. The user has moderator rights. If the user already exists,
	 * nothing is to do.
	 * @param identity
	 * @param roomId
	 * @return success
	 */
	public boolean createModerator(Identity identity, String roomId);
	
	/**
	 * Create a new user. The user has no specific rights. If the user already exists,
	 * nothing is to do.
	 * @param identity
	 * @param roomId
	 * @return success
	 */
	public boolean createUser(Identity identity, String roomId);
	
	/**
	 * Create a new guest. Dependent on the implementation the user must not be persistent.
	 * @param identity
	 * @param roomId
	 * @return success
	 */
	public boolean createGuest(Identity identity, String roomId);

	/**
	 * Create controller for using the virtual classroom.
	 * @param ureq
	 * @param wControl
	 * @param roomId
	 * @param name
	 * @param description
	 * @param isModerator
	 * @param config
	 * @return the controller to be embedded
	 */
	public Controller createDisplayController(UserRequest ureq, WindowControl wControl, String roomId, String name, String description,
			boolean isModerator, boolean readOnly, VCConfiguration config);
	
	/**
	 * Create controller for creation and configuration of the virtual classroom.
	 * @param ureq
	 * @param wControl
	 * @param roomId
	 * @param config
	 * @return the controller to be embedded
	 */
	public Controller createConfigController(UserRequest ureq, WindowControl wControl, String roomId, VCConfiguration config);
	
	/**
	 * Create a new default configuration. This configuration must reflect all
	 * necessary settings to ensure that the virtual classroom will work.
	 * @return new configuration
	 */
	public VCConfiguration createNewConfiguration();
}
//</OLATCE-103>