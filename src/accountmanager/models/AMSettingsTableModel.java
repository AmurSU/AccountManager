/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package accountmanager.models;
import javax.swing.table.TableModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import accountmanager.models.AMSettings;

/**
 *
 * @author georg
 */
public class AMSettingsTableModel implements TableModel{
    ArrayList<TableModelListener> listeners = new ArrayList<TableModelListener>();
    private ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
    AMSettings settings;
    String category;
    
    public AMSettingsTableModel(String c, AMSettings s){
        category = c;
        settings = s;
    } 
    
    public ArrayList<ArrayList<String>> select(){
        settings.chooseCategory(category);
        data.clear();
        Object[] keys = settings.getKeys().toArray();
        for(int i=0;i<keys.length;i++){
            if(keys[i].toString().equals("objectClass")) continue;
            ArrayList<String> l = new ArrayList<String>();
            l.add(keys[i].toString());
            l.add(settings.getStringProperty(keys[i].toString()));
            data.add(l);
        }
         ArrayList<String> ls = new ArrayList<String>();
         ls.add("");
         ls.add("");
         data.add(ls);
         updateListeners();
         return data;
    }
    
    public void addTableModelListener(TableModelListener l){
        listeners.add(l);
    }
    public void removeTableModelListener(TableModelListener l){
        listeners.remove(l);
    }
    public Class getColumnClass(int columnIndex){
        return String.class;
    }
    public int getColumnCount(){
        return 2;
    }
    public int getRowCount(){
        return data.size();
    }
    public String getColumnName(int columnIndex){
        switch(columnIndex){
            case 0: return "Параметр";
            case 1: return "Значение";
            default: return null;
        }
    }
    public Object getValueAt(int rowIndex, int columnIndex){
        return data.get(rowIndex).get(columnIndex);
    }
    public boolean isCellEditable(int rowIndex, int columnIndex){
        return true;
    }
    public void setValueAt(Object aValue, int rowIndex, int columnIndex){
        data.get(rowIndex).set(columnIndex, aValue.toString());
        sync();
        if(rowIndex==data.size()-1){
            ArrayList<String> ls = new ArrayList<String>();
            ls.add("");
            ls.add("");
            data.add(ls);
        }
        updateListeners();
    }
    
    public boolean removeRow(int row){
        if(data.size()<=row||row<0) return false;
        data.remove(row);
        sync();
        updateListeners();
        return true;
    }
    
    private void updateListeners(){
        for(int i=0;i<listeners.size();i++){
            listeners.get(i).tableChanged(new TableModelEvent(this));
        }
    }
    
    private void sync(){
        Object[] key = settings.getKeys().toArray();
        for(int i=0; i<key.length; i++){
            settings.removeProperty(category, key[i].toString());
        }
        
        for(int i=0;i<data.size();i++){
            if(!data.get(i).get(0).equals("")){
                settings.setStringProperty(category,data.get(i).get(0),data.get(i).get(1));
            }
        }
    }
    
}
