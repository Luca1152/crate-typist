package ro.luca1152.typing.actors;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import java.util.LinkedList;
import java.util.Queue;

import ro.luca1152.typing.TypingGame;
import ro.luca1152.typing.utils.Util;

@SuppressWarnings("FieldCanBeLocal")
public class Turret extends Image {
    private final TypingGame game;
    private Crate targetCrate;
    private Group bullets;

    private Sprite tempSprite;
    private int queuedBullets = 0;
    private boolean isFirstBullet = false;
    private Crate tempTargetCrate;

    Turret(TypingGame game, float x, float y) {
        super(game.getManager().get("textures/turret.png", Texture.class));
        setPosition(x, y);
        setOrigin(36f, 36f);
        tempSprite = new Sprite();
        tempSprite.setPosition(x - 5, y - 5);
        tempSprite.setOrigin(36 + 5f, 36 + 5f);
        tempSprite.setSize(82, 108);
        this.game = game;
        queuedBulletsTargets = new LinkedList<Crate>();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (targetCrate != null) {
            rotateTo(targetCrate);
        }
        tempSprite.setRotation(getRotation());
        shootQueuedBullets();
    }

    @Override
    protected void setParent(Group parent) {
        super.setParent(parent);
        if (parent != null) {
            this.bullets = ((GameMap) getParent()).getBullets();
        }
    }

    private void rotateTo(Actor targetCrate) {
        float angle = Util.getAngleBetween(this, targetCrate);
        if (getActions().size == 0 && !isFirstBullet)
            setRotation(angle);
    }

    private void shootQueuedBullets() {
        for (int i = 0; i < realQueuedBullets; i++) {
            if (targetCrate == null) break;
            targetCrate.shootAt();
            realQueuedBullets--;
            if (targetCrate.lastLife()) {
                targetCrate.removeLabel();
                removeTargetCrate();
            }
        }
        if (isFirstBullet) {
            float targetAngle = Util.getAngleBetween(this, targetCrate);
            float difference = getRotation() - targetAngle;
            if (Math.abs(difference) > 180)
                addAction(Actions.rotateBy(Math.abs(Math.abs(difference) - 360) * ((difference >= 0) ? 1 : -1), .15f));
            else
                addAction(Actions.rotateTo(targetAngle, .15f));
            isFirstBullet = false;
        }
        if (getActions().size == 0 && queuedBullets > 0) {
            float middleX = ((tempSprite.getVertices()[SpriteBatch.X2] + tempSprite.getVertices()[SpriteBatch.X3]) / 2f);
            float middleY = ((tempSprite.getVertices()[SpriteBatch.Y2] + tempSprite.getVertices()[SpriteBatch.Y3]) / 2f);
            for (int i = 0; i < queuedBullets; i++) {
                Bullet bullet = new Bullet(game, queuedBulletsTargets.element().getLastFirstCharFromWord(), middleX, middleY, queuedBulletsTargets.element());
                queuedBulletsTargets.poll();

                game.singleKeySound.play();
                bullets.addActor(bullet);
                queuedBullets--;
//                }
            }
        }
    }

    public void removeTargetCrate() {
        tempTargetCrate.setIsTargetCrate(false);
        targetCrate = null;
    }

    private int realQueuedBullets = 0;

    public void shoot(boolean isFirstBullet) {
        // Surprised this even works, to be fair
        if (!isFirstBullet)
            queuedBulletsTargets.add((Crate) targetCrate.getParent().getChildren().get(targetCrate.getParent().getChildren().indexOf(targetCrate, true)));
        ((GameMap) (bullets.getParent())).increaseScoreMultiplier();
        queuedBullets++;
        realQueuedBullets++;
        this.isFirstBullet = isFirstBullet;
    }

    public Crate getTargetCrate() {
        return targetCrate;
    }

    private Queue<Crate> queuedBulletsTargets;

    public void setTargetCrate(Crate targetCrate) {
        // Surprised this even works, to be fair
        queuedBulletsTargets.add((Crate) targetCrate.getParent().getChildren().get(targetCrate.getParent().getChildren().indexOf(targetCrate, true)));
        targetCrate.setIsTargetCrate(true);
        if (targetCrate != null)
            this.tempTargetCrate = targetCrate;
        this.targetCrate = targetCrate;
    }
}
