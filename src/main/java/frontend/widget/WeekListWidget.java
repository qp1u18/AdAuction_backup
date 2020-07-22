/*package frontend.widget;

import frontend.DataPoint;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;

public class WeekListWidget extends Widget {

    public WeekListWidget() {
    }

    public WeekListWidget(MetaData n) {
        super(n);
    }

    public WeekListWidget(ArrayList<MetaData> n) {
        super(n);
    }

    @Override
    public Parent create() {
        TableColumn<String, DataPoint> columnMetric = new TableColumn<>("Metric");
        columnMetric.setCellValueFactory(new PropertyValueFactory<>("metric"));

        TableColumn<String, DataPoint> columnValue = new TableColumn<>("Value");
        columnValue.setCellValueFactory(new PropertyValueFactory<>("value"));

        TableView table = new TableView();

        table.getColumns().add(columnMetric);
        table.getColumns().add(columnValue);

        for(DataPoint n : ((ListData) this.dataList.get(0)).getListData()) {
            table.getItems().add(n);
        }
        return table;
    }
}
*/