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
package org.olat.course.nodes;

import java.util.List;

import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.course.archiver.ExportFormat;
import org.olat.group.BusinessGroup;

/**
 * The window control must be set if the export use the PDF service.
 * 
 * Initial date: 20.12.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 * @author fkiefer
 */
public class ArchiveOptions {
	
	private BusinessGroup group;
	private List<Identity> identities;
	private ExportFormat exportFormat;
	
	private Identity doer;
	private boolean withPdfs = false;
	private WindowControl windowControl;
	
	public ArchiveOptions() {
		//
	}
	
	public BusinessGroup getGroup() {
		return group;
	}
	
	public void setGroup(BusinessGroup group) {
		this.group = group;
	}
	
	public List<Identity> getIdentities() {
		return identities;
	}
	
	public void setIdentities(List<Identity> identities) {
		this.identities = identities;
	}

	public ExportFormat getExportFormat() {
		return exportFormat;
	}

	public void setExportFormat(ExportFormat exportFormat) {
		this.exportFormat = exportFormat;
	}

	/**
	 * @return The window control
	 */
	public WindowControl getWindowControl() {
		return windowControl;
	}

	/**
	 * The window control is used by export which published a PDF.
	 * 
	 * @param windowControl The window control
	 */
	public void setWindowControl(WindowControl windowControl) {
		this.windowControl = windowControl;
	}

	public boolean isWithPdfs() {
		return withPdfs;
	}

	public void setWithPdfs(boolean withPdfs) {
		this.withPdfs = withPdfs;
	}

	public Identity getDoer() {
		return doer;
	}

	public void setDoer(Identity doer) {
		this.doer = doer;
	}
}
