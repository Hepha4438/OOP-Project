package main;

import fetchingtask.potentialdatafetcher.FilteringKOL;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

import filemanager.FileManager;

import static main.Main.filteringKOL;

public class WindowUser extends Application {
    private static WindowUser instance;
    private Setting setting = new Setting();


    public void getSetup() {
        setting.loadProgress();
    }

    public void start(Stage stage) {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(10));

        Button backButton = new Button("Quay lại");
        backButton.setStyle("-fx-font-size: 14px; -fx-padding: 5px;");
        backButton.setOnAction(e -> {
            MainMenu mainMenu = new MainMenu();
            mainMenu.start(stage); // Quay lại menu chính
        });

        HBox topLayout = new HBox(10);
        topLayout.setAlignment(Pos.CENTER_LEFT);
        topLayout.getChildren().add(backButton);
        layout.setTop(topLayout);
        Scene scene1 = createUnifiedScene(stage);
        layout.setCenter(scene1.getRoot());

        stage.setScene(new Scene(layout, 800, 700));
        stage.setTitle("Tìm KOL");
    }

    private Scene createUnifiedScene(Stage primaryStage) {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(20));

        Text titleText = new Text("Thu thập dữ liệu và phân tích dữ liệu KOL");
        titleText.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        layout.setTop(titleText);
        BorderPane.setAlignment(titleText, Pos.CENTER);

        VBox topSection = createTopSection();
        VBox bottomSection = createBottomSection();

        VBox mainLayout = new VBox();
        mainLayout.setSpacing(10);
        mainLayout.getChildren().addAll(topSection, bottomSection);

        layout.setCenter(mainLayout);

        return new Scene(layout, 1000, 900);
    }

    private VBox createTopSection() {
        VBox section = new VBox();
        section.setSpacing(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-border-color: black; -fx-border-width: 1px;");
        section.setAlignment(Pos.CENTER);

        Label title = new Label("Tìm kiếm KOL");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        section.getChildren().add(title);

        // Step 1 layout: Input and Results
        HBox step1Layout = new HBox(20);
        step1Layout.setPadding(new Insets(10));
        step1Layout.setAlignment(Pos.CENTER);

        VBox step1Inputs = new VBox(10);
        step1Inputs.setPadding(new Insets(10));
        step1Inputs.setPrefWidth(400);

        VBox step1Results = new VBox(10);
        step1Results.setPadding(new Insets(10));
        step1Results.setAlignment(Pos.CENTER);
        step1Results.setPrefWidth(600);

        StackPane resultsBox = new StackPane();
        resultsBox.setStyle("-fx-border-color: black; -fx-border-width: 1px;");
        resultsBox.setPrefSize(580, 150);

        TableView<String> resultsTable = new TableView<>();
        resultsTable.setPlaceholder(new Label("Kết quả tìm KOL sẽ hiển thị tại đây."));
        resultsBox.getChildren().add(resultsTable);
        step1Results.getChildren().add(resultsBox);

        TextField hashtagField = new TextField();
        hashtagField.setPromptText("Nhập hashtag");
        hashtagField.setText(setting.hashtags.isEmpty() ? "" : String.join(", ", setting.hashtags));  // Giữ nguyên nếu null
        TextField maxKOLField = new TextField();
        maxKOLField.setPromptText("Nhập số lượng KOL");
        maxKOLField.setText(setting.maxKOLSelected > 0 ? String.valueOf(setting.maxKOLSelected) : "");  // Giữ nguyên nếu null

        Button btnSaveStep1 = new Button("Lưu Setting");
        btnSaveStep1.setOnAction(e -> {
            try {
                String input = hashtagField.getText();
                if (!input.isBlank()) {
                    setting.hashtags = List.of(input.split(","));
                } else {
                    showMessage("Hãy nhập ít nhất 1 hashtag.");
                }

                setting.maxKOLSelected = Integer.parseInt(maxKOLField.getText());
                showMessage("Ghi nhận Setting này!");
                setting.saveProgress();
            } catch (NumberFormatException ex) {
                showMessage("Nhập số lượng KOL hợp lệ.");
            }
        });
        Button btnRunStep1 = new Button("Thực hiện tìm KOL");
        btnRunStep1.setOnAction(e -> {
            if (setting.hashtags.isEmpty() || setting.maxKOLSelected <= 0) {
                showMessage("Hãy lưu cài đặt trước khi thực hiện bước này.");
                return;
            }
            if(!setting.isTask1Completed) runTask1();
            displayData(filteringKOL.getOutputFilePath(), resultsBox, "Follower", Integer.class);
            showMessage("Đã hoàn thành tìm kiếm KOL");
        });
        Label parametersLabel = new Label("Nhập dữ liệu");
        parametersLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        step1Inputs.getChildren().addAll(
                parametersLabel,
                hashtagField,
                maxKOLField,
                btnSaveStep1,btnRunStep1
        );

        step1Layout.getChildren().addAll(step1Inputs, step1Results);
        section.getChildren().add(step1Layout);

        return section;
    }

    private VBox createBottomSection() {
        VBox section = new VBox();
        section.setSpacing(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-border-color: black; -fx-border-width: 1px;");
        section.setAlignment(Pos.CENTER);

        Label title = new Label("Thu thập và phân tích dữ liệu KOL");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        section.getChildren().add(title);

        HBox step2Layout = new HBox(20);
        step2Layout.setPadding(new Insets(10));
        step2Layout.setAlignment(Pos.CENTER);

        VBox step2Inputs = new VBox(10);
        step2Inputs.setPadding(new Insets(10));
        step2Inputs.setPrefWidth(400);

        VBox step2Results = new VBox(10);
        step2Results.setPadding(new Insets(10));
        step2Results.setAlignment(Pos.CENTER);
        step2Results.setPrefWidth(600);

        StackPane step2ResultsBox = new StackPane();
        step2ResultsBox.setStyle("-fx-border-color: black; -fx-border-width: 1px;");
        step2ResultsBox.setPrefSize(580, 200);

        TextField maxTweetsField = new TextField();
        maxTweetsField.setPromptText("Số lượng Tweet");
        maxTweetsField.setText(setting.maxPostRetrievedPerUser > 0 ? String.valueOf(setting.maxPostRetrievedPerUser) : "");

        TextField maxCommentsField = new TextField();
        maxCommentsField.setPromptText("Số lượng Comment của Tweet");
        maxCommentsField.setText(setting.maxComments > 0 ? String.valueOf(setting.maxComments) : "");

        TextField maxReposterField = new TextField();
        maxReposterField.setPromptText("Số người đăng lại");
        maxReposterField.setText(setting.maxReposter > 0 ? String.valueOf(setting.maxReposter) : "");

        Button btnSaveStep2 = new Button("Lưu Setting");
        btnSaveStep2.setOnAction(e -> {
            try {
                setting.maxPostRetrievedPerUser = Integer.parseInt(maxTweetsField.getText());
                setting.maxComments = Integer.parseInt(maxCommentsField.getText());
                setting.maxReposter = Integer.parseInt(maxReposterField.getText());
                setting.saveProgress();
                showMessage("Ghi nhận Setting này!");
            } catch (NumberFormatException ex) {
                showMessage("Nhập các tham số hợp lệ.");
            }
        });

        Button btnRunStep2 = new Button("Thực hiện chương trình");
        btnRunStep2.setOnAction(e -> {
            if (setting.maxReposter<=0||setting.maxComments<=0||setting.maxPostRetrievedPerUser<=0) {
                showMessage("Hãy lưu cài đặt trước khi thực hiện bước này.");
                return;
            }
            if (!setting.isTask1Completed) {
                showMessage("Hãy thực hiện bước 1 trước khi chuyển sang bước 2.");
            } else {
                if(!setting.isTask2Completed) runTask2();
                displayData("rank.json", step2ResultsBox, "PageRank" , Double.class);
                showMessage("Bước này đã hoàn thành. Kết quả được lưu tại rank.json");
            }
        });
        Label parametersLabel = new Label("Nhập dữ liệu");
        parametersLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        step2Inputs.getChildren().addAll(
                parametersLabel,  // Add the label first
                maxTweetsField,
                maxCommentsField,
                maxReposterField,
                btnSaveStep2,
                btnRunStep2
        );

        TableView<String> step2ResultsTable = new TableView<>();
        step2ResultsTable.setPlaceholder(new Label("Kết quả PageRank sẽ hiển thị tại đây."));
        step2ResultsBox.getChildren().add(step2ResultsTable);

        step2Results.getChildren().add(step2ResultsBox);
        step2Layout.getChildren().addAll(step2Inputs, step2Results);
        section.getChildren().add(step2Layout);

        return section;
    }
    
    private void runTask1() {
        try {
            Main.runTask1(setting.hashtags, setting.maxKOLSelected);
            setting.isTask1Completed = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage("Xuất hiện lỗi khi thực hiện chương trình.");
        }
    }

    private void runTask2() {
        try {
            Main.runTask2(setting.maxPostRetrievedPerUser, setting.maxComments, setting.maxReposter);
            setting.isTask2Completed = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage("Xuất hiện lỗi khi thực hiện chương trình.");
        }
    }

    private static void showMessage(String message) {
        Stage dialog = new Stage();
        dialog.setTitle("Message");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        layout.setAlignment(Pos.CENTER);  // Center the content inside VBox

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Button closeButton = new Button("Đóng");
        closeButton.setOnAction(e -> dialog.close());

        layout.getChildren().addAll(messageLabel, closeButton);
        Scene scene = new Scene(layout, 400, 150); // Adjust size if necessary
        dialog.setScene(scene);
        dialog.show();
    }


    private <T> void displayData(String fileName, StackPane resultsBox, String lastCol, Class<T> valueType) {
        // Load the data from the file
        Map<String, T> dataMap = FileManager.loadFile(fileName, new TypeToken<Map<String, T>>() {}.getType());

        if (dataMap == null || dataMap.isEmpty()) {
            showMessage("Không có dữ liệu để hiển thị!");
            return;
        }

        TableView<Map.Entry<String, T>> tableView = new TableView<>();

        // Rank column
        TableColumn<Map.Entry<String, T>, String> rankColumn = new TableColumn<>("Rank");
        rankColumn.setCellValueFactory(cellData -> {
            int rowIndex = tableView.getItems().indexOf(cellData.getValue());
            return new javafx.beans.property.SimpleStringProperty(String.valueOf(rowIndex + 1));
        });

        // KOL Link column
        TableColumn<Map.Entry<String, T>, String> kolColumn = new TableColumn<>("KOL Link");
        kolColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getKey()));

        // Value column (PageRank or any numeric value)
        TableColumn<Map.Entry<String, T>, String> valueColumn = new TableColumn<>(lastCol);
        valueColumn.setCellValueFactory(cellData -> {
            T value = cellData.getValue().getValue();
            String valueString = "";

            if (value != null) {
                // Handle Double and show only the decimal part if it's non-zero
                if (value instanceof Double) {
                    double doubleValue = (Double) value;
                    // Check if the decimal part is non-zero
                    if (doubleValue % 1 != 0) {
                        valueString = String.format("%.8f", doubleValue); // Keep decimal part, remove leading "0."
                    } else {
                        valueString = String.format("%.0f", doubleValue); // Just show the integer part
                    }
                }
            }

            return new javafx.beans.property.SimpleStringProperty(valueString);
        });

        // Set preferred width for the columns
        rankColumn.setPrefWidth(50);
        kolColumn.setPrefWidth(220);
        valueColumn.setPrefWidth(150);

        // Add the columns to the TableView
        tableView.getColumns().add(rankColumn);
        tableView.getColumns().add(kolColumn);
        tableView.getColumns().add(valueColumn);

        // Add the data (from dataMap) to the TableView
        tableView.getItems().addAll(dataMap.entrySet());

        // Bind the width and height to the resultsBox dimensions for dynamic resizing
        tableView.prefWidthProperty().bind(resultsBox.widthProperty());
        tableView.prefHeightProperty().bind(resultsBox.heightProperty());

        // Clear the results box and add the TableView
        resultsBox.getChildren().clear();
        resultsBox.getChildren().add(tableView);

        // Center the table inside the StackPane
        StackPane.setAlignment(tableView, Pos.CENTER);
    }



    public static void main(String[] args) {
        launch(args);
    }
}
