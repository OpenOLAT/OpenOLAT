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
package org.olat.modules.selectus.ui.rejection;

import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.VelocityContext;

/**
 * 
 * Initial date: 30 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VariablesValidationContext extends VelocityContext {
	
	private static final long serialVersionUID = -4153392034993856837L;
	
	private final List<String> unkownVariables = new ArrayList<>();
	private final List<String> nullVariables = new ArrayList<>();
	
	public VariablesValidationContext() {
		//
	}
	
	@Override
	public Object internalGet(String key) {
		if(!key.startsWith(".literal.")) {
			if(!internalContainsKey(key)) {
				unkownVariables.add(key);
			} else if(super.internalGet(key) == null) {
				nullVariables.add(key);
			}
		}
		return super.internalGet(key);
	}
	
	public String stringuifiedUnkownVariables() {
		StringBuilder sb = new StringBuilder(128);
		for(String variable:unkownVariables) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(variable);
		}
		return sb.toString();
	}
	
	public List<String> getUnkownVariables() {
		return unkownVariables;
	}
	
	public List<String> getNullVariables() {
		return nullVariables;
	}

}
