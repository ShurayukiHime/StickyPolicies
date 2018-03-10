# This is a *very simple* server
### answering the Android app.

It is powered by **Apache Tomcat**.

It includes a parser which can interpret Sticky Policies following the predefined XSD grammar.

The idea is to answer clients following Mont2003towards specification.

## Structure of a Sticky Policy
Following the specification, a Sticky Policy always refer to a single encrypted file. It contains:
- a list of trusted authorities which can validate it and are held accountable by the data owner for data disclosure.
- some details about the data owner
- one or more policies.

Each inner policy defines:
- a list of targets, i.e. entities allowed to access the data
- the specification of the data type
- the time validity of the policy
- one or more constraints which are to be checked by the trust authority before authorizing access
- one or more actions which are to be taken by the trust authority before authorizing access, e.g. notify owner.

## Internal structure
This server includes two servlets for the following purposes:
- Certificates handling: data owners' certificates are received and stored for future use, and the TA's certificate is sent to clients to encrypt data.
- Data access: decryption and verification of signatures when asked to disclose data; check policy correctness and compliance.