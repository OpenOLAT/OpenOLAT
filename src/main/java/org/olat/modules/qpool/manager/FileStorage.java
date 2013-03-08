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
package org.olat.modules.qpool.manager;

import java.util.UUID;

import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.qpool.QuestionPoolModule;

/**
 * 
 * 
 * 
 * Initial date: 07.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FileStorage {
	
	private static final OLog log = Tracing.createLoggerFor(FileStorage.class);

	public static String generateDir() {
		String uuid = UUID.randomUUID().toString();
		return generateDir(uuid);
	}
	
	public static String generateDir(String uuid) {
		String cleanUuid = uuid.replace("-", "");
		String firstToken = cleanUuid.substring(0, 2);
		String secondToken = cleanUuid.substring(2, 4);
		String thirdToken = cleanUuid.substring(4, 6);
		String forthToken = cleanUuid.substring(6, 8);
		StringBuilder sb = new StringBuilder();
		sb.append(firstToken).append("/")
		  .append(secondToken).append("/")
		  .append(thirdToken).append("/")
		  .append(forthToken).append("/");
		return sb.toString();
	}
	
	public static VFSContainer getContainer(String dir) {
		VFSContainer rootContainer = CoreSpringFactory.getImpl(QuestionPoolModule.class).getRootContainer();
		String[] tokens = dir.split("/");
		String firstToken = tokens[0];
		VFSContainer firstContainer = getNextDirectory(rootContainer, firstToken);
		String secondToken = tokens[1];
		VFSContainer secondContainer = getNextDirectory(firstContainer, secondToken);
		String thirdToken = tokens[2];
		VFSContainer thridContainer = getNextDirectory(secondContainer, thirdToken);
		String forthToken = tokens[3];
		return getNextDirectory(thridContainer, forthToken);
	}
	
	private static VFSContainer getNextDirectory(VFSContainer container, String token) {
		VFSItem nextContainer = container.resolve(token);
		if(nextContainer instanceof VFSContainer) {
			return (VFSContainer)nextContainer;
		} else if (nextContainer instanceof VFSLeaf) {
			log.error("");
			return null;
		}
		return container.createChildContainer(token);
	}
}