#!/bin/bash
# detailed_analysis_advanced.sh

WAREHOUSE="hdfs://GOLDEN/user/hive/warehouse"
OUTPUT_FILE="/tmp/hdfs_analiz_$(date +%Y%m%d_%H%M%S).txt"

# Renkli çıktı için (opsiyonel)
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Dosyaya yazdırma fonksiyonu (renksiz)
log() {
    echo "$1" >> "$OUTPUT_FILE"
}

# Ekrana yazdırma fonksiyonu (renkli)
display() {
    echo -e "$1"
}

# Her ikisine de yazdırma
log_and_display() {
    echo "$1" >> "$OUTPUT_FILE"
    echo -e "$2"
}

# Başlangıç
log "============================================"
log "DETAYLI ŞEMA VE TABLO ANALİZİ"
log "============================================"
log "Tarih: $(date '+%Y-%m-%d %H:%M:%S')"
log "Warehouse: $WAREHOUSE"
log ""

display "${GREEN}============================================${NC}"
display "${GREEN}DETAYLI ŞEMA VE TABLO ANALİZİ${NC}"
display "${GREEN}============================================${NC}"
display "Rapor: $OUTPUT_FILE"
display ""

# Şemaları listele
SCHEMAS=$(hdfs dfs -ls $WAREHOUSE | awk '{print $8}' | grep -v "^$")
SCHEMA_COUNT=$(echo "$SCHEMAS" | wc -l)

log "Toplam Şema Sayısı: $SCHEMA_COUNT"
display "Toplam Şema Sayısı: ${YELLOW}$SCHEMA_COUNT${NC}"
log ""
display ""

SCHEMA_NUM=0

for schema_path in $SCHEMAS; do
    schema=$(basename $schema_path)
    SCHEMA_NUM=$((SCHEMA_NUM + 1))
    
    log ""
    log "╔════════════════════════════════════════════════════════════════════════"
    log "║ ŞEMA: $schema [$SCHEMA_NUM/$SCHEMA_COUNT]"
    log "╚════════════════════════════════════════════════════════════════════════"
    
    display ""
    display "${YELLOW}╔════════════════════════════════════════════════════════════════════════${NC}"
    display "${YELLOW}║ ŞEMA: $schema [$SCHEMA_NUM/$SCHEMA_COUNT]${NC}"
    display "${YELLOW}╚════════════════════════════════════════════════════════════════════════${NC}"
    
    # Şema toplam boyutu
    schema_stats=$(hdfs dfs -count $schema_path)
    schema_size=$(echo $schema_stats | awk '{print $3}')
    schema_size_gb=$(echo $schema_size | awk '{printf "%.2f", $1/1024/1024/1024}')
    schema_files=$(echo $schema_stats | awk '{print $2}')
    
    log "   Toplam Boyut: $schema_size_gb GB"
    log "   Toplam Dosya: $schema_files"
    log ""
    
    display "   Toplam Boyut: ${GREEN}$schema_size_gb GB${NC}"
    display "   Toplam Dosya: $schema_files"
    display ""
    
    # Tablolar - geçici dosyaya kaydet
    TMP_TABLES="/tmp/tables_${schema}_$$.txt"
    > $TMP_TABLES
    
    TABLES=$(hdfs dfs -ls $schema_path 2>/dev/null | awk '{print $8}' | grep -v "^$")
    
    for table_path in $TABLES; do
        table=$(basename $table_path)
        
        # Tablo boyutu
        table_size=$(hdfs dfs -du -s $table_path 2>/dev/null | awk '{print $1}')
        
        if [ ! -z "$table_size" ] && [ "$table_size" -gt 0 ]; then
            echo "$table_size|$table" >> $TMP_TABLES
        fi
    done
    
    # Tablo sayısı
    TABLE_COUNT=$(wc -l < "$TMP_TABLES" 2>/dev/null || echo "0")
    
    # Başlık
    log "   Tablo Sayısı: $TABLE_COUNT"
    log ""
    log "   ┌─────────────────────────────────────────────────────────────────────────────"
    log "$(printf "   │ %-55s %20s %15s\n" "TABLO" "BOYUT" "YÜZDE")"
    log "   ├─────────────────────────────────────────────────────────────────────────────"
    
    display "   Tablo Sayısı: ${YELLOW}$TABLE_COUNT${NC}"
    display ""
    display "   ┌─────────────────────────────────────────────────────────────────────────────"
    printf "   │ %-55s %20s %15s\n" "TABLO" "BOYUT" "YÜZDE"
    display "   ├─────────────────────────────────────────────────────────────────────────────"
    
    # Sıralı şekilde yazdır (büyükten küçüğe)
    if [ -s "$TMP_TABLES" ]; then
        sort -t'|' -k1 -rn "$TMP_TABLES" | while IFS='|' read size table; do
            table_size_gb=$(echo $size | awk '{printf "%.2f", $1/1024/1024/1024}')
            
            # Yüzde hesapla
            if [ "$schema_size" -gt 0 ]; then
                percentage=$(echo $size $schema_size | awk '{printf "%.1f", ($1/$2)*100}')
            else
                percentage="0.0"
            fi
            
            # Dosyaya yaz
            log "$(printf "   │ %-55s %17s GB %13s %%\n" "$table" "$table_size_gb" "$percentage")"
            
            # Ekrana yaz (büyük tabloları vurgula)
            size_num=$(echo $table_size_gb | awk '{print $1}')
            if [ $(echo "$size_num > 100" | awk '{print ($1 > 100)}') -eq 1 ]; then
                printf "   │ ${RED}%-55s %17s GB %13s %%${NC}\n" "$table" "$table_size_gb" "$percentage"
            elif [ $(echo "$size_num > 10" | awk '{print ($1 > 10)}') -eq 1 ]; then
                printf "   │ ${YELLOW}%-55s %17s GB %13s %%${NC}\n" "$table" "$table_size_gb" "$percentage"
            else
                printf "   │ %-55s %17s GB %13s %%\n" "$table" "$table_size_gb" "$percentage"
            fi
        done
    else
        log "   │ (Tablo bulunamadı veya boş)"
        display "   │ ${RED}(Tablo bulunamadı veya boş)${NC}"
    fi
    
    log "   └─────────────────────────────────────────────────────────────────────────────"
    display "   └─────────────────────────────────────────────────────────────────────────────"
    
    # Geçici dosyayı temizle
    rm -f "$TMP_TABLES"
done

log ""
log "============================================"
log "ANALİZ TAMAMLANDI"
log "============================================"
log "Rapor Dosyası: $OUTPUT_FILE"

display ""
display "${GREEN}============================================${NC}"
display "${GREEN}ANALİZ TAMAMLANDI${NC}"
display "${GREEN}============================================${NC}"
display ""
display "✓ Rapor başarıyla oluşturuldu:"
display "  ${GREEN}$OUTPUT_FILE${NC}"
display ""
display "Raporu görüntülemek için:"
display "  ${YELLOW}cat $OUTPUT_FILE${NC}"
display "  veya"
display "  ${YELLOW}less $OUTPUT_FILE${NC}"