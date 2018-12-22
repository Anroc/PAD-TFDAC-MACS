#!/bin/bash

pdflatex multi-authority-abe.tex
bibtex multi-authority-abe
pdflatex multi-authority-abe.tex
pdflatex multi-authority-abe.tex
