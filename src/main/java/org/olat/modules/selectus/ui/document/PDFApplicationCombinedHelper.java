/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.EnvironmentProfile;
import org.apache.fop.apps.EnvironmentalProfileFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.apache.logging.log4j.Logger;
import org.apache.xmlgraphics.io.ResourceResolver;
import org.apache.xmlgraphics.io.URIResolverAdapter;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.xml.XMLFactories;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.DocumentOption;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.manager.TemplatesCache;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.pdf.PDFMergeUtility;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  8 sept. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PDFApplicationCombinedHelper {
	private static final Logger log = Tracing.createLoggerFor(PDFApplicationCombinedHelper.class);

	private final Position position;
	private final Application application;
	
	private final Translator translator;
	private final RecruitingModule recruitingModule;
	private final RecruitingService recruitingService;
	
	private final ApplicationXML applicationXml;
	private final ApplicationXMLV2 applicationXmlV2;

	public PDFApplicationCombinedHelper(Application application, Position position, RecruitingPositionSecurityCallback secCallback, Translator translator) {
		this.translator = Util.createPackageTranslator(PDFApplicationCombinedHelper.class, translator.getLocale(), translator);
		recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		recruitingService = CoreSpringFactory.getImpl(RecruitingService.class);
		this.position = position;
		this.application = application;
		applicationXml = new ApplicationXML(position, application, translator, recruitingModule);
		applicationXmlV2 = new ApplicationXMLV2(position, application, secCallback, translator);
	}
	
	public String getPDFFilename() {
		String name = application.getId() + "_" + application.getPerson().getLastName() 
			+ "_" + application.getPerson().getFirstName();
		return RecruitingHelper.normalizeFilename(name) + "_combined.pdf";
	}
	
	public String getZIPFilename() {
		String name = application.getId() + "_" + application.getPerson().getLastName() 
			+ "_" + application.getPerson().getFirstName();
		return RecruitingHelper.normalizeFilename(name) + "_combined.zip";
	}
	
	public String getExpertOpinionsFilename() {
		String name = application.getId() + "_" + application.getPerson().getLastName() 
			+ "_" + application.getPerson().getFirstName();
		return RecruitingHelper.normalizeFilename(name) + "_expert_opinions_combined.pdf";
	}
	
	public String getExpertOpinionsFilename(Reference reference) {
		String name = application.getId() + "_" + application.getPerson().getLastName() 
			+ "_" + application.getPerson().getFirstName() + "_" + reference.getFirstName() + "_" + reference.getLastName();
		return RecruitingHelper.normalizeFilename(name) + ".pdf";
	}
	
	public void combineDocumentsAndOtherDocumentsStreamed(String directory, ZipOutputStream zipOut) throws IOException {
		String filename = getPDFFilename();
		if(!recruitingModule.isAllDocumentsInCombinedFile()) {
			directory = RecruitingHelper.normalizeFilename(directory);
			if(StringHelper.containsNonWhitespace(directory)) {
				filename = directory + "/" + filename;
			}
		}

		zipOut.putNextEntry(new ZipEntry(filename));
		combineDocumentsStreamed(zipOut);
		zipOut.closeEntry();
		combineOtherDocuments(directory, zipOut);
	}
	
	public boolean combineOtherDocuments(String directory, ZipOutputStream zipOut) throws IOException {
		boolean hasOthers = false;
		
		Set<DocumentEnum> inCombined = recruitingModule.getDocumentsInCombinedFile(position);
		for(DocumentOption docOption:recruitingModule.getDocumentOptions()) {
			DocumentEnum doc = docOption.getDoc();
			if(!inCombined.contains(doc)) {
				Attachment attachment = doc.path(application);
				if(attachment != null) {
					byte[] datas = recruitingService.getAttachmentDatas(attachment);
					if(datas != null && datas.length > 0) {
						String filename = position.getDocumentName(doc, translator.getLocale());
						if(!StringHelper.containsNonWhitespace(filename)) {
							filename = translator.translate(doc.i18nKey());
						}
						String name = application.getId() + "_" + filename + "_" + application.getPerson().getLastName() 
								+ "_" + application.getPerson().getFirstName();
						name = RecruitingHelper.normalizeFilename(name);
						if(StringHelper.containsNonWhitespace(attachment.getType())) {
							name += "." + attachment.getType();
						} else {
							name += ".pdf";
						}
						
						if(StringHelper.containsNonWhitespace(directory)) {
							name = directory + "/" + name;
						}
						zipOut.putNextEntry(new ZipEntry(name));
						zipOut.write(datas);
						zipOut.closeEntry();
						zipOut.flush();
						hasOthers = true;
					}
				}
			}
		}
		
		return hasOthers;
	}
	
	public void combineDocumentsStreamed(OutputStream out) {
		try(PDFMergeUtility merger = new PDFMergeUtility()) {
			appendSummary(merger);

			Set<DocumentEnum> inCombined = recruitingModule.getDocumentsInCombinedFile(position);

			for(DocumentOption docOption:recruitingModule.getDocumentOptions()) {
				DocumentEnum doc = docOption.getDoc();
				if(inCombined.contains(doc)) {
					Attachment attachment = doc.path(application);
					if(attachment != null) {
						String name = position.getDocumentName(doc, translator.getLocale());
						if(!StringHelper.containsNonWhitespace(name)) {
							name = translator.translate(doc.i18nKey());
						}
						appendDocument(doc, name, merger);
					}
				}
			}
			
			List<Reference> references = recruitingService.getApplicationReferences(application, ReferenceType.recommendation);
			List<Reference> submittedReferences = new ArrayList<>(references.size());
			for(Reference reference:references) {
				if(reference.getReferenceType() == ReferenceType.recommendation && reference.getLetter() != null) {
					submittedReferences.add(reference);
				}
			}

			if(!submittedReferences.isEmpty()) {
				int count = 1;
				for(Reference reference:submittedReferences) {
					appendReference(reference, count++, merger);
				}
			}
			
			merger.merge(out);
		} catch (Exception e) {
			log.error("Cannot combined PDFs", e);
		} catch (Error e) {
			log.error("Cannot combined PDFs", e);
		}
	}
	
	private void appendReference(Reference reference, int count, PDFMergeUtility merger) {
		String name = "Reference " + count;
		try(InputStream summary = addForMerge(createReferenceDocument(reference, reference.getReferenceType(), count), "referencesep.xslt");
			InputStream document = addForMerge(reference.getLetter())) {
			if(!merger.addDocument(name, summary, document)) {
				appendError(name, merger);
			}
		} catch(Exception e) {
			log.error("Cannot combine reference document in PDF: {}", count, e);
			appendError(name, merger);
		}
	}
	
	private Document createReferenceDocument(Reference reference, ReferenceType type, int pos)
	throws ParserConfigurationException  {
		Document doc = createDocument();
		Element rootEl = doc.createElement("reference");
		applicationXml.appendReferenceDocument(doc, rootEl, reference, type, pos);
		return doc;
	}
	
	private void appendDocument(DocumentEnum doc, String name, PDFMergeUtility merger) {
		boolean allOk = true;
		
		if(recruitingModule.isApplicationPdfPageSeparatorEnabled()) {
			try(InputStream summary = addForMerge(createSeparatorDocument(doc), "pagesep.xslt");
				InputStream document = addForMerge(doc.path(application))) {
				if(document != null) {
					allOk &= merger.addDocument(name, summary, document);
					DBFactory.getInstance().commitAndCloseSession();
				}
			} catch(Exception e) {
				log.error("Cannot combine document in PDF: {}", doc, e);
			}
		} else {
			try(InputStream document = addForMerge(doc.path(application))) {
				if(document != null) {
					allOk &= merger.addDocument(name, null, document);
					DBFactory.getInstance().commitAndCloseSession();
				}
			} catch(Exception e) {
				log.error("Cannot combine document in PDF: {}", doc, e);
			}
		}
		
		if(!allOk) {
			appendError(name, merger);
		}
	}
	
	private Document createSeparatorDocument(DocumentEnum docEnum)
	throws ParserConfigurationException {
		Document doc = createDocument();
		Element rootEl = doc.createElement("separator");
		applicationXml.appendSeparatorDocument(doc, rootEl, docEnum);
		return doc;
	}
	
	private void appendError(String document, PDFMergeUtility merger) {
		try(InputStream summaryStream = addForMerge(createErrorDocument(document), "error.xslt")) {
			merger.addSummary("Error", summaryStream);
		} catch(Exception e) {
			log.error("", e);
		}
	}
	
	private Document createErrorDocument(String document)
	throws ParserConfigurationException {
		Document doc = createDocument();
		Element rootEl = (Element)doc.appendChild(doc.createElement("error"));
		applicationXml.appendErrorDocument(doc, rootEl, document);
		return doc;
	}
	
	private void appendSummary(PDFMergeUtility merger) {
		try(InputStream summaryStream = addForMerge(createCoverDocument(), "cover.xslt")) {
			merger.addSummary("Introduction", summaryStream);
		} catch(Exception e) {
			log.error("", e);
		}
	}
	
	private Document createCoverDocument()
	throws ParserConfigurationException {
		Document doc = createDocument();
		Element rootEl = (Element)doc.appendChild(doc.createElement("coverxml"));
		if("v2".equals(recruitingModule.getDocumentCombinedCoverVersion())) {
			Element coverV2El = (Element)rootEl.appendChild(doc.createElement("cover-v2"));
			applicationXmlV2.appendCoverDocument(doc, coverV2El);
		} else {
			Element coverEl = (Element)rootEl.appendChild(doc.createElement("cover"));
			applicationXml.appendCoverDocument(doc, coverEl);
		}
		return doc;
	}
	
	public void combineExpertOpinionsStreamed(OutputStream out) {
		try(PDFMergeUtility merger = new PDFMergeUtility()) {
			List<Reference> allReferences = recruitingService.getApplicationReferences(application, null);

			List<Reference> submittedReferences = new ArrayList<>(allReferences.size());
			for(Reference reference:allReferences) {
				if(((position.isExpertRecommendationEnabled() &&  reference.getReferenceType() == ReferenceType.expert)
						|| (position.isComparativeAssessmentExpertEnabled() && reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert))
						&& reference.getLetter() != null) {
					submittedReferences.add(reference);
				}
			}

			if(!submittedReferences.isEmpty()) {
				int count = 1;
				for(Reference reference:submittedReferences) {
					mergeReference(merger, reference, count++);
				}
			}

			merger.merge(out);
		} catch (Exception e) {
			log.error("Cannot combined PDFs", e);
		} catch (Error e) {
			log.error("Cannot combined PDFs", e);
		}
	}
	
	private void mergeReference(PDFMergeUtility merger, Reference reference, int count) {
		try(InputStream summary = addForMerge(createReferenceDocument(reference, reference.getReferenceType(), count), "referencesep.xslt");
				InputStream document = addForMerge(reference.getLetter())) {
			String name = "Expert " + count;
			merger.addDocument(name, summary, document);
		} catch (Exception e) {
			log.error("Cannot combined PDFs", e);
		}
	}
	
	private InputStream addForMerge(Document doc, String stylesheet) 
	throws FOPException, TransformerException, IOException {
		ByteArrayOutputStream coverOut = new ByteArrayOutputStream();
		transform(doc,  coverOut, stylesheet);
		coverOut.flush();
		
		InputStream coverIn = null;
		byte[] datas = coverOut.toByteArray();
		if(datas != null && datas.length > 0) {
			coverIn = new ByteArrayInputStream(datas);
		}
		coverOut.close();
		return coverIn;
	}
	
	private InputStream addForMerge(Attachment attachment) {
		byte[] datas = recruitingService.getAttachmentDatas(attachment);
		if(datas != null) {
			try {
				return new ByteArrayInputStream(datas);
			} catch (Exception e) {
				log.error("", e);
			}
		}
		return null;
	}
	
	private void transform(Document doc, OutputStream out, String xsltName) 
	throws TransformerException, FOPException {
		// Setup output
		// configure fopFactory as desired
		URI defaultBaseUri = new File(WebappHelper.getContextRoot()).toURI();
		ResourceResolver resourceResolver = new URIResolverAdapter(new ClasspathURIResolver());
		EnvironmentProfile env = EnvironmentalProfileFactory.createDefault(defaultBaseUri, resourceResolver);

		FopFactoryBuilder config = new FopFactoryBuilder(env);
        FopFactory fopFactory = config.build();

		FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
		// configure foUserAgent as desired
    	
		// Construct fop with desired output format
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);
		//print(doc);

		Transformer transformer = CoreSpringFactory.getImpl(TemplatesCache.class)
				.getTransformer(xsltName);
		Source src = new DOMSource(doc);

		// Resulting SAX events (the generated FO) must be piped through to FOP
		Result res = new SAXResult(fop.getDefaultHandler());
		transformer.transform(src, res);
	}
	
	private Document createDocument()
	throws ParserConfigurationException {
		DocumentBuilderFactory dbf = XMLFactories.newDocumentBuilderFactory();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.newDocument();
	}
	
	public static void print(Document document) {
	    try {
	    	TransformerFactory factory = XMLFactories.newTransformerFactory();
			Transformer transformer = factory.newTransformer();
			Source source = new DOMSource(document);
			Result output = new StreamResult(System.out);
			transformer.transform(source, output);
			System.out.println();
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private class ClasspathURIResolver implements URIResolver {
		@Override
		public Source resolve(String href, String base)
		throws TransformerException {
			if(href != null) {
				InputStream in = PDFApplicationCombinedHelper.class.getResourceAsStream(href);
				if(in != null) {
					return new StreamSource(in);
				}
			}
			return null;
		}
	}
}
