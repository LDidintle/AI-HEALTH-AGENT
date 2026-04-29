FROM tomcat:9.0-jdk8-temurin AS build

WORKDIR /workspace

COPY ["AI HEALTH AGENT/src/java", "src/java"]
COPY ["AI HEALTH AGENT/web", "web"]

RUN mkdir -p build/classes build/war/WEB-INF/classes \
    && find src/java -name "*.java" | sort > sources.txt \
    && javac -encoding UTF-8 -source 1.8 -target 1.8 \
        -cp "$CATALINA_HOME/lib/servlet-api.jar:$CATALINA_HOME/lib/jsp-api.jar:web/WEB-INF/lib/*" \
        -d build/classes @sources.txt \
    && cp -R web/* build/war/ \
    && cp -R build/classes/* build/war/WEB-INF/classes/ \
    && cd build/war \
    && jar -cf /workspace/smarthealth.war .

FROM tomcat:9.0-jre8-temurin

ENV CATALINA_OPTS="-Djava.security.egd=file:/dev/./urandom"

RUN rm -rf "$CATALINA_HOME/webapps"/*

COPY --from=build /workspace/smarthealth.war "$CATALINA_HOME/webapps/ROOT.war"
COPY docker/entrypoint.sh /usr/local/bin/smarthealth-entrypoint.sh

RUN chmod +x /usr/local/bin/smarthealth-entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["smarthealth-entrypoint.sh"]
CMD ["catalina.sh", "run"]
