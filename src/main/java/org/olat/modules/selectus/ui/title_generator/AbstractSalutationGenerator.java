
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
package org.olat.modules.selectus.ui.title_generator;

import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationShort;
import org.olat.modules.selectus.model.PersonName;

/**
 * 
 * Initial date: 24.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractSalutationGenerator implements SalutationGenerator {
	
	@Override
	public String getTitleLastName(Identity identity, Locale locale) {
		return getTitleLastName(new IdentityNameWrapper(identity, locale), locale);
	}
	
	@Override
	public String getTitleLastName(Application app, Locale locale) {
		return getTitleLastName(app.getPerson(), locale);
	}

	@Override
	public String getTitleLastName(ApplicationShort app, Locale locale) {
		return getTitleLastName(app.getPerson(), locale);
	}
	
	@Override
	public String getTitleLastName(ApplicationShort app, List<? extends ApplicationShort> applicationList, Locale locale) {
		StringBuilder sb = new StringBuilder();
		if(app != null && app.getPerson() != null) {
			sb.append(getTitleLastName(app, locale));
		}
		if(applicationList != null && !applicationList.isEmpty()) {
			for(ApplicationShort application:applicationList) {
				if(application != null && application.getPerson() != null) {
					if(sb.length() > 0) sb.append(", ");
					sb.append(getTitleLastName(application, locale));
				}
			}
		}
		return sb.toString();
	}
	
	@Override
	public String getTitleFullname(Identity identity, Locale locale) {
		return getTitleFullname(new IdentityNameWrapper(identity, locale), locale);
	}
	
	@Override
	public String getTitleFullname(Application app, Locale locale) {
		return getTitleFullname(app.getPerson(), locale);
	}

	@Override
	public String getTitleFullname(Application app, List<Application> applicationList, Locale locale) {
		StringBuilder sb = new StringBuilder();
		if(app != null && app.getPerson() != null) {
			sb.append(getTitleFullname(app, locale));
		}
		if(applicationList != null && !applicationList.isEmpty()) {
			for(Application application:applicationList) {
				if(application != null && application.getPerson() != null) {
					if(sb.length() > 0) sb.append(", ");
					sb.append(getTitleFullname(application, locale));
				}
			}
		}
		return sb.toString();
	}

	@Override
	public String getTitleFullname(ApplicationShort app, Locale locale) {
		return getTitleFullname(app.getPerson(), locale);
	}

	@Override
	public String getTitleFirstLastName(Identity identity, Locale locale) {
		return getTitleFirstLastName(new IdentityNameWrapper(identity, locale), locale);
	}

	@Override
	public String getTitleFirstLastName(Application app, Locale locale) {
		return getTitleFirstLastName(app.getPerson(), locale);
	}

	@Override
	public String getTitleFirstLastName(ApplicationShort app, Locale locale) {
		return getTitleFirstLastName(app.getPerson(), locale);
	}
	
	@Override
	public String getTitleFirstLastName(ApplicationShort application, List<? extends ApplicationShort> applicationsList, Locale locale) {
		StringBuilder sb = new StringBuilder();
		if(application != null && application.getPerson() != null) {
			sb.append(getTitleFirstLastName(application, locale));
		}
		if(applicationsList != null && !applicationsList.isEmpty()) {
			for(ApplicationShort app:applicationsList) {
				if(app != null && app.getPerson() != null) {
					if(sb.length() > 0) sb.append(", ");
					sb.append(getTitleFirstLastName(app, locale));
				}
			}
		}
		return sb.toString();
	}

	@Override
	public String getFullname(PersonName person, Locale locale) {
		StringBuilder sb = new StringBuilder(64);
		if(StringHelper.containsNonWhitespace(person.getFirstName())) {
			sb.append(person.getFirstName());
		}
		
		String lastName = person.getLastName();
		if(StringHelper.containsNonWhitespace(lastName)) {
			if(sb.length() > 0) sb.append(' ');
			sb.append(lastName);
		}
		return sb.toString();
	}
	
	@Override
	public String getFullname(Identity identity, Locale locale) {
		return getFullname(new IdentityNameWrapper(identity, locale), locale);
	}

	@Override
	public String getFullname(Application app, Locale locale) {
		return getFullname(app.getPerson(), locale);
	}

	@Override
	public String getFullname(ApplicationShort app, Locale locale) {
		return getFullname(app.getPerson(), locale);
	}
	
	@Override
	public String getFullname(Application app, List<Application> applicationList, Locale locale) {
		StringBuilder sb = new StringBuilder();
		if(app != null && app.getPerson() != null) {
			sb.append(getFullname(app, locale));
		}
		if(applicationList != null && !applicationList.isEmpty()) {
			for(Application application:applicationList) {
				if(application != null && application.getPerson() != null) {
					if(sb.length() > 0) sb.append(", ");
					sb.append(getFullname(application, locale));
				}
			}
		}
		return sb.toString();
	}

	@Override
	public String getSalutation(Identity identity, Locale locale) {
		return getSalutation(new IdentityNameWrapper(identity, locale), locale);
	}
}
