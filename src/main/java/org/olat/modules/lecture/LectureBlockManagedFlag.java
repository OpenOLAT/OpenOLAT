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
package org.olat.modules.lecture;

import java.util.Arrays;

import org.olat.core.CoreSpringFactory;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 8 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum LectureBlockManagedFlag {
	
	all,
      details(all),//details tab
        title(details,all),
        compulsory(details,all),
        plannedLectures(details,all),
        teachers(details,all),
        groups(details,all),
        description(details,all),
        preparation(details,all),
        location(details,all),
        dates(details,all),
      settings(all),
      delete(all);
	

	private LectureBlockManagedFlag[] parents;
	private static final Logger log = Tracing.createLoggerFor(LectureBlockManagedFlag.class);
	public static final LectureBlockManagedFlag[] EMPTY_ARRAY = new LectureBlockManagedFlag[0];

	private static LectureModule lectureModule;
	
	private LectureBlockManagedFlag() {
		//
	}
	
	private LectureBlockManagedFlag(LectureBlockManagedFlag... parents) {
		if(parents == null) {
			this.parents = new LectureBlockManagedFlag[0];
		} else {
			this.parents = parents;
		}
	}
	
	public static LectureBlockManagedFlag[] toEnum(String flags) {
		if(StringHelper.containsNonWhitespace(flags)) {
			String[] flagArr = flags.split(",");
			LectureBlockManagedFlag[] flagEnums = new LectureBlockManagedFlag[flagArr.length];
	
			int count = 0;
			for(String flag:flagArr) {
				if(StringHelper.containsNonWhitespace(flag)) {
					try {
						LectureBlockManagedFlag flagEnum = valueOf(flag);
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
		} else {
			return EMPTY_ARRAY;
		}
	}
	
	public static boolean isManaged(LectureBlock block, LectureBlockManagedFlag marker) {
		if(lectureModule == null) {
			lectureModule = CoreSpringFactory.getImpl(LectureModule.class);
		}
		if(!lectureModule.isLecturesManaged()) {
			return false;
		}
		
		if(block != null && (contains(block, marker) || contains(block, marker.parents))) {
			return true;
		}
		return false;
	}
	
	public static boolean isManaged(LectureBlockManagedFlag[] flags, LectureBlockManagedFlag marker) {
		if(lectureModule == null) {
			lectureModule = CoreSpringFactory.getImpl(LectureModule.class);
		}
		if(!lectureModule.isLecturesManaged()) {
			return false;
		}
		
		if(flags != null && (contains(flags, marker) || contains(flags, marker.parents))) {
			return true;
		}
		return false;
	}
	
	private static boolean contains(LectureBlock block, LectureBlockManagedFlag... markers) {
		if(block == null) return false;
		LectureBlockManagedFlag[] flags = block.getManagedFlags();
		return contains(flags, markers);
	}

	private static boolean contains(LectureBlockManagedFlag[] flags, LectureBlockManagedFlag... markers) {
		if(flags == null || flags.length == 0) return false;

		for(LectureBlockManagedFlag flag:flags) {
			for(LectureBlockManagedFlag marker:markers) {
				if(flag.equals(marker)) {
					return true;
				}
			}
		}
		return false;
	}
}