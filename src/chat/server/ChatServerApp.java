package chat.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class ChatServerApp {
	private static final int PORT = 9090;
	
	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		Map<String, PrintWriter> listPrintWriters = new HashMap<String, PrintWriter>();

		try {
			// 1. 서버소켓 생성
			serverSocket = new ServerSocket();

			// 2. binding
			serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), PORT));
			System.out.println("bind " + InetAddress.getLocalHost().getHostAddress() + ":" + PORT);

			// 3. 연결 요청 기다림
			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("[서버] 연결됨");

				Thread chatThread = new ChatServerThread(socket, listPrintWriters);
				chatThread.start();
				
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (serverSocket != null && serverSocket.isClosed() == false) {
				try {
					serverSocket.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}
