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
package org.olat.core.commons.services.doceditor.office365.manager;

import java.util.Date;

/**
 * 
 * Initial date: 2 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DateHelper {
	
	// Source https://stackoverflow.com/a/44028583

	private static final long TICKS_AT_EPOCH = 621355968000000000L;
	private static final long TICKS_PER_MILLISECOND = 10000;

	public static Date ticksToDate(long utcTicks){
		return new Date((utcTicks - TICKS_AT_EPOCH) / TICKS_PER_MILLISECOND);
	}
	
}
