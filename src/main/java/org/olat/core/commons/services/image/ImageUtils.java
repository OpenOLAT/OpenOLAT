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
package org.olat.core.commons.services.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.apache.poi.util.IOUtils;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;

/**
 * 
 * Initial date: 04.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImageUtils {
	
	private static final OLog log = Tracing.createLoggerFor(ImageUtils.class);
	
	
	public static Size getImageSize(File image) {
		InputStream in = null;
		try {
			String suffix = FileUtils.getFileSuffix(image.getName());
			in = new FileInputStream(image);
			return getImageSize(suffix, in);
		} catch (FileNotFoundException e) {
			IOUtils.closeQuietly(in);
			return null;
		}
	}
	
	public static Size getImageSize(String suffix, InputStream in) {
		Size result = null;

		Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
		if (iter.hasNext()) {
			ImageReader reader = iter.next();
			try {
				ImageInputStream stream = new MemoryCacheImageInputStream(in);
				reader.setInput(stream);
				int width = reader.getWidth(reader.getMinIndex());
				int height = reader.getHeight(reader.getMinIndex());
				result = new Size(width, height, 0, 0, false);
			} catch (IOException e) {
				log.error(e.getMessage());
			} finally {
				IOUtils.closeQuietly(in);
				reader.dispose();
			}
		} else {
			log.error("No reader found for given format: " + suffix);
		}
		return result;
	}
}
