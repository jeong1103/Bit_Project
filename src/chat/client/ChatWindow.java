package chat.client;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


public class ChatWindow implements ActionListener {

	private Frame frame;
	private Panel pannel;
	private Panel textPannel;
	private Button buttonSend;
	private TextField textField;
	private TextArea textArea;
	
	// 형근 추가
	private TextArea userList;
	private String nickName;
	// 통신 관련
	private Socket socket;
	private BufferedReader bufferedReader;
	private PrintWriter printWriter;
	
	
	public ChatWindow(String name, String ipAddress, int port) {
		frame = new Frame("Talk~Talk!");
		pannel = new Panel();
		textPannel = new Panel();
		buttonSend = new Button("Send");
		textField = new TextField();
		
		// 형근 수정한 부분
		textArea = new TextArea(30, 65);
		userList = new TextArea("참여 인원(", 30, 20, TextArea.SCROLLBARS_NONE);
		this.nickName = name;
		
		Connection(ipAddress, port);
	}

	private void Connection(String ip, int port) {
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(ip, port));
			
			if (socket != null) {
				ChatThread();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void ChatThread() {
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
			printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8 ), true);
			
			printWriter.println("join:" + getNickName());
			printWriter.flush();
			
			Thread chatThread = new Thread(new Runnable() { // 스레드를 돌려서 서버로부터 메세지를 수신
				@Override
				public void run() {
					while (true) {
						try {
							while (true) {
								String data = bufferedReader.readLine();
								
								// 0 - 메세지, 1 - 유저, 2 - 인원 수
								String[] s_data = data.split("/");

								// 클라이언트가 정상적 종료
								// GUI에서 메시지를 서버에 보내면 서버에서 다시 클라이언트로 보내면 그걸 출력하는 구조.
								if (data == null || data.equals("exit")) {
									break;
								}
								
								System.out.println(data);
								textArea.append(s_data[0] + "\n");
								
								userList.setText("참여 인원(");
								userList.append(s_data[2] + "명) \n");
								
								String[] ss_data = s_data[1].substring(1,s_data[1].length() - 1).split(",");
								for (int i = 0; i < ss_data.length; i++) {
									userList.append(ss_data[i].trim() + "\n");
								}
							}
						} catch (IOException e) {
							textArea.append("메세지 수신 에러!!\n");
							try {
								socket.close();
								break;
							} catch (IOException ex) {
								ex.printStackTrace();
							}
						} finally {
							try {
								if (bufferedReader != null) {
									bufferedReader.close();
								}

								if (printWriter != null) {
									printWriter.close();
								}

								if (socket != null && socket.isClosed() == false) {
									socket.close();
								}
							} catch (IOException ex) {
								ex.printStackTrace();
							}
						}
					}
				}
			});

			chatThread.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getNickName() {
		return nickName;
	}

	public void show() {
		// Button
		buttonSend.addActionListener(this);
		buttonSend.setBackground(Color.GRAY);
		buttonSend.setForeground(Color.WHITE);

		// Textfield
		textField.setColumns(80);
		textField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				char keyCode = e.getKeyChar();
				if (keyCode == KeyEvent.VK_ENTER) {
					actionPerformed(null);
				}
			}
		});

		// Pannel
		pannel.setBackground(Color.LIGHT_GRAY);
		pannel.add(textField);
		pannel.add(buttonSend);
		frame.add(BorderLayout.SOUTH, pannel);

		// TextArea
		textArea.setEditable(false);
		userList.setEditable(false);
		userList.setBackground(Color.orange);
		
		textPannel.add(textArea);
		textPannel.add(userList);
		frame.add(BorderLayout.CENTER, textPannel);

		// Frame
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.setVisible(true);
		frame.pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String message = textField.getText();
		
		if (message.equals("exit") == true) {
			printWriter.println("exit");
			printWriter.flush();
			System.exit(0);
		} else {
			printWriter.println("message:" + message);
			printWriter.flush();
		}
		
		textField.setText("");
		textField.requestFocus();
	}
}
