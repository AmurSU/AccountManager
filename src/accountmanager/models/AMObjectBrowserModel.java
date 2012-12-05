/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package accountmanager.models;
import javax.swing.table.TableModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.util.ArrayList;
import java.lang.Class;

/**
 *
 * @author georg
 */
public class AMObjectBrowserModel  implements TableModel{
    private AMLdapRecord record = null;
    private ArrayList<TableModelListener> listeners = new ArrayList<TableModelListener>();
    private int columnCount = 0;
    private int rowCount = 0;
    
    public AMObjectBrowserModel()
    {
       ;
    }
    public AMObjectBrowserModel(AMLdapRecord r)
    {
       setRecord(r);
    }
    
    public void setRecord(AMLdapRecord r)
    {
        record = r;
        columnCount = 2;
        rowCount = record.size();
        TableModelEvent e = new TableModelEvent(this);
        for(int i=0;i<listeners.size();i++)
        {
            listeners.get(i).tableChanged(e);
        }
    }
    
    public AMLdapRecord getRecord()
    {
        return record;
    }
    
    public void addTableModelListener(TableModelListener l)
    {
        listeners.add(l);
    }
    
    public void removeTableModelListener(TableModelListener l) 
    {
        listeners.remove(l);
    }
    
    public Class getColumnClass(int columnIndex)
    {
        String s = new String();
        return s.getClass();
    }
    
    public int 	getColumnCount() 
    {
        return columnCount;
    }
    
    public int getRowCount()
    {
        return rowCount;
    }
    
    public String getColumnName(int columnIndex)
    {
        switch(columnIndex)
        {
            case 0: return new String("Параметр");
            case 1: return new String("Значение");
        }
        return null;
    }
    
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        switch(columnIndex)
        {
            case 0: return record.header.get(rowIndex);
            case 1: return record.value.get(rowIndex);
        }
        return null;
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return false;
    }
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        ;
    }
}
