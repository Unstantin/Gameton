package org.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        // Создание экземпляра класса Controller и запуск его в отдельном потоке
        Thread controllerThread = new Thread(new Controller());
        controllerThread.start(); // Запуск потока

        // Создание экземпляра класса Registration и запуск его в отдельном потоке
        Thread registrationThread = new Thread(new Registration());
        registrationThread.start(); // Запуск потока

        // Ожидание завершения обоих потоков (необязательно, если вы хотите, чтобы основной поток завершился сразу после запуска)
        try {
            controllerThread.join();
            registrationThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}