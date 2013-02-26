package org.olat.search.service.indexer;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.olat.core.CoreSpringFactory;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionPoolService;
import org.olat.search.model.OlatDocument;
import org.olat.search.service.SearchResourceContext;

/**
 * 
 * Initial date: 25.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionItemIndexer extends AbstractHierarchicalIndexer {
	
	public static final String TYPE = "type.question.item";
	
	private static final int BATCH_SIZE = 100;

	@Override
	public String getSupportedTypeName() {
		return TYPE;
	}
	
	@Override
	public void doIndex(SearchResourceContext parentResourceContext, Object businessObj, OlatFullIndexer indexWriter)
	throws IOException,InterruptedException {
		
		QuestionPoolService qpoolService = CoreSpringFactory.getImpl(QuestionPoolService.class);

		int counter = 0;
		List<QuestionItem> items;
		do {
			items = qpoolService.getAllItems(counter, BATCH_SIZE);
			for(QuestionItem item:items) {
				processItem(item, parentResourceContext, indexWriter);
			}
			counter += items.size();
		} while(items.size() == BATCH_SIZE);
	}
	
	private void processItem(QuestionItem item, SearchResourceContext parentResourceContext, OlatFullIndexer indexWriter) {
		OlatDocument oDocument = new OlatDocument();
		/*Identity author = artefact.getAuthor();
		if(author != null) {
			document.setAuthor(author.getName());
		}*/
		oDocument.setCreatedDate(item.getCreationDate());
		oDocument.setTitle(item.getSubject());
		oDocument.setDescription(item.getDescription());
		oDocument.setResourceUrl("[QuestionItem:" + item.getKey() + "]");
		oDocument.setDocumentType(TYPE);
		oDocument.setCssIcon("o_qitem_icon");
		oDocument.setParentContextType(parentResourceContext.getParentContextType());
		oDocument.setParentContextName(parentResourceContext.getParentContextName());
		oDocument.setContent(item.getDescription());
		
		Document document = oDocument.getLuceneDocument();
		indexWriter.addPermDocument(document);
	}
}
