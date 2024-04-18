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

class AudioPlayer {
    // Method to play audio from a file
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

class Star {
    int x, y;
    Color color;
    int lifetime;

    Star(int x, int y, Color color, int lifetime) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.lifetime = lifetime;
    }
}

class Obstacle {
    Point position;
    int spriteIndex;

    public Obstacle(int x, int y, int spriteIndex) {
        this.position = new Point(x, y);
        this.spriteIndex = spriteIndex;
    }
}

public class SpaceGame extends JFrame implements KeyListener {
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
    private int selectedLevel = 1; // Default to level 1

    private JPanel gamePanel;
    private JLabel scoreLabel;
    private JLabel healthLabel;
    private JLabel timeLabel;
    private Timer timer;
    private Timer endGameTimer;
    private Timer shieldTimer;
    private boolean isGameOver;
    private int playerX, playerY;
    private int projectileX, projectileY;
    private boolean isProjectileVisible;
    private boolean isFiring;
    private boolean isShieldActive = false;
    private List<Obstacle> obstacles = new ArrayList<>();
    private Image playerImage = new ImageIcon(getClass().getResource("rsH6n.png")).getImage();
    private Image[] obstacleImages = new Image[4];

    public SpaceGame() {
        setTitle("Space Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        loadObstacleSprites();

        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
            }
        };

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setBounds(10, 10, 100, 20);
        scoreLabel.setForeground(Color.BLUE);
        gamePanel.add(scoreLabel);

        healthLabel = new JLabel("Health: 5");
        healthLabel.setBounds(10, 0, 100, 20);
        healthLabel.setForeground(Color.ORANGE);
        gamePanel.add(healthLabel);

        timeLabel = new JLabel("Time: " + remainingTime + "s");
        timeLabel.setBounds(10, 50, 100, 20);
        timeLabel.setForeground(Color.WHITE);
        gamePanel.add(timeLabel);

        add(gamePanel);
        gamePanel.setFocusable(true);
        gamePanel.addKeyListener(this);

        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = HEIGHT - PLAYER_HEIGHT - 20;
        projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
        projectileY = playerY;
        isProjectileVisible = false;
        isGameOver = false;
        isFiring = false;
        obstacles = new java.util.ArrayList<>();

    }

    private void startGameTimers() {
        timer = new Timer(20, e -> {
            if (!isGameOver) {
                update();
                gamePanel.repaint();
            }
        });
        timer.start();

        endGameTimer = new Timer(1000, e -> {
            remainingTime--;
            timeLabel.setText("Time: " + remainingTime + "s");
            if (remainingTime <= 0) {
                endGame();
            }
        });
        endGameTimer.start();
    }

    private void stopGameTimers() {
        if (timer != null) {
            timer.stop();
        }
        if (endGameTimer != null) {
            endGameTimer.stop();
        }
    }

    private void startShieldTimer() {
        shieldTimer = new Timer(5000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isShieldActive = false; // Deactivate the shield after 5 seconds
                shieldTimer.stop(); // Stop the timer
            }
        });
        shieldTimer.setRepeats(false); // Set to run once
        shieldTimer.start(); // Start the timer
    }

    private void loadObstacleSprites() {
        int spriteWidth = 250;
        int spriteHeight = 250;
        Image spritesheet = new ImageIcon(getClass().getResource("spritesheet.png")).getImage();

        for (int i = 0; i < 4; i++) {
            int xOffset = i * (spriteWidth + 30);
            Image sprite = Toolkit.getDefaultToolkit().createImage(
                new FilteredImageSource(spritesheet.getSource(),
                new CropImageFilter(xOffset, 0, spriteWidth - 20, spriteHeight)));

            obstacleImages[i] = sprite.getScaledInstance(OBSTACLE_WIDTH, OBSTACLE_HEIGHT, Image.SCALE_SMOOTH);
        }
    }

    private void draw(Graphics g) {
        if (!levelSelected) {
            // Set the background to black
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, WIDTH, HEIGHT);

            // Draw level selection options
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Select Level:", WIDTH / 2 - 100, HEIGHT / 2 - 30);
            g.drawString("1 - Easy", WIDTH / 2 - 80, HEIGHT / 2);
            g.drawString("2 - Hard", WIDTH / 2 - 80, HEIGHT / 2 + 30);
        } else {
            if (isGameOver) {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, WIDTH, HEIGHT); // Ensure background is cleared
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 24));
                g.drawString("Game Over!", WIDTH / 2 - 80, HEIGHT / 2);
                g.drawString("Press Enter to Play Again.", WIDTH / 2 - 150, HEIGHT / 2 + 20);
            }
            /// Set the background to black
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, WIDTH, HEIGHT);

            // Draw the player using the player image
            g.drawImage(playerImage, playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, this);

            // Draw the projectile if it is visible, using a simple rectangle
            if (isProjectileVisible) {
                g.setColor(Color.GREEN);
                g.fillRect(projectileX, projectileY, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
            }

            // Draw each obstacle using the obstacle image
            for (Obstacle obstacle : obstacles) {
                g.drawImage(obstacleImages[obstacle.spriteIndex], obstacle.position.x, obstacle.position.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT, this);
            }

            // Draw the shield if it is active
            if (isShieldActive) {
                g.setColor(Color.GREEN);
                g.drawOval(playerX - 5, playerY - 5, PLAYER_WIDTH + 10, PLAYER_HEIGHT + 10);
            }

            // Draw stars
            Iterator<Star> it = stars.iterator();
            while (it.hasNext()) {
                Star star = it.next();
                g.setColor(star.color);
                g.fillOval(star.x, star.y, 4, 4);
                if (--star.lifetime <= 0) {
                    it.remove();
                }
            }
        }
    }

    private void update() {
        if (levelSelected && !isGameOver) {
            // Star Updater
            if (random.nextInt(10) < 1) {
                int x = random.nextInt(WIDTH);
                int y = random.nextInt(HEIGHT);
                Color color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
                stars.add(new Star(x, y, color, 100));
            }

            // Move obstacles
            Iterator<Obstacle> iterator = obstacles.iterator();
            while (iterator.hasNext()) {
                Obstacle obstacle = iterator.next();
                obstacle.position.y += OBSTACLE_SPEED;
                if (obstacle.position.y > HEIGHT) {
                    iterator.remove();
                }
            }

            // Generate new obstacles
            if (Math.random() < 0.02) {
                createObstacle();
            }

            // Move projectile
            if (isProjectileVisible) {
                projectileY -= PROJECTILE_SPEED;
                if (projectileY < 0) {
                    isProjectileVisible = false;
                }
            }

            // Check collision with player
            Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
            iterator = obstacles.iterator();
            while (iterator.hasNext()) {
                Obstacle obstacle = iterator.next();
                Rectangle obstacleRect = new Rectangle(obstacle.position.x, obstacle.position.y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
                if (playerRect.intersects(obstacleRect) && !isShieldActive) {
                    health -= 1;
                    AudioPlayer.play("dead.wav");
                    iterator.remove(); // Remove the obstacle that collided with the player
                    if (health <= 0) {
                        isGameOver = true;
                        stopGameTimers(); // Stop the game and end game timers
                        gamePanel.repaint(); // Refresh to show the game over screen
                        break; // Exit loop to avoid concurrent modification exception
                    }
                }
            }

            // Check collision with obstacle
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

    private void createObstacle() {
        int obstacleX = random.nextInt(WIDTH - OBSTACLE_WIDTH);
        int spriteIndex = random.nextInt(obstacleImages.length);
        obstacles.add(new Obstacle(obstacleX, -OBSTACLE_HEIGHT, spriteIndex));
    }

    private void endGame() {
        isGameOver = true;
        endGameTimer.stop(); // Stop the endGameTimer
        gamePanel.repaint();
    }

    private void restartGame() {
        // Reset all game state variables
        score = 0;
        health = 5;
        remainingTime = 30;
        endGameTimer.start();
        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = HEIGHT - PLAYER_HEIGHT - 20;
        projectileX = playerX + PLAYER_WIDTH / 2 - PROJECTILE_WIDTH / 2;
        projectileY = playerY;
        isProjectileVisible = false;
        isGameOver = false;
        obstacles.clear(); // Clear all obstacles
        gamePanel.repaint(); // Redraw the panel to update the UI
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        // Handle level selection if no level has been selected
        if (!levelSelected) {
            if (keyCode == KeyEvent.VK_1) {
                selectedLevel = 1;  // Select Easy level
                levelSelected = true;
                OBSTACLE_SPEED = 3;  // Example speed setting for Easy level
                startGameTimers();
            } else if (keyCode == KeyEvent.VK_2) {
                selectedLevel = 2;  // Select Hard level
                levelSelected = true;
                OBSTACLE_SPEED = 6;  // Example speed setting for Hard level
                startGameTimers();
            }
            gamePanel.repaint(); // Redraw the panel to exit level selection screen
            return; // Exit the method to prevent further processing until a level is selected
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
                            Thread.sleep(500); // Limit firing rate
                            isFiring = false;
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }).start();
            } else if (keyCode == KeyEvent.VK_S && !isShieldActive) {
                isShieldActive = true;
                startShieldTimer(); // Start the shield timer
            }
        }

        // Restart game if game over and enter is pressed
        if (keyCode == KeyEvent.VK_ENTER && isGameOver) {
            restartGame();
            levelSelected = false;  // Ensure level selection screen shows up again
            stopGameTimers();  // Stop the game timers
            gamePanel.repaint();  // Redraw the level selection screen
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SpaceGame().setVisible(true);
            }
        });
    }
}