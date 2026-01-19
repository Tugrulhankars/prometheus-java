# 1. Aşama: Derleme (Build)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Bağımlılıkları önbelleğe almak için önce sadece pom.xml
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Kaynak kodları kopyala ve paketle
COPY src ./src
RUN mvn clean package -DskipTests

# 2. Aşama: Çalıştırma (Runtime)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Güvenlik için kullanıcı oluştur
RUN addgroup -S spring && adduser -S spring -G spring

# Sadece gerekli jar dosyasını kopyala (Spring Boot executable jar)
COPY --from=build /app/target/spring-boot-prometheus-app-*.jar app.jar

# Dosya sahipliğini spring kullanıcısına ver
RUN chown spring:spring app.jar

# Kullanıcıyı değiştir
USER spring:spring

# Uygulamanın dinlediği port (Spring ayarınla eşleşmeli)
EXPOSE 8081

# Health check (curl Alpine'de varsayılan olarak gelir)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Uygulamayı çalıştır
ENTRYPOINT ["java", "-jar", "app.jar"]