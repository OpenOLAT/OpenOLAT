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
package org.olat.core.commons.services.license.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.olat.core.commons.services.license.LicenseHandler;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 28.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
class LicensorFactory {
	
	private static final String DEFAULT_TYPE = LicensorNoneCreator.NONE_CREATOR_TYPE;

	@Autowired
	private LicenseModule licenseModule;
	@Autowired
    private List<LicensorCreator> loadedCreators;

    private static final Map<String, LicensorCreator> creators = new HashMap<>();

    @PostConstruct
    void init() {
        for(LicensorCreator creator: loadedCreators) {
        		creators.put(creator.getType(), creator);
        }
    }
    
    private LicensorCreator getCreator(LicenseHandler handler) {
    		String type = licenseModule.getLicensorCreatorType(handler);
    		LicensorCreator creator = creators.get(type);
    		if (creator == null) {
    			creator = creators.get(DEFAULT_TYPE);
    		}
    		return creator;
    }
    
	/**
	 * Creates the licensor in dependency of the configuration in the license module.
	 *
	 * @param handler
	 * @param identity the licensor identity
	 * @return
	 */
	String create(LicenseHandler handler, Identity identity) {
		return getCreator(handler).create(handler, identity);
	}
}
