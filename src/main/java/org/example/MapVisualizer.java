package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MapVisualizer extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Map Visualizer");
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawMap(gc);

        StackPane root = new StackPane();
        root.getChildren().add(canvas);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void drawMap(GraphicsContext gc) {
        // Очистка экрана
        gc.clearRect(0, 0, WIDTH, HEIGHT);

        // Пример рисования элементов
        drawBase(gc, 100, 100, 50, 50);
        drawCenter(gc, 200, 200, 20);
        drawZombie(gc, 300, 300, 10);
        drawEnemy(gc, 400, 400, 20);
        drawSpawn(gc, 500, 500, 15);
    }

    private void drawBase(GraphicsContext gc, int x, int y, int width, int height) {
        gc.setFill(Color.BLUE);
        gc.fillRect(x, y, width, height);
    }

    private void drawCenter(GraphicsContext gc, int x, int y, int radius) {
        gc.setFill(Color.RED);
        gc.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
    }

    private void drawZombie(GraphicsContext gc, int x, int y, int radius) {
        gc.setFill(Color.GREEN);
        gc.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
    }

    private void drawEnemy(GraphicsContext gc, int x, int y, int radius) {
        gc.setFill(Color.ORANGE);
        gc.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
    }

    private void drawSpawn(GraphicsContext gc, int x, int y, int radius) {
        gc.setFill(Color.PURPLE);
        gc.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
    }
}
