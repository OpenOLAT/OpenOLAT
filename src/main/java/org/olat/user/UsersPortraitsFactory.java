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
package org.olat.user;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.user.UsersPortraitsComponent.PortraitUser;

/**
 * 
 * Initial date: 7 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UsersPortraitsFactory {
	
	public static UsersPortraitsComponent create(UserRequest ureq, String name, VelocityContainer vc) {
		return create(ureq, name, vc, null, null);
	}
	
	public static UsersPortraitsComponent create(UserRequest ureq,  String name, VelocityContainer vc, ComponentEventListener listener, MapperKey mapperKey) {
		UsersPortraitsComponent usersPortraitsComponent = new UsersPortraitsComponent(ureq, name, mapperKey);
		if (listener != null) {
			usersPortraitsComponent.addListener(listener);
		}
		if (vc != null) {
			vc.put(usersPortraitsComponent.getComponentName(), usersPortraitsComponent);
		}
		return usersPortraitsComponent;
	}
	
	public static List<PortraitUser> createPortraitUsers(List<Identity> identities) {
		DisplayPortraitManager portraitManager = CoreSpringFactory.getImpl(DisplayPortraitManager.class);
		UserManager userManager = CoreSpringFactory.getImpl(UserManager.class);
		
		return identities.stream()
				.map(identity -> createPortraitUser(portraitManager, userManager, identity))
				.collect(Collectors.toList());
	}

	private static PortraitUser createPortraitUser(DisplayPortraitManager portraitManager, UserManager userManager, Identity identity) {
		boolean portraitAvailable = portraitManager.hasPortrait(identity);
		
		String portraitCssClass;
		String gender = identity.getUser().getProperty(UserConstants.GENDER, Locale.ENGLISH);
		if ("male".equalsIgnoreCase(gender)) {
			portraitCssClass = DisplayPortraitManager.DUMMY_MALE_BIG_CSS_CLASS;
		} else if ("female".equalsIgnoreCase(gender)) {
			portraitCssClass = DisplayPortraitManager.DUMMY_FEMALE_BIG_CSS_CLASS;
		} else {
			portraitCssClass = DisplayPortraitManager.DUMMY_BIG_CSS_CLASS;
		}
		
		String userDisplayName = userManager.getUserDisplayName(identity);
		
		return createPortraitUser(identity.getKey(), portraitAvailable, portraitCssClass, userDisplayName);
	}
	
	public static final PortraitUser createPortraitUser(Long identityKey, boolean portraitAvailable, String portraitCssClass, String displayName) {
		return new PortraitUserImpl(identityKey, portraitAvailable, portraitCssClass, displayName);
	}
	
	private static final class PortraitUserImpl implements PortraitUser {
		
		private final Long identityKey;
		private final boolean portraitAvailable;
		private final String portraitCssClass;
		private final String displayName;
		
		public PortraitUserImpl(Long identityKey, boolean portraitAvailable, String portraitCssClass, String displayName) {
			this.identityKey = identityKey;
			this.portraitAvailable = portraitAvailable;
			this.portraitCssClass = portraitCssClass;
			this.displayName = displayName;
		}

		@Override
		public Long getIdentityKey() {
			return identityKey;
		}
		
		@Override
		public boolean isPortraitAvailable() {
			return portraitAvailable;
		}

		@Override
		public String getPortraitCssClass() {
			return portraitCssClass;
		}

		@Override
		public String getDisplayName() {
			return displayName;
		}
		
	}

}
