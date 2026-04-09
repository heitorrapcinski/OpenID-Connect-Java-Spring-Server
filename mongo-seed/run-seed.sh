#!/bin/bash
# Executa os scripts de seed MongoDB nos containers corretos.
# Idempotente: pode ser rodado múltiplas vezes sem duplicar dados.
# Uso: bash mongo-seed/run-seed.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "=== MongoDB Seed ==="

echo ""
echo "→ [1/3] Seeding scope_db (system_scopes)..."
docker exec -i mongodb-scope mongosh --quiet \
  < "$SCRIPT_DIR/01-seed-scopes.js"

echo ""
echo "→ [2/3] Seeding client_db (clients)..."
docker exec -i mongodb-client mongosh --quiet \
  < "$SCRIPT_DIR/02-seed-clients.js"

echo ""
echo "→ [3/3] Seeding oidc_db (user_info)..."
docker exec -i mongodb-oidc mongosh --quiet \
  < "$SCRIPT_DIR/03-seed-users.js"

echo ""
echo "=== Seed concluído com sucesso ==="
