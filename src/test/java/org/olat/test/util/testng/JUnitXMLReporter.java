package org.olat.test.util.testng;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.annotations.Test;

/**
 * Listener generating XML output suitable to be processed by JUnitReport. Copied from TestNG (www.testng.org) and
 * modified
 * @author Bela Ban
 * @version $Id: JUnitXMLReporter.java,v 1.6 2008-08-12 08:22:59 eglis Exp $
 */
public class JUnitXMLReporter extends TestListenerAdapter {
    private String output_dir=null;
    private String suffix=null;

    private static final String SUFFIX="test.suffix";
    private static final String XML_DEF="<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
    private static final String CDATA="![CDATA[";
    private static final String LT="&lt;";
    private static final String GT="&gt;";
    private static final String SYSTEM_OUT="system-out";
    private static final String SYSTEM_ERR="system-err";

    PrintStream old_stdout=System.out;
    PrintStream old_stderr=System.err;


    private final ConcurrentMap<Class, List<ITestResult>> classes=new ConcurrentHashMap<Class,List<ITestResult>>();

    /** Map to keep systemout and systemerr associated with a class */
    final ConcurrentMap<Class,Tuple<ByteArrayOutputStream,ByteArrayOutputStream>> outputs=new ConcurrentHashMap<Class,Tuple<ByteArrayOutputStream,ByteArrayOutputStream>>();

    ByteArrayOutputStream unsolicitedOut = new ByteArrayOutputStream();
    ByteArrayOutputStream unsolicitedErr = new ByteArrayOutputStream();
    
    public static InheritableThreadLocal<Class> local=new InheritableThreadLocal<Class>();


    public JUnitXMLReporter() {
    	System.out.println("Start");
	}


    public void onTestStart(ITestResult result) {
        Class real_class=result.getTestClass().getRealClass();
        local.set(real_class);
        print(old_stdout, "REAL CLASS: ",  real_class.getName(), "");

        List<ITestResult> results=classes.get(real_class);
        if(results == null) {
            results=new LinkedList<ITestResult>();
            classes.putIfAbsent(real_class, results);
        }

        outputs.putIfAbsent(real_class, new Tuple<ByteArrayOutputStream,ByteArrayOutputStream>(new ByteArrayOutputStream(), new ByteArrayOutputStream()));
        // old_stdout.println(Thread.currentThread() + " running " + real_class.getName() + "." + result.getName() + "()");
    }



    /** Invoked each time a test succeeds */
    public void onTestSuccess(ITestResult tr) {
        Class real_class=tr.getTestClass().getRealClass();
        flushOtherOutput(real_class);
        addTest(real_class, tr);
        print(old_stdout, "OK:   ",  real_class.getName(), tr.getName());
    }

    public void onTestFailedButWithinSuccessPercentage(ITestResult tr) {
        Class real_class=tr.getTestClass().getRealClass();
        flushOtherOutput(real_class);
        addTest(tr.getTestClass().getRealClass(), tr);
        print(old_stdout, "OK:   ",  real_class.getName(), tr.getName());
    }



    /**
     * Invoked each time a test fails.
     */
    public void onTestFailure(ITestResult tr) {
        Class real_class=tr.getTestClass().getRealClass();
        flushOtherOutput(real_class);
        addTest(tr.getTestClass().getRealClass(), tr);
        print(old_stderr, "FAIL: ",  real_class.getName(), tr.getName());
    }

    private void flushOtherOutput(Class real_class) {
    	if (unsolicitedOut.size()>0 || unsolicitedErr.size()>0) {
			outputs.putIfAbsent(real_class, new Tuple<ByteArrayOutputStream,ByteArrayOutputStream>(unsolicitedOut, unsolicitedErr));
    		unsolicitedOut = new ByteArrayOutputStream();
    		unsolicitedErr = new ByteArrayOutputStream();
    	}
	}


	/**
     * Invoked each time a test is skipped.
     */
    public void onTestSkipped(ITestResult tr) {
        Class real_class=tr.getTestClass().getRealClass();
        flushOtherOutput(real_class);
        addTest(tr.getTestClass().getRealClass(), tr);
        print(old_stdout, "SKIP: ",  real_class.getName(), tr.getName());
    }

    private static void print(PrintStream out, String msg, String classname, String method_name) {
        out.println(msg + "[" + Thread.currentThread().getId()  + "] " + classname + "." + method_name + "()");
        // out.println(msg  + classname + "." + method_name + "()");
    }

    private void addTest(Class clazz, ITestResult result) {
        List<ITestResult> results=classes.get(clazz);
        if(results == null) {
            results=new LinkedList<ITestResult>();
            classes.putIfAbsent(clazz, results);
        }

        results=classes.get(clazz);
        results.add(result);
    }

    /**
     * Invoked after the test class is instantiated and before any configuration method is called.
     */
    public void onStart(ITestContext context) {
    	System.out.println("Changing System.out...");
    	while (System.out instanceof SysOutPrintStream) {
    		System.setOut(((SysOutPrintStream)System.out).getOriginalSysOut());
    	}
    	while (System.err instanceof SysOutPrintStream) {
    		System.setErr(((SysOutPrintStream)System.err).getOriginalSysOut());
    	}
        old_stdout=System.out;
        old_stderr=System.err;
		unsolicitedOut = new ByteArrayOutputStream();
		unsolicitedErr = new ByteArrayOutputStream();
        suffix=System.getProperty(SUFFIX);
        if(suffix != null)
            suffix=suffix.trim();
        output_dir=context.getOutputDirectory(); // + File.separator + context.getName() + suffix + ".xml";

        System.setOut(new SysOutPrintStream(new JUnitXMLReporterOutputStream(this, 1), old_stdout));

        System.setErr(new SysOutPrintStream(new JUnitXMLReporterOutputStream(this, 2), old_stderr));
    }

    /**
     * Invoked after all the tests have run and all their
     * Configuration methods have been called.
     */
    public void onFinish(ITestContext context) {
    	System.out.println("Changing System.out back...");
        System.setOut(old_stdout);
        System.setErr(old_stderr);
    	while (System.out instanceof SysOutPrintStream) {
    		System.setOut(((SysOutPrintStream)System.out).getOriginalSysOut());
    	}
    	while (System.err instanceof SysOutPrintStream) {
    		System.setErr(((SysOutPrintStream)System.err).getOriginalSysOut());
    	}

        try {
            generateReport();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * generate the XML report given what we know from all the test results
     */
    protected void generateReport() throws IOException {
        for(Map.Entry<Class,List<ITestResult>> entry: classes.entrySet()) {
            Class clazz=entry.getKey();
            List<ITestResult> results=entry.getValue();

            int num_failures=getFailures(results);
            int num_skips=getSkips(results);
            int num_errors=getErrors(results);
            long total_time=getTotalTime(results);

            String file_name=output_dir + File.separator + "TEST-" + clazz.getName();
            if(suffix != null)
                file_name=file_name + "-" + suffix;
            file_name=file_name + ".xml";
            FileWriter out=new FileWriter(file_name, false); // don't append, overwrite
            try {
                out.write(XML_DEF + "\n");

                out.write("\n<testsuite " +
                        " failures=\"" + num_failures +
                        "\" errors=\"" + num_errors +
                        "\" skips=\"" + num_skips +
                        "\" name=\"" + clazz.getName());
                if(suffix != null)
                    out.write(" (" + suffix + ")");
                out.write("\" tests=\"" + results.size() + "\" time=\"" + (total_time / 1000.0) + "\">");

                out.write("\n<properties>");
                Properties props=System.getProperties();

                for(Map.Entry<Object,Object> tmp: props.entrySet()) {
                    out.write("\n    <property name=\"" + tmp.getKey() + "\"" +
                    " value=\"" + tmp.getValue() + "\"/>");
                }
                out.write("\n</properties>\n");


                for(ITestResult result: results) {
                    if(result == null)
                        continue;
                    long time=result.getEndMillis() - result.getStartMillis();
                    out.write("\n    <testcase classname=\"" + clazz.getName());
                    if(suffix != null)
                        out.write(" (" + suffix + ")");
                    out.write("\" name=\"" + result.getMethod().getMethodName() +
                            "\" time=\"" + (time/1000.0) + "\">");

                    Throwable ex=result.getThrowable();

                    switch(result.getStatus()) {
                        case ITestResult.SUCCESS:
                            case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
                                break;
                        case ITestResult.FAILURE:
                            writeFailure("failure", result.getMethod().getMethod(), ex, "exception", out);
                            break;
                        case ITestResult.SKIP:
                            writeFailure("error", result.getMethod().getMethod(), ex, "SKIPPED", out);
                            break;
                        default:
                            writeFailure("error", result.getMethod().getMethod(), ex, "exception", out);
                    }

                    out.write("\n</testcase>");
                }

                Tuple<ByteArrayOutputStream, ByteArrayOutputStream> stdout=outputs.get(clazz);
                if(stdout != null) {
                	ByteArrayOutputStream system_out=stdout.getVal1();
                	ByteArrayOutputStream system_err=stdout.getVal2();
                    writeOutput(out, system_out.toString(), 1);
                    out.write("\n");
                    writeOutput(out, system_err.toString(), 2);
                }

                out.write("\n</testsuite>\n");
            }
            finally {
                out.close();
            }
        }

    }

    private static String encode(String s) {
    	if (s==null) {
    		return null;
    	}
    	s = s.replaceAll(Pattern.quote("<"), "&lt;");
    	s = s.replaceAll(Pattern.quote(">"), "&gt;");
    	s = s.replaceAll(Pattern.quote("&"), "&amp;");
    	s = s.replaceAll(Pattern.quote("\""), "&quot;");
    	s = s.replaceAll(Pattern.quote("'"), "&apos;");
    	return s;
    }
    
    private static void writeOutput(FileWriter out, String s, int type) throws IOException {
        if(s != null && s.length() > 0) {
            out.write("\n<" + (type == 2? SYSTEM_ERR : SYSTEM_OUT) + "><" + CDATA + "\n");
            out.write(encode(s));
            out.write("\n]]>");
            out.write("\n</" + (type == 2? SYSTEM_ERR : SYSTEM_OUT) + ">");
        }
    }


    private static void writeFailure(String type, Method method, Throwable ex, String msg, FileWriter out) throws IOException {
        Test annotation=method.getAnnotation(Test.class);
        if(annotation != null && ex != null) {
            Class[] expected_exceptions=annotation.expectedExceptions();
            for(int i=0; i < expected_exceptions.length; i++) {
                Class expected_exception=expected_exceptions[i];
                if(expected_exception.equals(ex.getClass())) {
                    return;
                }
            }
        }

        out.write("\n<" + type + " type=\"");
        if(ex != null) {
            out.write(ex.getClass().getName() + "\" message=\"" + escape(ex.getMessage()) + "\">");
            printException(ex, out);
        }
        else
            out.write("exception\" message=\"" + msg + "\">");
        out.write("\n</" + type + ">");
    }

    private static void printException(Throwable ex, FileWriter out) throws IOException {
        if(ex == null) return;
        StackTraceElement[] stack_trace=ex.getStackTrace();
        out.write("\n<" + CDATA + "\n");
        out.write(ex.getClass().getName() + " \n");
        for(int i=0; i < stack_trace.length; i++) {
            StackTraceElement frame=stack_trace[i];
            try {
                out.write("at " + frame.toString() + " \n");
            }
            catch(IOException e) {
            }
        }
        out.write("\n]]>");
    }

    private static String escape(String message) {
    	return encode(message);
//        return message != null? message.replaceAll("<", LT).replaceAll(">", GT) : message;
    }

    private static long getTotalTime(List<ITestResult> results) {
        long start=0, stop=0;
        for(ITestResult result: results) {
            if(result == null) continue;
            long tmp_start=result.getStartMillis(), tmp_stop=result.getEndMillis();
            if(start == 0)
                start=tmp_start;
            else {
                start=Math.min(start, tmp_start);
            }

            if(stop == 0)
                stop=tmp_stop;
            else {
                stop=Math.max(stop, tmp_stop);
            }
        }
        return stop-start;
    }

    private static int getFailures(List<ITestResult> results) {
        int retval=0;
        for(ITestResult result: results) {
            if(result != null && result.getStatus() == ITestResult.FAILURE)
                retval++;
        }
        return retval;
    }

    private static int getErrors(List<ITestResult> results) {
        int retval=0;
        for(ITestResult result: results) {
            if(result != null
                    && result.getStatus() != ITestResult.SUCCESS
                    && result.getStatus() != ITestResult.SUCCESS_PERCENTAGE_FAILURE
                    && result.getStatus() != ITestResult.FAILURE)
                retval++;
        }
        return retval;
    }

    private static int getSkips(List<ITestResult> results) {
        int retval=0;
        for(ITestResult result: results) {
            if(result != null && result.getStatus() == ITestResult.SKIP)
                retval++;
        }
        return retval;
    }



}
