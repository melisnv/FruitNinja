package com.example.fruitninja;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Random;

public class FruitNinja extends ApplicationAdapter implements InputProcessor {
	SpriteBatch batch;
	Texture background;
	Texture apple,bill,bomb,cherry,pear;

	BitmapFont bitmapFont;
	FreeTypeFontGenerator fontGen;

	int lives = 4;
	int scores  = 0;

	float genCounter = 0;
	private final float startGenSpeed = 1.1f;
	float genSpeed = startGenSpeed;

	Random random = new Random();
	Array<Fruit> fruitArray = new Array<Fruit>();

	private double currentTime = 0;
	private double gameOverTime = -1.0f;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		background = new Texture("background.png");

		apple = new Texture("apple.png");
		bill = new Texture("bill.png");
		bomb = new Texture("bomb.png");
		cherry = new Texture("cherry.png");
		pear = new Texture("pear.png");

		Fruit.radius = Math.max(Gdx.graphics.getHeight(),Gdx.graphics.getWidth()) / 20f;

		Gdx.input.setInputProcessor(this);

		fontGen = new FreeTypeFontGenerator(Gdx.files.internal("robotobold.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter params = new FreeTypeFontGenerator.FreeTypeFontParameter();
		params.color = Color.WHITE;
		params.size = 40;
		params.characters = "0123456789 ScreCutoplay:.+-";
		bitmapFont = fontGen.generateFont(params);
	}

	@Override
	public void render () {
		batch.begin();

		batch.draw(background,0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());

		double newTime = TimeUtils.millis() / 1000.0;
		double frameTime = Math.min(newTime - currentTime,0.3);
		float deltaTime = (float) frameTime; // frameTime'ın float versiyonu
		currentTime = newTime;

		if (lives <= 0 && gameOverTime == 0f){
			// game over
			gameOverTime = currentTime;
		}
		if(lives > 0){
			// game mode

			genSpeed -= deltaTime * 0.015f;
			if (genCounter <= 0f){
				genCounter = genSpeed;
				addItem();
			}
			else{
				genCounter -= deltaTime;
			}

			// to represent our lives with apples
			for (int i =0 ; i<= lives; i++){
				batch.draw(apple,i*30f + 20f,Gdx.graphics.getHeight()-35f,30f,30f);
			}

			for (Fruit fruit : fruitArray){
				fruit.update(deltaTime);

				switch (fruit.type){
					case REGULAR:
						batch.draw(apple,fruit.getPos().x,fruit.getPos().y,Fruit.radius,Fruit.radius);
						break;
					case ENEMY:
						batch.draw(bomb,fruit.getPos().x,fruit.getPos().y,Fruit.radius,Fruit.radius);
						break;
					case LIFE:
						batch.draw(bill,fruit.getPos().x,fruit.getPos().y,Fruit.radius,Fruit.radius);
						break;
					case EXTRA:
						batch.draw(cherry,fruit.getPos().x,fruit.getPos().y,Fruit.radius,Fruit.radius);
						break;
				}
			}

			boolean holdLives = false;

			Array<Fruit> toRemove = new Array<Fruit>();
			for (Fruit fruit : fruitArray){
				if (fruit.outOfScreen()){
					toRemove.add(fruit);

					if (fruit.living && fruit.type == Fruit.Type.REGULAR){
						lives--;
						holdLives = true;
						break;
					}
				}
			}

			if (holdLives){
				for (Fruit f : fruitArray){
					f.living = false; // böylelikle tüm meyveler yere düşse de canım azalmayacak
				}
			}

			for (Fruit f : toRemove){
				fruitArray.removeValue(f,true);
			}
		}

		bitmapFont.draw(batch,"Score: " + scores,45,40);
		if (lives <= 0) {
			bitmapFont.draw(batch,"Cut to play",Gdx.graphics.getHeight()*0.5f,Gdx.graphics.getWidth()*0.5f);
		}
		batch.end();
	}

	public void addItem(){

		float pos = random.nextFloat() * Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getWidth());
		Fruit item = new Fruit(new Vector2(pos,-Fruit.radius),
				new Vector2((Gdx.graphics.getWidth() * 0.5f - pos) * (0.3f + (random.nextFloat() - 0.5f)),
							(Gdx.graphics.getHeight() * 0.5f)));

		float type = random.nextFloat();
		if (type > 0.98){
			item.type = Fruit.Type.LIFE;
		}
		else if (type > 0.88){
			item.type = Fruit.Type.EXTRA;
		}
		else if (type > 0.78) {
			item.type = Fruit.Type.ENEMY;
		}

		fruitArray.add(item);
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		bitmapFont.dispose();
		fontGen.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (lives <= 0 && currentTime - gameOverTime > 2f) {
			// menu mode ( cut to play)
			scores = 0;
			gameOverTime= 0;
			lives = 4;
			genSpeed = startGenSpeed;
			fruitArray.clear();
		} else{
			// game mode ( if a fruit clicked delete it part)
			Array<Fruit> toRemove = new Array<Fruit>(); // çıkartılacaklar listesi
			Vector2 pos = new Vector2(screenX,Gdx.graphics.getHeight() - screenY); // kullanıcının tıkladığı position

			int plusScore = 0;

			for (Fruit f : fruitArray){
				if (f.clicked(pos)){
					toRemove.add(f);

					switch (f.type){
						case LIFE:
							lives++;
							break;
						case ENEMY:
							lives--;
							break;
						case EXTRA:
							plusScore += 2;
							break;
						case REGULAR:
							plusScore++;
							break;
					}
				}
			}

			scores += plusScore * plusScore;

			for (Fruit f : toRemove){
				fruitArray.removeValue(f,true);
			}
		}
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		return false;
	}
}
