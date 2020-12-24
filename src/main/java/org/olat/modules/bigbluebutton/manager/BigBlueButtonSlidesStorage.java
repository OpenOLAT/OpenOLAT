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
package org.olat.modules.bigbluebutton.manager;

import java.io.File;

import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingImpl;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class BigBlueButtonSlidesStorage {
	
	private static final String DIRECTORY = "bigbluebutton";

	private VFSContainer rootContainer;
	
	public VFSContainer getRootContainer() {
		if(rootContainer == null) {
			rootContainer = VFSManager.olatRootContainer(File.separator + DIRECTORY, null);
		}
		return rootContainer;
	}
	
	public VFSContainer getStorage(BigBlueButtonMeeting meeting) {
		VFSItem dir = getRootContainer().resolve(meeting.getDirectory());
		return dir instanceof VFSContainer ? (VFSContainer)dir : null;
	}
	
	public VFSContainer createStorage(BigBlueButtonMeeting meeting) {
		StringBuilder sb = new StringBuilder();
		if(meeting.getBusinessGroup() != null) {
			VFSManager.getOrCreateContainer(getRootContainer(), sb.toString());
			sb.append("group").append(File.separator).append(meeting.getBusinessGroup().getKey());
		} else if(meeting.getEntry() != null) {
			sb.append("course").append(File.separator).append(meeting.getEntry().getKey()).append(File.separator);
			if(StringHelper.containsNonWhitespace(meeting.getSubIdent())) {
				sb.append(meeting.getSubIdent());
			} else {
				sb.append("tool");
			}	
		}
		
		sb.append(File.separator).append(meeting.getKey());

		VFSContainer cont = VFSManager.olatRootContainer(File.separator + DIRECTORY + File.separator + sb.toString());

		((BigBlueButtonMeetingImpl)meeting).setDirectory(sb.toString());
		return cont;
	}
}
