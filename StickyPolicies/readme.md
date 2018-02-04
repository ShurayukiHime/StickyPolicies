## This is an Android app
### and a real mess.

It connects to a Facebook profile (successfully) and tries to display the user's photos (not so successfully).
This because a first idea for this project was to download pics from a social media and try to share them with some custom privacy setting.

This path was then abandoned towards another idea: a centralized server with some policy for each user. In this scenario,  an Android client connected to the server to register / send some new policy, specified in xml format (following a predefined grammar) - this is why there is a parser and some things like that. In this way, personal data was shared "encrypted", and users could access it only if the centralized, trusted server gave them the permission to.
After some time, the direction of this project changed again towards a different way of sharing personal data.

Currently, I am looking for a working IBE implementation so to allow encrypted content to flow between Android devices; a centralized server should exists, but only to issue the private key - which is not our main interest right now.
According to some paper (tang2008using, most probably), an intuitive implementation of fine-grained policies could be obtaining through IBE, using as an encryption key the attributes that the receiver should have (also time conditions). These attributes could be coded through "well-known" strings.