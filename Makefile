DEBUG ?=

run: compile
ifdef DEBUG
	jdb Solution sample_dict.txt
else
	java Solution sample_dict.txt
endif

compile: clean
ifdef DEBUG
	javac -g *.java
else
	javac *.java
endif

clean:
	rm -f *.class

