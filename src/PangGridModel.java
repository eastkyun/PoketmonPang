import java.sql.Time;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import javax.sound.midi.ControllerEventListener;

/**
 * 한국기술교육대학교 컴퓨터공학부
 * 2018년도 1학기 학기 프로젝트: 포켓몬팡
 * @author 김상진 
 * 포켓몬팡의 모델 클래스: 게임 로직 구현
 */

public class PangGridModel {
	private Pokemon[][] gridData 
		= new Pokemon[PangUtility.NUMBEROFMONS][PangUtility.NUMBEROFMONS];
	private Random randomGen = new Random();
	private int score = 0;
	private int comboCount = 0;
	private int pangCount = 0;
	/*
	 * 특수 아이템 관련 멤버들
	 */
	private boolean ghostTime = false;
	private boolean dragonTime = false;
	private boolean snorlaxTime = false;
	/*
	 * 뷰 및 뷰와 통신할 때 사용하는 데이터
	 */
	private PangGridView view;
	private ModelToViewData data = new ModelToViewData(new ArrayList<Location>(), gridData);
	
	public PangGridModel(PangGridView view){
		this.view = view;
	}
	public void initAssign(){
		//randomAssign();
		debugAssign();
		//debugGridDataOutput();
		
		int[][] checkMatrix = new int[PangUtility.GRIDSIZE][PangUtility.GRIDSIZE];
		if(checkColumn(checkMatrix)){
			for(int r=0; r<PangUtility.GRIDSIZE; r++)
				for(int c=0; c<PangUtility.GRIDSIZE; c++)
					if(checkMatrix[r][c]==2) changePokemon(r,c);
		}
		//debugGridDataOutput();
		checkMatrix = new int[PangUtility.GRIDSIZE][PangUtility.GRIDSIZE];

		if(checkRow(checkMatrix)){
			for(int r=0; r<PangUtility.GRIDSIZE; r++)
				for(int c=0; c<PangUtility.GRIDSIZE; c++)
					if(checkMatrix[r][c]==2) changePokemon(r,c);
		}
		//debugGridDataOutput(); 
		data.locationList.clear();
		view.updateGrid(data);
		// 점수, 콤보 수 초기화
		score = comboCount = 0;
		// 특수 아이템 관련 초기화 
		ghostTime = false;
		updateCombo(0);
		pangCount = 0;
		view.updateScore(score+"");
	}
	/**
	 * 7개 포켓몬들을 임의로 배치 
	 */
	private void randomAssign(){
		for(int r=0; r<gridData.length; r++){
			for(int c=0; c<gridData[r].length; c++){
				gridData[r][c] = Pokemon.values()[randomGen.nextInt(PangUtility.NUMBEROFMONS)];
			}
		}
	}
	/**
	 * 주어진 위치의 포켓몬을 바꾸어 팡을 제거함 
	 * 주변 4개와 다른 포켓몬으로 교체
	 */
	private void changePokemon(int r, int c){
		Set<Integer> set = new HashSet<>();
		Collections.addAll(set, 0,1,2,3,4,5,6);
		if(Location.valid(r-1,c)) set.remove(gridData[r-1][c].ordinal());
		if(Location.valid(r+1,c)) set.remove(gridData[r+1][c].ordinal());
		if(Location.valid(r,c-1)) set.remove(gridData[r][c-1].ordinal());
		if(Location.valid(r,c+1)) set.remove(gridData[r][c+1].ordinal());
		
		int idx = randomGen.nextInt(set.size());
		Integer[] list = new Integer[set.size()];
		set.toArray(list);
		gridData[r][c] = Pokemon.values()[list[idx]];
	}
	/**
	 * 행 기준으로 팡의 존재를 검사  
	 * 팡이 발견된 위치는 모두 1로 바꾸고
	 * 초기 팡 제거를 위해 포켓몬을 바꾸어야 하는 위치는 2로 바꿈
	 */
	private boolean checkRow(int[][] checkMatrix){
		boolean found = false;
		for(int r=0; r<PangUtility.GRIDSIZE; r++){
			for(int c=0; c<PangUtility.GRIDSIZE-2; c++){
				if(gridData[r][c].ordinal()<PangUtility.NUMBEROFMONS){
					int count = 1;
					for(int i=c+1; i<PangUtility.GRIDSIZE; i++){
						if(gridData[r][c]==gridData[r][i]) ++count;
						else break;
					}
					if(count>=3){
						updateScore(count);
						found = true;
						pangCount++;
						if(comboCount==0) {
							comboCount=1;
							
						}
						updateCombo(comboCount);
						
						
						for(int i=c; i<c+count; i++) checkMatrix[r][i] = 1;
						checkMatrix[r][c+2] = 2;
						if(count>=6) checkMatrix[r][c+5] = 2;
						if(count==5) ghostTime = true;
						if(pangCount%10==0) dragonTime = true;
						if(comboCount%10==0) snorlaxTime = true;
					}
					c += (count-1);
				}
			}
		}
		
		// debug
		// debugCheckMatrixOutput(checkMatrix);	
		return found;
	}
	/**
	 * 열 기준으로 팡의 존재를 검사  
	 * 팡이 발견된 위치는 모두 1로 바꾸고
	 * 초기 팡 제거를 위해 포켓몬을 바꾸어야 하는 위치는 2로 바꿈
	 */
	private boolean checkColumn(int[][] checkMatrix){
		boolean found = false;
		for(int c=0; c<PangUtility.GRIDSIZE; c++){
			for(int r=0; r<PangUtility.GRIDSIZE-2; r++){
				if(gridData[r][c].ordinal()<PangUtility.NUMBEROFMONS){
					int count = 1;
					for(int i=r+1; i<PangUtility.GRIDSIZE; i++){
						if(gridData[r][c]==gridData[i][c]) ++count;
						else break;
					}
					if(count>=3){
						updateScore(count);
						found = true;
						pangCount++;
//						System.out.println(pangCount);
						updateCombo(comboCount);
						
						for(int i=r; i<r+count; i++) checkMatrix[i][c] = 1;
						checkMatrix[r+2][c] = 2;
						if(count>=6) checkMatrix[r+5][c] = 2;
						if(count==5) ghostTime = true;
						if(pangCount%10==0) dragonTime = true;
						if(comboCount%10==0) snorlaxTime = true;
					}
					r += (count-1);
				}
			}
		}
		
		// debug
		// debugCheckMatrixOutput(checkMatrix);
		return found;
	}
	/*
	 * 점수 계산 방식:
	 * 1) 제거한 포켓몬 수 x 100
	 * 2) 5개 연속짜리 제거할 경우 보너스 500점 추가
	 * 3) 현재 콤보 수 x 100
	 */
	private void updateScore(int count){
		score += count*100;
		if(count==5) score += 500;
		//score += comboCount*100;
		view.updateScore(score+"");
		
	}
	public void updateCombo(int val){
		if(val==0) comboCount = 0;
		else ++comboCount;
		view.updateCombo(comboCount+"");
	}
	/**
	 * 힌트 위치를 모두 찾음
	 */
	boolean findHints(){
		boolean hasSpecialPokemon = false;
		ArrayList<Location> hintLocs = new ArrayList<>();
		for(int r=0; r<PangUtility.GRIDSIZE; r++){
			for(int c=0; c<PangUtility.GRIDSIZE; c++){
				if(gridData[r][c].ordinal()>=PangUtility.NUMBEROFMONS)
					hasSpecialPokemon = true;
				if(leftEquals(r,c)||rightEquals(r,c)||upEquals(r,c)||downEquals(r,c)){
					hintLocs.add(new Location(r,c));
				}
			}
		}
		if(!hintLocs.isEmpty()){
			// debug
			/*
			for(Location loc: hintLocs){
				System.out.print("("+loc.r+","+loc.c+"), ");
			}
			System.out.println();
			*/
			// debug
			int idx = randomGen.nextInt(hintLocs.size());
			view.updateHintLoc(hintLocs.get(idx));
			return true;
		}
		else{
			view.updateHintLoc(null);
			return hasSpecialPokemon;
		}
	}
	/*
	 * 특수아이템이 없는 랜덤 위치 확보
	 */
	private Location getNormalLoc(){
		Location location = null;
		while(true){
			int row = randomGen.nextInt(PangUtility.GRIDSIZE);
			int col = randomGen.nextInt(PangUtility.GRIDSIZE);
			if(gridData[row][col].ordinal()<PangUtility.NUMBEROFMONS){
				location = new Location(row, col);
				break;
			}
		}
		return location;
	}
	public void insertSpecialPokemon(){
		data.locationList.clear();
		if(ghostTime){
			Location ghostLoc = getNormalLoc();
			gridData[ghostLoc.r][ghostLoc.c] = Pokemon.DUSKULL;
			ghostTime = false;
			data.locationList.add(ghostLoc);
		}
		else if(dragonTime) {
			Location dragonLoc = getNormalLoc();
			gridData[dragonLoc.r][dragonLoc.c] = Pokemon.DRAGONITE;
			dragonTime = false;
			data.locationList.add(dragonLoc);
		}
		else if(snorlaxTime) {
			Location snorlaxLoc = getNormalLoc();
			gridData[snorlaxLoc.r][snorlaxLoc.c] = Pokemon.SNORLAX;
			snorlaxTime = false;
			data.locationList.add(snorlaxLoc);
		}
		view.updateGrid(data);
	}
	
	public boolean processSpecialPokemon(int r,int c){
		switch(gridData[r][c]){
		case DRAGONITE: clearRowAndColumn(r, c); return true;
		case DUSKULL: removeRandomPokemon(r,c); return true;
		case SNORLAX: increaseGameTime(r,c); return true;
		default: return false;
		}
	}
	private void clearRowAndColumn(int row, int col){
		int count = 1;
		for(int r=0; r<PangUtility.NUMBEROFMONS; r++){
			for(int c=0; c<PangUtility.NUMBEROFMONS; c++){
				if(r==row) {
					for(int i=0; i<PangUtility.NUMBEROFMONS; i++) {
						gridData[r][i]=Pokemon.POKEBALL;
						++count;
					}
				}
				if(c==col) {
					for(int i=0; i<PangUtility.NUMBEROFMONS; i++) {
						gridData[i][c]=Pokemon.POKEBALL;
						++count;						
					}
				}
			}
		}
		count--;
		score += 100*count;
		view.updateScore(score+"");
	}
	private void removeRandomPokemon(int row, int col){
		Pokemon removePokemon = Pokemon.values()[randomGen.nextInt(7)];
		int count = 1;
		for(int r=0; r<PangUtility.NUMBEROFMONS; r++){
			for(int c=0; c<PangUtility.NUMBEROFMONS; c++){
				if(gridData[r][c]==removePokemon){
					gridData[r][c]=Pokemon.POKEBALL;
					++count;
				}
			}
		}
		gridData[row][col]=Pokemon.POKEBALL;
		score += 100*count;
		view.updateScore(score+"");
	}
	private void increaseGameTime(int r, int c){
		
	}
	// 특수 아이템 관련 메소드 끝
	/**
	 * 팡이 있는 곳에 포켓몬을 포켓몬공으로 바꾸세요.
	 */
	public boolean checkAndMark(){
		int[][] checkMatrix = new int[PangUtility.NUMBEROFMONS][PangUtility.NUMBEROFMONS];
		boolean flag = checkColumn(checkMatrix);
		flag = checkRow(checkMatrix)||flag;
		if(flag){
			data.locationList.clear();
			for(int r=0; r<PangUtility.NUMBEROFMONS; r++)
				for(int c=0; c<PangUtility.NUMBEROFMONS; c++)
					if(checkMatrix[r][c]>=1){
						gridData[r][c] = Pokemon.POKEBALL;
						data.locationList.add(new Location(r,c));
					}
			view.updateGrid(data);
			return true;
		}
		return false;
	}
	/*
	 * 팡 위치 위에 있는 포켓몬들을 아래로 내려 포켓볼들이 맨 위에 위치하도록 함
	 * 열 단위로 작업
	 * 각 열마다 마직막 행부터 검사
	 * 		볼을 만나면 위치 기록
	 * 		볼을 만난 이후 포켓몽을 처음 만나면 최초 볼 위치와 교환
	 * 		그 다음 볼 위치 찾음
	 *  23@42@6 => 23@4@26 => 23@@426 => 2@@3426 => @@23426
	 *  다른 방법: 23@42@6 > 23426 추출 (큐에 삽입) > @@23426로 변경
	 */
	public void pushUpMarked(){
		/*
		for(int c=0; c<PangUtility.GRIDSIZE; c++){
			int ballLoc = -1;
			for(int r=PangUtility.GRIDSIZE-1; r>=0; r--){
				if(ballLoc==-1&&gridData[r][c]==Pokemon.POKEBALL){
					ballLoc = r;
				}
				else if(ballLoc!=-1&&gridData[r][c]!=Pokemon.POKEBALL){
					gridData[ballLoc][c] = gridData[r][c];
					gridData[r][c] = Pokemon.POKEBALL;
					int prevLoc = ballLoc;
					ballLoc = -1;
					for(int i=prevLoc-1; i>=0; i--)
						if(gridData[i][c]==Pokemon.POKEBALL){
							ballLoc = i; 
							break;
						}
				}
			}
		}*/
		Queue<Pokemon> queue = new ArrayDeque<>();
		for(int c=0; c<PangUtility.GRIDSIZE; c++){
			queue.clear();
			boolean hasBall = false;
			for(int r=PangUtility.GRIDSIZE-1; r>=0; r--){
				if(gridData[r][c]==Pokemon.POKEBALL) hasBall = true;
				else queue.add(gridData[r][c]);
			}
			//System.out.println(queue.toString());
			if(hasBall){
				int r = PangUtility.GRIDSIZE-1;
				while(!queue.isEmpty()){
					gridData[r][c] = queue.poll();
					--r;
				}
				for(;r>=0;r--) gridData[r][c]=Pokemon.POKEBALL;
			}
		}
	}
	/**
	 * 포켓몬공이 있는 위치를 새로운 랜덤 포켓몬으로 교체함
	 */
	public void replaceMarked(){
		data.locationList.clear();
		/*
		for(int r=0; r<PangUtility.NUMBEROFMONS; r++){
			for(int c=0; c<PangUtility.NUMBEROFMONS; c++){
				if(gridData[r][c]==Pokemon.POKEBALL){
					gridData[r][c] = Pokemon.values()[randomGen.nextInt(PangUtility.NUMBEROFMONS)];
				}
			}
		}
		*/
		// 볼 있는 위치만 교체 
		for(int c=0; c<PangUtility.GRIDSIZE; c++){
			boolean hasPokeball = false;
			for(int r=0; r<PangUtility.GRIDSIZE; r++){
				if(gridData[r][c]==Pokemon.POKEBALL){
					gridData[r][c] = Pokemon.values()[randomGen.nextInt(PangUtility.NUMBEROFMONS)];
				}
				else break;
			}
			if(hasPokeball){
				for(int r=0; r<PangUtility.GRIDSIZE; r++) data.locationList.add(new Location(r,c));
			}
		}
		view.updateGrid(data);
	}
	/**
	 * 사용자가 클릭한 두 개의 셀이 인접한 셀이고 교환하였을 때 팡이 되는지?
	 */
	public boolean isValidSwap(Location srcLoc, Location destLoc){
		if((srcLoc.r==destLoc.r)&&(srcLoc.c==destLoc.c+1)){
			return (leftEquals(srcLoc.r,srcLoc.c)||rightEquals(destLoc.r,destLoc.c));
		}
		else if((srcLoc.r==destLoc.r)&&(srcLoc.c==destLoc.c-1)){
			return (rightEquals(srcLoc.r,srcLoc.c)||leftEquals(destLoc.r,destLoc.c));
		}
		else if((srcLoc.c==destLoc.c)&&(srcLoc.r==destLoc.r+1)){
			return (upEquals(srcLoc.r,srcLoc.c)||downEquals(destLoc.r,destLoc.c));
		}
		else if((srcLoc.c==destLoc.c)&&(srcLoc.r==destLoc.r-1)){
			return (downEquals(srcLoc.r,srcLoc.c)||upEquals(destLoc.r,destLoc.c));
		}
		else return false;
	}
	/**
	 * 사용자가 선택한 두 개의 셀을 실제 교환함
	 */
	public void swap(Location srcLoc, Location destLoc){
		Pokemon tmp = gridData[srcLoc.r][srcLoc.c];
		gridData[srcLoc.r][srcLoc.c] = gridData[destLoc.r][destLoc.c];
		gridData[destLoc.r][destLoc.c] = tmp;
		data.locationList.clear();
		data.locationList.add(srcLoc);
		data.locationList.add(destLoc);
		view.updateGrid(data);
	}
	private boolean equals(Pokemon... list){
		if(list.length<=1) throw new IllegalArgumentException();
		Pokemon p = list[0];
		for(int i=1; i<list.length; i++)
			if(p!=list[i]) return false;
		return true;
	}
	/*
	 *  1) x@xx
	 *  2) 	 x
	 * 		x@
	 *  	 x
	 *  3) 	 x
	 *  	 x
	 *  	x@
	 *  4) 	x@
	 *  	 x
	 *  	 x
	 */
	private boolean rightEquals(int r, int c){
		return 
			((Location.valid(r, c+3) // x@xx
				&&equals(gridData[r][c],gridData[r][c+2],gridData[r][c+3]))||
			(Location.valid(r-1, c+1)&&Location.valid(r+1, c+1)
				&&equals(gridData[r][c],gridData[r-1][c+1],gridData[r+1][c+1]))||
			(Location.valid(r-2,c+1)
				&&equals(gridData[r][c],gridData[r-1][c+1],gridData[r-2][c+1]))||
			(Location.valid(r+2, c+1)
				&&equals(gridData[r][c],gridData[r+1][c+1],gridData[r+2][c+1])));
	}
	/*
	 *  1) xx@x
	 *  2) 	x
	 * 		@x
	 *  	x
	 *  3) 	x
	 *  	x
	 *  	@x
	 *  4) 	@x
	 *  	x
	 *  	x
	 */
	private boolean leftEquals(int r, int c){
		return ((Location.valid(r, c-3)
					&&equals(gridData[r][c],gridData[r][c-2],gridData[r][c-3]))||
			(Location.valid(r-1, c-1)&&Location.valid(r+1, c-1)
					&&equals(gridData[r][c],gridData[r-1][c-1],gridData[r+1][c-1]))||
			(Location.valid(r-2,c-1)
					&&equals(gridData[r][c],gridData[r-1][c-1],gridData[r-2][c-1]))||
			(Location.valid(r+2, c-1)
				&&equals(gridData[r][c],gridData[r+1][c-1],gridData[r+2][c-1])));
	}
	/*
	 *  1) 	x
	 *  	x
	 *  	@
	 *  	x
	 *  2) 	x@x
	 * 		 x
	 *  3) 	xx@
	 *  	  x
	 *  4) 	@xx
	 *  	x
	 */
	private boolean upEquals(int r, int c){
		return ((Location.valid(r-3, c)
					&&equals(gridData[r][c],gridData[r-2][c],gridData[r-3][c]))||
			(Location.valid(r-1, c-1)&&Location.valid(r-1, c+1)
				&&equals(gridData[r][c],gridData[r-1][c-1],gridData[r-1][c+1]))||	
			(Location.valid(r-1,c-2)
				&&equals(gridData[r][c],gridData[r-1][c-1],gridData[r-1][c-2]))||
			(Location.valid(r-1,c+2)
				&&equals(gridData[r][c],gridData[r-1][c+1],gridData[r-1][c+2])));
		
	}
	/*
	 *  1) 	x
	 *  	@
	 *  	x
	 *  	x
	 *  2) 	 x
	 * 		x@x
	 *  3) 	  x
	 *  	xx@
	 *  4) 	x
	 *  	@xx
	 */
	private boolean downEquals(int r, int c){
		return ((Location.valid(r+3, c)
					&&equals(gridData[r][c],gridData[r+2][c],gridData[r+3][c]))||
			(Location.valid(r+1, c-1)&&Location.valid(r+1, c+1)
				&&equals(gridData[r][c],gridData[r+1][c-1],gridData[r+1][c+1]))||		
			(Location.valid(r+1,c-2)
				&&equals(gridData[r][c],gridData[r+1][c-1],gridData[r+1][c-2]))||
			(Location.valid(r+1,c+2)
				&&equals(gridData[r][c],gridData[r+1][c+1],gridData[r+1][c+2])));
	}
	// debug related methods
	private void debugGridDataOutput(){
		for(int r=0; r<gridData.length; r++){
			for(int c=0; c<gridData[r].length; c++){
				System.out.print(gridData[r][c].ordinal()+",");
			}
			System.out.println();
		}
		System.out.println();
	}
	private void debugCheckMatrixOutput(int[][] checkMatrix){
		for(int r=0; r<gridData.length; r++){
			for(int c=0; c<gridData[r].length; c++){
				System.out.print(checkMatrix[r][c]+",");
			}
			System.out.println();
		}
		System.out.println();
	}
	private void debugAssign(){
		int[][] list = {
				{4,4,4,4,5,4,5},
				{3,0,2,2,2,0,4},
				{6,3,6,0,6,0,3},
				{6,3,5,0,2,0,5},
				{2,6,0,2,8,3,5},
				{6,2,3,3,3,5,5},
				{6,1,0,5,1,4,5},
		};
		for(int r=0; r<gridData.length; r++){
			for(int c=0; c<gridData[r].length; c++){
				gridData[r][c] = Pokemon.values()[list[r][c]];
			}
		}
	}
}
