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
package org.olat.search.service.document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.nodes.PageCourseNode;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PagePart;
import org.olat.modules.ceditor.manager.ContentEditorFileStorage;
import org.olat.modules.ceditor.model.TableColumn;
import org.olat.modules.ceditor.model.TableRow;
import org.olat.modules.ceditor.model.jpa.HTMLPart;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.model.jpa.ParagraphPart;
import org.olat.modules.ceditor.model.jpa.TablePart;
import org.olat.modules.ceditor.model.jpa.TitlePart;
import org.olat.modules.cemedia.Media;
import org.olat.search.model.OlatDocument;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.file.DocumentAccessException;
import org.olat.search.service.document.file.FileDocumentFactory;

/**
 * 
 * Initial date: 25 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageDocument extends OlatDocument {

	private static final long serialVersionUID = 1579376331301889183L;
	private static final Logger log = Tracing.createLoggerFor(PageDocument.class);
	
	public static List<Document> createDocument(SearchResourceContext searchResourceContext, Page page, PageCourseNode courseNode) {	
		List<Document> documents = new ArrayList<>();
		PageDocument pageDocument = new PageDocument();
		if(courseNode != null && StringHelper.containsNonWhitespace(courseNode.getLongTitle())) {
			pageDocument.setTitle(courseNode.getLongTitle());
		} else if(courseNode != null && StringHelper.containsNonWhitespace(courseNode.getShortTitle())) {
			pageDocument.setTitle(courseNode.getShortTitle());
		} else if(StringHelper.containsNonWhitespace(page.getTitle())) {
			pageDocument.setTitle(page.getTitle());
		}
		pageDocument.setContent(getContent(page, searchResourceContext, documents));
		pageDocument.setCreatedDate(page.getCreationDate());
		pageDocument.setLastChange(page.getLastModified());
		pageDocument.setResourceUrl(searchResourceContext.getResourceUrl());
		pageDocument.setDocumentType(searchResourceContext.getDocumentType());
		pageDocument.setCssIcon("o_page_icon");
		pageDocument.setParentContextType(searchResourceContext.getParentContextType());
		pageDocument.setParentContextName(searchResourceContext.getParentContextName());
		
		if (log.isDebugEnabled()) log.debug(pageDocument.toString());
		Document document = pageDocument.getLuceneDocument();
		documents.add(document);
		return documents;
	}

	private static String getContent(Page page, SearchResourceContext searchResourceContext, List<Document> documents) {
		List<PagePart> parts = page.getBody().getParts();
		StringBuilder sb = new StringBuilder(32000);
		for(PagePart part:parts) {
			if(part instanceof HTMLPart || part instanceof TitlePart || part instanceof ParagraphPart) {
				appendHtmlContent(sb, part);
			} else if(part instanceof TablePart table) {
				appendTableContent(sb, table);
			} else if(part instanceof MediaPart mediaPart) {
				Document document = createMediaDocument(mediaPart.getMedia(), searchResourceContext);
				if(document != null) {
					documents.add(document);
				}
			}
		}
		return sb.toString();
	}
	
	private static Document createMediaDocument(Media media, SearchResourceContext searchResourceContext) {
		try {
			FileDocumentFactory documentFactory = CoreSpringFactory.getImpl(FileDocumentFactory.class);
			ContentEditorFileStorage fileStorage = CoreSpringFactory.getImpl(ContentEditorFileStorage.class);
			VFSContainer container = fileStorage.getMediaContainer(media);
			VFSItem item = container.resolve(media.getRootFilename());
			if(item instanceof VFSLeaf leaf) {
				return documentFactory.createDocument(searchResourceContext, leaf);
			}
		} catch (IOException | DocumentAccessException e) {
			log.error("", e);
		}
		return null;
	}
	
	private static void appendHtmlContent(StringBuilder sb, PagePart part) {
		String content = FilterFactory.getHtmlTagAndDescapingFilter().filter(part.getContent());
		if(StringHelper.containsNonWhitespace(content)) {
			sb.append(content).append(" ");
		}
	}
	
	private static void appendTableContent(StringBuilder sb, TablePart table) {
		List<TableRow> rows = table.getTableContent().getRows();
		if(rows != null && !rows.isEmpty()) {
			for(TableRow row:rows) {
				List<TableColumn> columns = row.getColumns();
				if(columns != null && !columns.isEmpty()) {
					for(TableColumn column:columns) {
						String content = FilterFactory.getHtmlTagAndDescapingFilter().filter(column.getContent());
						if(StringHelper.containsNonWhitespace(content)) {
							sb.append(content).append(" ");
						}
					}
				}	
			}
		}
	}
}
