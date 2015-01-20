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
package org.olat.repository.ui;

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.login.LoginModule;
import org.olat.repository.RepositoryEntry;

/**
 * Render the access rule translated
 * 
 * Initial date: 26.07.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryAccessColumnDescriptor implements CustomCellRenderer {
	
	private final Translator translator;
	private final boolean guestLoginEnabled;
	
	public RepositoryEntryAccessColumnDescriptor(Translator translator) {
		this.translator = translator;
		guestLoginEnabled = CoreSpringFactory.getImpl(LoginModule.class).isGuestLoginLinksEnabled();
	}

	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		if(val instanceof RepositoryEntry) {
			RepositoryEntry re = (RepositoryEntry)val;
			if(re.isMembersOnly()) {
				sb.append(translator.translate("table.header.access.membersonly")); 
			} else {
				switch (re.getAccess()) {
					case RepositoryEntry.ACC_OWNERS:
						sb.append(translator.translate("table.header.access.owner"));
						break;
					case RepositoryEntry.ACC_OWNERS_AUTHORS:
						sb.append(translator.translate("table.header.access.author"));
						break;
					case RepositoryEntry.ACC_USERS:
						sb.append(translator.translate("table.header.access.user"));
						break;
					case RepositoryEntry.ACC_USERS_GUESTS: {
						if(!guestLoginEnabled) {
							sb.append(translator.translate("table.header.access.user"));
						} else {
							sb.append(translator.translate("table.header.access.guest"));
						}
						break;
					} default:						
						// OLAT-6272 in case of broken repo entries with no access code
						// return error instead of nothing
						sb.append("ERROR");
				}
			}
		}
	}
}
