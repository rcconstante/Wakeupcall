"""
Fix risk_level in database to match OSA probability from ML model
This corrects any surveys where risk_level doesn't match the actual OSA probability
"""

import sqlite3

DATABASE = 'wakeup_call.db'

def fix_risk_levels():
    """Update risk_level based on osa_probability"""
    conn = sqlite3.connect(DATABASE)
    cursor = conn.cursor()
    
    # Get all surveys
    cursor.execute('SELECT id, user_id, osa_probability, risk_level FROM user_surveys')
    surveys = cursor.fetchall()
    
    print(f"🔍 Checking {len(surveys)} surveys...")
    fixed_count = 0
    
    for survey in surveys:
        survey_id, user_id, osa_prob, current_risk = survey
        
        # Determine correct risk level based on OSA probability
        if osa_prob < 0.3:
            correct_risk = "Low Risk"
        elif osa_prob < 0.6:
            correct_risk = "Moderate Risk"
        else:
            correct_risk = "High Risk"
        
        # Update if incorrect
        if current_risk != correct_risk:
            print(f"  ❌ Survey {survey_id} (User {user_id}): OSA={osa_prob:.3f} but risk_level='{current_risk}'")
            print(f"     Fixing to: '{correct_risk}'")
            
            cursor.execute('''
                UPDATE user_surveys 
                SET risk_level = ? 
                WHERE id = ?
            ''', (correct_risk, survey_id))
            
            fixed_count += 1
        else:
            print(f"  ✅ Survey {survey_id} (User {user_id}): OSA={osa_prob:.3f}, risk_level='{current_risk}' (Correct)")
    
    conn.commit()
    conn.close()
    
    print(f"\n✅ Fixed {fixed_count} surveys")
    print(f"   {len(surveys) - fixed_count} surveys were already correct")

if __name__ == '__main__':
    print("🚀 Fixing risk levels based on OSA probability...")
    fix_risk_levels()
    print("\n✨ Done!")
