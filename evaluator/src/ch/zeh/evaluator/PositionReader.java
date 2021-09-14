
package ch.zeh.evaluator;

import java.io.*;

/**
  *Die Klasse PositionReader zaehlt die gelesenen Zeichen. Sie wird zwischen
  *zwei Reader eingefuegt. getPosition liefert jederzeit die aktuelle Position.
  *PositionReader wird in Evaluator verwendet, um im Fehlerfall die Position bestimmen
  *zu koennen, wo der Fehler auftrat.
  *
  *@see Evaluator
*/
class PositionReader extends FilterReader
{
    /**
       *erzeugt einen Positionreader, welcher von r
       * liest. 
    */
    public PositionReader(Reader r)
    {
        super(r);
        Position=0;
    }

    /** 
     *ueberschriebene Methode um Anzahl gelesene
     *Zeichen lesen zu koennen.
    */
    public int read() throws IOException
    {
         Position++;
         return super.read();
    }

     /** 
     *ueberschriebene Methode um Anzahl gelesene
     *Zeichen lesen zu koennen.
    */
   public int read(char cbuf[],int off,int len) throws IOException
    {
        Position+=len;
        return super.read(cbuf,off,len);
    }

    /**
    *liefert die aktuelle Position zurueck
    *@return aktuelle Position des Lesevorgangs
    */
    public int getPosition()
    {
        return Position;
    }

    int Position;
}