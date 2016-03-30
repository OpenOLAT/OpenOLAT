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

import javax.servlet.http.HttpServletRequest;

import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.user.DisplayPortraitController.Display;

/**
 * 
 * Initial date: 03.12.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserAvatarMapper implements Mapper {
	private static final String POSTFIX_SMALL = "/portrait_small.jpg";
	private static final String POSTFIX_LARGE = "/portrait.jpg";
	
	private final UserManager userManager;
	private final DisplayPortraitManager portraitManager;
	private final boolean useLarge;
	private final Display displayImage;
	
	public UserAvatarMapper(boolean useLargePortrait) {
		this(useLargePortrait, Display.portrait);
	}
	
	public UserAvatarMapper(boolean useLargePortrait, Display displayImage) {
		this.displayImage = displayImage;
		useLarge = useLargePortrait;
		portraitManager = DisplayPortraitManager.getInstance();
		userManager = CoreSpringFactory.getImpl(UserManager.class);
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		if(relPath != null && relPath.endsWith(POSTFIX_LARGE) || relPath.endsWith(POSTFIX_SMALL)) {
			if(relPath.startsWith("/")) {
				relPath = relPath.substring(1, relPath.length());
			}
			
			int endKeyIndex = relPath.indexOf('/');
			if(endKeyIndex > 0) {
				String idKey = relPath.substring(0, endKeyIndex);
				Long key = Long.parseLong(idKey);
				String username = userManager.getUsername(key);
				if(displayImage == Display.portrait) {
					if (useLarge) {
						return portraitManager.getBigPortraitResource(username);					
					} else {					
						return portraitManager.getSmallPortraitResource(username);
					}
				} else if(displayImage == Display.logo) {
					if (useLarge) {
						return portraitManager.getBigLogoResource(username);					
					} else {					
						return portraitManager.getSmallLogoResource(username);
					}
				} 
			}
		}
		return null;
	}
	
	public String createPathFor(String mapperPath, Identity identity) {
		return mapperPath + "/" + identity.getKey() + (useLarge ? POSTFIX_LARGE : POSTFIX_SMALL); 
	}
}
