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
package org.olat.modules.forms;

import java.io.Serializable;
import java.util.UUID;

/**
 * 
 * Initial date: 30.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormParticipationIdentifier implements Serializable {
	
	private static final long serialVersionUID = -8390433609723788334L;
	
	private final String type;
	private final String key;
	
	public EvaluationFormParticipationIdentifier() {
		this.type = "default";
		this.key = UUID.randomUUID().toString().replace("-", "");
	}

	public EvaluationFormParticipationIdentifier(String type, String key) {
		this.type = type;
		this.key = key;
	}

	public String getType() {
		return type;
	}

	public String getKey() {
		return key;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EvaluationFormParticipationIdentifier [type=");
		builder.append(type);
		builder.append(", key=");
		builder.append(key);
		builder.append("]");
		return builder.toString();
	}
	
}
