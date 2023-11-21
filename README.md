# clojure-api

## Description

---
This is a Clojure project titled "clojure-api".
This is created to understand the working of a stateful clojure API.
It has dependencies on various libraries _viz~_ aero, pedestal, slf4j, and component. 
This README provides instructions on how to set up and run the project.

## Prerequisites

---
1. **JAVA**: You should have Java installed. Clojure runs on JVM(Java Virtual Machine) hence Java is mandatory.
2. **Clojure**: This project is implemented in Clojure hence you need to install Clojure and Clojure CLI tool
3. **Leiningen**: Leiningen is a tool which makes it easy to use Clojure 
## Installation

---
1. ### Installing Java
    If you haven't installed Java then checkout [this link.](https://www.java.com/en/download/help/download_options.html)
2. ### Installing Clojure CLI
    If you haven't installed Clojure CLI then checkout [this link.](https://clojure.org/guides/install_clojure)
3. ### Installing Leiningen
    If you haven't installed Leiningen then checkout [this link](https://leiningen.org/).
## Clone the Project 

---
You can clone this project to your local machine:
```
git clone https://github.com/inukahbhsekum/clojure-api.git
cd clojure-api
```

## Running the project

---
1. Go to the project directory
   ```
   cd clojure-api
   ```
2. Compile the code
   ```
    lein do clean, deps, compile
   ```
3. Start the REPL
   ```
   lein repl
   ```
4. Start the server
   ```
   (require 'dev)
   (in-ns 'dev)
   (component-repl/reset)
   ```

## Conclusion
By now everything should be up and running.
Explore the code, make changes.
If you encounter any issues, please refer to the documentation for the libraries used, search on web for more guidance, or raise issue in this repository.