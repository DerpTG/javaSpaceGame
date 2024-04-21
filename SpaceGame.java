/** Project: Lab 6 Game Assignment
 * Purpose Details: To Demonstrate Game Design Skills Via Java
 * Course: IST 242
 * Author: Felix Naroditskiy
 * Date Developed: 4/15/2024
 * Last Date Changed: 4/21/2024
 * Rev: 1.0
 */

/**
 * Imports necessary for the SpaceGame class.
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.util.Iterator;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

/**
 * Represents an audio player capable of playing audio files.
 */
class AudioPlayer {
    /**
     * Plays audio from a specified file path.
     *
     * @param filePath The path of the audio file to be played.
     */
    public static void play(String filePath) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/**
 * Represents a star in the game.
 */
class Star {
    int x, y;
    Color color;
    int lifetime;

    /**
     * Constructs a new Star object with the specified properties.
     *
     * @param x        The x-coordinate of the star.
     * @param y        The y-coordinate of the star.
     * @param color    The color of the star.
     * @param lifetime The remaining lifetime of the star.
     */
    Star(int x, int y, Color color, int lifetime) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.lifetime = lifetime;
    }
}

/**
 * Represents an obstacle in the game.
 */
class Obstacle {
    Point position;
    int spriteIndex;

    /**
     * Constructs a new Obstacle object with the specified properties.
     *
     * @param x           The x-coordinate of the obstacle.
     * @param y           The y-coordinate of the obstacle.
     * @param spriteIndex The index of the obstacle sprite.
     */
    public Obstacle(int x, int y, int spriteIndex) {
        this.position = new Point(x, y);
        this.spriteIndex = spriteIndex;
    }
}

/**
 * The Main SpaceGame Class.
 */
public class SpaceGame extends JFrame implements KeyListener {
    /**
     * @param random Random number generator for generating random values.
     * @param stars List of stars in the game.
     * @param WIDTH Width of the game window.
     * @param HEIGHT Height of the game window.
     * @param PLAYER_WIDTH Width of the player character.
     * @param PLAYER_HEIGHT Height of the player character.
     * @param OBSTACLE_WIDTH Width of obstacles.
     * @param OBSTACLE_HEIGHT Height of obstacles.
     * @param PROJECTILE_WIDTH Width of projectiles.
     * @param PROJECTILE_HEIGHT Height of projectiles.
     * @param PLAYER_SPEED Speed of the player character.
     * @param OBSTACLE_SPEED Speed of obstacles.
     * @param score Current score in the game.
     * @param health Current health of the player character.
     * @param remainingTime Remaining time in the game.
     * @param levelSelected Flag indicating if a game level is selected.
     * @param gamePanel Panel for rendering the game graphics.
     * @param scoreLabel Label for displaying the current score.
     * @param healthLabel Label for displaying the current health.
     * @param timeLabel Label for displaying the remaining time.
     * @param timer Timer for updating the game state.
     * @param endGameTimer Timer for ending the game.
     * @param shieldTimer Timer for the player's shield power-up.
     * @param powerUpTimer Timer for activating power-ups.
     * @param healthBuffActive Flag indicating if the health power-up is active.
     * @param timeBuffActive Flag indicating if the time power-up is active.
     * @param isGameOver Flag indicating if the game is over.
     * @param shieldUsed Flag indicating if the shield power-up is used.
     * @param playerX X-coordinate of the player character.
     * @param playerY Y-coordinate of the player character.
     * @param projectileX X-coordinate of the projectile.
     * @param projectileY Y-coordinate of the projectile.
     * @param isProjectileVisible Flag indicating if the projectile is visible.
     * @param isFiring Flag indicating if the player character is firing.
     * @param isShieldActive Flag indicating if the shield power-up is active.
     * @param obstacles List of obstacles in the game.
     * @param playerImage Image of the player character.
     * @param obstacleImages Array of obstacle images.
     * @param healthBuff Image of the health power-up.
     * @param timeBuff Image of the time power-up.
     * @param powerUpPosition Position of power-ups in the game.
     */

    private Random random = new Random();
    private List<Star> stars = new ArrayList<>();

    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;
    private static final int PLAYER_WIDTH = 50;
    private static final int PLAYER_HEIGHT = 50;
    private static final int OBSTACLE_WIDTH = 40;
    private static final int OBSTACLE_HEIGHT = 40;
    private static final int PROJECTILE_WIDTH = 5;
    private static final int PROJECTILE_HEIGHT = 15;
    private static final int PLAYER_SPEED = 10;
    private int OBSTACLE_SPEED = 3;
    private static final int PROJECTILE_SPEED = 10;
    private int score = 0;
    private int health = 5;
    private int remainingTime = 30;
    private boolean levelSelected = false;

    private JPanel gamePanel;
    private JLabel scoreLabel;
    private JLabel healthLabel;
    private JLabel timeLabel;
    private Timer timer;
    private Timer endGameTimer;
    private Timer shieldTimer;
    private Timer powerUpTimer;
    private boolean healthBuffActive = false;
    private boolean timeBuffActive = false;
    private boolean isGameOver;
    private boolean shieldUsed = false;
    private int playerX, playerY;
    private int projectileX, projectileY;
    private boolean isProjectileVisible;
    private boolean isFiring;
    private boolean isShieldActive = false;
    private List<Obstacle> obstacles = new ArrayList<>();
    private Image playerImage = new ImageIcon(getClass().getResource("rsH6n.png")).getImage();
    private Image[] obstacleImages = new Image[4];
    private Image healthBuff;
    private Image timeBuff;
    private Point powerUpPosition;

    /**
     * This constructor initializes the game window, sets up the user interface, and initializes
     * game-related variables such as player position, projectile position, and game state.
     */
    public SpaceGame() {
        setTitle("Space Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        loadObstacleSprites();
        gamePanel = new JPanel() {
            /**
             * Overrides the paintComponent method to provide custom rendering of game graphics.
             *
             * @param g Graphics object for rendering.
             */
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draws different components based on game state.
                if (!levelSelected) {
                    drawLevelSelection(g);
                } else if (isGameOver) {
                    drawGameOver(g);
                } else {
                    drawGame(g);
                }
            }
        };

        // Initializes and adds score label to the game panel.
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setBounds(10, 10, 100, 20);
        scoreLabel.setForeground(Color.BLUE);
        gamePanel.add(scoreLabel);

        // Initializes and adds health label to the game panel.
        healthLabel = new JLabel("Health: 5");
        healthLabel.setBounds(10, 0, 100, 20);
        healthLabel.setForeground(Color.ORANGE);
        gamePanel.add(healthLabel);

        // Initializes and adds time label to the game panel.
        timeLabel = new JLabel("Time: " + remainingTime + "s");
        timeLabel.setBounds(10, 50, 100, 20);
        timeLabel.setForeground(Color.WHITE);
        gamePanel.add(timeLabel);

        add(gamePanel);
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(this);

        // Initializes player position.
        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = HEIGHT - PLAYER_HEIGHT - 20;

        // Initializes projectile position.
        projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
        projectileY = playerY;

        // Initializes game state variables.
        isProjectileVisible = false;
        isGameOver = false;
        isFiring = false;
        obstacles = new ArrayList<>();

        // Loads power-up images.
        healthBuff = new ImageIcon(getClass().getResource("heartBuff.png")).getImage();
        timeBuff = new ImageIcon(getClass().getResource("timeBuff.png")).getImage();
        powerUpPosition = new Point(WIDTH / 2 - PLAYER_WIDTH / 2, HEIGHT - PLAYER_HEIGHT - 20);
    }

    /**
     * Starts the game timers responsible for updating game state and managing game events.
     *
     * This method initializes and starts three timers:
     * - The main game timer, which updates the game state and repaints the game panel every 20 milliseconds.
     * - The end game timer, which decrements the remaining time and ends the game when the time runs out.
     * - The power-up timer, which randomly activates health or time buffs every 15 seconds.
     */
    private void startGameTimers() {
        // Main game timer.
        timer = new Timer(20, e -> {
            if (!isGameOver) {
                update();
                gamePanel.repaint();
            }
        });
        timer.start();

        // End game timer.
        endGameTimer = new Timer(1000, e -> {
            remainingTime--;
            timeLabel.setText("Time: " + remainingTime + "s");
            if (remainingTime <= 0) {
                endGame();
            }
        });
        endGameTimer.start();

        // Power-up timer.
        powerUpTimer = new Timer(15000, e -> {
            if (new Random().nextBoolean()) {
                healthBuffActive = true;
            } else {
                timeBuffActive = true;
            }
            gamePanel.repaint();
        });
        powerUpTimer.setRepeats(false);
        powerUpTimer.start();
    }

    /**
     * Stops all active game timers.
     */
    private void stopGameTimers() {
        // Stops the main game timer if it is active.
        if (timer != null) {
            timer.stop();
        }
        // Stops the end game timer if it is active.
        if (endGameTimer != null) {
            endGameTimer.stop();
        }
        // Stops the power-up timer if it is active.
        if (powerUpTimer != null) {
            powerUpTimer.stop();
        }
    }

    /**
     * Starts the shield timer to deactivate the shield after a specified duration.
     *
     * This method initializes and starts a timer to deactivate the shield after 5 seconds.
     */
    private void startShieldTimer() {
        shieldTimer = new Timer(5000, new ActionListener() {
            /**
             * Defines the action to be performed by the shield timer.
             *
             * @param e ActionEvent representing the action performed by the timer.
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                isShieldActive = false;
                shieldTimer.stop();
            }
        });
        shieldTimer.setRepeats(false);
        shieldTimer.start();
    }

    /**
     * Loads obstacle sprites from a sprite sheet image.
     *
     * This method loads obstacle sprites from a sprite sheet image named "spritesheet.png".
     * It extracts each sprite from the sprite sheet and scales it to the specified obstacle width and height.
     * The loaded sprites are stored in the obstacleImages array.
     */
    private void loadObstacleSprites() {
        int spriteWidth = 250;
        int spriteHeight = 250;

        Image spritesheet = new ImageIcon(getClass().getResource("spritesheet.png")).getImage();

        // Iterate over each sprite in the sprite sheet.
        for (int i = 0; i < 4; i++) {
            // Calculate x-offset for current sprite.
            int xOffset = i * (spriteWidth + 30);

            // Create a filtered image source for the current sprite.
            Image sprite = Toolkit.getDefaultToolkit().createImage(
                new FilteredImageSource(spritesheet.getSource(),
                new CropImageFilter(xOffset, 0, spriteWidth - 20, spriteHeight)));

            // Scale the sprite to the specified obstacle width and height.
            obstacleImages[i] = sprite.getScaledInstance(OBSTACLE_WIDTH, OBSTACLE_HEIGHT, Image.SCALE_SMOOTH);
        }
    }

    /**
     * This method draws the level selection screen on the provided Graphics object.
     *
     * @param g The Graphics object on which the level selection screen is drawn.
     */
    private void drawLevelSelection(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Select Level:", WIDTH / 2 - 100, HEIGHT / 2 - 30);
        g.drawString("1 - Easy", WIDTH / 2 - 80, HEIGHT / 2);
        g.drawString("2 - Hard", WIDTH / 2 - 80, HEIGHT / 2 + 30);
    }

    /**
     * Draws the game over screen.
     *
     * @param g The Graphics object on which the game over screen is drawn.
     */
    private void drawGameOver(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Game Over!", WIDTH / 2 - 100, HEIGHT / 2 - 20);
        g.drawString("Press Enter to Play Again.", WIDTH / 2 - 180, HEIGHT / 2 + 20);
    }

    /**
     * Draws the game components on the screen.
     *
     * @param g The Graphics object on which the game components are drawn.
     */
    private void drawGame(Graphics g) {
        // Set the background to black.
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw the player using the player image.
        g.drawImage(playerImage, playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, this);

        // Draw the projectile if it is visible, using a simple rectangle.
        if (isProjectileVisible) {
            g.setColor(Color.GREEN);
            g.fillRect(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        }

        // Draw each obstacle using the obstacle image.
        for (Obstacle obstacle : obstacles) {
            g.drawImage(obstacleImages[obstacle.spriteIndex], obstacle.position.x, obstacle.position.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT, this);
        }

        // Draw the shield if it is active.
        if (isShieldActive) {
            g.setColor(Color.GREEN);
            g.drawOval(playerX - 5, playerY - 5, PLAYER_WIDTH + 10, PLAYER_HEIGHT + 10);
        }

        // Draw stars.
        Iterator<Star> it = stars.iterator();
        while (it.hasNext()) {
            Star star = it.next();
            g.setColor(star.color);
            g.fillOval(star.x, star.y, 4, 4);
            if (--star.lifetime <= 0) {
                it.remove();
            }
        }

        // Draw power-ups if active.
        if (healthBuffActive) {
            g.drawImage(healthBuff, powerUpPosition.x, powerUpPosition.y, PLAYER_WIDTH, PLAYER_HEIGHT, this);
        } else if (timeBuffActive) {
            g.drawImage(timeBuff, powerUpPosition.x, powerUpPosition.y, PLAYER_WIDTH, PLAYER_HEIGHT, this);
        }
    }

    /**
     * Updates the game state for each frame.
     */
    private void update() {
        if (levelSelected && !isGameOver) {
            // Star Updater.
            if (random.nextInt(10) < 1) {
                int x = random.nextInt(WIDTH);
                int y = random.nextInt(HEIGHT);
                Color color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
                stars.add(new Star(x, y, color, 100));
            }

            // Move obstacles.
            Iterator<Obstacle> iterator = obstacles.iterator();
            while (iterator.hasNext()) {
                Obstacle obstacle = iterator.next();
                obstacle.position.y += OBSTACLE_SPEED;
                if (obstacle.position.y > HEIGHT) {
                    iterator.remove();
                }
            }

            // Generate new obstacles.
            if (Math.random() < 0.02) {
                createObstacle();
            }

            // Move projectile.
            if (isProjectileVisible) {
                projectileY -= PROJECTILE_SPEED;
                if (projectileY < 0) {
                    isProjectileVisible = false;
                }
            }

            // Check collision with player.
            Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
            Rectangle powerUpRect = new Rectangle(powerUpPosition.x, powerUpPosition.y, PLAYER_WIDTH, PLAYER_HEIGHT);

            if (healthBuffActive && playerRect.intersects(powerUpRect)) {
                health *= 2;
                healthBuffActive = false;
            }
            if (timeBuffActive && playerRect.intersects(powerUpRect)) {
                remainingTime *= 2;
                timeBuffActive = false;
            }

            iterator = obstacles.iterator();
            while (iterator.hasNext()) {
                Obstacle obstacle = iterator.next();
                Rectangle obstacleRect = new Rectangle(obstacle.position.x, obstacle.position.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
                if (playerRect.intersects(obstacleRect) && !isShieldActive) {
                    health -= 1;
                    AudioPlayer.play("dead.wav");
                    iterator.remove(); // Remove the obstacle that collided with the player.
                    if (health <= 0) {
                        isGameOver = true;
                        stopGameTimers();
                        break; // Exit loop to avoid concurrent modification exception.
                    }
                }
            }

            // Check collision with obstacle.
            Rectangle projectileRect = new Rectangle(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
            Iterator<Obstacle> obstacleIterator  = obstacles.iterator();
            while (obstacleIterator .hasNext()) {
                Obstacle obstacle = obstacleIterator .next();
                Rectangle obstacleRect = new Rectangle(obstacle.position.x, obstacle.position.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
                if (projectileRect.intersects(obstacleRect)) {
                    obstacleIterator .remove();
                    score += 10;
                    isProjectileVisible = false;
                    break;
                }
            }

            scoreLabel.setText("Score: " + score);
            healthLabel.setText("Health: " + health);
        }
    }

    /**
     * Creates a new obstacle and adds it to the list of obstacles.
     */
    private void createObstacle() {
        int obstacleX = random.nextInt(WIDTH - OBSTACLE_WIDTH);
        int spriteIndex = random.nextInt(obstacleImages.length);
        obstacles.add(new Obstacle(obstacleX, -OBSTACLE_HEIGHT, spriteIndex));
    }

    /**
     * Ends the game by setting the game over flag, stopping all game timers, and triggering a repaint of the game panel.
     */
    private void endGame() {
        isGameOver = true;
        stopGameTimers();
        gamePanel.repaint();
    }

    /**
     * Restarts the game by resetting all game state variables, starting game timers, and updating the player's position.
     */
    private void restartGame() {
        // Reset all game state variables
        score = 0;
        health = 5;
        remainingTime = 30;
        startGameTimers();
        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = HEIGHT - PLAYER_HEIGHT - 20;
        projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
        projectileY = playerY;
        isProjectileVisible = false;
        isGameOver = false;
        shieldUsed = false;
        healthBuffActive = false;
        timeBuffActive = false;
        obstacles.clear();
        gamePanel.repaint();
    }

    /**
     * This method overrides the keyPressed method in the KeyListener interface. It listens for key events and
     * performs corresponding actions based on the keys pressed. If no level has been selected, it allows the
     * player to choose between two difficulty levels. Once a level is selected, it enables game controls such as
     * moving the player left or right, firing projectiles, and activating the shield. It also allows the player
     * to restart the game after it's over by pressing the Enter key.
     *
     * @param e The KeyEvent representing the key press event.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        // Handle level selection if no level has been selected
        if (!levelSelected) {
            if (keyCode == KeyEvent.VK_1) {
                levelSelected = true;
                OBSTACLE_SPEED = 3;
                startGameTimers();
            } else if (keyCode == KeyEvent.VK_2) {
                levelSelected = true;
                OBSTACLE_SPEED = 6;
                startGameTimers();
            }
            gamePanel.repaint();
            return;
        }

        // Game controls
        if (!isGameOver) {
            if (keyCode == KeyEvent.VK_LEFT && playerX > 0) {
                playerX -= PLAYER_SPEED;
            } else if (keyCode == KeyEvent.VK_RIGHT && playerX < WIDTH - PLAYER_WIDTH) {
                playerX += PLAYER_SPEED;
            } else if (keyCode == KeyEvent.VK_SPACE && !isFiring) {
                isFiring = true;
                AudioPlayer.play("shoot.wav");
                projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
                projectileY = playerY;
                isProjectileVisible = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500);
                            isFiring = false;
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }).start();
            } else if (keyCode == KeyEvent.VK_S && !isShieldActive && !shieldUsed) {
                isShieldActive = true;
                shieldUsed = true;
                startShieldTimer();
            }
        }

        // Restart game if game over and enter is pressed
        if (keyCode == KeyEvent.VK_ENTER && isGameOver) {
            restartGame();
            levelSelected = false;
            stopGameTimers();
            gamePanel.repaint();
        }
    }

    /**
     * Unused method required by the KeyListener interface.
     *
     * @param e The KeyEvent representing the key typed event.
     */
    @Override
    public void keyTyped(KeyEvent e) {}

    /**
     * Unused method required by the KeyListener interface.
     *
     * @param e The KeyEvent representing the key released event.
     */
    @Override
    public void keyReleased(KeyEvent e) {}

    /**
     * The entry point for the SpaceGame application.
     *
     * @param args The command-line arguments passed to the program (not used).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SpaceGame().setVisible(true);
            }
        });
    }
}