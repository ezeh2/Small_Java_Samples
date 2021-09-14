
package ch.zeh.evaluator;

/**
 *Diese Helper-Klasse representiert ein Key-Value Paar. 
 *<p>
 *Beispiel: a=3+4 --> a ist key und 3+4 value
 *<p>
 *Es existieren zwei Konstruktoren. Der eine akzeptiert
 *ein String der Art a=3+4 und fuehrt die Aufteilung in Key
 *Value selbststaendig aus. Der andere erwartet, dass man ihm
 *key und value getrennt uebergibt. Beide Konstruktoren werfen
 *die Exception ExpressionException falls ein Syntaktischer Fehler
 *vorliegt.
 *<p>
 *@see ExpressionException
*/

final public class KeyValueString
{
    static public void main(String [] args)
    {
        try
        {
            KeyValueString kvstring=new KeyValueString("a=12+12");
            System.out.println( kvstring.getKey() );
            System.out.println( kvstring.getValue() );
            System.out.println( kvstring.getString() );
        }
        catch (ExpressionException e)
        {
            e.printMessage(System.out);
            System.out.println("-------");
        }
        try
        {
            KeyValueString kvstring=new KeyValueString("12+12");
            System.out.println( kvstring.getKey() );
            System.out.println( kvstring.getValue() );
            System.out.println( kvstring.getString() );
        }
        catch (ExpressionException e)
        {
            e.printMessage(System.out);
            System.out.println("-------");
        }
        try
        {
            KeyValueString kvstring=new KeyValueString("=12+12");
            System.out.println( kvstring.getKey() );
            System.out.println( kvstring.getValue() );
            System.out.println( kvstring.getString() );
        }
        catch (ExpressionException e)
        {
            e.printMessage(System.out);
            System.out.println("-------");
        }
        try
        {
            KeyValueString kvstring=new KeyValueString("abc=12**12");
            System.out.println( kvstring.getKey() );
            System.out.println( kvstring.getValue() );
            System.out.println( kvstring.getString() );
        }
        catch (ExpressionException e)
        {
            e.printMessage(System.out);
            System.out.println("-------");
        }
        try
        {
            KeyValueString kvstring=new KeyValueString("abc=");
            System.out.println( kvstring.getKey() );
            System.out.println( kvstring.getValue() );
            System.out.println( kvstring.getString() );
        }
        catch (ExpressionException e)
        {
            e.printMessage(System.out);
            System.out.println("-------");
        }
        try
        {
            KeyValueString kvstring=new KeyValueString("a","12+12");
            System.out.println( kvstring.getKey() );
            System.out.println( kvstring.getValue() );
            System.out.println( kvstring.getString() );
        }
        catch (ExpressionException e)
        {
            e.printMessage(System.out);
            System.out.println("-------");
        }
        try
        {
            KeyValueString kvstring=new KeyValueString("a=(12++++12)");
            System.out.println( kvstring.getKey() );
            System.out.println( kvstring.getValue() );
            System.out.println( kvstring.getString() );
        }
        catch (ExpressionException e)
        {
            e.printMessage(System.out);
            System.out.println("-------");
        }
        try
        {
            KeyValueString kv1=new KeyValueString("a=(12++++12)");
            KeyValueString kv2=new KeyValueString("a=(12++++12)");
            KeyValueString kv3=new KeyValueString("a=(13++++12)");
            System.out.println(kv1.equals(kv2));
            System.out.println(kv2.equals(kv3));
        }
        catch (ExpressionException e)
        {
            e.printMessage(System.out);
            System.out.println("-------");
        }
    }
    
    /**
     *Dieser Konstruktor erwartet, dass man ihm Key und Value
     *getrennt uebergibt.Falls ein Syntaktischer Fehler vor-
     *liegt wird die Exception ExpressionException geworfen.
     *<p>
     *@param key String der Art ab oder  a23
     *@param value String welche einen Ausdruck der Art enthaelt 3*(4+5)
     *@exception ExpressionException wird geworfen, falls im Value 
     *ein sytaktischer Fehler vorliegt.
    */
    public KeyValueString(String key,String value) throws ExpressionException
   {
        setKey(key);
        setValue(value);
    }

    /**
     *Dieser Konstruktor erwartet, dass ihm einen String der Art a=3+4
     *uebergibt. Er fuehrt die Aufteilung in key und value selbstaendig durch
     *<p>
     *@param keyvalue String der Art a=12+30 oder ab=3*(4+5)
     *@exception ExpressionException wird geworfen falls ein Syntax-Fehler
     *vorliegt.
    */
    public KeyValueString(String keyvalue) throws ExpressionException
    {
        // suche das Gleichheitszeichen
        int pos=0;
        while ( (pos<keyvalue.length()) && (keyvalue.charAt(pos)!='=') )
        {
            pos++;
        }

        // kein Gleichheitszeichen ?
        if (pos>=keyvalue.length())
        {
            // Fehler, wirf Exception !
            EvalException ex=ExceptionFactory.missingCharacter('=');
            throw ExceptionFactory.errorInExpression(ex,keyvalue,0);
        }
        
        // Gleichheitszeichen an erster Stelle ?
        else if (pos==0)
        {
            // Fehler, wirf Exception !
            EvalException ex=ExceptionFactory.unexpectedCharacter('=');
            throw ExceptionFactory.errorInExpression(ex,keyvalue,0);
        }
        else 
        {
            setKey(  keyvalue.substring(0,pos)  );
            try
            {
                setValue(  keyvalue.substring(pos+1,keyvalue.length())  );
            }
            // Fehler in Ausdruck ?
            catch (ExpressionException ex)
            {
                // Fehler, wirf Exception weiter ! Korrigiere Position, so dass
                // der Key und das = mitberuecksichtigt werden.
                throw ExceptionFactory.errorInExpression(ex,keyvalue,ex.getPosition()+pos+1);
            }
        }
    }

    /**
     *liefert true zurueck wenn Key und Value von kv
     *und diesem Object die gleiche Zeichenfolge
     *aufweisen.
     *<p>
     *@param kv die zu vergleichende Instanz
     *@return liefert true wenn uebergebenes Obkject identisch
     *mit diesem Object ist.
    */
    public boolean equals(KeyValueString kv)
    {
        return (  (kv.getKey().equals(getKey())) && (kv.getValue().equals(getValue()))  );
    }

    /**
     *liefert den Schluessel
     *
     *@return Schluessel-String
    */
    final public String getKey()
    {
        return Key;
    }

    /**
     *liefert den Value
     *
     *@return Value-String
    */
    final public String getValue()
    {
        return Value;
    }

    final public String getString()
    {
        return Key+"="+Value;   
    }
    
    final private void setKey(String key)
    {
        Key=key;   
    }
    
    final private void setValue(String value)  throws ExpressionException
    {
        Evaluator eval=new Evaluator();
        eval.check(value);
        Value=value;
    }
    
    private String Key;
    private String Value;
}