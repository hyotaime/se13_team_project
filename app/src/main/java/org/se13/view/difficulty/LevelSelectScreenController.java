package org.se13.view.difficulty;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import org.se13.SE13Application;
import org.se13.game.rule.GameLevel;
import org.se13.game.rule.GameMode;
import org.se13.online.IsFirst;
import org.se13.online.OnlineBattleTetrisServer;
import org.se13.server.LocalBattleTetrisServer;
import org.se13.server.LocalTetrisServer;
import org.se13.sqlite.config.ConfigRepositoryImpl;
import org.se13.sqlite.config.PlayerKeycode;
import org.se13.view.base.BaseController;
import org.se13.view.nav.AppScreen;
import org.se13.view.tetris.*;

import java.io.*;

public class LevelSelectScreenController extends BaseController {

    @FXML
    public void initialize() {
        modeChoiceBox.setItems(FXCollections.observableArrayList("default", "item", "timeLimit"));
        modeChoiceBox.setValue("default");
        typeChoiceBox.setItems(FXCollections.observableArrayList("single", "battle", "online"));
        typeChoiceBox.setValue("single");
    }

    @FXML
    private void handleEasyButtonAction() throws IOException, ClassNotFoundException {
        startTetrisGame(GameLevel.EASY, setGameMode(modeChoiceBox.getValue()), typeChoiceBox.getValue());
    }

    @FXML
    private void handleNormalButtonAction() throws IOException, ClassNotFoundException {
        startTetrisGame(GameLevel.NORMAL, setGameMode(modeChoiceBox.getValue()), typeChoiceBox.getValue());
    }

    @FXML
    private void handleHardButtonAction() throws IOException, ClassNotFoundException {
        startTetrisGame(GameLevel.HARD, setGameMode(modeChoiceBox.getValue()), typeChoiceBox.getValue());
    }

    private void startTetrisGame(GameLevel level, GameMode gameMode, String type) throws IOException, ClassNotFoundException {
        switch (type) {
            case "single" -> startLocalTetrisGame(level, gameMode);
            case "battle" -> startLocalBattleTetrisGame(level, gameMode);
            case "online" -> startOnlineTetrisGame(level, gameMode);
        }
    }

    private void startLocalTetrisGame(GameLevel level, GameMode mode) {
        Player player = new Player(-1, new ConfigRepositoryImpl(0).getPlayerKeyCode(), new TetrisEventRepositoryImpl());
        LocalTetrisServer server = new LocalTetrisServer(level, mode);
        player.connectToServer(server);
        SE13Application.navController.navigate(AppScreen.TETRIS, (controller) -> {
            ((TetrisScreenController) controller).setArguments(player);
        });
    }

    private void startLocalBattleTetrisGame(GameLevel level, GameMode mode) {
        LocalBattleTetrisServer server = new LocalBattleTetrisServer(level, mode);
        Player player1 = new Player(1, new ConfigRepositoryImpl(0).getPlayerKeyCode(), new TetrisEventRepositoryImpl());
        Player player2 = new Player(2, new ConfigRepositoryImpl(1).getPlayerKeyCode(), new TetrisEventRepositoryImpl());
        player1.connectToServer(server);
        player2.connectToServer(server);
        SE13Application.navController.navigate(AppScreen.BATTLE, (controller) -> {
            ((BattleScreenController) controller).setArguments(player1, player2);
        });
    }

    private void startOnlineTetrisGame(GameLevel level, GameMode mode) throws IOException, ClassNotFoundException {
        LocalBattleTetrisServer server = new LocalBattleTetrisServer(level, mode);
        OnlineBattleTetrisServer onlineServer = new OnlineBattleTetrisServer("localhost", 5555);
        // 상대방의 정보를 수신
        IsFirst isFirst = (IsFirst) onlineServer.getIn().readObject();
        int myPlayerId = 1;
        int opponentPlayerId = 2;
        if (!isFirst.isFirst()) {
            myPlayerId = 2;
            opponentPlayerId = 1;
        }
        // 수신한 정보로 플레이어 초기화
        Player myPlayer = new Player(myPlayerId, new ConfigRepositoryImpl(0).getPlayerKeyCode(), new TetrisEventRepositoryImpl());
        myPlayer.connectToServer(server);
        Player opponentPlayer = new Player(opponentPlayerId);
//        PlayerKeycode emptyKeycode = new PlayerKeycode("", "", "", "", "", "", "");
//        Player opponentPlayer = new Player(opponentPlayerId, emptyKeycode, new TetrisEventRepositoryImpl());
        opponentPlayer.connectToServer(server);

        SE13Application.navController.navigate(AppScreen.BATTLE, (controller) -> {
            ((BattleScreenController) controller).setArguments(myPlayer, opponentPlayer, server);
        });
    }

    private GameMode setGameMode(String gameMode) {
        return switch (gameMode) {
            case "default" -> GameMode.DEFAULT;
            case "item" -> GameMode.ITEM;
            case "timeLimit" -> GameMode.TIME_LIMIT;
            default -> {
                assert (false);
                yield null;
            }
        };
    }

    public static GameMode gameMode;
    public static GameLevel gameLevel;

    @FXML
    Button easyButton;

    @FXML
    Button normalButton;

    @FXML
    Button hardButton;

    @FXML
    ChoiceBox<String> modeChoiceBox;

    @FXML
    ChoiceBox<String> typeChoiceBox;
}
