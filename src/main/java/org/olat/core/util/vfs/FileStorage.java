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
package org.olat.core.util.vfs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * 
 * Initial date: 28.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FileStorage {
	
	private static final Logger log = Tracing.createLoggerFor(FileStorage.class);

	private VFSContainer rootContainer;
	
	public FileStorage(VFSContainer rootContainer) {
		this.rootContainer = rootContainer;
	}

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
	
	public String generateDir(String uuid, boolean addNumberedDir) {
		if(addNumberedDir) {
			return generateDir(uuid);
		}

		String cleanUuid = uuid.replace("-", "");
		String firstToken = cleanUuid.substring(0, 2);
		String secondToken = cleanUuid.substring(2, 4);
		String thirdToken = cleanUuid.substring(4, 6);

		VFSContainer firstContainer = getNextDirectory(rootContainer, firstToken);
		VFSContainer secondContainer = getNextDirectory(firstContainer, secondToken);
		getNextDirectory(secondContainer, thirdToken);

		StringBuilder sb = new StringBuilder();
		sb.append(firstToken).append("/")
		  .append(secondToken).append("/")
		  .append(thirdToken).append("/");
		String path = sb.toString();
		return path;
	}
	
	protected String createContainer(String firstToken, String secondToken, String thirdToken) {
		VFSContainer firstContainer = getNextDirectory(rootContainer, firstToken);
		VFSContainer secondContainer = getNextDirectory(firstContainer, secondToken);
		VFSContainer thirdContainer = getNextDirectory(secondContainer, thirdToken);
		//create a numbered container
		
		String lastToken = null;
		List<VFSItem> items = thirdContainer.getItems();
		if(items.isEmpty()) {
			lastToken = "01";
		} else {
			Set<String> names = new HashSet<>();
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
					break;
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
		String[] tokens = dir.split("/");		
		VFSContainer container = rootContainer;
		for(String token:tokens) {
			container = getNextDirectory(container, token);
		}
		return container;
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