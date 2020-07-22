package frontend.widget;

import javafx.scene.Parent;

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;

public abstract class Widget implements Serializable {

    public ArrayList<MetaData> dataList;

    public Widget() {
        dataList = new ArrayList<>();
    }

    public Widget(MetaData n) {
        dataList = new ArrayList<>();
        dataList.add(n);
    }

    public Widget(ArrayList<MetaData> n) {
        dataList = n;
    }

    public void widgetAddData(MetaData n) {
        dataList.add(n);
    }

	public void widgetSetData(int index, MetaData n) {
		dataList.set(index, n);
	}

	public void widgetDeleteData(int index) {
    	dataList.remove(index);
	}



    public ArrayList<MetaData> getWidgetData() {
        return dataList;
    }

    public static Widget deSerialize(String s ) throws IOException, ClassNotFoundException {
        byte [] data = Base64.getDecoder().decode( s );
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data) );
        Object o = ois.readObject();
        ois.close();
        return (Widget) o;
    }

    public String serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(this);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public abstract Parent create();

    public void refresh() {
    	dataList.forEach(MetaData::refresh);
	}

}
