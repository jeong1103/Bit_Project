package chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ChatServerThread extends Thread {
	private String nickname;
	private Socket socket;
	private Map<String, PrintWriter> listPrintWriters;
	
	public ChatServerThread(Socket socket, Map<String, PrintWriter> listPrintWriters) {
		this.socket = socket;
		this.listPrintWriters = listPrintWriters;
	}

	@Override
	public void run() {
		BufferedReader br = null;
		PrintWriter pw = null;
		
		try {
			br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
			pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);
			
			InetSocketAddress remoteSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
			int remotePort = remoteSocketAddress.getPort();
			String remoteAddress = remoteSocketAddress.getAddress().getHostAddress();
			System.out.println("[서버] 연결됨 " + remoteAddress + ":" + remotePort);
			
			while (true) {
				String message = br.readLine();

				if (message == null) {
					System.out.println("[서버]연결 끊어짐(클라이언트가 정상적 종료)");
					Exit(pw);
					break;
				}
				
				String[] s_message = message.split(":");
				
				switch (s_message[0]) {
				case "join":
					Join(pw, s_message[1]);
					break;
				case "exit":
					Exit(pw);
					break;
				case "message":
					Message(s_message[1]);
					break;
				}
			}
		} catch (SocketException e) {
			System.out.println("[서버]연결 끊어짐(클라이언트가 비정상적 종료)");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (socket != null && socket.isClosed() == false) {
					socket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void Message(String message) {
		String data = nickname + ":" + message;
		broadcast(data);
	}
	
	private void Join(PrintWriter printWriter, String nickname) {
		this.nickname = nickname;

		addClient(nickname, printWriter);
		
		String message = nickname + "님이 입장했습니다.";
		broadcast(message);
	}
	
	private void Exit(PrintWriter printWriter) {
		printWriter.println("exit");
		printWriter.flush();
		
		//퇴장 메세지 브로드캐스팅
		String data = nickname + "님이 퇴장하였습니다.";
		broadcast(data);
		
		// PrintWriter 제거
		removeClient(printWriter, nickname);
	}
	
	private void addClient(String nickname, PrintWriter printWriter) {
		synchronized(listPrintWriters) {
			listPrintWriters.put(nickname, printWriter);
		}
	}
	
	private void removeClient(PrintWriter printWriter, String nickName) {
		synchronized(listPrintWriters) {
			Set<String> set = listPrintWriters.keySet();
			
			Iterator<String> it = set.iterator();
			while(it.hasNext()) {
				if (it.next().equals(nickName)) {
					listPrintWriters.remove(nickName);
				}
			}
		}
	}
	
	private void broadcast(String data) {
		synchronized(listPrintWriters) {
			Set<String> set = listPrintWriters.keySet();
			
			Iterator<String> it = set.iterator();
			while(it.hasNext()) {
				PrintWriter printWriter = listPrintWriters.get(it.next());
				printWriter.println(data + "/" + set + "/" + set.size());
				printWriter.flush();
			}
		}
	}
}
