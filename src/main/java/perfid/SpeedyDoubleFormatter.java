package perfid;

import java.util.Arrays;

public class SpeedyDoubleFormatter {

	private static final long doubleSignMask  = 0x8000000000000000L;
	private static final long doubleExpMask   = 0x7ff0000000000000L;
	private static final int doubleExpShift = 52;
	private static final int doubleExpBias = 1023;
	private static final char[] charForDigit = {
			'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f','g','h',
			'i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'
	};

	public static final char[] DOUBLE_ZERO  = {'0','.','0'};
	public static final char[] DOUBLE_ZERO2 = {'0','.','0','0'};
	public static final char[] DOUBLE_ZERO0 = {'0','.'};
	public static final char[] DOT_ZERO = {'.','0'};
	public static final double[] d_magnitudes = new double[323+308+1];
	public static final double[] i_d_magnitudes = new double[323+308+1];
	public static final char[][] ZEROS = new char[323+308+1][];

	static {
		for (int i = -323; i <= 308; i++) {
			int offset = i+323;
			d_magnitudes[offset] = Math.pow(10, i);
			i_d_magnitudes[offset] = 1./d_magnitudes[offset];
			
			final char[] chars = new char[offset+1];
			for (int j = 0; j < offset+1; j++) {
				chars[j] = '0';
			}
			ZEROS[offset] = chars;
		}
	}

	public static String getMagnitudes() {
		return Arrays.toString(d_magnitudes);
	}


	public static final void append(StringBuilder sb, double d) {
		try {
			if (d == 0.0) {
				if ((Double.doubleToLongBits(d) & doubleSignMask) != 0) {
					sb.append('-');
				}
				sb.append(DOUBLE_ZERO);
			}
			else { 
				if (d < 0) {
					sb.append('-');
					d = -d;
				}
				//handle 0.001 up to 10000000 separately, without exponents
				if (d >= 0.001D && d < 0.01D)
				{
					long i = (long) (d * 1E12D);
					i = i%100 >= 50 ? (i/100) + 1 : i/100;
					sb.append(DOUBLE_ZERO2);
					appendFractDigits(sb, i,-1);
				}
				else if (d >= 0.01D && d < 0.1D)
				{
					long i = (long) (d * 1E11D);
					i = i%100 >= 50 ? (i/100) + 1 : i/100;
					sb.append(DOUBLE_ZERO);
					appendFractDigits(sb, i,-1);
				}
				else if (d >= 0.1D && d < 1D)
				{
					long i = (long) (d * 1E10D);
					i = i%100 >= 50 ? (i/100) + 1 : i/100;
					sb.append(DOUBLE_ZERO0);
					appendFractDigits(sb, i,-1);
				}
				else if (d >= 1D && d < 10D)
				{
					long i = (long) (d * 1E9D);
					i = i%100 >= 50 ? (i/100) + 1 : i/100;
					appendFractDigits(sb, i,1);
				}
				else if (d >= 10D && d < 100D)
				{
					long i = (long) (d * 1E8D);
					i = i%100 >= 50 ? (i/100) + 1 : i/100;
					appendFractDigits(sb, i,2);
				}
				else if (d >= 100D && d < 1000D)
				{
					long i = (long) (d * 1E7D);
					i = i%100 >= 50 ? (i/100) + 1 : i/100;
					appendFractDigits(sb, i,3);
				}
				else if (d >= 1000D && d < 10000D)
				{
					long i = (long) (d * 1E6D);
					i = i%100 >= 50 ? (i/100) + 1 : i/100;
					appendFractDigits(sb, i,4);
				}
				else if (d >= 10000D && d < 100000D)
				{
					long i = (long) (d * 1E5D);
					i = i%100 >= 50 ? (i/100) + 1 : i/100;
					appendFractDigits(sb, i,5);
				}
				else if (d >= 100000D && d < 1000000D)
				{
					long i = (long) (d * 1E4D);
					i = i%100 >= 50 ? (i/100) + 1 : i/100;
					appendFractDigits(sb, i,6);
				}
				else if (d >= 1000000D && d < 10000000D)
				{
					long i = (long) (d * 1E3D);
					i = i%100 >= 50 ? (i/100) + 1 : i/100;
					appendFractDigits(sb, i,7);
				}
				else
				{
					//Otherwise the number has an exponent
					int magnitude = magnitude(d);
					long i;
					if (magnitude < -305)
						i = (long) (d*1E18 / d_magnitudes[magnitude + 324]);
					else
						i = (long) (d / d_magnitudes[magnitude + 323 - 17]);
					i = i%10 >= 5 ? (i/10) + 1 : i/10;
					appendFractDigits(sb, i, 1);
					sb.append('E');
					sb.append(magnitude);

				} 
			}

		}
		catch (Exception e) { //+inf, -inf, NaN
			//TODO Check infs and NaNs
			throw new RuntimeException("Unable to parse " + d, e);
		}
	}

	private static int magnitude(double d, long doubleToLongBits) {
		int magnitude =
				(int) ((((doubleToLongBits & doubleExpMask) >> doubleExpShift)
						- doubleExpBias) * 0.301029995663981);
		if (magnitude < -323)
			magnitude = -323;
		else if (magnitude > 308)
			magnitude = 308;
		if (d >= d_magnitudes[magnitude+323])
		{
			while(magnitude < 309 && d >= d_magnitudes[magnitude+323])
				magnitude++;
			magnitude--;
			return magnitude;
		}
		else
		{
			while(magnitude > -324 && d < d_magnitudes[magnitude+323])
				magnitude--;
			return magnitude;
		}
	} 

	private static void appendFractDigits(StringBuilder sb, long i, int decimalOffset)
	{
		int mag = magnitude(i);
		long c;
		while ( i > 0 )
		{
			long exp = (long)d_magnitudes[mag+323];
			double i_exp = i_d_magnitudes[mag+323];
			c = (long) (i*i_exp);
			sb.append(charForDigit[(int) c]);
			decimalOffset--;
			if (decimalOffset == 0)
				sb.append('.'); //change to use international character
			c *= exp;
			if ( c <= i)
				i -= c;
			mag--;
		}
		if (i != 0)
			sb.append(charForDigit[(int) i]);
		else if (decimalOffset > 0)
		{
			sb.append(ZEROS[decimalOffset]); //ZEROS[n] is a char array of n 0's
			decimalOffset = 1;
		}
		decimalOffset--;
		if (decimalOffset == 0)
			sb.append(DOT_ZERO);
		else if (decimalOffset == -1)
			sb.append('0');
	}

	private static int magnitude(double d)
	{
		return magnitude(d,Double.doubleToLongBits(d));
	} 
}
