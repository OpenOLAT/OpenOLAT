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
package org.olat.modules.bigbluebutton.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonErrors implements Serializable {

	private static final long serialVersionUID = -7927445328496097183L;
	private final List<BigBlueButtonError> errors = new ArrayList<>(2);
	
	public List<BigBlueButtonError> getErrors() {
		return errors;
	}
	
	public void append(BigBlueButtonErrors error) {
		if(error.hasErrors()) {
			this.errors.addAll(error.getErrors());
		}
	}
	
	public void append(BigBlueButtonError error) {
		errors.add(error);
	}
	
	public boolean hasErrors() {
		return !errors.isEmpty();
	}
	
	public String getErrorMessages() {
		StringBuilder sb = new StringBuilder(256);
		for(BigBlueButtonError error:errors) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(error.getCode() == null ? "UNKOWN" : error.getCode().name());
		}
		return sb.toString();
	}
}
