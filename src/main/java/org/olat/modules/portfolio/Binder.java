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

import org.olat.core.id.OLATResourceable;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 07.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface Binder extends BinderLight, PortfolioElement, OLATResourceable {
	
	public Date getLastModified();
	
	public Date getCopyDate();
	
	public Date getReturnDate();
	
	public Date getDeadLine();
	
	public void setDeadLine(Date deadLine);
	
	public void setTitle(String title);
	
	public String getSummary();
	
	public void setSummary(String summary);
	
	public BinderStatus getBinderStatus();
	
	public void setBinderStatus(BinderStatus status);
	
	@Override
	public String getImagePath();
	
	public void setImagePath(String imagePath);

	/**
	 * The repository entry which is used as base for the assessment. It can
	 * be the course or perhaps the template if the binder is not
	 * linked to a course.
	 * 
	 * @return
	 */
	public RepositoryEntry getEntry();
	
	/**
	 * This is the shared olat resource between a template and its
	 * repository entry. Standard binder without an entry in the learn
	 * resources doesn't have one.
	 * 
	 * @return
	 */
	public OLATResource getOlatResource();
	
	public String getSubIdent();
	
	public Binder getTemplate();

}
