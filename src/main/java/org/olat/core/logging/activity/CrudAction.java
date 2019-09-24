/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.core.logging.activity;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * enum representing the four CRUD types which are
 * stored with each log entry:
 * <ul>
 *  <li>create: when a new resource is created</li>
 *  <li>retrieve/read: when a resource is read</li>
 *  <li>update: when a resource is changed/updated</li>
 *  <li>delete: when a resource is deleted</li>
 * </ul>
 * Note that it's not always easy to map log messages
 * to one of the four crud actions - if you find it hard
 * it is always an idea to fall back to the default 'retrieve'
 * <p>
 * NOTE: Make sure that the initial of any two
 * CrudActions are different! The initial is what is
 * stored to the DB!
 * <p>
 * Initial Date:  20.10.2009 <br>
 * @author Stefan
 */
public enum CrudAction {
	
	/** a resource is created **/
	create, 
	
	/** a resource is retrieved/read - this includes open/close **/
	retrieve, 
	
	/** a resource is updated **/
	update,
	
	/** a resource is deleted **/
	delete,
	
	/** an exit from viewing/editing/etc a resource **/
	exit;
	
	static {
		Field[] fields = CrudAction.class.getDeclaredFields();
		if (fields!=null) {
			Set<String> initials = new HashSet<>();
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (field.getType()==CrudAction.class) {
					String initial = field.getName().substring(0, 1);
					if (initials.contains(initial)) {
						throw new IllegalStateException("CrudAction contained at least two actions with the same initial '"+initial+"'. One of which is "+field.getName());
					}
					initials.add(initial);
				}
			}
		}
		
	}
}