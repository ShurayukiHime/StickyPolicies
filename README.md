# Sticky Policies App
### An Android app for managing personal data disclosure among third parties.

This project provides a working implementation to the ideas presented in [this paper](https://pdfs.semanticscholar.org/67ff/b0d21c529e99e8c19edb2de7879e6323b549.pdf).

Privacy constraints are expressed through an xml file (with a fixed grammar) and privacy is ensured through a combination of asymmetric and symmetric cryptography. In this implementation, personal data is shared encrypted across Android devices; to obtain data access, a client must ask a trusted web server to evaluate its policy compliance - if the client proves to be reliable, the web server grants him access to personal data.

Developed with Java JDK 1.8.0_91, Apache Tomcat 8.0.35 and tested on a Nexus 5X API 24 (Android 7.0, CPU x86_64).