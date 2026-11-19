"""
KidCurate — Explicit Child PIN Bcrypt Migration CLI

This script safely scans the database for any legacy plaintext 4-digit PINs
and hashes them using bcrypt ($2b$).

Safety rules:
- Default is DRY RUN. Does not mutate the database unless --apply is passed.
- Requires operator confirmation before mutating records.
- Zero PIN values are ever logged, printed, or saved to disk.
- Idempotent: Can be run multiple times safely.
"""

import asyncio
import argparse
import sys
import os

# Ensure backend directory is in path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.database import AsyncSessionLocal
from app.routes.children import remediate_plaintext_pins


async def main():
    parser = argparse.ArgumentParser(description="KidCurate Safe PIN Remediation Tool")
    parser.add_argument(
        "--apply",
        action="store_true",
        help="Execute the migration and commit changes to the database. Without this flag, a dry run is performed.",
    )
    args = parser.parse_args()

    dry_run = not args.apply

    print("==================================================")
    print(" KidCurate Child PIN Cryptographic Remediation")
    print(f" Mode: {'DRY RUN (Read-Only)' if dry_run else 'APPLY (Database Mutation)'}")
    print("==================================================")

    if not dry_run:
        confirm = input("Are you sure you want to commit bcrypt hashes to the database? (type 'yes' to proceed): ")
        if confirm.strip().lower() != "yes":
            print("Aborted by user.")
            sys.exit(0)

    async with AsyncSessionLocal() as session:
        stats = await remediate_plaintext_pins(session, dry_run=dry_run)

    print("\nScan Results:")
    print(f"  Total records scanned:     {stats['total_scanned']}")
    print(f"  Already bcrypt hashed:     {stats['already_hashed']}")
    print(f"  Eligible for upgrade:      {stats['eligible_for_upgrade']}")
    print(f"  Successfully upgraded:     {stats['upgraded']}")
    print(f"  Invalid / unparseable:     {stats['invalid_or_empty']}")

    if dry_run and stats['eligible_for_upgrade'] > 0:
        print(f"\n[NOTICE] {stats['eligible_for_upgrade']} record(s) can be upgraded. To execute, run with: python backend/scripts/migrate_pins.py --apply")
    elif not dry_run:
        print(f"\n[SUCCESS] {stats['upgraded']} record(s) successfully upgraded to bcrypt.")
    else:
        print("\n[OK] All child PINs are already securely hashed. No migration needed.")


if __name__ == "__main__":
    asyncio.run(main())
