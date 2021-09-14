
package ch.zeh.evaluator;

import java.io.*;
import java.util.*;

/** 
    With this class one can create all exceptions
    thrown in this package. ExceptionFactory cannot be
    instantieted and has only static methods.

    Mit dieser Klasse werden alle Exceptions im Package
    evaluator erzeugt. ExceptionFactory kann nicht instanziert werden
    und besitzt nur static-methoden.
    
    Mit Hilfe der Factory wird versteckt, welche Klasse tatsaechlich
    instanziert wird. 
    
    Die Klasse ist lokal zum Package, d.h. kann von
    einem anderen Package nicht aufgerufen werden.
*/
class ExceptionFactory
{
    // Diese Klasse kann man nicht instanzieren
    private ExceptionFactory()
    {
    }

    // Der Ausdruck hat ein unerwartetes Ende. z.b. 1+1*
    static public StringEvalException unexpectedEnd()
    {
        return new StringEvalException("unexpected End");
    }

    // Im Ausdruck kommen unbekannte Zeichen vor oder es sind
    // Zeichen am falschen Ort. z.b. 1**1
    static public StringEvalException unexpectedCharacter(char ch)
    {
        return new StringEvalException("unexpected Character: " + ch);
    }

    // Im Ausdruck kommt ein Wort an einer Stelle vor, wo
    // eigentlich keines sein duerfte. z.b. 10+10ha
    static public StringEvalException unexpectedWord(String word)
    {
        return new StringEvalException("unexpected Word: " + word);
    }

    // Im Ausdruck fehlen Klammern z.b. ((10+2)
    static public StringEvalException missingCharacter(char ch)
    {
        return new StringEvalException("missing Character: " + ch);
    }
    
    // Im Ausdruck kommen Variabeln vor, die nirgends definert sind.
    // z.b 10+a  Aber a ist nicht im Dictionary drin.
    static public StringEvalException unknownKey(String key)
    {
        return new StringEvalException("unknown key: " + key);
    }

    // Ein Ausdruck enthaelt eine Variable. Aber diese Variable verweist
    // indirekt auf den Ausdruck, der diese Variable enthaelt.
    // Wuerde man einen solchen Fehler nicht erkennen, wuerde das Programm
    // solange im Kreis laufen, bis ein StackOverflow auftritt.
    // z.B.
    // a=b
    // b=a
    static public StringEvalException alreadyUsedKey(String key)
    {
        return new StringEvalException("already used key " + key + " ---> you have a closed loop !");
    }

    // Es trat ein Fehler in einem Ausdruck auf. Es wird der 
    // fehlerhafte String und die Position, wo der Fehler auftrat
    // uebergeben. Zusaetzlich wird eine Referenz auf die
    // Exception uebergeben, welche vom betreffenden Baumelement
    // geworfen wurde.
    // z.B.
    // 10**a
    // die geworfene Exception enthaelt:
    // expr=10**a
    // pos=4
    // ex= eine Exception-Instanz, welche mit unexpectedCharacter(*) erzeugt wurde.
    static public ExpressionException errorInExpression(EvalException ex,String expr,int pos)
    {
        return new ExpressionException(ex,expr,pos);
    }

    // Der Ausdruck auf den dieser Key(=Variable) verweist
    // ist fehlerhaft. ex enthaelt Exception, welche beim
    // Auswerten des Stringes geworfen wurde.
    static public KeyException errorInKey(EvalException ex,String key)
    {
        return new KeyException(ex,key);
    }
}

class StringEvalException extends EvalException
{
    public StringEvalException(String s)
    {
        super(s);
    }
}

class KeyException extends EvalException
{
    public KeyException(EvalException ex,String key)
    {
        super("error in expression represented by "+key);
        _NestedEvalException=ex;
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
        v.addElement(getMessage());

        // 2. Zeile und folgende
        _NestedEvalException.getMessages(v);
    }
    
    private EvalException _NestedEvalException;
}

// ExpressionException ist in einem separaten File
// definiert.

