# APT-Project-Aldinucci


[![Build Status](https://travis-ci.com/Predictabowl/APT-Project-Aldinucci-Fork.svg?branch=master)](https://travis-ci.com/Predictabowl/APT-Project-Aldinucci-Fork)
[![Coverage Status](https://coveralls.io/repos/github/Predictabowl/APT-Project-Aldinucci-Fork/badge.svg?branch=master)](https://coveralls.io/github/Predictabowl/APT-Project-Aldinucci-Fork?branch=master)


##Build
To launch the full test suit, from the root folder, the command is:

./mvnw -f bookstore-parent/pom.xml clean verify -Pjacoco,e2e-test,mutation-test