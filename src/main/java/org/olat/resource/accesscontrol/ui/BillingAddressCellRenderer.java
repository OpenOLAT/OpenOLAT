/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.resource.accesscontrol.ui;

import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: Mar 2, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class BillingAddressCellRenderer implements FlexiCellRenderer {
	
	private final Translator baTranslator;
	private final boolean warningEnabled;
	
	public BillingAddressCellRenderer(Locale locale) {
		this(locale, false);
	}
			
	public BillingAddressCellRenderer(Locale locale, boolean missingAddressWarningEnabled) {
		this.baTranslator = Util.createPackageTranslator(BillingAddressCellRenderer.class, locale);
		this.warningEnabled = missingAddressWarningEnabled;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if (cellValue instanceof BillingAddressCellValue bacValue) {
			if (warningEnabled && bacValue.isNoBillingAddressAvailable()) {
				appendWarningIcon(renderer, target);
				target.append(baTranslator.translate("billing.address.not.available"));
			} else if (warningEnabled && bacValue.isMultiBillingAddressAvailable()) {
				appendWarningIcon(renderer, target);
				target.append(baTranslator.translate("billing.address.multi.available"));
			} else if (bacValue.isBillingAddressProposal()) {
				appendWarningIcon(renderer, target);
				target.append(baTranslator.translate("billing.address.proposal"));
			} else if (StringHelper.containsNonWhitespace(bacValue.getBillingAddressIdentifier())) {
				target.append(bacValue.getBillingAddressIdentifier());
			}
		}
	}

	private void appendWarningIcon(Renderer renderer, StringOutput target) {
		if (renderer != null) {
			// not in export
			target.append("<i class=\"o_icon o_icon_important\"> </i> ");
		}
	}
	
	public interface BillingAddressCellValue {
		
		boolean isNoBillingAddressAvailable();
		
		boolean isMultiBillingAddressAvailable();

		boolean isBillingAddressProposal();
		
		String getBillingAddressIdentifier();
		
	}

}
