package org.dimigo.bw;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class GameControllerSabon implements Initializable {
	
	@FXML private ImageView enemyOrder;
	@FXML private ImageView myOrder;
	
	@FXML private ImageView ready;
	
	@FXML private Label myName;
	@FXML private Label enemyName;
	
	@FXML private Label myPoint;
	@FXML private Label enemyPoint;
	
	@FXML private Label roundNum;
	
//	private BWSocketManager manager;
	public Socket socket;
	public String name;
	private DataInputStream din;			// DataInputStream
	private DataOutputStream dou;		// DataOutputStream
	
	private Image firstImage;
	private Image lastImage;
	
	private boolean isFirst;
	
	private boolean tileAccess = false;
	private boolean completed = false;
	private boolean readyAccess = false;
	
	final int WIN = 1;
	final int DRAW = 0;
	final int LOSE = -1;
	
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		Task<Void> manageMain = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				
				Platform.runLater(new Runnable() {

                    public void run() {
                    	
                    }
                });


				System.out.println("initialize 실행");
				
				if(socket == null) {
					System.out.println("null 발생!");
					System.exit(-1);
				}
				
				try {
					din = new DataInputStream(socket.getInputStream());
					dou = new DataOutputStream(socket.getOutputStream());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try {
					dou.writeUTF(name);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				synchronized(this) {
					readyAccess = true;
					
					while(readyAccess) {
						if(readyAccess) 
							break;
					}
				}
				
				
				firstImage = new Image(getClass().getResourceAsStream("picture/first.png"));
				lastImage = new Image(getClass().getResourceAsStream("picture/last.png"));
				
				myPoint.setText("0");
				enemyPoint.setText("0");
				roundNum.setText("1");
				
				myName.setText(name);
				
				try {
					enemyName.setText(din.readUTF());
				} catch (IOException e) { 
					// TODO Auto-generated catch block
					enemyName.setText("enemyName get 에러발생");
					e.printStackTrace();
				}
				
				try {
					isFirst = din.readBoolean();
					setFirst(isFirst);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Task<Void> manage = new Task<Void>() {
					
					@SuppressWarnings("unused")
					@Override
					protected Void call() throws Exception {
						
						int result = 0;
						boolean color = false;
						
						for(int i = 1; i<=9; i++) {
							if(isFirst) {
								
								synchronized(this) {
									tileAccess = true;
									
									// 상대편 색깔 수신
									while(completed) {
										color = getColor();
										completed = false;
									}
								}
								
								enemyColorAction(color); // 상대편 카드 color에 따른 시각화 코드
								
								result = din.readByte();
								
								if(result == WIN) { // WIN
									upNum(myPoint);
									resultShow(WIN);
								} else if(result == DRAW) { // DRAW
									resultShow(DRAW);
								} else/*LOSE*/{
									upNum(enemyPoint);
									resultShow(LOSE);
									setFirst(isFirst = false);
								}
								
							}
							else {
								color = getColor();
							
								enemyColorAction(color); // 상대편 카드 color에 따른 시각화 코드
								
								synchronized(this) {
									tileAccess = true;
									
									while(completed) {
										result = din.readByte();
										completed = false;
									}
								}
								
								if(result == WIN) { // WIN
									upNum(myPoint);
									resultShow(WIN);
									setFirst(isFirst = true);
								} else if(result == DRAW) { // DRAW
									resultShow(DRAW);
								} else/*LOSE*/{
									upNum(enemyPoint);
									resultShow(LOSE);
								}
								
							}
							upNum(roundNum);
						}
						
						return null;
					}
				};
				
				new Thread(manage).start();
				
				return null;
			}
			
		};
		
		new Thread(manageMain).start();
		
	}
	
	public void enemyColorAction(boolean color) {
		if(color) { // 하얀색을 전송받았을 경우
			
		} else { // 검정색을 전송받았을 경우
			
		}
	}
	
	public void resultShow(int result) {
		if(result == WIN) {
			
		}
		else if(result == DRAW) {
			
		}
		else if(result == LOSE){
			
		}
		else {
			System.out.println("resultShow 에러 발생!");
		}
	}
	
	public void upNum(Label label) {
		Integer num = new Integer(label.getText()) + 1;
		label.setText(num.toString());
	}
	
	public void setFirst(boolean first) {
		
		if(first == true) {
			myOrder.setImage(firstImage);
			enemyOrder.setImage(lastImage);
		} else {
			myOrder.setImage(lastImage);	 
			enemyOrder.setImage(firstImage);
		}
	}
	
	public synchronized void tileClickAction(MouseEvent event) {
		if(tileAccess) {
			
			ImageView selectedIv = ((ImageView)(event.getSource()));
			
			
			// 타일을 뒤집는 이미지로 변경
			
			// selectedIv 선택된 ImageView
			// 이동 애니메이션 부분(이영석)
			
			//
			
			
			
			byte selectedNum = new Byte(selectedIv.getId());
			try {
				dou.writeByte(selectedNum);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// selectedNum을 서버로 보냄.
			
			// 소멸 애니메이션 부분(이영석)
			
			//
			
			System.out.println(selectedNum);
			selectedIv.setVisible(false);
			synchronized(this) {
				tileAccess = false;
				completed = true;
			}
			
			
		}
		
	}
	
	public boolean getColor() {
		try {
			return din.readBoolean();
		} catch (IOException e) {
			System.out.println("getColor() 에러 발생!");
			e.printStackTrace();
			return false;
		}
	}
	
	public int getResult() {
		try {
			return din.readByte();
		} catch (IOException e) {
			System.out.println("getResult() 에러 발생!");
			e.printStackTrace();
			return -2;
		}
	}
	
	// 만들 메소드
	// 3. 타일 위에 마우스 올려놓을 경우 바뀌는 메소드(UI와 함께 작업)
	// 4. 게임진행상황(라운드, 대기중 등) 시각화 메소드
	
	public void readyClickAction(MouseEvent event) throws IOException {
		if(readyAccess) {
			Image image3 = new Image(getClass().getResourceAsStream("picture/clickedbutton.png"));
			ready.setImage(image3);
			ready.setVisible(false);
			
			dou.writeUTF("READY");
			synchronized(this) {
				readyAccess = false;
			}
		}
	}
	
	public void readyDragAction(MouseEvent event) {
		Image image4 = new Image(getClass().getResourceAsStream("picture/hoveredbutton.png"));
		ready.setImage(image4);
	}

	public void readyReleaseAction(MouseEvent event) {
		Image image5 = new Image(getClass().getResourceAsStream("picture/ready.png"));
		ready.setImage(image5);
	}
}
