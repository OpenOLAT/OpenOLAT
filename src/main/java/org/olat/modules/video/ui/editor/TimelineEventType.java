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
package org.olat.modules.video.ui.editor;

/**
 * Initial date: 2022-11-21<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public enum TimelineEventType {
	CHAPTER("timelineEventType.chapter", "o_icon_fa6_c"),
	ANNOTATION("timelineEventType.annotation", "o_icon_fa6_a"),
	QUIZ("timelineEventType.quiz", "o_icon_fa6_q"),
	SEGMENT("timelineEventType.segment", "o_icon_fa6_s"),
	VIDEO("timelineEventType.video", "o_icon_fa6_v");

	private final String i18nKey;
	private final String icon;

	TimelineEventType(String i18nKey, String icon) {
		this.i18nKey = i18nKey;
		this.icon = icon;
	}

	public String getI18nKey() {
		return i18nKey;
	}

	public String getIcon() {
		return icon;
	}
}
