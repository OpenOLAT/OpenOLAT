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
package org.olat.repository;

import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 11.07.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum RepositoryEntryManagedFlag {
	
	all,
	  editcontent(all),
    details(all),//details tab
      title(details,all),
      description(details,all),
      teaser(details,all),
      objectives(details,all),
      requirements(details,all),
      credits(details,all),
      location(details,all),
      organisations(details,all),
      educationalType(details,all),
    settings(all),//max num of participants...
      access(settings,all),
      search(settings, all),
      participantList(settings, all),
      participantInfo(settings, all),
      email(settings, all),
      teams(settings, all),
      bigbluebutton(settings, all),
	  zoom(settings, all),
      blog(settings, all),
      wiki(settings, all),
      forum(settings, all),
      documents(settings, all),
      chat(settings,all),
      layout(settings,all),
      resourcefolder(settings,all),
      efficencystatement(settings,all),
      calendar(settings,all),
      glossary(settings,all),
    lecture(all),
      lectureconfig(lecture, all),
      lecturemanagement(lecture, all),
    bookings(all),// change booking rules
    membersmanagement(all),
    groups(all),
    close(all),
    copy(all),
    delete(all);
	

	private RepositoryEntryManagedFlag[] parents;
	private static final Logger log = Tracing.createLoggerFor(RepositoryEntryManagedFlag.class);
	public static final RepositoryEntryManagedFlag[] EMPTY_ARRAY = new RepositoryEntryManagedFlag[0];
	
	private static RepositoryModule repositoryModule;
	
	private RepositoryEntryManagedFlag() {
		//
	}
	
	private RepositoryEntryManagedFlag(RepositoryEntryManagedFlag... parents) {
		if(parents == null) {
			this.parents = new RepositoryEntryManagedFlag[0];
		} else {
			this.parents = parents;
		}
	}
	
	public static RepositoryEntryManagedFlag[] toEnum(String flags) {
		if(StringHelper.containsNonWhitespace(flags)) {
			String[] flagArr = flags.split(",");
			RepositoryEntryManagedFlag[] flagEnums = new RepositoryEntryManagedFlag[flagArr.length];
	
			int count = 0;
			for(String flag:flagArr) {
				if(StringHelper.containsNonWhitespace(flag)) {
					try {
						RepositoryEntryManagedFlag flagEnum = valueOf(flag);
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
	
	public static boolean isManaged(RepositoryEntry re, RepositoryEntryManagedFlag marker) {
		if(repositoryModule == null) {
			repositoryModule = CoreSpringFactory.getImpl(RepositoryModule.class);
		}
		if(!repositoryModule.isManagedRepositoryEntries()) {
			return false;
		}
		
		if(re != null && (contains(re, marker) || contains(re, marker.parents))) {
			return true;
		}
		return false;
	}
	
	public static boolean isManaged(RepositoryEntryManagedFlag[] flags, RepositoryEntryManagedFlag marker) {
		if(repositoryModule == null) {
			repositoryModule = CoreSpringFactory.getImpl(RepositoryModule.class);
		}
		if(!repositoryModule.isManagedRepositoryEntries()) {
			return false;
		}
		
		if(flags != null && (contains(flags, marker) || contains(flags, marker.parents))) {
			return true;
		}
		return false;
	}
	
	private static boolean contains(RepositoryEntry re, RepositoryEntryManagedFlag... markers) {
		if(re == null) return false;
		RepositoryEntryManagedFlag[] flags = re.getManagedFlags();
		return contains(flags, markers);
	}

	private static boolean contains(RepositoryEntryManagedFlag[] flags, RepositoryEntryManagedFlag... markers) {
		if(flags == null || flags.length == 0) return false;

		for(RepositoryEntryManagedFlag flag:flags) {
			for(RepositoryEntryManagedFlag marker:markers) {
				if(flag.equals(marker)) {
					return true;
				}
			}
		}
		return false;
	}
}