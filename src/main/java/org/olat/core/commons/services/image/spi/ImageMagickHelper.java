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
package org.olat.core.commons.services.image.spi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.olat.core.commons.services.image.Crop;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.thumbnail.FinalSize;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.LocalImpl;
import org.olat.core.util.vfs.NamedLeaf;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * This is an implementation which call the ImageMagick command line tool to make the
 * scaling and thumbnailing og images and pdfs. For PDFs, ImageMagick use GhostScript
 * internally with the command "gs". Set the magickPath the path where the command line
 * tools are.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ImageMagickHelper extends AbstractImageHelper {
	
	private static final Logger log = Tracing.createLoggerFor(ImageMagickHelper.class);

	@Override
	public Size thumbnailPDF(VFSLeaf pdfFile, VFSLeaf thumbnailFile, int maxWidth, int maxHeight) {
		File baseFile = extractIOFile(pdfFile);
		File thumbnailBaseFile = extractIOFile(thumbnailFile);
		FinalSize finalSize = generateThumbnail(baseFile, thumbnailBaseFile, true, maxWidth, maxHeight, false);
		if(finalSize != null) {
			return new Size(finalSize.getWidth(), finalSize.getHeight(), true);
		}
		return null;
	}
	
	@Override
	public boolean cropImage(File image, File cropedImage, Crop cropSelection) {
		FinalSize size = cropImageWithImageMagick(image, cropedImage, cropSelection);
		return size != null;
	}

	@Override
	public Size scaleImage(File image, String imgExt, VFSLeaf scaledImage, int maxWidth, int maxHeight) {
		File scaledBaseFile = extractIOFile(scaledImage);
		FinalSize finalSize = generateThumbnail(image, scaledBaseFile, false, maxWidth, maxHeight, false);
		if(finalSize != null) {
			return new Size(finalSize.getWidth(), finalSize.getHeight(), true);
		}
		return null;
	}
	
	@Override
	public Size scaleImage(VFSLeaf image, VFSLeaf scaledImage, int maxWidth, int maxHeight, boolean fill) {
		FinalSize finalSize = generateThumbnail(image, scaledImage, maxWidth, maxHeight, fill);
		if(finalSize != null) {
			return new Size(finalSize.getWidth(), finalSize.getHeight(), true);
		}
		return null;
	}
	
	@Override
	public Size scaleImage(File image, String extension, File scaledImage, int maxWidth, int maxHeight, boolean fill) {
		FinalSize finalSize = generateThumbnail(image, scaledImage, false, maxWidth, maxHeight, fill);
		if(finalSize != null) {
			return new Size(finalSize.getWidth(), finalSize.getHeight(), true);
		}
		return null;
	}

	private final FinalSize generateThumbnail(VFSLeaf file, VFSLeaf thumbnailFile, int maxWidth, int maxHeight, boolean fill) {
		File baseFile = extractIOFile(file);
		File thumbnailBaseFile = extractIOFile(thumbnailFile);
		return generateThumbnail(baseFile, thumbnailBaseFile, false, maxWidth, maxHeight, fill);
	}
	
	private final File extractIOFile(VFSLeaf leaf) {
		if(leaf instanceof NamedLeaf) {
			leaf = ((NamedLeaf)leaf).getDelegate();
		}
		
		File file = null;
		if(leaf instanceof LocalImpl) {
			file = ((LocalImpl)leaf).getBasefile();
		}
		return file;
	}
		
	private final FinalSize generateThumbnail(File file, File thumbnailFile, boolean firstOnly,
			int maxWidth, int maxHeight, boolean fill) {
		if(file == null || thumbnailFile == null) {
			log.error("Input file or output file for thumbnailing? {} -> {}", file, thumbnailFile);
			return null;
		}
		
		if(!thumbnailFile.getParentFile().exists()) {
			thumbnailFile.getParentFile().mkdirs();
		}

		List<String> cmds = new ArrayList<>();
		cmds.add("convert");
		cmds.add("-verbose");
		cmds.add("-auto-orient");
		cmds.add("-thumbnail");
		if(fill) {
			cmds.add(maxWidth + "x" + maxHeight + "^");
			cmds.add("-gravity");
			cmds.add("center");
			cmds.add("-extent");
			cmds.add(maxWidth + "x" + maxHeight);
		} else {
			cmds.add(maxWidth + "x" + maxHeight + ">");
		}
		
		if(firstOnly) {
			cmds.add(file.getAbsolutePath() + "[0]");
		} else {
			cmds.add(file.getAbsolutePath());
		}
		cmds.add(thumbnailFile.getAbsolutePath());

		CountDownLatch doneSignal = new CountDownLatch(1);

		ProcessWorker worker = new ProcessWorker(thumbnailFile, cmds, doneSignal);
		worker.start();

		try {
			if(!doneSignal.await(3000, TimeUnit.MILLISECONDS)) {
				log.warn("Cannot generate a thumbnail in 3s: {}", file);
			}
		} catch (InterruptedException e) {
			log.error("", e);
		}
		
		worker.destroyProcess();
		
		return worker.size;
	}
	
	private final FinalSize cropImageWithImageMagick(File file, File cropedFile, Crop cropSelection) {
		if(file == null || cropedFile == null) {
			log.error("Input file or output file for thumbnailing? {} -> {}", file, cropedFile);
			return null;
		}
		
		if(!cropedFile.getParentFile().exists()) {
			cropedFile.getParentFile().mkdirs();
		}

		List<String> cmds = new ArrayList<>();
		cmds.add("convert");
		cmds.add("-verbose");
		cmds.add("-crop");
		
		//40x30+10+10
		//widthxheight+xOffset+yOffset
		int width = cropSelection.getWidth();
		int height = cropSelection.getHeight();
		int x = cropSelection.getX();
		int y = cropSelection.getY();
		cmds.add(width + "x" + height + "+" + x + "+" + y);
		cmds.add(file.getAbsolutePath());
		cmds.add(cropedFile.getAbsolutePath());

		CountDownLatch doneSignal = new CountDownLatch(1);

		ProcessWorker worker = new ProcessWorker(cropedFile, cmds, doneSignal);
		worker.start();

		try {
			if(!doneSignal.await(3000, TimeUnit.MILLISECONDS)) {
				log.warn("Image cannot be cropped in 3s: {}", file);
			}
		} catch (InterruptedException e) {
			log.error("", e);
		}
		
		worker.destroyProcess();
		return worker.size;
	}
	
	private final FinalSize executeProcess(File thumbnailFile, Process proc) {

		FinalSize rv = null;
		StringBuilder errors = new StringBuilder();
		StringBuilder output = new StringBuilder();
		String line;

		InputStream stderr = proc.getErrorStream();
		InputStreamReader iserr = new InputStreamReader(stderr);
		BufferedReader berr = new BufferedReader(iserr);
		line = null;
		try {
			while ((line = berr.readLine()) != null) {
				errors.append(line);
			}
		} catch (IOException e) {
			//
		}
		
		InputStream stdout = proc.getInputStream();
		InputStreamReader isr = new InputStreamReader(stdout);
		BufferedReader br = new BufferedReader(isr);
		line = null;
		try {
			while ((line = br.readLine()) != null) {
				output.append(line);
			}
		} catch (IOException e) {
			//
		}

		if (log.isDebugEnabled()) {
			log.debug("Error: {}", errors.toString());
			log.debug("Output: {}", output.toString());
		}

		try {
			int exitValue = proc.waitFor();
			if (exitValue == 0) {
				rv = extractSizeFromOutput(thumbnailFile, output);
				if (rv == null) {
					// sometimes verbose info of convert is in stderr
					rv = extractSizeFromOutput(thumbnailFile, errors);
				}
			}
		} catch (InterruptedException e) {
			//
		}
		if(rv == null) {
			log.warn("Could not generate thumbnail: {}", thumbnailFile);
		}
		return rv;
	}
	/**
	 * Extract informations from the process:<br/>
	 * The image was scaled:
	 * /HotCoffee/olatdatas/openolat/bcroot/tmp/ryomou.jpg JPEG 579x579 579x579+0+0 8-bit DirectClass 327KB 0.020u 0:00.020/HotCoffee/olatdatas/openolat/bcroot/tmp/ryomou.jpg=>/HotCoffee/olatdatas/openolat_mysql/bcroot/repository/27394049.png JPEG 579x579=>570x570 8-bit DirectClass 803KB 0.150u 0:00.160<br/>
	 * The image wasn't scaled:
	 * /HotCoffee/olatdatas/openolat/bcroot/tmp/yukino.jpg JPEG 184x184 184x184+0+0 8-bit DirectClass 17.5KB 0.000u 0:00.009/HotCoffee/olatdatas/openolat/bcroot/tmp/yukino.jpg=>/HotCoffee/olatdatas/openolat_mysql/bcroot/repository/27394049.png JPEG 184x184 8-bit DirectClass 49.2KB 0.060u 0:00.060
	 * 
	 * @param thumbnailBaseFile
	 * @param output
	 * @return
	 */
	private final FinalSize extractSizeFromOutput(File thumbnailBaseFile, StringBuilder output) {
		try {
			String verbose = output.toString();
			int lastIndex = verbose.lastIndexOf(thumbnailBaseFile.getName());
			if(lastIndex > 0) {
				int sizeIndex = verbose.indexOf("=>", lastIndex);
				// => appears if the image is downscaled
				if(sizeIndex > 0) {
					int stopIndex = verbose.indexOf(' ', sizeIndex);
					if(stopIndex > sizeIndex) {
						String sizeStr = verbose.substring(sizeIndex + 2, stopIndex);
						FinalSize size = extractSizeFromChuck(sizeStr);
						return size;
					}
				// no scaling apparently, try to find the size
				} else {
					String ending = verbose.substring(lastIndex + thumbnailBaseFile.getName().length());
					String[] endings = ending.split(" ");
					for(String chuck:endings) {
						FinalSize size = extractSizeFromChuck(chuck);
						if(size != null) {
							return size;
						}
					}	
				}
			}
		} catch (NumberFormatException e) {
			log.error("Error parsing output: {}", output);
		}
		return null;
	}
	
	private FinalSize extractSizeFromChuck(String chuck) {
		FinalSize size = null;
		if(chuck.indexOf('x') > 0) {
			String[] sizes = chuck.split("x");
			if(sizes.length == 2) {
				try {
					int width = Integer.parseInt(sizes[0]);
					int height = Integer.parseInt(sizes[1]);
					return new FinalSize(width, height);
				} catch (NumberFormatException e) {
					//not a number, it's possible
					if(log.isDebugEnabled()) {
						log.debug("Not a size: {}", chuck);
					}
				}
			}
		}
		return size;
	}
	
	private class ProcessWorker extends Thread {
		
		private volatile Process process;
		private volatile FinalSize size;

		private final List<String> cmd;
		private final File thumbnailFile;
		private final CountDownLatch doneSignal;
		
		public ProcessWorker(File thumbnailFile, List<String> cmd, CountDownLatch doneSignal) {
			this.cmd = cmd;
			this.thumbnailFile = thumbnailFile;
			this.doneSignal = doneSignal;
		}
		
		public void destroyProcess() {
			if (process != null) {
				process.destroy();
				process = null;
			}
		}

		@Override
		public void run() {
			try {
				if(log.isDebugEnabled()) {
					log.debug(cmd.toString());
				}
				
				ProcessBuilder builder = new ProcessBuilder(cmd);
				process = builder.start();
				size = executeProcess(thumbnailFile, process);
				doneSignal.countDown();
			} catch (IOException e) {
				log.error ("Could not spawn convert sub process", e);
				destroyProcess();
			}
		}
	}
}
