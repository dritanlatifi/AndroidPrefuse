package prefuse.util.io;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class LogTime
{
	static Map<String, Long> time = new HashMap<String, Long>();
	protected static void start(String key)
	{
//		Log.d("PERFORMANCE", " ++ start " + key);
		Long startTime = Long.valueOf(System.currentTimeMillis()) ;
		time.put(key, startTime);
	}
	
	protected static void stop(String key)
	{
		long end = System.currentTimeMillis();
		end = end - (long) time.get(key); 
		Log.d("PERFORMANCE", " ++ end " + key + ": " + end + " ms");
	}
	
	public static void log(String key)
	{
		if(time.containsKey(key))
		{
			stop(key);
			time.remove(key);
		}
		else
			start(key);
	}
	
}
