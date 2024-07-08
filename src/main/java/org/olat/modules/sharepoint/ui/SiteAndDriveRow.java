/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.sharepoint.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.util.StringHelper;
import org.olat.modules.sharepoint.model.MicrosoftDrive;
import org.olat.modules.sharepoint.model.MicrosoftSite;
import org.olat.modules.sharepoint.model.SiteAndDriveConfiguration;

/**
 * 
 * Initial date: 8 juil. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SiteAndDriveRow implements FlexiTreeTableNode {
	
	private final MicrosoftSite site;
	private final MicrosoftDrive drive;
	private SiteAndDriveRow parent;
	
	public SiteAndDriveRow(MicrosoftSite site) {
		this.site = site;
		drive = null;
	}
	
	public SiteAndDriveRow(MicrosoftSite site, MicrosoftDrive drive) {
		this.site = site;
		this.drive = drive;
	}
	
	public SiteAndDriveConfiguration toConfiguration() {
		if(drive != null) {
			return new SiteAndDriveConfiguration(site.name(), site.displayName(), site.id(), drive.name(), drive.id());
		}
		return new SiteAndDriveConfiguration(site.name(), site.displayName(), site.id());
	}

	public MicrosoftSite getSite() {
		return site;
	}

	public MicrosoftDrive getDrive() {
		return drive;
	}

	@Override
	public SiteAndDriveRow getParent() {
		return parent;
	}
	
	public void setParent(SiteAndDriveRow parent) {
		this.parent = parent;
	}

	@Override
	public String getCrump() {
		return getName();
	}
	
	public String getId() {
		if(drive != null) {
			return drive.id();
		}
		return site.id();
	}
	
	public String getName() {
		if(drive != null) {
			return drive.name();
		}
		String name = site.displayName();
		if(StringHelper.containsNonWhitespace(name)) {
			name += " (" + site.name() + ")";
		} else {
			name = site.name();
		}
		return name;
	}
}
