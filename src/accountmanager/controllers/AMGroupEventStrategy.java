/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package accountmanager.controllers;
import accountmanager.models.*;
import java.util.HashMap;
import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
/**
 *
 * @author georg
 */
public class AMGroupEventStrategy implements EventStrategy {
    private AMController c;
    private boolean editing = true;

    public AMGroupEventStrategy(AMController ctr) {
        this.c = ctr;
    }
    
    public void setController(AMController ctr)
    {
        c = ctr;
    }
    
    public void objectListDoubleClicked(int selected)
    {
        if(selected<0||selected>=c.objectListModel.getSize()){
            JOptionPane.showMessageDialog(c.view, "Для того чтобы редактировать\nэлемент, выберите его в списке",
                   "Предупреждение", JOptionPane.WARNING_MESSAGE);
            return;
        }
        AMLdapRecord r = new AMLdapRecord();
        r = c.objectListModel.getRecordAt(selected);
        c.groupEditDialog.groupNameEdit.setText(r.getValue("cn"));
        c.groupEditDialog.groupDescriptionMemo.setText(r.getValue("description"));
        c.groupEditDialog.setVisible(true);
    }
    public void showNewDialog(){
        c.groupEditDialog.groupNameEdit.setText("");
        c.groupEditDialog.groupDescriptionMemo.setText("");
        editing = false;
        c.groupEditDialog.setVisible(true);
    }
    public void saveDn(){
        HashMap<String,String> map = new HashMap<String,String>();
        map.put("groupName", c.groupEditDialog.groupNameEdit.getText());
        map.put("groupDescription", c.groupEditDialog.groupDescriptionMemo.getText());
        AMLdapRecord r = new AMLdapRecord();
        AMLdapQuery q = new AMLdapQuery(c.connection);
        if(editing){
            r.addValue("dn",c.objectListModel.getRecordAt(
                    c.view.objectsList.getSelectedIndex()).getValue("dn"),0);
            r.addValue("objectClass",c.settings.getStringProperty("group", "objectClass"),1);
            for(int i=0;i<c.groupSettingsModel.getRowCount()-1;i++){
                int natv = 3;
                if(!c.groupSettingsModel.getValueAt(i, 0).toString().equalsIgnoreCase("cn")) natv=1;
                r.addValue(c.groupSettingsModel.getValueAt(i, 0).toString(),
                        accountmanager.controllers.AMExpresionParser
                        .executeExpresion(c.groupSettingsModel.getValueAt(i, 1).toString(), map),natv);
            }
            q.modify(r);
            c.objectListModel.select();
            c.objectListModel.sort();
        }
        else{
            r.addValue("objectClass",c.settings.getStringProperty("group", "objectClass"),1);
            for(int i=0;i<c.groupSettingsModel.getRowCount()-1;i++){
                int natv = 4;
                if(!c.groupSettingsModel.getValueAt(i, 0).toString().equalsIgnoreCase("cn")) natv=1;
                r.addValue(c.groupSettingsModel.getValueAt(i, 0).toString(),
                        accountmanager.controllers.AMExpresionParser
                        .executeExpresion(c.groupSettingsModel.getValueAt(i, 1).toString(), map),natv);
            }
            if(!c.objectListModel.insertRecord(r)){
                JOptionPane.showMessageDialog(c.view, "Ошивка добавления записи.\n"+
                        c.objectListModel.getLastError(), "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
            c.objectListModel.select();
            c.objectListModel.sort();
            
        }
        editing = true;
        c.groupEditDialog.dispose();
    }
    public void deleteDn(){
        int selectedIndex = c.view.objectsList.getSelectedIndex();
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
        c.objectListModel.removeRow(selectedIndex);
        c.objectListModel.select();
        c.objectListModel.sort();
        c.groupEditDialog.dispose();
    }
}
