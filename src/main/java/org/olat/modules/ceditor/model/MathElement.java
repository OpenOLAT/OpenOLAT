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
package org.olat.modules.ceditor.model;

import java.beans.Transient;

import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.PageElement;

/**
 * 
 * Initial date: 22 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface MathElement extends PageElement {

	public String getLayoutOptions();

	public void setLayoutOptions(String options);

	public String getContent();
	
	public void setContent(String content);

	@Transient
	public default MathSettings getMathSettings() {
		if (StringHelper.containsNonWhitespace(getLayoutOptions())) {
			return ContentEditorXStream.fromXml(getLayoutOptions(), MathSettings.class);
		}
		return new MathSettings();
	}

	@Transient
	public default void setMathSettings(MathSettings mathSettings) {
		String settingsXml = ContentEditorXStream.toXml(mathSettings);
		setLayoutOptions(settingsXml);
	}

	public static String toCssClass(MathSettings mathSettings) {
		return toCssClass(mathSettings, "o_ce_math_run");
	}

	public static String toCssClass(MathSettings mathSettings, String cssClass) {
		String css = StringHelper.containsNonWhitespace(cssClass) ? cssClass + " " : "";
		if (mathSettings != null && mathSettings.getLayoutSettings() != null) {
			return css + mathSettings.getLayoutSettings().getCssClass(false);
		}
		return css + BlockLayoutSettings.getPredefined().getCssClass(false);
	}
}
