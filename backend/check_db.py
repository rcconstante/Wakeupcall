import sqlite3

conn = sqlite3.connect('wakeup_call.db')
cursor = conn.cursor()
cursor.execute("PRAGMA table_info(user_surveys)")
cols = cursor.fetchall()
print("Current columns in user_surveys:")
for col in cols:
    print(f"  {col[1]} ({col[2]})")
conn.close()
