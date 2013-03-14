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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.qpool.QPoolSPI;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.QPoolService;

/**
 * 
 * Initial date: 07.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractQPoolServiceProvider implements QPoolSPI {
	
	private static final OLog log = Tracing.createLoggerFor(AbstractQPoolServiceProvider.class);
	
	public abstract FileStorage getFileStorage();

	@Override
	public List<QuestionItem> importItems(Identity owner, String filename, File file) {
		List<QuestionItem> items = new ArrayList<QuestionItem>();
		QuestionItem item = importItem(owner, filename, file);
		if(item != null) {
			items.add(item);
		}
		return items;
	}

	public QuestionItem importItem(Identity owner, String filename, File file) {
		String dir = getFileStorage().generateDir();
		VFSContainer itemDir = getFileStorage().getContainer(dir);

		VFSLeaf leaf = itemDir.createChildLeaf(filename);
		OutputStream out = leaf.getOutputStream(false);
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			IOUtils.copy(in, out);
		} catch (FileNotFoundException e) {
			log.error("", e);
		} catch (IOException e) {
			log.error("", e);
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
		return CoreSpringFactory.getImpl(QPoolService.class)
				.createAndPersistItem(owner, filename, getFormat(), "de", null, dir, filename, null);
	}

	@Override
	public void exportItem(QuestionItemFull item, ZipOutputStream zout) {
		String directory = item.getDirectory();
		VFSContainer itemDir = getFileStorage().getContainer(directory);
		VFSItem file = itemDir.resolve(item.getRootFilename());
		if(file instanceof VFSLeaf) {
			exportFile((VFSLeaf)file, zout);
		}
	}
	
	private void exportFile(VFSLeaf leaf, ZipOutputStream zout) {
		InputStream in = null;
		try {
			in = leaf.getInputStream();
			if(in != null) {
				zout.putNextEntry(new ZipEntry(leaf.getName()));
				IOUtils.copy(in, zout);
				zout.closeEntry();
			}
		} catch (IOException e) {
			log.error("", e);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}
}
