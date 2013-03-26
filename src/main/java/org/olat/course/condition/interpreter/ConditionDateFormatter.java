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
package org.olat.course.condition.interpreter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.olat.core.util.StringHelper;

/**
 * 
 * simpleDateFormat are pretty heavy object, and take long to build.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ConditionDateFormatter {
	
	private static final SimpleDateFormat sdf;
	static {
		sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		sdf.setLenient(false);
	}
	
	public static Date parse(String source) {
		if(StringHelper.containsNonWhitespace(source)) {
			synchronized(sdf) {
				try {
					return sdf.parse(source);
				} catch (ParseException e) {
					return null;
				}
			}
		} else {
			return null;
		}
	}
	
	public static String format(Date source) {
		if(source == null) return null;
		synchronized(sdf) {
			try {
				return sdf.format(source);
			} catch (Exception e) {
				return null;
			}
		}
	}
}
