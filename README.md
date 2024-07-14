# nanoproxy
This is a proxy for the nano node written in java.

Requirments:

Gradle 7.2
Java 11.0.12

script to install with dependencies: 

```bash
#!/bin/bash

#Clone Repo
git clone https://github.com/blockracer/nanoproxy

# Install SDKMAN! if not already installed
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install Java 11.0.12
sdk install java 11.0.12-zulu

# Install Gradle 7.2
sdk install gradle 7.2

# Verify installations
echo "Java version:"
java -version
echo ""
echo "Gradle version:"
gradle -version
```
Edit Main.java with your proxy requirments. Check comments in file to understand how to edit.

How to run proxy:

In the project directory run gradle build, a new directory will be made named build. In the build directory go to libs, the jar file will be located there.

How to run
```bash
java -jar proxy-1-all.jar
```
To run the proxy using systemd so it runs as a background process create a file in /etc/systemd/system and choose a name e.g. proxy.service the service extension name must be present though.

Here is an example of how I set the file:

```bash
[Service]
ExecStart=/home/username/.sdkman/candidates/java/11.0.12-zulu/bin/java -jar /path/to/proxy-1-all.jar
User=username
Type=simple
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```
Now you can run the proxy like this:

```bash
sudo service [servicename] start

```















