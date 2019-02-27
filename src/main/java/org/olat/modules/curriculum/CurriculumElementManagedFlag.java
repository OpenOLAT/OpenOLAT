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
package org.olat.modules.curriculum;

import java.util.Arrays;

import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 16 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum CurriculumElementManagedFlag {
	
	all,
	 identifier(all),
	 displayName(all),
	 description(all),
	 externalId(all),
	 status(all),
	 dates(all),
	 type(all),
	 calendars(all),
	 lectures(all),
	 members(all),
	 resources(all),
	 move(all),
	 addChildren(all),
	 delete(all);
	
	private CurriculumElementManagedFlag[] parents;
	private static final OLog log = Tracing.createLoggerFor(CurriculumElementManagedFlag.class);
	public static final CurriculumElementManagedFlag[] EMPTY_ARRAY = new CurriculumElementManagedFlag[0];
	
	private static CurriculumModule curriculumModule;

	private CurriculumElementManagedFlag() {
		//
	}
	
	private CurriculumElementManagedFlag(CurriculumElementManagedFlag... parents) {
		if(parents == null) {
			this.parents = new CurriculumElementManagedFlag[0];
		} else {
			this.parents = parents;
		}
	}
	
	public static CurriculumElementManagedFlag[] toEnum(String flags) {
		if(StringHelper.containsNonWhitespace(flags)) {
			String[] flagArr = flags.split(",");
			CurriculumElementManagedFlag[] flagEnums = new CurriculumElementManagedFlag[flagArr.length];
	
			int count = 0;
			for(String flag:flagArr) {
				if(StringHelper.containsNonWhitespace(flag)) {
					try {
						CurriculumElementManagedFlag flagEnum = valueOf(flag);
						flagEnums[count++] = flagEnum;
					} catch (Exception e) {
						log.warn("Cannot parse this managed flag: " + flag, e);
					}
				}
			}
			
			if(count != flagEnums.length) {
				flagEnums = Arrays.copyOf(flagEnums, count);
			}
			return flagEnums;
		}
		return EMPTY_ARRAY;
	}
	
	public static String toString(CurriculumElementManagedFlag... flags) {
		StringBuilder sb = new StringBuilder();
		if(flags != null && flags.length > 0 && flags[0] != null) {
			for(CurriculumElementManagedFlag flag:flags) {
				if(flag != null) {
					if(sb.length() > 0) sb.append(",");
					sb.append(flag.name());
				}
			}
		}
		return sb.length() == 0 ? null : sb.toString();
	}
	
	public static boolean isManaged(CurriculumElement element, CurriculumElementManagedFlag marker) {
		if(curriculumModule == null) {
			curriculumModule = CoreSpringFactory.getImpl(CurriculumModule.class);
		}
		if(!curriculumModule.isCurriculumManaged()) {
			return false;
		}
		return (element != null && (contains(element, marker) || contains(element, marker.parents)));
	}
	
	public static boolean isManaged(CurriculumElementManagedFlag[] flags, CurriculumElementManagedFlag marker) {
		if(curriculumModule == null) {
			curriculumModule = CoreSpringFactory.getImpl(CurriculumModule.class);
		}
		if(!curriculumModule.isCurriculumManaged()) {
			return false;
		}
		
		return (flags != null && (contains(flags, marker) || contains(flags, marker.parents)));
	}
	
	private static boolean contains(CurriculumElement element, CurriculumElementManagedFlag... markers) {
		if(element == null) return false;
		CurriculumElementManagedFlag[] flags = element.getManagedFlags();
		return contains(flags, markers);
	}

	private static boolean contains(CurriculumElementManagedFlag[] flags, CurriculumElementManagedFlag... markers) {
		if(flags == null || flags.length == 0) return false;

		for(CurriculumElementManagedFlag flag:flags) {
			for(CurriculumElementManagedFlag marker:markers) {
				if(flag.equals(marker)) {
					return true;
				}
			}
		}
		return false;
	}
}