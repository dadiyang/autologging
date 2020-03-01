package com.github.dadiyang.autologging.test.cases;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.github.dadiyang.autologging.test.user.User;
import com.github.dadiyang.autologging.test.user.UserController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.NestedServletException;

import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@RunWith(SpringRunner.class)
public class UserControllerTest {
    @Autowired
    private UserController userController;
    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setMessageConverters(new FastJsonHttpMessageConverter())
                .build();
    }

    @Test(expected = NestedServletException.class)
    public void getById() throws Exception {
        Long id = ThreadLocalRandom.current().nextLong(1000000L);
        User user = new User(id, "张三");
        MockHttpServletRequestBuilder getRequestBuilder =
                MockMvcRequestBuilders.get("/user/getById")
                        .param("id", String.valueOf(id));
        mockMvc.perform(getRequestBuilder).andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(JSON.toJSONString(user)));

        MockHttpServletRequestBuilder postRequestBuilder =
                MockMvcRequestBuilders.get("/user/updateById").contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(user));
        mockMvc.perform(postRequestBuilder)
                .andExpect(status().is5xxServerError());
    }

}