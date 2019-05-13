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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.ims.qti21.model.xml.QTI21ExplorerHandler;
import org.olat.ims.qti21.model.xml.QTI21Infos;

/**
 * 
 * Explore a file system to find an "imsmanifest.xml", parse it and
 * extract some informations.
 * 
 * Initial date: 2 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21IMSManifestExplorerVisitor extends SimpleFileVisitor<Path> {
	
	private static final Logger log = Tracing.createLoggerFor(QTI21IMSManifestExplorerVisitor.class);
	
	private QTI21ExplorerHandler handler;
	
	public QTI21Infos getInfos() {
		return handler == null ? new QTI21Infos() : handler.getInfos();
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		String filename = file.getFileName().toString();
		if(filename.equals("imsmanifest.xml")) {
			 handler = scanFile(file);
		}
		return handler == null ? FileVisitResult.CONTINUE : FileVisitResult.TERMINATE;
	}
	
	private QTI21ExplorerHandler scanFile(Path inputFile) {
		QTI21ExplorerHandler infosHandler = new QTI21ExplorerHandler();
		try(InputStream in = Files.newInputStream(inputFile)) {
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			saxParser.setProperty("http://xml.org/sax/properties/lexical-handler", infosHandler);
			saxParser.parse(in, infosHandler);
		} catch(Exception e1) {
			log.error("", e1);
		}
		return infosHandler;
	}
}
