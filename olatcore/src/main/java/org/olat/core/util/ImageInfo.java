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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.util;


import java.io.DataInput;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Vector;

/**
 * ImageInfo.java Version 1.4 A Java class to determine image width, height and
 * color depth for a number of image file formats. Written by Marco Schmidt
 * <http://www.geocities.com/marcoschmidt.geo/contact.html>. Contributed to the
 * Public Domain. Last modification 2003-07-28
 *
 * 
 * Get file format, image resolution, number of bits per pixel and optionally
 * number of images, comments and physical resolution from JPEG, GIF, BMP, PCX,
 * PNG, IFF, RAS, PBM, PGM, PPM, PSD and SWF files (or input streams).
 * <p>
 * Use the class like this:
 * 
 * <pre>
 * ImageInfo ii = new ImageInfo();
 * ii.setInput(in); // in can be InputStream or RandomAccessFile
 * ii.setDetermineImageNumber(true); // default is false
 * ii.setCollectComments(true); // default is false
 * if (!ii.check()) {
 * 	System.err.println(&quot;Not a supported image file format.&quot;);
 * 	return;
 * }
 * System.out.println(ii.getFormatName() + &quot;, &quot; + ii.getMimeType() + &quot;, &quot; + ii.getWidth() + &quot; x &quot; + ii.getHeight() + &quot; pixels, &quot;
 * 		+ ii.getBitsPerPixel() + &quot; bits per pixel, &quot; + ii.getNumberOfImages() + &quot; image(s), &quot; + ii.getNumberOfComments() + &quot; comment(s).&quot;);
 * </pre>
 * 
 * You can also use this class as a command line program. Call it with a number
 * of image file names as parameters:
 * 
 * <pre>
 * 
 *  
 *     java ImageInfo *.jpg *.png *.gif
 *   
 *  
 * </pre>
 * 
 * or call it without parameters and pipe data to it:
 * 
 * <pre>
 * 
 *  
 *     cat image.jpg | java ImageInfo
 *   
 *  
 * </pre>
 * 
 * <p>
 * Known limitations:
 * <ul>
 * <li>When the determination of the number of images is turned off, GIF bits
 * per pixel are only read from the global header. For some GIFs, local palettes
 * change this to a typically larger value. To be certain to get the correct
 * color depth, call setDetermineImageNumber(true) before calling check(). The
 * complete scan over the GIF file will take additional time.</li>
 * <li>Transparency information is not included in the bits per pixel count.
 * Actually, it was my decision not to include those bits, so it's a feature!
 * ;-)</li>
 * </ul>
 * <p>
 * Requirements:
 * <ul>
 * <li>Java 1.1 or higher</li>
 * </ul>
 * <p>
 * The latest version can be found at <a
 * href="http://www.geocities.com/marcoschmidt.geo/image-info.html">http://www.geocities.com/marcoschmidt.geo/image-info.html
 * </a>.
 * <p>
 * Written by <a href="mailto:marcoschmidt@users.sourceforge.net">Marco Schmidt
 * </a>.
 * <p>
 * This class is contributed to the Public Domain. Use it at your own risk.
 * <p>
 * Last modification 2003-07-28.
 * <p>
 * History:
 * <ul>
 * <li><strong>2001-08-24 </strong> Initial version.</li>
 * <li><strong>2001-10-13 </strong> Added support for the file formats BMP and
 * PCX.</li>
 * <li><strong>2001-10-16 </strong> Fixed bug in read(int[], int, int) that
 * returned
 * <li><strong>2002-01-22 </strong> Added support for file formats Amiga IFF
 * and Sun Raster (RAS).</li>
 * <li><strong>2002-01-24 </strong> Added support for file formats Portable
 * Bitmap / Graymap / Pixmap (PBM, PGM, PPM) and Adobe Photoshop (PSD). Added
 * new method getMimeType() to return the MIME type associated with a particular
 * file format.</li>
 * <li><strong>2002-03-15 </strong> Added support to recognize number of images
 * in file. Only works with GIF. Use {@link #setDetermineImageNumber}with
 * <code>true</code> as argument to identify animated GIFs (
 * {@link #getNumberOfImages()}will return a value larger than <code>1</code>).
 * </li>
 * <li><strong>2002-04-10 </strong> Fixed a bug in the feature 'determine
 * number of images in animated GIF' introduced with version 1.1. Thanks to
 * Marcelo P. Lima for sending in the bug report. Released as 1.1.1.</li>
 * <li><strong>2002-04-18 </strong> Added {@link #setCollectComments(boolean)}.
 * That new method lets the user specify whether textual comments are to be
 * stored in an internal list when encountered in an input image file / stream.
 * Added two methods to return the physical width and height of the image in
 * dpi: {@link #getPhysicalWidthDpi()}and {@link #getPhysicalHeightDpi()}. If
 * the physical resolution could not be retrieved, these methods return
 * <code>-1</code>.</li>
 * <li><strong>2002-04-23 </strong> Added support for the new properties
 * physical resolution and comments for some formats. Released as 1.2.</li>
 * <li><strong>2002-06-17 </strong> Added support for SWF, sent in by Michael
 * Aird. Changed checkJpeg() so that other APP markers than APP0 will not lead
 * to a failure anymore. Released as 1.3.</li>
 * <li><strong>2003-07-28 </strong> Bug fix - skip method now takes return
 * values into consideration. Less bytes than necessary may have been skipped,
 * leading to flaws in the retrieved information in some cases. Thanks to
 * Bernard Bernstein for pointing that out. Released as 1.4.</li>
 * </ul>
 */
public class ImageInfo {
	/**
	 * Return value of {@link #getFormat()}for JPEG streams. ImageInfo can
	 * extract physical resolution and comments from JPEGs (only from APP0
	 * headers). Only one image can be stored in a file.
	 */
	public static final int FORMAT_JPEG = 0;

	/**
	 * Return value of {@link #getFormat()}for GIF streams. ImageInfo can extract
	 * comments from GIFs and count the number of images (GIFs with more than one
	 * image are animations). If you know of a place where GIFs store the physical
	 * resolution of an image, please <a
	 * href="http://www.geocities.com/marcoschmidt.geo/contact.html">send me a
	 * mail </a>!
	 */
	public static final int FORMAT_GIF = 1;

	/**
	 * Return value of {@link #getFormat()}for PNG streams. PNG only supports one
	 * image per file. Both physical resolution and comments can be stored with
	 * PNG, but ImageInfo is currently not able to extract those.
	 */
	public static final int FORMAT_PNG = 2;

	/**
	 * Return value of {@link #getFormat()}for BMP streams. BMP only supports one
	 * image per file. BMP does not allow for comments. The physical resolution
	 * can be stored.
	 * <em>The specification that I have says that the values must be
	 *  interpreted as dots per meter. However, given that I only
	 *  encounter typical dpi values like 72 or 300, I currently
	 *  consider those values dpi. Maybe someone can shed some light
	 *  on this, please send me a mail in that case.</em>
	 */
	public static final int FORMAT_BMP = 3;

	/**
	 * Return value of {@link #getFormat()}for PCX streams. PCX does not allow
	 * for comments or more than one image per file. However, the physical
	 * resolution can be stored.
	 */
	public static final int FORMAT_PCX = 4;

	/**
	 * Return value of {@link #getFormat()}for IFF streams.
	 */
	public static final int FORMAT_IFF = 5;

	/**
	 * Return value of {@link #getFormat()}for RAS streams. Sun Raster allows for
	 * one image per file only and is not able to store physical resolution or
	 * comments.
	 */
	public static final int FORMAT_RAS = 6;

	/** Return value of {@link #getFormat()}for PBM streams. */
	public static final int FORMAT_PBM = 7;

	/** Return value of {@link #getFormat()}for PGM streams. */
	public static final int FORMAT_PGM = 8;

	/** Return value of {@link #getFormat()}for PPM streams. */
	public static final int FORMAT_PPM = 9;

	/** Return value of {@link #getFormat()}for PSD streams. */
	public static final int FORMAT_PSD = 10;

	/** Return value of {@link #getFormat()}for SWF (Shockwave) streams. */
	public static final int FORMAT_SWF = 11;

	/**
	 * <code>COLOR_TYPE_UNKNOWN</code>
	 */
	public static final int COLOR_TYPE_UNKNOWN = -1;
	/**
	 * <code>COLOR_TYPE_TRUECOLOR_RGB</code>
	 */
	public static final int COLOR_TYPE_TRUECOLOR_RGB = 0;
	/**
	 * <code>COLOR_TYPE_PALETTED</code>
	 */
	public static final int COLOR_TYPE_PALETTED = 1;
	/**
	 * <code>COLOR_TYPE_GRAYSCALE</code>
	 */
	public static final int COLOR_TYPE_GRAYSCALE = 2;
	/**
	 * <code>COLOR_TYPE_BLACK_AND_WHITE</code>
	 */
	public static final int COLOR_TYPE_BLACK_AND_WHITE = 3;

	/**
	 * The names of all supported file formats. The FORMAT_xyz int constants can
	 * be used as index values for this array.
	 */
	private static final String[] FORMAT_NAMES = { "JPEG", "GIF", "PNG", "BMP", "PCX", "IFF", "RAS", "PBM", "PGM", "PPM", "PSD", "SWF" };

	/**
	 * The names of the MIME types for all supported file formats. The FORMAT_xyz
	 * int constants can be used as index values for this array.
	 */
	private static final String[] MIME_TYPE_STRINGS = { "image/jpeg", "image/gif", "image/png", "image/bmp", "image/pcx", "image/iff",
			"image/ras", "image/x-portable-bitmap", "image/x-portable-graymap", "image/x-portable-pixmap", "image/psd",
			"application/x-shockwave-flash" };

	private int width;
	private int height;
	private int bitsPerPixel;
	private int format;
	private InputStream in;
	private DataInput din;
	private boolean collectComments = true;
	private Vector comments;
	private boolean determineNumberOfImages;
	private int numberOfImages;
	private int physicalHeightDpi;
	private int physicalWidthDpi;
	private int bitBuf;
	private int bitPos;

	private void addComment(String s) {
		if (comments == null) {
			comments = new Vector();
		}
		comments.addElement(s);
	}

	/**
	 * Call this method after you have provided an input stream or file using
	 * {@link #setInput(InputStream)}or {@link #setInput(DataInput)}. If true is
	 * returned, the file format was known and information on the file's content
	 * can be retrieved using the various getXyz methods.
	 * 
	 * @return if information could be retrieved from input
	 */
	public boolean check() {
		format = -1;
		width = -1;
		height = -1;
		bitsPerPixel = -1;
		numberOfImages = 1;
		physicalHeightDpi = -1;
		physicalWidthDpi = -1;
		comments = null;
		try {
			int b1 = read() & 0xff;
			int b2 = read() & 0xff;
			if (b1 == 0x47 && b2 == 0x49) {
				return checkGif();
			} else if (b1 == 0x89 && b2 == 0x50) {
				return checkPng();
			} else if (b1 == 0xff && b2 == 0xd8) {
				return checkJpeg();
			} else if (b1 == 0x42 && b2 == 0x4d) {
				return checkBmp();
			} else if (b1 == 0x0a && b2 < 0x06) {
				return checkPcx();
			} else if (b1 == 0x46 && b2 == 0x4f) {
				return checkIff();
			} else if (b1 == 0x59 && b2 == 0xa6) {
				return checkRas();
			} else if (b1 == 0x50 && b2 >= 0x31 && b2 <= 0x36) {
				return checkPnm(b2 - '0');
			} else if (b1 == 0x38 && b2 == 0x42) {
				return checkPsd();
			} else if (b1 == 0x46 && b2 == 0x57) {
				return checkSwf();
			} else {
				return false;
			}
		} catch (IOException ioe) {
			return false;
		}
	}

	private boolean checkBmp() throws IOException {
		byte[] a = new byte[44];
		if (read(a) != a.length) { return false; }
		width = getIntLittleEndian(a, 16);
		height = getIntLittleEndian(a, 20);
		if (width < 1 || height < 1) { return false; }
		bitsPerPixel = getShortLittleEndian(a, 26);
		if (bitsPerPixel != 1 && bitsPerPixel != 4 && bitsPerPixel != 8 && bitsPerPixel != 16 && bitsPerPixel != 24 && bitsPerPixel != 32) { return false; }
		int x = getIntLittleEndian(a, 36);
		if (x > 0) {
			setPhysicalWidthDpi(x);
		}
		int y = getIntLittleEndian(a, 40);
		if (y > 0) {
			setPhysicalHeightDpi(y);
		}
		format = FORMAT_BMP;
		return true;
	}

	private boolean checkGif() throws IOException {
		final byte[] GIF_MAGIC_87A = { 0x46, 0x38, 0x37, 0x61 };
		final byte[] GIF_MAGIC_89A = { 0x46, 0x38, 0x39, 0x61 };
		byte[] a = new byte[11]; // 4 from the GIF signature + 7 from the global
		// header
		if (read(a) != 11) { return false; }
		if ((!equals(a, 0, GIF_MAGIC_89A, 0, 4)) && (!equals(a, 0, GIF_MAGIC_87A, 0, 4))) { return false; }
		format = FORMAT_GIF;
		width = getShortLittleEndian(a, 4);
		height = getShortLittleEndian(a, 6);
		int flags = a[8] & 0xff;
		bitsPerPixel = ((flags >> 4) & 0x07) + 1;
		if (!determineNumberOfImages) { return true; }
		// skip global color palette
		if ((flags & 0x80) != 0) {
			int tableSize = (1 << ((flags & 7) + 1)) * 3;
			skip(tableSize);
		}
		numberOfImages = 0;
		int blockType;
		do {
			blockType = read();
			switch (blockType) {
				case (0x2c): // image separator
				{
					if (read(a, 0, 9) != 9) { return false; }
					flags = a[8] & 0xff;
					int localBitsPerPixel = (flags & 0x07) + 1;
					if (localBitsPerPixel > bitsPerPixel) {
						bitsPerPixel = localBitsPerPixel;
					}
					if ((flags & 0x80) != 0) {
						skip((1 << localBitsPerPixel) * 3);
					}
					skip(1); // initial code length
					int n;
					do {
						n = read();
						if (n > 0) {
							skip(n);
						} else if (n == -1) { return false; }
					} while (n > 0);
					numberOfImages++;
					break;
				}
				case (0x21): // extension
				{
					int extensionType = read();
					if (collectComments && extensionType == 0xfe) {
						StringBuilder sb = new StringBuilder();
						int n;
						do {
							n = read();
							if (n == -1) { return false; }
							if (n > 0) {
								for (int i = 0; i < n; i++) {
									int ch = read();
									if (ch == -1) { return false; }
									sb.append((char) ch);
								}
							}
						} while (n > 0);
					} else {
						int n;
						do {
							n = read();
							if (n > 0) {
								skip(n);
							} else if (n == -1) { return false; }
						} while (n > 0);
					}
					break;
				}
				case (0x3b): // end of file
				{
					break;
				}
				default: {
					return false;
				}
			}
		} while (blockType != 0x3b);
		return true;
	}

	private boolean checkIff() throws IOException {
		byte[] a = new byte[10];
		// read remaining 2 bytes of file id, 4 bytes file size
		// and 4 bytes IFF subformat
		if (read(a, 0, 10) != 10) { return false; }
		final byte[] IFF_RM = { 0x52, 0x4d };
		if (!equals(a, 0, IFF_RM, 0, 2)) { return false; }
		int type = getIntBigEndian(a, 6);
		if (type != 0x494c424d && // type must be ILBM...
				type != 0x50424d20) { // ...or PBM
			return false;
		}
		// loop chunks to find BMHD chunk
		do {
			if (read(a, 0, 8) != 8) { return false; }
			int chunkId = getIntBigEndian(a, 0);
			int size = getIntBigEndian(a, 4);
			if ((size & 1) == 1) {
				size++;
			}
			if (chunkId == 0x424d4844) { // BMHD chunk
				if (read(a, 0, 9) != 9) { return false; }
				format = FORMAT_IFF;
				width = getShortBigEndian(a, 0);
				height = getShortBigEndian(a, 2);
				bitsPerPixel = a[8] & 0xff;
				return (width > 0 && height > 0 && bitsPerPixel > 0 && bitsPerPixel < 33);
			} else {
				skip(size);
			}
		} while (true);
	}

	private boolean checkJpeg() throws IOException {
		byte[] data = new byte[12];
		while (true) {
			if (read(data, 0, 4) != 4) { return false; }
			int marker = getShortBigEndian(data, 0);
			int size = getShortBigEndian(data, 2);
			if ((marker & 0xff00) != 0xff00) { return false; // not a valid marker
			}
			if (marker == 0xffe0) { // APPx
				if (size < 14) { return false; // APPx header must be >= 14 bytes
				}
				if (read(data, 0, 12) != 12) { return false; }
				final byte[] APP0_ID = { 0x4a, 0x46, 0x49, 0x46, 0x00 };
				if (equals(APP0_ID, 0, data, 0, 5)) {
					//System.out.println("data 7=" + data[7]);
					if (data[7] == 1) {
						setPhysicalWidthDpi(getShortBigEndian(data, 8));
						setPhysicalHeightDpi(getShortBigEndian(data, 10));
					} else if (data[7] == 2) {
						int x = getShortBigEndian(data, 8);
						int y = getShortBigEndian(data, 10);
						setPhysicalWidthDpi((int) (x * 2.54f));
						setPhysicalHeightDpi((int) (y * 2.54f));
					}
				}
				skip(size - 14);
			} else if (collectComments && size > 2 && marker == 0xfffe) { // comment
				size -= 2;
				byte[] chars = new byte[size];
				if (read(chars, 0, size) != size) { return false; }
				String comment = new String(chars, "iso-8859-1");
				comment = comment.trim();
				//System.out.println(comment);
				addComment(comment);
			} else if (marker >= 0xffc0 && marker <= 0xffcf && marker != 0xffc4 && marker != 0xffc8) {
				if (read(data, 0, 6) != 6) { return false; }
				format = FORMAT_JPEG;
				bitsPerPixel = (data[0] & 0xff) * (data[5] & 0xff);
				width = getShortBigEndian(data, 3);
				height = getShortBigEndian(data, 1);
				return true;
			} else {
				skip(size - 2);
			}
		}
	}

	private boolean checkPcx() throws IOException {
		byte[] a = new byte[64];
		if (read(a) != a.length) { return false; }
		if (a[0] != 1) { // encoding, 1=RLE is only valid value
			return false;
		}
		// width / height
		int x1 = getShortLittleEndian(a, 2);
		int y1 = getShortLittleEndian(a, 4);
		int x2 = getShortLittleEndian(a, 6);
		int y2 = getShortLittleEndian(a, 8);
		if (x1 < 0 || x2 < x1 || y1 < 0 || y2 < y1) { return false; }
		width = x2 - x1 + 1;
		height = y2 - y1 + 1;
		// color depth
		int bits = a[1];
		int planes = a[63];
		if (planes == 1 && (bits == 1 || bits == 2 || bits == 4 || bits == 8)) {
			// paletted
			bitsPerPixel = bits;
		} else if (planes == 3 && bits == 8) {
			// RGB truecolor
			bitsPerPixel = 24;
		} else {
			return false;
		}
		setPhysicalWidthDpi(getShortLittleEndian(a, 10));
		setPhysicalHeightDpi(getShortLittleEndian(a, 10));
		format = FORMAT_PCX;
		return true;
	}

	private boolean checkPng() throws IOException {
		final byte[] PNG_MAGIC = { 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a };
		byte[] a = new byte[24];
		if (read(a) != 24) { return false; }
		if (!equals(a, 0, PNG_MAGIC, 0, 6)) { return false; }
		format = FORMAT_PNG;
		width = getIntBigEndian(a, 14);
		height = getIntBigEndian(a, 18);
		bitsPerPixel = a[22] & 0xff;
		int colorType = a[23] & 0xff;
		if (colorType == 2 || colorType == 6) {
			bitsPerPixel *= 3;
		}
		return true;
	}

	private boolean checkPnm(int id) throws IOException {
		if (id < 1 || id > 6) { return false; }
		final int[] PNM_FORMATS = { FORMAT_PBM, FORMAT_PGM, FORMAT_PPM };
		format = PNM_FORMATS[(id - 1) % 3];
		boolean hasPixelResolution = false;
		String s;
		while (true) {
			s = readLine();
			if (s != null) {
				s = s.trim();
			}
			if (s == null || s.length() < 1) {
				continue;
			}
			if (s.charAt(0) == '#') { // comment
				if (collectComments && s.length() > 1) {
					addComment(s.substring(1));
				}
				continue;
			}
			if (!hasPixelResolution) { // split "343 966" into width=343, height=966
				int spaceIndex = s.indexOf(' ');
				if (spaceIndex == -1) { return false; }
				String widthString = s.substring(0, spaceIndex);
				spaceIndex = s.lastIndexOf(' ');
				if (spaceIndex == -1) { return false; }
				String heightString = s.substring(spaceIndex + 1);
				try {
					width = Integer.parseInt(widthString);
					height = Integer.parseInt(heightString);
				} catch (NumberFormatException nfe) {
					return false;
				}
				if (width < 1 || height < 1) { return false; }
				if (format == FORMAT_PBM) {
					bitsPerPixel = 1;
					return true;
				}
				hasPixelResolution = true;
			} else {
				int maxSample;
				try {
					maxSample = Integer.parseInt(s);
				} catch (NumberFormatException nfe) {
					return false;
				}
				if (maxSample < 0) { return false; }
				for (int i = 0; i < 25; i++) {
					if (maxSample < (1 << (i + 1))) {
						bitsPerPixel = i + 1;
						if (format == FORMAT_PPM) {
							bitsPerPixel *= 3;
						}
						return true;
					}
				}
				return false;
			}
		}
	}

	private boolean checkPsd() throws IOException {
		byte[] a = new byte[24];
		if (read(a) != a.length) { return false; }
		final byte[] PSD_MAGIC = { 0x50, 0x53 };
		if (!equals(a, 0, PSD_MAGIC, 0, 2)) { return false; }
		format = FORMAT_PSD;
		width = getIntBigEndian(a, 16);
		height = getIntBigEndian(a, 12);
		int channels = getShortBigEndian(a, 10);
		int depth = getShortBigEndian(a, 20);
		bitsPerPixel = channels * depth;
		return (width > 0 && height > 0 && bitsPerPixel > 0 && bitsPerPixel <= 64);
	}

	private boolean checkRas() throws IOException {
		byte[] a = new byte[14];
		if (read(a) != a.length) { return false; }
		final byte[] RAS_MAGIC = { 0x6a, (byte) 0x95 };
		if (!equals(a, 0, RAS_MAGIC, 0, 2)) { return false; }
		format = FORMAT_RAS;
		width = getIntBigEndian(a, 2);
		height = getIntBigEndian(a, 6);
		bitsPerPixel = getIntBigEndian(a, 10);
		return (width > 0 && height > 0 && bitsPerPixel > 0 && bitsPerPixel <= 24);
	}

	// Written by Michael Aird.
	private boolean checkSwf() throws IOException {
		//get rid of the last byte of the signature, the byte of the version and 4
		// bytes of the size
		byte[] a = new byte[6];
		if (read(a) != a.length) { return false; }
		format = FORMAT_SWF;
		int bitSize = (int) readUBits(5);
		//int minX = (int)readSBits( bitSize );
		int maxX = readSBits(bitSize);
		//int minY = (int)readSBits( bitSize );
		int maxY = readSBits(bitSize);
		width = maxX / 20; //cause we're in twips
		height = maxY / 20; //cause we're in twips
		setPhysicalWidthDpi(72);
		setPhysicalHeightDpi(72);
		return (width > 0 && height > 0);
	}

	/**
	 * Run over String list, return false iff at least one of the arguments equals
	 * <code>-c</code>.
	 */
	private static boolean determineVerbosity(String[] args) {
		if (args != null && args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				if ("-c".equals(args[i])) { return false; }
			}
		}
		return true;
	}

	private boolean equals(byte[] a1, int offs1, byte[] a2, int offs2, int num) {
		while (num-- > 0) {
			if (a1[offs1++] != a2[offs2++]) { return false; }
		}
		return true;
	}

	/**
	 * If {@link #check()}was successful, returns the image's number of bits per
	 * pixel. Does not include transparency information like the alpha channel.
	 * 
	 * @return number of bits per image pixel
	 */
	public int getBitsPerPixel() {
		return bitsPerPixel;
	}

	/**
	 * Returns the index'th comment retrieved from the image.
	 * 
	 * @param index
	 * @return @throws IllegalArgumentException if index is smaller than 0 or
	 *         larger than or equal to the number of comments retrieved
	 * @see #getNumberOfComments
	 */
	public String getComment(int index) {
		if (comments == null || index < 0 || index >= comments.size()) { throw new IllegalArgumentException("Not a valid comment index: "
				+ index); }
		return (String) comments.elementAt(index);
	}

	/**
	 * If {@link #check()}was successful, returns the image format as one of the
	 * FORMAT_xyz constants from this class. Use {@link #getFormatName()}to get a
	 * textual description of the file format.
	 * 
	 * @return file format as a FORMAT_xyz constant
	 */
	public int getFormat() {
		return format;
	}

	/**
	 * If {@link #check()}was successful, returns the image format's name. Use
	 * {@link #getFormat()}to get a unique number.
	 * 
	 * @return file format name
	 */
	public String getFormatName() {
		if (format >= 0 && format < FORMAT_NAMES.length) {
			return FORMAT_NAMES[format];
		} else {
			return "?";
		}
	}

	/**
	 * If {@link #check()}was successful, returns one the image's vertical
	 * resolution in pixels.
	 * 
	 * @return image height in pixels
	 */
	public int getHeight() {
		return height;
	}

	private int getIntBigEndian(byte[] a, int offs) {
		return (a[offs] & 0xff) << 24 | (a[offs + 1] & 0xff) << 16 | (a[offs + 2] & 0xff) << 8 | a[offs + 3] & 0xff;
	}

	private int getIntLittleEndian(byte[] a, int offs) {
		return (a[offs + 3] & 0xff) << 24 | (a[offs + 2] & 0xff) << 16 | (a[offs + 1] & 0xff) << 8 | a[offs] & 0xff;
	}

	/**
	 * If {@link #check()}was successful, returns a String with the MIME type of
	 * the format.
	 * 
	 * @return MIME type, e.g. <code>image/jpeg</code>
	 */
	public String getMimeType() {
		if (format >= 0 && format < MIME_TYPE_STRINGS.length) {
			return MIME_TYPE_STRINGS[format];
		} else {
			return null;
		}
	}

	/**
	 * If {@link #check()}was successful and {@link #setCollectComments(boolean)}
	 * was called with <code>true</code> as argument, returns the number of
	 * comments retrieved from the input image stream / file. Any number &gt;= 0
	 * and smaller than this number of comments is then a valid argument for the
	 * {@link #getComment(int)}method.
	 * 
	 * @return number of comments retrieved from input image
	 */
	public int getNumberOfComments() {
		if (comments == null) {
			return 0;
		} else {
			return comments.size();
		}
	}

	/**
	 * Returns the number of images in the examined file. Assumes that
	 * <code>setDetermineImageNumber(true);</code> was called before a
	 * successful call to {@link #check()}. This value can currently be only
	 * different from <code>1</code> for GIF images.
	 * 
	 * @return number of images in file
	 */
	public int getNumberOfImages() {
		return numberOfImages;
	}

	/**
	 * Returns the physical height of this image in dots per inch (dpi). Assumes
	 * that {@link #check()}was successful. Returns <code>-1</code> on failure.
	 * 
	 * @return physical height (in dpi)
	 * @see #getPhysicalWidthDpi()
	 * @see #getPhysicalHeightInch()
	 */
	public int getPhysicalHeightDpi() {
		return physicalHeightDpi;
	}

	/**
	 * If {@link #check()}was successful, returns the physical width of this
	 * image in dpi (dots per inch) or -1 if no value could be found.
	 * 
	 * @return physical height (in dpi)
	 * @see #getPhysicalHeightDpi()
	 * @see #getPhysicalWidthDpi()
	 * @see #getPhysicalWidthInch()
	 */
	public float getPhysicalHeightInch() {
		int h = getHeight();
		int ph = getPhysicalHeightDpi();
		if (h > 0 && ph > 0) {
			return ((float) h) / ((float) ph);
		} else {
			return -1.0f;
		}
	}

	/**
	 * If {@link #check()}was successful, returns the physical width of this
	 * image in dpi (dots per inch) or -1 if no value could be found.
	 * 
	 * @return physical width (in dpi)
	 * @see #getPhysicalHeightDpi()
	 * @see #getPhysicalWidthInch()
	 * @see #getPhysicalHeightInch()
	 */
	public int getPhysicalWidthDpi() {
		return physicalWidthDpi;
	}

	/**
	 * Returns the physical width of an image in inches, or <code>-1.0f</code>
	 * if width information is not available. Assumes that {@link #check}has been
	 * called successfully.
	 * 
	 * @return physical width in inches or <code>-1.0f</code> on failure
	 * @see #getPhysicalWidthDpi
	 * @see #getPhysicalHeightInch
	 */
	public float getPhysicalWidthInch() {
		int w = getWidth();
		int pw = getPhysicalWidthDpi();
		if (w > 0 && pw > 0) {
			return ((float) w) / ((float) pw);
		} else {
			return -1.0f;
		}
	}

	private int getShortBigEndian(byte[] a, int offs) {
		return (a[offs] & 0xff) << 8 | (a[offs + 1] & 0xff);
	}

	private int getShortLittleEndian(byte[] a, int offs) {
		return (a[offs] & 0xff) | (a[offs + 1] & 0xff) << 8;
	}

	/**
	 * If {@link #check()}was successful, returns one the image's horizontal
	 * resolution in pixels.
	 * 
	 * @return image width in pixels
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * To use this class as a command line application, give it either some file
	 * names as parameters (information on them will be printed to standard
	 * output, one line per file) or call it with no parameters. It will then
	 * check data given to it via standard input.
	 * 
	 * @param args the program arguments which must be file names
	 */
	public static void main(String[] args) {
		ImageInfo imageInfo = new ImageInfo();
		imageInfo.setDetermineImageNumber(true);
		boolean verbose = determineVerbosity(args);
		if (args.length == 0) {
			run(null, System.in, imageInfo, verbose);
		} else {
			int index = 0;
			while (index < args.length) {
				InputStream in = null;
				try {
					String name = args[index++];
					System.out.print(name + ";");
					if (name.startsWith("http://")) {
						in = new URL(name).openConnection().getInputStream();
					} else {
						in = new FileInputStream(name);
					}
					run(name, in, imageInfo, verbose);
					in.close();
				} catch (Exception e) {
					System.out.println(e);
					try {
						in.close();
					} catch (Exception ee) {
						//
					}
				}
			}
		}
	}

	private static void print(String sourceName, ImageInfo ii, boolean verbose) {
		if (verbose) {
			printVerbose(sourceName, ii);
		} else {
			printCompact(sourceName, ii);
		}
	}

	private static void printCompact(String sourceName, ImageInfo imageInfo) {
		System.out.println(sourceName + ";" + imageInfo.getFormatName() + ";" + imageInfo.getMimeType() + ";" + imageInfo.getWidth() + ";"
				+ imageInfo.getHeight() + ";" + imageInfo.getBitsPerPixel() + ";" + imageInfo.getNumberOfImages() + ";"
				+ imageInfo.getPhysicalWidthDpi() + ";" + imageInfo.getPhysicalHeightDpi() + ";" + imageInfo.getPhysicalWidthInch() + ";"
				+ imageInfo.getPhysicalHeightInch());
	}

	private static void printLine(int indentLevels, String text, float value, float minValidValue) {
		if (value < minValidValue) { return; }
		printLine(indentLevels, text, Float.toString(value));
	}

	private static void printLine(int indentLevels, String text, int value, int minValidValue) {
		if (value >= minValidValue) {
			printLine(indentLevels, text, Integer.toString(value));
		}
	}

	private static void printLine(int indentLevels, String text, String value) {
		if (value == null || value.length() == 0) { return; }
		while (indentLevels-- > 0) {
			System.out.print("\t");
		}
		if (text != null && text.length() > 0) {
			System.out.print(text);
			System.out.print(" ");
		}
		System.out.println(value);
	}

	private static void printVerbose(String sourceName, ImageInfo ii) {
		printLine(0, null, sourceName);
		printLine(1, "File format: ", ii.getFormatName());
		printLine(1, "MIME type: ", ii.getMimeType());
		printLine(1, "Width (pixels): ", ii.getWidth(), 1);
		printLine(1, "Height (pixels): ", ii.getHeight(), 1);
		printLine(1, "Bits per pixel: ", ii.getBitsPerPixel(), 1);
		printLine(1, "Number of images: ", ii.getNumberOfImages(), 1);
		printLine(1, "Physical width (dpi): ", ii.getPhysicalWidthDpi(), 1);
		printLine(1, "Physical height (dpi): ", ii.getPhysicalHeightDpi(), 1);
		printLine(1, "Physical width (inches): ", ii.getPhysicalWidthInch(), 1.0f);
		printLine(1, "Physical height (inches): ", ii.getPhysicalHeightInch(), 1.0f);
		int numComments = ii.getNumberOfComments();
		printLine(1, "Number of textual comments: ", numComments, 1);
		if (numComments > 0) {
			for (int i = 0; i < numComments; i++) {
				printLine(2, null, ii.getComment(i));
			}
		}
	}

	private int read() throws IOException {
		if (in != null) {
			return in.read();
		} else {
			return din.readByte();
		}
	}

	private int read(byte[] a) throws IOException {
		if (in != null) {
			return in.read(a);
		} else {
			din.readFully(a);
			return a.length;
		}
	}

	private int read(byte[] a, int offset, int num) throws IOException {
		if (in != null) {
			return in.read(a, offset, num);
		} else {
			din.readFully(a, offset, num);
			return num;
		}
	}

	private String readLine() throws IOException {
		return readLine(new StringBuilder());
	}

	private String readLine(StringBuilder sb) throws IOException {
		boolean finished;
		do {
			int value = read();
			finished = (value == -1 || value == 10);
			if (!finished) {
				sb.append((char) value);
			}
		} while (!finished);
		return sb.toString();
	}

	/**
	 * Read an unsigned value from the given number of bits
	 * @param numBits
	 * @return
	 * @throws IOException
	 */
	public long readUBits(int numBits) throws IOException {
		if (numBits == 0) { return 0; }
		int bitsLeft = numBits;
		long result = 0;
		if (bitPos == 0) { //no value in the buffer - read a byte
			if (in != null) {
				bitBuf = in.read();
			} else {
				bitBuf = din.readByte();
			}
			bitPos = 8;
		}

		while (true) {
			int shift = bitsLeft - bitPos;
			if (shift > 0) {
				// Consume the entire buffer
				result |= bitBuf << shift;
				bitsLeft -= bitPos;

				// Get the next byte from the input stream
				if (in != null) {
					bitBuf = in.read();
				} else {
					bitBuf = din.readByte();
				}
				bitPos = 8;
			} else {
				// Consume a portion of the buffer
				result |= bitBuf >> -shift;
				bitPos -= bitsLeft;
				bitBuf &= 0xff >> (8 - bitPos); // mask off the consumed bits

				return result;
			}
		}
	}

	/**
	 * Read a signed value from the given number of bits
	 */
	private int readSBits(int numBits) throws IOException {
		// Get the number as an unsigned value.
		long uBits = readUBits(numBits);

		// Is the number negative?
		if ((uBits & (1L << (numBits - 1))) != 0) {
			// Yes. Extend the sign.
			uBits |= -1L << numBits;
		}

		return (int) uBits;
	}

	/**
	 * Reset the bit buffer
	 */
	public void synchBits() {
		bitBuf = 0;
		bitPos = 0;
	}

	private static void run(String sourceName, InputStream in, ImageInfo imageInfo, boolean verbose) {
		imageInfo.setInput(in);
		imageInfo.setDetermineImageNumber(false);
		imageInfo.setCollectComments(verbose);
		if (imageInfo.check()) {
			print(sourceName, imageInfo, verbose);
		}
	}

	/**
	 * Specify whether textual comments are supposed to be extracted from input.
	 * Default is <code>false</code>. If enabled, comments will be added to an
	 * internal list.
	 * 
	 * @param newValue if <code>true</code>, this class will read comments
	 * @see #getNumberOfComments
	 * @see #getComment
	 */
	public void setCollectComments(boolean newValue) {
		collectComments = newValue;
	}

	/**
	 * Specify whether the number of images in a file is to be determined -
	 * default is <code>false</code>. This is a special option because some
	 * file formats require running over the entire file to find out the number of
	 * images, a rather time-consuming task. Not all file formats support more
	 * than one image. If this method is called with <code>true</code> as
	 * argument, the actual number of images can be queried via
	 * {@link #getNumberOfImages()}after a successful call to {@link #check()}.
	 * 
	 * @param newValue will the number of images be determined?
	 * @see #getNumberOfImages
	 */
	public void setDetermineImageNumber(boolean newValue) {
		determineNumberOfImages = newValue;
	}

	/**
	 * Set the input stream to the argument stream (or file). Note that
	 * {@link java.io.RandomAccessFile}implements {@link java.io.DataInput}.
	 * 
	 * @param dataInput the input stream to read from
	 */
	public void setInput(DataInput dataInput) {
		din = dataInput;
		in = null;
	}

	/**
	 * Set the input stream to the argument stream (or file).
	 * 
	 * @param inputStream the input stream to read from
	 */
	public void setInput(InputStream inputStream) {
		in = inputStream;
		din = null;
	}

	private void setPhysicalHeightDpi(int newValue) {
		physicalWidthDpi = newValue;
	}

	private void setPhysicalWidthDpi(int newValue) {
		physicalHeightDpi = newValue;
	}

	private void skip(int num) throws IOException {
		while (num > 0) {
			long result;
			if (in != null) {
				result = in.skip(num);
			} else {
				result = din.skipBytes(num);
			}
			if (result > 0) {
				num -= result;
			}
		}
	}
}