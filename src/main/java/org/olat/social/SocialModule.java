/**
 * <a href=“http://www.openolat.org“>
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
 * 13.09.2012 by frentix GmbH, http://www.frentix.com
 * <p>
 **/

package org.olat.social;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * <h3>Description:</h3>
 * <p>
 * The social module offers integration features into social networks. Currently
 * there is only a perma-link share feature available, expect a deeper
 * integration with social networks for the future.
 * <p>
 * Initial Date: 12.09.2012 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
@Service("socialModule")
public class SocialModule extends AbstractSpringModule {
	private static final String SHARE_ENABLED = "social.share.enabled";
	private static final String SHARE_LINK_BUTTONS = "social.share.link.buttons";
	private static final String SHARE_LINK_BUTTONS_AVAILABLE = "twitter,facebook,google,delicious,digg,mail,link";
	
	// the share enabled config saved in the persisted properties
	@Value("${social.share.enabled:false}")
	private boolean shareEnabled;
	// the share links enabled config separated by comma saved in the persisted properties
	@Value("${social.share.link.buttons}")
	private String shareLinkButtonsEnabled;
	
	// the list of all configurable share links
	private List<String> shareLinkButtonsAvailableList;
	// the list of enabled link buttons for your convenience as list object
	private List<String> shareLinkButtonsEnabledList;
	
	@Autowired
	public SocialModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}
	
	/* (non-Javadoc)
	 * @see org.olat.core.configuration.AbstractOLATModule#init()
	 */
	@Override
	public void init() {		
		// Override Enable/Disable social sharing globally from persisted properties config
		String shareEnabledFromPersistedProperties = getStringPropertyValue(SHARE_ENABLED, true);
		if(StringHelper.containsNonWhitespace(shareEnabledFromPersistedProperties)) {
			shareEnabled = "true".equals(shareEnabledFromPersistedProperties);
		}
		// Init the available share link buttons implemented in the footer template
		shareLinkButtonsAvailableList = new ArrayList<>(Arrays.asList(SHARE_LINK_BUTTONS_AVAILABLE.split(",")));
		// Init the enabled share link buttons from the configuration
		shareLinkButtonsEnabledList = new ArrayList<>();
		
		// Override enabled share link buttons config from persisted properties
		String shareLinkButtonsEnabledFromPersistedProperties = getStringPropertyValue(SHARE_LINK_BUTTONS, true);
		if(StringHelper.containsNonWhitespace(shareLinkButtonsEnabledFromPersistedProperties)) {
			shareLinkButtonsEnabled = shareLinkButtonsEnabledFromPersistedProperties;
		}
		// Convert to a list to simplify access later in the code 
		if(StringHelper.containsNonWhitespace(shareLinkButtonsEnabled)) {
			String[] shareLinks = shareLinkButtonsEnabled.split(",");
			for (String shareLink: shareLinks) {
				// check if this share link is implemented
				shareLink = shareLink.trim().toLowerCase();
				if (shareLinkButtonsAvailableList.contains(shareLink)) {
					shareLinkButtonsEnabledList.add(shareLink);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.olat.core.configuration.AbstractOLATModule#initFromChangedProperties()
	 */
	@Override
	protected void initFromChangedProperties() {
		init();
	}

	/**
	 * @return true: sharing with social networks is enabled; false: sharing
	 *         with social networks is disabled
	 */
	public boolean isShareEnabled() {
		return shareEnabled;
	}

	/**
	 * Enable/Disable the sharing with social networks. This change will be
	 * saved in a persisted property and thus stay active after a restart.
	 * 
	 * @param shareEnabled
	 */
	public void setShareEnabled(boolean shareEnabled) {
		if(this.shareEnabled != shareEnabled) {
			setStringProperty(SHARE_ENABLED, Boolean.toString(shareEnabled), true);
		}
	}
	
	
	/**
	 * Set the list of enabled share links. This change will be saved in a
	 * persisted property and thus stay active after a restart.
	 * 
	 * @param enabledLinks List of share link names, order is important.
	 */
	public void setEnabledShareLinkButtons(List<String> enabledLinks) {
		String enabledAsString = "";
		Iterator<String> it = enabledLinks.iterator();
		while (it.hasNext()) {
			String link = it.next();
			enabledAsString += link;
			if (it.hasNext()) {
				enabledAsString += ",";
			}
		}
		if (!enabledAsString.equals(this.shareLinkButtonsEnabled)) {
			setStringProperty(SHARE_LINK_BUTTONS, enabledAsString, true);
		}
		
	}
	
	/**
	 * @return The list of the enabled share link buttons as strings. Order of
	 *         the items in the list matters.
	 */
	public List<String> getEnabledShareLinkButtons() {
		return shareLinkButtonsEnabledList;
	}
}
