package gui;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import graph_components.Bullets;
import graph_components.Feathers;
import graph_components.MoorhunhBird;
import graph_components.MoorhunhBirdSheet;
import graph_components.NormalSlideMoorhunhBird;
import graph_components.SlideMoorhunhBird;
import graph_components.Sniper;
import rafgfxlib.GameFrame;
import rafgfxlib.Util;

public class AppFrame extends GameFrame {

	private static Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

	// Ovde cuvamo ptice. Pri cemu je KEY u hasmap-i ptica(MoorhunhBird), a
	// VALUE
	// predstavlja smer letenja ptica ("left" or "right").
	private HashMap<MoorhunhBird, String> flyingBirds;
	private SlideMoorhunhBird normalBird;
	private SlideMoorhunhBird psychoBird;

	public static final int WIDTH = 900;
	public static final int HEIGHT = 700;

	private MoorhunhBirdSheet birdSheetLeft;
	private MoorhunhBirdSheet birdSheetRight;
	private MoorhunhBirdSheet deadBirdSheet;
	private MoorhunhBirdSheet invertBirdSheetRight;
	private MoorhunhBirdSheet invertBirdSheetLeft;
	private MoorhunhBirdSheet invertDeadBirdSheet;
	private MoorhunhBirdSheet sheetForScoreNumbers;

	private BufferedImage backgroundImage;
	private BufferedImage sniperImage;
	private BufferedImage startCover;

	private Sniper sniper;
	Bullets bullets;
	private boolean gameActive;

	private int offX;
	private int offY;

	private AudioClip shootSound;
	private AudioClip emptySound;
	private AudioClip reloadSound;
	private AudioClip deadSound;

	private long startTime;
	private long playTime;
	private int currentTimeMinutes;
	private int currentTimeSeconds;
	private long durationEffect;
	private long startEffect;
	private long pshyBirdStartEffect;

	private int score;

	private Feathers feathers;

	private boolean effectActive;

	private boolean pshyBirdEffectActive;

	public AppFrame() {

		// Postavljamo prozor aplikacije na sredinu ekrana = NEUSPESNO
		super("RAF Game", WIDTH, HEIGHT);
		// super("RAF Game",800, 600);
		setHighQuality(true);
		// System.out.println(this.getX());
		// centerFrame();
		// System.out.println(this.getX());

		initComponents();

		// Stavljamo da kursor misa bude unvisible
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Cursor c = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
		this.setCursor(c);
		initBird();
		initGameWindow();
		this.getWindow().setLocationRelativeTo(null);

		offX = 0;
		offY = 0;
		music();
		startThread();
	}

	public void initComponents() {
		setUpdateRate(60);
		bullets = new Bullets(8);
		sniper = new Sniper();
		backgroundImage = Util.loadImage("pictures/background.jpg");
		// BufferedImage im = Util.loadImage("pictures/final.png");
		sniperImage = Util.loadImage("pictures/sniper.png");
		shootSound = Applet.newAudioClip(getClass().getResource("/sounds/shootsound.wav"));
		reloadSound = Applet.newAudioClip(getClass().getResource("/sounds/reloadsound.wav"));
		emptySound = Applet.newAudioClip(getClass().getResource("/sounds/emptysound.wav"));
		deadSound = Applet.newAudioClip(getClass().getResource("/sounds/deadsound.wav"));
		startCover = Util.loadImage("pictures/start.jpg");
		gameActive = false;
		playTime = 60000;
		score = 0;
		feathers = new Feathers();
		durationEffect = 5000;
		effectActive = false;
		pshyBirdEffectActive = false;
	}

	private void centerFrame() {

		Dimension windowSize = getSize();
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Point centerPoint = ge.getCenterPoint();

		int dx = centerPoint.x - windowSize.width / 2;
		int dy = centerPoint.y - windowSize.height / 2;
		this.setLocation(dx, dy);
	}

	public void initBird() {

		birdSheetLeft = new MoorhunhBirdSheet("pictures/birdLeftFinal.png", 4, 4);
		birdSheetLeft.setOffsets(32, 64);

		birdSheetRight = new MoorhunhBirdSheet("pictures/birdRightFinal.png", 4, 4);
		birdSheetRight.setOffsets(32, 64);

		invertBirdSheetRight = new MoorhunhBirdSheet("pictures/birdRightFinal.png", 4, 4);
		invertBirdSheetRight.setOffsets(32, 64);
		invertBirdSheetRight.invertSheet();

		invertBirdSheetLeft = new MoorhunhBirdSheet("pictures/birdLeftFinal.png", 4, 4);
		invertBirdSheetLeft.setOffsets(32, 64);
		invertBirdSheetLeft.invertSheet();

		// inicijalizujem sheet za mrtvu pticu
		deadBirdSheet = new MoorhunhBirdSheet("pictures/deadBird.png", 1, 4);
		deadBirdSheet.setOffsets(32, 64);

		invertDeadBirdSheet = new MoorhunhBirdSheet("pictures/deadBird.png", 1, 4);
		invertDeadBirdSheet.setOffsets(32, 64);
		invertDeadBirdSheet.invertSheet();

		Random r = new Random();
		// initBirds
		normalBird = new NormalSlideMoorhunhBird("pictures/NormalBird.png",
				this.getX() + r.nextInt((this.getWidth() - 300) - this.getX()),
				(this.getY() + this.getHeight() + 200) + r.nextInt(5000 - (this.getY() + this.getHeight() + 200)));

		flyingBirds = new HashMap<>();

		// Konstruisemo 4 ptica koje lete na levu stranu
		for (int i = 0; i < 4; i++) {
			MoorhunhBird bird = new MoorhunhBird(birdSheetLeft, r.nextInt(2000), r.nextInt(HEIGHT - 200));
			bird.setAnimationInterval();
			flyingBirds.put(bird, "left");

		}
		// Konstruisemo 3 ptica koje lete na desnu stranu
		for (int i = 0; i < 3; i++) {
			MoorhunhBird bird = new MoorhunhBird(birdSheetRight, r.nextInt(2000), r.nextInt(HEIGHT - 200));
			bird.setAnimationInterval();
			flyingBirds.put(bird, "right");

		}

	}

	@Override
	public void handleWindowInit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleWindowDestroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(Graphics2D g, int sw, int sh) {
		if (effectActive) {
			AffineTransform old = g.getTransform();
			g.translate(WIDTH, HEIGHT);
			g.rotate(180 * Math.PI / 180);
			g.translate(-0, -0);
			;
		}
		g.setFont(new Font("Gulim", Font.BOLD, 30));
		g.setColor(Color.RED);
		if (gameActive) {
			int x = (backgroundImage.getWidth() - WIDTH) / 2 + offX;
			// Util.clamp(x, 10, getWidth());
			int y = (backgroundImage.getHeight() - HEIGHT) / 2 + offY;
			// Util.clamp(y, 10, getHeight());
			if (x < 0) {
				x = 0;
			}
			if (x > (backgroundImage.getWidth() - WIDTH - 50)) {
				x = (backgroundImage.getWidth() - WIDTH - 50);
			}
			if (y < 0) {
				y = 1;
			}
			if (y > (backgroundImage.getHeight() - HEIGHT - 50)) {
				y = (backgroundImage.getHeight() - HEIGHT - 50);
			}
			// System.out.println(x + ":" + y);
			// System.out.println(backgroundImage.getWidth());
			// System.out.println(getWidth());
			// System.out.println(offX);

			BufferedImage img = backgroundImage;
			img = img.getSubimage(x, y, WIDTH, HEIGHT);
			if (pshyBirdEffectActive) {
				RescaleOp op = new RescaleOp(-1.0f, 255f, null);
				img = op.filter(img, null);
			}
			g.drawImage(img, 0, 0, this);
			// System.out.println(x + " " + y);

			// Iscrtavanje "Normalne" ptice
			((NormalSlideMoorhunhBird) normalBird).draw(g, ((NormalSlideMoorhunhBird) normalBird).getPosX() - 32,
					((NormalSlideMoorhunhBird) normalBird).getPosY() - 64);
			((NormalSlideMoorhunhBird) normalBird).getScore().draw(g);
			// Iscrtavanje ptica
			for (MoorhunhBird moorhunhBird : flyingBirds.keySet()) {
				moorhunhBird.draw(g);
				moorhunhBird.getScore().draw(g);
			}

			// BULLETS
			for (int i = 0; i < bullets.getCurrentNumberOfBullets(); i++) {
				g.drawImage(bullets.getBulletImg(), WIDTH - 50 - i * 30, getHeight() - 100, this);
			}
			if (currentTimeSeconds >= 10) {
				g.drawString("0" + currentTimeMinutes + ":" + currentTimeSeconds, WIDTH - 100, 50);
			} else {
				g.drawString("0" + currentTimeMinutes + ":" + "0" + currentTimeSeconds, WIDTH - 100, 50);
			}
			g.drawString(score + "", 30, 50);
		}
		if (gameActive == false) {
			g.drawImage(startCover, 0, 0, WIDTH, getHeight(), 0, 0, startCover.getWidth(), startCover.getHeight(),
					this);
			if (score != 0) {
				g.setFont(new Font("Gulim", Font.BOLD, 50));
				g.drawString(score + "", 210, 450);
				g.drawString("Score:", 30, 450);
			}
			// g.drawRect(25, 255, 280, 60);
			// g.drawRect(85, 335, 150, 60);

		}
		// Iscrtavanje Snipera na ekranu i pamcenje kordinata u modelu
		sniper.setX(getMouseX());
		sniper.setY(getMouseY());
		sniper.setWidth(sniperImage.getWidth());
		sniper.setHeight(sniperImage.getHeight());
		g.drawImage(sniperImage, getMouseX() - 20, getMouseY() - 20, this);
		feathers.render(g, sw, sh);

	}

	@Override
	public void update() {
		if (gameActive == true) {
			Random r = new Random();
			feathers.update();

			for (MoorhunhBird bird : flyingBirds.keySet()) {

				// Proveravamo da li je ptica mrtva, ako jeste , proveravamo da
				// li
				// je izasla
				// sa donjeg dela ekrana ako jeste onda je vracamo u zivot i
				// postavljamo na rand poziciju
				if (bird.isDead()) {
					bird.setMovingSpeed(8);
					bird.move(0, bird.getMovingSpeed());
					if (bird.getPositionY() > this.getY() + this.getHeight() && flyingBirds.get(bird).equals("left")) {
						bird.setMovingSpeed(r.nextInt(3) + 1);
						// bird.setMovingSpeed(r.nextInt(2) + 2);
						bird.setRowInSheetID(r.nextInt(3));
						int minW = this.getX() + this.getWidth() + bird.getSheet().getFrameWidth();
						int maxW = 2 * WIDTH;
						int minH = 50;
						int maxH = HEIGHT - 320;
						bird.setPosition(minW + r.nextInt(maxW - minW), minH + r.nextInt(maxH - minH));
						bird.setAnimationInterval();
						bird.setDead(false);
						int p = r.nextInt(4);
						if (p == 1) {
							bird.setBirdSheet(invertBirdSheetLeft);
						} else {
							bird.setBirdSheet(birdSheetLeft);
						}
					}
					if (bird.getPositionY() > this.getY() + this.getHeight() && flyingBirds.get(bird).equals("right")) {
						bird.setMovingSpeed(r.nextInt(3) + 1);
						// bird.setMovingSpeed(r.nextInt(2)+1);
						bird.setRowInSheetID(r.nextInt(3));
						int minW = -2 * WIDTH;
						int maxW = this.getX() - bird.getSheet().getFrameWidth();
						int minH = 50;
						int maxH = HEIGHT - 320;
						bird.setPosition(minW + r.nextInt(maxW - minW), minH + r.nextInt(maxH - minH));
						bird.setAnimationInterval();
						bird.setDead(false);
						int p = r.nextInt(4);
						if (p == 1) {
							bird.setBirdSheet(invertBirdSheetRight);
						} else {
							bird.setBirdSheet(birdSheetRight);
						}
					}

				}

				// Proveravamo da li ptica leti u levo, ako da, pomeramo je u
				// levo i
				// proveravamo da li izlazi iz sa frame-a u levo, ako je izasla,
				// vracamo je
				// na random poziciju u desno sa random velicinom i random
				// brzinom
				// kretanja(brzinom pokreta krila).
				if (flyingBirds.get(bird).equals("left") && !bird.isDead()) {
					bird.move(-(bird.getMovingSpeed()), 0);
					if (bird.getPositionX() + bird.getSheet().getFrameWidth() < this.getX()) {
						// bird.setMovingSpeed(r.nextInt(3) + 3);

						bird.setMovingSpeed(r.nextInt(3) + 1);
						bird.setRowInSheetID(r.nextInt(3));
						int minW = this.getX() + this.getWidth() + bird.getSheet().getFrameWidth();
						int maxW = 2 * WIDTH;
						int minH = 50;
						int maxH = HEIGHT - 320;
						bird.setPosition(minW + r.nextInt(maxW - minW), minH + r.nextInt(maxH - minH));
						bird.setAnimationInterval();
						bird.setDead(false);
						// Stvaranje random invert kokoske
						int p = r.nextInt(4);

						if (p == 1) {

							bird.setBirdSheet(invertBirdSheetLeft);
						} else {
							bird.setBirdSheet(birdSheetLeft);
						}
					}
				}
				// ako ptica ne lete u lefo (leti u desno) pomeramo je u desno i
				// radimo isto
				// kao i za "levo" samo sto je pomeramo na random poziciju levo.
				if (flyingBirds.get(bird).equals("right") && !bird.isDead()) {
					bird.move(bird.getMovingSpeed(), 0);
					if (bird.getPositionX() > this.getX() + this.getWidth()) {
						// bird.setMovingSpeed(r.nextInt(3) + 3);
						bird.setMovingSpeed(r.nextInt(3) + 1);
						bird.setRowInSheetID(r.nextInt(3));
						int minW = -2 * WIDTH;
						int maxW = this.getX() - bird.getSheet().getFrameWidth();
						int minH = 50;
						int maxH = HEIGHT - 320;
						bird.setPosition(minW + r.nextInt(maxW - minW), minH + r.nextInt(maxH - minH));
						bird.setAnimationInterval();
						bird.setDead(false);
						// Stvaranje random invert kokoske
						int p = r.nextInt(4);
						if (p == 1) {
							bird.setBirdSheet(invertBirdSheetRight);
						} else {
							bird.setBirdSheet(birdSheetRight);
						}
					}

				}

				bird.getScore().move(0, -(bird.getScore().getMovingSpeed()));
				bird.update();
			}

			// Pozivamo update za normalnu kokosku
			((NormalSlideMoorhunhBird) normalBird).update(this);
			((NormalSlideMoorhunhBird) normalBird).getScore().move(0,
					-(((NormalSlideMoorhunhBird) normalBird).getScore().getMovingSpeed()));

			int mouseX = getMouseX();
			int mouseY = getMouseY();

			if (mouseX >= getWidth() - 100) {
				offX += 10;
			} else if (mouseX <= 100) {
				offX -= 10;
			} /*
				 * else if (mouseY >= getHeight() - 100) { offY += 10; } else if
				 * (mouseY <= 100) { offY -= 10; }
				 */
		}
		int[] curTime = getTime();
		currentTimeMinutes = curTime[1];
		currentTimeSeconds = curTime[2];
		// System.out.println(curTime[0] + " : " + curTime[1] + " : " +
		// curTime[2] + " : " + curTime[3]);
		if (curTime[1] <= 0 && curTime[2] <= 0) {
			gameActive = false;
		}

		if (durationEffect - (System.currentTimeMillis() - startEffect) <= 0) {
			effectActive = false;
		}

		if (durationEffect - (System.currentTimeMillis() - pshyBirdStartEffect) <= 0) {
			pshyBirdEffectActive = false;
		}

	}

	public int[] getTime() {
		long milliTime = playTime - (System.currentTimeMillis() - this.startTime);
		int[] out = new int[] { 0, 0, 0, 0 };
		out[0] = (int) (milliTime / 3600000);
		out[1] = (int) (milliTime / 60000) % 60;
		out[2] = (int) (milliTime / 1000) % 60;
		out[3] = (int) (milliTime) % 1000;

		return out;
	}

	public void music() {
		URL url = Util.class.getClassLoader().getResource("sounds/bgsound.wav");
		try {
			AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File(url.toURI()));
			Clip clip = AudioSystem.getClip();
			clip.open(inputStream);
			clip.loop(Clip.LOOP_CONTINUOUSLY);
		} catch (LineUnavailableException | IOException | UnsupportedAudioFileException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void playSound(AudioClip clip) {
		clip.play();
	}

	public void restart() {
		score = 0;
		bullets.setCurrentNumberOfBullets(bullets.getMaxNumberOfBullets());
		effectActive = false;
	}

	@Override
	public void handleMouseDown(int x, int y, GFMouseButton button) {
		// TODO Auto-generated method stub
		if (button == GFMouseButton.Left) {
			if (bullets.getCurrentNumberOfBullets() > 0) {
				playSound(shootSound);
				for (MoorhunhBird bird : flyingBirds.keySet()) {
					if (gameActive) {

						// Proveravamo za svaku pticurinu intersekciju tj. da li
						// je
						// pogodjena,
						// ako jeste menjamo joj odgovarajuci sheet i
						// postavljamo da
						// je mrtva
						if (flyingBirds.get(bird).equals("left") && bird.getRowInSheetID() == 0
								&& sniper.contains(bird.getPositionX(), bird.getPositionY(),
										bird.getSheet().getFrameWidth() - 90, bird.getSheet().getFrameHeight() - 85)) {
							System.out.println("Pogodili smo pticurinu levu, veliku");
							if (bird.getSheet().isInvert()) {
								bird.setBirdSheet(invertDeadBirdSheet);
							} else {
								bird.setBirdSheet(deadBirdSheet);
							}

							bird.setRowInSheetID(0);
							bird.setPosition(bird.getPositionX() - 10, bird.getPositionY() - 20);
							bird.setDead(true);
							playSound(deadSound);
							feathers.genEx(x, y, 10.0f, 300, 50);
							if (bird.getSheet().isInvert()) {
								score -= 5;
								startEffect = System.currentTimeMillis();
								effectActive = true;
								(bird.getScore()).setRowInSheetID(5);
								(bird.getScore()).setPosition(bird.getPositionX(), bird.getPositionY());

							} else {
								score += 5;
								(bird.getScore()).setRowInSheetID(0);
								(bird.getScore()).setPosition(bird.getPositionX(), bird.getPositionY());
							}
						}

					}

					if (flyingBirds.get(bird).equals("left") && bird.getRowInSheetID() == 1
							&& sniper.contains(bird.getPositionX() + 27, bird.getPositionY(),
									bird.getSheet().getFrameWidth() - 103, bird.getSheet().getFrameHeight() - 85)) {
						System.out.println("Pogodili smo pticurinu levu, srednju");
						if (bird.getSheet().isInvert()) {
							bird.setBirdSheet(invertDeadBirdSheet);
						} else {
							bird.setBirdSheet(deadBirdSheet);
						}
						bird.setRowInSheetID(1);
						bird.setPosition(bird.getPositionX() - 10, bird.getPositionY() - 20);
						bird.setDead(true);
						playSound(deadSound);
						feathers.genEx(x, y, 10.0f, 300, 50);
						if (bird.getSheet().isInvert()) {
							score -= 10;
							startEffect = System.currentTimeMillis();
							effectActive = true;
							(bird.getScore()).setRowInSheetID(6);
							(bird.getScore()).setPosition(bird.getPositionX(), bird.getPositionY());
						} else {
							score += 10;
							(bird.getScore()).setRowInSheetID(1);
							(bird.getScore()).setPosition(bird.getPositionX(), bird.getPositionY());
						}
					}

					if (flyingBirds.get(bird).equals("left") && bird.getRowInSheetID() == 2
							&& sniper.contains(bird.getPositionX() + 40, bird.getPositionY() + 10,
									bird.getSheet().getFrameWidth() - 120, bird.getSheet().getFrameHeight() - 92)) {
						System.out.println("Pogodili smo pticurinu levu, malu");
						if (bird.getSheet().isInvert()) {
							bird.setBirdSheet(invertDeadBirdSheet);
						} else {
							bird.setBirdSheet(deadBirdSheet);
						}
						bird.setRowInSheetID(2);
						bird.setPosition(bird.getPositionX() - 20, bird.getPositionY() - 80);
						bird.setDead(true);
						playSound(deadSound);
						feathers.genEx(x, y, 10.0f, 300, 50);
						if (bird.getSheet().isInvert()) {
							score -= 15;
							startEffect = System.currentTimeMillis();
							effectActive = true;
							(bird.getScore()).setRowInSheetID(7);
							(bird.getScore()).setPosition(bird.getPositionX(), bird.getPositionY());
						} else {
							score += 15;
							(bird.getScore()).setRowInSheetID(2);
							(bird.getScore()).setPosition(bird.getPositionX(), bird.getPositionY());
						}
					}
					if (flyingBirds.get(bird).equals("left") && bird.getRowInSheetID() == 3
							&& sniper.contains(bird.getPositionX() + 43, bird.getPositionY() + 20,
									bird.getSheet().getFrameWidth() - 137, bird.getSheet().getFrameHeight() - 115)) {
						System.out.println("Pogodili smo pticurinu levu, najmanju");
						if (bird.getSheet().isInvert()) {
							bird.setBirdSheet(invertDeadBirdSheet);
						} else {
							bird.setBirdSheet(deadBirdSheet);
						}
						bird.setRowInSheetID(3);
						bird.setPosition(bird.getPositionX() - 20, bird.getPositionY() - 80);
						bird.setDead(true);
						playSound(deadSound);
						feathers.genEx(x, y, 10.0f, 300, 50);
						if (bird.getSheet().isInvert()) {
							score -= 20;
							startEffect = System.currentTimeMillis();
							effectActive = true;
							(bird.getScore()).setRowInSheetID(8);
							(bird.getScore()).setPosition(bird.getPositionX(), bird.getPositionY());
						} else {
							score += 20;
							(bird.getScore()).setRowInSheetID(3);
							(bird.getScore()).setPosition(bird.getPositionX(), bird.getPositionY());
						}
					}

					if (flyingBirds.get(bird).equals("right") && bird.getRowInSheetID() == 0
							&& sniper.contains(bird.getPositionX() + 57, bird.getPositionY(),
									bird.getSheet().getFrameWidth() - 90, bird.getSheet().getFrameHeight() - 85)) {
						System.out.println("Pogodili smo pticurinu desnu, veliku");
						if (bird.getSheet().isInvert()) {
							bird.setBirdSheet(invertDeadBirdSheet);
						} else {
							bird.setBirdSheet(deadBirdSheet);
						}
						bird.setRowInSheetID(0);
						bird.setPosition(bird.getPositionX() - 20, bird.getPositionY() - 80);
						bird.setDead(true);
						playSound(deadSound);
						feathers.genEx(x, y, 10.0f, 300, 50);
						if (bird.getSheet().isInvert()) {
							score -= 5;
							startEffect = System.currentTimeMillis();
							effectActive = true;
							(bird.getScore()).setRowInSheetID(5);
							(bird.getScore()).setPosition(bird.getPositionX(), bird.getPositionY());
						} else {
							score += 5;
							(bird.getScore()).setRowInSheetID(0);
							(bird.getScore()).setPosition(bird.getPositionX(), bird.getPositionY());
						}
					}

					if (flyingBirds.get(bird).equals("right") && bird.getRowInSheetID() == 1
							&& sniper.contains(bird.getPositionX() + 63, bird.getPositionY(),
									bird.getSheet().getFrameWidth() - 90, bird.getSheet().getFrameHeight() - 85)) {
						System.out.println("Pogodili smo pticurinu desnu, srednju");
						if (bird.getSheet().isInvert()) {
							bird.setBirdSheet(invertDeadBirdSheet);
						} else {
							bird.setBirdSheet(deadBirdSheet);
						}
						bird.setRowInSheetID(1);
						bird.setPosition(bird.getPositionX() - 20, bird.getPositionY() - 80);
						bird.setDead(true);
						playSound(deadSound);
						feathers.genEx(x, y, 10.0f, 300, 50);
						if (bird.getSheet().isInvert()) {
							score -= 10;
							startEffect = System.currentTimeMillis();
							effectActive = true;
							(bird.getScore()).setRowInSheetID(6);
							(bird.getScore()).setPosition(bird.getPositionX(), bird.getPositionY());
						} else {
							score += 10;
							(bird.getScore()).setRowInSheetID(1);
							(bird.getScore()).setPosition(bird.getPositionX(), bird.getPositionY());
						}
					}

					if (flyingBirds.get(bird).equals("right") && bird.getRowInSheetID() == 2
							&& sniper.contains(bird.getPositionX() + 69, bird.getPositionY() + 13,
									bird.getSheet().getFrameWidth() - 113, bird.getSheet().getFrameHeight() - 92)) {
						System.out.println("Pogodili smo pticurinu desnu, malu");
						if (bird.getSheet().isInvert()) {
							bird.setBirdSheet(invertDeadBirdSheet);
						} else {
							bird.setBirdSheet(deadBirdSheet);
						}
						bird.setRowInSheetID(2);
						bird.setPosition(bird.getPositionX() - 20, bird.getPositionY() - 80);
						bird.setDead(true);
						playSound(deadSound);
						feathers.genEx(x, y, 10.0f, 300, 50);
						if (bird.getSheet().isInvert()) {
							score -= 15;
							startEffect = System.currentTimeMillis();
							effectActive = true;
							(bird.getScore()).setRowInSheetID(7);
							(bird.getScore()).setPosition(bird.getPositionX(), bird.getPositionY());
						} else {
							score += 15;
							(bird.getScore()).setRowInSheetID(2);
							(bird.getScore()).setPosition(bird.getPositionX(), bird.getPositionY());
						}
					}

					if (flyingBirds.get(bird).equals("right") && bird.getRowInSheetID() == 3
							&& sniper.contains(bird.getPositionX() + 75, bird.getPositionY() + 20,
									bird.getSheet().getFrameWidth() - 136, bird.getSheet().getFrameHeight() - 113)) {
						System.out.println("Pogodili smo pticurinu desnu, najmanju");
						if (bird.getSheet().isInvert()) {
							bird.setBirdSheet(invertDeadBirdSheet);
						} else {
							bird.setBirdSheet(deadBirdSheet);
						}
						bird.setRowInSheetID(3);
						bird.setPosition(bird.getPositionX() - 20, bird.getPositionY() - 80);
						bird.setDead(true);
						playSound(deadSound);
						feathers.genEx(x, y, 10.0f, 300, 50);
						if (bird.getSheet().isInvert()) {
							score -= 20;
							startEffect = System.currentTimeMillis();
							effectActive = true;
							(bird.getScore()).setRowInSheetID(8);
							(bird.getScore()).setPosition(bird.getPositionX(), bird.getPositionY());
						} else {
							score += 20;
							(bird.getScore()).setRowInSheetID(3);
							(bird.getScore()).setPosition(bird.getPositionX(), bird.getPositionY());
						}
					}
				}

				if (!((NormalSlideMoorhunhBird) normalBird).isDead()
						&& sniper.contains(((NormalSlideMoorhunhBird) normalBird).getPosX() + 110,
								((NormalSlideMoorhunhBird) normalBird).getPosY() + 100,
								((NormalSlideMoorhunhBird) normalBird).getImage().getWidth() - 230,
								((NormalSlideMoorhunhBird) normalBird).getImage().getHeight())) {
					System.out.println("Pogodili smo slide pticu");
					if (((NormalSlideMoorhunhBird) normalBird).isPsycho()) {
						// OVDE IMPLEMENTIRATI EFEKAT ZA PSY PTICU
						pshyBirdStartEffect = System.currentTimeMillis();
						pshyBirdEffectActive = true;
					}
					((NormalSlideMoorhunhBird) normalBird).setDead(true);
					playSound(deadSound);
					if (((NormalSlideMoorhunhBird) normalBird).isPsycho()) {
						score -= 25;
						(((NormalSlideMoorhunhBird) normalBird).getScore()).setRowInSheetID(9);
						(((NormalSlideMoorhunhBird) normalBird).getScore()).setPosition(
								((NormalSlideMoorhunhBird) normalBird).getPosX(),
								((NormalSlideMoorhunhBird) normalBird).getPosY());
					} else {
						score += 25;
						(((NormalSlideMoorhunhBird) normalBird).getScore()).setRowInSheetID(4);
						(((NormalSlideMoorhunhBird) normalBird).getScore()).setPosition(
								((NormalSlideMoorhunhBird) normalBird).getPosX(),
								((NormalSlideMoorhunhBird) normalBird).getPosY());
					}
				}

				if (gameActive == true)
					bullets.setCurrentNumberOfBullets(bullets.getCurrentNumberOfBullets() - 1);
			} else {
				playSound(emptySound);
			}

		}
		if (button == GFMouseButton.Right) {
			if (bullets.getCurrentNumberOfBullets() == 0) {
				bullets.setCurrentNumberOfBullets(bullets.getMaxNumberOfBullets());
				playSound(reloadSound);
			}
		}

		if (gameActive == false) {
			if (button == GFMouseButton.Left) {
				if (getMouseX() > 25 && getMouseX() < 305 && getMouseY() > 255 && getMouseY() < 305) {
					// System.out.println("new game");
					gameActive = true;
					startTime = System.currentTimeMillis();
					restart();
				}
				if (getMouseX() > 85 && getMouseX() < 235 && getMouseY() > 335 && getMouseY() < 395) {
					// System.out.println("quite game");
					getWindow().dispose();
				}
			}
		}
	}

	@Override
	public void handleMouseUp(int x, int y, GFMouseButton button) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleMouseMove(int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleKeyDown(int keyCode) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.VK_SPACE) {
			if (bullets.getCurrentNumberOfBullets() == 0) {
				bullets.setCurrentNumberOfBullets(bullets.getMaxNumberOfBullets());
				playSound(reloadSound);
			}
		}
	}

	@Override
	public void handleKeyUp(int keyCode) {
		// TODO Auto-generated method stub

	}

}
