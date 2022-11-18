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
package org.olat.user.ui.admin;

import java.util.Locale;

import org.olat.admin.user.UserAdminController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 20 déc. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityStatusCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public IdentityStatusCellRenderer(Translator translator) {
		this.translator = translator;
	}
	
	public IdentityStatusCellRenderer(Locale  locale) {
		translator = Util.createPackageTranslator(UserAdminController.class, locale);
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		if(Identity.STATUS_PERMANENT.equals(cellValue)) {
			render(renderer, target, "o_icon_identity_permanent", "rightsForm.status.permanent");
		} else if(Identity.STATUS_LOGIN_DENIED.equals(cellValue)) {
			render(renderer, target, "o_icon_identity_login_denied", "rightsForm.status.login_denied");
		} else if(Identity.STATUS_PENDING.equals(cellValue)) {
			render(renderer, target, "o_icon_identity_pending", "rightsForm.status.pending");
		} else if(Identity.STATUS_INACTIVE.equals(cellValue)) {
			render(renderer, target, "o_icon_identity_inactive", "rightsForm.status.inactive");
		} else if(Identity.STATUS_DELETED.equals(cellValue)) {
			render(renderer, target, "o_icon_identity_deleted", "rightsForm.status.deleted");
		}
	}

	private void render(Renderer renderer, StringOutput target, String icon, String tooltipKey) {
		if(renderer == null) {
			target.append(translator.translate(tooltipKey));
		} else {
			target.append("<span title='").append(translator.translate(tooltipKey))
			      .append("'><i class='o_icon o_icon-fw ").append(icon).append("'> </i></span>");
		}
	}
}
