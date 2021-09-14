
package ch.zeh.evaluator;

import java.io.*;

// abstracte Oberklasse fuer alle Tree-Klassen
// Klasse ist lokal zum package. D.h. ein anderes
// Package kann diese Klasse nicht verwenden.
abstract class TreeElement
{
    public TreeElement()
    {
    }

    /**
      *Stosst die Auswertung an. Falls ein Fehler auftritt (z.B
      *eine Variable ist nicht im Dictionary eingetragen, wird eine
      *Exception angestossen.
    */
    abstract double eval() throws NullPointerException,EvalException;

     /**
      *Druckt den Inhalt des Baums aus.
    */
    abstract void print(PrintStream w) throws NullPointerException;
    
    /**
      *Druckt den Inhalt des Baums aus. Dabei wird die Verschachtlungstiefe
      *durch ein Einruecken nach rechts angezeigt.
    */
    public void printTree(PrintStream w,int level) throws NullPointerException
    {
        // gib dem Level entsprechend 
        // waagrechte Striche aus.
        for (int i=0;i<level;i++)
        {
            w.print("-");
        }
        print(w);
    }
}