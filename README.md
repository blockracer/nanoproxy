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
Edit Main.java with your proxy requirments. Check comments on file for more information.






