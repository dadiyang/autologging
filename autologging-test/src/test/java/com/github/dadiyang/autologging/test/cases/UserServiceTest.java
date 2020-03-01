package com.github.dadiyang.autologging.test.cases;

import com.github.dadiyang.autologging.test.user.User;
import com.github.dadiyang.autologging.test.user.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @Test
    public void getById() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            Long randomId = ThreadLocalRandom.current().nextLong(10000000L);
            User user = userService.getById(randomId);
            assertEquals(randomId, user.getId());
            try {
                userService.updateById(user);
            } catch (Exception ignored) {
            }
        }
        System.out.println("all rt: " + (System.currentTimeMillis() - start));
    }
}
