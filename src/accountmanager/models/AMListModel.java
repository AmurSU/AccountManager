/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package accountmanager.models;
import com.novell.ldap.LDAPConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;
import javax.swing.ListModel;
/**
 *
 * @author georg
 */
public class AMListModel extends AMLdapQuery implements ListModel{
    private List<ListDataListener> listeners = new ArrayList<ListDataListener>();
    
    public AMListModel(){super();}
    public AMListModel(AMLdapConnection ld){
        super(ld);
    }
    
    public AMListModel(AMLdapConnection ld,String sb,String f,String[] list,String rn){
        super(ld,sb,f,list,rn);
    }
    
    public void addListDataListener(ListDataListener l)
    {
        listeners.add(l);
    }
    
    public void removeListDataListener(ListDataListener l)
    {
        listeners.remove(l);
    }
    
    public int getSize()        
    {
        return data.toArray().length;
    }
    
    public Object getElementAt(int index) 
    {
        return data.get(index).getName();
    }
    
    public boolean select()
    {
        boolean s = this.search();
        updateListeners();
        return s;
    }
    
    public void sort()
    {
        this.dataSort(0, data.toArray().length-1);
        Iterator it = listeners.iterator();
        updateListeners();
    }
    
    public AMLdapRecord getRecordAt(int index)
    {
        return data.get(index);
    }
    public boolean insertRecord(AMLdapRecord r){
        return this.add(r);
    }
    public boolean setRecord(AMLdapRecord r, int index){
        boolean rez = false;
        if(index<0){
            rez = data.add(r);
        }
        else{
            if(index<data.size()-1){
                data.set(index, r);
                rez = true;
            }
            else{
                rez = data.add(r);
            }
        }
        updateListeners();
        return rez;
    }
    public boolean removeRecord(int index){
        if(index>=0&&index<data.size()){
            data.remove(index);
            updateListeners();
            return true;
        }
        else{
            updateListeners();
            return false;
        }
    }
    protected void updateListeners(){
        Iterator it = listeners.iterator();
        while(it.hasNext())
        {
            ListDataListener l = (ListDataListener)it.next();
            l.intervalAdded(new ListDataEvent(this,ListDataEvent.INTERVAL_ADDED,0,this.getSize()));
        }
    }
    public boolean removeRow(int index){
        return this.delete(data.get(index).getValue("dn"));
    }
    public void clear(){
        data.clear();
    }
    
    public ListDataListener[] getListDataListeners(){
        return (ListDataListener[])listeners.toArray();
    } 
}
