/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.todo;

import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 6 Oct 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public enum ToDoRight {
	
	all,
	view(all),
	edit(all),
	title(all, edit),
	description(all, edit),
	status(all, edit),
	priority(all, edit),
	expenditureOfWork(all, edit),
	startDate(all, edit),
	dueDate(all, edit),
	assignees(all, edit),
	delegates(all, edit),
	tags(all, edit),
	delete(all);
	
	private static final Logger log = Tracing.createLoggerFor(ToDoRight.class);
	public static final ToDoRight[] EMPTY_ARRAY = new ToDoRight[0];
	public static final ToDoRight[] EDIT_CHILDREN = children(edit);
	
	private ToDoRight[] parents;
	
	private ToDoRight() {
		//
	}
	
	private ToDoRight(ToDoRight... parents) {
		if(parents == null) {
			this.parents = new ToDoRight[0];
		} else {
			this.parents = parents;
		}
	}
	
	public static String toString(ToDoRight[] rights) {
		if (rights == null || rights.length == 0) return null;
		
		StringBuilder sb = new StringBuilder();
		for (ToDoRight flag : rights ) {
			if (sb.length() > 0) sb.append(",");
			sb.append(flag.name());
		}
		return sb.toString();
	}
	
	public static ToDoRight[] toEnum(String rights) {
		if (StringHelper.containsNonWhitespace(rights)) {
			String[] rightsArr = rights.split("[\\\\,]");
			ToDoRight[] rightEnums = new ToDoRight[rightsArr.length];
			
			int count = 0;
			for (String right : rightsArr) {
				if (StringHelper.containsNonWhitespace(right)) {
					try {
						ToDoRight rightEnum = valueOf(right);
						rightEnums[count++] = rightEnum;
					} catch (Exception e) {
						log.warn("Cannot parse this to-do right: {}", right, e);
					}
				}
			}
			
			if (count != rightEnums.length) {
				rightEnums = Arrays.copyOf(rightEnums, count);
			}
			return rightEnums;
		}
		return EMPTY_ARRAY;
	}
	
	public static boolean contains(ToDoRight[] rights, ToDoRight right) {
		if (rights != null && (containsAny(rights, right) || containsAny(rights, right.parents))) {
			return true;
		}
		return false;
	}
	
	public static boolean contains(ToDoRight[] rights, ToDoRight[] right) {
		if (rights != null) {
			for (ToDoRight toDoRight : right) {
				if (contains(rights, toDoRight)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean containsAny(ToDoRight[] rights, ToDoRight... markers) {
		if (rights == null || rights.length == 0 || markers == null || markers.length == 0) return false;

		for (ToDoRight right : rights) {
			for (ToDoRight marker : markers) {
				if(right.equals(marker)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static ToDoRight[] children(ToDoRight right) {
		return Arrays.stream(ToDoRight.values())
				.filter(value -> value == right || value.parents != null && Arrays.asList(value.parents).contains(right))
				.toArray(ToDoRight[]::new);
	}

}
