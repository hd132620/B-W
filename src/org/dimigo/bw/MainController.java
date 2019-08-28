package org.dimigo.bw;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class MainController {
	
	@FXML private TextField ip;
	@FXML private TextField port;
	@FXML private TextField nametext;
	@FXML private Label result;
	
	Socket socket = null;
	
	public void loginAction(ActionEvent event) {
		try {
			
			// 받은 정보를 바탕으로 소켓을 생성함
			socket = new Socket(ip.getText(), new Integer(port.getText()));
			
			Stage stage = new Stage();
			
			FXMLLoader fxmlLoader = new FXMLLoader();
			
			GameController controller = new GameController();
			
			// GameController에 소켓과 이름을 파라미터로 값을 넘겨줌
			
			controller.setSocket(socket);
			controller.setName(nametext.getText());
			
			fxmlLoader.setController(controller);
			fxmlLoader.setLocation(getClass().getResource("Game.fxml")); 
			
			Parent root = (Parent)fxmlLoader.load();
			
			stage.setScene(new Scene(root));
			stage.setTitle("Black & White");
				
			stage.show();
			
		} catch (NumberFormatException e) {
			result.setText("포맷이 잘못되었습니다");
			e.printStackTrace();
		} catch (UnknownHostException e) {
			result.setText("서버 주소가 잘못되었습니다");
			e.printStackTrace();
		} catch (IOException e) {
			result.setText("서버 연결을 할 수 없습니다. 잠시 후 다시 시도해주세요");
			e.printStackTrace();
		}

	}
	
	public void validate(String name) {
		if(name.length()>10) {
			nametext.setPromptText("10글자 이하로 입력해주세요!!");
		}
	}
	
	public void handleCloseAction(ActionEvent event) {
		Platform.exit();
	}

	
	
}