package model;

public class Client {
	private String nickName;
	
	
	public Client(String nickName){
		this.nickName = nickName;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	@Override
	public String toString() {
		return getNickName();
	}
}
