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
package org.olat.modules.quality.generator;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.OLATResourceable;
import org.olat.modules.quality.QualityGeneratorProviderReferenceable;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 06.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface QualityGenerator
		extends QualityGeneratorRef, OLATResourceable, QualityGeneratorProviderReferenceable, CreateInfo, ModifiedInfo {
	
	public String getTitle();
	
	public void setTitle(String title);

	public Boolean isEnabled();
	
	public void setEnabled(Boolean enabled);

	public Date getLastRun();

	public void setLastRun(Date lastRun);

	public RepositoryEntry getFormEntry();
	
	public void setFormEntry(RepositoryEntry formEntry);

}
