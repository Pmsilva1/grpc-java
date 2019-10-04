package com.codenotfound.grpc;

import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.codenotfound.grpc.helloworld.Greeting;
import com.codenotfound.grpc.helloworld.HelloWorldServiceGrpc;
import com.codenotfound.grpc.helloworld.Person;
import com.codenotfound.grpc.helloworld.protoQuery;
import com.codenotfound.grpc.helloworld.protoOwnerList;
import com.codenotfound.grpc.helloworld.protoXML;
import io.grpc.stub.StreamObserver;
import objects.*;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.util.concurrent.TimeUnit;

@GRpcService
public class HelloWorldServiceImpl
    extends HelloWorldServiceGrpc.HelloWorldServiceImplBase {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(HelloWorldServiceImpl.class);
  
  @Override
  public void sayHello(Person request, StreamObserver<Greeting> responseObserver){
      LOGGER.info("server received {}", request);
    String message = "Hello " + request.getFirstName() + " "
        + request.getLastName() + "!";
    Greeting greeting =
        Greeting.newBuilder().setMessage(message).build();
    LOGGER.info("server responded {}", greeting);

    responseObserver.onNext(greeting);
    responseObserver.onCompleted();
  }

  private boolean inited = false;
  private int ownerSize = 0, carSize = 0;
  private ArrayList<Owner> list_owners;

  private void init()
  {
    LOGGER.info("----------SERVER INIT-----------");
    inited = true;
    //LOGGER.info("Server going to read file");
    list_owners = new ArrayList<>();
    BufferedReader objReader = null;
    try {
        String strCurrentLine;

        objReader = new BufferedReader(new FileReader("input_owner.in"));
        strCurrentLine = objReader.readLine();
        int Lines = Integer.parseInt(strCurrentLine);
        while (Lines-- != 0 && ((strCurrentLine = objReader.readLine()) != null)) {
            String[] split = strCurrentLine.split("\\|");
            //System.out.println("ID: " + split[0] + "\tNome: " + split[1] + "\tTelefone: " + split[2] + "\tAddress: " + split[3]);  

            Owner owner = new Owner(Integer.parseInt(split[0]),split[1], Integer.parseInt(split[2]), split[3]);
            CarList l1 = new CarList();
            owner.setCarList(l1);
            list_owners.add(owner);
            ownerSize++;
        }
        
        objReader.close();

        objReader = new BufferedReader(new FileReader("input_car.in"));
        strCurrentLine = objReader.readLine();
        Lines = Integer.parseInt(strCurrentLine);
        while (Lines-- != 0 && ((strCurrentLine = objReader.readLine()) != null)) {
            String[] split = strCurrentLine.split("\\|");
            int fkey = Integer.parseInt(split[7]);
            
            //System.out.println("ID: " + split[0] + "\tBrand: " + split[1] + "\tModel: " + split[2] + "\tEngine Size: " + split[3] + "\tPower: " + split[4] + "\tConsumption: " + split[5] + "\tPlate: " + split[6] + "\tOwnerID: " + fkey );

            Car car1 = new Car(Integer.parseInt(split[0]),split[1],split[2], Integer.parseInt(split[3]), Integer.parseInt(split[4]), Float.parseFloat(split[5]), split[6]);
            
            int len=list_owners.size();
            for(int i=0; i<len; i++) {
                if (list_owners.get(i).getId() == fkey) {
                    list_owners.get(i).getCarList().getCar_array_list().add(car1);
                }
            }
            carSize++;
        }

        objReader.close();
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        try {
            if (objReader != null)
                objReader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //LOGGER.info("Server finished reading files");
    try {
      OwnerList owners = new OwnerList(list_owners);

      JAXBContext contextObj = JAXBContext.newInstance(OwnerList.class);

      Marshaller marshallerObj = contextObj.createMarshaller();
      marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

      marshallerObj.marshal(owners, new FileOutputStream("database.xml"));
    }catch (JAXBException | FileNotFoundException ex) {
      ex.printStackTrace();
    }
    LOGGER.info("Server Finished Setting Up");
  }

  @Override
  public void askOwners(protoQuery ids, StreamObserver<protoOwnerList> responseObserver)
  {
    
    if(inited == false)
      init();
    /*else
      LOGGER.info("----------SERVER ALREADY INIT-----------");*/

    int len = list_owners.size();
    protoOwnerList.Builder reply = protoOwnerList.newBuilder();

    long startTime = System.nanoTime();
    
    for(int id: ids.getIdList())
    {
      for(int i=0; i < len; i++) {
        if (list_owners.get(i).getId() == id) {
          
          protoOwnerList.Owner.Builder protoowner = protoOwnerList.Owner.newBuilder();
          protoowner.setId(list_owners.get(i).getId());
          protoowner.setName(list_owners.get(i).getName());
          protoowner.setPhone(list_owners.get(i).getPhone());
          protoowner.setAddress(list_owners.get(i).getAddress());
          
          for(Car car:  list_owners.get(i).getCarList().getCar_array_list())
          {
            protoOwnerList.Owner.Car.Builder protocar = protoOwnerList.Owner.Car.newBuilder();
            
            protocar.setId(car.getId());
            protocar.setPlate(car.getPlate());
            protocar.setBrand(car.getBrand());
            protocar.setPower(car.getPower());
            protocar.setConsumption(car.getConsumption());
            protocar.setEngineSize(car.getEngine_size());

            protoowner.addCar(protocar.build());
          }
          reply.addOwner(protoowner.build());
        }
      }
    }

    long endTime = System.nanoTime();
    LOGGER.info("ProtoBuffer took: {} Milliseconds", (double)(endTime - startTime)/1000000); //
    String orig = ids.toString();

    orig = orig.replaceAll("[\\n\\t id ]", "");
    orig = orig.replaceAll("[:]", "|");


    //LOGGER.info("data/{}-{}{}.out", ownerSize, carSize, orig);

    reply.setTime((double)(endTime - startTime)/1000000);
    reply.setFilename("data/"+ownerSize+"-"+carSize+orig+".out");

    responseObserver.onNext(reply.build());
    responseObserver.onCompleted();

/*

    OwnerList owners_reply = new OwnerList(reply);

    marshallerObj.marshal(owners_reply, new FileOutputStream("reply.xml"));
*/

  }
  
  public void askOwnersXML(protoQuery ids, StreamObserver<protoXML> responseObserver)
  {
    if(inited == false)
      init();
    /*else
      LOGGER.info("----------SERVER ALREADY INIT-----------");*/

    
    int len = list_owners.size();
    
    ArrayList<Owner> reply = new ArrayList<>();

    long startTime = System.nanoTime();

    for(int id: ids.getIdList())
    {
      for(int i=0; i < len; i++)
      {
        if (list_owners.get(i).getId() == id)
        {
            reply.add(list_owners.get(i));
        }
      }
    }

    OwnerList owners_reply = new OwnerList(reply);

    try
    {
      JAXBContext contextObj = JAXBContext.newInstance(OwnerList.class);
      Marshaller marshallerObj = contextObj.createMarshaller();
      marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshallerObj.marshal(owners_reply, new FileOutputStream("reply.xml"));
    }catch (JAXBException | FileNotFoundException ex) {
      ex.printStackTrace();
    }

    protoXML.Builder replyString = protoXML.newBuilder();
    replyString.setXml("goddammit.xml");
    
    long endTime = System.nanoTime();
    LOGGER.info("Fucking XML took: {} Miliseconds", (endTime - startTime)/1000000);

    responseObserver.onNext(replyString.build());
    responseObserver.onCompleted();
  }
}