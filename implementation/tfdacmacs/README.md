# Installing jPBC native extension

## 1. Install PBC

Download the [sources](https://crypto.stanford.edu/pbc/download.html) of the latest pbc release and
follow the instructions in [here](https://crypto.stanford.edu/pbc/manual/ch01.html).
Requirement for this installation is the existance of [GMP](https://gmplib.org/). 
Since GMP is quite old, it might be compiled manually.

## 2. Installing jPBC

Download [jPBC](http://gas.dia.unisa.it/projects/jpbc/download.html)
from [here](https://sourceforge.net/projects/jpbc/files/jpbc_2_0_0/). 

After the download finished access the directory via the terminal and type:

```
mvn install -DskipTests
```

If it fails remove the model android from the pom.xml.

## 3. Setting up native extension

You need `make`, `cmake` and a c-compiler to do the next set:
Access the `./jpbc-pbc` directory and type:

```
cmake .
```

If this compiles successfully you correctly installed PBC in your system. Congrats.

After this is finished compile the wrapper:

```
make
```

You will find a shared library called `jpbc-pbc.so` in the `./build` or root directory.

## 4. Add dependencies to project

In your project add the following dependencies:

```groovy
	compile 'net.java.dev.jna:jna:5.1.0'
	compile 'it.unisa.dia.gas:jpbc-pbc:2.0.0'
```

You might also have to add:

```groovy
repositories {
	mavenCentral()
	mavenLocal() // <--
}
```

to make the manual installed libraries discoverable.

Finally, you have include the `libjbc-pbc.so` in your classpath. It can be done
by copying the library to your project resource directory: 

```
cp <YOUR_PATH>/JPBC/jpbc-pbc/build/jpbc-pbc.so <PROJECT_PATH>/src/main/resources/libjpbc-pbc.so
```

# Troubleshooting

Debug the method on line 12 in `WrapperLibraryInfo.java` to see the error message. 




 


