package backend;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Client {

	private final int id;
	private String name;

	public Client(int id) {
		this.id = id;
		PreparedStatement statement = null;
		try {
			statement = Database.getConnection().prepareStatement("SELECT name from clients WHERE id = ?");
			statement.setInt(1, this.id);
			ResultSet rs = Database.query(statement);
			rs.next();
			this.name = rs.getString(1);
		} catch (SQLException e) {
			e.printStackTrace();
			this.name = null;
		}

	}

	public static List<Client> getClients() throws SQLException {
		List<Client> out = new ArrayList<>();
		PreparedStatement statement;
		statement = Database.getConnection().prepareStatement("SELECT id from clients");
		ResultSet resultSet = Database.query(statement);
		while (resultSet.next()) {
			out.add(new Client(resultSet.getInt(1)));
		}
		return out;
	}

	public static Client getFromName(String name) throws SQLException {
		PreparedStatement statement = Database.getConnection().prepareStatement("SELECT id from clients WHERE name = ?");
		statement.setString(1, name);
		ResultSet resultSet = Database.query(statement);
		resultSet.next();
		return new Client(resultSet.getInt(1));
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	public static ObservableList<String> clientNameList() {
		try {
			List<Client> clients = Client.getClients();
			List<String> clientNames = clients.stream().map(Client::getName).collect(Collectors.toList());
			return FXCollections.observableArrayList(clientNames);
		} catch (SQLException e) {
			e.printStackTrace();
			return FXCollections.observableArrayList();
		}
	}

	public void delete() throws SQLException {
		PreparedStatement statement = Database.getConnection().prepareStatement("DELETE FROM clients WHERE id = ?");
		statement.setInt(1, this.id);
		Database.execute(statement);
		Database.commitData();
	}
}
