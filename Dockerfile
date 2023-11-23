FROM openjdk:17
ADD build/libs/tutoring_calendar-0.0.1-SNAPSHOT.jar tutoring_calendar_backend-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "tutoring_calendar_backend-0.0.1-SNAPSHOT.jar"]