DICT ?= sample_dict.txt
DEBUG ?=

run: compile
ifdef DEBUG
	jdb Solution $(DICT)
else
	java Solution $(DICT)
endif

compile: clean
ifdef DEBUG
	javac -g *.java
else
	javac *.java
endif

clean:
	rm -f *.class

