"""
Migration script to add all missing questionnaire fields to user_surveys table
Addresses Issue: Questions from survey not contributing to calculation/model
Date: 2025-11-19
"""

import sqlite3

DATABASE = 'wakeup_call.db'

def migrate_database():
    """Add all missing questionnaire fields to user_surveys table"""
    conn = sqlite3.connect(DATABASE)
    cursor = conn.cursor()
    
    print("🔍 Checking current schema...")
    
    # Get current columns
    cursor.execute("PRAGMA table_info(user_surveys)")
    existing_columns = {row[1] for row in cursor.fetchall()}
    print(f"   Existing columns: {len(existing_columns)}")
    
    # Define all new columns to add
    new_columns = [
        # Snoring details
        ("snoring_level", "TEXT"),  # "Mild", "Moderate", "Loud", "Very Loud"
        ("snoring_frequency", "TEXT"),  # "Rarely", "Occasionally", "Frequently", "Always"
        ("snoring_bothers_others", "INTEGER", 0),  # Boolean: has snoring bothered others
        
        # Sleep quality and duration
        ("sleep_quality_rating", "INTEGER"),  # 0-10 scale
        
        # Fatigue and tiredness
        ("tired_during_day", "TEXT"),  # "Never", "Rarely", "Sometimes", "Often", "Always"
        ("tired_after_sleep", "TEXT"),  # "Never", "Rarely", "Sometimes", "Often", "Always"
        ("feels_sleepy_daytime", "INTEGER", 0),  # Boolean
        
        # Driving and alertness
        ("nodded_off_driving", "INTEGER", 0),  # Boolean: ever nodded off while driving
        
        # Physical activity timing
        ("physical_activity_time", "TEXT"),  # "Morning", "Afternoon", "Evening", "Night"
        
        # Individual ESS scores (for detailed analysis)
        ("ess_sitting_reading", "INTEGER"),
        ("ess_watching_tv", "INTEGER"),
        ("ess_public_sitting", "INTEGER"),
        ("ess_passenger_car", "INTEGER"),
        ("ess_lying_down_afternoon", "INTEGER"),
        ("ess_talking", "INTEGER"),
        ("ess_after_lunch", "INTEGER"),
        ("ess_traffic_stop", "INTEGER"),
    ]
    
    # Add columns if they don't exist
    added_count = 0
    for column_def in new_columns:
        column_name = column_def[0]
        column_type = column_def[1]
        default_value = column_def[2] if len(column_def) > 2 else None
        
        if column_name not in existing_columns:
            try:
                if default_value is not None:
                    cursor.execute(f'''
                        ALTER TABLE user_surveys 
                        ADD COLUMN {column_name} {column_type} DEFAULT {default_value}
                    ''')
                else:
                    cursor.execute(f'''
                        ALTER TABLE user_surveys 
                        ADD COLUMN {column_name} {column_type}
                    ''')
                print(f"   ✅ Added column: {column_name} ({column_type})")
                added_count += 1
            except sqlite3.OperationalError as e:
                print(f"   ⚠️ Column {column_name} already exists or error: {e}")
        else:
            print(f"   ℹ️  Column {column_name} already exists")
    
    conn.commit()
    conn.close()
    
    print(f"\n✅ Migration complete! Added {added_count} new columns.")
    print(f"   Total columns now in user_surveys table: {len(existing_columns) + added_count}")
    
    # Verify migration
    print("\n🔍 Verifying migration...")
    conn = sqlite3.connect(DATABASE)
    cursor = conn.cursor()
    cursor.execute("PRAGMA table_info(user_surveys)")
    all_columns = cursor.fetchall()
    conn.close()
    
    print(f"   Final column count: {len(all_columns)}")
    print("\n📋 All columns in user_surveys:")
    for col in all_columns:
        print(f"      {col[1]} ({col[2]})")

if __name__ == '__main__':
    print("🚀 Starting database migration...")
    migrate_database()
    print("\n✨ Done!")
