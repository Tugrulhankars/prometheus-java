# Spring Boot Prometheus Application

Sektör standartlarına uygun Spring Boot uygulaması - Prometheus metrikleri, Grafana dashboard'ları ve Alertmanager entegrasyonu ile Kubernetes ortamında çalışacak şekilde yapılandırılmıştır.

## Özellikler

- ✅ Spring Boot 3.2.0 ile Micrometer metrik üretimi
- ✅ Prometheus metrik export (Actuator endpoint)
- ✅ Kubernetes ServiceMonitor (Prometheus Operator desteği)
- ✅ PrometheusRule yapılandırması (alerting kuralları)
- ✅ Alertmanager entegrasyonu (Mail ve Slack bildirimleri)
- ✅ Grafana dashboard örneği
- ✅ Production-ready yapılandırma

## Proje Yapısı

```
spring-boot-prometheus-app/
├── src/
│   └── main/
│       ├── java/com/example/prometheusapp/
│       │   ├── PrometheusApplication.java
│       │   ├── controller/
│       │   │   └── MetricsController.java
│       │   ├── service/
│       │   │   └── BusinessMetricsService.java
│       │   └── config/
│       │       └── MetricsConfig.java
│       └── resources/
│           └── application.yml
├── k8s/
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── service-monitor.yaml
│   ├── prometheus-rule.yaml
│   └── alertmanager-config.yaml
├── config/
│   ├── alertmanager.yaml
│   └── grafana-dashboard.json
├── Dockerfile
├── pom.xml
└── README.md
```

## Hızlı Başlangıç

### 1. Uygulamayı Çalıştırma

```bash
# Maven ile build
mvn clean package

# Uygulamayı çalıştır
java -jar target/spring-boot-prometheus-app-1.0.0.jar
```

### 2. Metrikleri Kontrol Etme

Uygulama çalıştıktan sonra metrikler şu endpoint'te mevcut:

```bash
# Prometheus formatında metrikler
curl http://localhost:8080/actuator/prometheus

# Health check
curl http://localhost:8080/actuator/health

# Tüm metrikler
curl http://localhost:8080/actuator/metrics
```

### 3. Kubernetes'e Deployment

#### Gereksinimler
- Kubernetes cluster (1.19+)
- Prometheus Operator kurulu
- kubectl yapılandırılmış

#### Adımlar

```bash
# Namespace oluştur (eğer yoksa)
kubectl create namespace default

# Docker image build
docker build -t spring-boot-prometheus-app:1.0.0 .

# Image'ı registry'e push edin (veya local registry kullanın)
# docker push your-registry/spring-boot-prometheus-app:1.0.0

# Deployment
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/service-monitor.yaml
kubectl apply -f k8s/prometheus-rule.yaml
```

#### ServiceMonitor'ı güncelleme

`k8s/service-monitor.yaml` dosyasında Prometheus Operator'ün `release` label'ına göre güncelleyin:

```yaml
labels:
  app: spring-boot-prometheus-app
  release: prometheus  # Prometheus Operator kurulumunuza göre güncelleyin
```

### 4. Alertmanager Yapılandırması

#### Mail Yapılandırması (Gmail Örneği)

1. Gmail App Password oluşturun: https://myaccount.google.com/apppasswords
2. `config/alertmanager.yaml` dosyasını düzenleyin:

```yaml
global:
  smtp_smarthost: 'smtp.gmail.com:587'
  smtp_from: 'alertmanager@yourdomain.com'
  smtp_auth_username: 'your-email@gmail.com'
  smtp_auth_password: 'your-app-password'
```

#### Slack Yapılandırması

1. Slack webhook URL oluşturun: https://api.slack.com/messaging/webhooks
2. `config/alertmanager.yaml` dosyasını güncelleyin:

```yaml
global:
  slack_api_url: 'https://hooks.slack.com/services/YOUR/WEBHOOK/URL'
```

#### Kubernetes'te Alertmanager Config

```bash
# Secret oluştur (Mail ve Slack credentials için)
kubectl create secret generic alertmanager-secret \
  --from-literal=smtp-password='your-app-password' \
  --from-literal=slack-webhook-url='https://hooks.slack.com/services/YOUR/WEBHOOK/URL' \
  --namespace=monitoring

# AlertmanagerConfig uygula (Prometheus Operator kullanıyorsanız)
kubectl apply -f k8s/alertmanager-config.yaml
```

### 5. Grafana Dashboard Import

1. Grafana'ya giriş yapın
2. Dashboards > Import seçeneğine gidin
3. `config/grafana-dashboard.json` dosyasını import edin
4. Prometheus datasource'u seçin

## Alerting Kuralları

Uygulama aşağıdaki alertleri tanımlar:

### Critical Alerts

- **HighErrorRate**: Hata oranı %5'i aşarsa
- **ApplicationDown**: Uygulama 2 dakikadan fazla down ise
- **ServiceAvailabilityLow**: Servis erişilebilirliği %95'in altına düşerse

### Warning Alerts

- **SlowResponseTime**: 95. percentile yanıt süresi 1 saniyeyi aşarsa
- **HighMemoryUsage**: Heap memory kullanımı %85'i aşarsa
- **HighCPUUsage**: CPU kullanımı %80'i aşarsa
- **ConnectionPoolExhausted**: Connection pool %90'ı aşarsa
- **LongGarbageCollection**: GC pause süresi yüksekse

### Info Alerts

- **HighRequestRate**: İstek oranı çok yüksekse
- **LowProcessedItems**: İşlenen item sayısı düşükse

## Metrikler

Uygulama aşağıdaki metrikleri üretir:

### HTTP Metrikleri
- `http_server_requests_seconds_count` - Toplam HTTP istek sayısı
- `http_server_requests_seconds_bucket` - HTTP yanıt süresi histogramı
- `http_server_requests_seconds_sum` - Toplam yanıt süresi

### JVM Metrikleri
- `jvm_memory_used_bytes` - JVM memory kullanımı
- `jvm_memory_max_bytes` - JVM maksimum memory
- `jvm_gc_pause_seconds` - GC pause süreleri
- `process_cpu_usage` - CPU kullanımı

### Business Metrikleri
- `business_events_total` - İş olayları sayacı
- `business_items_processed` - İşlenen item sayısı
- `business_users_active` - Aktif kullanıcı sayısı
- `business_queue_size` - Kuyruk boyutu

### Custom API Metrikleri
- `api.requests.total` - API istek sayısı
- `api.request.duration` - API yanıt süresi
- `api.errors.total` - API hata sayısı

## API Endpoints

```
GET  /api/health          - Health check
GET  /api/data            - Veri çekme (metrik üretir)
POST /api/process         - Veri işleme (metrik üretir)
GET  /api/slow            - Yavaş endpoint (alert test için)
GET  /actuator/prometheus - Prometheus metrikleri
GET  /actuator/health     - Health status
GET  /actuator/metrics    - Tüm metrikler listesi
```

## Production Önerileri

1. **Resource Limits**: `k8s/deployment.yaml` içinde resource limitleri ayarlayın
2. **Replicas**: Production için en az 2 replica kullanın
3. **Liveness/Readiness Probes**: Health check endpoint'leri yapılandırılmıştır
4. **Monitoring**: Prometheus scraping interval'ini optimize edin
5. **Alerting**: Alert threshold'larını iş yükünüze göre ayarlayın
6. **Security**: RBAC, network policies, secrets management uygulayın
7. **Logging**: Centralized logging (ELK/Loki) entegrasyonu ekleyin

## Troubleshooting

### Metrikler görünmüyor

```bash
# ServiceMonitor'ın label'larını kontrol et
kubectl get servicemonitor -n default

# Prometheus'un target'ları scrape edip etmediğini kontrol et
# Prometheus UI'da Status > Targets sayfasını kontrol edin

# Pod loglarını kontrol et
kubectl logs -l app=spring-boot-prometheus-app
```

### Alertler tetiklenmiyor

```bash
# PrometheusRule'ın yüklendiğini kontrol et
kubectl get prometheusrule -n default

# Alertmanager config'i kontrol et
kubectl get secret alertmanager-config -n monitoring -o yaml

# Alertmanager loglarını kontrol et
kubectl logs -l app=alertmanager -n monitoring
```

## Lisans

Bu proje eğitim amaçlı oluşturulmuştur.

## İletişim

Sorularınız için issue açabilirsiniz.

