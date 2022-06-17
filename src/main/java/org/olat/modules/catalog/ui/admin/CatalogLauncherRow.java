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
package org.olat.modules.catalog.ui.admin;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.updown.UpDown;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.CatalogLauncherHandler;

/**
 * 
 * Initial date: 2 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogLauncherRow {
	
	private final CatalogLauncher catalogLauncher;
	private CatalogLauncherHandler handler;
	private String translatedType;
	private String translatedName;
	private String details;
	private UpDown upDown;
	private FormLink toolsLink;

	public CatalogLauncherRow(CatalogLauncher catalogLauncher) {
		this.catalogLauncher = catalogLauncher;
	}

	public CatalogLauncher getCatalogLauncher() {
		return catalogLauncher;
	}

	public CatalogLauncherHandler getHandler() {
		return handler;
	}

	public void setHandler(CatalogLauncherHandler handler) {
		this.handler = handler;
	}

	public String getTranslatedType() {
		return translatedType;
	}

	public void setTranslatedType(String translatedType) {
		this.translatedType = translatedType;
	}

	public String getTranslatedName() {
		return translatedName;
	}

	public void setTranslatedName(String translatedName) {
		this.translatedName = translatedName;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public UpDown getUpDown() {
		return upDown;
	}

	public void setUpDown(UpDown upDown) {
		this.upDown = upDown;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
	
}
