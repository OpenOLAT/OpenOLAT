/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.core.util;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.olat.core.util.vfs.VFSLeaf;

// FIXME:as:c google for deployment of servers with no X installed (fj)
// see also
// http://java.sun.com/j2se/1.5.0/docs/guide/awt/AWTChanges.html#headless
/**
 * Helper class which scale an image and saved it as jpeg. Input format are
 * the ones supported by standard java: gif, jpg, png.
 * 
 * @author Alexander Schneider, srosse
 */
public class ImageHelper {
	
	private static final String OUTPUT_FORMAT = "jpeg";
	
	/**
	 * scales *.gif, *.jpg, *.png
	 * 
	 * @param image the image to scale
	 * @param image extension if not available in file name (optional)
	 * @param scaledImage the new scaled image
	 * @param maxSize the maximum size (height or width) of the new scaled image
	 * @return boolean
	 */
	public static boolean scaleImage(File image, File scaledImage, int maxSize) {
		return scaleImage(image, null, scaledImage, maxSize, maxSize);
	}
	
	/**
	 * scales *.gif, *.jpg, *.png
	 * 
	 * @param image the image to scale
	 * @param image extension if not available in file name (optional)
	 * @param scaledImage the new scaled image
	 * @param maxSize the maximum size (height or width) of the new scaled image
	 * @return boolean
	 */
	public static boolean scaleImage(File image, String imageExt, File scaledImage, int maxSize) {
		return scaleImage(image, imageExt, scaledImage, maxSize, maxSize);
	}

	/**
	 * @param image the image to scale
	 * @param scaledImaged the new scaled image
	 * @param maxSize the maximum size (height or width) of the new scaled image
	 * @return
	 */
	public static boolean scaleImage(InputStream image, VFSLeaf scaledImage, int maxSize) {
		try {
			OutputStream bos = new BufferedOutputStream(scaledImage.getOutputStream(false));
			boolean result = scaleImage(image, bos, maxSize, maxSize, getImageFormat(scaledImage));
			FileUtils.closeSafely(image);
			FileUtils.closeSafely(bos);
			return result;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * @param image the image to scale
	 * @param scaledImaged the new scaled image
	 * @param maxSize the maximum size (height or width) of the new scaled image
	 * @return
	 */
	public static Size scaleImage(InputStream image, VFSLeaf scaledImage, int maxWidth, int maxHeight) {
		OutputStream bos = new BufferedOutputStream(scaledImage.getOutputStream(false));
		try {
			BufferedImage imageSrc = ImageIO.read(image);
			if (imageSrc == null) {
				// happens with faulty Java implementation, e.g. on MacOSX Java 10, or
				// unsupported image format
				return null;				
			}
			Size scaledSize = calcScaledSize(imageSrc, maxWidth, maxHeight);
			if(writeTo(scaleTo(imageSrc, scaledSize), bos, scaledSize, getImageFormat(scaledImage))) {
				return scaledSize;
			}
			return null;
		} catch (IOException e) {
			return null;
		} finally {
			FileUtils.closeSafely(image);
			FileUtils.closeSafely(bos);
		}
	}
	
	/**
	 * @param image the image to scale
	 * @param scaledImaged the new scaled image
	 * @param maxSize the maximum size (height or width) of the new scaled image
	 * @return
	 */
	public static Size scaleImage(VFSLeaf image, VFSLeaf scaledImage, int maxWidth, int maxHeight) {
		OutputStream bos = new BufferedOutputStream(scaledImage.getOutputStream(false));
		InputStream ins = image.getInputStream();
		try {
			BufferedImage imageSrc = ImageIO.read(ins);
			if (imageSrc == null) {
				// happens with faulty Java implementation, e.g. on MacOSX Java 10, or
				// unsupported image format
				return null;				
			}
			Size scaledSize = calcScaledSize(imageSrc, maxWidth, maxHeight);
			if(!scaledSize.isChanged() && isSameFormat(image, scaledImage)) {
				InputStream cloneIns = image.getInputStream();
				FileUtils.copy(cloneIns, bos);
				FileUtils.closeSafely(cloneIns);
				return scaledSize;
			} else if(writeTo(scaleTo(imageSrc, scaledSize), bos, scaledSize, getImageFormat(scaledImage))) {
				return scaledSize;
			}
			return null;
		} catch (IOException e) {
			return null;
		} finally {
			FileUtils.closeSafely(ins);
			FileUtils.closeSafely(bos);
		}
	}
	
	public static Size scaleImage(BufferedImage image, VFSLeaf scaledImage, int maxWidth, int maxHeight) {
		OutputStream bos = new BufferedOutputStream(scaledImage.getOutputStream(false));
		try {
			if (image == null) {
				// happens with faulty Java implementation, e.g. on MacOSX Java 10, or
				// unsupported image format
				return null;				
			}
			Size scaledSize = calcScaledSize(image, maxWidth, maxHeight);
			if(writeTo(scaleTo(image, scaledSize), bos, scaledSize, getImageFormat(scaledImage))) {
				return scaledSize;
			}
			return null;
		} catch (Exception e) {
			return null;
		} finally {
			FileUtils.closeSafely(bos);
		}
	}
	
	/**
	 * @param image the image to scale
	 * @param scaledImaged the new scaled image
	 * @param maxWidth the maximum width of the new scaled image
	 * @param maxheight the maximum height of the new scaled image
	 * @return
	 */
	public static boolean scaleImage(File image, File scaledImage, int maxWidth, int maxHeight) {
		return scaleImage(image, null, scaledImage, maxWidth, maxHeight);
	}
	
	/**
	 * @param image the image to scale
	 * @param image extension if not given by the image file (optional)
	 * @param scaledImaged the new scaled image
	 * @param maxWidth the maximum width of the new scaled image
	 * @param maxheight the maximum height of the new scaled image
	 * @return
	 */
	public static boolean scaleImage(File image, String imageExt, File scaledImage, int maxWidth, int maxHeight) {
		try {
			BufferedImage imageSrc = ImageIO.read(image);
			if (imageSrc == null) {
				// happens with faulty Java implementation, e.g. on MacOSX Java 10, or
				// unsupported image format
				return false;				
			}
			Size scaledSize = calcScaledSize(imageSrc, maxWidth, maxHeight);
			if(!scaledSize.isChanged() && isSameFormat(image, imageExt, scaledImage)) {
				return FileUtils.copyFileToFile(image, scaledImage, false);
			}
			return writeTo(scaleTo(imageSrc, scaledSize), scaledImage, scaledSize, getImageFormat(scaledImage));
		} catch (IOException e) {
			return false;
		}
	}
	
	private static String getImageFormat(File image) {
		String extension = FileUtils.getFileSuffix(image.getName());
		if(StringHelper.containsNonWhitespace(extension)) {
			return extension.toLowerCase();
		}
		return OUTPUT_FORMAT;
	}
	
	private static String getImageFormat(VFSLeaf image) {
		String extension = FileUtils.getFileSuffix(image.getName());
		if(StringHelper.containsNonWhitespace(extension)) {
			return extension.toLowerCase();
		}
		return OUTPUT_FORMAT;
	}
	
	private static boolean isSameFormat(VFSLeaf source, VFSLeaf scaled) {
		String sourceExt = FileUtils.getFileSuffix(source.getName());
		String scaledExt = getImageFormat(scaled);
		if(sourceExt != null && sourceExt.equals(scaledExt)) {
			return true;
		}
		return false;
	}
	
	private static boolean isSameFormat(File source, String sourceExt, File scaled) {
		if(!StringHelper.containsNonWhitespace(sourceExt)) {
			sourceExt = FileUtils.getFileSuffix(source.getName());
		}
		String scaledExt = getImageFormat(scaled);
		if(sourceExt != null && sourceExt.equals(scaledExt)) {
			return true;
		}
		return false;
	}
	
	/**
	 * @param image the image to scale
	 * @param scaledImaged the new scaled image
	 * @param maxWidth the maximum width of the new scaled image
	 * @param maxheight the maximum height of the new scaled image
	 * @return
	 */
	public static boolean scaleImage(InputStream image, OutputStream scaledImage, int maxWidth, int maxHeight, String outputFormat) {
		try {
			BufferedImage imageSrc = ImageIO.read(image);
			if (imageSrc == null) {
				// happens with faulty Java implementation, e.g. on MacOSX Java 10, or
				// unsupported image format
				return false;				
			}
			Size scaledSize = calcScaledSize(imageSrc, maxWidth, maxHeight);
			return writeTo(scaleTo(imageSrc, scaledSize), scaledImage, scaledSize, outputFormat);
		} catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * Calculate the size of the new image. The method keep the ratio and doesn't
	 * scale up the image.
	 * @param image the image to scale
	 * @param maxWidth the maximum width of the new scaled image
	 * @param maxheight the maximum height of the new scaled image
	 * @return
	 */
	private static Size calcScaledSize(BufferedImage image, int maxWidth, int maxHeight) {
		int width = image.getWidth();
		int height = image.getHeight();
		
		if(maxHeight > height && maxWidth > width) {
			return new Size(width, height, false);
    }

		double thumbRatio = (double)maxWidth / (double)maxHeight;
    double imageRatio = (double)width / (double)height;
    if (thumbRatio < imageRatio) {
      maxHeight = (int)(maxWidth / imageRatio);
    }
    else {
      maxWidth = (int)(maxHeight * imageRatio);
    }

		return new Size(maxWidth, maxHeight, true);
	}
	
	/**
	 * Can change this to choose a better compression level as the default
	 * @param image
	 * @param scaledImage
	 * @return
	 */
	private static boolean writeTo(BufferedImage image, File scaledImage, Size scaledSize, String outputFormat) {
		try {
			if(!StringHelper.containsNonWhitespace(outputFormat)) {
				outputFormat = OUTPUT_FORMAT;
			}

			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(outputFormat);
			if(writers.hasNext()) {
				ImageWriter writer = writers.next();
				ImageWriteParam iwp = getOptimizedImageWriteParam(writer, scaledSize);
				IIOImage iiOImage = new IIOImage(image, null, null);
				ImageOutputStream iOut = new FileImageOutputStream(scaledImage);
				writer.setOutput(iOut);
				writer.write(null, iiOImage, iwp);
				writer.dispose();
				iOut.flush();
				iOut.close();
				return true;
			} else {
				return ImageIO.write(image, outputFormat, scaledImage);
			}
		} catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * Can change this to choose a better compression level as the default
	 * @param image
	 * @param scaledImage
	 * @return
	 */
	private static boolean writeTo(BufferedImage image, OutputStream scaledImage, Size scaledSize, String outputFormat) {
		try {
			if(!StringHelper.containsNonWhitespace(outputFormat)) {
				outputFormat = OUTPUT_FORMAT;
			}

			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(outputFormat);
			if(writers.hasNext()) {
				ImageWriter writer = writers.next();
				ImageWriteParam iwp = getOptimizedImageWriteParam(writer, scaledSize);
				IIOImage iiOImage = new IIOImage(image, null, null);
				ImageOutputStream iOut = new MemoryCacheImageOutputStream(scaledImage);
				writer.setOutput(iOut);
				writer.write(null, iiOImage, iwp);
				writer.dispose();
				iOut.flush();
				iOut.close();
				return true;
			} else {
				return ImageIO.write(image, outputFormat, scaledImage);
			}
		} catch (IOException e) {
			return false;
		}
	}
	
	private static ImageWriteParam getOptimizedImageWriteParam(ImageWriter writer, Size scaledSize) {
		ImageWriteParam iwp = writer.getDefaultWriteParam();
		try {
			if(iwp.canWriteCompressed()) {
				iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				int maxSize = Math.max(scaledSize.getWidth(), scaledSize.getHeight());
				if(maxSize <= 50) {
					iwp.setCompressionQuality(0.95f);
				} else if (maxSize <= 100) {
					iwp.setCompressionQuality(0.90f);
				} else if (maxSize <= 200) {
					iwp.setCompressionQuality(0.85f);
				} else if (maxSize <= 500) {
					iwp.setCompressionQuality(0.80f);
				} else {
					iwp.setCompressionQuality(0.75f);
				}
			}
		} catch (Exception e) {
			//bmp can be compressed but don't allow it!!!
			return writer.getDefaultWriteParam();
		}
		return iwp;
	}
	
	private static BufferedImage scaleTo(BufferedImage image, Size scaledSize) {
		if(!scaledSize.isChanged()) return image;
		return scaleFastTo(image, scaledSize);
	}
	
	public static BufferedImage scaleSmoothTo(BufferedImage image, Size scaledSize) {
		Image imageSrc = image.getScaledInstance(scaledSize.getWidth(), scaledSize.getHeight(), Image.SCALE_SMOOTH);

		BufferedImage result;
		if (image.getType() == BufferedImage.TYPE_CUSTOM) {
			result = new BufferedImage(scaledSize.getWidth(), scaledSize.getHeight(), BufferedImage.TYPE_INT_ARGB);
		} else {
			result = new BufferedImage(scaledSize.getWidth(), scaledSize.getHeight(), image.getType());
		}
		
		Graphics2D g = result.createGraphics();
    if(image.getTransparency() != Transparency.OPAQUE) {
    	g.setComposite(AlphaComposite.Src);
    }
    
    g.drawImage(imageSrc, 0, 0, scaledSize.getWidth(), scaledSize.getHeight(), null);
    g.dispose();
    return result;
	}
	
	/**
	 * This code is very inspired on Chris Campbells article "The Perils of Image.getScaledInstance()"
	 *
	 * The article can be found here:
	 * http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html
	 *
	 * Note that the filter method is threadsafe
	 */
	public static BufferedImage scaleFastTo(BufferedImage img, Size scaledSize) {
		if(!scaledSize.isChanged()) return img;
		
		BufferedImage dest;
		if (img.getType() == BufferedImage.TYPE_CUSTOM) {
			dest = new BufferedImage(scaledSize.getWidth(), scaledSize.getHeight(), BufferedImage.TYPE_INT_ARGB);
		} else {
			dest = new BufferedImage(scaledSize.getWidth(), scaledSize.getHeight(), img.getType());
		}
		
		int dstWidth = scaledSize.getWidth();
		int dstHeight = scaledSize.getHeight();
		
    BufferedImage ret = img;
    int w, h;

    // Use multi-step technique: start with original size, then
    // scale down in multiple passes with drawImage()
    // until the target size is reached
    w = img.getWidth();
    h = img.getHeight();

    do {
    	if (w > dstWidth) {
    		w /= 2;
        if (w < dstWidth) {
            w = dstWidth;
        }
    	} else {
        w = dstWidth;
    	}

    	if (h > dstHeight) {
        h /= 2;
        if (h < dstHeight) {
            h = dstHeight;
        }
    	} else {
        h = dstHeight;
    	}

    	BufferedImage tmp;
    	if (dest.getWidth() == w && dest.getHeight() == h && w == dstWidth && h == dstHeight){
    		tmp = dest;
    	} else {
    		tmp = new BufferedImage(w, h, dest.getType());
    	}
    
    	Graphics2D g2 = tmp.createGraphics();
    	g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    	g2.drawImage(ret, 0, 0, w, h, null);
    	g2.dispose();

    	ret = tmp;
    } while (w != dstWidth || h != dstHeight);

    return ret;
	}
	
	public static final class Size {
		private final int width;
		private final int height;
		private final boolean changed;
		
		public Size(int width, int height, boolean changed) {
			this.width = width;
			this.height = height;
			this.changed = changed;
		}

		public int getWidth() {
			if(width <= 0) {
				return 1;
			}
			return width;
		}

		public int getHeight() {
			if(height <= 0) {
				return 1;
			}
			return height;
		}
		
		public boolean isChanged() {
			return changed;
		}
	}
}