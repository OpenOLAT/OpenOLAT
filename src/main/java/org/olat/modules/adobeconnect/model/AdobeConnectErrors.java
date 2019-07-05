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
package org.olat.modules.adobeconnect.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Initial date: 5 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectErrors implements Serializable {

	private static final long serialVersionUID = -6676284976532851249L;
	private final List<AdobeConnectError> errors = new ArrayList<>(2);
	
	public List<AdobeConnectError> getErrors() {
		return errors;
	}
	
	public void append(AdobeConnectErrors error) {
		if(error.hasErrors()) {
			this.errors.addAll(error.getErrors());
		}
	}
	
	public void append(AdobeConnectError error) {
		errors.add(error);
	}
	
	public boolean hasErrors() {
		return !errors.isEmpty();
	}
	
	public String getErrorMessages() {
		StringBuilder sb = new StringBuilder(256);
		for(AdobeConnectError error:errors) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(error.getCode() == null ? "UNKOWN" : error.getCode().name());
		}
		return sb.toString();
	}
}
