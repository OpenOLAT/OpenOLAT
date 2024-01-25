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
package org.olat.modules.quality.generator;

import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 1 Dec 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public enum QualityPreviewStatus {
	
	dataCollection,
	regular,
	changed,
	blacklist;
	
	public static String getTranslatedStatus(Translator translator, QualityPreviewStatus status) {
		return switch (status) {
		case dataCollection -> translator.translate("preview.status.data.collection");
		case regular -> translator.translate("preview.status.regular");
		case changed -> translator.translate("preview.status.changed");
		case blacklist -> translator.translate("preview.status.blacklist");
		default -> null;
		};
	}
	
	public static String getIconCss(QualityPreviewStatus status) {
		return switch (status) {
		case dataCollection -> "o_icon_qual_prev_data_collection";
		case regular -> "o_icon_qual_prev_regular";
		case changed -> "o_icon_qual_prev_changed";
		case blacklist -> "o_icon_qual_prev_blacklisted";
		default -> null;
		};
	}

	public static String getElementLightCss(QualityPreviewStatus status) {
		return switch (status) {
		case dataCollection -> "o_qual_prev_data_collection_light";
		case regular -> "o_qual_prev_regular_light";
		case changed -> "o_qual_prev_changed_light";
		case blacklist -> "o_qual_prev_blacklisted_light";
		default -> null;
		};
	}

}
