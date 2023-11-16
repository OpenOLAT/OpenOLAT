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
package org.olat.modules.todo;

import org.olat.modules.todo.model.ToDoContextImpl;

/**
 * 
 * Initial date: 21 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface ToDoContext {
	
	public String getType();

	public Long getOriginId();

	public String getOriginSubPath();
	
	public String getOriginTitle();
	
	public String getOriginSubTitle();
	
	public static ToDoContext of(String type) {
		return of(type, null, null);
	}
	
	public static ToDoContext of(String type, Long originId, String originTitle) {
		return of(type, originId, null, originTitle, null);
	}
	
	public static ToDoContext of(String type, Long originId, String originSubPath, String originTitle, String originSubTitle) {
		return new ToDoContextImpl(type, originId, originSubPath, originTitle, originSubTitle);
	}

}
