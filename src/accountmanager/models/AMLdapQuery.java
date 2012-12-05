/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package accountmanager.models;

import com.novell.ldap.*;
import com.novell.ldap.controls.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;
import java.util.Enumeration;
/**
 *
 * @author georg
 */

public class AMLdapQuery {
    private AMLdapConnection connection;
    private String searchBase;
    private String filter;
    private String[] attributesList;
    private String recordName;
    private String lastError;
    protected ArrayList<AMLdapRecord> data = new ArrayList<AMLdapRecord>();
    
    public AMLdapQuery(){}
    public AMLdapQuery(AMLdapConnection ld){
        connection = ld;
    } 
    public AMLdapQuery(AMLdapConnection ld,String sb,String f,String[] list,String rn){
        connection = ld;
        searchBase = sb;
        filter = f;
        attributesList = list;
        recordName = rn;
    }   
    public void setSearchBase(String sb){
        searchBase = sb;
    }
    public String getSearchBase(){
        return searchBase;
    }   
    public void setFilter(String f){
        filter = f;
    }    
    public String getFilter(){
        return filter;
    }  
    public boolean search(){
        if(connection==null)return false;
        LDAPSearchResults result = null;  
        LDAPEntry entry = null;
        LDAPAttributeSet attributes;
        LDAPAttribute attribute;
        data.clear();
        try{
            result = connection.search(searchBase, LDAPConnection.SCOPE_ONE, filter, attributesList, false);
        }
        catch(LDAPException e)
        {
            System.out.print("Ldap serch error:"+e.toString()+"\n");
            return false;
        }
        int i=0;     
        while(result.hasMore())
        {
            try
            {
                entry = result.next();
            }
            catch(LDAPException e)
            {
                System.out.print("ldap get entry error\n");
                return false;
            }
            String dn = entry.getDN().toString();
            AMLdapRecord r = new AMLdapRecord();
            r.addValue("dn", dn,0);
            try
            {
                    r.setName(entry.getAttribute(recordName).getStringValue());
                    LDAPAttributeSet set = entry.getAttributeSet();
                    Iterator it = set.iterator();
                    while(it.hasNext())
                    {
                        LDAPAttribute a = (LDAPAttribute)it.next();
                        Enumeration lv = a.getStringValues();
                        while(lv.hasMoreElements())
                        {
                            r.addValue(a.getName(), (String)lv.nextElement(),0);
                        }  
                    }
            }
            catch(java.lang.NullPointerException e)
            {
                r.setName("");
                continue;
            }  
            data.add(r);
        }
        return true;
    }    
    public void setRecordName(String name){
        recordName = name;
    }  
    public void dataSort(int low,int high){
        if(low>=high)return;
        int i = low;
        int j = high;
        AMLdapRecord x = data.get((low+high)/2);
        do {
            while(data.get(i).getName().compareTo(x.getName())<0) ++i;
            while(data.get(j).getName().compareTo(x.getName())>0) --j;
            if(i <= j){
                AMLdapRecord  temp = data.get(i);
                data.set(i, data.get(j));
                data.set(j, temp);
                i++; j--;
            }
        } while(i <= j);

        if(low < j) dataSort(low, j);
        if(i < high) dataSort(i, high);
    }
    protected boolean add(AMLdapRecord r){
        System.out.println(r.toSString());
        if(connection==null)return false;
        LDAPAttributeSet as = new LDAPAttributeSet();
        String[] ocList = r.getValue("objectClass").split("[;]");
        if(ocList.length>1){
            as.add(new LDAPAttribute("objectClass",ocList));
        }
        else{
            as.add(new LDAPAttribute("objectClass",r.getValue("objectClass")));
        }
        int rdnindex=-1;
        for(int i=0;i<r.size();i++){
            if(r.getHeaderAt(i).equals("objectClass")) continue;
            if(r.getNatAt(i)==4)rdnindex=i;
             as.add(new LDAPAttribute(r.getHeaderAt(i),
                    r.getValueAt(i)
                    ));
        }
        StringBuffer dn = new StringBuffer(r.getHeaderAt(rdnindex));
        dn.append("="+r.getValueAt(rdnindex)+","+searchBase);
        System.out.println(dn.toString());
        LDAPEntry newEntry = new LDAPEntry(dn.toString(),as);
        try{
           connection.add(newEntry);
           return true;
        }
        catch(LDAPException e){
            lastError = e.toString();
            return false;
        }
        
    }
    
    protected boolean delete(String dn){
        if(connection==null)return false;
        if(dn==null)return false;
        try{
            connection.delete(dn);
            return true;
        }
        catch(LDAPException e){
            lastError = e.toString();
            return false;
        }
    }
    
    public boolean modify(AMLdapRecord r){
        int rdnindex = -1;
        System.out.println(r.toSString());
        ArrayList modList = new ArrayList();
        for(int i=0;i<r.size();i++){
            switch(r.getNatAt(i)){
                case 1:{
                    modList.add(new LDAPModification(LDAPModification.REPLACE,
                            new LDAPAttribute(r.getHeaderAt(i),r.getValueAt(i))));
                    break;
                }
                case 3:{
                    rdnindex = i;
                    break;
                }
                case 2:{
                    try{
                        connection.modify(r.getValue("dn"), new LDAPModification(LDAPModification.DELETE,
                            new LDAPAttribute(r.getHeaderAt(i),r.getValueAt(i))));
                        break;
                    }
                    catch(LDAPException e){
                        if(e.getResultCode()!=LDAPException.NO_SUCH_ATTRIBUTE){
                            lastError = e.toString();
                            return false;
                        }
                    }
                    break;
                }
                case 4:{
                    modList.add(new LDAPModification(LDAPModification.ADD,
                            new LDAPAttribute(r.getHeaderAt(i),r.getValueAt(i))));
                    break;
                }
                default: break;
            }
        }
        try{
            LDAPModification[] m = new LDAPModification[modList.size()];
            if(modList.size()>0){
                m = (LDAPModification[])modList.toArray(m);
                connection.modify(r.getValue("dn"), m);
            }
        }
        catch(LDAPException e){
            System.out.println("modify error "+ r.getValue("dn"));
            lastError = e.toString();
            return false; 
        }
        if(rdnindex>=0){
            try{
                connection.rename(r.getValue("dn"), r.getHeaderAt(rdnindex)+
                        "="+r.getValueAt(rdnindex), true);
            }
            catch(LDAPException e){
                lastError = e.toString();
                return false; 
            }
        }
        return true;
    }
    
    public String getLastError(){
        return lastError;
    }
}
