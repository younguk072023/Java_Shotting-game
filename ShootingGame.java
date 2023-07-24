import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class ShootingGame extends JFrame {
    private Image bufferImage;			//더블 버퍼링을 위한 이미지.
    private Graphics screenGraphic;		//더블 버퍼링을 위한 그래픽 객체

    private Image mainScreen = new ImageIcon("src/images/main_screen.png").getImage();		//게임 화면에 사용될 이미지들.
    private Image loadingScreen = new ImageIcon("src/images/loading_screen.png").getImage();
    private Image gameScreen = new ImageIcon("src/images/game_screen.png").getImage();

    private boolean isMainScreen, isLoadingScreen, isGameScreen;		//게임 화면에 사용될 이미지들 

    private Game game = new Game();				//게임의 주요 로직을 처리하는 객체

    private Audio backgroundMusic;				//배경 음악을 재생하는 객체

    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;

    public ShootingGame() {					//shootingGame 생성자
        setTitle("Shooting Game");
        setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setLayout(null);
        init();
    }

    private void init() {			//게임 초기화 메소드
        isMainScreen = true;
        isLoadingScreen = false;
        isGameScreen = false;

        backgroundMusic = new Audio("src/audio/menuBGM.wav", true);			//배경 음악 객체 생성 및 재생
        backgroundMusic.start();

        addKeyListener(new keyListener());						//키 이벤트 리스너 등록
    }

    private void gameStart() {
        isMainScreen = false;			
        isLoadingScreen = true;

        Timer loadingTimer = new Timer();		//로딩 화면을 3초 동안 보여주고 게임 화면으로 전환
        TimerTask loadingTask = new TimerTask() {
            @Override
            public void run() {
                backgroundMusic.stop();
                isLoadingScreen = false;
                isGameScreen = true;
                game.startGame();
            }
        };
        loadingTimer.schedule(loadingTask, 3000);
    }

    public void paint(Graphics g) {
        bufferImage = createImage(SCREEN_WIDTH, SCREEN_HEIGHT);
        screenGraphic = bufferImage.getGraphics();
        screenDraw(screenGraphic);
        g.drawImage(bufferImage, 0, 0, null);
    }

    public void screenDraw(Graphics g) {
        if (isMainScreen) {
            g.drawImage(mainScreen, 0, 0, null);
        }
        if (isLoadingScreen) {
            g.drawImage(loadingScreen, 0, 0, null);
        }
        if (isGameScreen) {
            g.drawImage(gameScreen, 0, 0, null);
            game.gameDraw(g);
        }

        this.repaint();
    }

    class keyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_W:
                    game.setUp(true);			//플레이어의 위 이동 키를 누르면 설정
                    break;
                case KeyEvent.VK_S:
                    game.setDown(true);			//플레이어의 아래 이동 키를 누르면 설정
                    break;
                case KeyEvent.VK_A:
                    game.setLeft(true);			//플레이어의 왼쪽 이동 키를 누르면 설정
                    break;
                case KeyEvent.VK_D:
                    game.setRight(true);		//플레이어의 오른쪽 이동 키를 누르면 설정
                    break;
                case KeyEvent.VK_R:				//게임이 종료되었을 때 R키를 누르면 재시작
                    if (game.isOver()){
                    	game.startGame();
                    	game.reset();}
                    break;
                case KeyEvent.VK_SPACE:			//스페이스바를 누르면 플레이어 공격 설정
                    game.setShooting(true);
                    break;
                case KeyEvent.VK_ENTER:			//엔터 키를 누르면 게임 시작
                    if (isMainScreen) {
                        gameStart();
                    }
                    break;
                case KeyEvent.VK_ESCAPE:		//ESC 키를 누르면 게임 종료
                    System.exit(0);
                    break;
            }
        }

        public void keyReleased(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_W:			//플레이어의 위 이동 키를 떼면 해제
                    game.setUp(false);
                    break;
                case KeyEvent.VK_S:			//플레이어의 아래 이동 키를 떼면 해제
                    game.setDown(false);
                    break;
                case KeyEvent.VK_A:			//플레이어의 왼쪽 이동 키를 떼면 해제
                    game.setLeft(false);
                    break;
                case KeyEvent.VK_D:			//플레이어의 오른쪽 이동 키를 떼면 해제
                    game.setRight(false);
                    break;
                case KeyEvent.VK_SPACE:		//스페이스바를 떼면 플레이어 공격 해제
                    game.setShooting(false);
                    break;
            }
        }

    }

    public static void main(String[] args) {
        new ShootingGame();				//프로그램의 시작지점
    }
}

class Audio {						//오디오 클래스
    private Clip clip;
    private File audioFile;
    private AudioInputStream audioInputStream;
    private boolean isLoop;

    public Audio(String pathName, boolean isLoop) {
        try {
            clip = AudioSystem.getClip();
            audioFile = new File(pathName);
            audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            clip.open(audioInputStream);
            this.isLoop = isLoop;
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        clip.setFramePosition(0);
        clip.start();
        if (isLoop) clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop() {
        clip.stop();
    }
}

class Game {
    private int delay = 20;				//게임의 주기를 나타내는 변수로, 게임의 로직이 일정 주기마다 실햄됨.
    private long pretime;				//이전 게임 로직 실행 시점의 시간을 저장하는 변수로, 주기적인 로직 실행 간격을 조절하기 위해 사용함.
    private int cnt;					//게임 진행 횟수를 나타내는 변수로, 게임이 실행된 횟수를카운트함.
    private int score;					//플레이어의 점수를 나타내는 변수.

    private Image Player = new ImageIcon("src/Images/player.png").getImage();

    private int playerX, playerY;		//플레이어의 현재 위치를 나타내는 변수로, 게임 화면 상에서 플레이어의 가로 위치롸 세로 위치를 저장함.
    private int playerWidth = Player.getWidth(null);	//플레이엉 이미지의 가로 길이와 세로 길이를 나타내는 변수로, 플레이어 충돌 판정 등에 용
    private int playerHeight = Player.getHeight(null);	
    private int playerSpeed = 10;						//플레이어의 이동 속도
    private int playerHp = 30;							//플레이어의 hp체력

    private boolean up, down, left, right, shooting;
    private boolean isOver;						//종료 상태를 나타내는 변수로 게임이 종료되면 true 게임이 진행중이면 false

    private ArrayList<PlayerAttack> playerAttackList = new ArrayList<>();		
    private ArrayList<Enemy> enemyList = new ArrayList<>();
    private ArrayList<EnemyAttack> enemyAttackList = new ArrayList<>();

    private PlayerAttack playerAttack;
    private Enemy enemy;
    private EnemyAttack enemyAttack;

    private Audio backgroundMusic;
    private Audio hitSound;

    public void startGame() {			//게임을 시작하는 역할 담당하는 클래스
        reset();		//초기화

        Timer gameTimer = new Timer();		
        TimerTask gameTask = new TimerTask() {
            @Override
            public void run() {					
                if (!isOver) {			//만약에 종료가	아니면
                    keyProcess();		//밑에 함수 호출
                    playerAttackProcess();
                    enemyAppearProcess();
                    enemyMoveProcess();
                    enemyAttackProcess();
                    cnt++;
                } else {			//아니면  게임 종료
                    gameTimer.cancel();		//gameTask 객체를 중지함. 이로써 게임의 실행이 종료됨.
                }
            }
        };
        gameTimer.schedule(gameTask, delay, delay);
    }

    public void reset() {
        isOver = false;				//isOver변수를 false로 설정하고 이렇게 하면 게임이 다시 시작될 때 게임이 진행중임.
        cnt = 0;					//게임 진행 횟수 초기화함. cnt변수는 게임이 진행된 횟수를 나타내는 변수로 사용되며, 이를 초기화하여 새로운 게임이 시작할 때 적절한 게임 진행 횟수를 설정함.
        score = 0;					//플레이어 점수 초기화
        playerX = 10;				//플레이어의 x좌표를 초기 위치로 설정함.
        playerY = (ShootingGame.SCREEN_HEIGHT - playerHeight) / 2;		//플레이어의 y좌표를 초기 위치로 설정함. 	playerY변수는 플레이어의 세로 위치를 나타냄.
        playerHp = 30;													//플레이어의 체력을 30으로 초기화함.
        
        up=false;														//플레이어의 이동 및 공격 상태를 초기화함.
        down=false;
        left=false;
        right=false;
        shooting=false;

        
        hitSound = new Audio("src/audio/Pew-Pew-Sound.wav", false);		//효과음 재생 객체를 초기화함. hitsound 변수는 플레이어가 적과 충돌했을 때 재생되는 효과음

       

        playerAttackList.clear();					//플레이어의 공격, 적, 적의 공격을 담고 있는 리스트들을 모두 비움.즉, 초기화된 게임에서 이전에 생성되었던 플레이어의 공격, 적 적의 공격 객체들을 모두 제거
        enemyList.clear();							//이를 통해 새로운 게임이 시작될 때 이전의 객체들이 영향을 미치지 않도록 함.
        enemyAttackList.clear();
    }

    private void keyProcess() {
        if (up && playerY - playerSpeed > 0) playerY -= playerSpeed;	//만약 up키가 눌렸고 플레이어의 위치에서 플레이어 속도를 뺀 값이 0보다 크면 플레이어의 y좌표를 위로 이동시키고 즉, 화면 위쪽 경계를 넘어가지 않도록설정함.
        if (down && playerY + playerHeight + playerSpeed < ShootingGame.SCREEN_HEIGHT) playerY += playerSpeed;	//만약 아래쪽down키를 눌렀고,플레이어의 위치에서 플레이어 높이와 플레이어 속도를 더한 값이 화면 높이 보다 작으면, 플레이어의 y좌표를 아래로 이동시킴. 즉 화면 아래쪽 경계를 넘어가지 않도록 아래로 이동함.
        if (left && playerX - playerSpeed > 0) playerX -= playerSpeed;	//왼쪽 화살표키 left가 눌렸고, 플레이어의 위치에서 플레이어 속도를 뺀 값이 0보다 크면, 플레이어의 x좌표를 왼족으로 이동시킴. 즉, 화면 왼쪽 경계를 넘어가지 않도록 왼쪽으로 이동함.
        if (right && playerX + playerWidth + playerSpeed < ShootingGame.SCREEN_WIDTH) playerX += playerSpeed;	//오른쪽 화살표rigt키가 눌렸고, 플레이어의 위치에서 플레이어 너비와 플레이어 속도를 더한 값이 화면 너비보다 작으면, 플레이어의 x좌표를 오른쪽으로 이동시킴.즉, 화면 오른쪽 경계를 넘어가지 않도록 오른쪽으로 이동함.
        if (shooting && cnt % 15 == 0) {		//스페이스바키가 눌렸고 cnt변수를 15로 나눈 나머지가 0이면 아래코드를 실행함.
            playerAttack = new PlayerAttack(playerX + 222, playerY + 25);		//플레이어의 위치에서 222,25에 플레이어 공격 개게를 생성함.
            playerAttackList.add(playerAttack);			//생성한 플레이어 공격 객체를 playAttackList에 추가함.
        }
    }

    private void playerAttackProcess() {
        for (int i = 0; i < playerAttackList.size(); i++) {		//playerAttackList에 있는 모든 플레이어의 공격을 순회함.
            playerAttack = playerAttackList.get(i);				//현재 순회 중인 플레이어의 공격을 playerAttack 변수에 저장함.
            playerAttack.fire();								//플레이어의 공격을 발사하고 fire메소드를 호출하여 공격이 발사

            for (int j = 0; j < enemyList.size(); j++) {		//플레이어의 공격와 충돌하는 적을 찾기 위해 enemyList에 있는 모든 적들을 순회함.
                enemy = enemyList.get(j);				
                if (playerAttack.x > enemy.x && playerAttack.x < enemy.x + enemy.width &&
                        playerAttack.y > enemy.y && playerAttack.y < enemy.y + enemy.height) {	
                    enemy.hp -= playerAttack.attack;
                    playerAttackList.remove(playerAttack);		//만약 플레이어의 공격과 적의 위치가 겹치는지 확인하고 만약 충동하면 진행.
                }
                if (enemy.hp <= 0) {						//적의 체력이 0이하인 경우 
                    hitSound.start();						//적이 제거되었을때 사운드 시작
                    enemyList.remove(enemy);				//enemyList에서 제거함.
                    score += 1000;							//적을 제거하여 점수를 1000점 증가시킴.
                }
            }
        }
    }

    private void enemyAppearProcess() {
        if (cnt % 80 == 0) {		
            enemy = new Enemy(1120, (int) (Math.random() * 621));	//적의 위치는 오른쪽 1120에서 랜덤한 y좌표 0~620로 생성
            enemyList.add(enemy);				//enemyList는 게임 화면에 등장하는 모든 적들을 관리하는 ArrayList
        }
    }

    private void enemyMoveProcess() {		//enemyMoveProcess를 이용해서 적이 움직임.
        for (int i = 0; i < enemyList.size(); i++) {		//enemyList에 있는 모든 적들을 순회함.
            enemy = enemyList.get(i);					//enemyList에 현재 순회 중인 적을 가져옴.
            enemy.move();								//가져온 적에 대해서 move 메소드를 호출함. 

            if (enemy.x < 0) {							//만약 적의 x좌표가 0보다 작아지면(화면 왼쪽 경계를 넘어가면)
                score -= 1000;							//점수에서 1000을 감소시킴.
                enemyList.remove(enemy);				//해당적을 enemyList에서 제거함.
            }
        }
        
        if(score<=-5000) {								//만약에 점수가 -5000이하로 떨어지면 
        	isOver=true;								//게임이 종료됨.
        }
    }

    private void enemyAttackProcess() {
        if (cnt % 50 == 0) {						//게임의 진행횟수가 50으로 나누어 떨어질 때마다 
            enemyAttack = new EnemyAttack(enemy.x - 79, enemy.y + 35);		//현재 적의 위치를 기준으로 EnemyAttack객체를 생성함. 이때 적의 위치에서 x좌표를 79만큼 왼쪽으로 이동시키고, y좌표를 35만큼 아래로 이동시킴. 이렇게 함으로써 공격이 적의 위치에서 발사되도록 함.
            enemyAttackList.add(enemyAttack);		//적의객체를 enemyAttacList에 추가함.
        }
        for (int i = 0; i < enemyAttackList.size(); i++) {		//enemyAttackList에 추가함.
            enemyAttack = enemyAttackList.get(i);				//현재 순회 중인 적의 공격 객체를 가져옴.
            enemyAttack.fire();									//가져온 적의 공격에 대해서 fire메소드를 호출함.

            if (enemyAttack.x > playerX & enemyAttack.x < playerX + playerWidth &&
                    enemyAttack.y > playerY && enemyAttack.y < playerY + playerHeight) {		//만약 적의 공격이 플레이어의 위치와 겹친다면,
                hitSound.start();		//공격이 플레이어에게 도달했을 때 소리를 재생함.
                playerHp -= enemyAttack.attack;		//플레이어의 체력에서 적의 공격력을 enemyAttaxck.attack만큼 감소시킴.
                enemyAttackList.remove(enemyAttack);			//적의 공격 객체를 enemyAttackList에서 제거함.
                if (playerHp <= 0) isOver = true;				//만약 플레이어의 체력이 0이하로 떨어지면 isover변수를 true로 설정하여 게임을 종료함.
            }
        }
    }

    public void gameDraw(Graphics g) {
        playerDraw(g);
        enemyDraw(g);
        infoDraw(g);
    }

    public void infoDraw(Graphics g) {
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        
        // 플레이어 HP를 왼쪽 하단에 표시하도록 변경
        g.drawString("HP: " + playerHp, 40, ShootingGame.SCREEN_HEIGHT - 20);
        g.fillRect(40, ShootingGame.SCREEN_HEIGHT-80, playerHp*15,20);
        
        // 점수 표시를 오른쪽 하단에 표시하도록 변경
        g.drawString("SCORE: " + score, ShootingGame.SCREEN_WIDTH - 300, ShootingGame.SCREEN_HEIGHT - 20);

        if (isOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Batang", Font.BOLD, 40));
            g.drawString("재 시작은 R키", 295, 380);
        }
    }

    public void playerDraw(Graphics g) {
        g.drawImage(Player, playerX, playerY, null);
        for (int i = 0; i < playerAttackList.size(); i++) {
            playerAttack = playerAttackList.get(i);
            g.drawImage(playerAttack.image, playerAttack.x, playerAttack.y, null);
        }
    }

    public void enemyDraw(Graphics g) {						//enemyList에 있는 모든 적들을 순회하면서 그려줌.
        for (int i = 0; i < enemyList.size(); i++) {
            enemy = enemyList.get(i);
            g.drawImage(enemy.image, enemy.x, enemy.y, null);		//적의 이미지를 enemy.x,enemy.y 위치에 그려줌
            g.setColor(Color.GREEN);
            g.fillRect(enemy.x + 1, enemy.y - 40, enemy.hp * 15, 20);	//enemy.hp기준으로 너비 결정
        }
        for (int i = 0; i < enemyAttackList.size(); i++) {		
            enemyAttack = enemyAttackList.get(i);
            g.drawImage(enemyAttack.image, enemyAttack.x, enemyAttack.y, null);
        }
    }

    public boolean isOver() {				//게임이 종료되었는지를 나타내는 변수
        return isOver;
    }

    public void setUp(boolean up) {			
        this.up = up;
    }

    public void setDown(boolean down) {
        this.down = down;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public void setShooting(boolean shooting) {
        this.shooting = shooting;
    }
}

class PlayerAttack {
    Image image = new ImageIcon("src/Images/player_attack.png").getImage();		//플레이어의 공격 이미지를 로드함.
    int x, y;																	//플레이어의 공격 위치 좌표를 나타내는 변수임.
    int width = image.getWidth(null);											//플레이어 공격 이미지의 가로 크기를 저장함.
    int height = image.getHeight(null);											//플레이어 공격 이미지의 세로 크기를 저장함.
    int attack = 5;																//플레이어의 공격력을 나타내는 변수임.

    public PlayerAttack(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void fire() {								//플레이어 공격을 오른쪽으로 이동시키는 메소드.
        this.x += 15;									//플레이어 공격이 매 프레임마다 오른쪽으로 15의 속도로 날아감.
    }
}

class Enemy {		//적구현
    Image image = new ImageIcon("src/images/enemy.png").getImage();		//적 enemy 이미지
    int x, y;															//적의 현재 위치 좌표를 나타내는 변수
    int width = image.getWidth(null);									//적의 이미지의 가로 크기를 저장함.
    int height = image.getHeight(null);									//적 이미지의 세로 크기를 저장함.
    int hp = 10;														//적의 체력hp를 표시

    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void move() {
        this.x -= 7;			//적을 왼쪽으로 7의속도로 이동시킴.
    }
}

class EnemyAttack {			//적 공격 구현
    Image image = new ImageIcon("src/images/enemy_attack.png").getImage();		//적 enemy의 attack 이미지
    int x, y;																	//적의 공격 위치 좌표를 나타내는 변수
    int width = image.getWidth(null);											//적의 공격 이미지의 가로 크기를 저장함.
    int height = image.getHeight(null);											//적의 공격 이미지의 세로 크기를 저장함.
    int attack = 5;																//적의 공격력을 나타내는 변수

    public EnemyAttack(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void fire() {														//적의 공격을 왼쪽으로 이동시키는 메소드
        this.x -= 12;															//적의 공격이 매 프레임마다 왼쪽으로 12의 속도로 날아감.
    }
}
