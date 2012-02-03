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
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.util;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.CMMException;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.apache.commons.io.IOUtils;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.LocalFileImpl;
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
public class ImageHelper implements IImageHelper {
	
	private static final OLog log = Tracing.createLoggerFor(ImageHelper.class);
	
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
		String imageExt = FileUtils.getFileSuffix(image.getName());
		return scaleImage(image, imageExt, scaledImage, maxSize, maxSize);
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
	@Override
	public Size scaleImage(File image, String imageExt, VFSLeaf scaledImage, int maxWidth, int maxHeight) {
		
		ImageInputStream imageIns = null;
		OutputStream bos = new BufferedOutputStream(scaledImage.getOutputStream(false));
		try {
			imageIns = new FileImageInputStream(image);
			SizeAndBufferedImage scaledSize = calcScaledSize(imageIns, imageExt, maxWidth, maxHeight);
			if(scaledSize == null) {
				return null;
			}
			if(!scaledSize.getScaledSize().isChanged() && isSameFormat(image, scaledImage)) {
				InputStream cloneIns = new FileInputStream(image);
				IOUtils.copy(cloneIns, bos);
				IOUtils.closeQuietly(cloneIns);
				return scaledSize.getScaledSize();
			} else {
				BufferedImage imageSrc = scaledSize.getImage();
				if (imageSrc == null) {
					// happens with faulty Java implementation, e.g. on MacOSX Java 10, or
					// unsupported image format
					return null;				
				}
				BufferedImage scaledBufferedImage = scaleTo(imageSrc, scaledSize.getScaledSize());
				if(writeTo(scaledBufferedImage, bos, scaledSize.getScaledSize(), getImageFormat(scaledImage))) {
					return scaledSize.getScaledSize();
				}
				return null;
			}
		} catch (IOException e) {
			return null;
		} finally {
			closeQuietly(imageIns);
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
		OutputStream bos = null;
		ImageInputStream ins = null;
		try {
			ins = getInputStream(image);
			String extension = FileUtils.getFileSuffix(image.getName());
			SizeAndBufferedImage scaledSize = calcScaledSize(ins, extension, maxWidth, maxHeight);
			if(scaledSize == null || scaledSize.getImage() == null) {
				return null;
			}
			
			ins = getInputStream(image);
			bos = new BufferedOutputStream(scaledImage.getOutputStream(false));
			if(!scaledSize.getScaledSize().isChanged() && isSameFormat(image, scaledImage)) {
				InputStream cloneIns = image.getInputStream();
				IOUtils.copy(cloneIns, bos);
				IOUtils.closeQuietly(cloneIns);
				return scaledSize.getScaledSize();
			} else {
				BufferedImage imageSrc = scaledSize.getImage();
				BufferedImage scaledSrc = scaleTo(imageSrc, scaledSize.getScaledSize());
				boolean scaled = writeTo(scaledSrc, bos, scaledSize.getScaledSize(), getImageFormat(scaledImage));
				if(scaled) {
					return scaledSize.getScaledSize();
				}
				return null;
			}
		} catch (IOException e) {
			return null;
		//fxdiff FXOLAT-109: prevent red screen if the image has wrong EXIF data
		} catch (CMMException e) {
			return null;
		} finally {
			closeQuietly(ins);
			FileUtils.closeSafely(bos);
		}
	}
	
	/**
	 * 
	 * @param leaf
	 * @return
	 */
	private static ImageInputStream getInputStream(VFSLeaf leaf)
	throws IOException {
		if(leaf instanceof LocalFileImpl) {
			LocalFileImpl file = (LocalFileImpl)leaf;
			return new FileImageInputStream(file.getBasefile());
		}
		return new MemoryCacheImageInputStream(leaf.getInputStream());
	}
	
	public static Size scaleImage(BufferedImage image, VFSLeaf scaledImage, int maxWidth, int maxHeight) {
		OutputStream bos = null;
		try {
			if (image == null) {
				// happens with faulty Java implementation, e.g. on MacOSX Java 10, or
				// unsupported image format
				return null;				
			}
			bos = new BufferedOutputStream(scaledImage.getOutputStream(false));
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
		ImageInputStream imageSrc = null;
		try {
			imageSrc = new FileImageInputStream(image);
			SizeAndBufferedImage scaledSize = calcScaledSize(imageSrc, imageExt, maxWidth, maxHeight);
			if(scaledSize == null || scaledSize.image == null) {
				return false;
			}
			if(!scaledSize.getScaledSize().isChanged() && isSameFormat(image, imageExt, scaledImage)) {
				return FileUtils.copyFileToFile(image, scaledImage, false);
			}
			BufferedImage bufferedImage = scaledSize.image;
			BufferedImage scaledBufferedImage = scaleTo(bufferedImage, scaledSize.getScaledSize());
			return writeTo(scaledBufferedImage, scaledImage, scaledSize.getScaledSize(), getImageFormat(scaledImage));
		} catch (IOException e) {
			return false;
		//fxdiff FXOLAT-109: prevent red screen if the image has wrong EXIF data
		} catch (CMMException e) {
			return false;
		} finally {
			closeQuietly(imageSrc);
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
	
	private static boolean isSameFormat(File source, VFSLeaf scaled) {
		String sourceExt = FileUtils.getFileSuffix(source.getName());
		String scaledExt = getImageFormat(scaled);
		if(sourceExt != null && sourceExt.equals(scaledExt)) {
			return true;
		}
		return false;
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
		return computeScaledSize(width, height, maxWidth, maxHeight);
	}
	

	
	private static SizeAndBufferedImage calcScaledSize(ImageInputStream stream, String suffix, int maxWidth, int maxHeight) {
    Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
    if (iter.hasNext()) {
        ImageReader reader = iter.next();
        try {
            reader.setInput(stream);
            int width = reader.getWidth(reader.getMinIndex());
            int height = reader.getHeight(reader.getMinIndex());
            Size size = new Size(width, height, false);
            Size scaledSize = computeScaledSize(width, height, maxWidth, maxHeight);
            SizeAndBufferedImage  all = new SizeAndBufferedImage(size, scaledSize);
            
            double memoryKB = (width * height * 4) / 1024d;
            if(memoryKB > 2000) {//check limit at 20MB
            	double free = Runtime.getRuntime().freeMemory() / 1024d;
            	if(free > memoryKB) {
                all.setImage(reader.read(reader.getMinIndex()));
            	} else {
            		//make sub sampling to save memory
            		int ratio = (int)Math.round(Math.sqrt(memoryKB / free));
            		ImageReadParam param = reader.getDefaultReadParam();
            		param.setSourceSubsampling(ratio, ratio, 0, 0);
                all.setImage(reader.read(reader.getMinIndex(), param));
            	}
            } else {
            	all.setImage(reader.read(reader.getMinIndex()));
            }
            return all;
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            reader.dispose();
        }
    } else {
        log.error("No reader found for given format: " + suffix, null);
    }
    return null;
	}
	
	private static Size computeScaledSize(int width, int height, int maxWidth, int maxHeight) {
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
	
	private final static void closeQuietly(ImageInputStream ins) {
		if(ins != null) {
			try {
				ins.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static final class SizeAndBufferedImage {
		private Size size;
		private Size scaledSize;
		private BufferedImage image;
		
		public SizeAndBufferedImage(Size size, Size scaledSize) {
			this.size = size;
			this.scaledSize = scaledSize;
		}
		
		public Size getSize() {
			return size;
		}
		
		public Size getScaledSize() {
			return scaledSize;
		}

		public BufferedImage getImage() {
			return image;
		}

		public void setImage(BufferedImage image) {
			this.image = image;
		}
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