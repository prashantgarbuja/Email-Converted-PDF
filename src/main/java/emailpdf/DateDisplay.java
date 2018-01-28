package emailpdf;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.SqlDateModel;

/**
 * Created by Prashant on 12/07/2017.
 */

public class DateDisplay extends JFrame implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	private JDatePickerImpl datePicker;
	private JDatePickerImpl datePicker1;
	
	public DateDisplay() {
		super("Merchant Transaction Date");
		getContentPane().setLayout(null);
		
		//From Date 
		JLabel label = new JLabel("From Date :");
		label.setBounds(10, 13, 83, 14);
		getContentPane().add(label);
		
		SqlDateModel model = new SqlDateModel();
		model.setDate(2017, 1, 1); //This is a default Date in Calender. Note: month starts from 0.
		model.setSelected(true);
	
		Properties p = new Properties();
		p.put("text.today", "Today");
		p.put("text.month", "Month");
		p.put("text.year", "Year");
		
		JDatePanelImpl datePanel = new JDatePanelImpl(model,p);
		
		datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter()); //DateLabelFormatter 
		datePicker.setBounds(89, 9, 202, 23); //set position.
		
		getContentPane().add(datePicker);
		//End of From Date
		
		//To Date
		JLabel label1 = new JLabel("To Date : ");
		label1.setBounds(19, 71, 83, 14);
        	getContentPane().add(label1);
		 
		SqlDateModel model1 = new SqlDateModel();
		model1.setDate(2018, 1, 5);
		model1.setSelected(true);
		
		Properties p1 = new Properties();
		p1.put("text.today", "Today");
		p1.put("text.month", "Month");
		p1.put("text.year", "Year");
		
		JDatePanelImpl datePanel1 = new JDatePanelImpl(model1,p1);
		
		datePicker1 = new JDatePickerImpl(datePanel1, new DateLabelFormatter());
		datePicker1.setBounds(89, 62, 202, 23);
		
		getContentPane().add(datePicker1);
		//End of To Date
		
		//Create Submit Button.
		JButton btnSubmit = new JButton("Submit");
		btnSubmit.setBounds(118, 146, 89, 23);
		btnSubmit.addActionListener(this);
		getContentPane().add(btnSubmit);
		
		setSize(350, 250);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
				new DateDisplay().setVisible(true);
			}
		});
	}

	public void actionPerformed(ActionEvent event) {
		java.sql.Date selectedFromDate = (java.sql.Date) datePicker.getModel().getValue();
        java.sql.Date selectedToDate = (java.sql.Date) datePicker1.getModel().getValue();
       
        try {
		ConvertToPdf.query(selectedFromDate,selectedToDate); //call query method from ConvertTpPdf class.
		JOptionPane.showMessageDialog(this,"SUCCESSFULLY EXECUTED");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,"ERROR!!!");
		}
	}
}
