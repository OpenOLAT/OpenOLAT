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
package org.olat.repository.ui;

import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.AutomaticLifecycleService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryLifeCycleValue;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 juil. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ConfirmChangeLifecycleController extends ConfirmationController {
	
	private final String autoClose;
	private final String autoDelete;
	private final String autoDefinitivelyDelete;
	
	@Autowired
	private AutomaticLifecycleService lifecycleService;

	public ConfirmChangeLifecycleController(UserRequest ureq, WindowControl wControl,
			String message, String confirmation, String confirmButton, String cancel,
			String autoClose, String autoDelete, String autoDefinitivelyDelete) {
		super(ureq, wControl, message, confirmation, confirmButton, ButtonType.submitPrimary, cancel, false);
		setTranslator(Util.createPackageTranslator(ConfirmationController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.autoClose = autoClose;
		this.autoDelete = autoDelete;
		this.autoDefinitivelyDelete = autoDefinitivelyDelete;
		
		initForm(ureq);
	}

	@Override
	protected void initFormElements(FormLayoutContainer confirmCont) {
		StringBuilder impact = new StringBuilder();
		impact.append("<ul class='o_static_list'>");
		if(StringHelper.containsNonWhitespace(autoClose)) {
			RepositoryEntryLifeCycleValue autoCloseVal = RepositoryEntryLifeCycleValue.parse(autoClose);
			Date markerDate = autoCloseVal.limitDate(new Date());
			List<RepositoryEntry> entriesToClose = lifecycleService.getRepositoryEntriesToClose(markerDate);
			String i18nKey = entriesToClose.size() == 1
					? "confirmation.lifecycle.closed"
					: "confirmation.lifecycle.closed.plural";
			impact.append("<li>")
		          .append(translate(i18nKey, String.valueOf(entriesToClose.size())))
		          .append(" ").append(translate("confirmation.lifecycle.closed.details"))
		          .append("</li>");
		}
		
		if(StringHelper.containsNonWhitespace(autoDelete)) {
			RepositoryEntryLifeCycleValue autoDeleteVal = RepositoryEntryLifeCycleValue.parse(autoDelete);
			Date markerDate = autoDeleteVal.limitDate(new Date());
			List<RepositoryEntry> entriesToDelete = lifecycleService.getRepositoryEntriesToDelete(markerDate);
			String i18nKey = entriesToDelete.size() == 1
					? "confirmation.lifecycle.deleted"
					: "confirmation.lifecycle.deleted.plural";
			impact.append("<li>")
		          .append(translate(i18nKey, String.valueOf(entriesToDelete.size())))
		          .append(" ").append(translate("confirmation.lifecycle.deleted.details"))
		          .append("</li>");
		}
		
		if(StringHelper.containsNonWhitespace(autoDefinitivelyDelete)) {
			RepositoryEntryLifeCycleValue autoDefinitivelyDeleteVal = RepositoryEntryLifeCycleValue.parse(autoDefinitivelyDelete);
			Date markerDate = autoDefinitivelyDeleteVal.limitDate(new Date());
			List<RepositoryEntry> entriesToDefinitivelyDelete = lifecycleService.getRepositoryEntriesToDefinitivelyDelete(markerDate);
			String i18nKey = entriesToDefinitivelyDelete.size() == 1
					? "confirmation.lifecycle.definitively.deleted"
					: "confirmation.lifecycle.definitively.deleted.plural";
			impact.append("<li>")
		          .append(translate(i18nKey, String.valueOf(entriesToDefinitivelyDelete.size())))
		          .append(" ").append(translate("confirmation.lifecycle.definitively.deleted.details"))
		          .append("</li>");
		}

		impact.append("</ul>");
		
		StaticTextElement impactEl = uifactory.addStaticTextElement("confirmation.lifecycle.impacts", impact.toString(), confirmCont);
		impactEl.setDomWrapperElement(DomWrapperElement.div);
		impactEl.setVisible(impact.length() > 35);
	}
}
