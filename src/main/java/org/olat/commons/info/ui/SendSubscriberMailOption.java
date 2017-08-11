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

package org.olat.commons.info.ui;

import java.util.List;
import java.util.Locale;

import org.olat.commons.info.InfoMessageFrontendManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Util;

/**
 * 
 * Description:<br>
 * Send mail to the subscribers
 * 
 * <P>
 * Initial Date:  29 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SendSubscriberMailOption implements SendMailOption {

	private final String label;
	private final String resSubPath;
	private final OLATResourceable ores;
	
	public SendSubscriberMailOption(OLATResourceable ores, String resSubPath, Locale locale) {
		this.ores = ores;
		this.resSubPath = resSubPath;
		label = Util.createPackageTranslator(SendSubscriberMailOption.class, locale)
				.translate("wizard.step1.send_option.subscriber");
	}
	
	@Override
	public String getOptionKey() {
		return WizardConstants.SEND_MAIL_SUBSCRIBERS;
	}

	@Override
	public String getOptionName() {
		return label;
	}

	@Override
	public List<Identity> getSelectedIdentities() {
		return CoreSpringFactory.getImpl(InfoMessageFrontendManager.class).getInfoSubscribers(ores, resSubPath);
	}
}
