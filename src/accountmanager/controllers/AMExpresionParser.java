/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package accountmanager.controllers;
import java.util.Map;
/**
 *
 * @author georg
 */
public class AMExpresionParser {
    static public String executeExpresion(String expresion,Map map){
        if(expresion.matches("[a-zA-Zа-яА-Я0-9_=[ ][.][,]]+")){
            if(map.get(expresion)==null)
                return expresion;
            else
                return map.get(expresion).toString();
        }
        //([a-z]+)[(]([a-zA-Z0-9+()]+)[)]
        if(expresion.matches("([a-z]+)[(](.+)[)]")){
            String operation = expresion.replaceAll("[(](.+)[)]", "");
            String operand = expresion.replaceFirst("([a-zA-Z]+)[(]","");
            operand = operand.substring(0, operand.length()-1);
            String operandResult = AMExpresionParser.executeExpresion(operand, map);
            if(operandResult!=null&&operation.compareTo("tr")==0){
                return AMExpresionParser.translit(operandResult);
            }
            if(operandResult!=null&&operation.compareTo("upper")==0){
                return AMExpresionParser.translit(operandResult).toUpperCase();
            }
            if(operandResult!=null&&operation.compareTo("lower")==0){
                return AMExpresionParser.translit(operandResult).toLowerCase();
            }
            //System.out.println("global operation");
        }
        String expresion1 = expresion+"+";
        //[[+]?[a-zA-Z0-9()]+]+
        if(expresion1.matches("(.+)([+])+")){
            String[] operands = expresion.split("[+]");
            //System.out.println("concat");
            for(int i=0;i<operands.length;i++){
                operands[i] = AMExpresionParser.executeExpresion(operands[i], map);
            }
            return AMExpresionParser.concat(operands);
        }
        return null;
    }
    
    static public String translit(String str)
    {
        StringBuffer translited = new StringBuffer();
        String alpha = new String("абвгдеёжзийклмнопрстуфхцчшщьыэюяAБВГДЕЁЖЗИКЛМНОПРСТУФХЦЧШЩЬЫЭЮЯ");
	String[] _alpha = {"a","b","v","g","d","e","yo","zh","z","i","yi",
	                   "k","l","m","n","o","p","r","s","t","u",
	                   "f","h","tz","ch","sh","sh","'","y","e","yu","ya",
                           "A","B","V","G","D","E","Yo","Zh","Z","I","Yi",
	                   "K","L","M","N","O","P","R","S","T","U",
	                   "F","H","Tz","Ch","sh","sh","'","Y","E","Yu","Ya"};
        int ind;
        for(int i=0;i<str.length();i++){
            ind = alpha.indexOf(str.charAt(i));
            if(ind>=0)
                translited.append(_alpha[ind]);
            else
                translited.append(str.charAt(i));       
        }
        return translited.toString();
    }
    
    static public String concat(String[] list){
        StringBuffer concated = new StringBuffer();
        for(int i=0;i<list.length;i++){
            concated.append(list[i]);
        }
        return concated.toString();
    }
}
