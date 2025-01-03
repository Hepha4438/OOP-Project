package main;

import filemanager.FileManager;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import static filemanager.FileManager.moveFileToFolder;

public class MainMenu extends Application {
    private main.Setting setting;

    @Override
    public void start(Stage primaryStage) {
        setting = new Setting();

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Thu thập và phân tích dữ liệu từ X");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #2C3E50;");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-font-size: 16px;");

        Button btnStart = new Button("Tìm kiếm từ đầu");
        btnStart.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10px;");
        btnStart.setOnAction(e -> {
            moveFileToFolder("rank.json", "lastrun");
            Main.storeFile();
            setting.clearProgress();
            WindowUser windowUser = new WindowUser();
            windowUser.getSetup();
            windowUser.start(primaryStage);
        });

        Button btnContinue = new Button("Tiếp tục tìm kiếm");
        btnContinue.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10px;");
        btnContinue.setOnAction(e -> {
            if (!setting.hasData()) {
                messageLabel.setText("( Chưa có dữ liệu, vui lòng chọn Bắt đầu tìm kiếm. )");
                return;
            }
            WindowUser windowUser = new WindowUser();
            windowUser.getSetup();
            windowUser.start(primaryStage);
        });

        if (!setting.hasData()) {
            messageLabel.setText("Chưa có dữ liệu, vui lòng chọn Bắt đầu tìm kiếm.");
        } else {
            messageLabel.setText("Dữ liệu sẵn sàng. Bạn có thể tiếp tục tìm kiếm.");
        }

        root.getChildren().addAll(titleLabel, messageLabel, btnStart, btnContinue);

        Scene scene = new Scene(root, 500, 350);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Main Menu");
        primaryStage.show();
    }
}
