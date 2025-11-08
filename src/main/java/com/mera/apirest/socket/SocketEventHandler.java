package com.mera.apirest.socket;


import com.corundumstudio.socketio.SocketIOServer;
import com.mera.apirest.dto.socket.DriverPositionDTO;
import com.mera.apirest.dto.socket.MessageDTO;
import com.mera.apirest.dto.socket.SocketIdResponse;
import org.springframework.stereotype.Component;

@Component
public class SocketEventHandler {

    public SocketEventHandler(SocketIOServer server) {
        server.addConnectListener(client -> {
            System.out.println("Cliente Conectado: " + client.getSessionId());
        });

        server.addDisconnectListener(client -> {
            System.out.println("Cliente Desconectado: " + client.getSessionId());
            SocketIdResponse response = new SocketIdResponse();
            response.setIdSocket(client.getSessionId().toString());
            server.getBroadcastOperations().sendEvent("driver_disconnected", response);
        });

        server.addEventListener("message", MessageDTO.class, (client, data, ackSender) -> {
            System.out.println("Mensaje recibido: " + data.getNewMessage());
            server.getBroadcastOperations().sendEvent("new_message_response", "Hola desde el servidor: " + data);
        });

        server.addEventListener("change_driver_position", DriverPositionDTO.class, (client, data, ackSender) -> {

            DriverPositionDTO position = new DriverPositionDTO();
            position.setIdSocket(client.getSessionId().toString());
            position.setId(data.getId());
            position.setLat(data.getLat());
            position.setLng(data.getLng());
            System.out.println("Cambio de Posicion: " + position);

            server.getBroadcastOperations().sendEvent("new_driver_position", position);
        });
    }




}
