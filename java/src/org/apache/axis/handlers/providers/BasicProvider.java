package org.apache.axis.handlers.providers;

import java.util.Hashtable;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.utils.QName;

/**
 * This class has one way of keeping track of the
 * operations declared for a particular service
 * provider.  I'm not exactly married to this though.
 */
public abstract class BasicProvider extends BasicHandler { 
    
    public void addOperation(String name, QName qname) {
        Hashtable operations = (Hashtable)getOption("Operations");
        if (operations == null) {
            operations = new Hashtable();
            addOption("Operations", operations);
        }
        operations.put(qname, name);
    }
    
    public String getOperationName(QName qname) {
        Hashtable operations = (Hashtable)getOption("Operations");
        if (operations == null) return null;
        return (String)operations.get(qname);
    }
    
    public QName[] getOperationQNames() {
        Hashtable operations = (Hashtable)getOption("Operations");
        if (operations == null) return null;
        Object[] keys = operations.keySet().toArray();
        QName[] qnames = new QName[keys.length];
        System.arraycopy(keys,0,qnames,0,keys.length);
        return qnames;
    }
    
    public String[] getOperationNames() {
        Hashtable operations = (Hashtable)getOption("Operations");
        if (operations == null) return null;
        Object[] values = operations.values().toArray();
        String[] names = new String[values.length];
        System.arraycopy(values,0,names,0,values.length);
        return names;
    }
    
}
