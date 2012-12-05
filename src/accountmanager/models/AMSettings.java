/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package accountmanager.models;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import org.jconfig.handler.*;
import org.jconfig.*;
import java.io.File;
import java.util.Set;
import java.util.HashSet;

/**
 *
 * @author georg
 */
public class AMSettings {
    private ConfigurationManager cm = ConfigurationManager.getInstance();
    private File configFile;
    private String choosedCategory = null;
    private XMLFileHandler handler = new XMLFileHandler();   
    public AMSettings()
    {
    }
    
    public AMSettings(String fileName)
    {
        setConfigFile(fileName);   
    }

    int setConfigFile(String fileName)
    {
        configFile = new File(fileName);
        if(!configFile.exists()){
            return 1;
        }
        if(!configFile.canRead()){
            return 2;
        }
        handler.setFile(configFile);
        try{
            cm.load(handler, "settings");
        }
        catch(Exception e){
            return 3;
        }
        
        return 0;
    }
    
    public String getChoosedCategory()
    {
        return choosedCategory;
    }
    
    public void chooseCategory(String c)
    {
        choosedCategory = c;
    }   
    public String getStringProperty(String category,String property)
    {
        return cm.getConfiguration("settings").getProperty(property,null,category);    
    }
    public int getIntProperty(String category,String property)
    {
       return cm.getConfiguration("settings").getIntProperty(property, 0, category);
    } 
    public String getStringProperty(String property)
    {
        if(choosedCategory==null) return null;
        return getStringProperty(choosedCategory,property);
    }  
    public int getIntProperty(String property)
    {
        if(choosedCategory==null) return 0;
        return getIntProperty(choosedCategory,property);
    }   
    public void setStringProperty(String category,String key,String value){
        cm.getConfiguration("settings").setProperty(key, value, category);
    }
    public void setStringProperty(String key,String value){
        setStringProperty(choosedCategory, key, value);
    }
    public boolean save(){
        try{
            cm.save(handler, cm.getConfiguration("settings"));
        }
        catch(Exception e){
            return false;
        }
        return true;
    }
    public boolean reload(){
        try{
            cm.reload("settings");
        }
        catch(Exception e){
            return false;
        }
        return true;       
    }
    public Set<String> getKeys(){
        if(choosedCategory==null) return new HashSet<String>();
        return cm.getConfiguration("settings").getCategory(choosedCategory).getProperties().stringPropertyNames();
    }
    public void removeProperty(String category,String name){
        cm.getConfiguration("settings").removeProperty(name, category);
    }
            
}
