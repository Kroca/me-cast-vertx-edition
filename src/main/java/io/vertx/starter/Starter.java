package io.vertx.starter;

import io.vertx.core.Launcher;

public class Starter {
    public static void main(String[] args) {
        Launcher.executeCommand("run", MainVerticle.class.getName());
    }
}
