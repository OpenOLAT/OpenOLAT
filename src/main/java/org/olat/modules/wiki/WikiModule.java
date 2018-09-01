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
package org.olat.modules.wiki;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * For historical reason, the wiki.enable property is saved
 * in the base security module.
 * 
 * Initial date: 31 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class WikiModule extends AbstractSpringModule {

	private static final String WIKI_XSS_SCAN_ENABLED = "wiki.xss.scan";
	
	@Value("${wiki.xss.scan:enabled}")
	private String xssScanEnabled;
	
	@Autowired
	private BaseSecurityModule baseSecurityModule;
	
	@Autowired
	public WikiModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}

	private void updateProperties() {
		String enabled = getStringPropertyValue(WIKI_XSS_SCAN_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			xssScanEnabled = enabled;
		}
	}

	public boolean isWikiEnabled() {
		return baseSecurityModule.isWikiEnabled();
	}

	public void setWikiEnabled(boolean enable) {
		baseSecurityModule.setWikiEnabled(enable);
	}
	
	public boolean isXSScanEnabled() {
		return "enabled".equals(xssScanEnabled);
	}

	public void setXSSScanEnabled(boolean enable) {
		String enabled = enable ? "enabled" : "disabled";
		xssScanEnabled = enabled;
		setStringProperty(WIKI_XSS_SCAN_ENABLED, enabled, true);
	}
}
