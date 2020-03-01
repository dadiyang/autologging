package com.github.dadiyang.autologging.aop.serializer;

import com.github.dadiyang.autologging.core.configuration.AutoLogConfig;
import com.github.dadiyang.autologging.core.serializer.FastJsonSerializer;
import com.github.dadiyang.autologging.core.serializer.Serializer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.assertEquals;

public class SerializerWrapperTest {
    private SerializerWrapper serializerWrapper;
    private Serializer serializer;

    @Before
    public void setUp() throws Exception {
        serializer = new FastJsonSerializer();
        serializerWrapper = new SerializerWrapper(serializer, new AutoLogConfig.SerializeConfig());
    }

    @Test
    public void argsToString() {
        // null 和 空数组
        String rs = serializerWrapper.argsToString(null, true);
        assertEquals("", rs);
        rs = serializerWrapper.argsToString(new Object[0], true);
        assertEquals("", rs);

        Object[] args = new Object[]{1, "xx", new Object()};
        rs = serializerWrapper.argsToString(args, true);
        assertEquals(serializer.serialize(args), rs);

        // 存在不支持序列化的对象
        args = new Object[]{1, "xx", new MockHttpServletRequest()};
        rs = serializerWrapper.argsToString(args, true);
        System.out.println(rs);
        args = new Object[]{1, "xx", "unsupported: " + args[2].toString()};
        assertEquals(serializer.serialize(args), rs);
    }

    @Test
    public void resultToString() {

    }
}