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

import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 26.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LicenseRenderer implements FlexiCellRenderer {
	
	private final Locale locale;
	private final Translator translator;
	
	public LicenseRenderer(Locale locale) {
		this.locale = locale;
		translator = Util.createPackageTranslator(LicenseAdminConfigController.class, locale);
	}
	
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if (cellValue instanceof License) {
			License license = (License) cellValue;
			if (renderer == null) {
				// render for export
				target.append(LicenseUIFactory.translate(license.getLicenseType(), locale));
			} else {
				render(target, license);	
			}
		}
	}

	public void render(StringOutput sb, License license) {
		LicenseType licenseType = license.getLicenseType();
		long id = CodeHelper.getRAMUniqueID();
		
		// license icon
		sb.append("<a id='o_lic_").append(id).append("' href='javascript:;'><i class='o_icon o_icon-fw o_icon_lic_small ");
		sb.append(LicenseUIFactory.getCssOrDefault(licenseType));
		sb.append("'></i></a>");
		
		// popup with license informations
		sb.append("<div id='o_lic_pop_").append(id).append("' style='display:none;'><div>");
		appendStaticControl(sb, "license.popup.type", LicenseUIFactory.translate(licenseType, locale),
				LicenseUIFactory.getCssOrDefault(licenseType));
		String licensor = license.getLicensor() != null? license.getLicensor(): "";
		appendStaticControl(sb, "license.popup.licensor", licensor);
		appendStaticControl(sb, "license.popup.text", LicenseUIFactory.getFormattedLicenseText(license));
		sb.append("</div>");
		
		// JavaScript to pup up the popup
		sb.append("<script type='text/javascript'>")
	      .append("/* <![CDATA[ */")
		  .append("jQuery(function() {\n")
		  .append("  o_popover('o_lic_").append(id).append("','o_lic_pop_").append(id).append("','top');\n")
		  .append("});")
		  .append("/* ]]> */")
		  .append("</script>");
	}

	private void appendStaticControl(StringOutput sb, String i18n, String text) {
		sb.append("<label class='control-label'>").append(translator.translate(i18n)) .append("</label>");
		sb.append("<p class='form-control-static'>").append(text).append("</p>");
	}
	
	private void appendStaticControl(StringOutput sb, String i18n, String text, String immageCss) {
		sb.append("<label class='control-label'>").append(translator.translate(i18n)) .append("</label>");
		sb.append("<p class='form-control-static'>");
		sb.append("<i class='o_icon ").append(immageCss).append("'> </i>");
		sb.append("<span> ").append(text).append("</span>");
		sb.append("</p>");
	}

}
