import sqlite3
import json
import os

db_path = os.path.join(os.path.dirname(__file__), "..", "backend", "kidcurate.db")
conn = sqlite3.connect(db_path)
conn.row_factory = sqlite3.Row
cursor = conn.cursor()

cursor.execute("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%';")
tables = [row["name"] for row in cursor.fetchall()]

print("=" * 60)
print(f"DATABASE FILE: {os.path.abspath(db_path)}")
print(f"FILE SIZE: {os.path.getsize(db_path):,} bytes")
wal_path = db_path + "-wal"
if os.path.exists(wal_path):
    print(f"WAL SIZE: {os.path.getsize(wal_path):,} bytes")
print("=" * 60)

for table in tables:
    cursor.execute(f"PRAGMA table_info('{table}');")
    cols = cursor.fetchall()
    col_names = [c["name"] for c in cols]
    
    cursor.execute(f"SELECT COUNT(*) as count FROM '{table}';")
    count = cursor.fetchone()["count"]
    
    print(f"\nTABLE: {table} (Total Rows: {count})")
    print("Schema Columns:")
    for c in cols:
        pk = " [PRIMARY KEY]" if c["pk"] else ""
        notnull = " NOT NULL" if c["notnull"] else ""
        print(f"  - {c['name']}: {c['type']}{notnull}{pk}")
    
    if count > 0:
        cursor.execute(f"SELECT * FROM '{table}' LIMIT 10;")
        rows = cursor.fetchall()
        print(f"Data Preview (up to 10 rows):")
        for i, row in enumerate(rows, 1):
            row_dict = {}
            for k in col_names:
                val = row[k]
                if k == "password_hash" and val:
                    val = val[:15] + "..." + val[-10:]  # mask hash
                elif isinstance(val, str) and len(val) > 70:
                    val = val[:67] + "..."
                row_dict[k] = val
            print(f"  Row {i}: {json.dumps(row_dict, default=str)}")
    else:
        print("  (Table is currently empty)")

conn.close()
