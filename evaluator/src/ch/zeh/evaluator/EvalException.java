
package ch.zeh.evaluator;

import java.io.*;
import java.util.*;

/** 
    * Diese Klasse ist die Oberklasse aller Exceptions
    * die im Package Evaluator auftreten koennen.

    *@see ExpressionException 

*/

abstract public class EvalException extends Exception
{
    protected EvalException(String s)
    {
        super(s);
    }
    
    /**
	*Gibt eine detaillierte Fehlermeldung in
	*den Stream aus. Der Fehlertext kann
	*sich ueber mehrere Zeilen erstrecken.
    *
    *@param w In diesen Stream wird die
	*Fehlermeldung geschrieben.
    */

    final public void printMessage(PrintStream w)
    {
        Vector v=new Vector();
        getMessages(v);
        
        Enumeration _enum=v.elements();
        while (_enum.hasMoreElements())
        {
            w.println( (String)_enum.nextElement() );
        }
    }
    
    /**
     *Schreibt eine detaillierte Fehlermeldung in den 
     *Vector v. Jedes Element des Vectors entspricht einer
     *Zeile der Fehlermeldung und ist vom Typ String.
     *
     *@param v Der Vector in die Fehlermeldung geschrieben werden.
     *Es werden Strings eingefuegt. Jeder String stellt dabei
     *eine Zeile der Fehlermeldung dar.
    */
    public void getMessages(Vector v)
    {
        v.addElement( getMessage() );
    }
}