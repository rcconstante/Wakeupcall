import sqlite3

print("🔧 Migrating database schema...")
conn = sqlite3.connect('wakeup_call.db')
cursor = conn.cursor()

# Add missing columns
try:
    cursor.execute("ALTER TABLE user_surveys ADD COLUMN daily_steps INTEGER DEFAULT 5000")
    print("✅ Added daily_steps column")
except sqlite3.OperationalError as e:
    print(f"⚠️ daily_steps column: {e}")

try:
    cursor.execute("ALTER TABLE user_surveys ADD COLUMN average_daily_steps INTEGER DEFAULT 5000")
    print("✅ Added average_daily_steps column")
except sqlite3.OperationalError as e:
    print(f"⚠️ average_daily_steps column: {e}")

try:
    cursor.execute("ALTER TABLE user_surveys ADD COLUMN sleep_duration_hours REAL DEFAULT 7.0")
    print("✅ Added sleep_duration_hours column")
except sqlite3.OperationalError as e:
    print(f"⚠️ sleep_duration_hours column: {e}")

try:
    cursor.execute("ALTER TABLE user_surveys ADD COLUMN weekly_steps_json TEXT DEFAULT '{}'")
    print("✅ Added weekly_steps_json column")
except sqlite3.OperationalError as e:
    print(f"⚠️ weekly_steps_json column: {e}")

try:
    cursor.execute("ALTER TABLE user_surveys ADD COLUMN weekly_sleep_json TEXT DEFAULT '{}'")
    print("✅ Added weekly_sleep_json column")
except sqlite3.OperationalError as e:
    print(f"⚠️ weekly_sleep_json column: {e}")

conn.commit()
conn.close()

print("\n✅ Database migration complete!")

# Verify
conn = sqlite3.connect('wakeup_call.db')
cursor = conn.cursor()
cursor.execute("PRAGMA table_info(user_surveys)")
cols = cursor.fetchall()
print("\nFinal columns in user_surveys:")
for col in cols:
    print(f"  {col[1]} ({col[2]})")
conn.close()
