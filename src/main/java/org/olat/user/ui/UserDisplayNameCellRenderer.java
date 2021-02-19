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
package org.olat.user.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 19 Feb 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UserDisplayNameCellRenderer implements FlexiCellRenderer {
	
	private static final UserDisplayNameCellRenderer INSTANCE = new UserDisplayNameCellRenderer();
	
	private final UserManager userManager;
	
	public static UserDisplayNameCellRenderer get() {
		return INSTANCE;
	}
	
	private UserDisplayNameCellRenderer() {
		this.userManager = CoreSpringFactory.getImpl(UserManager.class);
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if (cellValue instanceof Identity) {
			String userDisplayName = userManager.getUserDisplayName(((Identity)cellValue).getKey());
			target.append(userDisplayName);
		} else if (cellValue instanceof User) {
			String userDisplayName = userManager.getUserDisplayName(((User)cellValue));
			target.append(userDisplayName);
		}
	}

}
