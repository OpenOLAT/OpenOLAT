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
package org.olat.course.nodes.pf.ui;

import java.util.Date;

import org.olat.user.UserPropertiesRow;

/**
 * The Class TranscodingRow.
 * Initial date: 07.12.2016<br>
 * @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
 */
public class DropBoxRow {
	
	private String status;
	private int filecount, filecountReturn, newfiles;
	private Date lastupdate, lastupdateReturn;
	private final UserPropertiesRow identity;
		
	public DropBoxRow(UserPropertiesRow identity, String status, int foldercount, int filecountReturn,
			int newfiles, Date lastupdate, Date lastupdateReturn) {
		super();
		this.identity = identity;
		this.status = status;
		this.filecount = foldercount;
		this.filecountReturn = filecountReturn;
		this.newfiles = newfiles;
		this.lastupdate = lastupdate;
		this.lastupdateReturn = lastupdateReturn;
	}
	

	public UserPropertiesRow getIdentity () {
		return identity;
	}
	public String getStatus() {
		return status;
	}
	public int getFilecount() {
		return filecount;
	}
	public int getFilecountReturn () {
		return filecountReturn;
	}	
	public int getNewfolders() {
		return newfiles;
	}
	public Date getLastupdate() {
		return lastupdate;
	}
	public Date getLastupdateReturn() {
		return lastupdateReturn;
	}


	

	
	
}
