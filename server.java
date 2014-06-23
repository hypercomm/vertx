import java.util.*;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.platform.Verticle;
import org.vertx.java.core.eventbus.EventBus;
import java.net.*;

  public class server extends Verticle {
      public void start() {
                try{
                        JsonArray arrayinbound = new JsonArray("[{}]");
                        JsonArray arrayoutbound = new JsonArray("[{}]");
			  //configuracao da variavel para modulo webserver
                        JsonObject configWebServer = new JsonObject();
                        configWebServer.putString("host", "Inet4Address.getLocalHost().getHostAddress()");
                        //configWebServer.putString("host", "192.168.1.14");
                        configWebServer.putNumber("port", 4443);
                        configWebServer.putArray("inbound_permitted", arrayinbound);
                        configWebServer.putArray("outbound_permitted", arrayoutbound);
                        configWebServer.putBoolean("bridge", true);
                        //configWebServer.putBoolean("ssl", true);
                        //configWebServer.putString("key_store_password", "webrtc");
			             JsonObject configMongo = new JsonObject();
                        configMongo.putString("address", "test.my_persistor");
                        configMongo.putString("db_name", "webrtc");

                        
                        container.deployModule("io.vertx~mod-mongo-persistor~2.1.0", configMongo);
            			

			//configNotify.putString("address","vertx.notify");
                        container.deployModule("io.vertx~mod-web-server~2.0.0-final", configWebServer);
                        container.deployModule("com.ptin~conversationmanager~1.0.0-SNAPSHOT");
                } catch (Exception ex) {
                        System.out.println("error: "+ex.toString());
                } 

        }
}

