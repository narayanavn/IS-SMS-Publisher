package com.harlandclarke.sms.publisher;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Publisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(Publisher.class);

  private String clientId;
  private Connection connection;
  private Session session;
  private MessageProducer messageProducer;

  public void create(String clientId, String topicName) throws JMSException {
    this.clientId = clientId;
    
    // create a Connection Factory
//    ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://sa1x-was-p1:9416"); // temp forwarder to esb-p1:61616
    ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://sa1x-amq-u1:61616");
 //  ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://sa1x-esb-p1:61616");
//    ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://sa1x-esb-d1:61616");
 // ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://sa1x-was-d1:9416"); // forwarder to esb-u1:61616

    // create a Connection
    connection = connectionFactory.createConnection();
    //connection.setClientID(clientId);

    // create a Session
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

    // create the Topic to which messages will be sent
    Topic topic = session.createTopic(topicName);

    // create a MessageProducer for sending messages
    messageProducer = session.createProducer(topic);
  }

  public void closeConnection() throws JMSException {
    connection.close();
  }

  public void sendOrderConfirmation(String tr, String acct, String phoneNumber, String quantity, String style, String deliveryMethod, String totalAmount, String date, String appname) throws JMSException, JSONException {
    // create a JMS TextMessage
    JSONObject message = new JSONObject(); 
    message.put("trNumber", tr);
    message.put("acctNumber", acct);
    message.put("phoneNumber", phoneNumber);
    message.put("quantity", quantity);
    message.put("productDescription", style);
    message.put("orderDate", date);
    message.put("deliveryMethod", deliveryMethod);
    message.put("totalAmount", totalAmount);
    message.put("appName", appname);
    TextMessage textMessage = session.createTextMessage(message.toString());

    // send the message to the topic destination
    messageProducer.send(textMessage);

    LOGGER.debug(clientId + ": sent message for order confirmation with text='{}'", message.toString());
  }
  
  public void sendShippingConfirmation(String tr, String acct, String phoneNumber, String quantity, String product, String deliveryMethod, String shipDate, String orderDate, String trackingNumber, String trackingURL) throws JMSException, JSONException {
    // create a JMS TextMessage
    JSONObject message = new JSONObject(); 
    message.put("trNumber", tr);
    message.put("acctNumber", acct);
    message.put("phoneNumber", phoneNumber);
    message.put("quantity", quantity);
    message.put("product", product);
    message.put("deliveryMethod", deliveryMethod);
    message.put("shipDate", shipDate);
    message.put("orderDate", orderDate);
    message.put("trackingNumber", trackingNumber);
    message.put("trackingLink", trackingURL);
    TextMessage textMessage = session.createTextMessage(message.toString());

    // send the message to the topic destination
    messageProducer.send(textMessage);

    LOGGER.debug(clientId + ": sent message for shipping confirmation with text='{}'", message.toString());
  }

  public static void main(String[] args) {
	System.out.println(" main method started ");

    Publisher pub = new Publisher();
    try {
      // this is for sending a sms for order confirmation.  
      //Note java orderconfirmation hard coded IVR.  Cobol order confirmation hard coded CCS.  so setting here is ignored?
    	System.out.println(" sending orderConfirmation");

    	pub.create("", "orderConfirmation");
       pub.sendOrderConfirmation("999999991", "70202", "2106831661", "200", "DUP CSTM COMERICA", "Check Protect", "43.27", "10/08/2020", "CCS");
      pub.closeConnection();
      
  	System.out.println(" orderConfirmation is sent mostly ");
  	
      pub = new Publisher();
      // this is for sending a sms for shipping confirmation.  Order date must match order to be picked up correctly?  no.  this works with different order date, rt, acct can all be different.
      //                              tr          acct       phnum      qty      product             delivmeth       shipdate      orddate       tracknum     trackurl
      pub.create("", "shippingConfirmation");
      pub.sendShippingConfirmation("999999991", "70202", "2106831661", "200", "DUP CSTM COMERICA", "Check Protect", "08/01/2017", "10/08/2020", "123456789", "http://blah.blah");
      pub.closeConnection();

    	System.out.println(" shippingConfirmation is sent mostly ");

    } catch (Exception e) {
      e.printStackTrace();
  	System.out.println(" Exception occured "+e.getMessage());

    }

  }

}
