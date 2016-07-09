package fourcasters.perfid;

import java.text.DecimalFormat;
import java.util.Random;

/**
 * VM  args: 
 * -XX:+UnlockDiagnosticVMOptions
 * -XX:+TraceClassLoading
 * -XX:+LogCompilation
 * -XX:+PrintAssembly
 *
 */
public class App 
{
	private static final int SIZE = 30000;
	private static final long rangeMin = -1024*1024*1024*1023;
	private static final long rangeMax = rangeMin*-1;

    public static void main( String[] args )
    {
    	//TODO evenly distribute samples by order of magnitude.
    	//TODO use jmh.
    	//TODO allow speedy formatter to be customised for number of decimals and trailing zeroes.
    	//TODO Explore n^round(log_10(n)) rather than the long list of if/elseif/else
    	
        final Random r = new Random(System.currentTimeMillis());
        final StringBuilder sb = new StringBuilder(512);
        final DecimalFormat df = new DecimalFormat();

        
        double[] records = new double[SIZE];
        //warmup
        for (int i = 0; i < SIZE; i++) {
        	double d = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
        	records[i] = d;
        	SpeedyDoubleFormatter.append(sb, d);
        	df.format(d);
        }
        //end warmup
        sb.setLength(0);
        long start = System.currentTimeMillis();
        for (int i = 0; i < SIZE; i++) {
        	sb.setLength(0);
        	SpeedyDoubleFormatter.append(sb, records[i]);
        	if (i % 1000 == 0) {
        		System.out.println(sb.toString());
        	}
        }
        System.out.println("speedy time = " + (System.currentTimeMillis() - start));
        
        start = System.currentTimeMillis();
        for (int i = 0; i < SIZE; i++) {
        	String s = df.format(records[i]);
        	if (i % 1000 == 0) {
        		System.out.println(s);
        	}
        }
        System.out.println("normy time = " + (System.currentTimeMillis() - start));
        
    }
}
