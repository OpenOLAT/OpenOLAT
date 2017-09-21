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
package org.olat.modules.video.ui;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;

/**
 * Initial date: 30.09.2016<br>
 * @author fkiefer, fabian.kiefer@frentix.com, http://www.frentix.com
 *
 */
public class TranscodingQueueTableRow {

	private FormLink resid;
	private String displayname;
	private FormLink creator;
	private Date creationDate;
	private String dimension;
	private String size;
	private String format;
	private FormLink deleteLink;
	private FormLink retranscodeLink;
	private Object[] failureReason;


	/**
	 * Instantiates a new transcoding queue table row.
	 *
	 * @param resid the resid
	 * @param resolution the resolution
	 * @param dimension the dimension
	 * @param size the size
	 * @param format the format
	 * @param deleteLink FormLink for delete row and corresponding Videodata or null if no deleteLink should be shown
	 */
	public TranscodingQueueTableRow(FormLink resid, String displayname, Date creationDate, FormLink resolution, String dimension, String size, String format, FormLink deleteLink) {
		this.resid = resid;
		this.displayname = displayname;
		this.creator = resolution;
		this.creator.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		this.creationDate = creationDate;
		this.dimension = dimension;
		this.size = size;
		this.format = format;
		if(deleteLink != null) this.deleteLink = deleteLink;
	}
	

	public Object[] getFailureReason() {
		return failureReason;
	}

	public void setFailureReason(Object[] failureReason) {
		this.failureReason = failureReason;
	}

	public Date getCreationDate(){
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate){
		this.creationDate = creationDate;
	}
	
	public String getDisplayname(){
		return displayname;
	}
	
	public void setDisplayname(String displayname){
		this.displayname = displayname;
	}
	
	public FormLink getResid(){
		return resid;
	}
	
	public void setResid(FormLink resid){
		this.resid = resid;
	}
	
	public FormLink getCreator() {
		return creator;
	}

	public void setType(FormLink type) {
		this.creator = type;
	}

	public String getDimension() {
		return dimension;
	}

	public void setDimension(String dimension) {
		this.dimension = dimension;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	
	public FormLink getDeleteLink() {
		return deleteLink;
	}
	
	public void setDeleteLink(FormLink deleteLink) {
		this.deleteLink = deleteLink;
	}

	public FormLink getRetranscodeLink() {
		return retranscodeLink;
	}

	public void setRetranscodeLink(FormLink retranscodeLink) {
		this.retranscodeLink = retranscodeLink;
	}	
}

