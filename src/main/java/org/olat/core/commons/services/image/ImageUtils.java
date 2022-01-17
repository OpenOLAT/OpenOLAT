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

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;

/**
 * 
 * Initial date: 04.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImageUtils {
	
	private static final Logger log = Tracing.createLoggerFor(ImageUtils.class);
	
	public static Size getImageSize(File image) {
		try(InputStream in = new FileInputStream(image);
				BufferedInputStream bis = new BufferedInputStream(in, FileUtils.BSIZE)) {
			String suffix = FileUtils.getFileSuffix(image.getName());
			return getImageSize(suffix, bis);
		} catch (IOException e) {
			return null;
		}
	}
	
	public static Size getImageSize(String suffix, InputStream in) {
		Size result = null;

		try(ImageInputStream stream = new MemoryCacheImageInputStream(in)) {
			Iterator<ImageReader> iter = ImageIO.getImageReaders(stream);
			if (iter.hasNext()) {
				result = getImageSize(iter.next(), stream);
			} else {
				log.error("No reader found for given format: {}", suffix);
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return result;
	}
	
	private static Size getImageSize(ImageReader reader, ImageInputStream stream) {
		try {
			reader.setInput(stream);
			
			int imageIndex = reader.getMinIndex();
			int width = reader.getWidth(imageIndex);
			int height = reader.getHeight(imageIndex);
			return new Size(width, height, 0, 0, false);
		} catch (IOException e) {
			log.error(e.getMessage());
			return null;
		} finally {
			reader.dispose();
		}
	}

	public static boolean hasAlphaChannel(BufferedImage image){
		return image.getColorModel().hasAlpha();
	}

	// https://stackoverflow.com/questions/61671195/java-imageio-check-if-a-png-with-alpha-is-opaque
	public static boolean hasTransparency(BufferedImage image){
		for (int i = 0; i < image.getHeight(); i++) {
			for (int j = 0; j < image.getWidth(); j++) {
				if (isTransparent(image, j, i)){
					return true;
				}
			}
		}
		return false;
	}

	private static boolean isTransparent(BufferedImage image, int x, int y) {
		int pixel = image.getRGB(x,y);
		return (pixel>>24) == 0x00;
	}
	
}
