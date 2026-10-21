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
package org.olat.core.commons.services.license.ui;

import java.util.Locale;

import org.olat.core.commons.services.license.License;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 *
 * Initial date: 21.10.2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class LicenseCellRenderer extends StaticFlexiCellRenderer {
	
	public static final String CMD_LICENSE = "license";
	
	private final LicenseRenderer licenseRenderer;
	
	public LicenseCellRenderer(Locale locale) {
		this(new LicenseRenderer(locale));
	}
	
	private LicenseCellRenderer(LicenseRenderer licenseRenderer) {
		super(CMD_LICENSE, licenseRenderer);
		this.licenseRenderer = licenseRenderer;
		setAriaDialogOpener();
	}
	
	public static String getId(int row) {
		return "o_c" + CMD_LICENSE + "_" + row;
	}
	
	@Override
	protected String getId(Object cellValue, int row, FlexiTableComponent source) {
		return getId(row);
	}
	
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if (cellValue instanceof License) {
			super.render(renderer, target, cellValue, row, source, ubu, translator);
		} else {
			licenseRenderer.render(renderer, target, cellValue, row, source, ubu, translator);
		}
	}
}
