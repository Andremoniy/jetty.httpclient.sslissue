FROM maven:3.6.1-jdk-11
COPY . /usr/src
WORKDIR /usr/src
RUN mvn clean test