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
import org.olat.core.util.filter.FilterFactory;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.PageElement;

/**
 * This is an abstract title element to use the generic
 * title / heading editor.
 * 
 * 
 * Initial date: 10 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface TitleElement extends PageElement {
	
	public String getLayoutOptions();
	
	public void setLayoutOptions(String options);
	
	public String getContent();
	
	public void setContent(String content);

	@Transient
	public default TitleSettings getTitleSettings() {
		if(StringHelper.containsNonWhitespace(getLayoutOptions())) {
			return ContentEditorXStream.fromXml(getLayoutOptions(), TitleSettings.class);
		}
		return new TitleSettings();	
	}

	@Transient
	public default void setTitleSettings(TitleSettings settings) {
		String settingsXml = ContentEditorXStream.toXml(settings);
		setLayoutOptions(settingsXml);
	}
	
	public static String toHtml(String content, TitleSettings settings) {
		if(settings != null && settings.getSize() > 0) {
			int size = settings.getSize();
			String text = FilterFactory.getHtmlTagsFilter().filter(content);
			return "<h" + size + ">" + text + "</h" + size + ">";
		}
		return content;
	}
	
	public static String toHtmlPlaceholder(String placeholderString, TitleSettings settings) {
		if (settings != null && settings.getSize() > 0) {
			int size = settings.getSize();
			String text = FilterFactory.getHtmlTagsFilter().filter(placeholderString);
			return "<h" + size + " class=\"o_title_placeholder\"><i class=\"o_icon o_icon-fw o_icon_header\"> </i> " + text + "</h" + size + ">";
		}
		return placeholderString;
	}

	public static String toHtmlForEditor(String content, TitleSettings settings) {
		if(settings != null && settings.getSize() > 0) {
			int size = settings.getSize();
			String text = FilterFactory.getHtmlTagsFilter().filter(content);
			return "<h" + size + " style='margin-top: 0; margin-bottom: 0;'>" + text + "</h" + size + ">";
		}
		return content;
	}

	public static String toCssClass(TitleSettings settings, String cssClass, boolean inForm) {
		String css = StringHelper.containsNonWhitespace(cssClass) ? cssClass + " " : "";
		if (settings != null && settings.getLayoutSettings() != null) {
			return css + settings.getLayoutSettings().getCssClass(inForm);
		}
		return css + BlockLayoutSettings.getPredefined().getCssClass(inForm);
	}

	public static String toCssClassWithMarkerClass(TitleSettings settings, boolean inForm) {
		return toCssClass(settings, "o_title_page_element", inForm);
	}
}
