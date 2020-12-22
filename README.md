# Oversecured Plugin for Jenkins

A SAST Android vulnerability scanner. Searches for vulnerabilities in [90+ categories](https://oversecured.com/vulnerabilities) using Oversecured API.

## Getting started

1. Active an Integration and create an API Key on [oversecured.com](https://oversecured.com/)
2. Build the plugin `mvn clean install`
3. Add it to the plugins list `cp target/oversecured.hpi ~/.jenkins/plugins/`
4. Add the plugin to your pipeline ![Pipeline](https://github.com/oversecured/oversecured-jenkins-plugin/blob/master/images/1.png)
5. Specify your Oversecured Integration ID and output APK file location ![Config](https://github.com/oversecured/oversecured-jenkins-plugin/blob/master/images/2.png)
6. Add your Oversecured API Key to Jenkins Credentials ![Jenkins Credentials](https://github.com/oversecured/oversecured-jenkins-plugin/blob/master/images/3.png)
7. In your project configuration, bind the created secret value to the `apiKey` variable ![Secret key binding](https://github.com/oversecured/oversecured-jenkins-plugin/blob/master/images/4.png)
6. Now it's ready to use! ![Output](https://github.com/oversecured/oversecured-jenkins-plugin/blob/master/images/5.png)


## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

