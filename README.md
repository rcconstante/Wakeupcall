
ESS:
The results are as follows:

0 to 5: Low daytime sleepiness (normal).
6 to 10: High daytime sleepiness (normal).
11 to 12: Mild excessive daytime sleepiness.
13 to 15: Moderate excessive daytime sleepiness.
16 to 24: Severe excessive daytime sleepiness.

Berlin:
Category 1: items 2, 3, 4, 5, and 6;
Item 2: if ‘Yes’, assign 1 point
Item 3: if either of the last two options is the response, assign 1 point
Item 4: if either of the first two options is the response, assign 1 point
Item 5: if ‘Yes’ is the response, assign 1 point
Item 6: if either of the first two options is the response, assign 2 points
Add points. Category 1 is positive if the total score is 2 or more points.
Category 2: items 7, 8, and 9.
Item 7: if either of the first two options is the response, assign 1 point
Item 8: if either of the first two options is the response, assign 1 point
Item 9: if ‘Yes’ is the response, assign 1 point
Add points. Category 2 is positive if the total score is 2 or more points.
Category 3 is positive if the answer to item 10 is ‘Yes’ or if the BMI of the patient is greater
than 30kg/m2. (BMI is defined as weight (kg) divided by height (m) squared, i.e.., kg/m2).
High Risk: if there are 2 or more categories where the score is positive.
Low Risk: if there is only 1 or no categories where the score is positive
Additional Question: item 9 should be noted separately. 

STOP BANG:
For general population
OSA - Low Risk : Yes to 0 - 2 questions
OSA - Intermediate Risk : Yes to 3 - 4 questions
OSA - High Risk : Yes to 5 - 8 questions
or Yes to 2 or more of 4 STOP questions + male gender
or Yes to 2 or more of 4 STOP questions + BMI > 35kg/m2
or Yes to 2 or more of 4 STOP questions + neck circumference 16 inches / 40cm



SAMPLE DATA FOR MACHINE LEARNING INPUT
input_data = {
    'Age': 45,
    'Sex': 1,
    'BMI': 31.5,
    'Neck_Circumference': 42,
    'Hypertension': 1,
    'Diabetes': 0,
    'Smokes': 0,
    'Alcohol': 1,
    'Snoring': 1,
    'Sleepiness': 1,
    'Epworth_Score': 14,
    'Berlin_Score': 1,
    'STOPBANG_Total': 5,
    'SleepQuality_Proxy_0to10': 4,
    'Sleep_Duration': 6.5,
    'Physical_Activity_Level': 2,
    'Daily_Steps': 8000
OUTPUT:
}Predicted Probability of OSA: 1.00
Predicted Class: 1 (High Risk)