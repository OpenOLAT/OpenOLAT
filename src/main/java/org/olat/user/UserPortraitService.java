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
package org.olat.user;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.instantMessaging.model.Presence;

/**
 * 
 * Initial date: Feb 3, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public interface UserPortraitService {
	
	public UserInfoProfileConfig createProfileConfig();
	
	public PortraitUser createPortraitUser(Long identityKey, String username, boolean portraitAvailable,
			String portraitCacheIdentifier, String initials, String initialsCss, String displayName, Presence presence);
	
	public PortraitUser createPortraitUser(Locale locale, Identity identity);

	public PortraitUser createAnonymousPortraitUser(Locale locale, String displayName);
	
	public PortraitUser createGuestPortraitUser(Locale locale);
	
	public PortraitUser createDeletedPortraitUser(Locale locale);
	
	public PortraitUser createUnknownPortraitUser(Locale locale);
	
	public List<PortraitUser> createPortraitUsers(Locale locale, Collection<Identity> identities);

}
