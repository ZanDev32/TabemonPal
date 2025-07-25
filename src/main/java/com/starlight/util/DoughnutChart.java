package com.starlight.util;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class DoughnutChart extends PieChart {
    private final Circle innerCircle;

    // Default constructor for FXML
    public DoughnutChart() {
        super();
        
        innerCircle = new Circle();

        // just styled in code for demo purposes,
        // use a style class instead to style via css.
        innerCircle.setFill(Color.WHITESMOKE);
        innerCircle.setStroke(Color.WHITE);
        innerCircle.setStrokeWidth(3);
    }

    public DoughnutChart(ObservableList<Data> pieData) {
        super(pieData);

        innerCircle = new Circle();

        // just styled in code for demo purposes,
        // use a style class instead to style via css.
        innerCircle.setFill(Color.WHITESMOKE);
        innerCircle.setStroke(Color.WHITE);
        innerCircle.setStrokeWidth(3);
    }

    /**
     * Forces a refresh of the doughnut chart to ensure the inner circle is properly displayed.
     * Call this method after data changes to maintain the doughnut appearance.
     */
    public void refreshDoughnut() {
        // Immediate refresh
        Platform.runLater(() -> {
            addInnerCircleIfNotPresent();
            updateInnerCircleLayout();
            requestLayout();
            
            // Secondary refresh with delay to handle timing issues
            Platform.runLater(() -> {
                addInnerCircleIfNotPresent();
                updateInnerCircleLayout();
                requestLayout();
                
                // Final refresh to ensure everything is properly positioned
                Platform.runLater(() -> {
                    addInnerCircleIfNotPresent();
                    updateInnerCircleLayout();
                });
            });
        });
    }

    @Override
    protected void layoutChartChildren(double top, double left, double contentWidth, double contentHeight) {
        super.layoutChartChildren(top, left, contentWidth, contentHeight);

        addInnerCircleIfNotPresent();
        updateInnerCircleLayout();
    }

    private void addInnerCircleIfNotPresent() {
        if (getData().size() > 0) {
            Node pie = getData().get(0).getNode();
            if (pie != null && pie.getParent() instanceof Pane) {
                Pane parent = (Pane) pie.getParent();

                // Remove any existing inner circle first to avoid duplicates
                parent.getChildren().remove(innerCircle);
                
                // Ensure inner circle styling is correct
                innerCircle.setFill(Color.WHITESMOKE);
                innerCircle.setStroke(Color.WHITE);
                innerCircle.setStrokeWidth(3);
                
                // Add the inner circle and bring it to front
                parent.getChildren().add(innerCircle);
                innerCircle.toFront();
            }
        }
    }

    private void updateInnerCircleLayout() {
        if (getData().isEmpty()) {
            return;
        }
        
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
        for (PieChart.Data data: getData()) {
            Node node = data.getNode();
            if (node == null) continue;

            Bounds bounds = node.getBoundsInParent();
            if (bounds.getMinX() < minX) {
                minX = bounds.getMinX();
            }
            if (bounds.getMinY() < minY) {
                minY = bounds.getMinY();
            }
            if (bounds.getMaxX() > maxX) {
                maxX = bounds.getMaxX();
            }
            if (bounds.getMaxY() > maxY) {
                maxY = bounds.getMaxY();
            }
        }

        innerCircle.setCenterX(minX + (maxX - minX) / 2);
        innerCircle.setCenterY(minY + (maxY - minY) / 2);

        innerCircle.setRadius((maxX - minX) / 4);
    }
}