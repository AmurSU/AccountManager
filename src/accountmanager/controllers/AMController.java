/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package accountmanager.controllers;
import accountmanager.AMMainWindow;
import accountmanager.AMSettingsDialog;
import accountmanager.AMAuthDialog;
import accountmanager.AMUserEditDialog;
import accountmanager.AMObjectBrowser;
import accountmanager.AMGroupEditDialog;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import accountmanager.models.*;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Dimension;
import javax.swing.JOptionPane;
import org.jconfig.handler.*;
import org.jconfig.*;
import java.io.File;
import java.awt.Dialog;
import javax.swing.ComboBoxModel;
/**
 *
 * @author georg
 */
public class AMController extends Object {
    AMMainWindow view;
    AMSettingsDialog settingsDialog;
    AMObjectBrowser browser;
    AMLdapConnection connection;
    AMListModel objectListModel;
    AMObjectBrowserModel objectBrowserModel;
    AMListModel groupListModel;
    AMListModel userGroupModel;
    AMAuthDialog authDialog;
    AMSettingsTableModel userSettingsModel;
    AMSettingsTableModel groupSettingsModel;
    AMUserEditDialog userEditDialog;
    AMSettings settings;
    ArrayList<AMLdapRecord> deletingBuffer;
    AMGroupEditDialog groupEditDialog;
    MyCombo ouListModel;
    private StringBuffer autoComplete;
    private EventStrategy eStrategy;
    
    public AMController()
    {    
        view = new AMMainWindow();
        settingsDialog = new AMSettingsDialog(view,true);
        authDialog = new AMAuthDialog(view,true);
        userEditDialog = new AMUserEditDialog(view,true);
        groupEditDialog = new AMGroupEditDialog(view,true);
        settings = new AMSettings("accountmanager.xml");
        deletingBuffer = new ArrayList<AMLdapRecord>();
        connection = new AMLdapConnection();
        eStrategy  = new AMUserEventStrategy(this);
        userSettingsModel = new AMSettingsTableModel("user",settings);
        userSettingsModel.select();
        groupSettingsModel = new AMSettingsTableModel("group",settings);
        groupSettingsModel.select();
        view.setVisible(true);
        establishConnection();
        groupListModel = new AMListModel(connection,settings.getStringProperty("group_base"),
                                            null,null,new String("description"));
        groupListModel.select();
        groupListModel.sort();
        userEditDialog.groupList.setModel(groupListModel);
        userGroupModel = new AMListModel(connection);
        userGroupModel.clear();
        userEditDialog.memberList.setModel(userGroupModel);
        objectListModel = new AMListModel(connection,settings.getStringProperty("user_base"),null,null,new String("displayName"));
        ouListModel = new MyCombo(connection,"cn=Departments,dc=amursu,dc=ru",
                                            null,null,new String("description"));
        if(objectListModel.select()){
            objectListModel.sort();
            view.objectsList.setModel(objectListModel);
        }
        objectBrowserModel = new AMObjectBrowserModel(objectListModel.getRecordAt(0));
        view.objectBrowser.setModel(objectBrowserModel);
        //view.objectBrowser.setSelectionMode(JTable.);
        view.objectsList.addComponentListener(new ComponentAdapter(){
            public void componentResized(ComponentEvent e)
            {
                view.objectsList.setPreferredSize(new Dimension(272,
                view.objectsList.getPreferredSize().height));    
            }
        });
        view.objectsList.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e)
            {
                switch(e.getClickCount())
                {
                    case 1: {
                                objectBrowserModel.setRecord(
                                    objectListModel.getRecordAt(
                                        view.objectsList.getSelectedIndex()));
                                break;
                            }
                    case 2: {
                                eStrategy.objectListDoubleClicked(view.objectsList.getSelectedIndex()); 
                                break;
                            }
                    default: break;
                }
            }
        });
        view.objectFilterField.addKeyListener(new KeyAdapter(){
            public void keyReleased(KeyEvent e) {
                objectListFilterChanged();
            } 
        });
        view.usersToogledButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                changeEventStrategy(0);
            }
        });
        view.groupToogledButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                changeEventStrategy(1);
            }
        });
        settingsDialog.userAttrTable.addKeyListener(new KeyAdapter(){
            public void keyReleased(KeyEvent e){
                userAttrTableKeyReleased(e);
            }
        });
        view.settingToolButton.addActionListener(new SettingsActionListener());
        view.setingsItem.addActionListener(new SettingsActionListener());
        view.quitItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){System.exit(0);}
        });
        view.addToolButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){eStrategy.showNewDialog();}
        });
        userEditDialog.saveButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){eStrategy.saveDn();}
        });
        userEditDialog.deleteButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){eStrategy.deleteDn();}
        });
        userEditDialog.addMemberButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){addMemberToUser();}
        });
        userEditDialog.deleteMemberButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){removeMemberToUser();}
        });
        groupEditDialog.acceptButton.addActionListener(userEditDialog.saveButton.getActionListeners()[0]);
        groupEditDialog.deleteButton.addActionListener(userEditDialog.deleteButton.getActionListeners()[0]);
        view.deleteToolButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){eStrategy.deleteDn();} 
        });
        view.editToolButton.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){eStrategy.objectListDoubleClicked(
                   view.objectsList.getSelectedIndex());} 
        });
        view.usersItem.addActionListener(view.usersToogledButton.getActionListeners()[0]);
        view.groupItem.addActionListener(view.groupToogledButton.getActionListeners()[0]);
        view.addItem.addActionListener(view.addToolButton.getActionListeners()[0]);
        view.editItem.addActionListener(view.editToolButton.getActionListeners()[0]);
        view.deleteItem.addActionListener(view.deleteToolButton.getActionListeners()[0]);
        view.quitToolButton.addActionListener(view.quitItem.getActionListeners()[0]);
    }
    
    public void objectListFilterChanged()
    {
        if(view.objectFilterField.getText().length()<=0)
        {
            objectListModel.setFilter("");
        }
        else
        {
            objectListModel.setFilter("displayName=_"+view.objectFilterField.getText()+"*");
        }
        objectListModel.select();
        objectListModel.sort();
    }
    
    public void changeEventStrategy(int strategyIndex)
    {
        switch(strategyIndex)
        {
            case 0:
            {
                eStrategy  = new AMUserEventStrategy(this); 
                view.usersToogledButton.setSelected(true);
                view.groupToogledButton.setSelected(false);
                objectListModel.setSearchBase("cn=Employees,dc=amursu,dc=ru");
                objectListModel.setRecordName("displayName");
                objectListModel.setFilter("");
                break;
            }
            case 1:
            {
                eStrategy  = new AMGroupEventStrategy(this);
                view.groupToogledButton.setSelected(true);
                view.usersToogledButton.setSelected(false);
                objectListModel.setSearchBase("cn=Groups,dc=amursu,dc=ru");
                objectListModel.setRecordName("description");
                objectListModel.setFilter("");
                break;
            }
        }
        objectListModel.select();
        objectListModel.sort();
    }
  
    public void createSettingsDialog()
    {
        System.out.print("hello");
        settingsDialog = new AMSettingsDialog(view,true);
        settingsDialog.setVisible(true);
    }
    
    public int restoreConfig()
    {
        view.setEnabled(true);
        settingsDialog.dispose();
        return 0;
    }
    
    private void establishConnection()
    {
        settings.chooseCategory("server"); 
        if(connection.tryToConnect(settings.getStringProperty("host"), settings.getIntProperty("port"))){           
            authDialog = new AMAuthDialog(view,true);
            authDialog.dnEdit.setText(settings.getStringProperty("bind_dn"));
            int x = view.getLocation().x, y = view.getLocation().y;
            x+=view.getWidth()/2;
            y+=view.getHeight()/2;
            authDialog.setLocation(x,y);
            authDialog.addWindowListener(null);
            authDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            boolean bind = false;
            while(!bind){
                authDialog.setVisible(true);
                bind = connection.tryToBind(settings.getStringProperty("bind_dn"), authDialog.passEdit.getText());
            }
        }
        else{
            JOptionPane.showMessageDialog(view, "Не удалось соединится с сервером: "
                                            +settings.getStringProperty("host")+":"
                                            +settings.getStringProperty("port")+".\nИспользуйте Файл->Настройки для исправления ошибки.",
                                            "Предупреждение",JOptionPane.WARNING_MESSAGE);
        }
    }
    
    public void userAttrTableKeyReleased(KeyEvent e){     
        switch(e.getKeyCode()){
            case 127: 
                int row = settingsDialog.userAttrTable.getSelectedRow();
                if (settingsDialog.userAttrTable.isEditing())
                    settingsDialog.userAttrTable.getCellEditor().stopCellEditing();
                userSettingsModel.removeRow(row);
                break;
            default: break;
        }
    }
    public void addMemberToUser(){
        int index = userEditDialog.groupList.getSelectedIndex();
        AMLdapRecord r = new AMLdapRecord();
        r.setName(groupListModel.getRecordAt(index).getName());
        r.addValue("dn", groupListModel.getRecordAt(index).getValue("dn"), 0);
        r.addValue("member", accountmanager.controllers.AMExpresionParser
                .executeExpresion("lower(tr(uid=+"
                +userEditDialog.givenNameEdit.getText().split(" ")[0] +"+.+"
                +userEditDialog.snEdit.getText()+","
                +settings.getStringProperty("server", "user_base")+
                "))", new java.util.HashMap()), 2);
        System.out.println(r.toSString());
        if(index>=0&&index<groupListModel.getSize()){
            if(!userGroupModel.setRecord(r, -1))
                JOptionPane.showMessageDialog(view, "Ошибка при переносе группы", "Ошибка"
                        , JOptionPane.ERROR_MESSAGE);
        }
        else{
           JOptionPane.showMessageDialog(view, "Выделите одну из групп\r\nв левом списке.", "Предупраждение"
                        ,JOptionPane.WARNING_MESSAGE);
        }
    }
    public void removeMemberToUser(){
        int index = userEditDialog.memberList.getSelectedIndex();
        AMLdapRecord r = new AMLdapRecord();
        r.addValue("dn",userGroupModel.getRecordAt(index).getValue("dn"),0);
        r.addValue("member", objectListModel.getRecordAt(view.objectsList.getSelectedIndex())
                .getValue("dn"), 2);
        deletingBuffer.add(r);
        if(!userGroupModel.removeRecord(index)){
           JOptionPane.showMessageDialog(view, "Выделите одну из групп\r\nв правом списке.", "Предупраждение"
                        ,JOptionPane.WARNING_MESSAGE);
        }

    }
    
    
    class SettingsActionListener implements ActionListener{
        @Override
            public void actionPerformed(ActionEvent e)
            {               
                settings.chooseCategory("server");
                settingsDialog.servetEdit.setText(settings.getStringProperty("host"));
                settingsDialog.dnEdit.setText(settings.getStringProperty("bind_dn"));
                settingsDialog.userBaseEdit.setText(settings.getStringProperty("user_base"));
                settingsDialog.groupBaseEdit.setText(settings.getStringProperty("group_base"));
                settingsDialog.userAttrTable.setModel(userSettingsModel);
                settingsDialog.groupAttrTable.setModel(groupSettingsModel);
                settings.chooseCategory("user");
                settingsDialog.objectClassesEdit.setText(settings.getStringProperty("objectClass"));
                settingsDialog.objectClassesEditGroup.setText(settings.getStringProperty("group","objectClass"));
                if(settingsDialog.exec()){
                    settings.chooseCategory("server");
                    settings.setStringProperty("host", settingsDialog.servetEdit.getText());
                    settings.setStringProperty("bind_dn", settingsDialog.dnEdit.getText());
                    settings.setStringProperty("user_base", settingsDialog.userBaseEdit.getText());
                    settings.setStringProperty("group_base", settingsDialog.groupBaseEdit.getText());
                    settings.chooseCategory("user");
                    settings.setStringProperty("objectClass", settingsDialog.objectClassesEdit.getText());
                    settings.setStringProperty("group","objectClass", settingsDialog.objectClassesEditGroup.getText());
                    if (settingsDialog.userAttrTable.isEditing())
                        settingsDialog.userAttrTable.getCellEditor().stopCellEditing();
                    if (settingsDialog.groupAttrTable.isEditing())
                        settingsDialog.groupAttrTable.getCellEditor().stopCellEditing();
                    if(!settings.save()){
                        JOptionPane.showMessageDialog(view, 
                                "Произошла ошибка при сохранении файла конфигурации"
                                , null, JOptionPane.WARNING_MESSAGE);
                    }
                    else{
                        try{
                            connection.disconnect();
                            establishConnection();
                        }
                        catch(Exception ex){
                            System.out.println("disconnect error");
                        }        
                    }
                }
                else{
                    settings.reload();
                }
            }
   }
   
   class MyCombo extends AMListModel implements ComboBoxModel{
       private String selectedItem; 
       public MyCombo(){super();}
       public MyCombo(AMLdapConnection ld){
             super(ld);
       }
       public MyCombo(AMLdapConnection ld,String sb,String f,String[] list,String rn){
             super(ld,sb,f,list,rn);
       }
       public Object getSelectedItem(){
           return selectedItem;
       }
       public boolean select(){
           boolean rez = super.select();
           if(rez){
                selectedItem = data.get(0).getName();
           }
           super.updateListeners();
           return rez;   
       }
       public void sort(){
           super.sort();
           selectedItem = data.get(0).getName();
           super.updateListeners();
       }
       public void setSelectedItem(Object nItem){
           selectedItem = nItem.toString();
           super.updateListeners();
       }
   }
}
