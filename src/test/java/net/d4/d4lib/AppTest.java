package net.d4.d4lib;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.d4.d4lib.utils.JsonUtil;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        Map<String,String> map = new HashMap<String,String>();
        map.put("m1","v111111");
        map.put("m2","v222222");
        
        String str =JsonUtil.toJSONString(map);
        System.out.println(str);
        assert(true);
    }
}
