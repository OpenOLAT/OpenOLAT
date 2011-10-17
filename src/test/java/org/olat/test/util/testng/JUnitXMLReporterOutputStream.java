package org.olat.test.util.testng;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class JUnitXMLReporterOutputStream extends OutputStream {

	private final JUnitXMLReporter reporter_;
	
	final int type_; // 1 == stdout, 2 == stderr

	public JUnitXMLReporterOutputStream(JUnitXMLReporter reporter, int type) {
		reporter_ = reporter;
		type_ = type;
        if(type != 1 && type != 2)
            throw new IllegalArgumentException("index has to be 1 or 2");
	}

	@Override
	public void write(int b) throws IOException {
		try{
	        Class clazz=JUnitXMLReporter.local.get();
	        if(clazz != null) {
	            Tuple<ByteArrayOutputStream,ByteArrayOutputStream> tuple=reporter_.outputs.get(clazz);
	            if(tuple != null) {
	                ByteArrayOutputStream sb=type_ == 1? tuple.getVal1() : tuple.getVal2();
	                sb.write(b);
	                return;
	            }
	        }
        	if (type_==1) {
        		reporter_.unsolicitedOut.write(b);
        	} else {
        		reporter_.unsolicitedErr.write(b);
        	}
		} finally {
			if (type_==1) {
				reporter_.old_stdout.write(b);
			} else {
				reporter_.old_stderr.write(b);
			}
		}
    }

}
