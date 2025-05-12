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
package org.olat.modules.topicbroker.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.topicbroker.TBBrokerRef;

/**
 * 
 * Initial date: May 7, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TopicBrokerFilesExport {
	
	private static final Logger log = Tracing.createLoggerFor(TopicBrokerFilesExport.class);
	
	private final TBBrokerRef broker;
	private final Map<String, Map<String, VFSLeaf>> topicIdentToFileIdentToLeaf;
	
	public TopicBrokerFilesExport(TBBrokerRef broker, Map<String, Map<String, VFSLeaf>> topicIdentToFileIdentToLeaf) {
		this.broker = broker;
		this.topicIdentToFileIdentToLeaf = topicIdentToFileIdentToLeaf;
	}
	
	public void export(ZipOutputStream zout, String filesPath) throws IOException {
		if (topicIdentToFileIdentToLeaf != null && !topicIdentToFileIdentToLeaf.isEmpty()) {
			for (Entry<String, Map<String, VFSLeaf>> entry : topicIdentToFileIdentToLeaf.entrySet()) {
				String topicPath = filesPath + StringHelper.transformDisplayNameToFileSystemName(entry.getKey()) + "/";
				for (Entry<String, VFSLeaf> fileIdentToLeaf : entry.getValue().entrySet()) {
					String filePath = topicPath + fileIdentToLeaf.getKey() + "/";
					VFSLeaf vfsLeaf = fileIdentToLeaf.getValue();
					if (vfsLeaf != null && vfsLeaf.exists()) {
						InputStream inputStream = vfsLeaf.getInputStream();
						try {
							filePath += vfsLeaf.getName();
							zout.putNextEntry(new ZipEntry(filePath));
							FileUtils.copy(inputStream, zout);
							zout.closeEntry();
						} catch(Exception e) {
							log.error("Error during export of topic broker files (key::{})", broker.getKey(), e);
						} finally {
							FileUtils.closeSafely(inputStream);
						}
					} else {
						zout.putNextEntry(new ZipEntry(filePath));
					}
				}
			}
		}
	}

}
