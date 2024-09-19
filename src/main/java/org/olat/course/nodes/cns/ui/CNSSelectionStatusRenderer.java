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
package org.olat.course.nodes.cns.ui;

import org.olat.core.gui.components.table.LabelCellRenderer;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 18 Sep 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CNSSelectionStatusRenderer extends LabelCellRenderer {
	
	private final boolean light;
	
	public CNSSelectionStatusRenderer(boolean light) {
		this.light = light;
	}
	
	@Override
	protected boolean isLabelLight() {
		return light;
	}
	
	@Override
	protected String getCellValue(Object val, Translator translator) {
		if (val instanceof CNSSelectionStatus status) {
			return switch (status) {
			case inProgress -> translator.translate("status.in.progress");
			case selected -> translator.translate("status.selected");
			case done -> translator.translate("status.done");
			};
		}
		return null;
	}
	
	@Override
	protected String getIconCssClass(Object val) {
		if (val instanceof CNSSelectionStatus status) {
			return switch (status) {
			case inProgress -> "o_icon_cns_status_in_progress";
			case selected -> "o_icon_cns_status_selected";
			case done -> "o_icon_cns_status_done";
			};
		}
		return null;
	}
	
	@Override
	protected String getElementCssClass(Object val) {
		if (val instanceof CNSSelectionStatus status) {
			if (light) {
				return switch (status) {
				case inProgress -> "o_cns_labled_light_status_in_progress";
				case selected -> "o_cns_labled_light_status_selected";
				case done -> "o_cns_labled_light_status_done";
				};
			}
			return switch (status) {
			case inProgress -> "o_cns_labled_status_in_progress";
			case selected -> "o_cns_labled_status_selected";
			case done -> "o_cns_labled_status_done";
			};
		}
		return null;
	}

}
