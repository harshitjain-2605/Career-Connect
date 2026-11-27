package com.campus.placement.controller;

import com.campus.placement.config.AppConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Programmatically constructed JavaFX screen (without FXML) demonstrating
 * Scene Graph composition with {@link BorderPane}, {@link StackPane}, {@link GridPane},
 * {@link VBox}, and {@link HBox}.
 */
// [Unit 5: Programmatic Scene Graph & layouts (BorderPane, StackPane, GridPane, VBox, HBox)]
public final class AboutPolicyView {

    private AboutPolicyView() {
    }

    /**
     * Builds the Policy & Architecture information screen programmatically.
     *
     * @param onBack callback invoked when the Back button is clicked
     * @return root {@link Parent} node
     */
    public static Parent build(Runnable onBack) {
        AppConfig config = AppConfig.getInstance();

        BorderPane root = new BorderPane();

        // Top Header (HBox)
        HBox header = new HBox(16);
        header.getStyleClass().add("app-header");
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Placement Policy & System Architecture");
        title.getStyleClass().add("app-title");
        header.getChildren().add(title);
        root.setTop(header);

        // Center Content (StackPane -> VBox card + GridPane)
        StackPane centerStack = new StackPane();
        centerStack.setPadding(new Insets(28));

        VBox card = new VBox(18);
        card.getStyleClass().add("card");
        card.setMaxWidth(720);

        Label heading = new Label("Active Configuration & Business Rules");
        heading.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);

        addRow(grid, 0, "Database Dialect:", config.getDbDialect().toUpperCase());
        addRow(grid, 1, "JDBC URL:", config.getDbUrl());
        addRow(grid, 2, "Placement Policy:", config.getPlacementPolicy()
                + " (SINGLE_OFFER blocks further apps; UPGRADE_1_5X allows >= 1.5x LPA)");
        addRow(grid, 3, "Eligibility Formula:",
                "CGPA >= min AND backlogs <= max AND branch in allowed set AND batch matches AND OPEN AND today <= lastDate");
        addRow(grid, 4, "Pipeline Stages:",
                "APPLIED -> SHORTLISTED -> TEST -> INTERVIEW -> SELECTED / REJECTED");
        addRow(grid, 5, "Security:", "Salted PBKDF2WithHmacSHA256 (65,536 iterations) + PreparedStatement everywhere");
        addRow(grid, 6, "Architecture:", "JavaFX + FXML -> Service Layer (Transactions) -> Generic DAO -> JDBC");

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("btn-primary");
        backBtn.setOnAction(e -> onBack.run());
        actions.getChildren().add(backBtn);

        card.getChildren().addAll(heading, grid, actions);
        centerStack.getChildren().add(card);
        root.setCenter(centerStack);

        return root;
    }

    private static void addRow(GridPane grid, int row, String label, String value) {
        Label k = new Label(label);
        k.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        Label v = new Label(value);
        v.setWrapText(true);
        v.setMaxWidth(480);
        grid.add(k, 0, row);
        grid.add(v, 1, row);
    }
}
