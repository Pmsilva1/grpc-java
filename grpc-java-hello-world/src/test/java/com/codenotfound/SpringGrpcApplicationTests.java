package com.codenotfound;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.codenotfound.grpc.HelloWorldClient;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringGrpcApplicationTests {

  @Autowired
  private HelloWorldClient helloWorldClient;

  @Test
  public void testSayHello() {
    /*assertThat(helloWorldClient.sayHello("John", "Doe"))
        .isEqualTo("Hello John Doe!");*/
        helloWorldClient.askOwners("1 3 5 6 7 10 15");
        helloWorldClient.askOwners("2 4 8 9 11 12 13");
        helloWorldClient.askOwners("14 20 15 21 16 22 17");
        helloWorldClient.askOwners("18 23 19 24 25 30 26");




        //helloWorldClient.askOwnersXML("5 3 10 15 20 35");

  }
}
