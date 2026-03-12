Claude İçin Bu Projenin Bilgilendirmesi (projenin ismi KASIRGA):
1- Bu proje Cloudera Manager benzeri bir araç olarak; on-prem yaklaşık 150+ fiziksel sunucu üzerinde açık kaynak olarak koşturduğumuz bazı toolların yönetimi ve monitoring'i için hazırlanmıştır.
2- Takip edilebilen araçlar: Trino, Spark, Kafka, Kafka connect, Apache Doris, Apache HDFS, pure storage
3- Kasırga, centos 8 üzerinde çalışır. Backend ve frontend componentlerinden oluşur. Backend de fastapi, frontend de react kullanır. Db layer olarak mysql kullanır. Kurulu olduğu sunucu üzerinden passwordless ssh üzerinden diğer hostlarla iletişimde olur. Bazen de atlama sunucusu kullanır.
4- Deploy süreci: Hedef sunucu kapalı networkte (air-gapped). Geliştirme döngüsü bittiğinde backend/, frontend/ ve frontend-dist/ dizinleri olduğu gibi sunucuya taşınır. Sunucuda npm/node yoktur; frontend önceden build edilmiş (frontend-dist/) haliyle deploy edilir. Bu yüzden frontend-dist/ dizini repo'da tutulur ve her frontend değişikliğinde yeniden build edilmelidir.
5- Repo'daki topology.yaml ve cluster_manager.cfg dummy/örnek veridir, gerçek sunucu bilgilerini içermez.
