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
package org.olat.modules.portfolio;

import java.util.Date;

/**
 * For XStream
 * 
 * Initial date: 21.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface Citation {
	
	public CitationSourceType getItemType();

	public String getEdition();

	public void setEdition(String edition);
	
	public String getEditor();
	
	public void setEditor(String editor);
	
	public String getIsbn();
	
	public void setIsbn(String isbn);

	public String getVolume();

	public void setVolume(String volume);

	public String getSeries();

	public void setSeries(String series);

	public String getPublicationTitle();

	public void setPublicationTitle(String publicationTitle);

	public String getIssue();

	public void setIssue(String issue);

	public String getPages();

	public void setPages(String pages);

	public String getInstitution();

	public void setInstitution(String institution);
	
	public Date getLastVisit();
	
	public void setLastVisit(Date date);

}
