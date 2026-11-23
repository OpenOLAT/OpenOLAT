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
package org.olat.modules.curriculum.ui.component;

import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryService;
import org.olat.resource.accesscontrol.ParticipantsAvailability;
import org.olat.resource.accesscontrol.ParticipantsAvailability.ParticipantsAvailabilityNum;

/**
 * 
 * Initial date: Apr 1, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ParticipantsAvailabilityNumRenderer implements FlexiCellRenderer {
	
	private final Translator reTranslator;
	
	public ParticipantsAvailabilityNumRenderer(Locale locale) {
		reTranslator = Util.createPackageTranslator(RepositoryService.class, locale);
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if (cellValue instanceof ParticipantsAvailabilityNum availabilityNum) {
			if (renderer == null) {
				if (getStateCss(availabilityNum.availability()) != null) {
					target.append(ParticipantsAvailability.getText(reTranslator, availabilityNum));
				} else if (availabilityNum.numAvailable() == 1) {
					target.append(reTranslator.translate("book.seats.left.single"));
				} else if (availabilityNum.numAvailable() < Integer.MAX_VALUE) {
					target.append(reTranslator.translate("book.seats.left.multi", String.valueOf(availabilityNum.numAvailable())));
				}
			} else {
				String stateCss = getStateCss(availabilityNum.availability());
				if (stateCss != null) {
					target.append("<span class=\"o_labeled_light ").append(stateCss).append(" o_nowrap\">");
					target.append(getAvailabilityText(availabilityNum));
					target.append("</span>");
				} else if (availabilityNum.numAvailable() == 1) {
					target.append(reTranslator.translate("book.seats.left.single"));
				} else if (availabilityNum.numAvailable() < Integer.MAX_VALUE) {
					target.append(reTranslator.translate("book.seats.left.multi", String.valueOf(availabilityNum.numAvailable())));
				}
			}
		}
	}
	
	private String getStateCss(ParticipantsAvailability availability) {
		return switch (availability) {
		case overbooked -> "o_state_danger";
		case fullyBooked -> "o_state_inactive";
		case fewLeft -> "o_state_warning";
		default -> null;
		};
	}
	
	private String getAvailabilityText(ParticipantsAvailabilityNum participantsAvailabilityNum) {
		return "<i class=\"o_icon o_icon-fw " + ParticipantsAvailability.getIconCss(participantsAvailabilityNum) + "\"> </i> " 
				+ ParticipantsAvailability.getText(reTranslator, participantsAvailabilityNum);
	}

}
