/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package accountmanager.controllers;
import java.util.HashMap;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import accountmanager.models.*;
/**
 *
 * @author georg
 */
public class AMUserEventStrategy implements EventStrategy {
    private AMController c;
    private boolean editing = true;
    AMLdapRecord record;
    private int selectedIndex; 

    public AMUserEventStrategy(AMController ctr) {
        this.c = ctr;
    }
    
    public void setController(AMController ctr)
    {
        c = ctr;
    }
    
    public void objectListDoubleClicked(int selected)
    {
        //int selected = c.view.objectBrowser
        if(selected<0||selected>=c.objectListModel.getSize()){
            JOptionPane.showMessageDialog(c.view, "Для того чтобы редактировать\nэлемент, выберите его в списке",
                   "Предупреждение", JOptionPane.WARNING_MESSAGE);
            return;
        }
        record = c.objectListModel.getRecordAt(selected);
        selectedIndex = selected;
        c.userEditDialog.snEdit.setText(record.getValue("sn"));
        c.userEditDialog.givenNameEdit.setText(record.getValue("givenName"));
        c.userEditDialog.titleEdit.setText(record.getValue("title"));
        c.userEditDialog.roomNumberEdit.setText(record.getValue("roomNumber"));
        c.userEditDialog.telephoneNumberEdit.setText(record.getValue("telephoneNumber"));
        c.userEditDialog.mailEdit.setText(record.getValue("mail"));
        c.userGroupModel.setSearchBase(c.settings.getStringProperty("server", "group_base"));
        c.userGroupModel.setFilter("member="+record.getValue("dn"));
        System.out.println(c.userGroupModel.getFilter());
        c.userGroupModel.setRecordName("description");
        c.userGroupModel.select();
        c.groupListModel.select();
        c.groupListModel.sort();
        for(int i=0;i<c.userGroupModel.getSize();i++){
            c.userGroupModel.getRecordAt(i).addValue("member", record.getValue("dn"), 2);
        }
        c.ouListModel.select();
        c.ouListModel.sort();
        c.ouListModel.setSelectedItem(record.getValue("ou"));
        c.userEditDialog.ouEdit.setModel(c.ouListModel);
        editing = true;
        c.userEditDialog.setVisible(true);
    }
    public void showNewDialog(){
        c.userEditDialog.snEdit.setText("");
        c.userEditDialog.givenNameEdit.setText("");
        c.userEditDialog.titleEdit.setText("");
        c.userEditDialog.telephoneNumberEdit.setText("");
        c.userEditDialog.roomNumberEdit.setText("");
        c.userEditDialog.mailEdit.setText("");
        c.userGroupModel.clear();
        c.groupListModel.select();
        c.groupListModel.sort();
        c.ouListModel.select();
        c.ouListModel.sort();
        c.userEditDialog.ouEdit.setModel(c.ouListModel);
        editing = false;
        c.userEditDialog.setVisible(true);
    }
    public void saveDn(){
        AMLdapRecord r = new AMLdapRecord();
        AMLdapQuery q = new AMLdapQuery(c.connection);
        HashMap<String,String> map = new HashMap<String,String>();
        map.put("gname",c.userEditDialog.givenNameEdit.getText());
        map.put("name",c.userEditDialog.givenNameEdit.getText().replaceAll("[ ][A-Za-zА-Яа-я]+", ""));
        map.put("surname",c.userEditDialog.snEdit.getText());
        map.put("mail",c.userEditDialog.mailEdit.getText());
        map.put("roomNumber", c.userEditDialog.roomNumberEdit.getText());
        map.put("title", c.userEditDialog.titleEdit.getText());
        map.put("telephoneNumber",c.userEditDialog.telephoneNumberEdit.getText());
        map.put("ou", c.ouListModel.getSelectedItem().toString());
        if(!editing){
            r.addValue("objectClass", c.settings.getStringProperty("user", "objectClass"),1);
            for(int i=0;i<c.userSettingsModel.getRowCount()-1;i++){
                int natv = 1;
                if(c.userSettingsModel.getValueAt(i, 0).toString().equalsIgnoreCase("uid")) natv=4;
                r.addValue(c.userSettingsModel.getValueAt(i, 0).toString(), 
                        accountmanager.controllers.AMExpresionParser.executeExpresion(
                            c.userSettingsModel.getValueAt(i, 1).toString(), map),natv);
            }
             if(c.objectListModel.insertRecord(r)){
                 c.objectListModel.select();
                 c.objectListModel.sort();
             }   
             else{
                 JOptionPane.showMessageDialog(c.view,
                         "Ошибка добавления.\r\n"+c.objectListModel.getLastError(), 
                         "Ошибка", JOptionPane.ERROR_MESSAGE);
             }
        }
        else{
            r.addValue("dn", record.getValue("dn"), 0);
            for(int i=0;i<c.userSettingsModel.getRowCount()-1;i++){
                if(!c.userSettingsModel.getValueAt(i, 0).toString().equalsIgnoreCase("uid")){
                    r.addValue(c.userSettingsModel.getValueAt(i, 0).toString(), 
                            accountmanager.controllers.AMExpresionParser.executeExpresion(
                                c.userSettingsModel.getValueAt(i, 1).toString(), map),1);
                }
                else{
                    r.addValue(c.userSettingsModel.getValueAt(i, 0).toString(),
                            accountmanager.controllers.AMExpresionParser.executeExpresion(
                                c.userSettingsModel.getValueAt(i, 1).toString(), map),3);
                }
            }
            
            if(!q.modify(r)){
                System.out.println(q.getLastError());
                System.out.println(r.toSString());
                c.objectListModel.select();
                c.objectListModel.sort();
            }
            else{
                c.objectListModel.select();
                c.objectListModel.sort();
            }
        }
        String newDn = "uid="+r.getValue("uid")+","+c.settings.getStringProperty("server", "user_base");
            for(int i=0;i<c.userGroupModel.getSize();i++){
                r = c.userGroupModel.getRecordAt(i);
                r.addValue("member", newDn, 4);
                if(!q.modify(c.userGroupModel.getRecordAt(i))){
                    System.out.println("Group modify error:\n"+q.getLastError());
                }
            }
        for(int i=0;i<c.deletingBuffer.size();i++){
                if(!q.modify(c.deletingBuffer.get(i))){
                    System.out.println("Deleting bufer error:\n"+q.getLastError()+"on index "+i);
                }
            }
        c.userEditDialog.dispose();
    }
    public void deleteDn(){
        selectedIndex = c.view.objectsList.getSelectedIndex();
        if(selectedIndex<0||selectedIndex>=c.objectListModel.getSize()){
            JOptionPane.showMessageDialog(c.view, "Для того чтобы удалить\nэлемент выберите его в списке.",
                    "Предупреждение",JOptionPane.WARNING_MESSAGE);
            return;
        }
        if(JOptionPane.showConfirmDialog(c.view, 
                "Вы уверены, что хотите удалить информацию о \r\n"+
                c.objectListModel.getElementAt(selectedIndex), "Вопрос", 
                    JOptionPane.OK_CANCEL_OPTION)==2){
            return;
        }
        AMLdapQuery q = new AMLdapQuery(c.connection);
        String dn = c.objectListModel.getRecordAt(selectedIndex).getValue("dn");
        c.userGroupModel.setSearchBase(c.settings.getStringProperty("server","group_base"));
        c.userGroupModel.setFilter("member="+dn);
        c.userGroupModel.select();
        System.out.println(dn);
        System.out.println("group count: "+c.userGroupModel.getSize());
        for(int i=0;i<c.userGroupModel.getSize();i++){
            AMLdapRecord r = new AMLdapRecord();
            r.addValue("dn", c.userGroupModel.getRecordAt(i).getValue("dn"), 0);
            r.addValue("member", dn, 2);
            q.modify(r);   
        }
        
        if(c.objectListModel.removeRow(selectedIndex)){
            c.objectListModel.select();
            c.objectListModel.sort();
            c.userEditDialog.dispose();
        }
        else{
            JOptionPane.showMessageDialog(c.view,
                    "Ошибка удаления.\r\n"+c.objectListModel.getLastError(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
    public int getSelectedIndex(){
        return selectedIndex;
    }
}
