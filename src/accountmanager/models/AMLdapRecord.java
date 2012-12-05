/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package accountmanager.models;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 *
 * @author georg
 */
public class AMLdapRecord {
    private String name = null; 
    protected List<String> header = new ArrayList<String>();
    protected List<String> value = new ArrayList<String>();
    protected List<Integer> nat = new ArrayList<Integer>();
    public AMLdapRecord()
    {
        ;
    }
    
    public String toString()
    {
        return name+"\n";
    }
    
    public void addValue(String key,String val,int natv)
    {
        header.add(key);
        value.add(val);
        nat.add(natv);
    }
    
    public int size()
    {
        if(header.size()!=value.size())return -1;
        return header.size();
    }
    
    public void setName(String n)
    {
        name = n;
    }
    
    public String getName()
    {
        return name;
    }
    
    public String getHeaderAt(int index)
    {
        return header.get(index);
    }
    
    public String getValueAt(int index)
    {
        return value.get(index);
    }
    
    public String getValue(String key){
        int ind = header.indexOf(key);
        if(ind>=0){
            return value.get(ind);
        }
        return null;
    }
    public int getNatAt(int index){
        return nat.get(index);
    }
    public String toSString(){
        StringBuffer buf = new StringBuffer();
        buf.append("Name=>"+name+"\r\n");
        for(int i=0;i<this.size();i++)
            buf.append(header.get(i)+"=>"+value.get(i)+" "+nat.get(i) +"\r\n");
        return buf.toString();
    }
}
