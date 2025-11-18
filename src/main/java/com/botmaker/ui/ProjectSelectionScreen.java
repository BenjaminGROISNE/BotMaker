package com.botmaker.ui;

import com.botmaker.project.ProjectInfo;
import com.botmaker.project.ProjectManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

/**
 * Project selection screen shown on startup
 */
public class ProjectSelectionScreen {

    private final ProjectManager projectManager;
    private final Consumer<String> onProjectSelected;
    private final Stage stage;

    public ProjectSelectionScreen(Stage stage, Consumer<String> onProjectSelected) {
        this.stage = stage;
        this.projectManager = new ProjectManager();
        this.onProjectSelected = onProjectSelected;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // Header
        Label titleLabel = new Label("Select a Project");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        VBox header = new VBox(10, titleLabel);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 20, 0));

        // Project list
        ListView<ProjectInfo> projectListView = new ListView<>();
        projectListView.setPrefHeight(400);

        // Custom cell factory to show project info
        projectListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ProjectInfo project, boolean empty) {
                super.updateItem(project, empty);
                if (empty || project == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox box = new VBox(5);
                    Label nameLabel = new Label(project.getName());
                    nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                    Label pathLabel = new Label(project.getProjectPath().toString());
                    pathLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");

                    Label dateLabel = new Label("Last modified: " +
                            project.getLastModified().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
                    dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");

                    box.getChildren().addAll(nameLabel, pathLabel, dateLabel);
                    setGraphic(box);
                }
            }
        });

        // Load projects
        List<ProjectInfo> projects = projectManager.listProjects();
        projectListView.getItems().addAll(projects);

        // Select first project by default
        if (!projects.isEmpty()) {
            projectListView.getSelectionModel().select(0);
        }

        // Buttons
        Button openButton = new Button("Open Project");
        openButton.setPrefWidth(150);
        openButton.setDefaultButton(true);
        openButton.setOnAction(e -> {
            ProjectInfo selected = projectListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                onProjectSelected.accept(selected.getName());
            }
        });

        // Handle double-click
        projectListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                ProjectInfo selected = projectListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    onProjectSelected.accept(selected.getName());
                }
            }
        });

        VBox buttonBox = new VBox(10, openButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        // Layout
        VBox center = new VBox(10, projectListView, buttonBox);
        root.setTop(header);
        root.setCenter(center);

        Scene scene = new Scene(root, 600, 500);
        scene.getStylesheets().add(getClass().getResource("/com/botmaker/styles.css").toExternalForm());

        return scene;
    }
}