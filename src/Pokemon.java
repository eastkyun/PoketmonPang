import javafx.scene.image.Image;

/**
 * 한국기술교육대학교 컴퓨터공학부
 * 2018년도 1학기 학기 프로젝트: 포켓몬팡
 * @author 김상진 
 * 포켓몽 열거형: 게임맵에 등장하는 포켓몬들
 * 이상해 ~ 꼬부기: 기본적으로 등장하는 7개 포켓몽
 * 망나뇽 ~ 잠맘보: 특수 아이템 포켓몽
 */
public enum Pokemon {
	BULBASAUR("bulbasaur.png"), 	// 이상해
	CHARMANDER("charmander.png"), 	// 피아리
	CYNDAQUIL("cyndaquil.png"), 	// 브케인 
	EEVEE("eevee.png"),				// 이브이 
	JIGGLYPUFF("jigglypuff.png"),	// 푸린 
	PIKACHU("pikachu.png"), 		// 피카추 
	SQUIRTLE("squirtle.png"), 		// 꼬부기
	POKEBALL("pokeball.png"),		// 체크 용도 
	DRAGONITE("dragonite.png"), 	// 망나뇽  특수 아이템 
	DUSKULL("duskull.png"), 		// 해골  특수 아이템
	SNORLAX("snorlax.png"); 		// 잠맘보  특수 아이템
	private Image image;
	private Pokemon(String fileName){
		this.image = new Image(fileName);
	}
	public Image getImage(){
		return image;
	}
}
