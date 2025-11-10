package com.mera.apirest.socket;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.mera.apirest.dto.socket.*;
import org.springframework.stereotype.Component;

@Component
public class SocketEventHandler {

    private final SocketIOServer server;

    public SocketEventHandler(SocketIOServer server) {
        this.server = server;
        setupEventListeners();
    }

    private void setupEventListeners() {

        // Conexión de cliente
        server.addConnectListener(client -> {
            System.out.println("Cliente CONECTADO | SessionId: " + client.getSessionId());
        });

        // Desconexión de cliente
        server.addDisconnectListener(client -> {
            System.out.println("Cliente DESCONECTADO | SessionId: " + client.getSessionId());
            SocketIdResponse response = new SocketIdResponse();
            response.setIdSocket(client.getSessionId().toString());
            System.out.println("Enviando evento 'driver_disconnected' | Payload: " + response);
            server.getBroadcastOperations().sendEvent("driver_disconnected", response);
        });

        // Evento: message
        server.addEventListener("message", MessageDTO.class, (client, data, ackSender) -> {
            System.out.println("Evento 'message' RECIBIDO | SessionId: " + client.getSessionId() +
                    " | Mensaje: " + data.getNewMessage());

            String responseMessage = "Hola desde el servidor: " + data.getNewMessage();
            System.out.println("Enviando 'new_message_response' a todos | Respuesta: " + responseMessage);
            server.getBroadcastOperations().sendEvent("new_message_response", responseMessage);
        });

        // Evento: change_driver_position
        server.addEventListener("change_driver_position", DriverPositionDTO.class, (client, data, ackSender) -> {
            System.out.println("Evento 'change_driver_position' RECIBIDO | SessionId: " + client.getSessionId() +
                    " | Datos: id=" + data.getId() + ", lat=" + data.getLat() + ", lng=" + data.getLng());

            DriverPositionDTO position = new DriverPositionDTO();
            position.setIdSocket(client.getSessionId().toString());
            position.setId(data.getId());
            position.setLat(data.getLat());
            position.setLng(data.getLng());

            System.out.println("Broadcasting 'new_driver_position' | Payload: " + position);
            server.getBroadcastOperations().sendEvent("new_driver_position", position);
        });

        // Evento: new_client_request
        server.addEventListener("new_client_request", NewClientRequestDTO.class, (client, data, ackSender) -> {
            System.out.println("Evento 'new_client_request' RECIBIDO | SessionId: " + client.getSessionId() +
                    " | IdClientRequest: " + data.getIdClientRequest());

            NewClientRequestDTO response = new NewClientRequestDTO();
            response.setIdClientRequest(data.getIdClientRequest());
            response.setIdSocket(client.getSessionId().toString());

            System.out.println("Broadcasting 'created_client_request' | Payload: " + response);
            server.getBroadcastOperations().sendEvent("created_client_request", response);
        });

        // Evento: new_driver_offer
        server.addEventListener("new_driver_offer", NewDriverOfferDTO.class, (client, data, ackSender) -> {
            System.out.println("OFERTA RECIBIDA DEL CONDUCTOR | SessionId: " + client.getSessionId() +
                    " | idClientRequest: " + data.getIdClientRequest());

            NewDriverOfferDTO response = new NewDriverOfferDTO();
            response.setIdClientRequest(data.getIdClientRequest());
            response.setIdSocket(client.getSessionId().toString());

            String eventName = "created_driver_offer/" + data.getIdClientRequest();

            System.out.println("ENVIANDO OFERTA A CLIENTE | Evento: " + eventName +
                    " | idClientRequest: " + data.getIdClientRequest() +
                    " | ConductorSocket: " + client.getSessionId());


            server.getBroadcastOperations().sendEvent(eventName, response);
        });
    }
}