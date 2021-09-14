
package ch.zeh.evaluator;

import java.io.*;
import java.util.*;

/** 
*Mit Hilfe der Klasse Evaluator kann man Ausdruecke der Art 3*(3+2*(-1+2)+1) 
*oder a*(bc+1) auswerten.
*<p>
*Der erste Ausdruck enthaelt keine Variabeln und kann deshalb einfach der 
*Methode eval uebergeben werden. Deren Returnwert ist das Ergebnis. Sollte 
*der Ausdruck einen syntaktischen Fehler enthalten, wird eine Exception geworfen.
*<p>
*Der Ausdruck a*(bc+1) ist ein Ausdruck mit Variabeln. Damit Evaluator diesen
*richtig aufloesen kann, muss beim Instanzieren dem Konstruktor ein Dictionary
*uebergeben werden. Dieses enthaelt fuer jede Variable einen Ausdruck. Sowohl Key
*als auch Value sind vom Typ String. Mit der Methode eval kann der Ausdruck
*ausgewertet werden. Neben Exceptions fuer Syntaxfehler werden auch Exceptions
*geworfen, falls eine Variable nicht verfuegbar ist.
*<p>
*Manchmal moechte man pruefen, ob ein Ausdruck syntaktisch richtig ist, auch
*wenn einzelne Variabeln im Ausdruck nicht definert sind. Dafuer steht die
*Methode check zur Verfuegung. Der ihr uebergeben String wird NUR auf Syntaxfehler
*geprueft. Falls alles ok ist, wird -1 zurueckgeliefert. Im Falle eines Fehlers
*ist der Rueckgabewert die Position im String, wo der Fehler auftrat.
*/

final public class Evaluator
{
    /**
     *Enthaelt Testcode. Diese Methode kann mittels java ch.zeh.evaluator.Evaluator
     *direkt aufgerufen werden.
     *
     *param args Argumente der Kommandozeile, welche aber nicht verwendet werden.
   */
    public static void main(String [] args)
    {
        /*
        Hashtable hash1=new Hashtable();
        hash1.put("a","3");
        hash1.put("b","2*a");
        hash1.put("c","-b/a");
        hash1.put("d","1>2?1:2+1");
        hash1.put("z","2*d");
        Evaluator e1=new Evaluator(hash1);

        try
        {
            Dictionary d=e1.eval();
            Enumeration res=d.keys();
            while (res.hasMoreElements())
            {
                Object key=res.nextElement();
                Object value=d.get(key);
                System.out.println( (String)key+" = "+(String)value );
            }
        }
        catch (EvalException ex)
        {
            ex.printMessage(System.out);
        }
        */
    
        Hashtable hash=new Hashtable();
        hash.put("a","3");
        hash.put("b","2*a");
        hash.put("c","2+*a");
        hash.put("d","2*c");
        hash.put("z","y");
        hash.put("y","z");
        Evaluator e=new Evaluator(hash);
        LineNumberReader lreader=new LineNumberReader(new InputStreamReader(System.in));
        String line=null;
        while (true)
        {   
            try
            {
                line=lreader.readLine();
            }
            catch (Exception f)
            {
            }
            try
            {
                System.out.println(e.eval(line));
            }
            catch (EvalException ex)
            {
                ex.printMessage(System.out);
            }
        }
    }
    
    /** 
    *instanziert Evaluator. Man kann damit Ausdruecke
    *auswerten. Die Ausdruecke duerfen Variabeln enthalten, 
    *welche in dic definiert sein muessen.
    *
    *@param dic Dictionary, welches fuer jede Variable
    *einen Ausdruck enthaelt.
    */
    public Evaluator(Dictionary dic)
    {
        Dic=dic;
        States=new EvaluatorState[2];
        States[0]=new EvaluatorStateS0(1);
        States[1]=new EvaluatorStateS1(0);
        reset();
    }
    
    /** 
    *instanziert auch einen Evaluator. Allerdings muss
    *kein Dictionary uebergeben werden. Es ist deshalb nicht 
    *moeglich Ausdruecke auszuwerten, welche Variabeln
    *enthalten.
    */
    public Evaluator()
    {
        this(null);
    }
    
    /**
     *prueft NUR ob der uebergebene String syntaktisch richtig ist.
     *Falls Variablen im String nicht definert sind, spielt dies
     *KEINE Rolle.
       
     *Falls kein Fehler vorliegt, kehrt die Funktion normal zurueck.
     *Im Falle eines Fehlers wird die Exception ExpressionException
     *geworfen.
     *
     *@param expr  Der zu ueberpruefende Ausdruck. 
     *@exception  ExpressionException
     *
     *@see ExpressionException
     *@param expr Der zu pruefende Ausdruck
     *@exception ExpressionException wird geworfen falls expr
     *Syntaxfehler enthaelt.
    */
    final public void check(String expr) throws ExpressionException
    {
        // baue den Syntaxbaum auf
        TreeElement treeElement=getTree(expr);
    }
    
    /** 
    *wertet den String aus und liefert das Ergebnis zurueck.
    *Falls der String syntaktische Fehler enthaelt, wird die
    *Exception ExpressionException geworfen. 
    *
    *Sollte eine Variable nicht definiert
    *sein, (d.h. sie ist nicht im Dictionary drin)
    *wird die Exception EvalException geworfen.
    *
    *@param expr Der auszuwertende String.
    *@return  Das Ergebnis der Auswertung
    *@exception EvalException wird geworfen, falls eine 
    *Variable nicht definiert ist oder eine 
    *Kreisabhaengigkeit vorliegt.
    *@exception ExpressionException wird geworfen falls ein Syntax
    *fehler der Art 2**2 vorliegt.
    */
    final public double eval(String expr) throws EvalException
    {
        return getTree(expr).eval();
    }

    /**
     *Das Dictionary, das dem ctor uebergeben wurde, wird
     *aufgelöst. Es wird ein neues Dictionary erzeugt, 
     *welches dieselben Schlüssel enthält, aber alle Values
     *entsprechen den Resultaten.
     *
     *@return  Das Dictionary, in welchem alle Ausdruecke aufgelöst sind.
     *@exception EvalException wird geworfen, falls irgendein Ausdruck einen
     *syntaktischen Fehler enthält.
    */
    final public Dictionary eval() throws EvalException
    {
        Dictionary ret=new Hashtable();
        
        Enumeration e=Dic.keys();
        while (e.hasMoreElements())
        {
            Object key=e.nextElement();
            double res=eval((String)Dic.get(key));
            // ret.put(key,(new Double(res)).toString());
			ret.put(key,(Double.valueOf(res)).toString());
        }
        
        return ret;
    }

    /** 
    *baut fuer den uebergebenen String NUR den Syntaxbaum auf.
    *Falls der String syntaktische Fehler enthaelt, wird eine
    *Exception geworfen.
    *
    *@param expr Der Ausdruck, fuer den der Baum aufzubauen ist.
    *@return Referenz auf das Wurzelelement des Baums
    *@exception ExpressionException wird geworfen wenn expr
    *ein Syntaxfehler enthaelt.
    */
    final private TreeElement getTree(String expr) throws ExpressionException
    {   
        
        // setzte state machine zurueck
        // System.out.println("reset");
        reset();
        
        // Der Parser wird eingerichtet. Er kann Zahlen, Woerter sowie 
        // die Operatoren voneinander unterscheiden.
        StreamTokenizer tokenizer=createStreamTokenizer(expr);
        tokenizer.parseNumbers();
        tokenizer.slashSlashComments(true);  // erkennt C-comment
        tokenizer.ordinaryChar('+');
        tokenizer.ordinaryChar('-');
        tokenizer.ordinaryChar('*');
        tokenizer.ordinaryChar('/');
        tokenizer.ordinaryChar('(');
        tokenizer.ordinaryChar(')');
        tokenizer.ordinaryChar('%');
        tokenizer.ordinaryChar('?');
        tokenizer.ordinaryChar(':');
        tokenizer.ordinaryChar('<');
        tokenizer.ordinaryChar('>');
        tokenizer.ordinaryChar('=');
        tokenizer.ordinaryChar('&');
        tokenizer.ordinaryChar('|');
        
        int oldPos=0;
        try
        {
            // speichert position. Im Fehlerfall
            // wird die Position in der geworfenen 
            // Exception mitgeliefert.
            oldPos=getPosition();
            
            // hole erstes Token
            int token=tokenizer.nextToken();
            
            // hole solange tokens bis es keine mehr hat
            while ( token!=StreamTokenizer.TT_EOF )
            {
                // abhaengig vom tokentyp wird ein
                // anderer Zweik angesprungen.
                switch (token)
                {
                    // ein Wort, z.B. a, aab, wert, zins usw.
                    case StreamTokenizer.TT_WORD:
                    {
                        // System.out.println(tokenizer.sval);
                        getStateInstance().eventWord(tokenizer.sval);
                        break;
                    }
                    // Prozentzeichen
                    case '%':
                    {
                        // System.out.println(tokenizer.sval);
                        getStateInstance().eventWord(new String("%"));
                        break;
                    }
                    // Fragezeichen, ist in Ausdruecken wie 2>3 ? 2 : 1 enthalten
                    case '?':
                    {
                        // System.out.println(tokenizer.sval);
                        getStateInstance().eventQuestion();
                        break;
                    }
                    // Doppelpunkt, ist in Ausdruecken wie 2>3 ? 2 : 1 enthalten
                    case ':':
                    {
                        // System.out.println(tokenizer.sval);
                        getStateInstance().eventPoints();
                        break;
                    }
                    case '<':
                    {
                        // System.out.println(tokenizer.sval);
                        getStateInstance().eventCondition('<');
                        break;
                    }
                    case '>':
                    {
                        // System.out.println(tokenizer.sval);
                        getStateInstance().eventCondition('>');
                        break;
                    }
                    case '=':
                    {
                        // System.out.println(tokenizer.sval);
                        getStateInstance().eventCondition('=');
                        break;
                    }
                    case '&':
                    {
                        // System.out.println(tokenizer.sval);
                        getStateInstance().eventAnd();
                        break;
                    }
                    case '|':
                    {
                        // System.out.println(tokenizer.sval);
                        getStateInstance().eventOr();
                        break;
                    }
                    // eine Zahl, z.B. 10, 34.12 usw.
                    case StreamTokenizer.TT_NUMBER:
                    {
                        // System.out.println(tokenizer.nval);
                        getStateInstance().eventNumber(tokenizer.nval);
                        break;
                    }
                    case '+':
                    {
                        // System.out.println((char)i);
                        getStateInstance().eventPlus();
                        break;
                    }
                    case '-':
                    {
                        // System.out.println((char)i);
                        getStateInstance().eventMinus();
                        break;
                    }
                    case '*':
                    {
                        // System.out.println((char)i);
                        getStateInstance().eventStar();
                        break;
                    }
                    case '/':
                    {
                        // System.out.println((char)i);
                        getStateInstance().eventSlash();
                        break;
                    }
                    case '(':
                    {
                        getStateInstance().eventBracket1();
                        break;
                    }
                    case ')':
                    {
                        getStateInstance().eventBracket2();
                        break;
                    }
                    // unbekanntes Zeichen
                    default:
                    {
                        // wirf Exception
                        throw ExceptionFactory.unexpectedCharacter('?');
                    }
                    
                } // switch

                // speichere Position
                oldPos=getPosition();
                
                // hole naechstes Token
                token=tokenizer.nextToken();
                
            } // while
            
            // pruefe ob irgendwelche syntaxfehler vorliegen
            // d.h ob es gleichviele schliessende wie oeffnende Klammern hat
            // und der letzte Zustand S0 ist.
            checkSyntax();
            
        } // try
        catch (EvalException ex)
        {
            // erzeuge neue exception mit dem fehlerhaften Ausdruck,
            // die Position wo der Fehler auftrat sowie die Exception
            // die das TreeElement geworfen hat.
            throw ExceptionFactory.errorInExpression(ex,expr,oldPos);
        }
        catch (IOException e)
        {
            // ???
        }
        
        // liefere Baum zurueck
        return getFactory().getTree();
    }
    
    // setzt state machine zurueck
    final private void reset()
    {
        Factory=new TreeFactory(Dic);
        Bracket=0;
        If=0;
        CurrentState=States[0];
    }
    
    // liefert die aktuelle Leseposition zurueck
    private int getPosition()
    {
        return _PositionReader.getPosition();
    }
    
    // erzeugt den StringReader der von einem Stream lesen kann.
    // der PositionReader ist eine Unterklasse von FileReader und
    // dient nur dazu ueber die aktuelle Leseposition Buch zu
    // fuehren. StreamTokenizer ist der konfigurierbare Parser.
    private StreamTokenizer createStreamTokenizer(String s)
    {
        _PositionReader=new PositionReader(new StringReader(s));
        return new StreamTokenizer(_PositionReader);
    }
    
    // setzt den aktuellen Zustand. Der aktuelle Zustand
    // wird durch eine Instanz der Unterklassen von EvalState
    // representiert.
    final private void setStateInstance(int i)
    {
        // System.out.println(i);
        CurrentState=States[i];
    }

    // Liefert eine Referenz auf die Instanz des
    // aktuellen Zustands zurueck.
    final private EvaluatorState getStateInstance()
    {
        return CurrentState;
    }

    final private TreeFactory getFactory()
    {
        return Factory;
    }

    // incBracket und decBracket wurden
    // eingefuehrt um fehlende '(' und
    // ')' zu erkennen und den Benutzer
    // mit einer Fehlermeldung darauf hinzuweisen

    // eine oeffnende Klammer mehr
    final private void incBracket()
    {
        // Hier wird um zwei erhoeht, weil die Zwischenebene
        // anderweitig verwendet wird. So wird beispielsweise
        // 2*3%*(1+2) in 2*3/100*(1+2) umgewandelt. Die Divison
        // ist aber hoeher als die Multiplikation und tiefer als die Multiplikation.
        // Die Multiplikation liegt in der Ebene 0, die Division in der Ebene 1
        // und die Addition in der Ebene 2. Siehe auch
        // EvaluatorStateS1.eventWord         

        Bracket+=2;
    }

    // eine schliessende Klammer mehr. 
    final private void decBracket()
    {
        Bracket-=2;
    }
    
    // incIf() und decIf() wurden eingefuehrt um
    // fehlende '?' und ':' zu entdecken und
    // dem Benutzer mittels Fehlermeldung
    // mitzuteilen

    // wird beim Auftreten eines '?' aufgerufen
    final private void incIf()
    {
        // ein 1?1+1:3 ist implizit ein 1?(1+1):3
        // d.h. der Ausdruck zwischen '?' und ':'
        // wird automatisch in Klammern gesetzt.
        incBracket();
        If++;
    }
    
    // wird beim Auftreten eines ':' aufgerufen
    final private void decIf()
    {
        decBracket();
        If--;
    }

    // liefert die aktuelle Klammerebene
    final private int getBracket()
    {
        return Bracket;
    }
    
    // Diese Methode wird am Schluss einer String-Auswertung
    // aufgerufen.Sie prueft, ob 
    // 1. Bracket den Wert null hat. Dies bedeutet, dass es im String
    //    gleichviele '(' wie ')' gibt.
    // 2. If den Wert null. Dies bedeutet, dass es im String gleichviele
    //    '?' wie ':' hat.
    // 2. der richtige Zustand vorliegt
    // Wird einer dieser Bedingungen verletzt, so wird eine Exception
    // geworfen.  
    final private void checkSyntax() throws EvalException
    {
        // 1. Gibt es gleichviele (-Klammern wie ) ?
        if (Bracket>0)
        {
            throw ExceptionFactory.missingCharacter(')');
        }
        if (Bracket<0)
        {
            throw ExceptionFactory.unexpectedCharacter(')');
        }
        
        // 2. Gibt es gleichviele '?' wie ':' ?
        if (If>0)
        {
            throw ExceptionFactory.missingCharacter(':');
        }
        if (If<0)
        {
            throw ExceptionFactory.unexpectedCharacter('?');
        }
        
        // 3. der richtige Endzustand
        if (!(CurrentState instanceof EvaluatorStateS1))
        {
            throw ExceptionFactory.unexpectedEnd();    
        }
    }
        
    // abstrakte Oberklasse fuer alle Zustandsklassen
    abstract class EvaluatorState
    {
        protected EvaluatorState(int nextState)
        {
            NextState=nextState;
        }

        abstract public void eventWord(String word) throws EvalException;
        abstract public void eventNumber(double number) throws EvalException ;
        abstract public void eventPlus()  throws EvalException;
        abstract public void eventMinus()  throws EvalException;
        abstract public void eventStar()  throws EvalException;
        abstract public void eventAnd()  throws EvalException;
        abstract public void eventOr()  throws EvalException;
        
        abstract public void eventSlash()  throws EvalException;
        abstract public void eventQuestion() throws EvalException;
        abstract public void eventPoints() throws EvalException;
        abstract public void eventCondition(char ch) throws EvalException;        

        abstract public void eventBracket1()  throws EvalException;
        abstract public void eventBracket2() throws EvalException ;
        
        protected void nextState()
        {
            setStateInstance(NextState);
        }
        
        private int NextState;
    }
    
    class EvaluatorStateS0 extends EvaluatorState
    {
        public EvaluatorStateS0(int nextState)
        {
            super(nextState);
        }

        public void eventWord(String word)
        {
            getFactory().addKey(word);
            nextState();
        }

        public void eventNumber(double number)
        {
            getFactory().addNumber(number);
            nextState();
        }

        public void eventPlus()
        {
            // ein +Vorzeichen
            
            // nichts zu tun
        }

        public void eventMinus()
        {
            // ein -Vorzeichen, fuege deshalb ein -1*
            // in den Baum ein
            
            getFactory().addNumber(-1);
            getFactory().addStar( getBracket()+1 );
        }

        public void eventStar() throws EvalException
        {
            throw ExceptionFactory.unexpectedCharacter('*');
        }

        public void eventSlash() throws EvalException
        {
            throw ExceptionFactory.unexpectedCharacter('/');
        }

        public void eventAnd() throws EvalException
        {
            throw ExceptionFactory.unexpectedCharacter('&');
        }
        
        public void eventOr() throws EvalException
        {
            throw ExceptionFactory.unexpectedCharacter('|');
        }
        
        public void eventQuestion() throws EvalException
        {
            throw ExceptionFactory.unexpectedCharacter('?');
        }

        public void eventPoints() throws EvalException
        {
            throw ExceptionFactory.unexpectedCharacter(':');
        }
        
        public void eventCondition(char ch) throws EvalException
        {
            getFactory().setCondition(ch);
        }
        
        public void eventBracket1()
        {
            incBracket();
        }

        public void eventBracket2() throws EvalException
        {
            throw ExceptionFactory.unexpectedCharacter(')');
        }
    }
    
    class EvaluatorStateS1 extends EvaluatorState
    {
        public EvaluatorStateS1(int nextState)
        {
            super(nextState);
        }

        public void eventWord(String word) throws EvalException
        {
            if (word.charAt(0)=='%')
            {
                getFactory().addSlash( getBracket()+1 );
                getFactory().addNumber(100);
            }
            else
            {
                throw ExceptionFactory.unexpectedWord(word);
            }
        }

        public void eventNumber(double number) throws EvalException
        {
            ExceptionFactory.unexpectedWord( Double.toString(number) );
        }

        public void eventPlus()
        {
            getFactory().addPlus( getBracket() );
            nextState();
        }

        public void eventMinus()
        {
            getFactory().addMinus( getBracket() );
            nextState();
        }

        public void eventStar()
        {
            getFactory().addStar( getBracket() );
            nextState();
        }

        public void eventSlash()
        {
            getFactory().addSlash( getBracket() );
            nextState();
        }
        
        public void eventAnd() throws EvalException
        {
            getFactory().addAnd( getBracket() );
            nextState();
        }
        
        public void eventOr() throws EvalException
        {
            getFactory().addOr( getBracket() );
            nextState();
        }
        
        public void eventQuestion()
        {
            getFactory().addQuestion( getBracket() );
            incIf();   // (
            nextState();
        }
        
        public void eventPoints() throws EvalException
        {
            decIf();  // )
            getFactory().addPoints( getBracket() );
            nextState();
        }
        
        public void eventCondition(char ch)
        {
            getFactory().addCondition( getBracket(),ch );
            nextState();
        }
        
        public void eventBracket1() throws EvalException
        {
            throw ExceptionFactory.unexpectedCharacter('(');
        }

        public void eventBracket2() throws EvalException
        {
            decBracket();
        }
    }
    
    private Dictionary Dic;
    private TreeFactory Factory;
    private int Bracket;
    private int If;
    private EvaluatorState CurrentState;
    private EvaluatorState States[];
    private PositionReader _PositionReader;
}

