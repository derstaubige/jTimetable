package de.bremen.jTimetable.fxmlController;

import java.net.URL;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import de.bremen.jTimetable.Classes.Lecturer;
import de.bremen.jTimetable.Classes.LecturerBlock;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class LecturerBlocksController implements Initializable{
    ResourceBundle resources;
    Lecturer lecturer;

    @FXML
    private GridPane grdpn_LecturerBlock;
    @FXML
    private Button btnSave;

    /**
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnSave.setOnAction(event -> {
            lecturer.setLecturerBlocks(new ArrayList<LecturerBlock>());
            List<CheckBox> listCheckBoxs = getNodesOfType(grdpn_LecturerBlock, CheckBox.class);
            for (CheckBox checkBox : listCheckBoxs) {
                if (checkBox.isSelected() == false) {                    
                    Integer timeslot = GridPane.getColumnIndex(checkBox) - 1; 
                    DayOfWeek dow = DayOfWeek.of(GridPane.getRowIndex(checkBox));
                    lecturer.addLecturerBlocks(dow, timeslot);
                }
            }
            try {
                lecturer.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private <T> List<T> getNodesOfType(Pane parent, Class<T> type) {
        List<T> elements = new ArrayList<>();
        for (Node node : parent.getChildren()) {
            if (node instanceof Pane) {
                elements.addAll(getNodesOfType((Pane) node, type));
            } else if (type.isAssignableFrom(node.getClass())) {
                //noinspection unchecked
                elements.add((T) node);
            }
        }
        return Collections.unmodifiableList(elements);
    }

    /**
     * Render the Blocked Matrix
     */
    public void populateBlocked(){
        List<CheckBox> listCheckBoxs = getNodesOfType(grdpn_LecturerBlock, CheckBox.class);
        for (CheckBox checkBox : listCheckBoxs) {
            Integer column = GridPane.getColumnIndex(checkBox);
            Integer row = GridPane.getRowIndex(checkBox);
            if (lecturer.checkifLecturerisBlocked(row, column - 1)){
                checkBox.setSelected(false);
            }else{
                checkBox.setSelected(true);
            }
        }
    }

    public void setLecturer(Lecturer lecturer){
        this.lecturer = lecturer;
    }

}
