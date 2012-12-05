/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package accountmanager.models;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;

/**
 *
 * @author georg
 */
public class AMLdapConnection extends LDAPConnection {
    
    public AMLdapConnection(int timeout)
    {
            super(1000);
    }    
    public AMLdapConnection()
    {
            super();
    }
    public boolean tryToConnect(String host,int port){
        try{
            this.connect(host, port);
        }
        catch(LDAPException e){
            return false;
        }
        return true;
    }
    public boolean tryToBind(String bind_dn,String bind_pass){
        try{
            this.bind(LDAPConnection.LDAP_V3,bind_dn,bind_pass);
        }
        catch(LDAPException e)
        {
            return false;
        }
        return true;
    }
}
