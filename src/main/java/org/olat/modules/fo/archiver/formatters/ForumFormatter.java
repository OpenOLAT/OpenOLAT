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

package org.olat.modules.fo.archiver.formatters;

import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.Visitor;
import org.olat.modules.fo.Forum;

/**
 * Initial Date: Nov 11, 2005 <br>
 * @author Patrick Brunner, Alexander Schneider
 */

public abstract class ForumFormatter implements Visitor {
	protected StringBuilder sb;
	protected boolean isTopThread = false;
	protected boolean filePerThread = false;
	private Long forumKey;

	protected final Translator translator;

	/**
	 * init string buffer
	 *
	 */
	protected ForumFormatter(Locale locale){
		sb = new StringBuilder(4096);
		translator = Util.createPackageTranslator(Forum.class, locale);
	}
	/**
	 * contains (translation keys, value) pairs such as:
	 * forum.metainfo.key,1234556
	 * forum.metainfo.topthreadcnt, number of top threads
	 * forum.metainfo.msgcnt, number of messages
	 * 
	 * @param metaInfo
	 */
	public void setForumKey(Long forumKey){
		this.forumKey = forumKey;
	}
	/**
	 * inform formatter that a new top thread has started, 
	 * e.g. ForumArchiveManager sets this if next node in topnode list is worked on
	 *
	 */
	public void openThread(){
		isTopThread = true;
	}
	/**
	 * inform formatter, that the top thread is completly consumed, thus create the formatted result for this thread
	 * @return
	 */
	public StringBuilder closeThread(){
		StringBuilder retVal = sb;
		sb = new StringBuilder();
		return retVal;
	}
	/**
	 * 
	 * @param key
	 * @return value of key
	 */
	public Long getForumKey() {
		return forumKey;
	}
	
	/**
	 * 
	 * @return true if every thread is saved in his own file; false if all threads are saved in one file
	 */
	public boolean isFilePerThread(){
		return filePerThread;
	}
	
	/**
	 * inform formatter that the forum is opened, and it must expect top threads being opened.
	 */
	public abstract void openForum();
	
	/**
	 *inform formatter that all top thread of the forum are consumed.
	 */
	public abstract StringBuilder closeForum();
	
	/**
	 * 
	 * @see org.olat.core.util.tree.Visitor#visit(org.olat.core.util.nodes.INode)
	 */
	public abstract void visit(INode node);
}
