package de.leon.bstcgf;

import de.leon.bstcgf.data.TableGameData;
import de.leon.bstcgf.data.steam.SteamGame;
import de.leon.bstcgf.data.steam.SteamJsonData;
import de.leon.bstcgf.data.steamcardexchange.SteamCardExchangeGameData;
import de.leon.bstcgf.data.steamcardexchange.SteamCardExchangeJsonData;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class BstcgfController implements Initializable {

    @FXML
    VBox vBox;

    @FXML
    private TableView<TableGameData> tableGameDataTableView;

    @FXML
    private TableColumn<TableGameData, String> tableColumnName;

    @FXML
    private TableColumn<TableGameData, Integer> tableColumnId;

    @FXML
    private TableColumn<TableGameData, Integer> tableColumnCards;

    @FXML
    private TableColumn<TableGameData, String> tableColumnPrice;

    @FXML
    private TableColumn<TableGameData, Double> tableColumnRating;

    @FXML
    private Button loadDataButton;

    private final ObservableList<TableGameData> tableGameDataObservableList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        initUI();

    }

    private void initUI() {

        tableColumnName.setCellValueFactory(
            new PropertyValueFactory<>("name")
        );

        tableColumnId.setCellValueFactory(
            new PropertyValueFactory<>("id")
        );

        tableColumnCards.setCellValueFactory(
            new PropertyValueFactory<>("cardsString")
        );

        tableColumnPrice.setCellValueFactory(
            new PropertyValueFactory<>("price")
        );

        tableColumnRating.setCellValueFactory(
            new PropertyValueFactory<>("rating")
        );

        /* This should set the column width to a percentage of the table width, but because the table doesn't have a fixed size, widthProperty returns 0.0
        tableColumnName.prefWidthProperty().bind(tableGameDataTableView.widthProperty().multiply(2/6));
        tableColumnId.prefWidthProperty().bind(tableGameDataTableView.widthProperty().multiply(1/6));
        tableColumnCards.prefWidthProperty().bind(tableGameDataTableView.widthProperty().multiply(1/6));
        tableColumnPrice.prefWidthProperty().bind(tableGameDataTableView.widthProperty().multiply(1/6));
        tableColumnRating.prefWidthProperty().bind(tableGameDataTableView.widthProperty().multiply(1/6));
         */

        tableGameDataTableView.setItems(tableGameDataObservableList);
    }

    public void clickAction(ActionEvent actionEvent) {
        loadData();
    }

    private void loadData() {

        tableGameDataObservableList.clear();
        List<TableGameData> tableGameDataList = new LinkedList<>();
        List<SteamCardExchangeGameData> steamCardExchangeGameDataList;
        List<SteamGame> steamGameList = new LinkedList<>();

        try {
            SteamCardExchangeJsonData steamCardExchangeJsonData = Request.getSteamCardExchangeData();
            steamCardExchangeGameDataList = new LinkedList<>(
                steamCardExchangeJsonData.getSteamCardExchangeGameData());
            steamCardExchangeJsonData.getInPackages(100).forEach(packages -> {
                try {
                    SteamJsonData steamJsonData = Request.getGameDataFromSteamIds(
                        packages.getOnlyIds());
                    steamGameList.addAll(steamJsonData.getSteamGames());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        steamCardExchangeGameDataList.forEach(scegd -> {

            SteamGame steamGame = steamGameList.stream().filter(sg -> sg.getId() == scegd.getId())
                .findFirst().orElseThrow();

            // skip if a game is free2play because you can only obtain 1 cord for ~10$ spend;
            // using initial price should still add free2keep games in the list;
            if (steamGame.getData().getSteamPriceOverview().getInitialPrice() > 0) {
                TableGameData tableGameData = new TableGameData(steamGame, scegd);
                tableGameDataList.add(tableGameData);
            }
        });

        tableGameDataObservableList.addAll(tableGameDataList
            .stream()
            .sorted(Comparator.comparing(TableGameData::getName))
            .sorted(Comparator.comparing(TableGameData::getRating))
            .collect(Collectors.toList())
        );
    }
}