
package ch.zeh.evaluator;

import java.io.*;
import java.util.*;

/**
  *Diese Exception wird geworfen, wenn ein Syntaxfehler
  *auftrat, wie z.B. 10**10
*/

public class ExpressionException extends EvalException
{
    /** 
      *erzeugt eine Exception, welche einen Sytaxfahler
      *anzeigt. Der Anwender dieses Packages wird wohl nie
      *solche Exception direkt erzeugen.
      *
      *@param ex      Exception, welche vom TreeElement ursprueglich geworfen wurde.
      *@param expr   Fehlerhafte Ausdruck.
      *@param pos    Position im String, wo der fehler auftrat.
    */
    public ExpressionException(EvalException ex,String expr,int pos)
    {
        super(expr+"  error at position "+pos);
        _NestedEvalException=ex;
        Pos=pos;
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
        // 1. Zeile
        v.addElement( getMessage() );

        // 2. Zeile
        StringBuffer sbuffer=new StringBuffer();
        for (int i=0;i<Pos-1;i++)
        {
            sbuffer.append(' ');
        }
        sbuffer.append('|');
        v.addElement( sbuffer.toString() );

        // 3. Zeile und folgende
        _NestedEvalException.getMessages(v);
    }

    /** Liefert die genaue Cursorposition, wo der 
        *Fehler auftrat.
        *
        *@return Position im Ausdruck, wo der Fehler auftrat.
    */
    public int getPosition()
    {
        return Pos;
    }
    
    private EvalException _NestedEvalException;
    private int Pos;
}
