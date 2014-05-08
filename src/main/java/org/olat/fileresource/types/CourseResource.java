package org.olat.fileresource.types;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryEntryImportExport.RepositoryEntryImport;

public class CourseResource extends FileResource {
	
	private static final OLog log = Tracing.createLoggerFor(CourseResource.class);
	
	public static final String TYPE_NAME = "CourseModule";
	public static final String EDITOR_XML = "editortreemodel.xml";
	
	public CourseResource() {
		setTypeName(TYPE_NAME);
	}
	
	public static ResourceEvaluation evaluate(File file, String filename) {
		ResourceEvaluation eval = new ResourceEvaluation();
		try {
			IndexFileFilter visitor = new IndexFileFilter();
			Path fPath = visit(file, filename, visitor);
			
			if(visitor.isValid()) {
				Path repoXml = fPath.resolve("export/repo.xml");
				if(repoXml != null) {
					eval.setValid(true);
					
					RepositoryEntryImport re = RepositoryEntryImportExport.getConfiguration(repoXml);
					if(re != null) {
						eval.setDisplayname(re.getDisplayname());
						eval.setDescription(re.getDescription());
					}
				}
			}
			eval.setValid(visitor.isValid());
		} catch (IOException e) {
			log.error("", e);
		}
		return eval;
	}
	
	private static class IndexFileFilter extends SimpleFileVisitor<Path> {
		private boolean editorFile;

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
		throws IOException {

			String filename = file.getFileName().toString();
			if(EDITOR_XML.equals(filename)) {
				editorFile = true;
			}
			return editorFile ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
		}
		
		public boolean isValid() {
			return editorFile;
		}
	}

}
