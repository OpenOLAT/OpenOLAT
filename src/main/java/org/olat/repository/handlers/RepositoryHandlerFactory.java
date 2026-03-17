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
 */
@Service
public class RepositoryHandlerFactory {

	public static final int OTHER_TYPES_ORDER = 100000;

	private static Map<String, RepositoryHandler> handlerMap;
	private static List<OrderedRepositoryHandler> handlerList;
	private static List<OrderedRepositoryHandler> forCreationOnlyHandlerList;
	static {
		handlerMap = new HashMap<>(21);
		handlerList = new ArrayList<>(21);
		forCreationOnlyHandlerList = new ArrayList<>();

		registerHandler(new CourseHandler(), 0);
		registerHandler(new CourseTemplateHandler(), 1, true);
		
		// 10: QTI 2.1 Test
		
		registerHandler(new SCORMCPHandler(), 20);
		// 21: Video
		// 22: Evaluation form
		registerHandler(new SharedFolderHandler(), 23);
		
		registerHandler(new BlogHandler(), 30);
		registerHandler(new PodcastHandler(), 31);
		registerHandler(new WebDocumentHandler(SoundFileResource.TYPE_NAME), 32);
		registerHandler(new ImsCPHandler(), 33);
		registerHandler(new WikiHandler(), 34);
		
		// 40: Portfolio 2.0 template
		registerHandler(new GlossaryHandler(), 41);
		
		DocumentEditorDelegate wordDelegate = new DocumentEditorDelegate(new WordVFSEditorDelegateType());
		registerHandler(new WebDocumentHandler(DocFileResource.TYPE_NAME, wordDelegate, wordDelegate), 10001);
		DocumentEditorDelegate excelDelegate = new DocumentEditorDelegate(new ExcelVFSEditorDelegateType());
		registerHandler(new WebDocumentHandler(XlsFileResource.TYPE_NAME, excelDelegate, excelDelegate), 10002);
		DocumentEditorDelegate powerPointDelegate = new DocumentEditorDelegate(new PowerPointVFSEditorDelegateType());
		registerHandler(new WebDocumentHandler(PowerpointFileResource.TYPE_NAME, powerPointDelegate, powerPointDelegate), 10003);
		registerHandler(new WebDocumentHandler(PdfFileResource.TYPE_NAME), 10010);
		registerHandler(new WebDocumentHandler(ImageFileResource.TYPE_NAME), 10011);
		
		registerHandler(new WebDocumentHandler(MovieFileResource.TYPE_NAME), 100002);
		registerHandler(new WebDocumentHandler(AnimationFileResource.TYPE_NAME), 100003);
		registerHandler(new WebDocumentHandler(FileResource.GENERIC_TYPE_NAME), 100004);
		
		// Legacy
		registerHandler(new QTITestHandler(), 100000);
		registerHandler(new QTISurveyHandler(), 100001);
	}

	public static void registerHandler(RepositoryHandler handler, int order) {
		registerHandler(handler, order, false);
	}

	public static void registerHandler(RepositoryHandler handler, int order, boolean forCreationOnly) {
		handlerMap.put(handler.getSupportedType(), handler);
		OrderedRepositoryHandler oHandler = new OrderedRepositoryHandler(handler, order);

		if (forCreationOnly) {
			if (forCreationOnlyHandlerList.contains(oHandler)) {
				forCreationOnlyHandlerList.remove(oHandler);
			}
			forCreationOnlyHandlerList.add(oHandler);
		} else {
			if (handlerList.contains(oHandler)) {
				handlerList.remove(oHandler);
			}
			handlerList.add(oHandler);
		}
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

	public List<String> getOtherTypes() {
		List<String> otherTypes = new ArrayList<>();
		for (OrderedRepositoryHandler handler : handlerList) {
			if (handler.getOrder() >= OTHER_TYPES_ORDER) {
				otherTypes.add(handler.getHandler().getSupportedType());
			}
		}
		return otherTypes;
	}
	
	public List<OrderedRepositoryHandler> getOrderedRepositoryHandlersForCreation() {
		List<OrderedRepositoryHandler> extended = new ArrayList<>(handlerList);
		extended.addAll(forCreationOnlyHandlerList);
		Collections.sort(extended);
		return extended;
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
