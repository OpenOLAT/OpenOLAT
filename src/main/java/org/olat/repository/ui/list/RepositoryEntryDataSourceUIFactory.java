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
package org.olat.repository.ui.list;

import org.olat.core.gui.translator.Translator;


/**
 * 
 * Delegate to the controller the job of forgeing the links and the mapperUrl.
 * 
 * Initial date: 29.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface RepositoryEntryDataSourceUIFactory {
	
	public String getMapperThumbnailUrl();
	
	public void forgeMarkLink(RepositoryEntryRow row);
	
	public void forgeCompletion(RepositoryEntryRow row);
	
	public void forgeSelectLink(RepositoryEntryRow row);
	
	public void forgeStartLink(RepositoryEntryRow row);
	
	public void forgeDetails(RepositoryEntryRow row);
	
	public void forgeRatings(RepositoryEntryRow row);
	
	public void forgeComments(RepositoryEntryRow row);
	
	public Translator getTranslator();

}
