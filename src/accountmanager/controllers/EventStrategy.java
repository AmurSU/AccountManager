/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package accountmanager.controllers;

/**
 *
 * @author georg
 */
public interface EventStrategy {
    public void objectListDoubleClicked(int selected);
    public void showNewDialog();
    public void saveDn();
    public void deleteDn();
}
