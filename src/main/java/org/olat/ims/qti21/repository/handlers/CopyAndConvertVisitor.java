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
package org.olat.ims.qti21.repository.handlers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.UUID;

import javax.xml.parsers.SAXParser;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.xml.XMLFactories;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.xml.AssessmentItemChecker;
import org.olat.ims.qti21.model.xml.BadRessourceHelper;
import org.olat.ims.qti21.model.xml.Onyx38ToQtiWorksHandler;
import org.olat.ims.qti21.model.xml.OnyxToQtiWorksHandler;
import org.olat.ims.qti21.model.xml.QTI21ExplorerHandler;
import org.olat.ims.qti21.model.xml.QTI21Infos;
import org.olat.ims.qti21.model.xml.QTI21Infos.InputType;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.DefaultHandler;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.node.RootNode;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.provision.BadResourceException;
import uk.ac.ed.ph.jqtiplus.reading.AssessmentObjectXmlLoader;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlInterpretationException;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.validation.ItemValidationResult;
import uk.ac.ed.ph.jqtiplus.validation.TestValidationResult;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.FileResourceLocator;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * Copy and eventually convert the XML files. Converter available:
 * <ul>
 * 	<li>Onyx 3.x to QtiWorks fix text directly under itemBody, remove font tags</li>
 *  <li>Onyx Web to QtiWorks fix the rubric issue (difference between the QTI model and the XSD)</li>
 * </ul>
 * It does a validation (as indication) but check if it can load the assessmentItem/assessmentTest
 * and open it. 
 * 
 * Initial date: 1 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class CopyAndConvertVisitor extends SimpleFileVisitor<Path> {
	
	private static final Logger log = Tracing.createLoggerFor(CopyAndConvertVisitor.class);
	
	private final Path source;
	private final Path destDir;
	private final PathMatcher filter;
	
	private QTI21Infos infos;
	
	public CopyAndConvertVisitor(Path source, Path destDir, QTI21Infos infos, PathMatcher filter) {
		this.source = source;
		this.destDir = destDir;
		this.filter = filter;
		this.infos = infos;
	}
	
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
    throws IOException {
		Path relativeFile = source.relativize(file);
		final Path destFile = Paths.get(destDir.toString(), relativeFile.toString());
		if(filter.matches(file)) {
			String filename = file.getFileName().toString();
			if(filename.startsWith(".")) {
				//ignore
			} else if(filename.endsWith("xml") && !filename.equals("imsmanifest.xml")) {
				convertXmlFile(file, destFile);
			} else {
				Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
			}
		}
        return FileVisitResult.CONTINUE;
	}
 
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
	throws IOException {
		Path relativeDir = source.relativize(dir);
        final Path dirToCreate = Paths.get(destDir.toString(), relativeDir.toString());
        if(!dirToCreate.toFile().exists()) {
        	Files.createDirectory(dirToCreate);
        }
        return FileVisitResult.CONTINUE;
	}
	
	/**
	 * Convert the XML files, assessmentItem or assessmentTest
	 * 
	 * @param inputFile
	 * @param outputFile
	 */
	public boolean convertXmlFile(Path inputFile, Path outputFile) {
		try {
			boolean validated = true;
			QTI21Infos fileInfos = scanFile(inputFile);
			//inherit from test if needed
			if(fileInfos.getEditor() == null && infos.getEditor() != null) {
				fileInfos.setEditor(infos.getEditor());
				fileInfos.setVersion(infos.getVersion());
			}
			if(onyx38Family(fileInfos)) {
				validated = convertXmlFile(inputFile, outputFile, fileInfos.getType(), Onyx38ToQtiWorksHandler::new);
			} else if(onyxWebFamily(fileInfos)) {
				validated = convertXmlFile(inputFile, outputFile, fileInfos.getType(), xtw ->
					 new OnyxToQtiWorksHandler(xtw, infos));
				
				if(validated && fileInfos.getType() == InputType.assessmentItem) {
					//check templateVariables
					checkAssessmentItem(outputFile);
				}
			} else {
				Files.copy(inputFile, outputFile, StandardCopyOption.REPLACE_EXISTING);
			}
			return validated;
		} catch (IOException | FactoryConfigurationError e) {
			log.error("", e);
			return false;
		}
	}
	
	private boolean onyx38Family(QTI21Infos fileInfos) {
		if(fileInfos == null || fileInfos.getEditor() == null) return false;
		String version = fileInfos.getVersion();
		return "Onyx Editor".equals(fileInfos.getEditor()) && version != null &&
				(version.startsWith("2.") || version.startsWith("3."));
	}
	
	private boolean onyxWebFamily(QTI21Infos fileInfos) {
		if(fileInfos == null || fileInfos.getEditor() == null) return false;
		return "ONYX Editor".equals(fileInfos.getEditor());
	}
	
	private QTI21Infos scanFile(Path inputFile) {
		QTI21ExplorerHandler infosHandler = new QTI21ExplorerHandler();
		try(InputStream in = Files.newInputStream(inputFile)) {
			SAXParser saxParser = XMLFactories.newSAXParser();
			saxParser.setProperty("http://xml.org/sax/properties/lexical-handler", infosHandler);
			saxParser.parse(in, infosHandler);
		} catch(Exception e1) {
			log.error("", e1);
		}
		return infosHandler.getInfos();
	}

	private boolean convertXmlFile(Path inputFile, Path outputFile, InputType type, HandlerProvider provider) {
		File tmpFile = new File(WebappHelper.getTmpDir(), UUID.randomUUID() + ".xml");
		try(InputStream in = Files.newInputStream(inputFile);
				Writer out = Files.newBufferedWriter(tmpFile.toPath(), StandardCharsets.UTF_8)) {
			XMLOutputFactory xof = XMLOutputFactory.newInstance();
	        XMLStreamWriter xtw = xof.createXMLStreamWriter(out);
			SAXParser saxParser = XMLFactories.newSAXParser();
			DefaultHandler myHandler = provider.create(xtw);
			saxParser.setProperty("http://xml.org/sax/properties/lexical-handler", myHandler);
			saxParser.parse(in, myHandler);
			
			boolean valid = validate(tmpFile.toPath(), type, true);
			if(valid) {
				if(!outputFile.getParent().toFile().exists()) {
					outputFile.getParent().toFile().mkdirs();
				}
				Files.copy(tmpFile.toPath(), outputFile, StandardCopyOption.REPLACE_EXISTING);
			}
			return valid;
		} catch(Exception e1) {
			log.error("", e1);
			return false;
		} finally {
			FileUtils.deleteFile(tmpFile);
		}
	}
	
	private boolean validate(Path inputFile, InputType type, boolean verbose) {
		try {
			QtiXmlReader qtiXmlReader = new QtiXmlReader(new JqtiExtensionManager());
			ResourceLocator fileResourceLocator = new PathResourceLocator(inputFile.getParent());
			AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, fileResourceLocator);
			
			RootNode rootNode = null;
			BadResourceException e = null;
			URI uri = new URI("zip", inputFile.getFileName().toString(), null);
			if(type == InputType.assessmentItem) {
				ItemValidationResult itemResult = assessmentObjectXmlLoader.loadResolveAndValidateItem(uri);
				e = itemResult.getResolvedAssessmentItem().getItemLookup().getBadResourceException();
				ResolvedAssessmentItem resolvedAssessmentItem = assessmentObjectXmlLoader.loadAndResolveAssessmentItem(uri);
				rootNode = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
			} else if(type == InputType.assessmentTest) {
				TestValidationResult testResult = assessmentObjectXmlLoader.loadResolveAndValidateTest(uri);
				e = testResult.getResolvedAssessmentTest().getTestLookup().getBadResourceException();
				ResolvedAssessmentTest resolvedAssessmentTest = assessmentObjectXmlLoader.loadAndResolveAssessmentTest(uri);
				rootNode = resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful();
			}
			
			if(e != null && verbose) {
				StringBuilder err = new StringBuilder();
				BadRessourceHelper.extractMessage(e, err);
				log.warn(err.toString());
			}
			
			return (rootNode != null) && (e == null || (e instanceof QtiXmlInterpretationException && ((QtiXmlInterpretationException)e).getXmlParseResult().getFatalErrors().isEmpty()));
		} catch (URISyntaxException e) {
			log.error("", e);
			return false;
		}
	}
	
	private void checkAssessmentItem(Path outputFile) {
		QTI21Service qtiService = CoreSpringFactory.getImpl(QTI21Service.class);
		QtiXmlReader qtiXmlReader = new QtiXmlReader(qtiService.jqtiExtensionManager());
		ResourceLocator fileResourceLocator = new FileResourceLocator();
		ResourceLocator inputResourceLocator = 
				ImsQTI21Resource.createResolvingResourceLocator(fileResourceLocator);
		
		URI assessmentObjectSystemId = outputFile.toFile().toURI();
		AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, inputResourceLocator);
		ResolvedAssessmentItem resolvedAssessmentItem = assessmentObjectXmlLoader.loadAndResolveAssessmentItem(assessmentObjectSystemId);
		AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();

		if(!AssessmentItemChecker.checkAndCorrect(assessmentItem)) {
			try(FileOutputStream out = new FileOutputStream(outputFile.toFile())) {
				qtiService.qtiSerializer().serializeJqtiObject(assessmentItem, out);
			} catch(Exception e) {
				log.error("", e);
			}
		}
	}
	
	public interface HandlerProvider {
		
		public DefaultHandler2 create(XMLStreamWriter xtw);
		
	}
}