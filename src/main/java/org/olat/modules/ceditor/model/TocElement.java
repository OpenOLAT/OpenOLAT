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
package org.olat.modules.ceditor.model;

import java.beans.Transient;

import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.PageElement;

/**
 * Initial date: 15 Apr 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public interface TocElement extends PageElement {

	String getLayoutOptions();

	void setLayoutOptions(String options);

	@Transient
	default TocSettings getTocSettings() {
		if (StringHelper.containsNonWhitespace(getLayoutOptions())) {
			return ContentEditorXStream.fromXml(getLayoutOptions(), TocSettings.class);
		}
		return new TocSettings();
	}

	@Transient
	default void setTocSettings(TocSettings settings) {
		String settingsXml = ContentEditorXStream.toXml(settings);
		setLayoutOptions(settingsXml);
	}
}
