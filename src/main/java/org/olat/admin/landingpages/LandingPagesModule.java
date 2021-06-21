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
package org.olat.admin.landingpages;

import java.util.ArrayList;

import org.olat.admin.landingpages.model.Rule;
import org.olat.admin.landingpages.model.Rules;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.xml.XStreamHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * Initial date: 15.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("landingPagesModule")
public class LandingPagesModule extends AbstractSpringModule {
	private static final String CONFIG_RULES = "rules";
	private static final XStream rulesXStream;
	static {
		rulesXStream = XStreamHelper.createXStreamInstance();
		XStreamHelper.allowDefaultPackage(rulesXStream);
		rulesXStream.alias("rules", Rules.class);
		rulesXStream.alias("rule", Rule.class);
	}
	
	private Rules rules;
	
	@Autowired
	public LandingPagesModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		String rulesObj = getStringPropertyValue(CONFIG_RULES, true);
		if(StringHelper.containsNonWhitespace(rulesObj)) {
			rules = (Rules)rulesXStream.fromXML(rulesObj);
		} else {
			rules = new Rules();
			rules.setRules(new ArrayList<>(1));
		}
	}
	
	@Override
	protected void initDefaultProperties() {
		//
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	public Rules getRules() {
		return rules;
	}

	public void setRules(Rules rules) {
		String value = rulesXStream.toXML(rules);
		setStringProperty(CONFIG_RULES, value, true);
	}
}
