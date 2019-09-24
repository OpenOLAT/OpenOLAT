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

package org.olat.core.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.LocalImpl;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Description:<br>
 * Only a map which logged the current work of threads
 * 
 * <P>
 * Initial Date:  6 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class WorkThreadInformations {
	
	private static final Logger log = Tracing.createLoggerFor(WorkThreadInformations.class);
	
	private static final Map<String,String> works = new HashMap<>();
	private static final List<String> longRunningTasks = new ArrayList<>();
	
	public synchronized static void setLongRunningTask(String taskDesc) {
		longRunningTasks.add(taskDesc);
	}
	
	public synchronized static void unsetLongRunningTask(String taskDesc) {
		longRunningTasks.remove(taskDesc);
	}
	
	public synchronized static List<String> getLongRunningTasks() {
		return new ArrayList<>(longRunningTasks);
	}
	
	public synchronized static void set(String message) {
		String threadName = Thread.currentThread().getName();
		if(StringHelper.containsNonWhitespace(message)) {
			works.put(threadName, message);
		} else {
			works.remove(threadName);
		}
	}
	
	public synchronized static void unset() {
		String threadName = Thread.currentThread().getName();
		works.remove(threadName);
	}
	
	public synchronized static String get(String threadName) {
		return works.get(threadName);
	}
	
	public synchronized static void currentThreadNames(List<String> threadNames) {
		for(Iterator<String> threadNameIt=works.keySet().iterator(); threadNameIt.hasNext(); ) {
			if(!threadNames.contains(threadNameIt.next())) {
				threadNameIt.remove();
			}
		}
	}
	
	public static void setInfoFiles(String filePath, VFSLeaf leaf) {
		try {
			File file = new File(WebappHelper.getUserDataRoot(), "threadInfos");
			if(!file.exists()) {
				file.mkdirs();
			}
			if(leaf instanceof LocalImpl) {
				filePath = ((LocalImpl)leaf).getBasefile().getAbsolutePath();
			}
			File infoFile = new File(file, Thread.currentThread().getName());
			FileUtils.save(infoFile, filePath, "UTF-8");
		} catch (Exception e) {
			log.error("Cannot write info message about FolderIndexerWorker: " + filePath, e);
		}
	}
}
