package org.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Registration registration = new Registration();
        registration.run();

        Controller controller = new Controller();

        // Запуск метода run() в отдельном потоке
        Thread controllerThread = new Thread(controller);
        controllerThread.start(); // Запуск потока
    }
}