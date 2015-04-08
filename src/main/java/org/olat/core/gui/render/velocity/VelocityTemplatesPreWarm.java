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
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.WebappHelper;
import org.springframework.stereotype.Service;

/**
 * 
 * @author srosse
 *
 */
@Service
public class VelocityTemplatesPreWarm implements PreWarm {
	private static final OLog log = Tracing.createLoggerFor(VelocityTemplatesPreWarm.class);

	@Override
	public void run() {
		long start = System.nanoTime();
		log.info("Start filling the velocity template cache");
		
		final VelocityContext context = new VelocityContext();
		final AtomicInteger numOfTemplates = new AtomicInteger(0);
		final File root = new File(WebappHelper.getContextRoot(), "WEB-INF/classes");
		final Path fPath = root.toPath();
		try {
			Files.walkFileTree(fPath, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					try {
						
						String path = fPath.relativize(file).toString();
						if(path.endsWith(".html") && path.contains("/_content/")) {
							StringOutput writer = new StringOutput();
							VelocityHelper.getInstance().mergeContent(path, context, writer, null);
							
							numOfTemplates.incrementAndGet();
						}
					} catch (ResourceNotFoundException e) {
						e.printStackTrace();
					} catch (ParseErrorException e) {
						e.printStackTrace();
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.info("Velocity cache filled with " + numOfTemplates + " templates in (ms): " + CodeHelper.nanoToMilliTime(start));
	}

}
