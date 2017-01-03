/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamteamjson;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;

/**
 *
 * @author Buhrkall
 */


public class DreamTeamJSON {
    
    
    static final String LISTENING_QUEUE_NAME = "DreamTeamJSONQueue"; 
    static final String SENDING_QUEUE_NAME = "NormalizerQueue"; 
    static Gson gson = new Gson();


    private static String message;
    

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("datdb.cphbusiness.dk");
    factory.setUsername("Dreamteam");
    factory.setPassword("bastian");
    Connection connection = factory.newConnection();
    Channel listeningChannel = connection.createChannel();
    Channel sendingChannel = connection.createChannel();


    listeningChannel.queueDeclare(LISTENING_QUEUE_NAME, false, false, false, null);
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    Consumer consumer = new DefaultConsumer(listeningChannel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {
         message = new String(body, "UTF-8");
         System.out.println(" [x] Received '" + message + "'");
         
         Message m = gson.fromJson(message,Message.class);
              System.out.println(m.toString());
              
       double interestRate = 14;
        
       if(m.creditScore > 600){
       interestRate -= 4.5;
       }
       else if (m.creditScore < 601 && m.creditScore > 500){
       interestRate -= 2.7;
       }
       else if(m.creditScore < 501 && m.creditScore > 400){
       interestRate -=0.9;
       }
       
       
       int durationCut = m.loanDuration / 360;
       
       interestRate -= (durationCut * 0.18);
       
       double amountCut = m.loanAmount / 100000;
       
       interestRate -= (amountCut * 0.18);      
              
              // Insert bank logic
   
       
       
       String loanResponse = "{\"interestRate\":" + interestRate +",\"ssn\":" + m.ssn + "}";
   
        sendingChannel.queueDeclare(SENDING_QUEUE_NAME, false, false, false, null);
        sendingChannel.basicPublish("", SENDING_QUEUE_NAME, null, loanResponse.getBytes());     
      }
    };
    listeningChannel.basicConsume(LISTENING_QUEUE_NAME, true, consumer);
        
        
    }
    
}
