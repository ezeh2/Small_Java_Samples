@echo off

@echo "compiling..."
cd src\ch\zeh\evaluator
javac *.java
cd ..\..\..\..
@echo "compiling done"

@echo ""
@echo ""
@echo ""

@echo "executing Evaluator"
@echo ""
@echo "enter math expressions like"
@echo "enter 12+12<CR>"
@echo "enter 2*2+2-9<CR>"
cd src
java ch.zeh.evaluator.Evaluator

pause


