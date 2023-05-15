package de.bremen.jTimetable.fxmlController;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

public class LecturerBlocksController {
    ResourceBundle resources;

    @FXML
    private GridPane grdpn_LecturerBlock;

    public void initialize(URL location, ResourceBundle resources) {
        int[][] gridPaneNodes = new int[10][8] ;
        for (Node child : grdpn_LecturerBlock.getChildren()) {
            Integer column = GridPane.getColumnIndex(child);
            Integer row = GridPane.getRowIndex(child);
            if (column != null && column > 0 && row != null && row >= 0) {
                // gridPaneNodes[column][row] = child ;
                child = new Text("U+2713");
            }
        }
        return;
    }
}
