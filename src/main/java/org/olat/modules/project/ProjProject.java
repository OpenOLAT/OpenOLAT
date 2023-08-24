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
package org.olat.modules.project;

import java.util.Date;

import org.olat.basesecurity.Group;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.OLATResourceable;

/**
 * 
 * Initial date: 22 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface ProjProject extends ProjProjectRef, OLATResourceable, ModifiedInfo, CreateInfo {
	
	public static final String TYPE = "Project";
	
	public String getExternalRef();

	public void setExternalRef(String externalRef);
	
	public ProjectStatus getStatus();
	
	public void setStatus(ProjectStatus status);
	
	public String getTitle();
	
	public void setTitle(String title);
	
	public String getTeaser();

	public void setTeaser(String teaser);
	
	public String getDescription();
	
	public void setDescription(String description);
	
	public String getAvatarCssClass();
	
	public void setAvatarCssClass(String avatarCssClass);
	
	public boolean isTemplatePrivate();
	
	public void setTemplatePrivate(boolean templatePrivate);
	
	public boolean isTemplatePublic();
	
	public void setTemplatePublic(boolean templatePublic);
	
	public Date getDeletedDate();
	
	public void setDeletedDate(Date deletedDate);
	
	public Identity getDeletedBy();
	
	public void setDeletedBy(Identity deletedBy);
	
	public Identity getCreator();

	public Group getBaseGroup();
	
}
