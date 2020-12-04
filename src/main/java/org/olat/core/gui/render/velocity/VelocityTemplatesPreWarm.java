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
package org.olat.core.gui.render.velocity;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.olat.core.configuration.PreWarm;
import org.olat.core.gui.render.StringOutput;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.WebappHelper;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 09.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class VelocityTemplatesPreWarm implements PreWarm {
	private static final Logger log = Tracing.createLoggerFor(VelocityTemplatesPreWarm.class);

	@Override
	public void run() {
		long start = System.nanoTime();
		log.info("Start filling the velocity template cache");
		
		final VelocityContext context = new VelocityContext();
		final AtomicInteger numOfTemplates = new AtomicInteger(0);
		final File root = new File(WebappHelper.getContextRoot(), "WEB-INF/classes");
		final Path fPath = root.toPath();
		try {
			if(fPath.toFile().exists()) {
				Files.walkFileTree(fPath, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
						String path = fPath.relativize(file).toString();
						if(path.contains("/_i18n/")) {
							return FileVisitResult.SKIP_SUBTREE;
						}
						if(path.endsWith(".html") && path.contains("/_content/")) {
							try(StringOutput writer = new StringOutput()) {
								VelocityHelper.getInstance().mergeContent(path, context, writer, null);
								numOfTemplates.incrementAndGet();
							} catch (IOException | ResourceNotFoundException | ParseErrorException e) {
								log.error("", e);
							}
						}
						return FileVisitResult.CONTINUE;
					}
				});
			}
		} catch (IOException e) {
			log.error("", e);
		}
		log.info("Velocity cache filled with {} templates in (ms): {}", numOfTemplates, CodeHelper.nanoToMilliTime(start));
	}

}
