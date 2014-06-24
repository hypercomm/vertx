WONDER Vertx Messaging Server 
=====

The Vertx Conversation Manager module enables [vert.x](http://vertx.io/) to be used as Messaging Server of [WONDER WebRTC Framework](http://hypercomm.github.io/wonder/).

Just follow these steps to have it running:

1. Install [vert.x](http://vertx.io/install.html).
2. Add vertx bin folder to your classpath environment variable
3. Create VERTX_MODS environment variable in your system, that should point to the directory where you have all vertx modules. [see here](http://vertx.io/mods_manual.html#system);
4. Copy the folder [com.ptin~conversationmanager~1.0.0-SNAPSHOT](https://github.com/hypercomm/vertx/tree/master/build/mods) to VERTX_MODS directory created in the previous step. 
5. Copy the [server.java](https://github.com/hypercomm/vertx/tree/master/server.java) file to your computer in a folder of your choice.
6. Then execute ``vertx run server.java``.
