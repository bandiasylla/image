package com.example.image;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.awt.geom.Point2D;
import java.io.File;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;

import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;


public class image extends Application {

    private Stage primaryStage;
    private Scene primaryScene;

    private final ImageView vueCarte = new ImageView();
    //private AnimationTimer timer;

    Group figurines = new Group();
    private GridPane vueRoot;

    private Region mapArea = new Region() {
        {
            getChildren().add(vueCarte);
            getChildren().add(figurines);
        }
    };

    private long t0 = -1;
    private double dir=60;

    @Override
    public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;

        for (int fig = 0; fig < 3; fig++) {
            File file = new File("Images/Test/Fig" + fig + ".png");
            Image imgFig = new Image(file.toURI().toString(), false);
            ImageView vueFig = new ImageView();
            vueFig.setImage(imgFig);
            vueFig.getTransforms().add(scale);
            vueFig.relocate(100*(fig+1),200);
            vueFig.setRotate(dir);
            vueFig.visibleProperty().set(true);
            vueFig.setUserData(new Point2D.Double((fig+1) * 100, 200));
            figurines.getChildren().add(vueFig);
        }

        //
        File file = new File("Images/Test/Fond.png");
        Image image = new Image(file.toURI().toString(), false);
        vueCarte.setImage(image);
        final Rectangle2D viewport = new Rectangle2D(0, 0, 500, 500);
        vueCarte.setViewport(viewport);
        vueCarte.setOnMouseDragged(this::handleMouseDragged);
        vueCarte.setOnMouseMoved(this::handleMouseMoved);
        vueCarte.setOnScroll(this::handleZoom);
        vueCarte.getTransforms().add(scale);
        Platform.runLater(() ->

        {
            mapArea.widthProperty().addListener(this::mapAreaSizeChanged);
            mapArea.heightProperty().addListener(this::mapAreaSizeChanged);
        });
        vueRoot = new GridPane();


        vueRoot.add(mapArea, 1, 1);
        vueRoot.setConstraints(mapArea, 1, 1, 1, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.ALWAYS);

        primaryScene = new Scene(vueRoot, 500, 500);
        primaryStage.setTitle("Test position");
        primaryStage.setScene(primaryScene);
        primaryStage.show();
        corrigeLocation(0, 0);
    }
    private double startX;
    private double startY;

    private double lastX;
    private double lastY;

    private void handleMouseMoved(MouseEvent e) {
        double XX = e.getX() + vueCarte.getViewport().getMinX();
        double YY = e.getY() + vueCarte.getViewport().getMinY();
        System.out.println(e.getX()+","+e.getY());
    }



    private void handleMouseDragged(MouseEvent e) {
        if (e.isPrimaryButtonDown()) {

            double draggedDistanceX = startX - e.getX();
            double draggedDistanceY = startY - e.getY();

            startX = e.getX();
            startY = e.getY();


            double viewWidth = mapArea.getWidth() / zoom;
            double viewHeight = mapArea.getHeight() / zoom;

            final Rectangle2D viewport = vueCarte.getViewport();
            double curMinX = viewport.getMinX();
            double curMinY = viewport.getMinY();

            double newMinX = curMinX + draggedDistanceX;
            double newMinY = curMinY + draggedDistanceY;
            newMinX = clamp(newMinX, 0, Math.max(0, vueCarte.getImage().getWidth() - viewWidth));
            newMinY = clamp(newMinY, 0, Math.max(0, vueCarte.getImage().getHeight() - viewHeight));
            vueCarte.setViewport(new Rectangle2D(newMinX, newMinY, viewWidth, viewHeight));
            corrigeLocation(newMinX, newMinY);
        }
    }

    double clamp(double min, double value, double max) {
        double result = Math.max(min, value);
        result = Math.min(result, max);
        return result;
    }

    private void mapAreaSizeChanged(Observable o) {
        double viewWidth = mapArea.getWidth() / zoom;
        double viewHeight = mapArea.getHeight() / zoom;
        final Rectangle2D viewport = vueCarte.getViewport();
        if (viewport.getWidth() != viewWidth || viewport.getHeight() != viewHeight) {
            double newMinX = viewport.getMinX();
            double newMinY = viewport.getMinY();
            newMinX = clamp(newMinX, 0, Math.max(0, vueCarte.getImage().getWidth() - viewWidth));
            newMinY = clamp(newMinY, 0, Math.max(0, vueCarte.getImage().getHeight() - viewHeight));
            vueCarte.setViewport(new Rectangle2D(newMinX, newMinY, viewWidth, viewHeight));
        }
    }

    private double zoom = 1.0;
    private Scale scale = new Scale(zoom, zoom);
    private static final double MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 2.0;

    private void handleZoom(ScrollEvent event) {
        double delta = event.getDeltaY();
        double newZoom = zoom;
        if (delta < 0) {
            newZoom *= 2;
        }
        if (delta > 0) {
            newZoom /= 2;
        }
        newZoom = clamp(MIN_ZOOM, newZoom, MAX_ZOOM);
        if (newZoom == zoom) {
            return;
        }

        zoom = newZoom;

        scale.setX(zoom);
        scale.setY(zoom);
        double viewWidth = mapArea.getWidth() / zoom;
        double viewHeight = mapArea.getHeight() / zoom;
        final Rectangle2D viewport = vueCarte.getViewport();

        if (viewport.getWidth() != viewWidth || viewport.getHeight() != viewHeight) {
            double newMinX = viewport.getMinX();
            double newMinY = viewport.getMinY();
            newMinX = clamp(newMinX, 0, Math.max(0, vueCarte.getImage().getWidth() - viewWidth));
            newMinY = clamp(newMinY, 0, Math.max(0, vueCarte.getImage().getHeight() - viewHeight));
            vueCarte.setViewport(new Rectangle2D(newMinX, newMinY, viewWidth, viewHeight));
            corrigeLocation(newMinX, newMinY);
        }
    }

    private void corrigeLocation(double orgx, double orgy) {

        for (Node vueFig : figurines.getChildren()) {
            Point2D unite = (Point2D) vueFig.getUserData();
            double nx = (unite.getX() - orgx) * zoom;
            double ny = (unite.getY() - orgy) * zoom;
            int m=0;
            if (nx >= 0 && ny >= 0) {
                Bounds bnd=vueFig.getLayoutBounds();
                double xc=(bnd.getMinX()+bnd.getMaxX())/2;
                double yc=(bnd.getMinY()+bnd.getMaxY())/2;
                vueFig.setLayoutX(nx-xc*zoom);
                vueFig.setLayoutY(ny-yc*zoom);
                vueFig.visibleProperty().set(true);
            } else
                vueFig.visibleProperty().set(false);
        }
    }


    public static void main(String[] args) {
        launch(args);
    }


}