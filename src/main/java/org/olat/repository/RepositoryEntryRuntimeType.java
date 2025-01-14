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
package org.olat.repository;

import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 22 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum RepositoryEntryRuntimeType {
	standalone,
	embedded,
	curricular;

	public static ILoggingAction loggingAction(RepositoryEntryRuntimeType status) {
		switch(status) {
			case standalone: return LearningResourceLoggingAction.LEARNING_RESOURCE_RUNTIME_TYPE_STANDALONE;
			case embedded: return LearningResourceLoggingAction.LEARNING_RESOURCE_RUNTIME_TYPE_EMBEDDED;
			case curricular: return LearningResourceLoggingAction.LEARNING_RESOURCE_RUNTIME_TYPE_CURRICULAR;
			default: return null;
		}
	}

	public static RepositoryEntryRuntimeType secureValueOf(String value, RepositoryEntryRuntimeType defaultValue) {
		if (StringHelper.containsNonWhitespace(value)) {
			for (RepositoryEntryRuntimeType type : RepositoryEntryRuntimeType.values()) {
				if (type.name().equalsIgnoreCase(value)) {
					return type;
				}
			}
		}
		return defaultValue;
	}
}
