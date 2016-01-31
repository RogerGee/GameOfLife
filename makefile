################################################################################
# makefile for GameOfLife
################################################################################

# options
CLASSDIR = GameOfLife
SOURCEDIR = src
COMPILE = javac -d $(CLASSDIR)
CLASSPATH = /usr/local/lib/lwjgl/jar/lwjgl.jar:$(CLASSDIR)

# output: class files must be listed in build order so those with
# dependencies are evaluated after the dependencies have been built
MAINCLASS = GameOfLife
CLASSES = World Window GameOfLife
CLASSFILES = $(addsuffix .class,$(addprefix $(CLASSDIR)/,$(CLASSES)))

# rules
all: $(CLASSDIR) $(CLASSFILES)

$(CLASSFILES): $(CLASSDIR)/%.class: $(SOURCEDIR)/%.java
	CLASSPATH=$(CLASSPATH) $(COMPILE) $<

$(CLASSDIR):
	mkdir $(CLASSDIR)
