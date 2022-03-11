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
package org.olat.instantMessaging.ui.component;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.instantMessaging.model.RosterChannelInfos.RosterStatus;

/**
 * 
 * Initial date: 24 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RosterStatusCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public RosterStatusCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		if(RosterStatus.active.equals(cellValue)) {
			renderStatus(target, "channel.status.active", "o_im_status_active");
		} else if(RosterStatus.request.equals(cellValue)) {
			renderStatus(target, "channel.status.request", "o_im_status_requested");
		} else if(RosterStatus.completed.equals(cellValue) || RosterStatus.ended.equals(cellValue)) {
			renderStatus(target, "channel.status.completed", "o_im_status_completed");
		}
	}
	
	private void renderStatus(StringOutput target, String i18nKey, String cssClass) {
		target.append("<span class='").append(cssClass).append(" o_labeled'>")
		      .append(translator.translate(i18nKey)).append("</span>");
	}
}
