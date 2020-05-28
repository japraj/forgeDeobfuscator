# forgeDeobfuscator

A simple script that automatically maps [deobfuscates] decompiled Minecraft Forge mods; works on all platforms.

![](https://i.imgur.com/T2mMiYD.png)

### Install instructions:

Download the forgeDeobfuscator.jar file from the repository. If your browser automatically cancels the download due to a false positive security flag, you should download and extract the forgeDeobfuscator.zip file; both are exactly the same, but with different file formats.

### Optional Steps for Windows Users:

- You can download the runDeobfuscator.bat file to streamline the process of launching the program. All the file does is launch command prompt for you and run the .jar file.
- If you do this, you can just double-click the batch file instead of following the run instructions below; the batch file must be located in the same directory as the .jar file.

### Running the program:

- Open up a Command Line Interface/Terminal/Shell
- Run the following command: `java -jar PATH/forgeDeobfuscator.jar` where PATH is the path for the .jar file; example for a Windows user named Steve who has the file on their desktop: `java -jar C:/Users/Steve/Desktop/forgeDeobfuscator.jar`
- Enter a command to be executed (input the name of the command).

### Issues/bugs:

- If you get an error along the lines of "java is not a valid command" when trying to run the script with the commands described above, you should install Java. If the issue persists, your PATH/System variables are probably not set properly; you can find a tutorial online describing how to fix that.
- If have any other issues (such as the script failing to deobfuscate something), please create a new issue via Github.
- Before making an issue, run `java -v` to ensure that the error is not related to your JRE installation/System variables.
