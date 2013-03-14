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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.qpool.QuestionPoolModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * 
 * 
 * Initial date: 07.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("qpoolFileStorage")
public class FileStorage {
	
	private static final OLog log = Tracing.createLoggerFor(FileStorage.class);
	
	@Autowired
	private QuestionPoolModule qpoolModule;

	public String generateDir() {
		String uuid = UUID.randomUUID().toString();
		return generateDir(uuid);
	}
	
	public String generateDir(String uuid) {
		String cleanUuid = uuid.replace("-", "");
		String firstToken = cleanUuid.substring(0, 2);
		String secondToken = cleanUuid.substring(2, 4);
		String thirdToken = cleanUuid.substring(4, 6);
		String forthToken = createContainer(firstToken, secondToken, thirdToken);
		StringBuilder sb = new StringBuilder();
		sb.append(firstToken).append("/")
		  .append(secondToken).append("/")
		  .append(thirdToken).append("/")
		  .append(forthToken).append("/");
		String path = sb.toString();
		return path;
	}
	
	protected String createContainer(String firstToken, String secondToken, String thirdToken) {
		VFSContainer rootContainer = qpoolModule.getRootContainer();
		VFSContainer firstContainer = getNextDirectory(rootContainer, firstToken);
		VFSContainer secondContainer = getNextDirectory(firstContainer, secondToken);
		VFSContainer thirdContainer = getNextDirectory(secondContainer, thirdToken);
		//create a numbered container
		
		String lastToken = null;
		List<VFSItem> items = thirdContainer.getItems();
		if(items.isEmpty()) {
			lastToken = "01";
		} else {
			Set<String> names = new HashSet<String>();
			for(VFSItem item:items) {
				names.add(item.getName());
			}
			
			for(int i=0; i<100; i++) {
				String potentielName = Integer.toString(i);
				if(potentielName.length() == 1) {
					potentielName = "0" + potentielName;
				}
				if(!names.contains(potentielName)) {
					lastToken = potentielName;
				}
			}
		}
		
		if(lastToken == null) {
			log.error("");
		}
		getNextDirectory(thirdContainer, lastToken);
		return lastToken;
	}
	
	public VFSContainer getContainer(String dir) {
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
	
	private VFSContainer getNextDirectory(VFSContainer container, String token) {
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