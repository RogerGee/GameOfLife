################################################################################
# makefile for GameOfLife
################################################################################

# options
CLASSDIR = GameOfLife
SOURCEDIR = src
COMPILE = javac -d $(CLASSDIR)
CLASSPATH = /usr/local/lib/lwjgl/jar/lwjgl.jar:$(CLASSDIR)

MAINCLASS = GameOfLife
CLASSES = World Window GameOfLife
CLASSFILES = $(addsuffix .class,$(addprefix $(CLASSDIR)/,$(CLASSES)))

# rules
all: $(CLASSDIR) $(CLASSFILES)

$(CLASSFILES): $(CLASSDIR)/%.class: $(SOURCEDIR)/%.java
	CLASSPATH=$(CLASSPATH) $(COMPILE) $(addsuffix .java,$(addprefix $(SOURCEDIR)/,$(CLASSES)))

$(CLASSDIR):
	mkdir $(CLASSDIR)
