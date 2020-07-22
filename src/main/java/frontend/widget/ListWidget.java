package frontend.widget;

import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListWidget extends Widget {
	public ListWidget() {
	}

	public ListWidget(MetaData n) {
		super(n);
	}

	public ListWidget(ArrayList<MetaData> n) {
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

		List<DataPoint> dataPoints = this.dataList.get(0).getData().entrySet().stream().map(e -> new DataPoint(e.getKey(), e.getValue())).collect(Collectors.toList());

		for(DataPoint n : dataPoints) {
			table.getItems().add(n);
		}
		return table;
	}
}
