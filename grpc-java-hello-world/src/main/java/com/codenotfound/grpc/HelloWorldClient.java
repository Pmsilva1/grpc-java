package com.codenotfound.grpc;

import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.codenotfound.grpc.helloworld.Greeting;
import com.codenotfound.grpc.helloworld.HelloWorldServiceGrpc;
import com.codenotfound.grpc.helloworld.Person;
import com.codenotfound.grpc.helloworld.protoQuery;
import com.codenotfound.grpc.helloworld.protoOwnerList;
import com.codenotfound.grpc.helloworld.protoXML;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class HelloWorldClient {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(HelloWorldClient.class);

  private HelloWorldServiceGrpc.HelloWorldServiceBlockingStub helloWorldServiceBlockingStub;

  @PostConstruct
  private void init() {
    ManagedChannel managedChannel = ManagedChannelBuilder
        .forAddress("localhost", 6565).usePlaintext().build();

    helloWorldServiceBlockingStub =
        HelloWorldServiceGrpc.newBlockingStub(managedChannel);
  }

  public String sayHello(String firstName, String lastName) {
    Person person = Person.newBuilder().setFirstName(firstName)
        .setLastName(lastName).build();
    LOGGER.info("client sending {}", person);

    Greeting greeting =
        helloWorldServiceBlockingStub.sayHello(person);
    LOGGER.info("client received {}", greeting);

    return greeting.getMessage();
  }

  public String askOwners(String request) {
  
    //LOGGER.info("client received: " + request);

    String[] split = request.split(" ");
    int size = split.length;
    protoQuery.Builder query = protoQuery.newBuilder();
    
    for(int i = 0; i < size; i++)
      query.addId(Integer.parseInt(split[i]));

    protoOwnerList reply = helloWorldServiceBlockingStub.askOwners(query.build());
    long startTime = System.nanoTime();

    BufferedWriter bw = null;
    try 
    {
      File file = new File(reply.getFilename().toString());
      
      if (!file.getParentFile().exists())
        file.getParentFile().mkdirs();
      if (!file.exists())
        file.createNewFile();

        FileWriter fw = new FileWriter(file, true);
	      bw = new BufferedWriter(fw);
        bw.write("Server took: " + reply.getTime() + " milliseconds\n");
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    finally
    { 
        try
        {
          if(bw!=null)
            bw.close();
        }catch(Exception ex){
            System.out.println("Error in closing the BufferedWriter"+ex);
        }
    }

    //LOGGER.info("client received from server:\b" + reply.toString());

    return "worked";
  }

  public String askOwnersXML(String request) {
  
    //LOGGER.info("client received: " + request);

    String[] split = request.split(" ");
    int size = split.length;
    protoQuery.Builder query = protoQuery.newBuilder();
    
    for(int i = 0; i < size; i++)
      query.addId(Integer.parseInt(split[i]));

    protoXML reply = helloWorldServiceBlockingStub.askOwnersXML(query.build());

    //LOGGER.info("client received from server:\b" + reply.toString());

    return "worked";
  }
}
