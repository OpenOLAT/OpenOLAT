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

package org.olat.core.commons.services.image.spi;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.olat.core.commons.services.image.Crop;
import org.olat.core.commons.services.image.Size;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WorkThreadInformations;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;

// see also
// http://java.sun.com/j2se/1.5.0/docs/guide/awt/AWTChanges.html#headless
/**
 * Helper class which scale an image and saved it as jpeg. Input format are
 * the ones supported by standard java: gif, jpg, png.
 * 
 * @author Alexander Schneider, srosse
 */
public class ImageHelperImpl extends AbstractImageHelper {
	
	private static final Logger log = Tracing.createLoggerFor(ImageHelperImpl.class);
	
	private static final String OUTPUT_FORMAT = "jpeg";

	@Override
	public Size thumbnailPDF(VFSLeaf pdfFile, VFSLeaf thumbnailFile, int maxWidth, int maxHeight) {	
		PDDocument document = null;
		try(InputStream in = pdfFile.getInputStream()) {
			WorkThreadInformations.setInfoFiles(null, pdfFile);
			WorkThreadInformations.set("Generate thumbnail VFSLeaf=" + pdfFile);
			
			document = PDDocument.load(in);
			PDFRenderer pdfRenderer = new PDFRenderer(document);
			BufferedImage image = pdfRenderer.renderImageWithDPI(0, 72, ImageType.RGB);
			Size size = scaleImage(image, thumbnailFile, maxWidth, maxHeight);
			if(size != null) {
				return size;
			}
			return null;
		} catch(InvalidPasswordException e) {
			log.warn("Unable to decrypt. Unable to create image from pdf file.", e);
			return null;
		} catch (Exception e) {
			log.warn("Unable to create image from pdf file.", e);
			return null;
		} finally {
			WorkThreadInformations.unset();
			if (document != null) {
				try {
					document.close();
				} catch (IOException e) {
					//only a try, fail silently
				}
			}
		}
	}
	
	@Override
	public boolean cropImage(File image, File cropedImage, Crop cropSelection) {
		try (ImageInputStream imageSrc = new FileImageInputStream(image)) {
			String extension = FileUtils.getFileSuffix(cropedImage.getName());
			SizeAndBufferedImage img = getImage(imageSrc, extension, image.getName());
			if(img != null) {
				BufferedImage croppedImg = cropTo(img.getImage(), img.getSize(), cropSelection);
				Size size = new Size(cropSelection.getWidth(), cropSelection.getHeight(), false);
				return writeTo(croppedImg, cropedImage, size, extension);
			}
			return false;
		} catch (IOException e) {
			return false;
		} catch (CMMException e) {// if the image has wrong EXIF data
			return false;
		}
	}

	/**
	 * @param image the image to scale
	 * @param imageExt extension of image (jpg, png, ...)
	 * @param scaledImage the new scaled image
	 * @param maxWidth the maximum width of the new scaled image
	 * @param maxHeight the maximum width of the new scaled image
	 * @return
	 */
	@Override
	public Size scaleImage(File image, String imageExt, VFSLeaf scaledImage, int maxWidth, int maxHeight) {
		
		ImageInputStream imageIns = null;
		OutputStream bos = new BufferedOutputStream(scaledImage.getOutputStream(false));
		try {
			imageIns = new FileImageInputStream(image);
			SizeAndBufferedImage scaledSize = calcScaledSize(imageIns, imageExt, maxWidth, maxHeight, false, image.getName());
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
			closeQuietly(imageIns, image.getName());
			FileUtils.closeSafely(bos);
		}
	}
	
	/**
	 * @param image the image to scale
	 * @param scaledImage the new scaled image
	 * @param maxWidth the maximum width of the new scaled image
	 * @param maxHeight the maximum width of the new scaled image
	 * @param fill
	 * @return
	 */
	@Override
	public Size scaleImage(VFSLeaf image, VFSLeaf scaledImage, int maxWidth, int maxHeight, boolean fill) {
		OutputStream bos = null;
		ImageInputStream ins = null;
		try {
			ins = getInputStream(image);
			if(ins == null) {
				return null;
			}
			String extension = FileUtils.getFileSuffix(image.getName());
			SizeAndBufferedImage scaledSize = calcScaledSize(ins, extension, maxWidth, maxHeight, fill, image.getName());
			if(scaledSize == null || scaledSize.getImage() == null) {
				return null;
			}
			
			ins = getInputStream(image);
			if(ins == null) {
				return null;
			}
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
			closeQuietly(ins, image.getName());
			FileUtils.closeSafely(bos);
		}
	}
	
	/**
	 * 
	 * @param leaf
	 * @return
	 */
	private ImageInputStream getInputStream(VFSLeaf leaf)
	throws IOException {
		if(leaf == null) {
			return null;
		}
		if(leaf instanceof LocalFileImpl) {
			LocalFileImpl file = (LocalFileImpl)leaf;
			if(file.getBasefile() != null) {
				return new FileImageInputStream(file.getBasefile());
			}
			return null;
		}
		InputStream ins = leaf.getInputStream();
		if(ins == null) {
			return null;
		}
		return new MemoryCacheImageInputStream(leaf.getInputStream());
	}
	
	private Size scaleImage(BufferedImage image, VFSLeaf scaledImage, int maxWidth, int maxHeight) {
		try(OutputStream bos = new BufferedOutputStream(scaledImage.getOutputStream(false))) {
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
		}
	}
	
	/**
	 * @param image The image to scale
	 * @param imageExt The extension if not given by the image file (optional)
	 * @param scaledImage the new scaled image
	 * @param maxWidth the maximum width of the new scaled image
	 * @param maxHeight the maximum height of the new scaled image
	 * @return
	 */
	@Override
	public Size scaleImage(File image, String imageExt, File scaledImage, int maxWidth, int maxHeight, boolean fill) {
		ImageInputStream imageSrc = null;
		try {
			imageSrc = new FileImageInputStream(image);
			SizeAndBufferedImage scaledSize = calcScaledSize(imageSrc, imageExt, maxWidth, maxHeight, fill, image.getName());
			if(scaledSize == null || scaledSize.image == null) {
				return null;
			}
			if(!scaledSize.getScaledSize().isChanged() && isSameFormat(image, imageExt, scaledImage)) {
				if(FileUtils.copyFileToFile(image, scaledImage, false)) {
					return scaledSize.getSize();
				}
			}
			BufferedImage bufferedImage = scaledSize.image;
			BufferedImage scaledBufferedImage = scaleTo(bufferedImage, scaledSize.getScaledSize());
			if(writeTo(scaledBufferedImage, scaledImage, scaledSize.getScaledSize(), getImageFormat(scaledImage))) {
				return scaledSize.getScaledSize();
			}
			return null;
		} catch (IOException e) {
			return null;
		//fxdiff FXOLAT-109: prevent red screen if the image has wrong EXIF data
		} catch (CMMException e) {
			return null;
		} finally {
			closeQuietly(imageSrc, image.getName());
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
	 * @param maxHeight the maximum height of the new scaled image
	 * @return
	 */
	public static Size calcScaledSize(BufferedImage image, int maxWidth, int maxHeight) {
		int width = image.getWidth();
		int height = image.getHeight();
		return computeScaledSize(width, height, maxWidth, maxHeight, false);
	}
	
	private static SizeAndBufferedImage calcScaledSize(ImageInputStream stream,
			String suffix, int maxWidth, int maxHeight, boolean fill, String imageName) {
		Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
		if(iter.hasNext()) {
			ImageReader reader = iter.next();
			try {
				reader.setInput(stream, true, true);
				int width = reader.getWidth(reader.getMinIndex());
				int height = reader.getHeight(reader.getMinIndex());
				Size size = new Size(width, height, false);
				Size scaledSize = computeScaledSize(width, height, maxWidth, maxHeight, fill);
				SizeAndBufferedImage all = new SizeAndBufferedImage(size, scaledSize);
				
				int readerMinIndex = reader.getMinIndex();
				ImageReadParam param = reader.getDefaultReadParam();
                Iterator<ImageTypeSpecifier> imageTypes = reader.getImageTypes(0);
                while (imageTypes.hasNext()) {
                    try {
						ImageTypeSpecifier imageTypeSpecifier = imageTypes.next();
						int bufferedImageType = imageTypeSpecifier.getBufferedImageType();
						if (bufferedImageType == BufferedImage.TYPE_BYTE_GRAY) {
						    param.setDestinationType(imageTypeSpecifier);
						}
						
						double memoryKB = (width * height * 4) / 1024d;
						if (memoryKB > 2000) {// check limit at 20MB
							double free = Runtime.getRuntime().freeMemory() / 1024d;
							if (free > memoryKB) {
								all.setImage(reader.read(readerMinIndex, param));
							} else {
								// make sub sampling to save memory
								int ratio = (int) Math.round(Math.sqrt(memoryKB / free));
								param.setSourceSubsampling(ratio, ratio, 0, 0);
								all.setImage(reader.read(readerMinIndex, param));
							}
						} else {
							all.setImage(reader.read(readerMinIndex, param));
						}
						return all;
					} catch (IllegalArgumentException e) {
						log.warn(e.getMessage(), e);
					}
                }
			} catch (IOException e) {
				log.error("Exception when trying to read {}: {}", imageName, e.getMessage(), e);
			} finally {
				reader.dispose();
			}
		} else {
			log.error("No reader found for given format: {}", suffix);
		}
		return null;
	}
	
	private static SizeAndBufferedImage getImage(ImageInputStream stream, String suffix, String imageName) {
		Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
		if(iter.hasNext()) {
			ImageReader reader = iter.next();
			try {
				reader.setInput(stream, true, true);
				int width = reader.getWidth(reader.getMinIndex());
				int height = reader.getHeight(reader.getMinIndex());
				Size size = new Size(width, height, false);
				SizeAndBufferedImage all = new SizeAndBufferedImage(size, null);
			
				int readerMinIndex = reader.getMinIndex();
				ImageReadParam param = reader.getDefaultReadParam();
                Iterator<ImageTypeSpecifier> imageTypes = reader.getImageTypes(0);
                while (imageTypes.hasNext()) {
                    try {
						ImageTypeSpecifier imageTypeSpecifier = imageTypes.next();
						int bufferedImageType = imageTypeSpecifier.getBufferedImageType();
						if (bufferedImageType == BufferedImage.TYPE_BYTE_GRAY) {
						    param.setDestinationType(imageTypeSpecifier);
						}
						all.setImage(reader.read(readerMinIndex, param));
						return all;
					} catch (IllegalArgumentException e) {
						log.warn(e.getMessage(), e);
					}
                }
			} catch (IOException e) {
				log.error("Exception when trying to read {}: {}", imageName, e.getMessage(), e);
			} finally {
				reader.dispose();
			}
		} else {
			log.error("No reader found for given format: {}", suffix);
		}
		return null;
	}
	
	private static Size computeScaledSize(int width, int height, int maxWidth, int maxHeight, boolean fill) {
		if(maxHeight > height && maxWidth > width) {
			return new Size(width, height, 0, 0, false);
		}
		
		int xOffset = 0;
		int yOffset = 0;

		double thumbRatio = (double)maxWidth / (double)maxHeight;
		double imageRatio = (double)width / (double)height;
		if (fill) {
			if (thumbRatio < imageRatio) {
				int newWidth = (int)(maxHeight * imageRatio);
				if(newWidth > maxWidth) {
					// we need to limit the xOffset, subImage calculation in
					// scaleFastTo will fail otherwise
					xOffset = Math.min((newWidth - maxWidth) / 2, maxWidth / 2);
				}
				maxWidth = Math.min(maxWidth, newWidth);
			} else {
				int newHeight = (int)(maxWidth / imageRatio);
				if(newHeight > maxHeight) {
					// we need to limit the yOffset, subImage calculation in
					// scaleFastTo will fail otherwise
					yOffset = Math.min((newHeight - maxHeight) / 2, maxHeight / 2);
				}
				maxHeight = Math.min(maxHeight, newHeight);
			}
		} else {
			if (thumbRatio < imageRatio) {
				maxHeight = (int)(maxWidth / imageRatio);
			} else {
				maxWidth = (int)(maxHeight * imageRatio);
			}
		}
		return new Size(maxWidth, maxHeight, xOffset, yOffset, true);
	}
	
	/**
	 * Can change this to choose a better compression level as the default
	 * @param image
	 * @param scaledImage
	 * @param scaledSize
	 * @param outputFormat
	 * @return
	 */
	public static boolean writeTo(BufferedImage image, File scaledImage, Size scaledSize, String outputFormat) {
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
	 * @param scaledSize
	 * @param outputFormat
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

	private static BufferedImage cropTo(BufferedImage img, Size size, Crop cropSelection) {
		int w = cropSelection.getWidth();
		int h = cropSelection.getHeight();
	    int x = cropSelection.getX();
	    int y = cropSelection.getY();
	    //make sure that the sub image is in the raster (if not boum!!)
	    w = Math.min(w, size.getWidth() - x);
	    h = Math.min(h, size.getHeight() - y);
	    //crop the image to see only the center of the image
	    return img.getSubimage(x, y, w, h);
	}
	
	/**
	 * This code is very inspired on Chris Campbells article "The Perils of Image.getScaledInstance()"
	 *
	 * The article can be found here:
	 * http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html
	 *
	 * Note that the filter method is threadsafe
	 */
	private static BufferedImage scaleFastTo(BufferedImage img, Size scaledSize) {
		if(!scaledSize.isChanged()) return img;

		int dstWidth = scaledSize.getWidth();
		int dstHeight = scaledSize.getHeight();
		
		BufferedImage dest;
		if (img.getType() == BufferedImage.TYPE_CUSTOM) {
			dest = new BufferedImage(dstWidth, dstHeight, BufferedImage.TYPE_INT_ARGB);
		} else {
			dest = new BufferedImage(dstWidth, dstHeight, img.getType());
		}
		
	    BufferedImage ret = img;
	    int w, h;
	
	    // Use multi-step technique: start with original size, then
	    // scale down in multiple passes with drawImage()
	    // until the target size is reached
	    w = img.getWidth();
	    h = img.getHeight();
	    
	    int x = scaledSize.getXOffset();
	    int y = scaledSize.getYOffset();
	    
	    //crop the image to see only the center of the image
	    if(x > 0 || y > 0) {
	    	ret = img.getSubimage(x, y, w - (2 * x), h - (2 * y));
	    }
	
	    do {
	    	if (w > dstWidth) {
	    		if(x > 0) {
	    			x /= 2;
	    		}
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
	
	private static final void closeQuietly(ImageInputStream ins, String imageName) {
		if(ins != null) {
			try {
				ins.close();
			} catch (IOException e) {
				log.error("Exception when trying to close {}", imageName, e);
			}
		}
	}
	
	public static final class SizeAndBufferedImage {
		private final Size size;
		private final Size scaledSize;
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
}