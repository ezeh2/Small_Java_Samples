
package ch.zeh.evaluator;

import java.util.*;
import java.io.*;

/**
    Die TreeFactory baut den Syntaxbaum auf. Es muessen
    einfach die entsprechenden add-methoden aufgerufen
    werden.
    
    Nachdem alle Elemente uebergeben wurde, kann mit getTree der
    Baum abgeholt werden.

    Klasse ist lokal zum Package, d.h. sie kann
    ausserhalb des Packages nicht aufgerufen werden.
*/
class TreeFactory
{
    // Testcode Testcode 
    public static void main(String [] args)
    {
        try
        {
        // 1. (2+3)*5
        
        TreeFactory factory1=new TreeFactory(null);
        factory1.addNumber(2.0);
        factory1.addPlus(1);
        factory1.addNumber(3.0);
        factory1.addStar(0);
        factory1.addNumber(5.0);
        
        factory1.getTree().print(System.out);
        System.out.println();
        
        System.out.println(factory1.getTree().eval());
        
        // 2. 2+3*5
        
        TreeFactory factory2=new TreeFactory(null);
        factory2.addNumber(2.0);
        factory2.addPlus(0);
        factory2.addNumber(3.0);
        factory2.addStar(0);
        factory2.addNumber(5.0);
        
        factory2.getTree().print(System.out);
        System.out.println();
        
        System.out.println(factory2.getTree().eval());
        
        // 3. 2+3*5+7+9*(2*(1+1)+1)
        
        TreeFactory factory3=new TreeFactory(null);
        factory3.addNumber(2.0);
        factory3.addPlus(0);
        factory3.addNumber(3.0);
        factory3.addStar(0);
        factory3.addNumber(5.0);
        factory3.addPlus(0);
        factory3.addNumber(7.0);
        factory3.addPlus(0);
        factory3.addNumber(9.0);
        factory3.addStar(0);
        factory3.addNumber(2.0);
        factory3.addStar(1);
        factory3.addNumber(1.0);
        factory3.addPlus(2);
        factory3.addNumber(1.0);
        // factory3.addPlus(1);
        // factory3.addNumber(1.0);
        
        factory3.getTree().printTree(System.out,0);
        System.out.println();
        
        System.out.println(factory3.getTree().eval());
        }
        catch (EvalException ex)
        {
            System.out.println(ex.getMessage());
        }
        
        try
        {
            int i=System.in.read();
        }
        catch (Exception e)
        {
        }
        
    }
    
    /** Instanziert Factory. Es muss ihr ein Dictionary
        uebergeben werden, damit beim Auswerten auf die
        Variablen zugegriffen werden koennen.
    */
    public TreeFactory(Dictionary dic)
    {
        Dic=dic;
    }
    
    public void addPlus(int brackets)
    {
        addNonLeaf(new TreePlusElement(brackets));
    }
    
    public void addMinus(int brackets)
    {
        addNonLeaf(new TreeMinusElement(brackets));
    }
    
    public void addStar(int brackets)
    {
        addNonLeaf(new TreeStarElement(brackets));
    }
    
    public void addSlash(int brackets)
    {
        addNonLeaf(new TreeSlashElement(brackets));
    }
    
    public void addAnd(int brackets)
    {
        addNonLeaf(new TreeAndElement(brackets));
    }
    
    public void addOr(int brackets)
    {
        addNonLeaf(new TreeOrElement(brackets));
    }
    
    public void addQuestion(int brackets)
    {
        addNonLeaf(new TreeQuestionElement(brackets));
    }
    
    public void addPoints(int brackets) throws EvalException
    {
        // fuege ein : ein
        addNonLeaf(new TreePointsElement(brackets));
            
        // pruefe ob links vom eingefuegten Element ein '?'
        // ist. Links von ':' muss immer ein '?' sein.
        if ( !(Last.getLeft() instanceof TreeQuestionElement) )
        {
            // Nein, es ist kein '?', wirf eine Exception !
            throw ExceptionFactory.missingCharacter('?');
        }
    }
    
    // wird fuer erstes Auftreten von <, > oder =
    // aufgerufen.
    public void addCondition(int brackets,char ch)
    {
        addNonLeaf(new TreeConditionElement(brackets,ch));
    }
    
    // Wird ab dem zweiten Auftreten von <, > oder =
    // aufgerufen
    public void setCondition(char ch) throws EvalException
    {
        // ist aktuelles Wurzelelement eine Instanz von
        // TreeConditionElement ? Nur dann kann nämlich die
        // condition erweitert werden.
        if ( (Root!=null) && (Root instanceof TreeConditionElement) )
        {
            ((TreeConditionElement)Root).setCondition(ch);
        }
        else
        {
            throw ExceptionFactory.unexpectedCharacter(ch);
        }
    }
    
    public void addNumber(double number)
    {
        addLeaf(new TreeNumberElement(number));
    }
    
    /**  Fuegt eine Variable hinzu. Diese muss im
         Dictionary definert sein.
    */
    public void addKey(String key)
    {
        addLeaf(new TreeKeyElement(key,Dic));
    }
    
    private void addNonLeaf(TreeNonLeafElement nonLeaf)
    {
        // Wir iterieren solange nach rechts, bis wir entweder 
        // das Ende des Baumes erreichen oder die Prioritaet
        // des einzufuegendes Knotens kleiner ist als die des
        // aktuellen Knotens.
        TreeElement prev=null;
        TreeElement next=Root;
        while (  (next instanceof TreeNonLeafElement) && (nonLeaf.getPriority()>((TreeNonLeafElement)next).getPriority())  )
        {
                    prev=next;
                    next=((TreeNonLeafElement)next).getRight();
        }

        nonLeaf.setLeft(next);
        if (prev!=null)
        {
            ((TreeNonLeafElement)prev).setRight(nonLeaf);
        }
        else 
        {
            Root=nonLeaf;
        }
        Last=nonLeaf;
    }
    
    private void addLeaf(TreeElement element)
    {
        // Ist dies der erste Aufruf fuer diesen Baum ?
        if (Root==null)
        {
            // ja !!
            Root=element;
        }
        else
        {
            // Ist beim letzten Knoten rechts
            // noch Platz frei ?
            if (  (Last!=null) && (Last.getRight()==null)  )
            {
                // Ja !! Deshalb einfuegen !
                Last.setRight(element);
                
            }
            else
            {
                // Nein, kein Platz mehr frei --> Fehler !!!
            }
        }
    }
    
    public TreeElement getTree()
    {
        return Root;
    }
    
    // Referenz auf das Wurzelelement
    TreeElement Root;
    
    // Das letzte NonLeaf-Element, das in den Baum eingefuegt wurde.
    TreeNonLeafElement Last;
    
    // enthaelt fuer jeden Key, einen Ausdruck
    // Es wird von der Klasse TreeKeyElement
    // verwendet um Variabeln aufzuloesen.
    Dictionary Dic;
}

// Für jedes Zeichen gibt es eine Unterklasse von
// TreeElement. Jedes Zeichen besitzt eine Wertigkeit.
class ElementValues
{
    // hoechste Wertigkeit
    static final public int Top=10;      // Multiplikator fuer 
                                         // Klammern. Ein '+' ohne
                                         // Klammern hat W. 1. Ist aber
                                         // (+) mit Klammern umschlossen
                                         // hat es W. 11, ist es von zwei Klammern
                                         // umschlossen hat es sogar 12.
    
    static final public int Condition=7; // <, >, =
    
    static final public int And=6;       // &
    static final public int Or=5;        // |
    
    static final public int Question=4;  // ?
    static final public int Points=3;    // :
    
    static final public int Star=2;      // *
    static final public int Slash=2;     // /

    static final public int Plus=1;      // +
    static final public int Minus=1;     // -
    // kleinste Wertigkeit
}

abstract class TreeNonLeafElement extends TreeElement
{
    public TreeNonLeafElement(int brackets)
    {
        Brackets=brackets;
    }
    
    public void setLeft(TreeElement l)
    {
        Left=l;
    }
    
    public void setRight(TreeElement r)
    {
        Right=r;
    }
    
    public TreeElement getLeft()
    {
        return Left;
    }
    
    public TreeElement getRight()
    {
        return Right;
    }
    
    public void printTree(PrintStream w,int level)
    {
        for (int i=0;i<level;i++)
        {
            w.print("-");
        }
        w.println(getSymbol());
        getLeft().printTree(w,level+3);
        getRight().printTree(w,level+3);
    }
    
    public void print(PrintStream w)
    {
        w.println( getSymbol() );
        
        if ( getLeft()!=null )
        {
            getLeft().print(w);
        }
        else
        {
            w.print("null ");
        }
        if ( getRight()!=null )
        {
            getRight().print(w);
        }
        else
        {
            w.print("null ");
        }
    }
    
    public int getBrackets()
    {
        return Brackets*ElementValues.Top;
    }
    
    public abstract int getPriority();
    
    protected abstract char getSymbol();
    
    int Brackets;
    
    TreeElement Left;
    TreeElement Right;
}

class TreePlusElement extends TreeNonLeafElement
{
    public TreePlusElement(int brackets)
    {
        super(brackets);
    }
    
    public double eval() throws EvalException
    {
        return ( getLeft().eval() + getRight().eval() );
    }

    public int getPriority()
    {
        return getBrackets()+ElementValues.Plus;
    }
    

    protected char getSymbol()
    {
        return '+';
    }
}

class TreeMinusElement extends TreeNonLeafElement
{
    public TreeMinusElement(int brackets)
    {
        super(brackets);
    }
    
    public double eval() throws EvalException
    {
        return ( getLeft().eval() - getRight().eval() );
    }

    public int getPriority()
    {
        return getBrackets()+ElementValues.Minus;
    }
    
    protected char getSymbol()
    {
        return '-';
    }
}

class TreeStarElement extends TreeNonLeafElement
{
    public TreeStarElement(int brackets)
    {
        super(brackets);
    }
    
    public double eval() throws EvalException
    {
        return ( getLeft().eval() * getRight().eval() );
    }

    public int getPriority()
    {
        return getBrackets()+ElementValues.Star;
    }
    
    protected char getSymbol()
    {
        return '*';
    }
}

class TreeSlashElement extends TreeNonLeafElement
{
    public TreeSlashElement(int brackets)
    {
        super(brackets);
    }
    
    public double eval() throws EvalException
    {
        return ( getLeft().eval() / getRight().eval() );
    }

    public int getPriority()
    {
        return getBrackets()+ElementValues.Slash;
    }
    
    protected char getSymbol()
    {
        return '/';
    }
}

class TreeAndElement extends TreeNonLeafElement
{
    public TreeAndElement(int brackets)
    {
        super(brackets);
    }
    
    public double eval() throws EvalException
    {
        if ( ((getLeft().eval())!=0) && ((getRight().eval())!=0) )
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    public int getPriority()
    {
        return getBrackets()+ElementValues.And;
    }
    
    protected char getSymbol()
    {
        return '&';
    }
}

class TreeOrElement extends TreeNonLeafElement
{
    public TreeOrElement(int brackets)
    {
        super(brackets);
    }
    
    public double eval() throws EvalException
    {
        if ( (getLeft().eval()!=0) || (getRight().eval()!=0) )
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    public int getPriority()
    {
        return getBrackets()+ElementValues.Or;
    }
    
    protected char getSymbol()
    {
        return '|';
    }
}



class TreeQuestionElement extends TreeNonLeafElement
{
    public TreeQuestionElement(int brackets)
    {
        super(brackets);
    }
    
    public double eval() throws EvalException
    {
        return getLeft().eval();
    }
    
    public int getPriority()
    {
        return getBrackets()+ElementValues.Question;
    }
    
    protected char getSymbol()
    {
        return '?';
    }
}

class TreePointsElement extends TreeNonLeafElement
{
    public TreePointsElement(int brackets)
    {
        super(brackets);
    }
    
    public double eval() throws EvalException
    {
        if (getLeft().eval()!=0)
        {
            return getTrueValue();
        }
        else
        {
            return getFalseValue();
        }
    }
    
    public int getPriority()
    {
        return getBrackets()+ElementValues.Points;
    }
    
    protected char getSymbol()
    {
        return ':';
    }
    
    private double getTrueValue()  throws EvalException
    {
        return ((TreeNonLeafElement)getLeft()).getRight().eval();
    }


    private double getFalseValue() throws EvalException
    {
        return getRight().eval();
    }

}

class TreeConditionElement extends TreeNonLeafElement
{
    public TreeConditionElement(int brackets,char ch)
    {
        super(brackets);
        More=false;
        Less=false;
        Equal=false;
        
        setVar(ch);
    }
    
    public void setCondition(char ch)
    {
        setVar(ch);  
    }
    
    public double eval() throws EvalException
    {
        boolean ret=false;
        if (More)
        {
            ret|=( getLeftValue() > getRightValue() );
        }
        if (Less)
        {
            ret|=( getLeftValue() < getRightValue() );
        }
        if (Equal)
        {
            ret|=( getLeftValue() == getRightValue() );
        }
        if (ret)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }
    
    public int getPriority()
    {
        return getBrackets()+ElementValues.Condition;
    }
    
    protected char getSymbol()
    {
        return '=';
    }
    
    private double getLeftValue() throws EvalException
    {
        return getLeft().eval();
    }
    
    private double getRightValue() throws EvalException
    {
        return getRight().eval();
    }
    
    private void setVar(char ch)
    {
        switch (ch)
        {
            case '>':
            {
                More=true;
                break;
            }
            case '<':
            {
                Less=true;
                break;
            }
            case '=':
            {
                Equal=true;
                break;
            }
            default:
            {
                // fehler
            }
        }
    }
    
    private boolean More;
    private boolean Less;
    private boolean Equal;
 }


class TreeNumberElement extends TreeElement
{
    public TreeNumberElement(double number)
    {
        Number=number;
    }
    
    public void print(PrintStream w)
    {
        w.println(Number);
    }
    
    public double eval()
    {
        return Number;
    }
    
    double Number;
}

class TreeKeyElement extends TreeElement
{
    public TreeKeyElement(String key,Dictionary dic)
    {
        Key=key;
        Dic=dic;
    }
    
    public void print(PrintStream w)
    {
        w.println(0);
    }
        
    public double eval() throws EvalException
    {
        // Ist gar kein Dictionary vorhanden ?
        if (Dic==null)
        {
            // Ok, es ist keines da --> das wars !
            throw ExceptionFactory.unknownKey(Key);
        }
        
        // Ist der Schluessel in der Dictionary
        // drin ?
        Object value=Dic.get(Key);
        if (value==null)
        {
            // Der Schluessel ist nicht vorhanden !
            throw ExceptionFactory.unknownKey(Key);
        }
        else if (value instanceof AlreadyInUse)
        {
            // Der Schluessel wurde schon gebraucht, d.h
            // wir sind in einer Schleife drin !
            throw ExceptionFactory.alreadyUsedKey(Key);
        }
        
        // Nun wissen, dass es ein Dictionary gibt und
        // das der Schluessel vorhanden ist.
        
        // Wir ersetzen den Wert des Schluessels durch
        // die Dummy-Instanz AlreadyInUse.
        Dic.put(Key,AlreadyInUse.getInstance());
        
        // Der returnwert
        double ret=0;
        
        try
        {
            if (value instanceof String)
            {
                Evaluator e=new Evaluator(Dic);
                ret=e.eval((String)value);
            } 
            else if (value instanceof TreeElement)
            {
                ret=((TreeElement)value).eval();
            }
        }
        catch (EvalException ex)
        {
            throw ExceptionFactory.errorInKey(ex,Key);
        }
        finally
        {
            // Wir seten den Schluessel wieder in den 
            // Originalzustand.
            Dic.put(Key,value);
        }
        
        return ret;
    }
    
    String Key;
    Dictionary Dic;
    
    // Markierungsklasse
    static class AlreadyInUse
    {
        // Diese Klasse kann man nicht instanzieren
        private AlreadyInUse()
        {
        }
        
        // liefet die einzige Instanz dieser Klasse
        static public AlreadyInUse getInstance()
        {
            if (Instance==null)
            {
                Instance=new AlreadyInUse();
            }
            return Instance;
        }
        
        static private AlreadyInUse Instance;
    }
}





