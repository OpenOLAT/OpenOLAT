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
package org.olat.modules.appointments;

/**
 * 
 * Initial date: 19 Nov 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface TopicLight {
	
	public enum Type { enrollment, finding }

	String getTitle();

	void setTitle(String title);

	String getDescription();

	void setDescription(String description);

	Type getType();

	void setType(Type type);

	boolean isMultiParticipation();

	void setMultiParticipation(boolean multiParticipation);

	boolean isAutoConfirmation();

	void setAutoConfirmation(boolean autoConfirmation);

	boolean isParticipationVisible();

	void setParticipationVisible(boolean participtionVisible);

}