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
package org.olat.modules.forms.rules;

import java.util.ArrayList;
import java.util.List;

import org.olat.modules.forms.model.xml.Rule;

/**
 * 
 * Initial date: 6 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RulesEngine {
	
	private final List<Rule> rules;
	private List<RuleFulfiledListener> listeners = new ArrayList<>();
	
	public RulesEngine(List<Rule> rules) {
		this.rules = rules;
	}

	public List<Rule> getRules() {
		return rules;
	}
	
	public void registerListener(RuleFulfiledListener listener) {
		listeners.add(listener);
	}

	public void fulfilledChanged(Rule rule, boolean fulfilled) {
		listeners.forEach(listener -> listener.onFulfilledChanged(rule, fulfilled));
	}
	
	
	public interface RuleFulfiledListener {
		
		public void onFulfilledChanged(Rule rule, boolean fulfilled);
	}

}
