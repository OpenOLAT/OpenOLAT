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
package org.olat.modules.selectus.dispatcher;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.util.i18n.I18nModule;

/**
 * Reference are limited to the default language (english).
 * 
 * 
 * Initial date: 11.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferenceDispatcher extends AbstractRecruitingDispatcher {
	
	public static final String REFERENCE_SOURCE = "reference";

	public ReferenceDispatcher() {
		super(REFERENCE_SOURCE);
	}
	
	@Override
	protected Locale getLang(UserRequest ureq) {
		return I18nModule.getDefaultLocale();
	}
}
