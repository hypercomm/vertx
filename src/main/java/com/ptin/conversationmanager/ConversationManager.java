package com.ptin.conversationmanager;
/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */

import org.vertx.java.platform.Verticle;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import org.jboss.aerogear.unifiedpush.*;
import org.jboss.aerogear.unifiedpush.message.MessageResponseCallback;
import org.jboss.aerogear.unifiedpush.message.UnifiedMessage;

public class ConversationManager extends BusModBase implements Handler<Message<JsonObject>> {

	protected String address;
	protected String host;
	protected int port;
	public ConversationManager that;
	
	public void start() {
		System.out.println("Conversation Module started");
		super.start();
		that = this;
		address = getOptionalStringConfig("address", "ptin.conversationmanager");
		host = getOptionalStringConfig("host", "localhost");
		port = getOptionalIntConfig("port", 8080);
	
		eb.registerHandler(address, this);
	}
	
	
	public void handle(Message<JsonObject> message) {
		//System.out.println("MESSAGE: " + message.body().toString());
		String action = message.body().getString("action");
		if (action == null) {
			sendError(message, "action must be specified");
			return;
		}

		switch (action) {
			case "message":
				onMessage(message);
				break;
			case "notify":
				sendNotification(message);
				break;
			case "connected":
				retrieveMessages(message, action);
				break;
			case "addedParticipant":
				retrieveMessages(message, action);
				break;
			default:
				sendError(message, "Invalid action: " + action);
				return;
		}
		
	}
	
	private ArrayList<String> convertArray(JsonArray array){
		ArrayList<String> stringArray = new ArrayList<String>();
		for(int i = 0, count = array.size(); i< count; i++)	
			stringArray.add(array.get(i).toString());

		return stringArray;
	}
	
	private void sendNotification(Message<JsonObject> message) { 
		
		JsonObject body = message.body().getObject("body");
		
		String from = body.getString("from");
		JsonArray to = body.getArray("to");
		String contexId = body.getString("contextId");
		
		System.out.println("CONTEXTID: "+ contexId);
		System.out.println("FROM: " + from);
		System.out.println("TO: "+ convertArray(to).toString());
		
		JavaSender defaultJavaSender = new SenderClient("http://150.140.184.246:4441/unifiedpush-server-0.10.1/");
		ArrayList<String> variants = new ArrayList<String>();
		variants.add("c345abf3-e335-416d-bd52-93b87edb918f");
		variants.add("a09cb859-08b4-4d41-93a7-77131ee51b38");
		
		UnifiedMessage unifiedMessage = new UnifiedMessage.Builder()
        .pushApplicationId("1a4dc628-354f-48ed-aade-6406d2524c39")
        .masterSecret("02ef40ce-0fca-41a8-9142-8e7a0e1914b4")
        .variants(variants)
        .aliases(convertArray(to)) //to field
        .alert("{\"to\": \""+convertArray(to).toString()+"\", \"from\":\""+ from +"\", \"contextId\":\"" + contexId + "\"}")
        //.sound("default")
        .build();
		
		defaultJavaSender.send(unifiedMessage, callback);
		
				
	}
	
	MessageResponseCallback callback = new MessageResponseCallback() {

        @Override
        public void onComplete(int statusCode) {
          //do cool stuff
        	System.out.println("statusCode: " + statusCode);
        }

        @Override
        public void onError(Throwable throwable) {
          //bring out the bad news
        	System.out.println("statusCode: " + throwable.getMessage().toString());
        }
    };
    
    private void onMessage(Message<JsonObject> message) {
		//Registar o utilizador com as especificacoes mandadas
		//necessario pegar nos servicos que sao subscritos para depois retornar a biblioteca a ser usada e assim
    	final Message<JsonObject> msg = message;
    	
    	//forward message to the users if they are online they will answer
    	forwardMessage(msg);
		
		if(message.body().getObject("body").getString("type").toString().equals("invitation")){
			sendNotification(msg);
			
			System.out.println("OBJECT: " + message.body().toString());
			Handler<Message<JsonObject>> publishHandler = new Handler<Message<JsonObject>>() {
			    public void handle(Message<JsonObject> message) {
			        System.out.println("I received a message " + message.body().toString());
			    }
			};
			
			eb.registerHandler(message.body().getObject("body").getString("contextId"), publishHandler);
			
		}
		
		JsonObject data = new JsonObject();
		data.putString("action", "save");
		data.putString("collection", "messages");
		data.putObject("document", message.body().getObject("body"));
		
		eb.send("test.my_persistor", data);
		
			
	}
	
	
	
	private void forwardMessage(Message<JsonObject> message){
		
		//System.out.println("FORWARD MESSAGE: "+ message.body().toString() + " \n");
		
		JsonObject body = message.body().getObject("body");
		if(body.getArray("to").get(0) != null){
			if(message.body().getObject("body").getString("type").equals("invitation")){
				System.out.println("FORWARD MESSAGE: "+ message.body().toString() + " \n");
			}
			ArrayList<String> peers = convertArray(body.getArray("to"));
			for(int i = 0; i < peers.size(); i++){
				System.out.println("\n Peer: " + peers.get(i).toString() + " \n");
				eb.send(peers.get(i), body);
			}
		}
		else {
			eb.publish(body.getString("contextId"), body);
		}
		
		
		
	}
	
	
	private void saveMessage(Message<JsonObject> message){
		System.out.println("Save Message: "+  message.body().toString());
		final Message<JsonObject> mensagem = message;
		JsonObject putData = new JsonObject();
		putData.putString("action", "put");
		//putData.putString("sessionId", message.body().getString("sessionId")); <- how to get the session ID
		
		JsonObject messageData = new JsonObject();
		//messageData.putString("contextId", contextId);
		//messageData.putString("message", msg.body().getObject("body()").toString());
		putData.putObject("data", messageData);
		
		eb.send("test.session_manager", 
				putData,
				new Handler<Message<JsonObject>>() {
					public void handle(Message<JsonObject> message) {
						
						
					}	
				});
	}
	
	private void retrieveMessages(Message<JsonObject> message, String action){
		
		System.out.println("  Message from contextId: "+  message.body().toString());
		
		final Message<JsonObject> mensagem = message;
		
		JsonObject getData = new JsonObject();
		getData.putString("action", "find");
		getData.putString("collection", "messages");
		JsonArray rtcIdentities = new JsonArray();
		rtcIdentities.add(message.body().getString("to"));
		
		JsonObject match = new JsonObject();
		//match.putArray("to", rtcIdentities);
		match.putString("contextId", message.body().getString("contextId"));
	
		
		getData.putObject("matcher", match);
		
		eb.send("test.my_persistor", getData, new Handler<Message<JsonObject>>() {
			public void handle(Message<JsonObject> message) {
				//System.out.println("Message: " +  message.body().toString());
				JsonObject resposta2 = new JsonObject();
				resposta2.putString("status","ok");
				resposta2.putArray("message", message.body().getArray("results"));
				mensagem.reply(resposta2);
				sendOK(message,resposta2);
			}	
		});
	}
	
	
  public void stop() {
  }
  
}

