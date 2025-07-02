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
package org.olat.modules.openbadges.ui;

import org.olat.core.gui.components.table.LabelCellRenderer;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.OpenBadgesManager;

/**
 * Initial date: 2024-08-23<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgeAssertionStatusRenderer extends LabelCellRenderer {
	
	private final OpenBadgesManager openBadgesManager;

	public BadgeAssertionStatusRenderer(OpenBadgesManager openBadgesManager) {
		super();
		this.openBadgesManager = openBadgesManager;
	}

	@Override
	protected String getCellValue(Object val, Translator translator) {
		if (val instanceof BadgeAssertion badgeAssertion) {
			if (openBadgesManager.isBadgeAssertionExpired(badgeAssertion)) {
				return translator.translate("expired");
			}
			if (badgeAssertion.getStatus() != null) {
				return translator.translate("assertion.status." + badgeAssertion.getStatus().name());
			}
		}
		return "";
	}

	@Override
	protected String getIconCssClass(Object val) {
		return "";
	}

	@Override
	protected String getElementCssClass(Object val) {
		if (val instanceof BadgeAssertion badgeAssertion) {
			if (openBadgesManager.isBadgeAssertionExpired(badgeAssertion)) {
				return "o_badge_assertion_status_expired";
			}
			if (badgeAssertion.getStatus() != null) {
				return "o_badge_assertion_status_" + badgeAssertion.getStatus().name();
			}
		}
		return null;
	}
}
