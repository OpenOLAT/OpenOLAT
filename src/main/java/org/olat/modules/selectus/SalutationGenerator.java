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
package org.olat.modules.selectus;

import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationShort;
import org.olat.modules.selectus.model.PersonName;

/**
 * 
 * Initial date: 17.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface SalutationGenerator {

	public String getTitleLastName(Identity identity, Locale locale);
	
	public String getTitleLastName(PersonName person, Locale locale);
	
	public String getTitleLastName(Application app, Locale locale);
	
	public String getTitleLastName(ApplicationShort app, Locale locale);
	
	public String getTitleLastName(ApplicationShort app, List<? extends ApplicationShort> applicationList, Locale locale);
	

	public String getTitleFirstLastName(Identity identity, Locale locale);
	
	public String getTitleFirstLastName(PersonName person, Locale locale);
	
	public String getTitleFirstLastName(Application app, Locale locale);
	
	public String getTitleFirstLastName(ApplicationShort app, Locale locale);
	
	public String getTitleFirstLastName(ApplicationShort app, List<? extends ApplicationShort> applicationList, Locale locale);


	public String getTitleFullname(Identity identity, Locale locale);
	
	public String getTitleFullname(PersonName person, Locale locale);
	
	public String getTitleFullname(Application app, Locale locale);
	
	public String getTitleFullname(Application app, List<Application> applicationList, Locale locale);
	
	public String getTitleFullname(ApplicationShort app, Locale locale);
	
	
	public String getSalutation(Identity identity, Locale locale);
	
	public String getSalutation(PersonName person, Locale locale);
	
	public String getSalutation(Application app, Locale locale);

	public String getSalutation(ApplicationShort app, Locale locale);


	public String getFullname(Identity identity, Locale locale);
	
	public String getFullname(PersonName person, Locale locale);

	public String getFullname(Application app, Locale locale);
	
	public String getFullname(ApplicationShort app, Locale locale);
	
	public String getFullname(Application app, List<Application> applicationList, Locale locale);

}
