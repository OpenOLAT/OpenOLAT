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

package org.olat.modules.fo;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.modules.fo.archiver.MessageNode;

/**
 * 
 * Description:<br>
 * Provides utilities methods and classes.
 *  
 * <P>
 * Initial Date:  14.08.2007 <br>
 * @author Lavinia Dumitrescu
 */
public class ForumHelper {
	public static final String CSS_ICON_CLASS_FORUM = "o_fo_icon";
	public static final String CSS_ICON_CLASS_MESSAGE = "o_forum_message_icon";
	
	public static int NOT_MY_JOB = 0;
	
	public static final VFSContainer getArchiveContainer(UserRequest ureq, Forum forum) {
		VFSContainer container = new OlatRootFolderImpl(FolderConfig.getUserHomes() + File.separator + ureq.getIdentity().getName() + "/private/archive", null);
		// append export timestamp to avoid overwriting previous export 
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss");
		String folder = "forum_" + forum.getKey().toString()+"_"+formatter.format(new Date());
		VFSItem vfsItem = container.resolve(folder);
		if (vfsItem == null || !(vfsItem instanceof VFSContainer)) {
			vfsItem = container.createChildContainer(folder);
		}
		container = (VFSContainer) vfsItem;
		return container;
	}
	

	/**
	 * Comparators can be passed to a sort method (such as Collections.sort) 
	 * to allow precise control over the sort order.  
	 * <p>
	 * Sticky threads first, last modified first.
	 * 
	 * @return a MessageNode comparator.
	 * @see java.util.Comparator 
	 */
	public static Comparator<MessageNode> getMessageNodeComparator() {
		return new MessageNodeComparator();
	}
	
	private static class MessageNodeComparator implements Comparator<MessageNode> {
		@Override
		public int compare(final MessageNode m1, final MessageNode m2) {			
			if(m1.isSticky() && m2.isSticky()) {
				return m2.getModifiedDate().compareTo(m1.getModifiedDate()); //last first
			} else if(m1.isSticky()) {
				return -1;
			} else if(m2.isSticky()){
				return 1;
			} else {
				return m2.getModifiedDate().compareTo(m1.getModifiedDate()); //last first
			}				
		}
	}
}