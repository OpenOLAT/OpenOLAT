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
package org.olat.core.commons.services.vfs;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * A very simple configuration bean for versioning. There is a default value for
 * the maximum allowed number of revisions, this number can be overridden by an
 * second value saved in the persisted properties.
 * 
 * <P>
 * Initial Date: 21 sept. 2009 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
@Service
public class VFSVersionModule extends AbstractSpringModule implements ConfigOnOff {

	private static final String MAX_NUMBER_OF_VERSIONS = "maxnumber.versions";

	@Value("${maxnumber.versions:0}")
	private int maxNumberOfVersions;

	@Autowired
	public VFSVersionModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager, "org.olat.core.util.vfs.version.SimpleVersionConfig");
	}

	@Override
	public boolean isEnabled() {
		return maxNumberOfVersions > 0;
	}

	@Override
	public void init() {
		String maxNumberOfVersionsObj = getStringPropertyValue(MAX_NUMBER_OF_VERSIONS, true);
		if(StringHelper.containsNonWhitespace(maxNumberOfVersionsObj)) {
			maxNumberOfVersions = Integer.parseInt(maxNumberOfVersionsObj);
		}
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}

	/**
	 * @return maximum number of revisions, defined in admin. of Olat
	 */
	public int getMaxNumberOfVersions() {
		return maxNumberOfVersions;
	}

	public void setMaxNumberOfVersions(int maxNumber) {
		this.maxNumberOfVersions = maxNumber;
		setStringProperty(MAX_NUMBER_OF_VERSIONS, Integer.toString(maxNumber), true);
	}
}
