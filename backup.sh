#!/bin/sh
# Backup diário do banco. Uso: ./backup.sh  (agende no cron)
mkdir -p backups
docker compose -f docker-compose.prod.yml exec -T db pg_dump -U leads leads | gzip > "backups/leads-$(date +%F).sql.gz"
find backups -name 'leads-*.sql.gz' -mtime +14 -delete
