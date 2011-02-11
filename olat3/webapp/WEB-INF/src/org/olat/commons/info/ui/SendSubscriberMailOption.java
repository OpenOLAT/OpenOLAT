/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.commons.info.ui;

import java.util.List;
import java.util.Locale;

import org.olat.commons.info.manager.InfoMessageFrontendManager;
import org.olat.core.gui.translator.Translator;
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

	private final OLATResourceable ores;
	private final String resSubPath;
	private final InfoMessageFrontendManager messageManager;
	
	public SendSubscriberMailOption(OLATResourceable ores, String resSubPath, InfoMessageFrontendManager messageManager) {
		this.ores = ores;
		this.resSubPath = resSubPath;
		this.messageManager = messageManager;
	}
	
	@Override
	public String getOptionKey() {
		return WizardConstants.SEND_MAIL_SUBSCRIBERS;
	}

	@Override
	public String getOptionTranslatedName(Locale locale) {
		Translator translator = Util.createPackageTranslator(SendSubscriberMailOption.class, locale);
		return translator.translate("wizard.step1.send_option.subscriber");
	}

	@Override
	public List<Identity> getSelectedIdentities() {
		List<Identity> identities = messageManager.getInfoSubscribers(ores, resSubPath);
		return identities;
	}
}
