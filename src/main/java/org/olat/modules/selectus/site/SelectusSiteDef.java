/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.site;

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.AbstractSiteDefinition;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.util.StringHelper;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.dispatcher.PublicFeedbackDispatcher;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  21 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class SelectusSiteDef extends AbstractSiteDefinition {

	@Override
	protected SiteInstance createSite(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		if(!ureq.getUserSession().getRoles().isGuestOnly()
				&& !StringHelper.containsNonWhitespace((String)ureq.getUserSession().getEntry(PublicFeedbackDispatcher.PUBLIC_FEEDBACK_ID))) {
			Locale locale = ureq.getLocale();
			return new SelectusSite(locale);
		}
		return null;
	}
	
	@Override
	public boolean isEnabled() {
		RecruitingModule selectusModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		return selectusModule.isEnabled() && super.isEnabled();
	}
	
	@Override
	public boolean isFeatureEnabled() {
		RecruitingModule selectusModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		return selectusModule.isEnabled();
	}
}
