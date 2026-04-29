# Overview
This minimal skeleton provides an example of using **Vaadin Swing Bridge** technology that runs existing Swing applications within a **Vaadin** to make them accessible for the end users through the web in no time.

This project uses maven for managing the dependencies and configurations.
Similar to any other Vaadin projects, you can run it from commandline `mvn clean spring-boot:run`.
For further instructions of building for productions, please refer to the [Production Build](#production-build) section.

For making the initial setup straightforward, Vaadin Swing Bridge runtime assumes some configurations are in place. 
These configurations are mostly about where the Swing application's jar file(s) and dependencies are located. 
Please refer to the [Important Directories](#important-directories) to make sure you have a smooth experience. :+1:

## Important Directories:

### applibs
This is the directory that is automatically being scanned for the jar files of the Swing App. 
Swing application packaging models can differ from exporting one fat jar, to having a light main jar plus its dependencies right next to it, or loaded at runtime in some way.
In any case, all the jar files should be in this directory so that they are available to the Swing Bridge runtime.

> **_NOTE:_**  The `applibs` name of this directory is important and case-sensitive.
If you wish to change it, you can pass the path to such a directory that contains all the jar files by passing `-Dapplibs.dir` from commandline.
It is also possible to configure this in the `pom.xml`. There is a commented placeholder for this in the `<systemPropertyVariables>` section of the `pom.xml`.

### swing-bridge-repo
This is the local maven repository for necessary libraries which are not publicly available in any repositories.

## How to Run
The `pom.xml` is configured with necessary command-line arguments and configurations, so you can run it in development mode simply by running:
```
mvn clean spring-boot:run
```

## Production Build
Also, similar to any spring-boot Vaadin application, it is possible to build it using:
```
mvn clean package
```

and the result is a Spring-Boot fat jar that can be run with the commands below.
The `<MAVEN_REPO>` placeholder refers to your local Maven repository path:
- **Linux/macOS**: `$HOME/.m2/repository` (do **not** use `~` as it won't expand inside JVM flags like `-Xbootclasspath/a:`)
- **Windows**: `C:\Users\<your-username>\.m2\repository`

If you have a custom repository location, you can find the exact path by running:
```
mvn help:evaluate -Dexpression=settings.localRepository -q -DforceStdout
```

**Linux/macOS:**
```bash
java \
  --patch-module java.desktop=<MAVEN_REPO>/com/vaadin/swing-bridge-patch/1.0.0/swing-bridge-patch-1.0.0.jar \
  -Xbootclasspath/a:<MAVEN_REPO>/com/vaadin/swing-bridge-graphics/1.0.0/swing-bridge-graphics-1.0.0.jar \
  --add-reads java.desktop=ALL-UNNAMED \
  --add-exports=java.desktop/sun.font=ALL-UNNAMED \
  --add-exports=java.desktop/sun.awt=ALL-UNNAMED \
  --add-exports=java.desktop/sun.awt.dnd=ALL-UNNAMED \
  --add-exports=java.desktop/sun.awt.dnd.peer=ALL-UNNAMED \
  --add-exports=java.base/sun.nio.cs=ALL-UNNAMED \
  --add-exports=java.desktop/sun.java2d=ALL-UNNAMED \
  --add-exports=java.desktop/sun.java2d.pipe=ALL-UNNAMED \
  --add-exports=java.desktop/sun.awt.datatransfer=ALL-UNNAMED \
  --add-exports=java.desktop/sun.awt.image=ALL-UNNAMED \
  --add-exports=java.desktop/java.awt.peer=ALL-UNNAMED \
  --add-exports=java.desktop/java.awt.dnd=ALL-UNNAMED \
  --add-exports=java.desktop/java.awt.dnd.peer=ALL-UNNAMED \
  --add-exports=java.desktop/sun.print=ALL-UNNAMED \
  --add-exports=java.desktop/sun.swing=ALL-UNNAMED \
  --add-opens=java.desktop/java.awt.event=ALL-UNNAMED \
  --add-opens=java.desktop/sun.awt=ALL-UNNAMED \
  -Djava.awt.headless=false \
  -Dapplibs.dir=./applibs \
  -jar target/skeleton-starter-vaadin-swing-bridge-1.0-SNAPSHOT.jar
```

**Windows (Command Prompt):**
```cmd
java ^
  --patch-module java.desktop=<MAVEN_REPO>\com\vaadin\swing-bridge-patch\1.0.0\swing-bridge-patch-1.0.0.jar ^
  -Xbootclasspath/a:<MAVEN_REPO>\com\vaadin\swing-bridge-graphics\1.0.0\swing-bridge-graphics-1.0.0.jar ^
  --add-reads java.desktop=ALL-UNNAMED ^
  --add-exports=java.desktop/sun.font=ALL-UNNAMED ^
  --add-exports=java.desktop/sun.awt=ALL-UNNAMED ^
  --add-exports=java.desktop/sun.awt.dnd=ALL-UNNAMED ^
  --add-exports=java.desktop/sun.awt.dnd.peer=ALL-UNNAMED ^
  --add-exports=java.base/sun.nio.cs=ALL-UNNAMED ^
  --add-exports=java.desktop/sun.java2d=ALL-UNNAMED ^
  --add-exports=java.desktop/sun.java2d.pipe=ALL-UNNAMED ^
  --add-exports=java.desktop/sun.awt.datatransfer=ALL-UNNAMED ^
  --add-exports=java.desktop/sun.awt.image=ALL-UNNAMED ^
  --add-exports=java.desktop/java.awt.peer=ALL-UNNAMED ^
  --add-exports=java.desktop/java.awt.dnd=ALL-UNNAMED ^
  --add-exports=java.desktop/java.awt.dnd.peer=ALL-UNNAMED ^
  --add-exports=java.desktop/sun.print=ALL-UNNAMED ^
  --add-exports=java.desktop/sun.swing=ALL-UNNAMED ^
  --add-opens=java.desktop/java.awt.event=ALL-UNNAMED ^
  --add-opens=java.desktop/sun.awt=ALL-UNNAMED ^
  -Djava.awt.headless=false ^
  -Dapplibs.dir=.\applibs ^
  -jar target\skeleton-starter-vaadin-swing-bridge-1.0-SNAPSHOT.jar
```
