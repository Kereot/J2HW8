package server;

import org.omg.CORBA.PUBLIC_MEMBER;
import service.ServiceMessages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket server;
    private Socket socket;
    private final int PORT = 8189;

    private List<ClientHandler> clients;
    private AuthService authService;


    public Server() {
        clients = new CopyOnWriteArrayList<>();
//        authService = new SimpleAuthService();
        authService = new DatabaseAuthService();
        ExecutorService service = Executors.newCachedThreadPool();

        try {
            server = new ServerSocket(PORT);
            System.out.println("Server started!");
            DBHandler.connect();
            System.out.println("DB connected");

            while (true) {
                socket = server.accept();
                System.out.println("Client connected: " + socket.getRemoteSocketAddress());
//                new ClientHandler(this, socket);
                service.execute(() -> {
                    new ClientHandler(this, socket);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Server stop");
            service.shutdown();
            DBHandler.disconnect();
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public void broadcastMsg(ClientHandler sender, String msg) {
        String message = String.format("[ %s ]: %s", sender.getNickname(), msg);
        for (ClientHandler c : clients) {
            c.sendMsg(message);
        }
    }

    public void privateMsg(ClientHandler sender, String receiver, String msg) {
        String message = String.format("[ %s ] to [ %s ]: %s", sender.getNickname(), receiver, msg);
        for (ClientHandler c : clients) {
            if (c.getNickname().equals(receiver)) {
                c.sendMsg(message);
                if (!sender.getNickname().equals(receiver)) {
                    sender.sendMsg(message);
                }
                return;
            }
        }
        sender.sendMsg("not found user: " + receiver);
    }

    public boolean isLoginAuthenticated(String login) {
        for (ClientHandler c : clients) {
            if (c.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    public void broadcastClientList() {
        StringBuilder sb = new StringBuilder(ServiceMessages.CLIENTLIST);

        for (ClientHandler c : clients) {
            sb.append(" ").append(c.getNickname());
        }

        String message = sb.toString();

        for (ClientHandler c : clients) {
            c.sendMsg(message);
        }
    }

    public AuthService getAuthService() {
        return authService;
    }

    public void changeNickname(ClientHandler sender, String login, String nickname) {
        try {
            PreparedStatement psChNick = DBHandler.getConnection().prepareStatement("UPDATE clients SET nickname = ? WHERE login = ?;");
            psChNick.setString(1, nickname);
            psChNick.setString(2, login);
            try {
                if (nickname.equals(sender.getNickname())) {
                    throw new SQLException();
                }
                psChNick.executeUpdate();
                sender.sendMsg("Nickname changed to " + nickname);
            } catch (SQLException e) {
                sender.sendMsg("Nickname change failed, new nickname might be in use");
            }
        } catch (SQLException e) {
            sender.sendMsg("Nickname change failed");
            e.printStackTrace();
        }
    }
}
