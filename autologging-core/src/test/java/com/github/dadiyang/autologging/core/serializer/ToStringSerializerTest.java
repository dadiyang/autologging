package com.github.dadiyang.autologging.core.serializer;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class ToStringSerializerTest {
    private ToStringSerializer toStringSerializer = new ToStringSerializer();

    @Test
    public void serialize() {
        String s = "xxx";
        String rs = toStringSerializer.serialize(s);
        System.out.println(rs);
        assertEquals(s, rs);

        rs = toStringSerializer.serialize(null);
        System.out.println(rs);
        assertEquals("", rs);

        int[] intArr = new int[]{1, 2, 3, 9, 5};
        rs = toStringSerializer.serialize(intArr);
        System.out.println(rs);
        assertEquals(Arrays.toString(intArr), rs);
    }
}