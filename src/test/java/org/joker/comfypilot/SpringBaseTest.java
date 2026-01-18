package org.joker.comfypilot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
public class SpringBaseTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void test() {
        System.out.println(applicationContext);
    }

}
