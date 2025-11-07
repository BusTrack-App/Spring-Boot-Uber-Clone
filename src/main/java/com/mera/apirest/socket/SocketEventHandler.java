package com.mera.apirest.socket;


import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.stereotype.Component;

@Component
public class SocketEventHandler {

    public SocketEventHandler(SocketIOServer server) {
        server.addConnectListener(client -> {
            System.out.println("Cliente Conectado: " + client.getSessionId());
        });

        server.addDisconnectListener(client -> {
            System.out.println("Cliente Desconectado: " + client.getSessionId());
        });

        server.addEventListener("message", String.class, (client, data, ackSender) -> {
            System.out.println("Mensaje recibido: " + data);
            client.sendEvent("message", "Data" + data);
        });
    }




}
