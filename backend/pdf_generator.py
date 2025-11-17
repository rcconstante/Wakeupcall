from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, Image
from reportlab.lib import colors
from reportlab.lib.enums import TA_LEFT, TA_CENTER
from typing import Dict
from io import BytesIO
import matplotlib.pyplot as plt
import matplotlib
matplotlib.use('Agg')

class WakeUpCallPDFGenerator:
    """
    Generate Sleep Apnea Report PDF with consistent layout/design
    Only values change, design stays the same
    """
    
    def __init__(self):
        self.styles = getSampleStyleSheet()
        self._setup_custom_styles()
    
    def _setup_custom_styles(self):
        """Define custom styles for the report"""
        # Title style
        self.styles.add(ParagraphStyle(
            name='ReportTitle',
            parent=self.styles['Heading1'],
            fontSize=16,
            textColor=colors.HexColor('#1a1a1a'),
            spaceAfter=6,
            alignment=TA_CENTER
        ))
        
        # Subtitle style
        self.styles.add(ParagraphStyle(
            name='Subtitle',
            parent=self.styles['Normal'],
            fontSize=10,
            textColor=colors.HexColor('#666666'),
            spaceAfter=20
        ))
        
        # Section header style
        self.styles.add(ParagraphStyle(
            name='SectionHeader',
            parent=self.styles['Heading2'],
            fontSize=14,
            textColor=colors.HexColor('#2c3e50'),
            spaceAfter=10,
            spaceBefore=15,
            fontName='Helvetica-Bold'
        ))
        
        # Assessment result style (highlighted)
        self.styles.add(ParagraphStyle(
            name='HighRisk',
            parent=self.styles['Normal'],
            fontSize=12,
            textColor=colors.HexColor('#c0392b'),
            fontName='Helvetica-Bold'
        ))
    
    def generate_pdf(self, data: Dict, output_path: str = None) -> BytesIO:
        """
        Generate PDF report with fixed design, dynamic values
        
        Args:
            data: Dictionary containing all report data
            output_path: Optional file path to save PDF. If None, returns BytesIO
            
        Returns:
            BytesIO object containing the PDF
        """
        # Create PDF buffer
        buffer = BytesIO()
        doc = SimpleDocTemplate(
            buffer if output_path is None else output_path,
            pagesize=letter,
            rightMargin=72,
            leftMargin=72,
            topMargin=50,
            bottomMargin=50
        )
        
        # Container for PDF elements
        story = []
        
        # HEADER
        story.append(Paragraph(
            "Sleep Apnea Risk Assessment – Detailed Report by<br/>WakeUpCall",
            self.styles['ReportTitle']
        ))
        
        story.append(Paragraph(
            "This detailed report includes patient information, STOP-BANG scoring, Epworth Sleepiness Scale, risk<br/>"
            "assessment, lifestyle factors, medical history, and SHAP model explanation for physician review.",
            self.styles['Subtitle']
        ))
        
        # ASSESSMENT RESULT SECTION
        story.append(Paragraph("Assessment Result", self.styles['SectionHeader']))
        
        assessment = data.get('assessment', {})
        risk_level = assessment.get('risk_level', 'N/A')
        
        assessment_data = [
            ['Predicted Risk Level:', risk_level.upper()],
            ['OSA Probability:', f"{assessment.get('osa_probability', 0)}%"],
            ['Recommendation:', assessment.get('recommendation', 'N/A')]
        ]
        
        assessment_table = Table(assessment_data, colWidths=[2*inch, 4*inch])
        assessment_table.setStyle(TableStyle([
            ('ALIGN', (0, 0), (0, -1), 'LEFT'),
            ('ALIGN', (1, 0), (1, -1), 'LEFT'),
            ('FONTNAME', (0, 0), (0, -1), 'Helvetica-Bold'),
            ('FONTNAME', (1, 0), (1, 0), 'Helvetica-Bold'),
            ('FONTSIZE', (0, 0), (-1, -1), 10),
            ('TEXTCOLOR', (1, 0), (1, 0), colors.HexColor('#c0392b') if 'HIGH' in risk_level.upper() else colors.black),
            ('BOTTOMPADDING', (0, 0), (-1, -1), 6),
            ('GRID', (0, 0), (-1, -1), 0.5, colors.grey),
            ('BOX', (0, 0), (-1, -1), 1, colors.black),
        ]))
        
        story.append(assessment_table)
        story.append(Spacer(1, 0.2*inch))
        
        # PATIENT INFORMATION SECTION
        story.append(Paragraph("Patient Information", self.styles['SectionHeader']))
        
        patient = data.get('patient', {})
        patient_data = [
            ['Name:', patient.get('name', '')],
            ['Age:', str(patient.get('age', ''))],
            ['Sex:', patient.get('sex', '')],
            ['Height:', patient.get('height', '')],
            ['Weight:', patient.get('weight', '')],
            ['BMI:', str(patient.get('bmi', ''))],
            ['Neck Circumference:', patient.get('neck_circumference', '')]
        ]
        
        patient_table = Table(patient_data, colWidths=[2*inch, 4*inch])
        patient_table.setStyle(TableStyle([
            ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
            ('FONTNAME', (0, 0), (0, -1), 'Helvetica-Bold'),
            ('FONTSIZE', (0, 0), (-1, -1), 9),
            ('BOTTOMPADDING', (0, 0), (-1, -1), 4),
            ('GRID', (0, 0), (-1, -1), 0.5, colors.grey),
            ('BOX', (0, 0), (-1, -1), 1, colors.black),
        ]))
        
        story.append(patient_table)
        story.append(Spacer(1, 0.2*inch))
        
        # STOP-BANG ASSESSMENT SECTION
        story.append(Paragraph("STOP-BANG Assessment", self.styles['SectionHeader']))
        
        stop_bang = data.get('stop_bang', {})
        stop_bang_score = stop_bang.get('score', 0)
        
        if stop_bang_score >= 5:
            risk_text = "High Risk"
        elif stop_bang_score >= 3:
            risk_text = "Intermediate Risk"
        else:
            risk_text = "Low Risk"
        
        stop_bang_data = [
            ['Snoring', 'Yes' if stop_bang.get('snoring') else 'No'],
            ['Tiredness', 'Yes' if stop_bang.get('tiredness') else 'No'],
            ['Observed Apnea', 'Yes' if stop_bang.get('observed_apnea') else 'No'],
            ['High Blood Pressure', 'Yes' if stop_bang.get('high_blood_pressure') else 'No'],
            ['BMI > 35', 'Yes' if stop_bang.get('bmi_over_35') else 'No'],
            ['Age > 50', 'Yes' if stop_bang.get('age_over_50') else 'No'],
            ['Neck ≥ 40 cm', 'Yes' if stop_bang.get('neck_circumference_large') else 'No'],
            ['Gender Male', 'Yes' if stop_bang.get('gender_male') else 'No'],
            ['Total Score', f"{stop_bang_score}/8 ({risk_text})"]
        ]
        
        stop_bang_table = Table(stop_bang_data, colWidths=[2.5*inch, 3.5*inch])
        stop_bang_table.setStyle(TableStyle([
            ('ALIGN', (0, 0), (0, -1), 'LEFT'),
            ('ALIGN', (1, 0), (1, -1), 'CENTER'),
            ('FONTNAME', (0, 0), (0, -1), 'Helvetica-Bold'),
            ('FONTNAME', (0, -1), (-1, -1), 'Helvetica-Bold'),
            ('FONTSIZE', (0, 0), (-1, -1), 9),
            ('BOTTOMPADDING', (0, 0), (-1, -1), 4),
            ('GRID', (0, 0), (-1, -2), 0.5, colors.grey),
            ('LINEABOVE', (0, -1), (-1, -1), 1, colors.black),
        ]))
        
        story.append(stop_bang_table)
        story.append(Spacer(1, 0.2*inch))
        
        # EPWORTH SLEEPINESS SCALE SECTION
        story.append(Paragraph("Epworth Sleepiness Scale (ESS)", self.styles['SectionHeader']))
        
        ess = data.get('epworth_sleepiness_scale', {})
        ess_total = ess.get('total_score', 0)
        
        if ess_total > 10:
            ess_interpretation = "Excessive Daytime Sleepiness"
        elif ess_total > 6:
            ess_interpretation = "Higher Normal Daytime Sleepiness"
        else:
            ess_interpretation = "Normal Daytime Sleepiness"
        
        ess_data = [
            ['Sitting and reading', str(ess.get('sitting_reading', 0))],
            ['Watching TV', str(ess.get('watching_tv', 0))],
            ['Public place sitting', str(ess.get('public_sitting', 0))],
            ['Passenger in car', str(ess.get('passenger_car', 0))],
            ['Lying down PM', str(ess.get('lying_down_pm', 0))],
            ['Talking', str(ess.get('talking', 0))],
            ['After lunch', str(ess.get('after_lunch', 0))],
            ['Traffic stop', str(ess.get('traffic_stop', 0))],
            ['Total ESS Score', f"{ess_total}/24 ({ess_interpretation})"]
        ]
        
        ess_table = Table(ess_data, colWidths=[2.5*inch, 3.5*inch])
        ess_table.setStyle(TableStyle([
            ('ALIGN', (0, 0), (0, -1), 'LEFT'),
            ('ALIGN', (1, 0), (1, -1), 'LEFT'),
            ('FONTNAME', (0, 0), (0, -1), 'Helvetica'),
            ('FONTNAME', (0, -1), (-1, -1), 'Helvetica-Bold'),
            ('FONTSIZE', (0, 0), (-1, -1), 9),
            ('BOTTOMPADDING', (0, 0), (-1, -1), 4),
            ('GRID', (0, 0), (-1, -1), 0.5, colors.grey),
            ('BOX', (0, 0), (-1, -1), 1, colors.black),
            ('LINEABOVE', (0, -1), (-1, -1), 2, colors.black),
        ]))
        
        story.append(ess_table)
        story.append(Spacer(1, 0.2*inch))
        
        # LIFESTYLE & MEDICAL HISTORY SECTION
        story.append(Paragraph("Lifestyle & Medical History", self.styles['SectionHeader']))
        
        lifestyle = data.get('lifestyle', {})
        medical = data.get('medical_history', {})
        
        lifestyle_data = [
            ['Smoking', 'Yes' if lifestyle.get('smoking') else 'No'],
            ['Alcohol Intake', 'Yes' if lifestyle.get('alcohol') else 'No'],
            ['Hypertension', 'Yes' if medical.get('hypertension') else 'No'],
            ['Diabetes', 'Yes' if medical.get('diabetes') else 'No']
        ]
        
        lifestyle_table = Table(lifestyle_data, colWidths=[2.5*inch, 3.5*inch])
        lifestyle_table.setStyle(TableStyle([
            ('ALIGN', (0, 0), (0, -1), 'LEFT'),
            ('ALIGN', (1, 0), (1, -1), 'LEFT'),
            ('FONTNAME', (0, 0), (0, -1), 'Helvetica-Bold'),
            ('FONTSIZE', (0, 0), (-1, -1), 9),
            ('BOTTOMPADDING', (0, 0), (-1, -1), 4),
            ('GRID', (0, 0), (-1, -1), 0.5, colors.grey),
            ('BOX', (0, 0), (-1, -1), 1, colors.black),
        ]))
        
        story.append(lifestyle_table)
        story.append(Spacer(1, 0.2*inch))
        
        # SHAP MODEL EXPLANATION SECTION
        if 'shap_chart' in data and data['shap_chart']:
            story.append(Paragraph("SHAP Model Explanation", self.styles['SectionHeader']))
            
            story.append(Paragraph(
                "SHAP values help quantify how much each feature contributed to the final sleep apnea risk prediction. "
                "Positive values increase risk, while lower values have less influence.",
                self.styles['Normal']
            ))
            
            story.append(Spacer(1, 0.1*inch))
            
            img = Image(data['shap_chart'], width=5*inch, height=3*inch)
            story.append(img)
            story.append(Spacer(1, 0.2*inch))
        
        # FOOTER
        story.append(Spacer(1, 0.3*inch))
        story.append(Paragraph(
            f"<i>Report generated on {data.get('generated_date', '')} by WakeUpCall Sleep Health System</i>",
            self.styles['Normal']
        ))
        
        # Build PDF
        doc.build(story)
        
        if output_path is None:
            buffer.seek(0)
            return buffer
        
        return None
