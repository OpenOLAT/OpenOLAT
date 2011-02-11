package ch.goodsolutions.codeutils;

import java.io.File;

import org.olat.core.util.FileUtils;
import org.olat.core.util.FileVisitor;

public class CleanCVSTags {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// work in progress
		FileVisitor fv = new FileVisitor() {

			public void visit(File file) {
				// TODO Auto-generated method stub
				String fname = file.getName();
				if (fname.endsWith(".java")) {
					System.out.println(fname);
					// use ^ ?\*.*\$[^\$]*\$$ to replace cvs tags
				}
			}
		};
		
		FileUtils.visitRecursively(new File("C:/development/workspace/olat4head/webapp/WEB-INF/src"), fv);
		

	}

}
