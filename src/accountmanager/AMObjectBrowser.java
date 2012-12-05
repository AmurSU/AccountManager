/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package accountmanager;
import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.GridLayout;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.util.List;
import java.util.ArrayList;
import accountmanager.models.AMLdapRecord;
/**
 *
 * @author georg
 */
public class AMObjectBrowser extends JPanel{
    private AMLdapRecord model = null;
    public List<JTextField> fields = new ArrayList<JTextField>();
    public List<JLabel> labels = new ArrayList<JLabel>();
    private GridLayout layout;
    private boolean firstInit = true;
    
    public AMObjectBrowser()
    {
        super();
    }
    
    public void setModel(AMLdapRecord m)
    { 
        model = m;
        init();
    }
    
    public AMLdapRecord getModel()
    {
        return model;
    }
    private void init()
    {
        //fields.clear();
        //labels.clear();
        int n = model.size();
        
        layout = new GridLayout(n,2);
        this.setLayout(layout);
        if(firstInit){
            for(int i=0;i<n;i++)
            {
                fields.add(new JTextField(model.getValueAt(i)));
                labels.add(new JLabel(model.getHeaderAt(i)));
                fields.get(i).setEditable(false);
                this.add(labels.get(i));
                this.add(fields.get(i));
            }
            firstInit = false;
        }
        else
        {
            for(int i=0;i<n;i++)
            {
                fields.get(i).setText(model.getValueAt(i));
                labels.get(i).setText(model.getHeaderAt(i));
            }
        }
        //this.setPreferredSize(new java.awt.Dimension(400,400));
        //this.setMinimumSize(new java.awt.Dimension(400,400));
        System.out.print(n);
    }
}
