package com.xucheng;

import org.junit.jupiter.api.Test;

public class CommonTest {

    @Test
    public void testDir(){
        String dir = System.getProperty("user.dir");
        System.out.println(dir);
    }

}
