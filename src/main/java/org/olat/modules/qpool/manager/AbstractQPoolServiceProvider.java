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
package org.olat.modules.qpool.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.qpool.ExportFormatOptions;
import org.olat.modules.qpool.QPoolSPI;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.model.DefaultExportFormat;
import org.olat.modules.qpool.model.QItemType;
import org.olat.search.model.AbstractOlatDocument;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.file.DocumentAccessException;
import org.olat.search.service.document.file.FileDocumentFactory;

/**
 * 
 * Initial date: 07.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractQPoolServiceProvider implements QPoolSPI {
	
	private static final OLog log = Tracing.createLoggerFor(AbstractQPoolServiceProvider.class);
	
	public abstract QPoolFileStorage getFileStorage();
	
	public abstract QItemType getDefaultType();
	
	private static final List<ExportFormatOptions> exportFormats
		= Collections.singletonList(DefaultExportFormat.ZIP_EXPORT_FORMAT); 

	@Override
	public boolean isConversionPossible(QuestionItemShort question) {
		return false;
	}
	
	@Override
	public QuestionItem convert(Identity owner, QuestionItemShort question, Locale locale) {
		return null;
	}

	@Override
	public List<ExportFormatOptions> getTestExportFormats() {
		return exportFormats;
	}

	@Override
	public String extractTextContent(QuestionItemFull item) {
		String directory = item.getDirectory();
		VFSContainer itemDir = getFileStorage().getContainer(directory);
		VFSItem file = itemDir.resolve(item.getRootFilename());
		if(file instanceof VFSLeaf) {
			FileDocumentFactory docFactory = CoreSpringFactory.getImpl(FileDocumentFactory.class);
			SearchResourceContext ctxt = new SearchResourceContext();
			ctxt.setBusinessControlFor(item);
			try {
				String content = null;
				Document doc = docFactory.createDocument(ctxt, (VFSLeaf)file);
				for(IndexableField field:doc.getFields()) {
					if(AbstractOlatDocument.CONTENT_FIELD_NAME.equals(field.name())) {
						content = field.stringValue();
					}
				}
				return content;
			} catch (IOException e) {
				log.error("", e);
			} catch (DocumentAccessException e) {
				log.warn("", e);
			}
		}
		return null;
	}

	@Override
	public List<QuestionItem> importItems(Identity owner, Locale defaultLocale, String filename, File file) {
		List<QuestionItem> items = new ArrayList<>();
		QuestionItem item = importItem(owner, defaultLocale, filename, file);
		if(item != null) {
			items.add(item);
		}
		return items;
	}

	public QuestionItem importItem(Identity owner, Locale defaultLocale, String filename, File file) {
		String dir = getFileStorage().generateDir();
		VFSContainer itemDir = getFileStorage().getContainer(dir);

		VFSLeaf leaf = itemDir.createChildLeaf(filename);
		try(OutputStream out = leaf.getOutputStream(false);
				InputStream in = new FileInputStream(file)) {
			IOUtils.copy(in, out);
		} catch (IOException e) {
			log.error("", e);
		}
		
		String language = defaultLocale.getLanguage();
		QItemType type = getDefaultType();
		return CoreSpringFactory.getImpl(QPoolService.class)
				.createAndPersistItem(owner, filename, getFormat(), language, null, dir, filename, type);
	}

	@Override
	public MediaResource exportTest(List<QuestionItemShort> items, ExportFormatOptions format, Locale locale) {
		return null;//Zip are made by qpool service
	}

	@Override
	public void exportItem(QuestionItemFull item, ZipOutputStream zout, Locale locale, Set<String> names) {
		String directory = item.getDirectory();
		VFSContainer itemDir = getFileStorage().getContainer(directory);
		VFSItem file = itemDir.resolve(item.getRootFilename());
		if(file instanceof VFSLeaf) {
			exportFile((VFSLeaf)file, zout);
		}
	}
	
	private void exportFile(VFSLeaf leaf, ZipOutputStream zout) {
		try(InputStream in = leaf.getInputStream()) {
			if(in != null) {
				zout.putNextEntry(new ZipEntry(leaf.getName()));
				IOUtils.copy(in, zout);
				zout.closeEntry();
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}

	@Override
	public void copyItem(QuestionItemFull original, QuestionItemFull copy) {
		VFSContainer originalDir = getFileStorage().getContainer(original.getDirectory());
		VFSContainer copyDir = getFileStorage().getContainer(copy.getDirectory());
		VFSManager.copyContent(originalDir, copyDir);
	}
}
