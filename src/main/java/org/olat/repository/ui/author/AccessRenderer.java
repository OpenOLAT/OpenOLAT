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
package org.olat.repository.ui.author;

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.login.LoginModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryLight;
import org.olat.repository.RepositoryService;

/**
 * 
 * Initial date: 29.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AccessRenderer implements FlexiCellRenderer {
	
	private final boolean guestLoginEnabled;
	private final Translator translator;
	
	public AccessRenderer(Locale locale) {
		guestLoginEnabled = CoreSpringFactory.getImpl(LoginModule.class).isGuestLoginLinksEnabled();
		translator = Util.createPackageTranslator(RepositoryService.class, locale);
	}

	@Override
	public void render(Renderer renderer, StringOutput sb, Object val,
			int row, FlexiTableComponent source, URLBuilder ubu, Translator trans)  {
		if(val instanceof RepositoryEntryLight) {
			RepositoryEntryLight re = (RepositoryEntryLight)val;
			render(sb, re.getAccess(), re.isMembersOnly());
		} else if(val instanceof RepositoryEntry) {
			RepositoryEntry re = (RepositoryEntry)val;
			render(sb, re.getAccess(), re.isMembersOnly());
		}
	}
	
	private void render(StringOutput sb, int access, boolean membersOnly) {
		if(access == RepositoryEntry.DELETED) {
			sb.append(translator.translate("table.header.access.deleted"));
		} else if(membersOnly) {
			sb.append(translator.translate("table.header.access.membersonly")); 
		} else {
			switch (access) {
				case RepositoryEntry.DELETED: {
					sb.append(translator.translate("table.header.access.deleted"));
					break;
				}
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
