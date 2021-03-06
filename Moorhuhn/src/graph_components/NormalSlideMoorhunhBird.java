package graph_components;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

import rafgfxlib.GameFrame;
import rafgfxlib.Util;

public class NormalSlideMoorhunhBird implements SlideMoorhunhBird {

	
	private int PosX = 0;
	private int PosY = 0;
	
	private int birdDuration = 500;
	private long startTimeBird = 0;
	
	private BufferedImage image;
	private boolean isDead = false;
	private boolean stop = false;
	private boolean goingDown = false;
	private boolean psycho = false;
	private int movingSpeed = 3;
	
	private ScoreNumbers score;
	
	public NormalSlideMoorhunhBird(String nameOfImage, int PosX, int PosY) {
		image = Util.loadImage(nameOfImage);
		this.PosX = PosX;
		this.PosY = PosY;
		Random r = new Random();
		movingSpeed = r.nextInt(3)+5;
		MoorhunhBirdSheet sheetScore = new MoorhunhBirdSheet("pictures/score.png", 1, 10);	
		score = new ScoreNumbers(sheetScore, 0, 0);
		
	}
	
	public void move(int movX, int movY) {
		PosX += movX;
		PosY += movY;
		
	}
	
	public void update(GameFrame frame) {

		Random r = new Random();
		// Proveravamo da li je izasla cela slika, ako jeste stopiramo kretanje
		if (!isDead && this.getPosY() + this.getImage().getHeight() - 70 < frame.getY() + frame.getHeight()) {
			this.setStop(true);
			this.move(0, 6);
			startTimeBird = System.currentTimeMillis();
			birdDuration = 4000;

		}
		// noramalno kretanje ka gore
		if (!isDead && !this.isStop()) {
			if (!this.isGoingDown()) {
				this.move(0, -(this.getMovingSpeed()));
			}
		}
		// stopirano kretanje
		else {
			this.move(0, 0);

		}
		// Proveravamo da li je proslo 3 sekunde, ako jeste spustamo pticu dole
		if (!isDead && startTimeBird != 0 && birdDuration - (System.currentTimeMillis() - startTimeBird) <= 0) {
			this.setGoingDown(true);
			this.setStop(false);
			this.setMovingSpeed(11);

		}
		// krecemo se ka dole
		if (!isDead && !this.isStop() && this.isGoingDown()) {
			this.move(0, this.getMovingSpeed());
		}

		// ako je slika spustena , postavljamo je na random poziciju
		if (this.getPosY() > frame.getY() + frame.getHeight() && this.isGoingDown()) {
			this.setGoingDown(false);
			this.setStop(false);
			this.setDead(false);
			startTimeBird = 0;
			int p = r.nextInt(2);
			if(p == 1){
				this.image = Util.loadImage("pictures/PsyBird.png");
				psycho = true;
			}
			else{
				this.image = Util.loadImage("pictures/NormalBird.png");
				psycho = false;
			}
			this.setMovingSpeed(r.nextInt(3)+5);
			this.setPosition(frame.getX() + r.nextInt((frame.getWidth() - 300) - frame.getX()),
					(frame.getY() + frame.getHeight() + 200)
							+ r.nextInt(10000 - (frame.getY() + frame.getHeight() + 1500)));
		}
		if(isDead){
			this.setMovingSpeed(12);
			this.setGoingDown(true);
			if(isPsycho()){
				this.image = Util.loadImage("pictures/PsyBird.png");
			}
			else{
				this.image = Util.loadImage("pictures/DeadNormalBird.png");
			}
			
			this.move(0, this.getMovingSpeed());
		}

	}

	public void setPosition(int PosX, int PosY){
		this.PosX = PosX;
		this.PosY = PosY;
	}
	
	public int getPosX() {return PosX; }
	public int getPosY() {return PosY; } 
	public boolean isDead() {return isDead; }
	public void setDead(boolean isDead) {this.isDead = isDead; }
	public int getMovingSpeed() {return movingSpeed; }
	public void setMovingSpeed(int movingSpeed) {this.movingSpeed = movingSpeed; }
	

	public boolean isGoingDown() {return goingDown;}
	public void setGoingDown(boolean goingDown) {this.goingDown = goingDown;}
	public boolean isStop() {return stop; }
	public void setStop(boolean stop) {this.stop = stop; }
	public BufferedImage getImage() {return image; }
	
	public boolean isPsycho() {return psycho;}
	public void setPsycho(boolean psycho) {this.psycho = psycho;}

	@Override
	public void draw(Graphics g, int posX, int posY) {
		
		g.drawImage(image, posX, posY, null);
		
	}
	
	public ScoreNumbers getScore() {return score; }
	public void setScore(ScoreNumbers score) {this.score = score; }
	
}
