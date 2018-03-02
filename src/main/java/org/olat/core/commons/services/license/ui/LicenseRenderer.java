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
package org.olat.core.commons.services.license.ui;

import java.util.Locale;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 26.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LicenseRenderer implements FlexiCellRenderer {
	
	private final String idPrefix;
	private final Locale locale;
	
	public LicenseRenderer(Locale locale) {
		this(locale, null);
	}
	
	public LicenseRenderer(Locale locale, String idPrefix) {
		this.locale = locale;
		this.idPrefix = idPrefix;
	}
	
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if (cellValue instanceof License) {
			License license = (License) cellValue;
			LicenseType licenseType = license.getLicenseType();
			if (renderer == null) {
				// render for export
				if (licenseType != null) {
					target.append(LicenseUIFactory.translate(licenseType, locale));
				}
			} else {
				target.append("<div");
				target.append(" style='white-space: nowrap;'");
				String hoverText = getHoverText(licenseType);
				if (StringHelper.containsNonWhitespace(hoverText)) {
					target.append(" title=\"");
					target.append(StringEscapeUtils.escapeHtml(hoverText));
				}
				target.append("\">");
				target.append("<i");
				String id = getId(row);
				if (StringHelper.containsNonWhitespace(id)) {
					target.append(" id='").append(id).append("'");
				}
				target.append(" class='").append(getCssClass(licenseType)).append("'> </i>");
				target.append("</div>");	
			}
		}
	}

	protected String getCssClass(LicenseType licenseType) {
		if (licenseType != null) {
			return "o_icon o_icon-lg " + LicenseUIFactory.getCssOrDefault(licenseType);
		}
		return null;
	}
	
	private String getHoverText(LicenseType licenseType) {
		if (licenseType != null) {
			return LicenseUIFactory.translate(licenseType, locale);
		}
		return null;
	}

	private String getId(int row) {
		if (StringHelper.containsNonWhitespace(idPrefix)) {
			return idPrefix + String.valueOf(row);
		}
		return null;
	}

}
