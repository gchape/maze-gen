package com.gen.maze.app;

import javafx.scene.layout.Region;

public class Controller {
    private final Model model;
    private final View view;
    private Thread aThread;

    public Controller() {
        model = new Model();
        view = new View(model::depthFirstSearch, model.rankProperty(), model.cellDimensionProperty());

        view.algorithmInProcessProperty().bindBidirectional(model.algorithmInProcessProperty());
        view.algorithmCancelProperty().addListener((o, v1, v2) -> aThread.interrupt());
        view.clickedButtonIdProperty().addListener((o, v1, v2) -> aThread = Thread.ofPlatform().start(() ->
                model.runAlgorithm(v2, view::animateCell, view::animateWall, view::eraseWall)));
    }

    public Region getView() {
        return view.getRoot();
    }
}
