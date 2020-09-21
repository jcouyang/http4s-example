FROM openjdk:8-jre-slim

ADD ./http4s-example .

CMD ./http4s-example -zipkin.initialSampleRate=1