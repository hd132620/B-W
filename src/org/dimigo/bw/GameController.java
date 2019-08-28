package org.dimigo.bw;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.animation.PathTransition.OrientationType;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

public class GameController implements Initializable {

	@FXML
	private ImageView enemyOrder;
	// 상대방의 선/후플레이어 여부

	@FXML
	private ImageView myOrder;
	// 나의 선/후플레이어 여부

	@FXML
	private ImageView resultimg;
	// 라운드 결과 이미지

	@FXML
	private ImageView ready;
	// 레디버튼

	@FXML
	private Label myName;
	// 나의 이름(출력용)

	@FXML
	private Label enemyName;
	// 상대방의 이름

	@FXML
	private Label myPoint;
	// 나의 승점
	@FXML
	private Label enemyPoint;
	// 상대방의 승점

	@FXML
	private Label roundNum;
	// 라운드넘버

	// @FXML
	// private Label mngLb;

	// private BWSocketManager manager;
	public Socket socket; // 서버와의 연결을 위한 메인소켓
	public String name; // 플레이어 이름(저장용)
	private DataInputStream din; // DataInputStream
	private DataOutputStream dou; // DataOutputStream

	private Image firstImage; // 선 이미지
	private Image lastImage; // 후 이미지

	private byte[] myResults = new byte[9]; // 나의 결과
	private byte[] enemyResults = new byte[9]; // 상대방의 결과
	private boolean isFirst; // 선/후플레이여 여부 제어용 변수
	boolean color = false; // 상대방의 색깔
	byte result = 0; // 라운드 결과
	private int rand; // 상대방 카드가 나오는 패턴( 무작위 숫자 )

	// 이하 상대방 카드

	@FXML
	private ImageView enemyTile0;
	@FXML
	private ImageView enemyTile1;
	@FXML
	private ImageView enemyTile2;
	@FXML
	private ImageView enemyTile3;
	@FXML
	private ImageView enemyTile4;
	@FXML
	private ImageView enemyTile5;
	@FXML
	private ImageView enemyTile6;
	@FXML
	private ImageView enemyTile7;
	@FXML
	private ImageView enemyTile8;

	// 상대방 타일 배열
	private ImageView[] enemyTiles;

	// //

	private ImageView selectedIv; // 선택된 타일

	private boolean tileAccess = false; // 타일선택 접근권한 제어 변수
	private boolean completed = false; // 타일선택 완료여부(부정확)-코드 삭제
	private boolean readyAccess = false; // 레디버튼 접근권한 제어 변수

	public static final byte WIN = 1; // WIN 상수
	public static final byte DRAW = 0; // DRAW 상수
	public static final byte LOSE = -1; // LOSE 상수

	private int cnt;
	private int cnt1;
	private int[] white;
	private int[] black;

	@Override
	public synchronized void initialize(URL arg0, ResourceBundle arg1) {

		// 리소스 로딩
		// 상대방 타일 정보 로딩
		enemyTiles = new ImageView[] { enemyTile0, enemyTile1, enemyTile2,
				enemyTile3, enemyTile4, enemyTile5, enemyTile6, enemyTile7,
				enemyTile8 };

		resultimg.setOpacity(0.0);

		// 선/후플레이어 이미지 로딩
		firstImage = new Image(getClass().getResourceAsStream(
				"picture/first.png"));
		lastImage = new Image(getClass()
				.getResourceAsStream("picture/last.png"));

		// 음악 재생
		Media sound = new Media(new File("music/VoodooPeople.mp3").toURI()
				.toString());
		MediaPlayer mediaPlayer = new MediaPlayer(sound);
		mediaPlayer.setCycleCount(6);

		white = new int[4];
		black = new int[5];

		// 중앙 백그라운드 쓰레드 부분
		// 백그라운드에서 서버와의 연결을 통해 절차적으로 프로그램이 진행될 수 있도록 한다.
		// 이벤트들을 제어하는 역할을 한다.
		// UI변경 메소드가 있으므로 JavaFX Application Thread에서 실행되게 하는
		// Platform.runLater() 함수를 사용하였다.

		Task<Void> manageMain = new Task<Void>() {

			@Override
			protected Void call() throws Exception {

				// 에러검출
				if (socket == null) {
					System.out.println("null 발생!");
					System.exit(-1);
				}

				// 소켓 초기화
				try {
					din = new DataInputStream(socket.getInputStream());
					dou = new DataOutputStream(socket.getOutputStream());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// 서버로 나의 이름 전송
				try {
					System.out.println("name : " + name);
					dou.writeUTF(name);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				synchronized (this) {
					readyAccess = true;
					// 레디 접근 허용

					// updategMessage("레디를 기다리는 중..");

					while (readyAccess) {
						if (readyAccess)
							break;
					}
					// 레디 버튼이 눌러지면 자동으로 무한루프를 빠져나옴
				}

				// updategMessage("게임이 곧 시작 예정");

				// Thread UI접근을 위한 코드
				// Platform.runLater(runnable);

				mediaPlayer.play();

				// 점수를 0으로 초기화
				Platform.runLater(() -> myPoint.setText("0"));
				Platform.runLater(() -> enemyPoint.setText("0"));
				// roundNum.setText("1");

				// 나의 이름 초기화
				Platform.runLater(() -> myName.setText(name));

				// 상대방 이름 수신
				try {
					String enemyname = din.readUTF();

					System.out.println(name + " - " + enemyname + ":"
							+ enemyname);
					// 상대방 이름 초기화

					Platform.runLater(() -> enemyName.setText(enemyname));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Platform.runLater(() -> enemyName
							.setText("enemyName get 에러발생"));
					e.printStackTrace();
				}

				try {
					// 선/후플레이어 여부 수신
					isFirst = din.readBoolean();

					System.out.println(name + " - " + "isFirst : " + isFirst);

					// 선/후플레이어 여부 보여주기
					Platform.runLater(() -> setFirst(isFirst));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// 게임 시작 부분

				for (int i = 1; i <= 9; i++) {

					System.out.println("client " + name + " " + i + "라운드 시작");

					if (isFirst) {
						// 선플레이어일 경우 실행

						synchronized (this) {
							// 타일송신 접근 허용
							tileAccess = true;

							// 상대편 색깔 수신
							// updategMessage("상대방이 타일을 고르는 중..");

							color = getColor();
							System.out.println(name + " - " + "receivedcolor :"
									+ color);
							completed = false; // 문제소지 있는 코드(무시함)

						}

						Platform.runLater(() -> enemyColorAction(color));
						// 상대편 카드 color에 따른 시각화 코드

						// 라운드 결과 수신
						try {
							result = din.readByte();
							System.out.println(name + " - "
									+ "receivedresult :" + result);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						Thread.sleep(3000); // 애니메이션을 위한 대기

						// 이미 낸 타일들을 모두 없앰
						Platform.runLater(() -> FadeOutImage(selectedIv));
						Platform.runLater(() -> FadeOutImage(enemyTiles[rand]));

						Thread.sleep(1000); // 애니메이션을 위한 대기

						// 타일 선택 막기
						Platform.runLater(() -> selectedIv.setVisible(false));
						Platform.runLater(() -> enemyTiles[rand]
								.setVisible(false));

						// 결과 디스플레이
						if (result == WIN) { // WIN
							Platform.runLater(() -> upNum(myPoint));
							Platform.runLater(() -> resultShow(WIN));
						} else if (result == DRAW) { // DRAW
							Platform.runLater(() -> resultShow(DRAW));
						} else/* LOSE */{
							Platform.runLater(() -> upNum(enemyPoint));
							Platform.runLater(() -> resultShow(LOSE));
							isFirst = false;
							Platform.runLater(() -> setFirst(isFirst));
						}

						Thread.sleep(4500);

					} else {
						// 후플레이어일 경우 실행

						// updategMessage("상대방이 타일을 고르는 중..");

						color = getColor();
						System.out.println(name + " - " + "receivedcolor :"
								+ color);

						Platform.runLater(() -> enemyColorAction(color)); // 상대편
																			// 카드
																			// color에
																			// 따른
																			// 시각화
																			// 코드

						synchronized (this) {

							// updategMessage("타일을 고르시오");

							tileAccess = true;

							try {
								result = din.readByte();
								System.out.println(name + " - "
										+ "receivedresult :" + result);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							completed = false;

						}

						// updategMessage("결과발표");

						Thread.sleep(3000);

						Platform.runLater(() -> FadeOutImage(selectedIv));
						Platform.runLater(() -> FadeOutImage(enemyTiles[rand]));

						Thread.sleep(1000);

						Platform.runLater(() -> selectedIv.setVisible(false));
						Platform.runLater(() -> enemyTiles[rand]
								.setVisible(false));

						if (result == WIN) { // WIN
							Platform.runLater(() -> upNum(myPoint));
							Platform.runLater(() -> resultShow(WIN));
							isFirst = true;
							Platform.runLater(() -> setFirst(isFirst));
						} else if (result == DRAW) { // DRAW
							Platform.runLater(() -> resultShow(DRAW));
						} else/* LOSE */{
							Platform.runLater(() -> upNum(enemyPoint));
							Platform.runLater(() -> resultShow(LOSE));
						}

						Thread.sleep(4500);

					}

					// 라운드 끝

					if (!(i == 9))
						Platform.runLater(() -> upNum(roundNum));
					// 라운드 넘버 UP 메소드

				}

				// 게임 끝
				// 게임플레이 정보 수신 (나, 상대편)

				/*
				 * din.readFully(myResults, 0, myResults.length);
				 * System.out.println(myResults.toString());
				 * 
				 * din.readFully(enemyResults, 0, enemyResults.length);
				 * System.out.println(enemyResults.toString());
				 */
				int iMyP = new Integer(myPoint.getText());
				int iEP = new Integer(enemyPoint.getText());

				if (iMyP > iEP)
					finalResultShow(WIN);
				else if (iMyP < iEP)
					finalResultShow(LOSE);
				else
					finalResultShow(DRAW);

				// 최종결과

				// int finalResult = din.readByte(); // 최종결과 처리
				// System.out.println(finalResult);

				socket.close();

				// Platform.runLater(() -> upNum(roundNum));
				// roundNum 추후 추가할 것

				return null;
			}
		};

		new Thread(manageMain).start();

	}

	// public void updategMessage(String ifm) {
	// Platform.runLater(() -> mngLb.setText(ifm));
	// }

	public void enemyColorAction(boolean isWhite) {
		Random random = new Random();

		// 디버깅용 코드
		System.out.println(name + " - enemyColorAction : " + isWhite);
		//

		synchronized (this) {

			// 상대방 카드가 순서대로 나오게 하는 코드
			// 이영석

			if (isWhite == true) {
				rand = random.nextInt(4) * 2 + 1;
				if (cnt >= 0 && cnt <= 3) {
					white[cnt] = rand;
					if (cnt == 0) {
						cnt++;
					} else if (cnt == 1) {
						if (white[cnt] == white[cnt - 1]) {
							while (true) {
								rand = random.nextInt(4) * 2 + 1;
								white[cnt] = rand;
								if (white[cnt] != white[cnt - 1])
									break;
							}
						}
						cnt++;
					} else if (cnt == 2) {
						if (white[cnt] == white[cnt - 2]
								|| white[cnt] == white[cnt - 1]) {
							while (true) {
								rand = random.nextInt(4) * 2 + 1;
								white[cnt] = rand;
								if (white[cnt] != white[cnt - 2]
										&& white[cnt] != white[cnt - 1])
									break;
							}
						}
						cnt++;
					} else if (cnt == 3) {
						if (white[cnt] == white[cnt - 1]
								|| white[cnt] == white[cnt - 2]
								|| white[cnt] == white[cnt - 3]) {
							while (true) {
								rand = random.nextInt(4) * 2 + 1;
								white[cnt] = rand;
								if (white[cnt] != white[cnt - 2]
										&& white[cnt] != white[cnt - 1]
										&& white[cnt] != white[cnt - 3])
									break;
							}
						}
						cnt++;
					}
				}

				enemyTiles[rand].setImage(new Image(getClass()
						.getResourceAsStream("picture/wq.png")));
				enemyTiles[rand].setRotate(180);

			} else {
				rand = random.nextInt(5) * 2;
				if (cnt1 >= 0 && cnt1 <= 4) {
					black[cnt1] = rand;
					if (cnt1 == 0) {
						cnt1++;
					} else if (cnt1 == 1) {
						if (black[cnt1] == black[cnt1 - 1]) {
							while (true) {
								rand = random.nextInt(5) * 2;
								black[cnt1] = rand;
								if (black[cnt1] != black[cnt1 - 1])
									break;
							}
						}
						cnt1++;
					} else if (cnt1 == 2) {
						if (black[cnt1] == black[cnt1 - 2]
								|| black[cnt1] == black[cnt1 - 1]) {
							while (true) {
								rand = random.nextInt(5) * 2;
								black[cnt1] = rand;
								if (black[cnt1] != black[cnt1 - 2]
										&& black[cnt1] != black[cnt1 - 1])
									break;
							}
						}
						cnt1++;
					} else if (cnt1 == 3) {
						if (black[cnt1] == black[cnt1 - 1]
								|| black[cnt1] == black[cnt1 - 2]
								|| black[cnt1] == black[cnt1 - 3]) {
							while (true) {
								rand = random.nextInt(5) * 2;
								black[cnt1] = rand;
								if (black[cnt1] != black[cnt1 - 2]
										&& black[cnt1] != black[cnt1 - 1]
										&& black[cnt1] != black[cnt1 - 3])
									break;
							}
						}
						cnt1++;
					} else if (cnt1 == 4) {
						if (black[cnt1] == black[cnt1 - 1]
								|| black[cnt1] == black[cnt1 - 2]
								|| black[cnt1] == black[cnt1 - 3]
								|| black[cnt1] == black[cnt1 - 4]) {
							while (true) {
								rand = random.nextInt(5) * 2;
								black[cnt1] = rand;
								if (black[cnt1] != black[cnt1 - 2]
										&& black[cnt1] != black[cnt1 - 1]
										&& black[cnt1] != black[cnt1 - 3]
										&& black[cnt1] != black[cnt1 - 4])
									break;
							}
						}
						cnt1++;
					}
				}
				enemyTiles[rand].setImage(new Image(getClass()
						.getResourceAsStream("picture/bq.png")));
				enemyTiles[rand].setRotate(180);
			}

			// 선택된 타일을 상하반전시킨 물음표 카드로 이미지를 바꾸고
			// 중앙으로 이동시킨다.

			final Path path = new Path();
			path.getElements().add(
					new MoveTo(enemyTiles[rand].getX() + 35, enemyTiles[rand]
							.getY() + 52));
			path.getElements().add(
					new LineTo(enemyTiles[rand].getX() + 35, enemyTiles[rand]
							.getY() + 178));
			int x = rand * 70;
			path.getElements().add(
					new LineTo(225 - x, enemyTiles[rand].getY() + 178));
			path.setOpacity(0.0);

			final PathTransition pathTransition = new PathTransition();
			pathTransition.setDuration(Duration.seconds(3.0));
			pathTransition.setDelay(Duration.seconds(.2));
			pathTransition.setPath(path);
			pathTransition.setNode(enemyTiles[rand]);
			pathTransition.setOrientation(OrientationType.NONE);
			pathTransition.setCycleCount(1);
			pathTransition.setAutoReverse(false);

			final ParallelTransition parallelTransition = new ParallelTransition(
					pathTransition);
			parallelTransition.play();

		}

		/*
		 * Thread t1 = new Thread(new Runnable() { public void run() { try {
		 * Thread.sleep(4000); FadeOutImage(enemyTiles[rand]); } catch
		 * (InterruptedException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } } });
		 * 
		 * Thread t2 = new Thread(new Runnable() { public void run() { try {
		 * Thread.sleep(5500); enemyTiles[rand].setVisible(false); } catch
		 * (InterruptedException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } } });
		 * 
		 * t1.start(); t2.start();
		 */

	}

	// 라운드 결과 출력 메소드
	public void resultShow(int result) {

		// Debugging 코드
		if (result == WIN) {
			System.out.println(name + " - " + "roundresult : WIN");
		} else if (result == DRAW) {
			System.out.println(name + " - " + "roundresult : DRAW");
		} else if (result == LOSE) {
			System.out.println(name + " - " + "roundresult : LOSE");
		} else {
			System.out.println("resultShow 에러 발생!");
		}
		//

		// 실 구현부

		if (result == WIN) {
			Image winImage = new Image(getClass().getResourceAsStream(
					"picture/win.png"));
			resultimg.setImage(winImage);
		} else if (result == DRAW) {
			Image drawImage = new Image(getClass().getResourceAsStream(
					"picture/draw.png"));
			resultimg.setImage(drawImage);
		} else if (result == LOSE) {
			Image loseImage = new Image(getClass().getResourceAsStream(
					"picture/lose.png"));
			resultimg.setImage(loseImage);
		}
		resultimg.setVisible(true);
		FadeInImage(resultimg);

		Thread t1 = new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(4500);
					resultimg.setVisible(false);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		t1.start();
		//

	}

	public void finalResultShow(int result) {
		if (result == WIN) {
			Image winImage = new Image(getClass().getResourceAsStream(
					"picture/win.png"));
			resultimg.setImage(winImage);
		} else if (result == DRAW) {
			Image drawImage = new Image(getClass().getResourceAsStream(
					"picture/draw.png"));
			resultimg.setImage(drawImage);
		} else if (result == LOSE) {
			Image loseImage = new Image(getClass().getResourceAsStream(
					"picture/lose.png"));
			resultimg.setImage(loseImage);
		}
		resultimg.setVisible(true);
		FadeInImage(resultimg);
	}

	// 이미지뷰를 페이드아웃하는 메소드
	public void FadeOutImage(ImageView image) {
		FadeTransition fo = new FadeTransition(Duration.millis(1000), image);
		fo.setFromValue(1.0);
		fo.setToValue(0.0);
		fo.setCycleCount(1);
		fo.setAutoReverse(false);
		fo.play();
	}

	// 이미지뷰를 페이드인하는 메소드
	public void FadeInImage(ImageView winImage) {
		FadeTransition fi = new FadeTransition(Duration.millis(1000), winImage);
		fi.setFromValue(0.0);
		fi.setToValue(1.0);
		fi.setCycleCount(1);
		fi.setAutoReverse(false);
		fi.play();
	}

	// 라벨의 숫자를 1 올리는 메소드(여러 객체에서 여러 용도로 사용가능)
	public synchronized void upNum(Label label) {
		Integer num = new Integer(label.getText()) + 1;

		// 디버깅용 코드
		System.out.println(name + " - " + "변경 전: " + label.getText()
				+ ", 변경 후 : " + num);
		//

		Platform.runLater(() -> label.setText(num.toString()));
	}

	// 나를 기준으로 true일경우 선플레이어, false일 경우 후플레이어가 되는 메소드
	public void setFirst(boolean first) {

		if (first == true) {
			myOrder.setImage(firstImage);
			enemyOrder.setImage(lastImage);
		} else {
			myOrder.setImage(lastImage);
			enemyOrder.setImage(firstImage);
		}
	}

	// 내 어떤 타일이라도 메소드를 클릭하면 연결됨, 실질적인 타일숫자 송신메소드
	public synchronized void tileClickAction(MouseEvent event) {

		// 디버깅코드
		System.out.println(name + " " + "현재 tileAccess 상황" + tileAccess);
		//

		if (tileAccess) {
			// 백그라운드 중앙 쓰레드가 tileAccess를 열어주면 송신을 할 수 있다.

			// /////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 데이터 송신 부분

			synchronized (this) {
				selectedIv = ((ImageView) (event.getSource()));
				// 전역변수에 저장해 백그라운드 중앙 서버에서 참조할 수 있게 함
			}

			// 선택된 카드의 id로 숫자 입력받음
			byte selectedNum = new Byte(selectedIv.getId());

			// 입력받은 숫자 송신
			try {
				dou.writeByte(selectedNum);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// 디버깅용 코드
			System.out.println(selectedNum);

			// 한번만 송신하고 종료시키므로 바로 tileAccess를 닫아준다
			synchronized (this) {
				tileAccess = false;
				completed = true;
			}

			// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			// 타일을 뒤집는 이미지로 변경하고 중앙으로 이동함

			// selectedIv 선택된 ImageView
			// 이동 애니메이션 부분(이영석)
			// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			Image wbackImage = new Image(getClass().getResourceAsStream(
					"picture/wq.png"));
			Image bbackImage = new Image(getClass().getResourceAsStream(
					"picture/bq.png"));
			if (selectedNum % 2 == 1)
				selectedIv.setImage(wbackImage);
			else
				selectedIv.setImage(bbackImage);

			final Path path = new Path();
			path.getElements().add(
					new MoveTo(selectedIv.getX() + 35, selectedIv.getY() + 52));
			path.getElements().add(
					new LineTo(selectedIv.getX() + 35, selectedIv.getY() - 78));
			int x = new Integer(selectedIv.getId()) * 70;
			path.getElements().add(new LineTo(400 - x, selectedIv.getY() - 78));
			path.setOpacity(0.0);

			final PathTransition pathTransition = new PathTransition();
			pathTransition.setDuration(Duration.seconds(3.0));
			pathTransition.setDelay(Duration.seconds(.2));
			pathTransition.setPath(path);
			pathTransition.setNode(selectedIv);
			pathTransition.setOrientation(OrientationType.NONE);
			pathTransition.setCycleCount(1);
			pathTransition.setAutoReverse(false);

			final ParallelTransition parallelTransition = new ParallelTransition(
					pathTransition);
			parallelTransition.play();

			/*
			 * Thread t1 = new Thread(new Runnable() { public void run() { try {
			 * Thread.sleep(4000); FadeOutImage(selectedIv); } catch
			 * (InterruptedException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); } } });
			 * 
			 * Thread t2 = new Thread(new Runnable() { public void run() { try {
			 * Thread.sleep(5500); selectedIv.setVisible(false); } catch
			 * (InterruptedException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); } } });
			 * 
			 * t1.start(); t2.start();
			 */

			// ////////////////////////////////////////////////////////////////////

		}

	}

	// 서버로부터 상대방의 타일 색깔을 수신받음
	public boolean getColor() {
		try {
			return din.readBoolean();
		} catch (IOException e) {
			System.out.println("getColor() 에러 발생!");
			e.printStackTrace();
			return false;
		}
	}

	// 서버로부터 라운드 결과를 수신받음
	public byte getResult() {
		try {
			return din.readByte();
		} catch (IOException e) {
			System.out.println("getResult() 에러 발생!");
			e.printStackTrace();
			return -2;
		}
	}

	// main에서 서버와 연결될 경우 초기화하는 메소드
	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	// 이하동문
	public void setName(String name) {
		this.name = name;
	}

	// 만들 메소드
	// 모두 만듬

	// 레디 버튼을 클릭할 때 호출되는 메소드
	public synchronized void readyClickAction(MouseEvent event)
			throws IOException {
		if (readyAccess) {
			// 중앙 메소드에서 tileAccess 권한을 열어놓았을 경우 실행됨
			Image image3 = new Image(getClass().getResourceAsStream(
					"picture/clickedbutton.png"));
			ready.setImage(image3);
			ready.setVisible(false);

			dou.writeUTF("READY");
			synchronized (this) {
				readyAccess = false;
			}
		}
	}

	// 레디버튼에 마우스가 들어올 때 호출되는 메소드
	public void readyDragAction(MouseEvent event) {
		Image image4 = new Image(getClass().getResourceAsStream(
				"picture/hoveredbutton.png"));
		ready.setImage(image4);
	}

	// 레디버튼에 마우스가 나갈 때 호출되는 메소드
	public void readyReleaseAction(MouseEvent event) {
		Image image5 = new Image(getClass().getResourceAsStream(
				"picture/ready.png"));
		ready.setImage(image5);
	}
}
