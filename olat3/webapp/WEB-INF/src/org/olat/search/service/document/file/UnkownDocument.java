package org.olat.search.service.document.file;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.search.service.SearchResourceContext;

public class UnkownDocument extends FileDocument {
	
	private final static OLog log = Tracing.createLoggerFor(UnkownDocument.class);

	public final static String UNKOWN_TYPE = "type.file.unkown";
	
	public static Document createDocument(SearchResourceContext leafResourceContext, VFSLeaf leaf)
	throws IOException, DocumentException, DocumentAccessException {
		UnkownDocument openDocument = new UnkownDocument();
		openDocument.init(leafResourceContext, leaf);
		openDocument.setFileType(UNKOWN_TYPE);
		openDocument.setCssIcon(CSSHelper.createFiletypeIconCssClassFor(leaf.getName()));
		if (log.isDebug()) log.debug(openDocument.toString());
		return openDocument.getLuceneDocument();
	}

	@Override
	protected String readContent(VFSLeaf leaf) {
		return "";
	}
}
