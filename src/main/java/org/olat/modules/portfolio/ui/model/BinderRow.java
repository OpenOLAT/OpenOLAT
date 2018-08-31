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
package org.olat.modules.portfolio.ui.model;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.BinderStatus;
import org.olat.modules.portfolio.model.BinderStatistics;

/**
 * 
 * Initial date: 19.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderRow implements BinderRef {
	
	private final BinderStatistics binderStats;
	private VFSLeaf image;
	private final FormLink openLink;
	private final FormLink toolsLink;
	private final boolean newBinder;
	
	public BinderRow() {
		binderStats = null;
		image = null;
		openLink = null;
		newBinder = true;
		toolsLink = null;
	}
	
	public BinderRow(BinderStatistics binderStats, VFSLeaf image, FormLink openLink, FormLink toolsLink) {
		this.binderStats = binderStats;
		this.image = image;
		this.openLink = openLink;
		this.toolsLink = toolsLink;
		newBinder = false;
	}
	
	public boolean isNewBinder() {
		return newBinder;
	}
	
	public boolean isDeleted() {
		if(binderStats == null) return false;
		return  binderStats.getStatus() == null ? false : BinderStatus.deleted.name().equals(binderStats.getStatus());
	}
	
	public BinderStatistics getStatistics() {
		return binderStats;
	}

	@Override
	public Long getKey() {
		return binderStats == null ? null : binderStats.getKey();
	}

	public String getTitle() {
		return binderStats == null ? null : binderStats.getTitle();
	}
	
	public String getReferenceEntryName() {
		return binderStats == null ? null : binderStats.getEntryDisplayname();
	}
	
	public Date getLastUpdate() {
		return binderStats == null ? null : binderStats.getLastModified();
	}
	
	public String[] getNumOfSectionsAndPages() {
		return new String[]{
				Integer.toString(binderStats.getNumOfSections()),
				Integer.toString(binderStats.getNumOfPages())
			};
	}
	
	public String[] getNumOfComments() {
		return new String[]{
				Integer.toString(binderStats.getNumOfComments())
			};
	}

	public FormLink getOpenLink() {
		return openLink;
	}
	
	public FormLink getToolsLink() {
		return toolsLink;
	}
	
	public boolean isBackground() {
		return image != null;
	}
	
	public VFSLeaf getBackgroundImage() {
		return image;
	}
	
	public void setBackgroundImage(VFSLeaf image) {
		this.image = image;
	}
	
	public String getImageName() {
		return image == null ? null : image.getName();
	}

	@Override
	public int hashCode() {
		Long key = getKey();
		return key == null ? 36578 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof BinderRow) {
			BinderRow row = (BinderRow)obj;
			return (newBinder && row.newBinder) || (getKey() != null && getKey().equals(row.getKey()));		
		}
		return false;
	}
	
	
}