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

package org.olat.repository.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.fileresource.types.AnimationFileResource;
import org.olat.fileresource.types.DocFileResource;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.ImageFileResource;
import org.olat.fileresource.types.MovieFileResource;
import org.olat.fileresource.types.PdfFileResource;
import org.olat.fileresource.types.PowerpointFileResource;
import org.olat.fileresource.types.SoundFileResource;
import org.olat.fileresource.types.XlsFileResource;
import org.olat.repository.RepositoryEntry;
import org.springframework.stereotype.Service;

/**
 * Initial Date:  Apr 6, 2004
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
@Service
public class RepositoryHandlerFactory {

	private static Map<String, RepositoryHandler> handlerMap;
	private static List<OrderedRepositoryHandler> handlerList;
	static {
		handlerMap = new HashMap<>(21);
		handlerList = new ArrayList<>(21);

		// 0-9 Most important resources = 0-9
		registerHandler(new CourseHandler(), 0);
		// 10-19 Assessment modules
		// 20-29 Content modules
		registerHandler(new SCORMCPHandler(), 20);
		registerHandler(new ImsCPHandler(), 21);
		registerHandler(new WikiHandler(), 22);
		// 30-39 Interactive modules
		registerHandler(new PodcastHandler(), 31);
		registerHandler(new BlogHandler(), 32);
		// 40-49 Supporting resources
		registerHandler(new SharedFolderHandler(), 40);
		registerHandler(new GlossaryHandler(), 41);
		
		
		DocumentEditorDelegate wordDelegate = new DocumentEditorDelegate(new WordVFSEditorDelegateType());
		registerHandler(new WebDocumentHandler(DocFileResource.TYPE_NAME, wordDelegate, wordDelegate), 10001);
		DocumentEditorDelegate excelDelegate = new DocumentEditorDelegate(new ExcelVFSEditorDelegateType());
		registerHandler(new WebDocumentHandler(XlsFileResource.TYPE_NAME, excelDelegate, excelDelegate), 10002);
		DocumentEditorDelegate powerPointDelegate = new DocumentEditorDelegate(new PowerPointVFSEditorDelegateType());
		registerHandler(new WebDocumentHandler(PowerpointFileResource.TYPE_NAME, powerPointDelegate, powerPointDelegate), 10003);
		registerHandler(new WebDocumentHandler(PdfFileResource.TYPE_NAME), 10010);
		registerHandler(new WebDocumentHandler(ImageFileResource.TYPE_NAME), 10011);
		registerHandler(new WebDocumentHandler(SoundFileResource.TYPE_NAME), 10020);
		registerHandler(new WebDocumentHandler(MovieFileResource.TYPE_NAME), 10021);
		registerHandler(new WebDocumentHandler(AnimationFileResource.TYPE_NAME), 10022);
		registerHandler(new WebDocumentHandler(FileResource.GENERIC_TYPE_NAME), 10100);
		
		// Legacy
		registerHandler(new QTISurveyHandler(), 10);
		registerHandler(new QTITestHandler(), 10);
	}

	public static void registerHandler(RepositoryHandler handler, int order) {
		handlerMap.put(handler.getSupportedType(), handler);
		OrderedRepositoryHandler oHandler = new OrderedRepositoryHandler(handler, order);
		if(handlerList.contains(oHandler)) {
			handlerList.remove(oHandler);
		}
		handlerList.add(oHandler);
	}
	
	public static RepositoryHandlerFactory getInstance() {
		return CoreSpringFactory.getImpl(RepositoryHandlerFactory.class);
	}
	
	/**
	 * Get the repository handler for this repository entry.
	 * @param re
	 * @return the handler or null if no appropriate handler could be found
	 */
	public RepositoryHandler getRepositoryHandler(RepositoryEntry re) {
		OLATResourceable ores = re.getOlatResource();
		if (ores == null) throw new AssertException("No handler found for resource. ores is null.");
		return getRepositoryHandler(ores.getResourceableTypeName());
	}
	
	/**
	 * Get a repository handler which supports the given resourceable type.
	 * @param resourceableTypeName
	 * @return the handler or null if no appropriate handler could be found
	 */
	public RepositoryHandler getRepositoryHandler(String resourceableTypeName) {
		return handlerMap.get(resourceableTypeName);
	}
	
	public List<OrderedRepositoryHandler> getOrderRepositoryHandlers() {
		List<OrderedRepositoryHandler> ordered = new ArrayList<>(handlerList);
		Collections.sort(ordered);
		return ordered;
	}
	
	/**
	 * Get a set of types this factory supports.
	 * @return Set of supported types.
	 */
	public Set<String> getSupportedTypes() {
		return handlerMap.keySet();
	}
	
	public static class OrderedRepositoryHandler implements Comparable<OrderedRepositoryHandler> {
		private final int order;
		private final RepositoryHandler handler;
		
		public OrderedRepositoryHandler(RepositoryHandler handler, int order) {
			this.handler = handler;
			this.order = order;
		}

		public int getOrder() {
			return order;
		}

		public RepositoryHandler getHandler() {
			return handler;
		}

		@Override
		public int compareTo(OrderedRepositoryHandler o) {
			return order - o.order;
		}

		@Override
		public int hashCode() {
			return handler.getSupportedType().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof OrderedRepositoryHandler) {
				OrderedRepositoryHandler oh = (OrderedRepositoryHandler)obj;
				return handler.getSupportedType().equals(oh.getHandler().getSupportedType());
			}
			return false;
		}
	}
}
