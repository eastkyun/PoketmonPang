import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * 한국기술교육대학교 컴퓨터공학부
 * 2018년도 1학기 학기 프로젝트: 포켓몬팡
 * @author 김상진 
 * 포켓몬팡의 컨트롤러 클래스: 사용자 상호작용 부분과 각종 타이머 사건 처리
 */
public class PangGridController {
	// 게임 종료 타이머: 1초마다 알람, gameOverCount를 통해 게임 시간 조절
	private Timeline gameOverTimeline = new Timeline();
	private Timeline hintTimeline = new Timeline();
	private PangGridView view = null;
	private PangGridModel model = null;
	private int gameOverCount = 60;
	private Location srcLoc = null;		// 첫 번째 클릭 위치
	private Location destLoc = null;	// 두 번째 클릭 위치
	/*
	 *  콤보 관련 멤버들
	 *  콤보는 최초 3초 이전에 계속 팡하면 콤보 증가, 그 다음에 2.5초, 2초, 1초
	 */
	private Timeline comboTimeline = new Timeline();
	private int[] comboTime = {3000, 2500, 2000, 1000};
	private int comboTimeIndex = 0;
	
	
	/**
	 * 게임 종료 타이머를 재시작 
	 */
	private void comboTimeline() {		
		comboTimeline.stop();
		comboTimeline.getKeyFrames().clear();
		comboTimeline.getKeyFrames().add(
			new KeyFrame(Duration.millis(comboTime[comboTimeIndex++]),e->{
					comboTimeIndex%=4;
					model.updateCombo(comboTimeIndex);					
					view.updateCombo(comboTimeIndex+"");
					}
					));
		comboTimeline.setCycleCount(comboTimeIndex);
		gameOverTimeline.play();
	}
	private void restartGameOverTimeLine(){
		gameOverTimeline.stop();
		gameOverTimeline.getKeyFrames().clear();
		gameOverTimeline.getKeyFrames().add(
			new KeyFrame(Duration.millis(1000),e->gameOverHandle(e)));
		gameOverTimeline.setCycleCount(Animation.INDEFINITE);
		gameOverTimeline.play();
	}
	private void restartHintTimeLine(){
		view.removeHint();
		hintTimeline.stop();
		hintTimeline.getKeyFrames().clear();
		hintTimeline.getKeyFrames().add(
			new KeyFrame(Duration.millis(3000),e->{
				model.findHints();
				view.showHint();
			}));
		hintTimeline.setCycleCount(1);
		hintTimeline.play();
	}
	/**
	 * 1초마다 타이머에 의해 호출되는 함수 
	 */
	public void gameOverHandle(ActionEvent event) {
		--gameOverCount;
		view.updateTime(gameOverCount+"");
		if(gameOverCount==0){
			gameOver();
		}
	}
	/**
	 * 마우스 클릭 처리 메소드 
	 */
	private void mouseClickHandle(MouseEvent mouseEvent) {
		double x = mouseEvent.getX()+1;
		double y = mouseEvent.getY()+1;
		
		int r = (int)(y/PangUtility.POKETMONIMAGESIZE);
    	int c = (int)(x/PangUtility.POKETMONIMAGESIZE);
    	if(srcLoc==null){
    		srcLoc = new Location(r,c);
    		view.showEffect(srcLoc);
    		if(model.processSpecialPokemon(r,c)){
    			processClick();
    			restartHintTimeLine();
    			srcLoc = null;
    		}
    	}
    	else{
    		destLoc = new Location(r,c);
    		if(model.isValidSwap(srcLoc,destLoc)){
    			model.swap(srcLoc, destLoc);
    			view.removeEffect();
    			if(model.checkAndMark()) processClick();
    			restartHintTimeLine();
    		}
    		srcLoc = destLoc = null;
    	}
    }
	private void processClick(){
		SequentialTransition seq = new SequentialTransition();
		PauseTransition pushUpTransition = new PauseTransition(Duration.millis(100));
		pushUpTransition.setOnFinished(event->model.pushUpMarked());
		PauseTransition replaceTransition = new PauseTransition(Duration.millis(100));
		replaceTransition.setOnFinished(event->model.replaceMarked());
		seq.getChildren().addAll(pushUpTransition, replaceTransition);
		seq.setOnFinished(event->{
			if(model.checkAndMark()) processClick();
			else{
				model.insertSpecialPokemon();
    			if(!model.findHints()) gameOver();
			}
		});
		seq.play();
	}
	
	public PangGridController(PangGridView view, PangGridModel model){
		this.view = view;
		this.model = model;
		view.getPangGrid().setOnMouseClicked(e->mouseClickHandle(e));
	}
	public void startGame(){
		PangUtility.pokemonInfoDialog("PokemonPang 게임시작", "게임을 시작하시겠습니까???");
		initGame();
	}
	/**
	 * 새 게임마다 새롭게 초기화되어야 하는 것들을 초기화
	 */
	public void initGame(){
		gameOverCount = 60;			// 실제로는 60이 되어야 함
		gameOverTimeline.stop();
		model.initAssign();
		model.findHints();
		view.updateTime(gameOverCount+"");
		restartGameOverTimeLine(); 	// 타이머 시작
		restartHintTimeLine(); 		// 힌트 타이머 시작
		comboTimeline();
	}
	public void gameOver(){
		gameOverTimeline.stop(); 	// 타이머 종료
		final Window stage = view.getScene().getWindow();
		stage.hide();
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
            	if(PangUtility.pokemonConfirmDialog("PokemonPang 게임종료", "새 게임을 하시겠습니까???", 
            			"새 게임", "게임 종료")){
            		initGame();
            		((Stage)stage).show();
        		}
        		else Platform.exit();
            }
        });
	}
}